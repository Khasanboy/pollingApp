package uz.poll.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uz.poll.exception.ResourceNotFoundException;
import uz.poll.model.User;
import uz.poll.payload.PagedResponse;
import uz.poll.payload.PollResponse;
import uz.poll.payload.UserIdentityAvailability;
import uz.poll.payload.UserProfile;
import uz.poll.payload.UserSummary;
import uz.poll.repository.PollRepository;
import uz.poll.repository.UserRepository;
import uz.poll.repository.VoteRepository;
import uz.poll.security.CurrentUser;
import uz.poll.security.UserPrincipal;
import uz.poll.service.PollService;
import uz.poll.util.AppConstants;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PollRepository pollRepository;
	
	@Autowired
	private VoteRepository voteRepository;
	
	@Autowired
	private PollService pollService;
	
	@GetMapping("/user/me")
	@PreAuthorize("hasRole('User')")
	public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
		
		UserSummary userSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
		
		return userSummary;
	}
	
	@GetMapping("/user/checkUsernameAvailability")
	public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
		
		Boolean isAvailable = !userRepository.existsByUsername(username);
		
		return new UserIdentityAvailability(isAvailable);
		
	}
	
	@GetMapping("/user/checkEmailAvailability")
	public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
		
		Boolean isAvailable = !userRepository.existsByEmail(email);
		
		return new UserIdentityAvailability(isAvailable);
		
	}
	
	@GetMapping("/username/{username}")
	public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
		
		User user = userRepository.findByUsername(username)
				.orElseThrow(()-> new ResourceNotFoundException("User", "username", username));
		
		long pollCount = pollRepository.countByCreatedBy(user.getId());
		long voteCount = voteRepository.countByUserId(user.getId());
		
		UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(), pollCount, voteCount);
		
		return userProfile;		
		
	}
	
	@GetMapping("/users/{username}/polls")
	public PagedResponse<PollResponse> getPollsCreatedBy(@PathVariable(value = "username") String username,
														 @CurrentUser UserPrincipal currentUser,
														 @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
														 @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size){
		
		return pollService.getPollsCreatedBy(username, currentUser, page, size);
		
	}
	
	
	@GetMapping("/users/{username}/votes")
	public PagedResponse<PollResponse> getPollsVoteBy(@PathVariable(value = "username") String username,
													   @CurrentUser UserPrincipal currentUser,
													   @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
													   @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size){
		
		return pollService.getPollsVotedBy(username, currentUser, page, size);
		
	}
	
	
}
