/*-
 * #%L
 * JWT Separate Authority Server Tutorial (Authority Server)
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.tutorial.jwtauthorityhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.jwtauthorityhttpserver.JwtTokens.Credentials;
import net.officefloor.tutorial.jwtauthorityhttpserver.JwtTokens.Token;
import net.officefloor.tutorial.jwtauthorityhttpserver.JwtTokens.Tokens;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the JWT Authority HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class JwtAuthorityHttpServerTest {

	private static ObjectMapper mapper = new ObjectMapper();

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	private String refreshToken;

	private String accessToken;

	@Test
	public void login() throws Exception {

		// Undertake login
		Credentials credentials = new Credentials("daniel", "daniel");
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/login").secure(true).method(HttpMethod.POST)
						.header("Content-Type", "application/json").entity(mapper.writeValueAsString(credentials)));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Extract the access token
		Tokens tokens = mapper.readValue(response.getEntity(), Tokens.class);
		assertNotNull("Should have refresh token", tokens.getRefreshToken());
		assertNotNull("Should have access token", tokens.getAccessToken());
		this.refreshToken = tokens.getRefreshToken();
		this.accessToken = tokens.getAccessToken();
	}

	@Test
	public void refreshAccessToken() throws Exception {

		// Undertake login to obtain refresh token
		this.login();

		// Obtain new access token
		Token refreshToken = new Token(this.refreshToken);
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/refresh").secure(true).method(HttpMethod.POST)
						.header("Content-Type", "application/json").entity(mapper.writeValueAsString(refreshToken)));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Extract the access token
		Token token = mapper.readValue(response.getEntity(), Token.class);
		assertNotNull("Should have access token", token.getToken());
		assertNotEquals("Should be new access token", this.accessToken, token.getToken());
	}

	@Test
	public void jwksPublishing() throws Exception {

		// Ensure can publish keys via JWKS
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/jwks.json").secure(true));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Should have two keys available (one active and one in future rotation)
		JwksKeys keys = mapper.readValue(response.getEntity(), JwksKeys.class);
		assertEquals("Incorrect number of keys", 2, keys.getKeys().size());
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
