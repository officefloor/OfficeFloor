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

import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyPair;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.managedobject.poll.StatePollContext;
import net.officefloor.plugin.managedobject.poll.StatePoller;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriter;
import net.officefloor.web.jwt.authority.jwks.JwksPublishSectionSource;
import net.officefloor.web.jwt.authority.key.AesCipherFactory;
import net.officefloor.web.jwt.authority.key.AesSynchronousKeyFactory;
import net.officefloor.web.jwt.authority.key.AsynchronousKeyFactory;
import net.officefloor.web.jwt.authority.key.CipherFactory;
import net.officefloor.web.jwt.authority.key.Rsa256AynchronousKeyFactory;
import net.officefloor.web.jwt.authority.key.SynchronousKeyFactory;
import net.officefloor.web.jwt.authority.repository.JwtAccessKey;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityKey;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityRepository;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityRepository.RetrieveKeysContext;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityRepository.SaveKeysContext;
import net.officefloor.web.jwt.authority.repository.JwtRefreshKey;
import net.officefloor.web.jwt.jwks.JwksKeyParser;
import net.officefloor.web.jwt.jwks.JwksSectionSource;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * <p>
 * {@link JwtAuthority} {@link ManagedObjectSource}.
 * <p>
 * Key activation period is as follows:
 * <ol>
 * <li>Access/Refresh token - expire period</li>
 * <li>Encode access/refresh token keys - refreshed every expire period and key
 * must be active for minimum number of expire periods</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public class JwtAuthorityManagedObjectSource
		extends AbstractManagedObjectSource<None, JwtAuthorityManagedObjectSource.Flows> {

	/**
	 * {@link Flow} keys.
	 */
	public static enum Flows {
		RETRIEVE_ENCODE_KEYS, RETRIEVE_REFRESH_KEYS
	}

	/**
	 * Dependencies for {@link ManagedFunction} to retrieve the {@link JwtAccessKey}
	 * instances.
	 */
	private static enum RetrieveKeysDependencies {
		COLLECTOR, JWT_AUTHORITY_REPOSITORY
	}

	/**
	 * {@link Property} name for the identity {@link Class}.
	 */
	public static final String PROPERTY_IDENTITY_CLASS = "identity.class";

	/**
	 * {@link Property} name for the expiration period for access token. Period
	 * measured in seconds.
	 */
	public static final String PROPERTY_ACCESS_TOKEN_EXPIRATION_PERIOD = "access.token.expiration.period";

	/**
	 * Default expiration period for access tokens.
	 */
	public static final long DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD = TimeUnit.MINUTES.toSeconds(20);

	/**
	 * {@link Property} name for number of overlap access token periods for the
	 * {@link JwtAccessKey} instances.
	 */
	public static final String PROPERTY_ACCESS_KEY_OVERLAP_PERIODS = "access.key.token.overlap.periods";

	/**
	 * Minimum number of overlap access token periods for he {@link JwtAccessKey}
	 * instances.
	 */
	public static final int MINIMUM_ACCESS_KEY_OVERLAP_PERIODS = 3;

	/**
	 * {@link Property} name for the expiration period for the {@link JwtAccessKey}.
	 * Period measured in seconds.
	 */
	public static final String PROPERTY_ACCESS_KEY_EXPIRATION_PERIOD = "access.key.expiration.period";

	/**
	 * Default expiration period for {@link JwtAccessKey}.
	 */
	public static final long DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD = TimeUnit.DAYS.toSeconds(7);

	/**
	 * {@link Property} for the {@link AsynchronousKeyFactory} {@link Class} for the
	 * {@link JwtAccessKey}.
	 */
	public static final String PROPERTY_ACCESS_TOKEN_KEY_FACTORY = "access.token.key.factory";

	/**
	 * Default {@link JwtAccessKey} {@link AsynchronousKeyFactory}.
	 */
	public static final String DEFAULT_ACCESS_TOKEN_KEY_FACTORY = Rsa256AynchronousKeyFactory.class.getName();

	/**
	 * {@link Property} name for the expiration period for refresh token. Period
	 * measured in seconds.
	 */
	public static final String PROPERTY_REFRESH_TOKEN_EXPIRATION_PERIOD = "refresh.token.expiration.period";

	/**
	 * Default expiration period for refresh tokens.
	 */
	public static final long DEFAULT_REFRESH_TOKEN_EXPIRATION_PERIOD = TimeUnit.HOURS.toSeconds(8);

	/**
	 * {@link Property} name for number of overlap refresh token periods for the
	 * {@link JwtRefreshKey} instances.
	 */
	public static final String PROPERTY_REFRESH_KEY_OVERLAP_PERIODS = "refresh.key.token.overlap.periods";

	/**
	 * Minimum number of overlap refresh token periods for he {@link JwtRefreshKey}
	 * instances.
	 */
	public static final int MINIMUM_REFRESH_KEY_OVERLAP_PERIODS = 3;

	/**
	 * {@link Property} name for the expiration period for the
	 * {@link JwtRefreshKey}. Period measured in seconds.
	 */
	public static final String PROPERTY_REFRESH_KEY_EXPIRATION_PERIOD = "refresh.key.expiration.period";

	/**
	 * Default expiration period for {@link JwtRefreshKey}.
	 */
	public static final long DEFAULT_REFRESH_KEY_EXPIRATION_PERIOD = TimeUnit.DAYS.toSeconds(28);

	/**
	 * {@link Property} for the {@link JwtRefreshKey} {@link CipherFactory}.
	 */
	public static final String PROPERTY_REFRESH_TOKEN_CIPHER_FACTORY = "refresh.token.cipher.factory";

	/**
	 * Default {@link JwtRefreshKey} {@link CipherFactory}.
	 */
	public static final String DEFAULT_REFRESH_TOKEN_CIPHER_FACTORY = AesCipherFactory.class.getName();

	/**
	 * {@link Property} for the {@link SynchronousKeyFactory} {@link Class} for the
	 * {@link JwtRefreshKey}.
	 */
	public static final String PROPERTY_REFRESH_TOKEN_KEY_FACTORY = "refresh.token.key.factory";

	/**
	 * Default {@link JwtRefreshKey} {@link SynchronousKeyFactory}.
	 */
	public static final String DEFAULT_REFRESH_TOKEN_KEY_FACTORY = AesSynchronousKeyFactory.class.getName();

	/**
	 * {@link Property} for the wait time for loading {@link JwtRefreshKey} and
	 * {@link JwtAccessKey} instances. Time measured in seconds.
	 */
	public static final String PROPERTY_KEY_LOAD_WAIT_TIME = "key.load.wait.time";

	/**
	 * Default wait time for the {@link JwtRefreshKey} and {@link JwtAccessKey}
	 * instances to be available.
	 */
	public static final int DEFAULT_KEY_LOAD_WAIT_TIME = 3;

	/**
	 * {@link Charset}.
	 */
	private static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * {@link DateTimeFormatter} for writing out times.
	 */
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
			.withZone(ZoneId.systemDefault());

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link TimeWindow} {@link JavaType}.
	 */
	private static final JavaType timeWindowJavaType = mapper.constructType(TimeWindow.class);

	static {
		// Ensure JSON deserialising is valid
		if (!mapper.canDeserialize(timeWindowJavaType)) {
			throw new IllegalStateException("Unable to deserialize " + TimeWindow.class.getSimpleName());
		}

		// Ensure ignore unknown properties (avoid added "exp" causing problems)
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * Generates a random string.
	 * 
	 * @param minLength Minimum length of the string.
	 * @param maxLength Maximum length of the string.
	 * @return Random string.
	 */
	public static String randomString(int minLength, int maxLength) {

		// Obtain the random
		Random random = ThreadLocalRandom.current();

		// Generate random length
		int length;
		if (minLength == maxLength) {
			length = maxLength;
		} else {
			length = minLength + random.nextInt(maxLength - minLength);
		}

		// Generate the random string
		int increase = 1;
		for (;;) {
			byte[] bytes = new byte[length * increase];
			random.nextBytes(bytes);
			String value = Base64.getUrlEncoder().encodeToString(bytes);
			if (value.length() >= length) {
				return value.substring(0, length);
			}
			increase++;
		}
	}

	/**
	 * Encrypts the value.
	 * 
	 * @param key           {@link Key}.
	 * @param initVector    Initialise vector.
	 * @param startSalt     Start salt.
	 * @param laceBytes     Lace.
	 * @param endSalt       End salt.
	 * @param value         Value.
	 * @param cipherFactory {@link CipherFactory}.
	 * @return Encrypted value.
	 * @throws Exception If fails to encrypt value.
	 */
	public static String encrypt(Key key, byte[] initVector, byte[] startSalt, byte[] laceBytes, byte[] endSalt,
			String value, CipherFactory cipherFactory) throws Exception {

		// Obtain the bytes to encrypt
		byte[] valueBytes = value.getBytes(UTF8);

		// Create array for salted value
		byte[] salted = new byte[startSalt.length + (valueBytes.length * 2) + endSalt.length];

		// Copy in the start salt
		System.arraycopy(startSalt, 0, salted, 0, startSalt.length);

		// Copy in the laced value
		for (int i = 0; i < valueBytes.length; i++) {
			int laceIndex = i % laceBytes.length;
			byte laceByte = laceBytes[laceIndex];

			// Load the values
			int pairIndex = startSalt.length + (i * 2);
			salted[pairIndex] = laceByte;
			salted[pairIndex + 1] = valueBytes[i];
		}

		// Copy in the end salt
		System.arraycopy(endSalt, 0, salted, startSalt.length + (valueBytes.length * 2), endSalt.length);

		// Encrypt
		IvParameterSpec iv = new IvParameterSpec(initVector);
		Cipher cipher = cipherFactory.createCipher();
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] encrypted = cipher.doFinal(salted);

		// Return in URL safe content
		return Base64.getUrlEncoder().encodeToString(encrypted);
	}

	/**
	 * Decrypts the value.
	 * 
	 * @param key           {@link Key}.
	 * @param initVector    Initialise vector.
	 * @param startSalt     Start salt.
	 * @param laceBytes     Lace.
	 * @param endSalt       End salt.
	 * @param cipherText    Encrypted value.
	 * @param cipherFactory {@link CipherFactory}.
	 * @return Plaintext value.
	 * @throws Exception If fails to decrypt value.
	 */
	public static String decrypt(Key key, byte[] initVector, byte[] startSalt, byte[] laceBytes, byte[] endSalt,
			String cipherText, CipherFactory cipherFactory) throws Exception {

		// Obtain the encrypted bytes
		byte[] encrypted = Base64.getUrlDecoder().decode(cipherText);

		// Decrypt
		IvParameterSpec iv = new IvParameterSpec(initVector);
		Cipher cipher = cipherFactory.createCipher();
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] salted = cipher.doFinal(encrypted);

		// Ensure starts with start salt
		if ((startSalt.length + endSalt.length) > salted.length) {
			return null; // too short
		}

		// Ensure start salt matches
		for (int i = 0; i < startSalt.length; i++) {
			if (salted[i] != startSalt[i]) {
				return null; // not match start salt
			}
		}

		// Ensure the end salt matches
		int endSaltOffset = salted.length - endSalt.length;
		for (int i = 0; i < endSalt.length; i++) {
			if (salted[endSaltOffset + i] != endSalt[i]) {
				return null; // not match end salt
			}
		}

		// Pull out value (ensuring correct lacing)
		int valueLength = (salted.length - startSalt.length - endSalt.length) / 2;
		byte[] value = new byte[valueLength];
		for (int i = 0; i < valueLength; i++) {

			// Obtain expected lace byte
			int laceIndex = i % laceBytes.length;
			byte expectedLaceByte = laceBytes[laceIndex];

			// Determine pair index
			int pairIndex = startSalt.length + (i * 2);

			// Ensure correct lace byte
			byte checkLaceByte = salted[pairIndex];
			if (checkLaceByte != expectedLaceByte) {
				return null; // invalid
			}

			// Obtain the value
			value[i] = salted[pairIndex + 1];
		}

		// Return the value
		return new String(value, UTF8);
	}

	/**
	 * Identity {@link Class}.
	 */
	private Class<?> identityClass;

	/**
	 * Identity {@link JavaType}.
	 */
	private JavaType identityJavaType;

	/**
	 * Default time in seconds expire the access token.
	 */
	private long accessTokenExpirationPeriod;

	/**
	 * Time in seconds to expire the {@link JwtAccessKey}.
	 */
	private long accessKeyExpirationPeriod;

	/**
	 * Number of overlap access token periods for the {@link JwtAccessKey}
	 * instances.
	 */
	private int accessKeyOverlapPeriods;

	/**
	 * {@link JwtAccessKey} {@link AsynchronousKeyFactory}.
	 */
	private AsynchronousKeyFactory accessTokenKeyFactory;

	/**
	 * Default time in seconds expire the refresh token.
	 */
	private long refreshTokenExpirationPeriod;

	/**
	 * Time in seconds to expire the {@link JwtRefreshKey}.
	 */
	private long refreshKeyExpirationPeriod;

	/**
	 * Number of overlap refresh token periods for the {@link JwtRefreshKey}
	 * instances.
	 */
	private int refreshKeyOverlapPeriods;

	/**
	 * {@link JwtRefreshKey} {@link CipherFactory}.
	 */
	private CipherFactory refreshTokenCipherFactory;

	/**
	 * {@link JwtRefreshKey} {@link SynchronousKeyFactory}.
	 */
	private SynchronousKeyFactory refreshTokenKeyFactory;

	/**
	 * {@link Clock} to obtain time in seconds.
	 */
	private Clock<Long> timeInSeconds;

	/**
	 * Wait time in seconds for the {@link JwtRefreshKey} and {@link JwtAccessKey}
	 * instances.
	 */
	private long keyLoadWaitTime;

	/**
	 * {@link StatePoller} to keep the {@link JwtAccessKey} instances up to date
	 * with appropriate keys.
	 */
	private StatePoller<JwtAccessKey[], Flows> jwtAccessKeys;

	/**
	 * {@link StatePoller} to keep the {@link JwtRefreshKey} instances up to date
	 * with appropriate keys.
	 */
	private StatePoller<JwtRefreshKey[], Flows> jwtRefreshKeys;

	/**
	 * Factory to create the {@link RetrieveKeysContext}.
	 */
	private Function<Long, RetrieveKeysContext> retrieveKeysContextFactory;

	/**
	 * {@link SaveKeysContext}.
	 */
	private SaveKeysContext saveKeysContext;

	/**
	 * {@link JwtAuthorityKey} retriever.
	 */
	@FunctionalInterface
	private static interface JwtAuthorityKeyRetriever<K extends JwtAuthorityKey> {

		/**
		 * Retrieve the {@link JwtAuthorityKey} instances.
		 * 
		 * @param loadTime   Load time.
		 * @param repository {@link JwtAuthorityRepository}.
		 * @return {@link JwtAuthorityKey} instances.
		 * @throws Exception If fails to retrieve the {@link JwtAuthorityKey} instances.
		 */
		K[] retrieveKeys(long loadTime, JwtAuthorityRepository repository) throws Exception;
	}

	/**
	 * {@link JwtAuthorityKey} saver.
	 */
	@FunctionalInterface
	private static interface JwtAuthorityKeySaver<K extends JwtAuthorityKey> {

		/**
		 * Saves the {@link JwtAuthorityKey} instances.
		 * 
		 * @param keys       {@link JwtAuthorityKey} instances to save.
		 * @param repository {@link JwtAuthorityRepository}.
		 * @throws Exception If fails to save the {@link JwtAuthorityKey} instances.
		 */
		void saveKeys(List<K> keys, JwtAuthorityRepository repository) throws Exception;
	}

	/**
	 * Creates a new {@link JwtAuthorityKey}.
	 */
	@FunctionalInterface
	private static interface JwtAuthorityKeyFactory<K extends JwtAuthorityKey> {

		/**
		 * Creates a new {@link JwtAuthorityKey}.
		 * 
		 * @param startTime Start time for the {@link JwtAuthorityKey}.
		 * @return New {@link JwtAuthorityKey}.
		 * @throws Exception If fails to create new {@link JwtAuthorityKey}.
		 */
		K createJwtAuthorityKey(long startTime) throws Exception;
	}

	/**
	 * Loads the {@link JwtAuthorityKey} instances to cover the up coming time
	 * period.
	 * 
	 * @param repository            {@link JwtAuthorityRepository}.
	 * @param tokenExpirationPeriod Token expiration period.
	 * @param overlapPeriods        Number of token expiration periods to overlap
	 *                              key period.
	 * @param keyExpirationPeriod   Key expiration period.
	 * @param keyRetriever          {@link JwtAuthorityKeyRetriever}.
	 * @param keySaver              {@link JwtAuthorityKeySaver}.
	 * @param keyFactory            {@link JwtAuthorityKeyFactory}.
	 * @return {@link JwtAuthorityKey} instances to cover the up coming time period.
	 * @throws Exception If fails to load the {@link JwtAuthorityKey} instances.
	 */
	private <K extends JwtAuthorityKey> K[] loadKeysForCoverage(JwtAuthorityRepository repository,
			long tokenExpirationPeriod, int overlapPeriods, long keyExpirationPeriod,
			JwtAuthorityKeyRetriever<K> keyRetriever, JwtAuthorityKeySaver<K> keySaver,
			JwtAuthorityKeyFactory<K> keyFactory) throws Exception {

		// Obtain time
		long currentTimeSeconds = this.timeInSeconds.getTime();

		// Obtain the reload time
		long reloadTime = currentTimeSeconds - tokenExpirationPeriod;

		// Obtain the active keys
		K[] activeKeys = keyRetriever.retrieveKeys(reloadTime, repository);

		// Determine minimum period to have active keys
		long overlapTime = tokenExpirationPeriod * overlapPeriods;
		long activeUntilTime = currentTimeSeconds - overlapTime + keyExpirationPeriod - overlapTime
				+ keyExpirationPeriod;

		// Determine if have coverage
		long coverageTime = this.calculateKeyCoverageUntil(currentTimeSeconds, activeUntilTime, activeKeys);
		if (coverageTime <= activeUntilTime) {

			// Keep track of the new keys (likely only one)
			List<K> newKeys = new ArrayList<>(1);

			// Period is not covered, so must create new keys
			repository.doClusterCriticalSection((contextRepository) -> {

				// As potential cluster lock, reload state
				K[] coverageKeys = keyRetriever.retrieveKeys(reloadTime, contextRepository);

				// Determine coverage (as may have changed keys)
				long coverage = this.calculateKeyCoverageUntil(currentTimeSeconds, activeUntilTime, coverageKeys);

				// Ensure full coverage (creating keys as necessary)
				while (coverage < activeUntilTime) {

					// Create the key
					long startTime = coverage - overlapTime;
					K newKey = keyFactory.createJwtAuthorityKey(startTime);

					// Include key
					newKeys.add(newKey);
					coverageKeys = Arrays.copyOf(coverageKeys, coverageKeys.length + 1);
					coverageKeys[coverageKeys.length - 1] = newKey;

					// Determine the new coverage
					coverage = this.calculateKeyCoverageUntil(coverage, activeUntilTime, coverageKeys);
				}

				// Save the new keys
				keySaver.saveKeys(newKeys, contextRepository);
			});

			// As keys saved, include in encode keys
			activeKeys = Arrays.copyOf(activeKeys, activeKeys.length + newKeys.size());
			for (int i = 0; i < newKeys.size(); i++) {
				activeKeys[activeKeys.length - newKeys.size() + i] = newKeys.get(i);
			}
		}

		// Return the keys
		return activeKeys;
	}

	/**
	 * Calculates the key coverage from start time until end time.
	 * 
	 * @param startTimeSeconds Start time in seconds since Epoch.
	 * @param endTimeSeconds   End time in seconds since Epoch.
	 * @param authorityKeys    Available {@link JwtAuthorityKey} instances.
	 * @return Time in seconds from start time to coverage by
	 *         {@link JwtAuthorityKey} instances.
	 */
	private long calculateKeyCoverageUntil(long startTimeSeconds, long endTimeSeconds,
			JwtAuthorityKey[] authorityKeys) {

		// Determine coverage
		long coverageTime = startTimeSeconds;
		long lastRunTime;
		do {
			long startTime = coverageTime;
			lastRunTime = coverageTime;
			NEXT_KEY: for (JwtAuthorityKey authorityKey : authorityKeys) {

				// Ignore if not overlaps at start
				if (authorityKey.getStartTime() > startTime) {
					continue NEXT_KEY; // does not cover start
				}

				// Determine if covers greater time
				if (authorityKey.getExpireTime() > coverageTime) {
					coverageTime = authorityKey.getExpireTime();
				}
			}

			// Loop until coverage (or no further coverage)
		} while ((coverageTime <= endTimeSeconds) && (lastRunTime != coverageTime));

		// Return the time covered until
		return coverageTime;
	}

	/**
	 * Tokenises the payload with the key.
	 */
	@FunctionalInterface
	private static interface Tokeniser<K extends JwtAuthorityKey, J> {

		/**
		 * Tokenises the payload with the key.
		 * 
		 * @param payload    Payload.
		 * @param key        {@link JwtAuthorityKey}.
		 * @param expireTime Expire time.
		 * @return Token.
		 * @throws Exception If fails to tokenise the payload.
		 */
		J tokenise(String payload, K key, long expireTime) throws Exception;
	}

	/**
	 * Creates the token.
	 * 
	 * @param content               Content for the token.
	 * @param tokenExpirationPeriod Token expiration period.
	 * @param poller                {@link StatePoller} for the
	 *                              {@link JwtAuthorityKey} instances.
	 * @param exceptionFactory      Factory for {@link Exception}.
	 * @param tokeniser             {@link Tokeniser}.
	 * @return Token.
	 * @throws T If fails to create the token.
	 */
	private <J, K extends JwtAuthorityKey, T extends Exception> J createToken(Object content,
			long tokenExpirationPeriod, StatePoller<K[], ?> poller,
			BiFunction<HttpStatus, Exception, T> exceptionFactory, Tokeniser<K, J> tokeniser) throws T {

		// Easy access to source
		JwtAuthorityManagedObjectSource source = JwtAuthorityManagedObjectSource.this;

		// Obtain the payload
		String payload;
		try {
			payload = mapper.writeValueAsString(content).trim();
		} catch (Exception ex) {
			throw new AccessTokenException(ex);
		}

		// Determine if JSON object (and where last bracket)
		int lastBracketIndex = payload.lastIndexOf('}');
		if (lastBracketIndex != (payload.length() - 1)) {
			throw exceptionFactory.apply(null,
					new IllegalArgumentException("Must be JSON object (start end with {}) - but was " + payload));
		}

		// Determine time window for token
		TimeWindow timeWindow;
		try {
			timeWindow = mapper.readValue(payload, timeWindowJavaType);
		} catch (Exception ex) {
			throw exceptionFactory.apply(null, ex);
		}

		// Obtain the current time
		long currentTime = source.timeInSeconds.getTime();

		// Obtain the not before time
		long notBeforeTime = currentTime;
		if (timeWindow.nbf != null) {
			notBeforeTime = timeWindow.nbf;
		}

		// Obtain the expire time (and ensure token expires)
		long expireTime;
		if (timeWindow.exp != null) {
			expireTime = timeWindow.exp;

		} else {
			// No expire, so calculate the default expire time
			expireTime = currentTime + tokenExpirationPeriod;

			// Append the expire time
			payload = payload.substring(0, lastBracketIndex) + ",\"exp\":" + expireTime + "}";
		}

		// Ensure valid time period
		if (notBeforeTime > expireTime) {
			throw exceptionFactory.apply(null, new IllegalArgumentException(
					"nbf (" + notBeforeTime + ") must not be after exp (" + expireTime + ")"));
		}

		// Obtain the keys
		K[] keys;
		try {
			keys = poller.getState(this.keyLoadWaitTime, TimeUnit.SECONDS);
		} catch (TimeoutException ex) {
			throw exceptionFactory.apply(HttpStatus.SERVICE_UNAVAILABLE, ex);
		}

		/*
		 * Find the most appropriate key:
		 * 
		 * - key must be active
		 * 
		 * - key must be active for so many default refreshes from now (allows key to be
		 * superseded by another).
		 * 
		 * - key is shortest time to expire (least risk if compromised)
		 */
		K selectedKey = null;
		long minimumExpireTime = expireTime + (2 * (expireTime - notBeforeTime));
		NEXT_KEY: for (K candidateKey : keys) {

			// Ensure key is active
			if (candidateKey.getStartTime() > notBeforeTime) {
				continue NEXT_KEY; // key not active
			}

			// Ensure key will not expire too early
			if (candidateKey.getExpireTime() < minimumExpireTime) {
				continue NEXT_KEY; // key expires too early
			}

			// Determine if shortest time
			if ((selectedKey != null) && (selectedKey.getExpireTime() < candidateKey.getExpireTime())) {
				continue NEXT_KEY; // expires later
			}

			// Use the key
			selectedKey = candidateKey;
		}

		// Ensure have key
		if (selectedKey == null) {

			// Trigger loading keys (to possibly generate new key)
			poller.poll();

			// Indicate no keys
			String notBeforeDateTime = dateTimeFormatter
					.format(Instant.ofEpochSecond(notBeforeTime).atZone(ZoneId.systemDefault()));
			String expireDateTime = dateTimeFormatter
					.format(Instant.ofEpochSecond(expireTime).atZone(ZoneId.systemDefault()));
			throw exceptionFactory.apply(null, new IllegalStateException(
					"No key available for encoding (nbf: " + notBeforeDateTime + ", exp: " + expireDateTime + ")"));
		}

		// Generate the token
		try {
			return tokeniser.tokenise(payload, selectedKey, expireTime);
		} catch (Exception ex) {
			throw exceptionFactory.apply(null, ex);
		}
	}

	/*
	 * =================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_IDENTITY_CLASS, "Identity Class");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
		ManagedObjectSourceContext<Flows> sourceContext = context.getManagedObjectSourceContext();

		// Obtain the identity class
		this.identityClass = sourceContext.loadClass(sourceContext.getProperty(PROPERTY_IDENTITY_CLASS));

		// Load and ensure valid identity class
		this.identityJavaType = mapper.constructType(this.identityClass);
		if (!mapper.canSerialize(this.identityClass)) {
			throw new IllegalStateException("Unable to serialize " + this.identityClass.getName());
		}
		if (!mapper.canDeserialize(this.identityJavaType)) {
			throw new IllegalStateException("Unable to deserialize " + this.identityClass.getName());
		}

		// Obtain access token properties
		this.accessTokenExpirationPeriod = Long.parseLong(sourceContext.getProperty(
				PROPERTY_ACCESS_TOKEN_EXPIRATION_PERIOD, String.valueOf(DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD)));
		this.accessKeyExpirationPeriod = Long.parseLong(sourceContext.getProperty(PROPERTY_ACCESS_KEY_EXPIRATION_PERIOD,
				String.valueOf(DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD)));
		this.accessKeyOverlapPeriods = Integer.parseInt(sourceContext.getProperty(PROPERTY_ACCESS_KEY_OVERLAP_PERIODS,
				String.valueOf(MINIMUM_ACCESS_KEY_OVERLAP_PERIODS)));

		// Obtain the refresh token properties
		this.refreshTokenExpirationPeriod = Long.parseLong(sourceContext.getProperty(
				PROPERTY_REFRESH_TOKEN_EXPIRATION_PERIOD, String.valueOf(DEFAULT_REFRESH_TOKEN_EXPIRATION_PERIOD)));
		this.refreshKeyExpirationPeriod = Long.parseLong(sourceContext.getProperty(
				PROPERTY_REFRESH_KEY_EXPIRATION_PERIOD, String.valueOf(DEFAULT_REFRESH_KEY_EXPIRATION_PERIOD)));
		this.refreshKeyOverlapPeriods = Integer.parseInt(sourceContext.getProperty(PROPERTY_REFRESH_KEY_OVERLAP_PERIODS,
				String.valueOf(MINIMUM_REFRESH_KEY_OVERLAP_PERIODS)));

		// Obtain the key load wait time
		this.keyLoadWaitTime = Long.parseLong(
				sourceContext.getProperty(PROPERTY_KEY_LOAD_WAIT_TIME, String.valueOf(DEFAULT_KEY_LOAD_WAIT_TIME)));

		// Load the access key factory
		String asynchronousKeyFactoryClassName = sourceContext.getProperty(PROPERTY_ACCESS_TOKEN_KEY_FACTORY,
				DEFAULT_ACCESS_TOKEN_KEY_FACTORY);
		this.accessTokenKeyFactory = (AsynchronousKeyFactory) sourceContext.loadClass(asynchronousKeyFactoryClassName)
				.getDeclaredConstructor().newInstance();

		// Load the refresh cipher factory
		String cipherFactoryClassName = sourceContext.getProperty(PROPERTY_REFRESH_TOKEN_CIPHER_FACTORY,
				DEFAULT_REFRESH_TOKEN_CIPHER_FACTORY);
		this.refreshTokenCipherFactory = (CipherFactory) sourceContext.loadClass(cipherFactoryClassName)
				.getDeclaredConstructor().newInstance();

		// Load the refresh key factory
		String synchronousKeyFactoryClassName = sourceContext.getProperty(PROPERTY_REFRESH_TOKEN_KEY_FACTORY,
				DEFAULT_REFRESH_TOKEN_KEY_FACTORY);
		this.refreshTokenKeyFactory = (SynchronousKeyFactory) sourceContext.loadClass(synchronousKeyFactoryClassName)
				.getDeclaredConstructor().newInstance();

		// Ensure appropriate timings
		long accessOverlapPeriod = this.accessKeyOverlapPeriods * this.accessTokenExpirationPeriod;
		long minimumAccessKeyPeriod = 2 * accessOverlapPeriod;
		if (this.accessKeyExpirationPeriod <= minimumAccessKeyPeriod) {
			throw new IllegalArgumentException(
					JwtAccessKey.class.getSimpleName() + " expiration period (" + this.accessKeyExpirationPeriod
							+ " seconds) is below overlap period ((" + this.accessTokenExpirationPeriod
							+ " seconds period * " + this.accessKeyOverlapPeriods + " periods = " + accessOverlapPeriod
							+ " seconds) * 2 for overlap start/end = " + minimumAccessKeyPeriod + " seconds)");
		}

		// Ensure appropriate timings
		long refreshOverlapPeriod = this.refreshKeyOverlapPeriods * this.refreshTokenExpirationPeriod;
		long minimumRefreshKeyPeriod = 2 * refreshOverlapPeriod;
		if (this.refreshKeyExpirationPeriod <= minimumRefreshKeyPeriod) {
			throw new IllegalArgumentException(JwtRefreshKey.class.getSimpleName() + " expiration period ("
					+ this.refreshKeyExpirationPeriod + " seconds) is below overlap period (("
					+ this.refreshTokenExpirationPeriod + " seconds period * " + this.refreshKeyOverlapPeriods
					+ " periods = " + refreshOverlapPeriod + " seconds) * 2 for overlap start/end = "
					+ minimumRefreshKeyPeriod + " seconds)");
		}

		// Load meta-data
		context.setObjectClass(JwtAuthority.class);
		context.setManagedObjectClass(JwtAuthorityManagedObject.class);

		// Configure flows
		context.addFlow(Flows.RETRIEVE_ENCODE_KEYS, JwtAccessKeyCollector.class);
		context.addFlow(Flows.RETRIEVE_REFRESH_KEYS, JwtRefreshKeyCollector.class);

		// Obtain the clock
		this.timeInSeconds = sourceContext.getClock((time) -> time);

		// Obtain the JWT Authority Repository dependency
		ManagedObjectFunctionDependency jwtAuthorityRepository = sourceContext
				.addFunctionDependency(JwtAuthorityRepository.class.getSimpleName(), JwtAuthorityRepository.class);

		// Create the factory for retrieve keys context
		JwksKeyParser[] keyParsers = JwksSectionSource.loadJwksKeyParsers(sourceContext);
		this.retrieveKeysContextFactory = (activeAfter) -> new RetrieveKeysContext() {
			@Override
			public long getActiveAfter() {
				return activeAfter;
			}

			@Override
			public Key deserialise(String serialisedKeyContent) {
				Key key = JwksSectionSource.parseKey(serialisedKeyContent, keyParsers);
				if (key == null) {

					// Obtain the error message
					StringBuilder msg = new StringBuilder();
					msg.append("No ");
					msg.append(JwksKeyParser.class.getSimpleName());
					msg.append(" available for key ");
					try {
						JsonNode keyNode = mapper.readTree(serialisedKeyContent);
						msg.append("{ ");
						Iterator<String> fieldNames = keyNode.fieldNames();
						boolean isFirst = true;
						while (fieldNames.hasNext()) {
							String fieldName = fieldNames.next();
							if (!isFirst) {
								msg.append(", ");
							}
							isFirst = false;
							msg.append("\"");
							msg.append(fieldName);
							msg.append("\":");
							switch (fieldName) {
							case "kty":
							case "alg":
								msg.append("\"");
								msg.append(keyNode.get(fieldName).asText());
								msg.append("\"");
								break;
							default:
								msg.append("***");
								break;
							}
						}
						msg.append(" }");
					} catch (Exception ex) {
						// Not JSON, so no content
						msg.append("<Non JSON>");
					}

					// Indicate invalid
					throw new IllegalArgumentException(msg.toString());
				}
				return key;
			}
		};

		// Create the save keys context
		JwksKeyWriter<?>[] keyWriters = JwksPublishSectionSource.loadJwksKeyWriters(sourceContext);
		this.saveKeysContext = new SaveKeysContext() {
			@Override
			public String serialise(Key key) {
				try {
					String serialisedKey = JwksPublishSectionSource.writeKey(key, keyWriters);
					if (serialisedKey == null) {
						throw new IllegalArgumentException("No " + JwksKeyWriter.class.getSimpleName()
								+ " available for key (algorithm " + key.getAlgorithm() + ", format " + key.getFormat()
								+ ", type " + key.getClass().getName() + ")");
					}
					return serialisedKey;
				} catch (Exception ex) {
					throw new IllegalArgumentException(ex);
				}
			}
		};

		// Obtain the init vector size
		int initVectorSize = this.refreshTokenCipherFactory.getInitVectorSize();

		// Function to handle retrieving encode keys
		ManagedObjectFunctionBuilder<RetrieveKeysDependencies, None> retrieveEncodeKeys = sourceContext
				.addManagedFunction(Flows.RETRIEVE_ENCODE_KEYS.name(), () -> (functionContext) -> {

					// Obtain the JWT authority repository
					JwtAccessKeyCollector collector = (JwtAccessKeyCollector) functionContext
							.getObject(RetrieveKeysDependencies.COLLECTOR);
					JwtAuthorityRepository repository = (JwtAuthorityRepository) functionContext
							.getObject(RetrieveKeysDependencies.JWT_AUTHORITY_REPOSITORY);

					// Obtain the keys
					JwtAccessKey[] encodeKeys = this.loadKeysForCoverage(repository, this.accessTokenExpirationPeriod,
							this.accessKeyOverlapPeriods, this.accessKeyExpirationPeriod, (loadTime, repo) -> {

								// Load keys from repository
								List<JwtAccessKey> keys = repo
										.retrieveJwtAccessKeys(this.retrieveKeysContextFactory.apply(loadTime));

								// Keep only the valid keys
								JwtAccessKey[] validKeys = keys.stream().map((key) -> new JwtAccessKeyImpl(key))
										.filter((key) -> key.isValid()).toArray(JwtAccessKey[]::new);

								// Return the valid keys
								return validKeys;

							},
							(newKeys, repo) -> {

								// Save the new keys
								repo.saveJwtAccessKeys(this.saveKeysContext,
										newKeys.toArray(new JwtAccessKey[newKeys.size()]));

							}, (startTime) -> {

								// Create the JWT access key
								long expireTime = startTime + this.accessKeyExpirationPeriod;
								KeyPair keyPair = this.accessTokenKeyFactory.createAsynchronousKeyPair();
								JwtAccessKeyImpl newAccessKey = new JwtAccessKeyImpl(startTime, expireTime,
										keyPair.getPrivate(), keyPair.getPublic());

								// Return the JWT access key
								return newAccessKey;
							});

					// Load the keys
					collector.setKeys(encodeKeys);
				});
		retrieveEncodeKeys.linkParameter(RetrieveKeysDependencies.COLLECTOR, JwtAccessKeyCollector.class);
		retrieveEncodeKeys.linkObject(RetrieveKeysDependencies.JWT_AUTHORITY_REPOSITORY, jwtAuthorityRepository);
		sourceContext.getFlow(Flows.RETRIEVE_ENCODE_KEYS).linkFunction(Flows.RETRIEVE_ENCODE_KEYS.name());

		// Function to handle retrieving refresh keys
		ManagedObjectFunctionBuilder<RetrieveKeysDependencies, None> retrieveRefreshKeys = sourceContext
				.addManagedFunction(Flows.RETRIEVE_REFRESH_KEYS.name(), () -> (functionContext) -> {

					// Obtain the JWT authority repository
					JwtRefreshKeyCollector collector = (JwtRefreshKeyCollector) functionContext
							.getObject(RetrieveKeysDependencies.COLLECTOR);
					JwtAuthorityRepository repository = (JwtAuthorityRepository) functionContext
							.getObject(RetrieveKeysDependencies.JWT_AUTHORITY_REPOSITORY);

					// Obtain the keys
					JwtRefreshKey[] refreshKeys = this.loadKeysForCoverage(repository,
							this.refreshTokenExpirationPeriod, this.refreshKeyOverlapPeriods,
							this.refreshKeyExpirationPeriod, (loadTime, repo) -> {

								// Load keys from repository
								List<JwtRefreshKey> keys = repo
										.retrieveJwtRefreshKeys(this.retrieveKeysContextFactory.apply(loadTime));

								// Keep only the valid keys
								JwtRefreshKey[] validKeys = keys.stream().map((key) -> new JwtRefreshKeyImpl(key))
										.filter((key) -> key.isValid()).toArray(JwtRefreshKey[]::new);

								// Return the valid keys
								return validKeys;

							},
							(newKeys, repo) -> {

								// Save the new keys
								repo.saveJwtRefreshKeys(this.saveKeysContext,
										newKeys.toArray(new JwtRefreshKey[newKeys.size()]));

							}, (startTime) -> {

								// Create the JWT refresh key
								long expireTime = startTime + this.refreshKeyExpirationPeriod;
								String initVector = randomString(initVectorSize, initVectorSize);
								String startSalt = randomString(5, 25);
								String lace = randomString(80, 100);
								String endSalt = randomString(5, 25);
								Key key = this.refreshTokenKeyFactory.createSynchronousKey();
								JwtRefreshKeyImpl newRefreshKey = new JwtRefreshKeyImpl(startTime, expireTime,
										initVector, startSalt, lace, endSalt, key);

								// Return the JWT encode key
								return newRefreshKey;
							});

					// Load the keys
					collector.setKeys(refreshKeys);
				});
		retrieveRefreshKeys.linkParameter(RetrieveKeysDependencies.COLLECTOR, JwtRefreshKeyCollector.class);
		retrieveRefreshKeys.linkObject(RetrieveKeysDependencies.JWT_AUTHORITY_REPOSITORY, jwtAuthorityRepository);
		sourceContext.getFlow(Flows.RETRIEVE_REFRESH_KEYS).linkFunction(Flows.RETRIEVE_REFRESH_KEYS.name());
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {

		// Keep JWT encoding keys up to date
		this.jwtAccessKeys = StatePoller
				.builder(JwtAccessKey[].class, this.timeInSeconds, Flows.RETRIEVE_ENCODE_KEYS, context,
						(pollContext) -> new JwtAuthorityManagedObject<>())
				.parameter((pollContext) -> new JwtAccesKeysCollectorImpl(pollContext)).identifier("JWT Access Keys")
				.defaultPollInterval(this.accessTokenExpirationPeriod, TimeUnit.SECONDS).build();

		// Keep JWT refresh keys up to date
		this.jwtRefreshKeys = StatePoller
				.builder(JwtRefreshKey[].class, this.timeInSeconds, Flows.RETRIEVE_REFRESH_KEYS, context,
						(pollContext) -> new JwtAuthorityManagedObject<>())
				.parameter((pollContext) -> new JwtRefreshCollectorImpl(pollContext)).identifier("JWT Refresh Keys")
				.defaultPollInterval(this.refreshTokenExpirationPeriod, TimeUnit.SECONDS).build();

	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JwtAuthorityManagedObject<>();
	}

	/**
	 * {@link JwtAuthority} {@link ManagedObject}.
	 */
	private class JwtAuthorityManagedObject<I> implements ManagedObject, JwtAuthority<I> {

		/*
		 * ==================== ManagedObject ======================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ==================== JwtAuthority =======================
		 */

		@Override
		public RefreshToken createRefreshToken(I identity) throws RefreshTokenException {

			// Easy access to source
			JwtAuthorityManagedObjectSource source = JwtAuthorityManagedObjectSource.this;

			// Ensure appropriate type
			if ((identity == null) || (!source.identityClass.isAssignableFrom(identity.getClass()))) {
				throw new RefreshTokenException(new IllegalArgumentException(
						"Identity was " + (identity != null ? identity.getClass().getName() : "null")
								+ " but required to be " + source.identityClass.getName()));
			}

			// Create the refresh token
			return source.createToken(identity, source.refreshTokenExpirationPeriod, source.jwtRefreshKeys, (status,
					ex) -> status != null ? new RefreshTokenException(status, ex) : new RefreshTokenException(ex),
					(payload, key, expireTime) -> {
						JwtRefreshKeyImpl impl = (JwtRefreshKeyImpl) key;
						String token = JwtAuthorityManagedObjectSource.encrypt(key.getKey(), impl.initVectorBytes,
								impl.startSaltBytes, impl.laceBytes, impl.endSaltBytes, payload,
								source.refreshTokenCipherFactory);
						return new RefreshToken(token, expireTime);
					});
		}

		@Override
		public I decodeRefreshToken(String refreshToken) {

			// Easy access to source
			JwtAuthorityManagedObjectSource source = JwtAuthorityManagedObjectSource.this;

			// Obtain the refresh keys
			JwtRefreshKey[] refreshKeys;
			try {
				refreshKeys = source.jwtRefreshKeys.getState(source.keyLoadWaitTime, TimeUnit.SECONDS);
			} catch (TimeoutException ex) {
				throw new RefreshTokenException(HttpStatus.SERVICE_UNAVAILABLE, ex);
			}

			// Attempt to decode the refresh token
			NEXT_KEY: for (JwtRefreshKey refreshKey : refreshKeys) {
				try {

					// Downcast to implementation to pull in pre-computed bytes
					JwtRefreshKeyImpl implKey = (JwtRefreshKeyImpl) refreshKey;

					// Attempt to decrypt refresh token
					String json = decrypt(refreshKey.getKey(), implKey.initVectorBytes, implKey.startSaltBytes,
							implKey.laceBytes, implKey.endSaltBytes, refreshToken, source.refreshTokenCipherFactory);
					if (json == null) {
						continue NEXT_KEY; // unable to decrypt with refresh key
					}

					// Read in the identity
					I identity = mapper.readValue(json, source.identityJavaType);

					// Return the identity
					return identity;

				} catch (Exception ex) {
					continue NEXT_KEY; // unable decrypt with refresh key
				}
			}

			// As here, not able to decode refresh token
			throw new RefreshTokenException(HttpStatus.UNAUTHORIZED,
					new IllegalArgumentException("Unable to decode refresh token"));
		}

		@Override
		public void reloadRefreshKeys() {
			JwtAuthorityManagedObjectSource.this.jwtRefreshKeys.clear();
			JwtAuthorityManagedObjectSource.this.jwtRefreshKeys.poll();
		}

		@Override
		public AccessToken createAccessToken(Object claims) {

			// Easy access to source
			JwtAuthorityManagedObjectSource source = JwtAuthorityManagedObjectSource.this;

			// Create the access token
			return source.createToken(
					claims, source.accessTokenExpirationPeriod, source.jwtAccessKeys, (status,
							ex) -> status != null ? new AccessTokenException(status, ex) : new AccessTokenException(ex),
					(payload, key, expireTime) -> {
						String token = Jwts.builder().signWith(key.getPrivateKey()).setPayload(payload).compact();
						return new AccessToken(token, expireTime);
					});
		}

		@Override
		public void reloadAccessKeys() {
			JwtAuthorityManagedObjectSource.this.jwtAccessKeys.clear();
			JwtAuthorityManagedObjectSource.this.jwtAccessKeys.poll();
		}

		@Override
		public JwtValidateKey[] getActiveJwtValidateKeys() {

			// Easy access to source
			JwtAuthorityManagedObjectSource source = JwtAuthorityManagedObjectSource.this;

			// Obtain the access keys
			JwtAccessKey[] accessKeys;
			try {
				accessKeys = source.jwtAccessKeys.getState(source.keyLoadWaitTime, TimeUnit.SECONDS);
			} catch (TimeoutException ex) {
				throw new ValidateKeysException(HttpStatus.SERVICE_UNAVAILABLE, ex);
			}

			// Create the validate keys
			JwtValidateKey[] validateKeys = new JwtValidateKey[accessKeys.length];
			for (int i = 0; i < validateKeys.length; i++) {
				JwtAccessKey accessKey = accessKeys[i];
				validateKeys[i] = new JwtValidateKey(accessKey.getStartTime(), accessKey.getExpireTime(),
						accessKey.getPublicKey());
			}

			// Return the validate keys
			return validateKeys;
		}
	}

	/**
	 * Time window to determine appropriate token.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	protected static class TimeWindow {

		/**
		 * Not before.
		 */
		private Long nbf;

		/**
		 * Expiry.
		 */
		private Long exp;

		/**
		 * Specifies the not before.
		 * 
		 * @param nbf Not before.
		 */
		public void setNbf(Long nbf) {
			this.nbf = nbf;
		}

		/**
		 * Specifies the expire.
		 * 
		 * @param exp Expires.
		 */
		public void setExp(Long exp) {
			this.exp = exp;
		}
	}

	/**
	 * {@link JwtRefreshKeyCollector} implementation.
	 */
	private class JwtRefreshCollectorImpl implements JwtRefreshKeyCollector {

		/**
		 * {@link StatePollContext}.
		 */
		private final StatePollContext<JwtRefreshKey[]> pollContext;

		/**
		 * Instantiate.
		 * 
		 * @param pollContext {@link StatePollContext}.
		 */
		public JwtRefreshCollectorImpl(StatePollContext<JwtRefreshKey[]> pollContext) {
			this.pollContext = pollContext;
		}

		/*
		 * ==================== JwtRefreshCollector ==================
		 */

		@Override
		public void setKeys(JwtRefreshKey... keys) {
			this.pollContext.setNextState(keys, -1, null);
		}
	}

	/**
	 * {@link JwtRefreshKey} implementation that ensure the data is available.
	 */
	private static class JwtRefreshKeyImpl implements JwtRefreshKey {

		/**
		 * Translates the string to bytes.
		 * 
		 * @param value Value.
		 * @return Bytes for the value.
		 */
		private static byte[] bytes(String value) {
			return (value != null) ? value.getBytes(UTF8) : null;
		}

		/**
		 * Start time.
		 */
		private final long startTime;

		/**
		 * Expire time.
		 */
		private final long expireTime;

		/**
		 * Init vector.
		 */
		private final String initVector;

		/**
		 * Init vector bytes.
		 */
		private final byte[] initVectorBytes;

		/**
		 * Start salt.
		 */
		private final String startSalt;

		/**
		 * Start salt bytes.
		 */
		private final byte[] startSaltBytes;

		/**
		 * Lace.
		 */
		private final String lace;

		/**
		 * Lace bytes.
		 */
		private final byte[] laceBytes;

		/**
		 * End salt.
		 */
		private final String endSalt;

		/**
		 * End salt bytes.
		 */
		private final byte[] endSaltBytes;

		/**
		 * {@link Key}.
		 */
		private final Key key;

		/**
		 * Instantiate from {@link JwtRefreshKey}.
		 * 
		 * @param key {@link JwtRefreshKey}.
		 */
		private JwtRefreshKeyImpl(JwtRefreshKey key) {
			this(key.getStartTime(), key.getExpireTime(), key.getInitVector(), key.getStartSalt(), key.getLace(),
					key.getEndSalt(), key.getKey());
		}

		/**
		 * Instantiate.
		 * 
		 * @param startTime  Start time.
		 * @param expireTime Expire time.
		 * @param initVector Init vector.
		 * @param startSalt  Start salt.
		 * @param lace       Lace.
		 * @param endSalt    End salt.
		 * @param key        {@link Key}.
		 */
		private JwtRefreshKeyImpl(long startTime, long expireTime, String initVector, String startSalt, String lace,
				String endSalt, Key key) {
			this.startTime = startTime;
			this.expireTime = expireTime;
			this.initVector = initVector;
			this.initVectorBytes = bytes(initVector);
			this.startSalt = startSalt;
			this.startSaltBytes = bytes(startSalt);
			this.lace = lace;
			this.laceBytes = bytes(lace);
			this.endSalt = endSalt;
			this.endSaltBytes = bytes(endSalt);
			this.key = key;
		}

		/**
		 * Indicates if the {@link JwtAccessKey} is valid to use.
		 * 
		 * @return <code>true</code> if valid to use.
		 */
		private boolean isValid() {
			boolean isValid = this.startTime > 0;
			isValid &= this.expireTime > this.startTime;
			isValid &= (this.initVector != null) & (this.initVector.length() > 0);
			isValid &= (this.startSalt != null) & (this.startSalt.length() > 0);
			isValid &= (this.lace != null) & (this.lace.length() > 0);
			isValid &= (this.endSalt != null) & (this.endSalt.length() > 0);
			isValid &= this.key != null;
			return isValid;
		}

		/*
		 * ==================== JwtEncodeKey =====================
		 */

		@Override
		public long getStartTime() {
			return this.startTime;
		}

		@Override
		public long getExpireTime() {
			return this.expireTime;
		}

		@Override
		public String getInitVector() {
			return this.initVector;
		}

		@Override
		public String getStartSalt() {
			return this.startSalt;
		}

		@Override
		public String getLace() {
			return this.lace;
		}

		@Override
		public String getEndSalt() {
			return this.endSalt;
		}

		@Override
		public Key getKey() {
			return this.key;
		}
	}

	/**
	 * {@link JwtAccessKeyCollector} implementation.
	 */
	private class JwtAccesKeysCollectorImpl implements JwtAccessKeyCollector {

		/**
		 * {@link StatePollContext}.
		 */
		private final StatePollContext<JwtAccessKey[]> pollContext;

		/**
		 * Instantiate.
		 * 
		 * @param pollContext {@link StatePollContext}.
		 */
		public JwtAccesKeysCollectorImpl(StatePollContext<JwtAccessKey[]> pollContext) {
			this.pollContext = pollContext;
		}

		/*
		 * ==================== JwtEncodeCollector ==================
		 */

		@Override
		public void setKeys(JwtAccessKey[] keys) {
			this.pollContext.setNextState(keys, -1, null);
		}
	}

	/**
	 * {@link JwtAccessKey} implementation that ensure the data is available.
	 */
	private static class JwtAccessKeyImpl implements JwtAccessKey {

		/**
		 * Start time.
		 */
		private final long startTime;

		/**
		 * Expire time.
		 */
		private final long expireTime;

		/**
		 * Private {@link Key}.
		 */
		private final Key privateKey;

		/**
		 * Public {@link Key}.
		 */
		private final Key publicKey;

		/**
		 * Instantiate from {@link JwtAccessKey}.
		 * 
		 * @param key {@link JwtAccessKey}.
		 */
		private JwtAccessKeyImpl(JwtAccessKey key) {
			this.startTime = key.getStartTime();
			this.expireTime = key.getExpireTime();
			this.privateKey = key.getPrivateKey();
			this.publicKey = key.getPublicKey();
		}

		/**
		 * Instantiate.
		 * 
		 * @param startTime  Start time.
		 * @param expireTime Expire time.
		 * @param privateKey Private {@link Key}.
		 * @param publicKey  Public {@link Key}.
		 */
		private JwtAccessKeyImpl(long startTime, long expireTime, Key privateKey, Key publicKey) {
			this.startTime = startTime;
			this.expireTime = expireTime;
			this.privateKey = privateKey;
			this.publicKey = publicKey;
		}

		/**
		 * Indicates if the {@link JwtAccessKey} is valid to use.
		 * 
		 * @return <code>true</code> if valid to use.
		 */
		private boolean isValid() {
			boolean isValid = this.startTime > 0;
			isValid &= this.expireTime > this.startTime;
			isValid &= this.privateKey != null;
			isValid &= this.publicKey != null;
			return isValid;
		}

		/*
		 * ==================== JwtEncodeKey =====================
		 */

		@Override
		public long getStartTime() {
			return this.startTime;
		}

		@Override
		public long getExpireTime() {
			return this.expireTime;
		}

		@Override
		public Key getPrivateKey() {
			return this.privateKey;
		}

		@Override
		public Key getPublicKey() {
			return this.publicKey;
		}
	}

}
