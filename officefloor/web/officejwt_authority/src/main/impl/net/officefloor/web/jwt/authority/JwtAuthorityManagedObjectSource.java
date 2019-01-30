package net.officefloor.web.jwt.authority;

import java.nio.charset.Charset;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
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
	 * {@link Charset}.
	 */
	private static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * Default translation.
	 */
	private static final String DEFAULT_TRANSLATION = "AES/CBC/PKCS5PADDING";

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

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
	 * Loads the {@link JwtEncodeKey} instances.
	 * 
	 * @param clockTimeSeconds {@link Clock} for current time in seconds.
	 * @param repository       {@link JwtAuthorityRepository}.
	 * @param collector        {@link JwtEncodeCollector}.
	 */
	public static void loadJwtEncodeKeys(Clock<Long> clockTimeSeconds, JwtAuthorityRepository repository,
			JwtEncodeCollector collector) {

		// Retrieve the keys
		long currentTimeSeconds = clockTimeSeconds.getTime();
		// TODO determine period for all active keys for start time
		List<JwtEncodeKey> keys = repository.retrieveJwtEncodeKeys(Instant.ofEpochSecond(currentTimeSeconds));

		// Load the keys (keeping only valid keys)
		JwtEncodeKeyImpl[] encodeKeys = keys.stream().map((key) -> new JwtEncodeKeyImpl(key))
				.filter((key) -> key.isValid()).toArray(JwtEncodeKeyImpl[]::new);

		// TODO determine if need to create new key

		// Load the JWT encoder
		collector.setEncoding(encodeKeys);
	}

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

		// Load meta-data
		context.setObjectClass(JwtAuthority.class);
		context.setManagedObjectClass(JwtAuthorityManagedObject.class);

		// Configure flows
		context.addFlow(Flows.RETRIEVE_ENCODE_KEYS, JwtEncodeCollector.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {

		// Keep JWT encoding keys up to date
		this.jwtEncodeKeys = StatePoller
				.builder(JwtEncodeKey[].class, Flows.RETRIEVE_ENCODE_KEYS, context,
						(pollContext) -> new JwtAuthorityManagedObject<>())
				.parameter((pollContext) -> new JwtEncodeCollectorImpl(pollContext)).identifier("JWT Encode Keys")
				.build();

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
		public <C> String createAccessToken(C claims) {

			// Obtain the JWT encode keys
			JwtEncodeKey[] encodeKeys;
			try {
				encodeKeys = JwtAuthorityManagedObjectSource.this.jwtEncodeKeys.getState(1, TimeUnit.SECONDS);
			} catch (TimeoutException ex) {
				throw new AccessTokenException(HttpStatus.SERVICE_UNAVAILABLE, ex);
			}

			// TODO look through keys to find the most appropriate (based on current time)
			JwtEncodeKey selectedKey = encodeKeys[0];

			// Obtain the claims payload
			String payload;
			try {
				payload = mapper.writeValueAsString(claims);
			} catch (Exception ex) {
				throw new AccessTokenException(ex);
			}

			// Generate the access token
			JwtBuilder builder = Jwts.builder().signWith(selectedKey.getPrivateKey()).setPayload(payload);
			return builder.compact();
		}

		@Override
		public JwtDecodeKey[] getActiveJwtDecodeKeys() {
			// TODO implement JwtAuthority<C>.getActiveJwtDecodeKeys(...)
			throw new UnsupportedOperationException("TODO implement JwtAuthority<C>.getActiveJwtDecodeKeys(...)");
		}
	}

	/**
	 * {@link JwtEncodeCollector} implementation.
	 */
	public class JwtEncodeCollectorImpl implements JwtEncodeCollector {

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
			// TODO implement JwtEncodeKey.startTime(...)
			throw new UnsupportedOperationException("TODO implement JwtEncodeKey.startTime(...)");
		}

		@Override
		public long expireTime() {
			// TODO implement JwtEncodeKey.expireTime(...)
			throw new UnsupportedOperationException("TODO implement JwtEncodeKey.expireTime(...)");
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