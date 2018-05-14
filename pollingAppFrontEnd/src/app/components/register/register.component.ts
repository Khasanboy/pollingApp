import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  name:string;
  username:string;
  email:string;
  password:string;

  constructor() { }

  ngOnInit() {
  }

  register(){
    const user={
      name:this.name,
      username: this.username,
      email:this.email,
      password: this.password
    }
  }

}
