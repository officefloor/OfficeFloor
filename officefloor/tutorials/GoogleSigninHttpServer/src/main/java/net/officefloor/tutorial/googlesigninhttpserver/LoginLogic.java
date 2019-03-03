package net.officefloor.tutorial.googlesigninhttpserver;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;

/**
 * Google Sign-in logic.
 * 
 * @author Daniel Sagenschneider
 */
public class LoginLogic {

	@HttpObject
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class LoginRequest {
		private String googleIdToken;
	}

	@Data
	@AllArgsConstructor
	public static class LoginResponse {
		private String email;
	}

	public void login(LoginRequest request, GoogleIdTokenVerifier tokenVerifier, ObjectResponse<LoginResponse> response)
			throws Exception {

		// Verify the token
		GoogleIdToken token = tokenVerifier.verify(request.getGoogleIdToken());

		// Send email response
		response.send(new LoginResponse(token.getPayload().getEmail()));
	}

}