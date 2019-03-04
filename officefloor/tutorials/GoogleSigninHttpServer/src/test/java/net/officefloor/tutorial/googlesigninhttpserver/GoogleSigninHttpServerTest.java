package net.officefloor.tutorial.googlesigninhttpserver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.identity.google.mock.GoogleIdTokenRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.googlesigninhttpserver.LoginLogic.LoginRequest;
import net.officefloor.tutorial.googlesigninhttpserver.LoginLogic.LoginResponse;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Google Sign-in HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleSigninHttpServerTest {

	// START SNIPPET: tutorial
	private GoogleIdTokenRule googleSignin = new GoogleIdTokenRule();

	private MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain order = RuleChain.outerRule(this.googleSignin).around(this.server);

	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void ensureLogin() throws Exception {

		// Create mock token
		String token = this.googleSignin.getMockIdToken("TEST", "mock@officefloor.net");

		// Ensure can login
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/login").method(HttpMethod.POST)
				.header("Content-Type", "application/json").entity(mapper.writeValueAsString(new LoginRequest(token))));
		response.assertResponse(200, mapper.writeValueAsString(new LoginResponse("mock@officefloor.net")));
	}
	// END SNIPPET: tutorial

}