package net.officefloor.web.jwt;

import java.security.Key;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;

/**
 * Ensure not able to construct invalid {@link JwtDecodeKey}.
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
	 * Ensure mock {@link JwtDecodeKey}.
	 */
	public void testValidMock() {
		assertJwtDecodeKey(new JwtDecodeKey(keyPair.getPublic()), 0, Long.MAX_VALUE, keyPair.getPublic());
	}

	/**
	 * Ensure raw {@link JwtDecodeKey}.
	 */
	public void testValidRaw() {
		assertJwtDecodeKey(new JwtDecodeKey(10, 20, keyPair.getPublic()), 10, 20, keyPair.getPublic());
	}

	/**
	 * Ensure appropriately calculate {@link JwtDecodeKey}.
	 */
	public void testValidCalculated() {
		assertJwtDecodeKey(new JwtDecodeKey(this.clockFactory.createClock((time) -> time), 20, TimeUnit.MINUTES,
				keyPair.getPublic()), mockCurrentTime, mockCurrentTime + (20 * 60), keyPair.getPublic());
	}

	/**
	 * <p>
	 * Ensure appropriately defaults {@link TimeUnit} for calculating
	 * {@link JwtDecodeKey}.
	 * <p>
	 * Should default to seconds.
	 */
	public void testValidDefaultTimeUnit() {
		assertJwtDecodeKey(
				new JwtDecodeKey(this.clockFactory.createClock((time) -> time), 10, null, keyPair.getPublic()),
				mockCurrentTime, mockCurrentTime + 10, keyPair.getPublic());
	}

	/**
	 * Asserts the {@link JwtDecodeKey} is valid.
	 * 
	 * @param decodeKey          {@link JwtDecodeKey} to validate.
	 * @param expectedStartTime  Expected start time.
	 * @param expectedExpireTime Expected expire time.
	 * @param key                Expected {@link Key}.
	 */
	private static void assertJwtDecodeKey(JwtDecodeKey decodeKey, long expectedStartTime, long expectedExpireTime,
			Key key) {
		assertEquals("Incorrect start time", expectedStartTime, decodeKey.getStartTime());
		assertEquals("Incorrect expire time", expectedExpireTime, decodeKey.getExpireTime());
		assertSame("Incorrect key", key, decodeKey.getKey());
	}

	/**
	 * Ensure can not construct invalid {@link JwtDecodeKey} instances.
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
		assertInvalid.accept(() -> new JwtDecodeKey(null), "Must provide Key");
		assertInvalid.accept(() -> new JwtDecodeKey(10, 20, null), "Must provide Key");
		assertInvalid.accept(() -> new JwtDecodeKey(null, 0, null, null), "Must provide Clock");
		assertInvalid.accept(() -> new JwtDecodeKey(clockFactory.createClock((time) -> time), 10, null, null),
				"Must provide Key");
	}

}
