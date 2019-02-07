import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable( {
    providedIn: 'root'
} )
export class ServerApiService {

    // Direct to OfficeFloor default port when running in development
    private serverUrl: string = window.location.href.startsWith('http://localhost:4200') ? 'http://localhost:7878' : '';

    constructor( private http: HttpClient ) {
    }

    public authenticate( idToken: string ) {
        this.http.post( `${this.serverUrl}/authenticate`, {
            idToken: idToken
        } ).subscribe();
    }

}