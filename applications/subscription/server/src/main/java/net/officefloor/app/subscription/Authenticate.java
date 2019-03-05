package net.officefloor.app.subscription;

import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Ref;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.app.subscription.jwt.JwtCredentials;
import net.officefloor.app.subscription.store.GoogleSignin;
import net.officefloor.app.subscription.store.User;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.jwt.authority.JwtAuthority;

/**
 * Provides authentication.
 */
public class Authenticate {

	@Data
	@HttpObject
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AuthenticateRequest {
		private String idToken;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AuthenticateResponse {
		private String refreshToken;
		private String accessToken;
	}

	public void authenticate(AuthenticateRequest idTokenInput, GoogleIdTokenVerifier verifier, Objectify objectify,
			JwtAuthority<JwtCredentials> authority, ObjectResponse<AuthenticateResponse> response) throws Exception {

		// Verify token
		GoogleIdToken token = verifier.verify(idTokenInput.getIdToken());
		if (token == null) {
			throw new HttpException(HttpStatus.UNAUTHORIZED);
		}

		// Ensure using verified email
		Payload payload = token.getPayload();
		Boolean emailVerified = payload.getEmailVerified();
		if ((emailVerified == null) || (!emailVerified)) {
			throw new HttpException(HttpStatus.FORBIDDEN, new IllegalStateException("Must verify your email"));
		}

		// Obtain the user details
		String googleId = payload.getSubject();
		String email = payload.getEmail();
		String name = payload.get("name").toString();
		String photoUrl = payload.get("picture").toString();

		// Determine if the user exists
		List<GoogleSignin> logins = objectify.load().type(GoogleSignin.class).filter("googleId", googleId).list();

		// Update or create the user
		User loggedInUser = objectify.transact(() -> {
			GoogleSignin login;
			User user;
			if (logins.size() > 0) {
				// Update the existing user
				login = logins.get(0);
				login.setEmail(email);
				login.setName(name);
				login.setPhotoUrl(photoUrl);
				user = login.getUser().get();
				user.setEmail(login.getEmail());
				user.setName(name);
				user.setPhotoUrl(photoUrl);
				objectify.save().entities(user, login).now();

			} else {
				// Load the new user
				user = new User(email);
				user.setName(name);
				user.setPhotoUrl(photoUrl);
				objectify.save().entity(user).now();
				login = new GoogleSignin(googleId, email);
				login.setUser(Ref.create(user));
				login.setName(name);
				login.setPhotoUrl(photoUrl);
				objectify.save().entity(login).now();
			}
			return user;
		});

		// Create the JWT refresh and access token
		JwtCredentials credentials = new JwtCredentials(loggedInUser.getId());
		String refreshToken = authority.createRefreshToken(credentials);
		String accessToken = authority.createAccessToken(credentials);

		// Send back the tokens
		response.send(new AuthenticateResponse(refreshToken, accessToken));
	}

	@Data
	@HttpObject
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RefreshRequest {
		private String refreshToken;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RefreshResponse {
		private String accessToken;
	}

	public void refreshAccessToken(RefreshRequest refreshRequest, JwtAuthority<JwtCredentials> authority,
			ObjectResponse<RefreshResponse> response) throws Exception {

		// Obtain the JWT credentials
		JwtCredentials credentials = authority.decodeRefreshToken(refreshRequest.getRefreshToken());

		// Create new access token
		String accessToken = authority.createAccessToken(credentials);

		// Send back the access token
		response.send(new RefreshResponse(accessToken));
	}

}