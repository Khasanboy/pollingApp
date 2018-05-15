import { User } from './../../models/user';
import { AuthService } from './../../services/auth.service';
import { ValidationService } from './../../services/validation.service';
import { Component, OnInit } from '@angular/core';
import { FlashMessagesService } from 'angular2-flash-messages';
import { send } from 'q';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  name: string;
  username: string;
  email: string;
  password: string;

  constructor(private validationService: ValidationService,
    private flashMessagesService: FlashMessagesService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
  }

  register() {
    const user: User = new User(this.name, this.username, this.email, this.password);

    if (!this.validationService.validateRegister(user)) {
      console.log("fuck off")
      this.flashMessagesService.show("fuck off")
      return false;
    }

    if (!this.validationService.validateEmail(user.email)) {
      console.log("email fucked")
      this.flashMessagesService.show('email fucked', { cssClass: 'alert-danger', timeout: 3000 })
      return false;
    }

    console.log("sending " + user.email);
    this.authService.registerUser(user).subscribe(
      res => {
        this.flashMessagesService.show(res['message'] +". Please log in", { cssClass: 'alert-success', timeout: 10000 })
        this.router.navigateByUrl('/login');
      },
      error => {
        this.flashMessagesService.show("Unexpected error happened", { cssClass: 'alert-danger', timeout: 10000 })
        console.log(error)
        this.router.navigateByUrl("/register")
      }

    );

  }

}
