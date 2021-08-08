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

package net.officefloor.web.jwt.jwks;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Keys;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.web.jwt.validate.JwtValidateKey;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;

/**
 * Ensure {@link JwksSectionSource} provides appropriate means to retrieve
 * {@link JwtValidateKey} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class JwksSectionSourceTest extends OfficeFrameTestCase implements JwksRetriever {

	/**
	 * {@link ObjectMapper}.
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link RSAPublicKey}.
	 */
	private static RSAPublicKey publicKey = (RSAPublicKey) Keys.keyPairFor(SignatureAlgorithm.RS256).getPublic();

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Retrieve keys failure.
	 */
	private Exception retrieveKeysFailure = null;

	/**
	 * JWKS content.
	 */
	private String jwksContent = "{\"keys\":[]}";

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure handle failure to retrieve keys.
	 */
	public void testFailRetrieveKeys() {
		this.retrieveKeysFailure = new IOException("TEST");
		MockJwtValidateKeyCollector collector = this.collect();
		assertSame("Should have failure", this.retrieveKeysFailure, collector.failure);
		assertNull("Should be no keys", collector.keys);
	}

	/**
	 * Ensure handle no keys retrieved.
	 */
	public void testNoKeys() {
		MockJwtValidateKeyCollector collector = this.collect();
		assertEquals("Should be no keys", 0, collector.keys.length);
	}

	/**
	 * Ensure default time window.
	 */
	public void testDefaultTimePeriod() {
		JwksKey key = new JwksKey(null, null);
		MockJwtValidateKeyCollector collector = this.collect(key);
		JwtValidateKey validateKey = collector.keys[0];
		assertEquals("Incorrect start time", 0, validateKey.getStartTime());
		assertEquals("Incorrect expire time", Long.MAX_VALUE, validateKey.getExpireTime());
	}

	/**
	 * Ensure specify time window.
	 */
	public void testSpecifiedTimeWindow() {
		JwksKey key = new JwksKey(20L, 500L);
		MockJwtValidateKeyCollector collector = this.collect(key);
		JwtValidateKey validateKey = collector.keys[0];
		assertEquals("Incorrect start time", key.nbf.longValue(), validateKey.getStartTime());
		assertEquals("Incorrect expire time", key.exp.longValue(), validateKey.getExpireTime());
	}

	/**
	 * Ensure can retrieve the {@link JwtValidateKey} instances.
	 */
	public void testRetrieveRsaKey() {

		// Provide the JWKS content
		String modulus = base64UrlValue(publicKey.getModulus());
		String exponent = base64UrlValue(publicKey.getPublicExponent());

		// Collect the keys
		MockJwtValidateKeyCollector collector = this.collect(new JwksKey("RSA", modulus, exponent));

		// Ensure loaded key
		assertEquals("Incorrect number of keys", 1, collector.keys.length);
		Key key = collector.keys[0].getKey();
		assertTrue("Should be RSA public key", key instanceof RSAPublicKey);
		RSAPublicKey rsaKey = (RSAPublicKey) key;
		assertEquals("Incorrect algorithm", "RSA", rsaKey.getAlgorithm());
		assertEquals("Incorrect modulus", publicKey.getModulus(), rsaKey.getModulus());
		assertEquals("Incorrect exponent", publicKey.getPublicExponent(), rsaKey.getPublicExponent());
	}

	/**
	 * Ensure can retrieve mock {@link JwtValidateKey} instance.
	 */
	public void testRetrieveMockKey() {

		// Collect the keys
		MockJwtValidateKeyCollector collector = this.collect(new JwksKey("MOCK", null, null));

		// Ensure loaded key
		assertEquals("Incorrect number of keys", 1, collector.keys.length);
		Key key = collector.keys[0].getKey();
		assertSame("Should be mock key", MockJwksKeyParser.mockKey, key);
	}

	/**
	 * Ensure can retrieve multiple {@link JwtValidateKey} instance.
	 */
	public void testRetrieveMultipleKeys() {

		// Provide the JWKS content
		String modulus = base64UrlValue(publicKey.getModulus());
		String exponent = base64UrlValue(publicKey.getPublicExponent());

		// Collect the keys
		MockJwtValidateKeyCollector collector = this.collect(new JwksKey("RSA", modulus, exponent),
				new JwksKey("MOCK", null, null));

		// Ensure loaded key
		assertEquals("Incorrect number of keys", 2, collector.keys.length);
		assertTrue("First should be RSA key", collector.keys[0].getKey() instanceof RSAPublicKey);
		assertSame("Second should be mock key", MockJwksKeyParser.mockKey, collector.keys[1].getKey());
	}

	/**
	 * Ensure failed {@link JwtValidateKey} instance.
	 */
	public void testRetrieveFailedParseKey() {

		// Collect the keys
		MockJwtValidateKeyCollector collector = this.collect(new JwksKey("FAIL", null, null),
				new JwksKey("MOCK", null, null));

		// Ensure only the mock is loaded
		assertEquals("Incorrect number of keys", 1, collector.keys.length);
		assertSame("Incorrect provided key", MockJwksKeyParser.mockKey, collector.keys[0].getKey());
	}

	/**
	 * Collect the {@link JwtValidateKey} instances.
	 * 
	 * @param jwksKeys {@link JwksKey} instances.
	 * @return {@link MockJwtValidateKeyCollector}.
	 */
	private MockJwtValidateKeyCollector collect(JwksKey... jwksKeys) {
		try {

			// Compile
			CompileOfficeFloor compiler = new CompileOfficeFloor();
			compiler.office((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();

				// Add JWKS section source to test
				office.addOfficeSection("COLLECTOR", JwksSectionSource.class.getName(), null);

				// Provide mock retriever
				office.addOfficeManagedObjectSource("JWKS_RETRIEVER", new Singleton(this))
						.addOfficeManagedObject("JWKS_RETRIEVER", ManagedObjectScope.THREAD);
			});
			this.officeFloor = compiler.compileAndOpenOfficeFloor();

			// Provide the JWKS content
			this.jwksContent(jwksKeys);

			// Obtain the JWT validate keys
			MockJwtValidateKeyCollector collector = new MockJwtValidateKeyCollector();
			CompileOfficeFloor.invokeProcess(this.officeFloor, "COLLECTOR.retrieveJwtValidateKeys", collector);

			// Return the collector
			return collector;

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

	/**
	 * Translates to base 64 URL value.
	 * 
	 * @param value {@link BigInteger} value.
	 * @return Base 64 URL value.
	 */
	private static String base64UrlValue(BigInteger value) {
		byte[] bytes = value.toByteArray();
		return Base64.getUrlEncoder().encodeToString(bytes);
	}

	/**
	 * Mock {@link JwtValidateKeyCollector}.
	 */
	private class MockJwtValidateKeyCollector implements JwtValidateKeyCollector {

		private JwtValidateKey[] keys = null;

		private Throwable failure = null;

		@Override
		public JwtValidateKey[] getCurrentKeys() {
			fail("Should not be required");
			return null;
		}

		@Override
		public void setKeys(JwtValidateKey... keys) {
			this.keys = keys;
		}

		@Override
		public void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit) {
			this.failure = cause;
		}
	}

	/**
	 * Specifies the JWKS content.
	 * 
	 * @param keys {@link JwksKey} instances.
	 */
	private void jwksContent(JwksKey... keys) throws Exception {
		this.jwksContent = mapper.writeValueAsString(new JwksKeys(keys));
	}

	protected static class JwksKeys {

		public JwksKey[] keys;

		private JwksKeys(JwksKey... keys) {
			this.keys = keys;
		}
	}

	protected static class JwksKey {

		public Long nbf;

		public Long exp;

		public String kty;

		public String n;

		public String e;

		/**
		 * Instantiate for {@link RSAPublicKey}.
		 */
		private JwksKey(String kty, String n, String e) {
			this.kty = kty;
			this.n = n;
			this.e = e;
		}

		private JwksKey(Long nbf, Long exp) {
			this("RSA", base64UrlValue(publicKey.getModulus()), base64UrlValue(publicKey.getPublicExponent()));
			this.nbf = nbf;
			this.exp = exp;
		}
	}

	/*
	 * ================ JwksRetriever =================
	 */

	@Override
	public InputStream retrieveJwks() throws Exception {
		if (this.retrieveKeysFailure != null) {
			throw this.retrieveKeysFailure;
		}
		return new ByteArrayInputStream(this.jwksContent.getBytes());
	}

}
