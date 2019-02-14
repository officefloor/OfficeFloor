package net.officefloor.tutorial.jwthttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.jwthttpserver.JwtTokens.Credentials;
import net.officefloor.tutorial.jwthttpserver.JwtTokens.Token;
import net.officefloor.tutorial.jwthttpserver.JwtTokens.Tokens;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the JWT HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtHttpServerTest {

	private static ObjectMapper mapper = new ObjectMapper();

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	private String refreshToken;

	@Test
	public void testEnsureResourceSecured() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/resource").secure(true));
		response.assertResponse(401, "");
	}

	@Test
	public void testLoginAndAccessSecureResource() throws Exception {

		// Undertake login
		Credentials credentials = new Credentials("daniel", "daniel");
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/login").secure(true).method(HttpMethod.POST)
						.header("Content-Type", "application/json").entity(mapper.writeValueAsString(credentials)));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Extract the access token
		Tokens tokens = mapper.readValue(response.getEntity(), Tokens.class);
		assertNotNull("Should have access token", tokens.getAccessToken());
		assertNotNull("Should have refresh token", tokens.getRefreshToken());
		this.refreshToken = tokens.getRefreshToken();

		// Access the secured resource
		response = this.server.send(MockHttpServer.mockRequest("/resource").secure(true).header("authorization",
				"Bearer " + tokens.getAccessToken()));
		response.assertResponse(200, "Hello JWT secured World");
	}

	@Test
	public void testRefreshTokenToAccessSecureResource() throws Exception {

		// Undertake login and access with original access token
		this.testLoginAndAccessSecureResource();

		// Obtain new access token
		Token refreshToken = new Token(this.refreshToken);
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/refresh").secure(true).method(HttpMethod.POST)
						.header("Content-Type", "application/json").entity(mapper.writeValueAsString(refreshToken)));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Extract the access token
		Token token = mapper.readValue(response.getEntity(), Token.class);
		assertNotNull("Should have access token", token.getToken());

		// Access the secured resource with refreshed access token
		response = this.server.send(MockHttpServer.mockRequest("/resource").secure(true).header("authorization",
				"Bearer " + token.getToken()));
		response.assertResponse(200, "Hello JWT secured World");
	}

}