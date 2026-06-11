package net.officefloor.tutorial.jwthttpserver;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.jwt.authority.AccessToken;
import net.officefloor.web.jwt.authority.JwtAuthority;
import net.officefloor.web.jwt.authority.RefreshToken;

/**
 * Undertakes login.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtTokens {

	public static final String REFRESH_TOKEN_COOKIE_NAME = "RefreshToken";

	@Data
	@HttpObject
	@RequiredArgsConstructor
	@AllArgsConstructor
	public static class Credentials {
		private String username;
		private String password;
	}

	@Data
	@HttpObject
	@AllArgsConstructor
	@RequiredArgsConstructor
	public static class Token {
		private String token;
	}

	public void login(Credentials credentials, JwtAuthority<Identity> authority, ObjectResponse<Token> response,
			ServerHttpConnection connection) {

		// Mock authentication
		// (production solution would restrict tries and check appropriate user store)
		// (or use potential OpenId third party login)
		if ((credentials.getUsername() == null) || (!credentials.getUsername().equals(credentials.getPassword()))) {
			throw new HttpException(HttpStatus.UNAUTHORIZED);
		}

		// Create the refresh token from identity
		Identity identity = new Identity(credentials.username);
		RefreshToken refreshToken = authority.createRefreshToken(identity);

		// Provide refresh token
		connection.getResponse().getCookies().setCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken.getToken())
				.setHttpOnly(true).setSecure(true).setExpires(Instant.ofEpochSecond(refreshToken.getExpireTime()));

		// Create the access token
		Claims claims = this.createClaims(credentials.username);
		AccessToken accessToken = authority.createAccessToken(claims);

		// Send response
		response.send(new Token(accessToken.getToken()));
	}

	public void refreshAccessToken(ServerHttpConnection connection, JwtAuthority<Identity> authority,
			ObjectResponse<Token> response) {

		// Obtain the refresh token
		HttpRequestCookie cookie = connection.getRequest().getCookies().getCookie(REFRESH_TOKEN_COOKIE_NAME);
		if (cookie == null) {
			throw new HttpException(HttpStatus.UNAUTHORIZED);
		}
		String refreshToken = cookie.getValue();

		// Obtain the identity from refresh token
		Identity identity = authority.decodeRefreshToken(refreshToken);

		// Create a new access token
		Claims claims = this.createClaims(identity.getId());
		AccessToken accessToken = authority.createAccessToken(claims);

		// Send refreshed access token
		response.send(new Token(accessToken.getToken()));
	}

	private Claims createClaims(String username) {

		// Mock claims
		// (claim information should be pulled from user store)
		String[] roles = new String[] { "tutorial" };

		// Return the claims
		return new Claims(username, roles);
	}

}