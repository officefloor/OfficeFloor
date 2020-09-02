package net.officefloor.tutorial.googlesigninhttpserver;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.identity.google.mock.GoogleIdTokenExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.tutorial.googlesigninhttpserver.LoginLogic.LoginRequest;
import net.officefloor.tutorial.googlesigninhttpserver.LoginLogic.LoginResponse;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the Google Sign-in HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleSigninHttpServerTest {

	// START SNIPPET: tutorial
	@Order(1)
	@RegisterExtension
	public final GoogleIdTokenExtension googleSignin = new GoogleIdTokenExtension();

	@Order(2)
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

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