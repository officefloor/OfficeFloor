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

package net.officefloor.web.jwt.authority;

import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.web.jwt.authority.repository.JwtAccessKey;
import net.officefloor.web.jwt.validate.JwtValidateKey;

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
		AccessToken accessToken = this.createAccessToken();
		assertEquals("Incorrect expire time",
				mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD,
				accessToken.getExpireTime());
		this.claims.assertAccessToken(accessToken.getToken(), keyPair.getPublic(), mockCurrentTime);
	}

	/**
	 * Ensure issue if {@link JwtAccessKey} expiration period is too short.
	 */
	public void testAccessKeyExpirationTooShort() throws Exception {

		// Record issue in configuration
		this.compilerIssues = new MockCompilerIssues(this);
		this.compilerIssues.recordCaptureIssues(false);
		this.compilerIssues.recordCaptureIssues(false);
		this.compilerIssues.recordIssue("OFFICE.JWT_AUTHORITY", ManagedObjectSourceNodeImpl.class, "Failed to init",
				new IllegalArgumentException(
						"JwtAccessKey expiration period (8 seconds) is below overlap period ((1 seconds period * 4 periods = 4 seconds) * 2 for overlap start/end = 8 seconds)"));

		// Ensure issue if key period too short (no overlap buffer)
		this.replayMockObjects();
		this.loadOfficeFloor(JwtAuthorityManagedObjectSource.PROPERTY_ACCESS_TOKEN_EXPIRATION_PERIOD, String.valueOf(1),
				JwtAuthorityManagedObjectSource.PROPERTY_ACCESS_KEY_OVERLAP_PERIODS, String.valueOf(4),
				JwtAuthorityManagedObjectSource.PROPERTY_ACCESS_KEY_EXPIRATION_PERIOD, String.valueOf(8));
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
		AccessToken accessToken = this.createAccessToken();
		this.claims.exp = mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;
		assertEquals("Incorrect expire time", this.claims.exp.longValue(), accessToken.getExpireTime());
		this.claims.assertAccessToken(accessToken.getToken(), keyPair.getPublic(), mockCurrentTime);
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
		JwtAccessKey key = this.mockAccessKeys.get(1);
		this.claims.exp = key.getExpireTime();
		this.claims.nbf = this.claims.exp - JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;
		this.assertInvalidAccessToken(this.claims.getInvalidAccessTokenCause());
	}

	/**
	 * Ensure if access token spans longer than {@link JwtAccessKey} period that
	 * invalid.
	 */
	public void testFailOnAttemptingAccessTokenWithTooLongerPeriod() {
		this.claims.exp = JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD + 1;
		this.assertInvalidAccessToken(this.claims.getInvalidAccessTokenCause());
	}

	/**
	 * Ensure creates the {@link JwtAccessKey} instances (should none be available
	 * at start up).
	 */
	public void testNoAccessKeysOnStartup() {

		// Clear keys and start server (should generate keys)
		this.mockAccessKeys.clear();
		AccessToken accessToken = this.createAccessToken();

		// Ensure correct request time loaded
		assertEquals("Incorrect request time for keys",
				Long.valueOf(mockCurrentTime - JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD),
				this.retrieveJwtAccessKeysTime);

		// Determine default overlap time
		long overlapTime = JwtAuthorityManagedObjectSource.MINIMUM_ACCESS_KEY_OVERLAP_PERIODS
				* JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;

		// Should generate keys
		assertEquals("Should generate new keys", 2, this.mockAccessKeys.size());
		JwtAccessKey firstKey = this.mockAccessKeys.get(0);
		long firstKeyStart = mockCurrentTime - overlapTime;
		assertEquals("Incorrect first key start", firstKeyStart, firstKey.getStartTime());
		assertEquals("Incorrect first key expire",
				firstKeyStart + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD,
				firstKey.getExpireTime());

		// Ensure able to use new key
		this.claims.assertAccessToken(accessToken.getToken(), firstKey.getPublicKey(), mockCurrentTime);

		// Ensure second key overlaps
		long secondKeyStart = firstKeyStart + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD
				- overlapTime;
		JwtAccessKey secondKey = this.mockAccessKeys.get(1);
		assertEquals("Incorrect second key start", secondKeyStart, secondKey.getStartTime());
		assertEquals("Incorrect second key expire",
				secondKeyStart + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD,
				secondKey.getExpireTime());

		// Ensure valid validate keys
		JwtValidateKey[] validateKeys = this.doAuthorityTest((authority) -> authority.getActiveJwtValidateKeys());
		assertEquals("Incorrect number of validate keys", 2, validateKeys.length);
		for (int i = 0; i < this.mockAccessKeys.size(); i++) {
			JwtAccessKey accessKey = this.mockAccessKeys.get(i);
			JwtValidateKey validateKey = validateKeys[i];
			assertNotSame("Should wrap key to avoid easy class cast access to private key: " + i, accessKey,
					validateKey);
			assertEquals("Incorrect start time: " + i, accessKey.getStartTime(), validateKey.getStartTime());
			assertEquals("Incorrect expire time: " + i, accessKey.getExpireTime(), validateKey.getExpireTime());
			assertSame("Incorrect public key: " + i, accessKey.getPublicKey(), validateKey.getKey());
		}
	}

	/**
	 * Ensure creates new key as required on refresh.
	 */
	public void testCreateNextAccessKey() {

		// Obtain the access token
		AccessToken accessToken = this.createAccessToken();
		this.claims.assertAccessToken(accessToken.getToken(), keyPair.getPublic(), mockCurrentTime);
		assertEquals("Should just be the two setup keys", 2, this.mockAccessKeys.size());

		// Move time forward and refresh (so loads new key)
		long renewKeyTime = mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD;
		this.clockFactory.setCurrentTimeSeconds(renewKeyTime);
		this.doAuthorityTest((authority) -> {
			authority.reloadAccessKeys();

			// Create access token (waits for keys to be reloaded)
			return authority.createAccessToken(this.claims);
		});

		// Ensure now have three keys
		assertEquals("Should create new key (as old about to expire)", 3, this.mockAccessKeys.size());

		// Third key should appropriately overlap second key
		JwtAccessKey secondKey = this.mockAccessKeys.get(1);
		long expectedStartTime = secondKey.getExpireTime()
				- (JwtAuthorityManagedObjectSource.MINIMUM_ACCESS_KEY_OVERLAP_PERIODS
						* JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD);
		JwtAccessKey newKey = this.mockAccessKeys.get(2);
		assertEquals("Incorrect new key start", expectedStartTime, newKey.getStartTime());
		assertEquals("Incorrect new key expire",
				expectedStartTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD,
				newKey.getExpireTime());

		// Ensure as time moved forward (that uses second key to encode)
		this.claims.nbf = null;
		this.claims.exp = null;
		accessToken = this.createAccessToken();
		this.claims.exp = renewKeyTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;
		this.claims.assertAccessToken(accessToken.getToken(), secondKey.getPublicKey(), renewKeyTime);

		// Ensure valid validate keys
		JwtValidateKey[] validateKeys = this.doAuthorityTest((authority) -> authority.getActiveJwtValidateKeys());
		assertEquals("Incorrect number of validate keys", 3, validateKeys.length);
		for (int i = 0; i < this.mockAccessKeys.size(); i++) {
			JwtAccessKey accessKey = this.mockAccessKeys.get(i);
			JwtValidateKey validateKey = validateKeys[i];
			assertNotSame("Should wrap key to avoid easy class cast access to private key: " + i, accessKey,
					validateKey);
			assertEquals("Incorrect start time: " + i, accessKey.getStartTime(), validateKey.getStartTime());
			assertEquals("Incorrect expire time: " + i, accessKey.getExpireTime(), validateKey.getExpireTime());
			assertSame("Incorrect public key: " + i, accessKey.getPublicKey(), validateKey.getKey());
		}
	}

}
