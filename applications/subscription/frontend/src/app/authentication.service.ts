import { Injectable } from '@angular/core';
import { AuthService, SocialUser } from "angularx-social-login";
import { GoogleLoginProvider } from "angularx-social-login";
import { ServerApiService, AuthenticateResponse, AccessTokenResponse } from './server-api.service';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable( {
    providedIn: 'root'
} )
export class AuthenticationService {

    // Login state
    private state: BehaviorSubject<SocialUser> = new BehaviorSubject<SocialUser>( null );

    constructor(
        private authService: AuthService,
        private serverApiService: ServerApiService
    ) {
        // Initiate login
        this.authService.authState.subscribe(( user: SocialUser ) => {

            // Notify auth token
            if ( user != null ) {

                // Inform server of login
                this.serverApiService.authenticate( user.idToken ).subscribe(( response: AuthenticateResponse ) => {

                    // Capture the tokens
                    const refreshToken: string = response.refreshToken;
                    const accessToken: string = response.accessToken;

                    // Store the tokens
                    localStorage.setItem( 'refreshToken', refreshToken );
                    localStorage.setItem( 'accessToken', accessToken );

                    // Notify logged in
                    this.state.next( user );
                } )

            } else {
                // Notify of logout
                this.state.next( user );
            }
        } );
    }

    public signIn(): void {
        this.authService.signIn( GoogleLoginProvider.PROVIDER_ID );
    }

    public signOut(): void {

        // Clear the tokens
        localStorage.removeItem( 'refreshToken' );
        localStorage.removeItem( 'accessToken' );

        // Google sign-out
        this.authService.signOut();
    }

    public refreshAccessToken(): Observable<AccessTokenResponse> {

        // Obtain the refresh token
        const refreshToken: string = localStorage.getItem( 'refreshToken' );
        if ( !refreshToken ) {
            return of( null ); // no refresh token so no access token
        }

        // Undertake refreshing the access token
        return this.serverApiService.refreshAccessToken( refreshToken ).pipe( map(( response: AccessTokenResponse ) => {

            // Capture the new access token
            localStorage.setItem( 'accessToken', response.accessToken );

            // Return the response
            return response;
        } ) );
    }

    public getAccessToken(): string {
        return localStorage.getItem( 'accessToken' );
    }

    public authenticationState(): Observable<SocialUser> {
        return this.state;
    }

}