package net.officefloor.web.jwt.authority;

import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.web.jwt.spi.repository.JwtEncodeKey;

/**
 * Tests the {@link JwtAuthority} implementation for access tokens.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtAuthorityAccessTokenTest extends AbstractJwtAuthorityTokenTest {

	/**
	 * Ensure able to inject {@link JwtAuthority}.
	 */
	public void testEnsureAuthorityAvailable() {
		boolean isAvailable = this.doAuthorityTest((authority) -> authority != null);
		assertTrue("JWT authority should be available", isAvailable);
	}

	/**
	 * Ensure able to generate access token.
	 */
	public void testCreateAccessToken() {
		String accessToken = this.createAccessToken();
		this.claims.assertAccessToken(accessToken, keyPair.getPublic(), mockCurrentTime);
	}

	/**
	 * Ensure issue if {@link JwtEncodeKey} expiration period is too short.
	 */
	public void testEncodeKeyExpirationTooShort() throws Exception {

		// Record issue in configuration
		this.compilerIssues = new MockCompilerIssues(this);
		this.compilerIssues.recordCaptureIssues(false);
		this.compilerIssues.recordIssue("JWT_AUTHORITY", ManagedObjectSourceNodeImpl.class, "Failed to init",
				new IllegalArgumentException(
						"JwtEncodeKey expiration period (8 seconds) is below overlap period ((1 seconds period * 4 periods = 4 seconds) * 2 for overlap start/end = 8 seconds)"));

		// Ensure issue if encode key period too short (no overlap buffer)
		this.replayMockObjects();
		this.loadOfficeFloor(JwtAuthorityManagedObjectSource.PROPERTY_ACCESS_TOKEN_EXPIRATION_PERIOD, String.valueOf(1),
				JwtAuthorityManagedObjectSource.PROPERTY_ENCODE_KEY_OVERLAP_PERIODS, String.valueOf(4),
				JwtAuthorityManagedObjectSource.PROPERTY_ENCODE_KEY_EXPIRATION_PERIOD, String.valueOf(8));
		this.verifyMockObjects();
		assertNull("Should not compile OfficeFloor", this.officeFloor);
	}

	/**
	 * Ensure issue if access token not a JSON object.
	 */
	public void testInvalidAccessToken() {
		AccessTokenException exception = this.doAuthorityTest((authority) -> {
			try {
				authority.createAccessToken(new String[] { "not", "an", "object" });
				return null;
			} catch (AccessTokenException ex) {
				return ex;
			}
		});
		assertNotNull("Should not successfully create access token", exception);
		assertEquals("Incorrect cause",
				IllegalArgumentException.class.getName()
						+ ": Must be JSON object (start end with {}) - but was [\"not\",\"an\",\"object\"]",
				exception.getMessage());
	}

	/**
	 * Ensure default the exp time.
	 */
	public void testDefaultPeriodFromNow() {
		this.claims.nbf = null;
		this.claims.exp = null;
		String accessToken = this.createAccessToken();
		this.claims.exp = mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;
		this.claims.assertAccessToken(accessToken, keyPair.getPublic(), mockCurrentTime);
	}

	/**
	 * Ensure nbf is always before exp.
	 */
	public void testInvalidClaimTimes() {
		this.claims.nbf = this.claims.exp + 1;
		this.assertInvalidAccessToken(IllegalArgumentException.class.getName() + ": nbf (" + this.claims.nbf
				+ ") must not be after exp (" + this.claims.exp + ")");
	}

	/**
	 * Ensure fails if creating access token instances in the past.
	 */
	public void testFailOnAttemptingPastAccessToken() {
		this.claims.nbf = mockCurrentTime - JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;
		this.claims.exp = mockCurrentTime;
		this.assertInvalidAccessToken(this.claims.getInvalidAccessTokenCause());
	}

	/**
	 * Ensure fails if creating access token too far into future.
	 */
	public void testFailOnAttemptingAccessTokenTooFarIntoFuture() {
		JwtEncodeKey key = this.mockEncodeKeys.get(1);
		this.claims.exp = key.getExpireTime();
		this.claims.nbf = this.claims.exp - JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;
		this.assertInvalidAccessToken(this.claims.getInvalidAccessTokenCause());
	}

	/**
	 * Ensure if access token spans longer than {@link JwtEncodeKey} period that
	 * invalid.
	 */
	public void testFailOnAttemptingAccessTokenWithTooLongerPeriod() {
		this.claims.exp = JwtAuthorityManagedObjectSource.DEFAULT_ENCODE_KEY_EXPIRATION_PERIOD + 1;
		this.assertInvalidAccessToken(this.claims.getInvalidAccessTokenCause());
	}

	/**
	 * Ensure creates the {@link JwtEncodeKey} instances (should none be available
	 * at start up).
	 */
	public void testNoEncodeKeysOnStartup() {

		// Clear keys and start server (should generate keys)
		this.mockEncodeKeys.clear();
		String accessToken = this.createAccessToken();

		// Ensure correct request time loaded
		assertEquals("Incorrect request time for keys",
				mockCurrentTime - JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD,
				this.retrieveJwtEncodeKeysTime.getEpochSecond());

		// Determine default overlap time
		long overlapTime = JwtAuthorityManagedObjectSource.MINIMUM_ENCODE_KEY_OVERLAP_PERIODS
				* JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;

		// Should generate keys
		assertEquals("Should generate new encode keys", 2, this.mockEncodeKeys.size());
		JwtEncodeKey firstKey = this.mockEncodeKeys.get(0);
		long firstKeyStart = mockCurrentTime - overlapTime;
		assertEquals("Incorrect first key start", firstKeyStart, firstKey.getStartTime());
		assertEquals("Incorrect first key expire",
				firstKeyStart + JwtAuthorityManagedObjectSource.DEFAULT_ENCODE_KEY_EXPIRATION_PERIOD,
				firstKey.getExpireTime());

		// Ensure able to use new key
		this.claims.assertAccessToken(accessToken, firstKey.getPublicKey(), mockCurrentTime);

		// Ensure second key overlaps
		long secondKeyStart = firstKeyStart + JwtAuthorityManagedObjectSource.DEFAULT_ENCODE_KEY_EXPIRATION_PERIOD
				- overlapTime;
		JwtEncodeKey secondKey = this.mockEncodeKeys.get(1);
		assertEquals("Incorrect second key start", secondKeyStart, secondKey.getStartTime());
		assertEquals("Incorrect second key expire",
				secondKeyStart + JwtAuthorityManagedObjectSource.DEFAULT_ENCODE_KEY_EXPIRATION_PERIOD,
				secondKey.getExpireTime());
	}

	/**
	 * Ensure creates new key as required on refresh.
	 */
	public void testCreateNextEncodeKey() {

		// Obtain the access token
		String accessToken = this.createAccessToken();
		this.claims.assertAccessToken(accessToken, keyPair.getPublic(), mockCurrentTime);
		assertEquals("Should just be the two setup keys", 2, this.mockEncodeKeys.size());

		// Move time forward and refresh (so loads new key)
		long renewKeyTime = mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_ENCODE_KEY_EXPIRATION_PERIOD;
		this.clockFactory.setCurrentTimeSeconds(renewKeyTime);
		this.doAuthorityTest((authority) -> {
			authority.reloadAccessKeys();

			// Create access token (waits for keys to be reloaded)
			return authority.createAccessToken(this.claims);
		});

		// Ensure now have three keys
		assertEquals("Should create new key (as old about to expire)", 3, this.mockEncodeKeys.size());

		// Third key should appropriately overlap second key
		JwtEncodeKey secondKey = this.mockEncodeKeys.get(1);
		long expectedStartTime = secondKey.getExpireTime()
				- (JwtAuthorityManagedObjectSource.MINIMUM_ENCODE_KEY_OVERLAP_PERIODS
						* JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD);
		JwtEncodeKey newKey = this.mockEncodeKeys.get(2);
		assertEquals("Incorrect new key start", expectedStartTime, newKey.getStartTime());
		assertEquals("Incorrect new key expire",
				expectedStartTime + JwtAuthorityManagedObjectSource.DEFAULT_ENCODE_KEY_EXPIRATION_PERIOD,
				newKey.getExpireTime());

		// Ensure as time moved forward (that uses second key to encode)
		this.claims.nbf = null;
		this.claims.exp = null;
		accessToken = this.createAccessToken();
		this.claims.exp = renewKeyTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;
		this.claims.assertAccessToken(accessToken, secondKey.getPublicKey(), renewKeyTime);
	}

}