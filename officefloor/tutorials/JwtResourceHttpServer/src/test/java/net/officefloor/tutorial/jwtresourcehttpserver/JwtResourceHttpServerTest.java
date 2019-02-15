package net.officefloor.tutorial.jwtresourcehttpserver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.jwt.mock.MockJwtAccessTokenRule;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the JWT Resource HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtResourceHttpServerTest {

	public MockJwtAccessTokenRule authority = new MockJwtAccessTokenRule();

	public MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain orderedRules = RuleChain.outerRule(this.authority).around(this.server);

	@Test
	public void ensureResourceSecured() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/resource").secure(true));
		response.assertResponse(401, "");
	}

	@Test
	public void accessSecureResource() throws Exception {

		// Create access token
		String accessToken = this.authority.createAccessToken(new Claims("daniel", new String[] { "tutorial" }));

		// Access the secured resource
		MockHttpResponse response = this.server.send(
				MockHttpServer.mockRequest("/resource").secure(true).header("authorization", "Bearer " + accessToken));
		response.assertResponse(200, "Hello JWT secured World");
	}

}