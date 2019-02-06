package net.officefloor.web.jwt.authority;

import java.security.Key;
import java.security.KeyPair;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.crypto.KeyGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.jwt.spi.encode.JwtEncodeKey;
import net.officefloor.web.jwt.spi.refresh.JwtRefreshKey;
import net.officefloor.web.jwt.spi.repository.JwtAuthorityRepository;

/**
 * Tests the {@link JwtAuthority} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtAuthorityAccessTokenTest extends OfficeFrameTestCase implements JwtAuthorityRepository {

	/**
	 * {@link KeyPair} for testing.
	 */
	private static final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

	/**
	 * Mock current time in seconds since Epoch.
	 */
	private static final long mockCurrentTime = 40000;

	/**
	 * {@link MockClockFactory}.
	 */
	private final MockClockFactory clockFactory = new MockClockFactory(mockCurrentTime);

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link MockClaims}.
	 */
	private MockClaims claims = new MockClaims();

	/**
	 * Time requested for retrieving {@link JwtEncodeKey} instances.
	 */
	private Instant retrieveJwtEncodeKeysTime = null;

	/**
	 * Mock {@link JwtEncodeKey} instances for testing.
	 */
	private List<JwtEncodeKey> mockEncodeKeys = new ArrayList<>(Arrays.asList(new MockJwtEncodeKey(mockCurrentTime,
			mockCurrentTime + TimeUnit.MINUTES.toSeconds(5), keyPair.getPrivate(), keyPair.getPublic())));

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
		assertEquals("Incorrect cause", IllegalStateException.class.getName()
				+ ": Access Token must be JSON object (start end with {}) - but was [\"not\",\"an\",\"object\"]",
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
	 * Ensure creates the {@link JwtEncodeKey} instances (should none be available).
	 */
	public void testNoEncodeKeys() {
		this.mockEncodeKeys.clear();
		String accessToken = this.createAccessToken();
		assertEquals("Should create the new keys", 3, this.mockEncodeKeys.size());
		this.claims.assertAccessToken(accessToken, this.mockEncodeKeys.get(0).getPublicKey(), mockCurrentTime);
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.close();
		}
	}

	/**
	 * Creates the access token.
	 * 
	 * @return Created access token.
	 */
	private String createAccessToken() {
		return this.doAuthorityTest((authority) -> authority.createAccessToken(this.claims));
	}

	/**
	 * Undertakes test with the {@link JwtAuthority}.
	 * 
	 * @param testLogic Logic on the {@link JwtAuthority}.
	 * @return Result of test logic.
	 */
	private <T> T doAuthorityTest(Function<JwtAuthority<MockClaims>, T> testLogic) {
		try {

			// Compile the server
			WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
			compiler.getOfficeFloorCompiler().setClockFactory(this.clockFactory);
			compiler.mockHttpServer((server) -> this.server = server);
			compiler.web((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();

				// JWT Authority registered through extension
				office.addOfficeManagedObjectSource("JWT_AUTHORITY", JwtAuthorityManagedObjectSource.class.getName())
						.addOfficeManagedObject("JWT_AUTHORITY", ManagedObjectScope.THREAD);

				// Register the JWT authority repository
				office.addOfficeManagedObjectSource("REPOSITORY", new Singleton(this))
						.addOfficeManagedObject("REPOSITORY", ManagedObjectScope.THREAD);

				// Provide section for handling
				context.addSection("handle", HandlerSection.class);
			});
			this.officeFloor = compiler.compileAndOpenOfficeFloor();

			// Undertake the test logic
			ThreadSafeClosure<T> closure = new ThreadSafeClosure<>();
			WebCompileOfficeFloor.invokeProcess(this.officeFloor, "handle.service",
					(Consumer<JwtAuthority<MockClaims>>) (authority) -> {
						closure.set(testLogic.apply(authority));
					});

			// Return the value
			return closure.get();

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

	public static class HandlerSection {
		public void service(JwtAuthority<MockClaims> authority,
				@Parameter Consumer<JwtAuthority<MockClaims>> servicer) {
			servicer.accept(authority);
		}
	}

	/**
	 * Ensure the encrypt/decrypt works as expected.
	 */
	public void testAes() throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		String message = mapper.writeValueAsString(new MockClaims());

		// Generate the random values
		String initVector = JwtAuthorityManagedObjectSource.randomString(16, 16);
		String startSalt = JwtAuthorityManagedObjectSource.randomString(5, 25);
		String endSalt = JwtAuthorityManagedObjectSource.randomString(5, 25);
		String lace = JwtAuthorityManagedObjectSource.randomString(80, 100);
		System.out.println("Init Vector: " + initVector + " (" + initVector.length() + "), Start Salt: " + startSalt
				+ ", Lace: " + lace + " (" + lace.length() + ")," + "(" + startSalt.length() + "), End Salt: " + endSalt
				+ "(" + endSalt.length() + ")");

		// Generate key
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(256);
		Key key = kgen.generateKey();

		// Encrypt and decrypt
		String encrypted = JwtAuthorityManagedObjectSource.encrypt(key, initVector, startSalt, lace, endSalt, message);
		String decrypted = JwtAuthorityManagedObjectSource.decrypt(key, initVector, startSalt, endSalt, encrypted);

		// Indicate values
		System.out.println("encrypted: " + encrypted + "\ndecrypted: " + decrypted);
		assertEquals("Should decrypt to plain text", message, decrypted);
	}

	public static class MockClaims {
		public String sub = "Daniel";
		public Long nbf = mockCurrentTime;
		public Long exp = mockCurrentTime + (20 * 60);

		public void assertAccessToken(String accessToken, Key key, long currentTimeInSeconds) {
			Claims claims = Jwts.parser().setSigningKey(key).setClock(() -> new Date(currentTimeInSeconds * 1000))
					.parseClaimsJws(accessToken).getBody();
			assertEquals("Incorrect subject", this.sub, claims.getSubject());
			assertEquals("Incorrect not before", getDate(this.nbf), claims.getNotBefore());
			assertEquals("Incorrect expiry", getDate(this.exp), claims.getExpiration());
		}
	}

	/**
	 * Obtains the {@link Date} from JWT time in seconds from Epoch.
	 * 
	 * @param timeInSeconds Time in seconds from Epoch.
	 * @return {@link Date}.
	 */
	private static Date getDate(Long timeInSeconds) {
		return timeInSeconds != null ? new Date(timeInSeconds * 1000) : null;
	}

	/**
	 * Mock {@link JwtEncodeKey}.
	 */
	private static class MockJwtEncodeKey implements JwtEncodeKey {

		private final long startTime;

		private final long expireTime;

		private final Key privateKey;

		private final Key publicKey;

		private MockJwtEncodeKey(long startTime, long expireTime, Key privateKey, Key publicKey) {
			this.startTime = startTime;
			this.expireTime = expireTime;
			this.privateKey = privateKey;
			this.publicKey = publicKey;
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
			return this.publicKey;
		}
	}

	/*
	 * ================== JwtAuthorityRepository ===================
	 */

	@Override
	public List<JwtEncodeKey> retrieveJwtEncodeKeys(Instant currentTime) {
		this.retrieveJwtEncodeKeysTime = currentTime;
		return this.mockEncodeKeys;
	}

	@Override
	public void saveJwtEncodeKey(JwtEncodeKey encodeKey) {
		// TODO implement JwtAuthorityRepository.saveJwtEncodeKey(...)
		throw new UnsupportedOperationException("TODO implement JwtAuthorityRepository.saveJwtEncodeKey(...)");
	}

	@Override
	public List<JwtRefreshKey> retrieveJwtRefreshKeys(Instant currentTime) {
		// TODO implement JwtAuthorityRepository.retrieveJwtRefreshKeys(...)
		throw new UnsupportedOperationException("TODO implement JwtAuthorityRepository.retrieveJwtRefreshKeys(...)");
	}

	@Override
	public void saveJwtRefreshKey(JwtRefreshKey refreshKey) {
		// TODO implement JwtAuthorityRepository.saveJwtRefreshKey(...)
		throw new UnsupportedOperationException("TODO implement JwtAuthorityRepository.saveJwtRefreshKey(...)");
	}

}