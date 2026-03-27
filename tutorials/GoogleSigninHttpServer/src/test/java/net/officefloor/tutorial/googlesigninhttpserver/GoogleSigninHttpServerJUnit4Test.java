package net.officefloor.tutorial.googlesigninhttpserver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import net.officefloor.identity.google.mock.GoogleIdTokenRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.tutorial.googlesigninhttpserver.LoginLogic.LoginRequest;
import net.officefloor.tutorial.googlesigninhttpserver.LoginLogic.LoginResponse;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Google Sign-in HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleSigninHttpServerJUnit4Test {

	// START SNIPPET: tutorial
	private final GoogleIdTokenRule googleSignin = new GoogleIdTokenRule();

	private final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public final RuleChain order = RuleChain.outerRule(this.googleSignin).around(this.server);

	@Test
	public void ensureLogin() throws Exception {

		// Create mock token
		String token = this.googleSignin.getMockIdToken("TEST", "mock@officefloor.net");

		// Ensure can login
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/login", new LoginRequest(token)));
		response.assertJson(200, new LoginResponse("mock@officefloor.net"));
	}
	// END SNIPPET: tutorial

}