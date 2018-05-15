import { Router } from '@angular/router';
import { FlashMessagesService } from 'angular2-flash-messages';
import { AuthService } from './../../services/auth.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  username: string;
  password: string;

  constructor(private authService: AuthService,
              private flashMessagesService: FlashMessagesService,
              private router: Router            
  ) { }

  ngOnInit() {
  }

  login(){
    const user = {
      usernameOrEmail: this.username,
      password: this.password
    }

    this.authService.loginUser(user).subscribe(
      res => {
        console.log(res);
        this.authService.storeToken(res['accessToken']);
        this.flashMessagesService.show('Logging in was successfull', {cssClass: 'alert-success text-center', timeout:4000});
        this.router.navigate(['/']);
      },
      error => {
        console.log(error);
        this.flashMessagesService.show('Username or password is incorrect', {cssClass: 'alert-danger text-center', timeout: 5000});
        this.router.navigate(['login']);
      }
    )
  }

}
