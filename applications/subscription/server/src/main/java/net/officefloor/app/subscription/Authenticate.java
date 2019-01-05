package net.officefloor.app.subscription;

import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.googlecode.objectify.Objectify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.app.subscription.store.GoogleSignin;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;

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
	public static class AuthenticateResponse {
		private final boolean isSuccessful;
		private final String error;
	}

	public void service(AuthenticateRequest idTokenInput, GoogleIdTokenVerifier verifier, Objectify objectify,
			ObjectResponse<AuthenticateResponse> response) throws Exception {

		// Verify token
		GoogleIdToken token = verifier.verify(idTokenInput.getIdToken());
		if (token == null) {
			throw new HttpException(HttpStatus.UNAUTHORIZED);
		}

		// Ensure using verified email
		Payload payload = token.getPayload();
		Boolean emailVerified = payload.getEmailVerified();
		if ((emailVerified == null) || (!emailVerified)) {
			response.send(new AuthenticateResponse(false, "Please verify your email"));
			return;
		}

		// Obtain the user details
		String googleId = payload.getSubject();
		String email = payload.getEmail();
		String name = payload.get("name").toString();
		String photoUrl = payload.get("picture").toString();

		// Determine if the user exists
		List<GoogleSignin> users = objectify.load().type(GoogleSignin.class).filter("googleId", googleId).list();

		// Update or create the user
		GoogleSignin user;
		if (users.size() > 0) {
			// Update the existing user
			user = users.get(0);
			user.setEmail(email);

		} else {
			// Load the new user
			user = new GoogleSignin(googleId, email);
		}
		user.setName(name);
		user.setPhotoUrl(photoUrl);
		objectify.save().entity(user).now();

		// Indicate successfully authenticated
		response.send(new AuthenticateResponse(true, null));
	}

}