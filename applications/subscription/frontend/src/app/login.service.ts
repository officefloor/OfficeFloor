import { Injectable } from '@angular/core';
import { AuthService, SocialUser } from "angularx-social-login";
import { GoogleLoginProvider } from "angularx-social-login";
import { ServerApiService } from './server-api.service';

export type LoginListener = ( user: SocialUser ) => void;

@Injectable( {
    providedIn: 'root'
} )
export class LoginService {

    private user: SocialUser = null;
    private listeners: LoginListener[] = [];

    constructor(
        private authService: AuthService,
        private serverApiService: ServerApiService
    ) {
        this.authService.authState.subscribe(( user: SocialUser ) => {

            // Capture the user
            this.user = user;

            // Notify auth token
            if ( this.user != null ) {
                this.serverApiService.authenticate( this.user.idToken );
            }

            // Notify the users
            this.listeners.forEach(( listener: LoginListener ) => {
                listener( this.user );
            } )
        } );
    }

    public signIn(): void {
        this.authService.signIn( GoogleLoginProvider.PROVIDER_ID );
    }

    public signOut(): void {
        this.authService.signOut();
    }

    public subscribe( listener: LoginListener ) {
        // Set initial state
        listener( this.user );

        // Register for further state
        this.listeners.push( listener );
    }
}