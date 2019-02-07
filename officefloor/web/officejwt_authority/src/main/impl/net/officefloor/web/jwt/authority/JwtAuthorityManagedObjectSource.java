package net.officefloor.web.jwt.authority;

import java.nio.charset.Charset;
import java.security.Key;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtBuilder;
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
import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;
import net.officefloor.web.jwt.spi.encode.JwtEncodeCollector;
import net.officefloor.web.jwt.spi.encode.JwtEncodeKey;
import net.officefloor.web.jwt.spi.repository.JwtAuthorityRepository;

/**
 * {@link JwtAuthority} {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtAuthorityManagedObjectSource
		extends AbstractManagedObjectSource<None, JwtAuthorityManagedObjectSource.Flows> {

	/**
	 * {@link Flow} keys.
	 */
	public static enum Flows {
		RETRIEVE_ENCODE_KEYS
	}

	/**
	 * Dependencies for {@link ManagedFunction} to retrieve the {@link JwtEncodeKey}
	 * instances.
	 */
	private static enum RetrieveEncodeKeysDependencies {
		COLLECTOR, JWT_AUTHORITY_REPOSITORY
	}

	/**
	 * Default translation.
	 */
	public static final String DEFAULT_TRANSLATION = "AES/CBC/PKCS5PADDING";

	/**
	 * {@link Property} name for the expiration period for access token. Period
	 * measures in seconds.
	 */
	public static final String PROPERTY_ACCESS_TOKEN_EXPIRATION_PERIOD = "access.token.expiration.period";

	/**
	 * Default expiration period for access tokens.
	 */
	public static final long DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD = TimeUnit.MINUTES.toSeconds(20);

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
	 * {@link Claims} {@link JavaType}.
	 */
	private static final JavaType claimsJavaType = mapper.constructType(Claims.class);

	static {
		// Ensure JSON deserialising is valid
		if (!mapper.canDeserialize(claimsJavaType)) {
			throw new IllegalStateException("Unable to deserialize " + Claims.class.getSimpleName());
		}
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
			do {
				length = random.nextInt(maxLength);
			} while (length < minLength);
		}

		// Generate the random string
		int increase = 1;
		for (;;) {
			byte[] bytes = new byte[length * increase];
			random.nextBytes(bytes);
			String value = Base64.getEncoder().encodeToString(bytes);
			if (value.length() >= length) {
				return value.substring(0, length);
			}
			increase++;
		}
	}

	/**
	 * Encrypts the value.
	 * 
	 * @param key        {@link Key}.
	 * @param initVector Initialise vector.
	 * @param startSalt  Start salt.
	 * @param lace       Lace.
	 * @param endSalt    End salt.
	 * @param value      Value.
	 * @return Encrypted value.
	 * @throws Exception If fails to encrypt value.
	 */
	public static String encrypt(Key key, String initVector, String startSalt, String lace, String endSalt,
			String value) throws Exception {
		IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(UTF8));
		Cipher cipher = Cipher.getInstance(DEFAULT_TRANSLATION);
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] encrypted = cipher.doFinal((startSalt + laceString(value, lace) + endSalt).getBytes());
		return Base64.getUrlEncoder().encodeToString(encrypted);
	}

	/**
	 * Decrypts the value.
	 * 
	 * @param key        {@link Key}.
	 * @param initVector Initialise vector.
	 * @param startSalt  Start salt.
	 * @param endSalt    End salt.
	 * @param encrypted  Encrypted value.
	 * @return Plaintext value.
	 * @throws Exception If fails to decrypt value.
	 */
	public static String decrypt(Key key, String initVector, String startSalt, String endSalt, String encrypted)
			throws Exception {
		IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
		Cipher cipher = Cipher.getInstance(DEFAULT_TRANSLATION);
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] original = cipher.doFinal(Base64.getUrlDecoder().decode(encrypted));
		String value = new String(original);
		value = value.substring(startSalt.length());
		value = value.substring(0, value.length() - endSalt.length());
		return unlaceString(value);
	}

	/**
	 * As the JWT claims is known string, this can reduce effectiveness of cipher.
	 * Therefore, randomly insert values so each section of the AES encryption is
	 * not derivable.
	 * 
	 * @param value Value.
	 * @param lace  Random data to lace the value.
	 * @return Laced value.
	 */
	public static String laceString(String value, String lace) {
		byte[] valueBytes = value.getBytes(UTF8);
		byte[] laceBytes = lace.getBytes(UTF8);
		byte[] laced = new byte[valueBytes.length * 2];
		for (int i = 0; i < valueBytes.length; i++) {
			int laceIndex = i % lace.length();
			byte laceByte = laceBytes[laceIndex];

			// Load the values
			int pairIndex = i * 2;
			laced[pairIndex] = laceByte;
			laced[pairIndex + 1] = valueBytes[i];
		}
		return Base64.getEncoder().encodeToString(laced);
	}

	/**
	 * Unlaces the laced value.
	 * 
	 * @param laced Laced value.
	 * @return Unlaced value.
	 */
	public static String unlaceString(String laced) {
		byte[] lacedBytes = Base64.getDecoder().decode(laced);
		byte[] value = new byte[lacedBytes.length / 2];
		for (int i = 0; i < value.length; i++) {
			value[i] = lacedBytes[(i * 2) + 1];
		}
		return new String(value, Charset.forName("UTF-8"));
	}

	/**
	 * Default time in seconds expire the access token.
	 */
	private long accessTokenExpirationPeriod;

	/**
	 * {@link Clock} to obtain time in seconds.
	 */
	private Clock<Long> timeInSeconds;

	/**
	 * {@link StatePoller} to keep the {@link JwtEncodeKey} instances up to date
	 * with appropriate keys.
	 */
	private StatePoller<JwtEncodeKey[], Flows> jwtEncodeKeys;

	/*
	 * =================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
		ManagedObjectSourceContext<Flows> sourceContext = context.getManagedObjectSourceContext();

		// Obtain the properties
		this.accessTokenExpirationPeriod = Long.parseLong(sourceContext.getProperty(
				PROPERTY_ACCESS_TOKEN_EXPIRATION_PERIOD, String.valueOf(DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD)));

		// Load meta-data
		context.setObjectClass(JwtAuthority.class);
		context.setManagedObjectClass(JwtAuthorityManagedObject.class);

		// Configure flows
		context.addFlow(Flows.RETRIEVE_ENCODE_KEYS, JwtEncodeCollector.class);

		// Obtain the clock
		this.timeInSeconds = sourceContext.getClock((time) -> time);

		// Obtain the JWT Authority Repository dependency
		ManagedObjectFunctionDependency jwtAuthorityRepository = sourceContext
				.addFunctionDependency(JwtAuthorityRepository.class.getSimpleName(), JwtAuthorityRepository.class);

		// Function to handle retrieving encode keys
		ManagedObjectFunctionBuilder<RetrieveEncodeKeysDependencies, None> retrieveEncodeKeys = sourceContext
				.addManagedFunction(Flows.RETRIEVE_ENCODE_KEYS.name(), () -> (functionContext) -> {

					// Obtain the JWT authority repository
					JwtEncodeCollector collector = (JwtEncodeCollector) functionContext
							.getObject(RetrieveEncodeKeysDependencies.COLLECTOR);
					JwtAuthorityRepository repository = (JwtAuthorityRepository) functionContext
							.getObject(RetrieveEncodeKeysDependencies.JWT_AUTHORITY_REPOSITORY);

					// Retrieve the keys
					long currentTimeSeconds = this.timeInSeconds.getTime();
					// TODO determine period for all active keys for start time
					List<JwtEncodeKey> keys = repository
							.retrieveJwtEncodeKeys(Instant.ofEpochSecond(currentTimeSeconds));

					// Load the keys (keeping only valid keys)
					JwtEncodeKeyImpl[] encodeKeys = keys.stream().map((key) -> new JwtEncodeKeyImpl(key))
							.filter((key) -> key.isValid()).toArray(JwtEncodeKeyImpl[]::new);

					// TODO determine if need to create new key

					// Load the JWT encoder
					collector.setEncoding(encodeKeys);

					// Nothing further
					return null;
				});
		retrieveEncodeKeys.linkParameter(RetrieveEncodeKeysDependencies.COLLECTOR, JwtEncodeCollector.class);
		retrieveEncodeKeys.linkObject(RetrieveEncodeKeysDependencies.JWT_AUTHORITY_REPOSITORY, jwtAuthorityRepository);
		sourceContext.getFlow(Flows.RETRIEVE_ENCODE_KEYS).linkFunction(Flows.RETRIEVE_ENCODE_KEYS.name());
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {

		// Keep JWT encoding keys up to date
		this.jwtEncodeKeys = StatePoller
				.builder(JwtEncodeKey[].class, Flows.RETRIEVE_ENCODE_KEYS, context,
						(pollContext) -> new JwtAuthorityManagedObject<>())
				.parameter((pollContext) -> new JwtEncodeCollectorImpl(pollContext)).identifier("JWT Encode Keys")
				.defaultPollInterval(this.accessTokenExpirationPeriod, TimeUnit.SECONDS).build();
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
		public String createRefreshToken(I identity) {
			// TODO implement JwtAuthority<C>.createRefreshToken(...)
			throw new UnsupportedOperationException("TODO implement JwtAuthority<C>.createRefreshToken(...)");
		}

		@Override
		public I decodeRefreshToken(String refreshToken) {
			// TODO implement JwtAuthority<C>.decodeRefreshToken(...)
			throw new UnsupportedOperationException("TODO implement JwtAuthority<C>.decodeRefreshToken(...)");
		}

		@Override
		public void reloadRefreshKeys() {
			// TODO implement JwtAuthority<I>.reloadRefreshKeys(...)
			throw new UnsupportedOperationException("TODO implement JwtAuthority<I>.reloadRefreshKeys(...)");
		}

		@Override
		public <C> String createAccessToken(C claims) {

			// Easy access to source
			JwtAuthorityManagedObjectSource source = JwtAuthorityManagedObjectSource.this;

			// Obtain the claims payload
			String payload;
			try {
				payload = mapper.writeValueAsString(claims).trim();
			} catch (Exception ex) {
				throw new AccessTokenException(ex);
			}

			// Determine if JSON object (and where last bracket)
			int lastBracketIndex = payload.lastIndexOf('}');
			if (lastBracketIndex != (payload.length() - 1)) {
				throw new AccessTokenException(new IllegalStateException(
						"Access Token must be JSON object (start end with {}) - but was " + payload));
			}

			// Determine details for access token
			Claims standardClaims;
			try {
				standardClaims = mapper.readValue(payload, claimsJavaType);
			} catch (Exception ex) {
				throw new AccessTokenException(ex);
			}

			// Obtain the current time
			long currentTime = JwtAuthorityManagedObjectSource.this.timeInSeconds.getTime();

			// Obtain the not before time
			long notBeforeTime = currentTime;
			if (standardClaims.nbf != null) {
				notBeforeTime = standardClaims.nbf;
			}

			// Obtain the expire time (and ensure access token expires)
			long expireTime;
			if (standardClaims.exp != null) {
				expireTime = standardClaims.exp;

			} else {
				// No expire, so calculate the default expire time
				expireTime = currentTime + source.accessTokenExpirationPeriod;

				// Append the expire time
				payload = payload.substring(0, lastBracketIndex) + ",\"exp\":" + expireTime + "}";
			}

			// Obtain the JWT encode keys
			JwtEncodeKey[] encodeKeys;
			try {
				encodeKeys = source.jwtEncodeKeys.getState(1, TimeUnit.SECONDS);
			} catch (TimeoutException ex) {
				throw new AccessTokenException(HttpStatus.SERVICE_UNAVAILABLE, ex);
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
			JwtEncodeKey selectedKey = null;
			long minimumExpireTime = expireTime + (2 * (expireTime - notBeforeTime));
			NEXT_KEY: for (JwtEncodeKey candidateKey : encodeKeys) {

				// Ensure key is active
				if (candidateKey.startTime() > notBeforeTime) {
					continue NEXT_KEY; // key not active
				}

				// Ensure key will not expire too early
				if (candidateKey.expireTime() > minimumExpireTime) {
					continue NEXT_KEY; // key expires too early
				}

				// Determine if shortest time
				if ((selectedKey != null) && (selectedKey.expireTime() < candidateKey.expireTime())) {
					continue NEXT_KEY; // expires later
				}

				// Use the key
				selectedKey = candidateKey;
			}

			// Ensure have key
			if (selectedKey == null) {

				// Trigger loading keys (to possibly generate new key)
				source.jwtEncodeKeys.poll();

				// Indicate no keys
				String notBeforeDateTime = dateTimeFormatter
						.format(Instant.ofEpochSecond(notBeforeTime).atZone(ZoneId.systemDefault()));
				String expireDateTime = dateTimeFormatter
						.format(Instant.ofEpochSecond(expireTime).atZone(ZoneId.systemDefault()));
				throw new AccessTokenException(new IllegalStateException("No " + JwtEncodeKey.class.getSimpleName()
						+ " available for encoding (nbf: " + notBeforeDateTime + ", exp: " + expireDateTime + ")"));
			}

			// Generate the access token
			JwtBuilder builder = Jwts.builder().signWith(selectedKey.getPrivateKey()).setPayload(payload);
			return builder.compact();
		}

		@Override
		public void reloadAccessKeys() {
			// TODO implement JwtAuthority<I>.reloadAccessKeys(...)
			throw new UnsupportedOperationException("TODO implement JwtAuthority<I>.reloadAccessKeys(...)");
		}

		@Override
		public JwtDecodeKey[] getActiveJwtDecodeKeys() {
			// TODO implement JwtAuthority<C>.getActiveJwtDecodeKeys(...)
			throw new UnsupportedOperationException("TODO implement JwtAuthority<C>.getActiveJwtDecodeKeys(...)");
		}
	}

	/**
	 * Claims details to determine appropriate access token.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Claims {

		/**
		 * Not before.
		 */
		private Long nbf;

		/**
		 * Expiry.
		 */
		private Long exp;
	}

	/**
	 * {@link JwtEncodeCollector} implementation.
	 */
	private class JwtEncodeCollectorImpl implements JwtEncodeCollector {

		/**
		 * {@link StatePollContext}.
		 */
		private final StatePollContext<JwtEncodeKey[]> pollContext;

		/**
		 * Instantiate.
		 * 
		 * @param pollContext {@link StatePollContext}.
		 */
		public JwtEncodeCollectorImpl(StatePollContext<JwtEncodeKey[]> pollContext) {
			this.pollContext = pollContext;
		}

		/*
		 * ==================== JwtEncodeCollector ==================
		 */

		@Override
		public JwtEncodeKey[] getCurrentKeys() {
			// TODO implement JwtEncodeCollector.getCurrentKeys(...)
			throw new UnsupportedOperationException("TODO implement JwtEncodeCollector.getCurrentKeys(...)");
		}

		@Override
		public void setEncoding(JwtEncodeKey[] keys) {
			this.pollContext.setNextState(keys, -1, null);
		}

		@Override
		public void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit) {
			// TODO implement JwtEncodeCollector.setFailure(...)
			throw new UnsupportedOperationException("TODO implement JwtEncodeCollector.setFailure(...)");
		}
	}

	/**
	 * {@link JwtEncodeKey} implementation that ensure the data is available.
	 */
	private static class JwtEncodeKeyImpl implements JwtEncodeKey {

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
		 * Instantiate from {@link JwtEncodeKey}.
		 * 
		 * @param key {@link JwtEncodeKey}.
		 */
		public JwtEncodeKeyImpl(JwtEncodeKey key) {
			this.startTime = key.startTime();
			this.expireTime = key.expireTime();
			this.privateKey = key.getPrivateKey();
			this.publicKey = key.getPublicKey();
		}

		/**
		 * Indicates if the {@link JwtEncodeKey} is valid to use.
		 * 
		 * @return <code>true</code> if valid to use.
		 */
		public boolean isValid() {
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
		public long startTime() {
			return this.startTime;
		}

		@Override
		public long expireTime() {
			return this.expireTime;
		}

		@Override
		public Key getPrivateKey() {
			return this.privateKey;
		}

		@Override
		public Key getPublicKey() {
			// TODO implement JwtEncodeKey.getPublicKey(...)
			throw new UnsupportedOperationException("TODO implement JwtEncodeKey.getPublicKey(...)");
		}
	}

}