package net.officefloor.tutorial.jwtauthorityhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import lombok.Data;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.tutorial.jwtauthorityhttpserver.JwtTokens.Credentials;
import net.officefloor.tutorial.jwtauthorityhttpserver.JwtTokens.Token;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the JWT Authority HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class JwtAuthorityHttpServerTest {

	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	private String refreshToken;

	private String accessToken;

	@Test
	public void login() throws Exception {

		// Undertake login
		Credentials credentials = new Credentials("daniel", "daniel");
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/login", credentials).secure(true));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");

		// Extract the refresh token
		WritableHttpCookie cookie = response.getCookie(JwtTokens.REFRESH_TOKEN_COOKIE_NAME);
		assertNotNull(cookie, "Should have refresh token");

		// Extract the access token
		Token accessToken = response.getJson(200, Token.class);
		assertNotNull(accessToken.getToken(), "Should have access token");

		// Capture for other tests
		this.refreshToken = cookie.getValue();
		this.accessToken = accessToken.getToken();
	}

	@Test
	public void refreshAccessToken() throws Exception {

		// Undertake login to obtain refresh token
		this.login();

		// Attempt to obtain access token without refresh token
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockRequest("/refresh").secure(true).method(HttpMethod.POST));
		assertEquals(401, response.getStatus().getStatusCode(), "Should not be authorised");

		// Obtain new access token with refresh token
		response = this.server.send(MockWoofServer.mockRequest("/refresh").secure(true).method(HttpMethod.POST)
				.cookie(JwtTokens.REFRESH_TOKEN_COOKIE_NAME, this.refreshToken));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");

		// Extract the access token
		Token token = response.getJson(200, Token.class);
		assertNotNull(token.getToken(), "Should have access token");
		assertNotEquals(this.accessToken, token.getToken(), "Should be new access token");
	}

	@Test
	public void jwksPublishing() throws Exception {

		// Publish keys via JWKS
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/jwks.json").secure(true));

		// Should have two keys available (one active and one in future rotation)
		JwksKeys keys = response.getJson(200, JwksKeys.class);
		assertEquals(2, keys.getKeys().size(), "Incorrect number of keys");
	}

	@Data
	public static class JwksKeys {
		private List<RsaJwksKey> keys;
	}

	@Data
	public static class RsaJwksKey {

		// As per RFC 7517 for RSA public key
		private String kty;
		private String n;
		private String e;

		// Additional to allow rotating keys
		private Long nbf; // epoch start time in seconds
		private Long exp; // epoch expire time in seconds
	}

}
// END SNIPPET: tutorial