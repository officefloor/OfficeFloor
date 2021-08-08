/*-
 * #%L
 * JWT Authority
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

package net.officefloor.web.jwt.authority.jwks;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.jwt.authority.AccessToken;
import net.officefloor.web.jwt.authority.AccessTokenException;
import net.officefloor.web.jwt.authority.JwtAuthority;
import net.officefloor.web.jwt.authority.RefreshToken;
import net.officefloor.web.jwt.authority.RefreshTokenException;
import net.officefloor.web.jwt.authority.ValidateKeysException;
import net.officefloor.web.jwt.authority.jwks.MockJwksKeyWriterServiceFactory.MockKey;
import net.officefloor.web.jwt.jwks.JwksRetriever;
import net.officefloor.web.jwt.jwks.JwksSectionSource;
import net.officefloor.web.jwt.validate.JwtValidateKey;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;

/**
 * Tests the {@link JwksPublishSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JwksPublishSectionSourceTest extends OfficeFrameTestCase implements JwtAuthority<Object> {

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Mock {@link RSAPublicKey}.
	 */
	private static final RSAPublicKey publicKey = (RSAPublicKey) Keys.keyPairFor(SignatureAlgorithm.RS256).getPublic();

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link JwtValidateKey} instances.
	 */
	private JwtValidateKey[] validateKeys;

	/**
	 * JWKS {@link InputStream} for {@link JwksRetriever}.
	 */
	private InputStream jwksContent = null;

	/**
	 * {@link JwksRetriever}.
	 */
	private JwksRetriever jwksRetriever = new JwksRetriever() {
		@Override
		public InputStream retrieveJwks() throws Exception {
			return JwksPublishSectionSourceTest.this.jwksContent;
		}
	};

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can handle no {@link JwtValidateKey} instances.
	 */
	public void testNoKeys() {
		List<JwksKey> keys = this.getJwksKeys();
		assertEquals("Should be no keys", 0, keys.size());
	}

	/**
	 * Ensure can send {@link RSAPublicKey}.
	 */
	public void testRsaPublicKey() {

		// Obtain the JWKS keys
		List<JwksKey> keys = this.getJwksKeys(new JwtValidateKey(200, 5000, publicKey));

		// Ensure correct keys
		assertEquals("Incorrect number of keys", 1, keys.size());
		JwksKey key = keys.get(0);
		assertEquals("Incorrect key type", "RSA", key.kty);
		assertEquals("Incorrect start time", Long.valueOf(200), key.nbf);
		assertEquals("Incorrect expire time", Long.valueOf(5000), key.exp);
		assertEquals("Incorrect modulus", Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray()),
				key.n);
		assertEquals("Incorrect exponent",
				Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray()), key.e);
		assertNull("Should not load mock", key.mock);
	}

	/**
	 * Ensure can send {@link MockKey}.
	 */
	public void testMockKey() {

		// Obtain the JWKS keys
		List<JwksKey> keys = this.getJwksKeys(new JwtValidateKey(10, 20, MockJwksKeyWriterServiceFactory.MOCK_KEY));

		// Ensure correct keys
		assertEquals("Incorrect number of keys", 1, keys.size());
		JwksKey key = keys.get(0);
		assertEquals("Incorrect key type", "MOCK", key.kty);
		assertEquals("Incorrect start time", Long.valueOf(10), key.nbf);
		assertEquals("Incorrect expire time", Long.valueOf(20), key.exp);
		assertEquals("Incorrect mock key", "mocked", key.mock);
	}

	/**
	 * Ensure can handle multiple {@link JwtValidateKey} instances.
	 */
	public void testMultipleKeys() {

		// Obtain the JWKS keys
		List<JwksKey> keys = this.getJwksKeys(new JwtValidateKey(publicKey),
				new JwtValidateKey(10, 20, MockJwksKeyWriterServiceFactory.MOCK_KEY));

		// Ensure correct keys
		assertEquals("Incorrect number of keys", 2, keys.size());
		JwksKey rsaKey = keys.get(0);
		assertEquals("Incorrect first key", "RSA", rsaKey.kty);
		JwksKey mockKey = keys.get(1);
		assertEquals("Incorrect mock key", "MOCK", mockKey.kty);
	}

	/**
	 * Fail on unknown key.
	 */
	public void testFailOnUnknownKey() throws IOException {

		// Create the mock unknown key
		Key unknownKey = this.createMock(Key.class);

		// Record unknown
		this.recordReturn(unknownKey, unknownKey.getAlgorithm(), "UNKNOWN");
		this.replayMockObjects();

		// Attempt to obtain the keys
		MockHttpResponse response = this.getJwksKeys(new JwtValidateKey[] { new JwtValidateKey(unknownKey) },
				HttpStatus.INTERNAL_SERVER_ERROR, (mockResponse) -> mockResponse);
		assertEquals("Incorrect response",
				JacksonHttpObjectResponderFactory
						.getEntity(new HttpException(new Exception("No JwksKeyWriter for key UNKNOWN")), mapper),
				response.getEntity(null));

		// Ensure verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can serialise to JSON and then deserialise back to
	 * {@link JwtValidateKey}.
	 */
	public void testRoundTripToValidateKeysForCollector() throws Throwable {

		// Create the validate keys
		JwtValidateKey[] validateKeys = new JwtValidateKey[] { new JwtValidateKey(5000L, 10000L, publicKey) };

		// Retrieve the raw JWKS content
		this.jwksContent = this.getJwksKeys(validateKeys, HttpStatus.OK, (response) -> response.getEntity());

		// Ensure receive the same JWK validate keys
		Closure<JwtValidateKey[]> roundTripKeys = new Closure<>();
		CompileOfficeFloor.invokeProcess(this.officeFloor, "JWKS." + JwksSectionSource.INPUT,
				new JwtValidateKeyCollector() {

					@Override
					public void setKeys(JwtValidateKey... keys) {
						roundTripKeys.value = keys;
					}

					@Override
					public void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit) {
						fail("Should not fail");
					}

					@Override
					public JwtValidateKey[] getCurrentKeys() {
						fail("Should not require current keys");
						return null;
					}
				});

		// Ensure same keys
		assertEquals("Incorrect number of keys", validateKeys.length, roundTripKeys.value.length);
		for (int i = 0; i < validateKeys.length; i++) {
			JwtValidateKey validateKey = validateKeys[i];
			JwtValidateKey roundTripKey = roundTripKeys.value[i];
			assertNotSame("Should be different keys for " + i, validateKey, roundTripKey);
			assertEquals("Incorrect start time for " + i, validateKey.getStartTime(), roundTripKey.getStartTime());
			assertEquals("Incorrect expire time for " + i, validateKey.getExpireTime(), roundTripKey.getExpireTime());
			assertEquals("Incorrect key for " + i, validateKey.getKey(), roundTripKey.getKey());
		}
	}

	/**
	 * Translates response.
	 */
	@FunctionalInterface
	private static interface TranslateResponse<R> {
		R translate(MockHttpResponse response) throws Exception;
	}

	/**
	 * Obtains the {@link JwksKey} instances.
	 * 
	 * @param validateKeys {@link JwtValidateKey} instances.
	 * @return {@link JwksKey} instances.
	 */
	private List<JwksKey> getJwksKeys(JwtValidateKey... validateKeys) {
		return this.getJwksKeys(validateKeys, HttpStatus.OK, (response) -> {

			// Parse out the JWKS content
			JwksKeys keys = mapper.readValue(response.getEntity(), JwksKeys.class);

			// Return the keys
			return keys.keys;
		});
	}

	/**
	 * Obtains the {@link JwksKey} instances.
	 * 
	 * @param validateKeys   {@link JwtValidateKey} instances.
	 * @param expectedStatus Expected {@link HttpStatus}.
	 * @param translator     {@link TranslateResponse}.
	 * @return
	 */
	private <T> T getJwksKeys(JwtValidateKey[] validateKeys, HttpStatus expectedStatus,
			TranslateResponse<T> translator) {
		try {

			// Load the keys
			this.validateKeys = validateKeys;

			// Compile JWKS server
			WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
			compiler.mockHttpServer((server) -> this.server = server);
			compiler.web((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();
				WebArchitect web = context.getWebArchitect();

				// Add JWT authority
				office.addOfficeManagedObjectSource("JWT_AUTHORITY", new Singleton(this))
						.addOfficeManagedObject("JWT_AUTHORITY", ManagedObjectScope.THREAD);

				// Add JWKS publishing
				OfficeSection jwksPublish = office.addOfficeSection("JWKS_PUBLISH",
						JwksPublishSectionSource.class.getName(), null);
				HttpUrlContinuation jwksJson = web.getHttpInput(true, "/jwks.json");
				office.link(jwksJson.getInput(), jwksPublish.getOfficeSectionInput(JwksPublishSectionSource.INPUT));

				// Add JWKS sourcing
				office.addOfficeSection("JWKS", JwksSectionSource.class.getName(), null);
				office.addOfficeManagedObjectSource("JWKS_RETREIVER", new Singleton(this.jwksRetriever))
						.addOfficeManagedObject("JWKS_RETRIEVER", ManagedObjectScope.THREAD);
			});
			this.officeFloor = compiler.compileAndOpenOfficeFloor();

			// Obtain the JWKS data
			MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/jwks.json").secure(true));
			assertEquals("Incorrect response status", expectedStatus.getStatusCode(),
					response.getStatus().getStatusCode());

			// Return the translated response
			return translator.translate(response);

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

	protected static class JwksKeys {
		public List<JwksKey> keys;
	}

	protected static class JwksKey {
		public Long nbf;
		public Long exp;
		public String kty;
		public String n;
		public String e;
		public String mock;
	}

	/*
	 * ========================= JwtAuthority =========================
	 */

	@Override
	public RefreshToken createRefreshToken(Object identity) throws RefreshTokenException {
		fail("Should not be required");
		return null;
	}

	@Override
	public Object decodeRefreshToken(String refreshToken) throws RefreshTokenException {
		fail("Should not be required");
		return null;
	}

	@Override
	public void reloadRefreshKeys() {
		fail("Should not be required");
	}

	@Override
	public AccessToken createAccessToken(Object claims) throws AccessTokenException {
		fail("Should not be required");
		return null;
	}

	@Override
	public void reloadAccessKeys() {
		fail("Should not be required");
	}

	@Override
	public JwtValidateKey[] getActiveJwtValidateKeys() throws ValidateKeysException {
		return this.validateKeys;
	}

}
