/*-
 * #%L
 * Identity for Google Logins
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

package net.officefloor.identity.google.mock;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.openidconnect.HttpTransportFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebSignature.Header;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Clock;

import net.officefloor.identity.google.GoogleIdTokenVerifierManagedObjectSource;
import net.officefloor.test.JUnitAgnosticAssert;
import net.officefloor.test.module.ModuleAccessible;

/**
 * Abstract JUnit mocking the {@link GoogleIdTokenVerifier}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractGoogleIdTokenJUnit {

	/**
	 * Mock {@link KeyPair} id.
	 */
	private static final String KEY_ID = "MOCK_KEY";

	/**
	 * Algorithm.
	 */
	private static final String KEY_TYPE = "RSA";

	/**
	 * Key algorithm.
	 */
	private static final String KEY_ALGORITHM = "RS256";

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
				pair = KeyPairGenerator.getInstance(KEY_TYPE).generateKeyPair();
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
		Header header = new JsonWebSignature.Header().setKeyId(KEY_ID).setAlgorithm(KEY_ALGORITHM);
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
	 * 
	 * @throws Exception If fails to setup mock tokens.
	 */
	protected void setupMockTokens() throws Exception {

		// Create the public key to verify token
		PublicKey publicKey = getMockKeyPair().getPublic();
		RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;

		// Key manager
		GooglePublicKeysManager manager = new GooglePublicKeysManager(new MockHttpTransport(),
				GsonFactory.getDefaultInstance()) {

			@Override
			public GooglePublicKeysManager refresh() throws GeneralSecurityException, IOException {
				List<PublicKey> keys = new ArrayList<>();
				keys.add(publicKey);
				ModuleAccessible.setFieldValue(this, "publicKeys", keys, "Setting up Google mock tokens");
				return this;
			}
		};

		// Build the mock verifier
		Clock clock = () -> 300;

		// Create the JWK response content
		JsonWebKey webKey = new JsonWebKey();
		webKey.kid = KEY_ID;
		webKey.kty = KEY_TYPE;
		webKey.alg = KEY_ALGORITHM;
		Base64.Encoder encoder = Base64.getEncoder();
		webKey.n = encoder.encodeToString(rsaPublicKey.getModulus().toByteArray());
		webKey.e = encoder.encodeToString(rsaPublicKey.getPublicExponent().toByteArray());
		JsonWebKeySet keySet = new JsonWebKeySet();
		keySet.keys.add(webKey);
		byte[] jwkContent = new ObjectMapper().writeValueAsBytes(keySet);

		// Create the mock transport to provide the keys
		HttpTransportFactory jwkHttpTransportFactory = () -> {
			MockLowLevelHttpResponse jwkResponse = new MockLowLevelHttpResponse();
			jwkResponse.setContent(jwkContent);
			return new MockHttpTransport.Builder().setLowLevelHttpResponse(jwkResponse).build();
		};

		// Create mock verifier
		this.mockVerifier = (GoogleIdTokenVerifier) new GoogleIdTokenVerifier.Builder(manager)
				.setHttpTransportFactory(jwkHttpTransportFactory).setClock(clock).build();

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

	/**
	 * JWT key set.
	 */
	public static class JsonWebKeySet {

		public List<JsonWebKey> keys = new ArrayList<>(1);
	}

	/**
	 * JWT key.
	 */
	public static class JsonWebKey {

		public String kid;

		public String kty;

		public String alg;

		public String e;

		public String n;
	}

}
