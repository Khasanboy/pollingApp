package uz.poll.controller;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import uz.poll.exception.BadRequestException;
import uz.poll.exception.ResourceNotFoundException;
import uz.poll.model.Choice;
import uz.poll.model.ChoiceVoteCount;
import uz.poll.model.Poll;
import uz.poll.model.User;
import uz.poll.model.Vote;
import uz.poll.payload.ApiResponse;
import uz.poll.payload.PagedResponse;
import uz.poll.payload.PollRequest;
import uz.poll.payload.PollResponse;
import uz.poll.payload.VoteRequest;
import uz.poll.repository.PollRepository;
import uz.poll.repository.UserRepository;
import uz.poll.repository.VoteRepository;
import uz.poll.security.CurrentUser;
import uz.poll.security.UserPrincipal;
import uz.poll.service.PollService;
import uz.poll.util.AppConstants;
import uz.poll.util.ModelMapper;

@RestController
@RequestMapping("/api/polls")
public class PollController {

	@Autowired
	private PollRepository pollRepository;

	@Autowired
	private VoteRepository voteRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PollService pollService;

	@GetMapping
	public PagedResponse<PollResponse> getPolls(@CurrentUser UserPrincipal currentUser,
			@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
			@RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

		return pollService.getAllPolls(currentUser, page, size);

	}

	@PostMapping
	@PreAuthorize("hasRole('User')")
	public ResponseEntity<?> createPoll(@Valid @RequestBody PollRequest pollRequest) {
		Poll poll = new Poll();
		poll.setQuestion(pollRequest.getQuestion());

		pollRequest.getChoices().forEach(choiceRequest -> {

			poll.addChoice(new Choice(choiceRequest.getText()));
		});

		Instant now = Instant.now();
		Instant expirationDateTime = now.plus(Duration.ofDays(pollRequest.getPollLength().getDays()))
				.plus(Duration.ofHours(pollRequest.getPollLength().getHours()));

		poll.setExpirationDateTime(expirationDateTime);

		Poll result = pollRepository.save(poll);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{pollId}").buildAndExpand(result.getId())
				.toUri();

		return ResponseEntity.created(location).body(new ApiResponse(true, "Poll created successfully"));

	}

	@GetMapping("/{pollId}")
	public PollResponse getPollById(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		// Retrieve Vote Counts of every choice belonging to the current poll
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

		// Retrieve poll creator details
		User creator = userRepository.findById(poll.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));

		// Retrieve vote done by logged in user
		Vote userVote = null;
		if (currentUser != null) {
			userVote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);

		}

		return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator,
				userVote != null ? userVote.getChoice().getId() : null);

	}

	@PostMapping("/{pollId}/votes")
	@PreAuthorize("hasRole('USER')")
	public PollResponse castVote(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId,
			@Valid @RequestBody VoteRequest voteRequest) {

		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		if (poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}

		User user = userRepository.getOne(currentUser.getId());

		Choice selectedChoice = poll.getChoices().stream()
				.filter(choice -> choice.getId().equals(voteRequest.getChoiceId())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));

		Vote vote = new Vote();
		vote.setPoll(poll);
		vote.setUser(user);
		vote.setChoice(selectedChoice);

		vote = voteRepository.save(vote);

		// -- Vote Saved, Return the updated Poll Response now --

		// Retrieve Vote Counts of every choice belonging to the current poll
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

		// Retrieve poll creator details
		User creator = userRepository.findById(poll.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));

		return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, vote.getChoice().getId());

	}

}
