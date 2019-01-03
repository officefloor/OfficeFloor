package net.officefloor.app.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import net.officefloor.app.subscription.Authenticate.AuthenticateRequest;
import net.officefloor.app.subscription.Authenticate.AuthenticateResponse;
import net.officefloor.app.subscription.rule.GoogleIdTokenRule;
import net.officefloor.app.subscription.rule.ObjectifyRule;
import net.officefloor.app.subscription.store.Domain;
import net.officefloor.app.subscription.store.GoogleSignin;
import net.officefloor.woof.mock.MockObjectResponse;

/**
 * Ensure can load authentication to the database.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticateTest {

	@Rule
	public GoogleIdTokenRule verifier = new GoogleIdTokenRule();

	@Rule
	public ObjectifyRule obectify = new ObjectifyRule(Domain.class, GoogleSignin.class);

	private MockObjectResponse<AuthenticateResponse> response = new MockObjectResponse<>();

	@Test
	public void ensureTokenRulePasses() throws Exception {

		// Create the mock token
		String token = this.verifier.getMockIdToken("1", "daniel@officefloor.net", "name", "Daniel Sagenschneider");

		// Verify the token
		GoogleIdToken idToken = this.verifier.getGoogleIdTokenVerifier().verify(token);
		assertNotNull("Should have token", idToken);

		// Ensure correct details
		assertEquals("Incorrect google Id", "1", idToken.getPayload().getSubject());
		assertEquals("Incorrect email", "daniel@officefloor.net", idToken.getPayload().getEmail());
		assertEquals("Incorrect name", "Daniel Sagenschneider", idToken.getPayload().get("name"));
	}

	@Test
	public void ensureUserCreated() throws Exception {

		// Undertake authentication
		String token = this.verifier.getMockIdToken("1", "daniel@officefloor.net", "email_verified", "true", "name",
				"Daniel Sagenschneider", "photoUrl", "http://officefloor.net/photo.png");
		new Authenticate().service(new AuthenticateRequest(token), this.verifier.getGoogleIdTokenVerifier(),
				this.obectify.ofy(), this.response);
		assertTrue("Should be successful", response.getObject().isSuccessful());

		// Ensure the user is loaded into the database
		GoogleSignin user = this.obectify.get(GoogleSignin.class,
				(load) -> load.filter("email", "daniel@officefloor.net"));

		// Ensure correct details
		assertUser(user, "1", "daniel@officefloor.net", "Daniel Sagenschneider", "http://officefloor.net/photo.png");
	}

	@Test
	public void ensureUserUpdated() throws Exception {

		// Create the existing user
		GoogleSignin user = new GoogleSignin("1", "daniel@officefloor.net");
		user.setName("Daniel Sagenschneider");
		user.setPhotoUrl("http://officefloor.net/photo.png");
		this.obectify.ofy().save().entity(user).now();

		// Undertake authentication
		String token = this.verifier.getMockIdToken("1", "changed@officefloor.net", "email_verified", "true", "name",
				"Changed Sagenschneider", "photoUrl", "http://officefloor.net/changed.png");
		new Authenticate().service(new AuthenticateRequest(token), this.verifier.getGoogleIdTokenVerifier(),
				this.obectify.ofy(), this.response);
		assertTrue("Should be successful", this.response.getObject().isSuccessful());

		// Ensure the user is loaded into the database
		user = this.obectify.get(GoogleSignin.class, (load) -> load.filter("email", "changed@officefloor.net"));
		assertNotNull("Should have user", user);

		// Ensure correct details
		assertUser(user, "1", "changed@officefloor.net", "Changed Sagenschneider",
				"http://officefloor.net/changed.png");
	}

	private static void assertUser(GoogleSignin user, String googleId, String email, String name, String photoUrl) {
		assertEquals("Incorrect google id", googleId, user.getGoogleId());
		assertEquals("Incorrect email", email, user.getEmail());
		assertEquals("Incorrect name", name, user.getName());
		assertEquals("Incorrect photo URL", photoUrl, user.getPhotoUrl());
	}

}