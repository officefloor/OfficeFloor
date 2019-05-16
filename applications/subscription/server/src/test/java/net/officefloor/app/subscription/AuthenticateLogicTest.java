package net.officefloor.app.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.objectify.Ref;

import net.officefloor.app.subscription.AuthenticateLogic.AuthenticateRequest;
import net.officefloor.app.subscription.AuthenticateLogic.AuthenticateResponse;
import net.officefloor.app.subscription.AuthenticateLogic.RefreshRequest;
import net.officefloor.app.subscription.AuthenticateLogic.RefreshResponse;
import net.officefloor.app.subscription.store.GoogleSignin;
import net.officefloor.app.subscription.store.User;
import net.officefloor.identity.google.mock.GoogleIdTokenRule;
import net.officefloor.nosql.objectify.mock.ObjectifyRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests integration of authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticateLogicTest {

	public static void setupUser(ObjectifyRule objectify, String name, String email, String photUrl) {
		User user = new User(email);
		user.setName(name);
		user.setPhotoUrl(photoUrl);
		objectify.store(user);
		GoogleSignin login = new GoogleSignin(googleId, email);
		login.setUser(Ref.create(user));
		login.setName(name);
		login.setPhotoUrl(photoUrl);
		objectify.save().entity(login).now();
	}

	private GoogleIdTokenRule verifier = new GoogleIdTokenRule();

	private ObjectifyRule obectify = new ObjectifyRule();

	private MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain chain = RuleChain.outerRule(this.verifier).around(this.obectify).around(this.server);

	private final ObjectMapper mapper = new ObjectMapper();

	private AuthenticateResponse authenticateResponse = null;

	@Test
	public void authenticate() throws Exception {

		// Undertake authentication
		String token = this.verifier.getMockIdToken("1", "daniel@officefloor.net", "email_verified", "true", "name",
				"Daniel Sagenschneider", "picture", "http://officefloor.net/photo.png");
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/authenticate").secure(true)
				.method(HttpMethod.POST).header("Content-Type", "application/json")
				.entity(this.mapper.writeValueAsString(new AuthenticateRequest(token))));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure login created in store
		GoogleSignin login = this.obectify.get(GoogleSignin.class,
				(load) -> load.filter("email", "daniel@officefloor.net"));
		assertNotNull("Should have the login", login);
		assertEquals("Incorrect name", "Daniel Sagenschneider", login.getName());
		assertEquals("Incorrect photoUrl", "http://officefloor.net/photo.png", login.getPhotoUrl());

		// Ensure user created in store
		User user = this.obectify.get(User.class, (load) -> load.filter("email", "daniel@officefloor.net"));
		assertEquals("Incorrect user", user.getId(), login.getUser().get().getId());
		assertEquals("Incorrect name", "Daniel Sagenschneider", user.getName());
		assertEquals("Incorrect photoUrl", "http://officefloor.net/photo.png", user.getPhotoUrl());

		// Ensure refresh and access token point to user
		String entity = response.getEntity(null);
		this.authenticateResponse = mapper.readValue(entity, AuthenticateResponse.class);
		assertNotNull("Should have refresh token", this.authenticateResponse.getRefreshToken());
		assertNotNull("Should have access token", this.authenticateResponse.getAccessToken());
	}

	@Test
	public void refreshAccessToken() throws Exception {

		// Authenticate (to get refresh token)
		this.authenticate();
		String refreshToken = this.authenticateResponse.getRefreshToken();

		// Refresh the token
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/refreshAccessToken").secure(true)
				.method(HttpMethod.POST).header("Content-Type", "application/json")
				.entity(this.mapper.writeValueAsString(new RefreshRequest(refreshToken))));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		RefreshResponse refreshResponse = mapper.readValue(response.getEntity(null), RefreshResponse.class);

		// As using same keys, should be same access token (times to second)
		assertEquals("Should be same token", this.authenticateResponse.getAccessToken(),
				refreshResponse.getAccessToken());
	}

}