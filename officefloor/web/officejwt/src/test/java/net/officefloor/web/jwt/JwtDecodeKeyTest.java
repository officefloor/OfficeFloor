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

package net.officefloor.web.jwt;

import java.security.Key;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * Ensure not able to construct invalid {@link JwtValidateKey}.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtDecodeKeyTest extends OfficeFrameTestCase {

	/**
	 * {@link KeyPair}.
	 */
	private static final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

	/**
	 * Mock current time.
	 */
	private static final long mockCurrentTime = 20000;

	/**
	 * {@link MockClockFactory}.
	 */
	private MockClockFactory clockFactory = new MockClockFactory(mockCurrentTime);

	/**
	 * Ensure mock {@link JwtValidateKey}.
	 */
	public void testValidMock() {
		assertJwtDecodeKey(new JwtValidateKey(keyPair.getPublic()), 0, Long.MAX_VALUE, keyPair.getPublic());
	}

	/**
	 * Ensure raw {@link JwtValidateKey}.
	 */
	public void testValidRaw() {
		assertJwtDecodeKey(new JwtValidateKey(10, 20, keyPair.getPublic()), 10, 20, keyPair.getPublic());
	}

	/**
	 * Ensure appropriately calculate {@link JwtValidateKey}.
	 */
	public void testValidCalculated() {
		assertJwtDecodeKey(new JwtValidateKey(this.clockFactory.createClock((time) -> time), 20, TimeUnit.MINUTES,
				keyPair.getPublic()), mockCurrentTime, mockCurrentTime + (20 * 60), keyPair.getPublic());
	}

	/**
	 * <p>
	 * Ensure appropriately defaults {@link TimeUnit} for calculating
	 * {@link JwtValidateKey}.
	 * <p>
	 * Should default to seconds.
	 */
	public void testValidDefaultTimeUnit() {
		assertJwtDecodeKey(
				new JwtValidateKey(this.clockFactory.createClock((time) -> time), 10, null, keyPair.getPublic()),
				mockCurrentTime, mockCurrentTime + 10, keyPair.getPublic());
	}

	/**
	 * Asserts the {@link JwtValidateKey} is valid.
	 * 
	 * @param decodeKey          {@link JwtValidateKey} to validate.
	 * @param expectedStartTime  Expected start time.
	 * @param expectedExpireTime Expected expire time.
	 * @param key                Expected {@link Key}.
	 */
	private static void assertJwtDecodeKey(JwtValidateKey decodeKey, long expectedStartTime, long expectedExpireTime,
			Key key) {
		assertEquals("Incorrect start time", expectedStartTime, decodeKey.getStartTime());
		assertEquals("Incorrect expire time", expectedExpireTime, decodeKey.getExpireTime());
		assertSame("Incorrect key", key, decodeKey.getKey());
	}

	/**
	 * Ensure can not construct invalid {@link JwtValidateKey} instances.
	 */
	public void testMissingJwtDecodeKeyData() throws Exception {

		// Assert invalid
		BiConsumer<Runnable, String> assertInvalid = (creator, expectedError) -> {
			try {
				creator.run();
				fail("Should not be successful");
			} catch (IllegalArgumentException ex) {
				assertEquals("Incorrect cause", expectedError, ex.getMessage());
			}
		};

		// Validate not able to construct invalid JWT decode key
		assertInvalid.accept(() -> new JwtValidateKey(null), "Must provide Key");
		assertInvalid.accept(() -> new JwtValidateKey(10, 20, null), "Must provide Key");
		assertInvalid.accept(() -> new JwtValidateKey(null, 0, null, null), "Must provide Clock");
		assertInvalid.accept(() -> new JwtValidateKey(clockFactory.createClock((time) -> time), 10, null, null),
				"Must provide Key");
	}

}
