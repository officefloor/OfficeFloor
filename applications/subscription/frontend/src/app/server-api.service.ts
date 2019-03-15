import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable( {
    providedIn: 'root'
} )
export class ServerApiService {

    // Direct to OfficeFloor default port when running in development
    private serverUrl: string = window.location.href.startsWith( 'http://localhost:4200' ) ? 'http://localhost:8080' : '';

    constructor( private http: HttpClient ) {
    }

    public authenticate( idToken: string ): Observable<AuthenticateResponse> {
        return this.http.post<AuthenticateResponse>( `${this.serverUrl}/authenticate`, {
            idToken: idToken
        } )
    }

    public refreshAccessToken( refreshToken: string ): Observable<AccessTokenResponse> {
        return this.http.post<AccessTokenResponse>( `${this.serverUrl}/refreshAccessToken`, {
            refreshToken: refreshToken
        } )
    }

}

export interface AuthenticateResponse {
    refreshToken: string;
    accessToken: string;
}

export interface AccessTokenResponse {
    accessToken: string;
}