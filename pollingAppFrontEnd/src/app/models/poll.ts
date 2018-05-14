import { User } from "./user";

export class Poll{
    createdByName:User;
    name:string;
    votes:number;
    timeLeft: string;

    constructor(user, name, votes, timeLeft){
        this.createdByName = user;
        this.name = name;
        this.votes = votes;
        this.timeLeft = timeLeft;
    }

}