/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt.mock;

import java.security.KeyPair;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * <p>
 * Abstract JUnit mock JWT access tokens for the {@link JwtHttpSecuritySource}.
 * <p>
 * This allows generating access tokens for testing the application.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractMockJwtAccessTokenJUnit {

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link KeyPair} for signing and validating the JWT.
	 */
	private final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

	/**
	 * {@link JwtValidateKey} that does not expire (well within the context of
	 * testing).
	 */
	private final JwtValidateKey validateKey = new JwtValidateKey(this.keyPair.getPublic());

	/**
	 * Creates the access token.
	 * 
	 * @param claims Claims for the access token.
	 * @return Access token.
	 */
	public String createAccessToken(Object claims) {

		// Obtain the claims
		String payload;
		try {
			payload = mapper.writeValueAsString(claims);
		} catch (Exception ex) {
			throw new AssertionError("Failed to serialise claims to payload", ex);
		}

		// Create the access token
		String accessToken = Jwts.builder().signWith(this.keyPair.getPrivate()).setPayload(payload).compact();

		// Return the access token
		return accessToken;
	}

	/**
	 * Obtains the active {@link JwtValidateKey} instances.
	 * 
	 * @return Active {@link JwtValidateKey} instances.
	 */
	public JwtValidateKey[] getActiveJwtValidateKeys() {
		return new JwtValidateKey[] { this.validateKey };
	}

	/**
	 * Convenience method to add <code>authorization</code> {@link HttpHeader} to
	 * the {@link MockHttpRequestBuilder}.
	 * 
	 * @param claims         Claims for the access token.
	 * @param requestBuilder {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpRequestBuilder} with {@link HttpHeader} configured.
	 */
	public MockHttpRequestBuilder authorize(Object claims, MockHttpRequestBuilder requestBuilder) {
		return requestBuilder.header("authorization", "Bearer " + this.createAccessToken(claims));
	}

	/**
	 * Setups up the mock JWT keys.
	 */
	protected void setupMockKeys() {
		JwtHttpSecuritySource
				.setOverrideKeys(() -> new JwtValidateKey[] { AbstractMockJwtAccessTokenJUnit.this.validateKey });
	}

	/**
	 * Tears down the mock JWT keys.
	 */
	protected void teardownMockKeys() {
		JwtHttpSecuritySource.setOverrideKeys(null);
	}

}
