package net.officefloor.app.subscription;

import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import lombok.Data;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpObject;

/**
 * Provides authentication.
 */
public class Authenticate {

	private static HttpTransport transport = new NetHttpTransport();

	private static JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

	private static GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
			.setAudience(Collections
					.singletonList("443132781504-19vekci7r4t2qvqpbg9q1s32kjnp1c7t.apps.googleusercontent.com"))
			.build();

	@Data
	@HttpObject
	public static class Authentication {
		String idToken;
	}

	public void service(Authentication authentication, ServerHttpConnection connection) throws Exception {

		// Verify token
		GoogleIdToken token = verifier.verify(authentication.getIdToken());

		// Provide details
		Payload payload = token.getPayload();
		System.out.println(
				"User: " + payload.get("name") + " (" + payload.getSubject() + " - " + payload.getEmail() + ")");

		// TODO create user in database

		// Send response
		connection.getResponse().getEntityWriter().write("{}");
	}

}