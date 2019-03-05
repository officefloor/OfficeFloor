package net.officefloor.app.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.app.subscription.Authenticate.AuthenticateRequest;
import net.officefloor.app.subscription.Authenticate.AuthenticateResponse;
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
public class AuthenticateTest {

	private GoogleIdTokenRule verifier = new GoogleIdTokenRule();

	private ObjectifyRule obectify = new ObjectifyRule();

	private MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain chain = RuleChain.outerRule(this.verifier).around(this.obectify).around(this.server);

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void authenticate() throws Exception {

		// Undertake authentication
		String token = this.verifier.getMockIdToken("1", "daniel@officefloor.net", "email_verified", "true", "name",
				"Daniel Sagenschneider", "picture", "http://officefloor.net/photo.png");
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/authenticate").method(HttpMethod.POST)
				.header("Content-Type", "application/json")
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
		AuthenticateResponse authenticateResponse = mapper.readValue(entity, AuthenticateResponse.class);
		assertNotNull("Should have refresh token", authenticateResponse.getRefreshToken());
		assertNotNull("Should have access token", authenticateResponse.getAccessToken());
	}

}