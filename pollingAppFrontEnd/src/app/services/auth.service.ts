import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  authToken:any;
  user:any;
  constructor(private http: HttpClient) { }

  registerUser(user){
    let headers = new HttpHeaders();
    headers.append('Content-Type', 'application/json');
    return this.http.post('api/auth/signup', user, {headers: headers});
      
  }

  loginUser(user){
    let headers = new HttpHeaders();
    headers.append('Content-Type', 'application/json');
    return this.http.post('api/auth/signin', user, {headers: headers});
  }

  logout(){
    this.authToken = null;
    this.user = null;
    localStorage.clear();
  }

  storeToken(token){
    localStorage.setItem('token', token);
    this.authToken = token;
  }

  saveUser(user){
    this.user = user;
  }
}
