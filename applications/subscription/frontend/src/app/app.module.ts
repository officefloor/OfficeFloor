import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SocialLoginModule, AuthServiceConfig } from "angularx-social-login";
import { GoogleLoginProvider, FacebookLoginProvider, LinkedInLoginProvider } from "angularx-social-login";
import { LoginComponent } from './login/login.component';

let config = new AuthServiceConfig( [
    {
        id: GoogleLoginProvider.PROVIDER_ID,
        provider: new GoogleLoginProvider( "443132781504-19vekci7r4t2qvqpbg9q1s32kjnp1c7t.apps.googleusercontent.com" )
    }
] );

export function provideConfig() {
    return config;
}

@NgModule( {
    declarations: [
        AppComponent,
        LoginComponent
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        SocialLoginModule,
        AppRoutingModule
    ],
    providers: [{
        provide: AuthServiceConfig,
        useFactory: provideConfig
    }],
    bootstrap: [AppComponent]
} )
export class AppModule { }
