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

		// Ensure successfully authenticated
		response.assertResponse(200, this.mapper.writeValueAsString(new AuthenticateResponse(true, null)));

		// Ensure login created in store
		GoogleSignin user = this.obectify.get(GoogleSignin.class,
				(load) -> load.filter("email", "daniel@officefloor.net"));
		assertNotNull("Should have the user", user);
		assertEquals("Incorrect name", "Daniel Sagenschneider", user.getName());
		assertEquals("Incorrect photoUrl", "http://officefloor.net/photo.png", user.getPhotoUrl());
	}

}
