import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable( {
    providedIn: 'root'
} )
export class ServerApiService {

    private serverUrl: string = 'http://localhost:7878';

    constructor( private http: HttpClient ) {
    }

    public authenticate( idToken: string ) {
        this.http.post( `${this.serverUrl}/authenticate`, {
            idToken: idToken
        } ).subscribe();
    }

}