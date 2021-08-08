/*-
 * #%L
 * JWT Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
