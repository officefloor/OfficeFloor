package net.officefloor.tutorial.jwtresourcehttpserver;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.jwt.mock.MockJwtAccessTokenExtension;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the JWT Resource HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class JwtResourceHttpServerTest {

	// Sets up server to accept created JWT access tokens for testing
	@Order(1)
	@RegisterExtension
	public final MockJwtAccessTokenExtension authority = new MockJwtAccessTokenExtension();

	@Order(2)
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void ensureResourceSecured() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/resource").secure(true));
		response.assertResponse(401, "");
	}

	@Test
	public void accessSecureResource() throws Exception {

		// Create mock access token
		String accessToken = this.authority.createAccessToken(new Claims("daniel", new String[] { "tutorial" }));

		// Access the secured resource
		MockHttpResponse response = this.server.send(
				MockHttpServer.mockRequest("/resource").secure(true).header("authorization", "Bearer " + accessToken));
		response.assertResponse(200, "Hello daniel");
	}

}
// END SNIPPET: tutorial