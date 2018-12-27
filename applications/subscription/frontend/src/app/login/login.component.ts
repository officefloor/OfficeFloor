import { Component, OnInit } from '@angular/core';
import { SocialUser } from "angularx-social-login";
import { LoginService, LoginListener } from '../login.service';
import { GoogleLoginProvider } from "angularx-social-login";

@Component( {
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
} )
export class LoginComponent implements OnInit {

    constructor( private loginService: LoginService ) { }

    private user: SocialUser;

    ngOnInit() {
        this.loginService.subscribe(( user: SocialUser ) => {
            this.user = user;
        } );
    }

    signIn(): void {
        this.loginService.signIn();
    }

    signOut(): void {
        this.loginService.signOut();
    }

}