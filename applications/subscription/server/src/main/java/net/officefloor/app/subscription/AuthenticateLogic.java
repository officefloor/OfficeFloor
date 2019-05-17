package net.officefloor.app.subscription;

import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Ref;

import lombok.Value;
import net.officefloor.app.subscription.store.GoogleSignin;
import net.officefloor.app.subscription.store.User;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.jwt.authority.JwtAuthority;

/**
 * Provides authentication.
 */
public class AuthenticateLogic {

	@Value
	@HttpObject
	public static class AuthenticateRequest {
		private String idToken;
	}

	@Value
	public static class AuthenticateResponse {
		private String refreshToken;
		private String accessToken;
	}

	public void authenticate(AuthenticateRequest idTokenInput, GoogleIdTokenVerifier verifier, Objectify objectify,
			JwtAuthority<User> authority, ObjectResponse<AuthenticateResponse> response,
			ServerHttpConnection connection) throws Exception {

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

			// Ensure login exists
			GoogleSignin login;
			User user;
			if (logins.size() > 0) {
				// Update the existing user
				login = logins.get(0);
				login.setEmail(email);
				login.setName(name);
				login.setPhotoUrl(photoUrl);

				// Determine the user
				boolean isNewUser;
				if (login.getUser() == null) {
					user = new User(email);
					isNewUser = true;
				} else {
					user = login.getUser().get();
					isNewUser = false;
				}
				user.setEmail(login.getEmail());
				user.setName(name);
				user.setPhotoUrl(photoUrl);
				if (isNewUser) {
					// Save the user (to obtain identifier)
					objectify.save().entities(user).now();
					login.setUser(Ref.create(user));
					objectify.save().entities(login).now();

				} else {
					// Save updates to existing
					objectify.save().entities(user, login).now();
				}

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
		String refreshToken = authority.createRefreshToken(loggedInUser);
		String accessToken = authority.createAccessToken(loggedInUser);

		// Send back the tokens
		response.send(new AuthenticateResponse(refreshToken, accessToken));
	}

	@Value
	@HttpObject
	public static class RefreshRequest {
		private String refreshToken;
	}

	@Value
	public static class RefreshResponse {
		private String accessToken;
	}

	public void refreshAccessToken(RefreshRequest refreshRequest, JwtAuthority<User> authority,
			ObjectResponse<RefreshResponse> response) throws Exception {

		// Obtain the user
		User user = authority.decodeRefreshToken(refreshRequest.getRefreshToken());

		// Create new access token
		String accessToken = authority.createAccessToken(user);

		// Send back the access token
		response.send(new RefreshResponse(accessToken));
	}

}