export class User{
    name: string;
    username:string;
    email:string;
    password:string;

    constructor(name, username, email, password){
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}