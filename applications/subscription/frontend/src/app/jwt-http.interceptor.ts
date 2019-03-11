import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpResponse, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, switchMap, filter, take, finalize, catchError } from 'rxjs/operators';
import { AuthenticationService } from './authentication.service';
import { AccessTokenResponse } from './server-api.service';

@Injectable()
export class JwtHttpInterceptor implements HttpInterceptor {

    // Enable only one refresh access token request
    private refreshedAccessToken: BehaviorSubject<string> = null;

    constructor(
        private authenticationService: AuthenticationService
    ) { }

    intercept( req: HttpRequest<any>, next: HttpHandler ): Observable<HttpEvent<any>> {

        // Create request with JWT
        const jwtRequest: ( req: HttpRequest<any>, accessToken: string ) => HttpRequest<any> = ( req: HttpRequest<any>, accessToken: string ) => {
            if ( accessToken ) {
                return req.clone( {
                    setHeaders: {
                        Authorization: `Bearer ${accessToken}`
                    }
                } )
            } else {
                return req;
            }
        };

        // Undertake the request
        return next.handle( jwtRequest( req, this.authenticationService.getAccessToken() ) ).pipe(

            // Handle failures
            catchError(( error ) => {

                // Log failure
                console.error( "HttpRequest: " + JSON.stringify( req, null, 2 ) + '\n\nError: ' + JSON.stringify( error, null, 2 ) );

                // Handle possible failure
                if ( error instanceof HttpErrorResponse ) {
                    const response: HttpErrorResponse = error;

                    // Determine if expired access token
                    if ( response.status === 401 ) {

                        // Do not refresh if authenticate/refresh access token requests
                        if ( req.url.includes( '/authenticate' ) || req.url.includes( '/refreshAccessToken' ) ) {
                            return throwError( error );
                        }

                        // Determine if refreshing access token
                        if ( !this.refreshedAccessToken ) {

                            // Create the refresh to block multiple requests
                            this.refreshedAccessToken = new BehaviorSubject<string>( null );

                            // Attempt to refresh access token
                            let isRefreshed: boolean = false;
                            this.authenticationService.refreshAccessToken().pipe(
                                map(( refreshResponse: AccessTokenResponse ) => {

                                    // Notify of refreshed access token
                                    if ( refreshResponse.accessToken ) {
                                        this.refreshedAccessToken.next( refreshResponse.accessToken );
                                        isRefreshed = true;
                                    }

                                    return refreshResponse;
                                } ),
                                finalize(() => {
                                    // Clear to allow refresh again
                                    // Note; if failure then existing requests are not made
                                    this.refreshedAccessToken = null;

                                    // Determine if refreshed
                                    if ( !isRefreshed ) {
                                        // Not refreshed, so log out
                                        this.authenticationService.signOut();
                                    }
                                } )
                            ).subscribe();
                        }

                        // Wait on refreshed access token
                        return this.refreshedAccessToken.pipe(
                            filter(( token ) => token != null ),
                            take( 1 ),
                            switchMap(( accessToken: string ) => {

                                // Retry with the refresh access token
                                return next.handle( jwtRequest( req, accessToken ) );
                            } )
                        );
                    }
                }

                // Propagate the failure
                return throwError( error );
            } ),

            // Log the response
            map(( event: HttpEvent<any> ) => {
                if ( event instanceof HttpResponse ) {
                    console.log( "HttpRequest: " + JSON.stringify( req, null, 2 ) + "\n\nHttpResponse: " + JSON.stringify( event, null, 2 ) );
                }
                return event;
            } )
        );
    }

}