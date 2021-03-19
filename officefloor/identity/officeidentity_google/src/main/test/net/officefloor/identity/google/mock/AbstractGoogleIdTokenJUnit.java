/*-
 * #%L
 * Identity for Google Logins
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

package net.officefloor.identity.google.mock;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebSignature.Header;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.util.Clock;

import net.officefloor.identity.google.GoogleIdTokenVerifierManagedObjectSource;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Abstract JUnit mocking the {@link GoogleIdTokenVerifier}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractGoogleIdTokenJUnit {

	/**
	 * Mock {@link KeyPair}.
	 */
	private static KeyPair pair = null;

	/**
	 * Obtains the mock {@link KeyPair}.
	 * 
	 * @return Mock {@link KeyPair}.
	 */
	private static KeyPair getMockKeyPair() {
		if (pair == null) {
			// Avoid heavy computation by reusing key pair
			try {
				pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
			} catch (Exception ex) {
				return JUnitAgnosticAssert.fail(ex);
			}
		}
		return pair;
	}

	/**
	 * {@link MockVerifier}.
	 */
	private volatile GoogleIdTokenVerifier mockVerifier = null;

	/**
	 * Generates a mock {@link GoogleIdToken} string.
	 * 
	 * @param googleId       Google identifier.
	 * @param email          Email address.
	 * @param nameValuePairs Name/value pairs.
	 * @return {@link GoogleIdToken} string.
	 */
	public String getMockIdToken(String googleId, String email, String... nameValuePairs) {

		// Ensure mock verifier available
		this.ensureInContext("mock id token");

		// Obtain the private key
		PrivateKey privateKey = getMockKeyPair().getPrivate();

		// Generate the id token
		Header header = new JsonWebSignature.Header().setAlgorithm("RS256");
		Payload payload = new GoogleIdToken.Payload().setSubject(googleId).setEmail(email).setEmailVerified(true)
				.setIssuedAtTimeSeconds(mockVerifier.getClock().currentTimeMillis()).setExpirationTimeSeconds(10L)
				.setIssuer(mockVerifier.getIssuer());
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = nameValuePairs[i];
			String value = nameValuePairs[i + 1];
			payload = payload.set(name, value);
		}
		String token;
		try {
			token = GoogleIdToken.signUsingRsaSha256(privateKey, GsonFactory.getDefaultInstance(), header, payload);
		} catch (Exception ex) {
			return JUnitAgnosticAssert.fail(ex);
		}

		// Return the id token
		return token;
	}

	/**
	 * Obtains the {@link GoogleIdTokenVerifier}.
	 * 
	 * @return {@link GoogleIdTokenVerifier}.
	 */
	public GoogleIdTokenVerifier getGoogleIdTokenVerifier() {

		// Ensure mock verifier available
		this.ensureInContext(GoogleIdTokenVerifier.class.getSimpleName());

		// Return the verifier
		return this.mockVerifier;
	}

	/**
	 * Ensure within context.
	 * 
	 * @param item Item attempting to obtain within context.
	 */
	private void ensureInContext(String item) {
		if (this.mockVerifier == null) {
			throw new IllegalStateException("Can only obtain " + item + " within context of rule");
		}
	}

	/**
	 * Setups to use mock tokens.
	 */
	protected void setupMockTokens() {

		// Create the public key to verify token
		KeyPair pair = getMockKeyPair();
		PublicKey publicKey = pair.getPublic();

		// Key manager
		GooglePublicKeysManager manager = new GooglePublicKeysManager(new MockHttpTransport(),
				GsonFactory.getDefaultInstance()) {
			@Override
			public GooglePublicKeysManager refresh() throws GeneralSecurityException, IOException {
				try {
					Field publicKeys = GooglePublicKeysManager.class.getDeclaredField("publicKeys");
					publicKeys.setAccessible(true);
					List<PublicKey> keys = new ArrayList<>();
					keys.add(publicKey);
					publicKeys.set(this, keys);
				} catch (Exception ex) {
					throw new GeneralSecurityException(ex);
				}
				return this;
			}
		};

		// Build the mock verifier
		Clock clock = () -> 300;

		// Create mock verifier
		this.mockVerifier = new GoogleIdTokenVerifier.Builder(manager).setClock(clock).build();

		// Setup the mock token
		GoogleIdTokenVerifierManagedObjectSource.setVerifyFactory(() -> this.getGoogleIdTokenVerifier());
	}

	/**
	 * Tears down using the mock tokens.
	 */
	protected void tearDownMockTokens() {

		// Clear the mock tokens
		GoogleIdTokenVerifierManagedObjectSource.setVerifyFactory(null);
		this.mockVerifier = null;
	}

}
