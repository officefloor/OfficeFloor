package net.officefloor.tutorial.jwthttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.jwt.authority.JwtAuthority;

/**
 * Undertakes login.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtTokens {

	@Data
	@HttpObject
	@RequiredArgsConstructor
	@AllArgsConstructor
	public static class Credentials {
		private String username;
		private String password;
	}

	@Data
	@RequiredArgsConstructor
	@AllArgsConstructor
	public static class Tokens {
		private String refreshToken;
		private String accessToken;
	}

	public void login(Credentials credentials, JwtAuthority<Identity> authority, ObjectResponse<Tokens> response) {

		// Mock authentication
		// (production solution would check appropriate user store)
		// (or use potential OpenId third party login)
		if ((credentials.getUsername() == null) || (!credentials.getUsername().equals(credentials.getPassword()))) {
			throw new HttpException(HttpStatus.UNAUTHORIZED);
		}

		// Create the identity and claims
		Identity identity = new Identity(credentials.username);
		Claims claims = this.createClaims(credentials.username);

		// Create the refresh and access tokens
		String refreshToken = authority.createRefreshToken(identity);
		String accessToken = authority.createAccessToken(claims);

		// Send response
		response.send(new Tokens(refreshToken, accessToken));
	}

	@Data
	@HttpObject
	@AllArgsConstructor
	@RequiredArgsConstructor
	public static class Token {
		private String token;
	}

	public void refreshAccessToken(Token request, JwtAuthority<Identity> authority, ObjectResponse<Token> response) {

		// Obtain the identity from refresh token
		Identity identity = authority.decodeRefreshToken(request.token);

		// Create a new access token
		Claims claims = this.createClaims(identity.getId());
		String accessToken = authority.createAccessToken(claims);

		// Send refreshed access token
		response.send(new Token(accessToken));
	}

	private Claims createClaims(String username) {

		// Mock claims
		// (claim information should be pulled from user store)
		String[] roles = new String[] { "tutorial" };

		// Return the claims
		return new Claims(username, roles);
	}

}