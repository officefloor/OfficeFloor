package net.officefloor.web.jwt.authority;

import java.security.Key;
import java.security.KeyPair;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.crypto.KeyGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.jwt.spi.repository.JwtAuthorityRepository;
import net.officefloor.web.jwt.spi.repository.JwtEncodeKey;
import net.officefloor.web.jwt.spi.repository.JwtRefreshKey;

/**
 * Abstract test functionality for {@link JwtAuthority}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJwtAuthorityTokenTest extends OfficeFrameTestCase implements JwtAuthorityRepository {

	/**
	 * {@link KeyPair} for testing.
	 */
	protected static final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

	/**
	 * Refresh {@link Key} for testing.
	 */
	protected static final Key refreshKey;

	static {
		Key key;
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(256);
			key = kgen.generateKey();
		} catch (Exception ex) {
			key = null;
			fail(ex);
		}
		refreshKey = key;
	}

	/**
	 * Mock current time in seconds since Epoch.
	 */
	protected static final long mockCurrentTime = 40000;

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link MockClockFactory}.
	 */
	protected final MockClockFactory clockFactory = new MockClockFactory(mockCurrentTime);

	/**
	 * {@link DateTimeFormatter} for writing out times.
	 */
	protected static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
			.withZone(ZoneId.systemDefault());

	/**
	 * Allows overriding the {@link CompilerIssues}.
	 */
	protected MockCompilerIssues compilerIssues = null;

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	/**
	 * {@link MockIdentity}.
	 */
	protected MockIdentity identity = new MockIdentity();

	/**
	 * {@link MockClaims}.
	 */
	protected MockClaims claims = new MockClaims();

	/**
	 * Time requested for retrieving {@link JwtEncodeKey} instances.
	 */
	protected Instant retrieveJwtEncodeKeysTime = null;

	/**
	 * Indicates if within cluster critical section.
	 */
	private boolean isWithinClusterCriticalSection = false;

	/**
	 * Mock {@link JwtRefreshKey} instances for testing.
	 */
	protected List<JwtRefreshKey> mockRefreshKeys = new ArrayList<>(Arrays.asList(new MockJwtRefreshKey()));

	/**
	 * Mock {@link JwtEncodeKey} instances for testing.
	 */
	protected List<JwtEncodeKey> mockEncodeKeys = new ArrayList<>(
			Arrays.asList(new MockJwtEncodeKey(mockCurrentTime, keyPair),
					new MockJwtEncodeKey(
							mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_ENCODE_KEY_EXPIRATION_PERIOD
									- (JwtAuthorityManagedObjectSource.MINIMUM_ENCODE_KEY_OVERLAP_PERIODS
											* JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD),
							Keys.keyPairFor(SignatureAlgorithm.RS256))));

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
	protected String createAccessToken() {
		return this.doAuthorityTest((authority) -> authority.createAccessToken(this.claims));
	}

	/**
	 * Asserts that access token can not be created.
	 * 
	 * @param expectedCause Expected cause.
	 */
	protected void assertInvalidAccessToken(String expectedCause) {
		try {
			this.createAccessToken();
			fail("Should not be successful");
		} catch (AccessTokenException ex) {
			assertEquals("Incorrect cause", expectedCause, ex.getMessage());
		}
	}

	/**
	 * Creates the refresh token.
	 * 
	 * @return Created refresh token.
	 */
	protected String createRefreshToken() {
		return this.doAuthorityTest((authority) -> authority.createRefreshToken(this.identity));
	}

	/**
	 * Undertakes test with the {@link JwtAuthority}.
	 * 
	 * @param testLogic              Logic on the {@link JwtAuthority}.
	 * @param propertyNameValuePairs {@link Property} name/value pairs for the
	 *                               {@link JwtAuthorityManagedObjectSource}.
	 * @return Result of test logic.
	 */
	protected <T> T doAuthorityTest(Function<JwtAuthority<MockIdentity>, T> testLogic,
			String... propertyNameValuePairs) {
		try {

			// Load the OfficeFloor
			this.loadOfficeFloor(propertyNameValuePairs);

			// Undertake the test logic
			ThreadSafeClosure<T> closure = new ThreadSafeClosure<>();
			WebCompileOfficeFloor.invokeProcess(this.officeFloor, "handle.service",
					(Consumer<JwtAuthority<MockIdentity>>) (authority) -> {
						closure.set(testLogic.apply(authority));
					});

			// Return the value
			return closure.get();

		} catch (Throwable ex) {
			if (ex instanceof AccessTokenException) {
				throw (AccessTokenException) ex;
			} else {
				throw fail(ex);
			}
		}
	}

	/**
	 * Loads the {@link OfficeFloor}.
	 * 
	 * @param propertyNameValuePairs {@link Property} name/value pairs for the
	 *                               {@link JwtAuthorityManagedObjectSource}.
	 */
	protected void loadOfficeFloor(String... propertyNameValuePairs) throws Exception {

		// Compile the server (only once)
		if (this.officeFloor == null) {
			WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
			compiler.getOfficeFloorCompiler().setClockFactory(this.clockFactory);
			compiler.mockHttpServer(null);
			if (this.compilerIssues != null) {
				compiler.getOfficeFloorCompiler().setCompilerIssues(this.compilerIssues);
			}
			compiler.web((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();

				// JWT Authority registered through extension
				OfficeManagedObjectSource jwtAuthoritySource = office.addOfficeManagedObjectSource("JWT_AUTHORITY",
						JwtAuthorityManagedObjectSource.class.getName());
				for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
					jwtAuthoritySource.addProperty(propertyNameValuePairs[i], propertyNameValuePairs[i + 1]);
				}
				jwtAuthoritySource.addOfficeManagedObject("JWT_AUTHORITY", ManagedObjectScope.THREAD);

				// Register the JWT authority repository
				office.addOfficeManagedObjectSource("REPOSITORY", new Singleton(this))
						.addOfficeManagedObject("REPOSITORY", ManagedObjectScope.THREAD);

				// Provide section for handling
				context.addSection("handle", HandlerSection.class);
			});
			this.officeFloor = compiler.compileOfficeFloor();
			if (this.officeFloor != null) {
				this.officeFloor.openOfficeFloor();
			}
		}
	}

	public static class HandlerSection {
		public void service(JwtAuthority<MockClaims> authority,
				@Parameter Consumer<JwtAuthority<MockClaims>> servicer) {
			servicer.accept(authority);
		}
	}

	public static class MockIdentity {
		public String id = "123";
		public String name = "Daniel";
		public Long nbf = mockCurrentTime;
		public Long exp = mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_REFRESH_TOKEN_EXPIRATION_PERIOD;

		@JsonIgnore
		public void assertRefreshToken(String refreshToken, JwtRefreshKey refreshKey) {
			MockIdentity token;
			try {
				String identityJson = JwtAuthorityManagedObjectSource.decrypt(refreshKey.getKey(),
						refreshKey.getInitVector(), refreshKey.getStartSalt(), refreshKey.getEndSalt(), refreshToken);
				token = mapper.readValue(identityJson, MockIdentity.class);
			} catch (Exception ex) {
				throw fail(ex);
			}
			assertEquals("Incorrect id", this.id, token.id);
			assertEquals("Incorrect name", this.name, token.name);
			assertEquals("Incorrect not before", this.nbf, token.nbf);
			assertEquals("Incorrect expiry", this.exp, token.exp);
		}
	}

	public static class MockClaims {
		public String sub = "Daniel";
		public Long nbf = mockCurrentTime;
		public Long exp = mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;

		@JsonIgnore
		public void assertAccessToken(String accessToken, Key key, long currentTimeInSeconds) {
			Claims claims = Jwts.parser().setSigningKey(key).setClock(() -> new Date(currentTimeInSeconds * 1000))
					.parseClaimsJws(accessToken).getBody();
			assertEquals("Incorrect subject", this.sub, claims.getSubject());
			assertEquals("Incorrect not before", getDate(this.nbf), claims.getNotBefore());
			assertEquals("Incorrect expiry", getDate(this.exp), claims.getExpiration());
		}

		@JsonIgnore
		public String getInvalidAccessTokenCause() {
			return IllegalStateException.class.getName() + ": No key available for encoding (nbf: "
					+ getDateText(this.nbf) + ", exp: " + getDateText(this.exp) + ")";
		}
	}

	/**
	 * Obtains the {@link Date} from JWT time in seconds from Epoch.
	 * 
	 * @param timeInSeconds Time in seconds from Epoch.
	 * @return {@link Date}.
	 */
	protected static Date getDate(Long timeInSeconds) {
		return timeInSeconds != null ? new Date(timeInSeconds * 1000) : null;
	}

	/**
	 * Obtains the date formatted to text.
	 * 
	 * @param timeInSeconds Time in seconds from Epoch.
	 * @return Date formatted to text.
	 */
	protected static String getDateText(long timeInSeconds) {
		return dateTimeFormatter.format(Instant.ofEpochSecond(timeInSeconds).atZone(ZoneId.systemDefault()));
	}

	/**
	 * Mock {@link JwtEncodeKey}.
	 */
	private static class MockJwtEncodeKey implements JwtEncodeKey {

		private final long startTime;

		private final long expireTime;

		private final Key privateKey;

		private final Key publicKey;

		private MockJwtEncodeKey(long startTime, KeyPair keyPair) {
			this(startTime, startTime + JwtAuthorityManagedObjectSource.DEFAULT_ENCODE_KEY_EXPIRATION_PERIOD,
					keyPair.getPrivate(), keyPair.getPublic());
		}

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

	/**
	 * Mock {@link JwtRefreshKey}.
	 */
	private static class MockJwtRefreshKey implements JwtRefreshKey {

		/*
		 * ==================== JwtRefreshKey =====================
		 */

		@Override
		public long getStartTime() {
			// TODO implement JwtRefreshKey.getStartTime(...)
			throw new UnsupportedOperationException("TODO implement JwtRefreshKey.getStartTime(...)");
		}

		@Override
		public long getExpireTime() {
			// TODO implement JwtRefreshKey.getExpireTime(...)
			throw new UnsupportedOperationException("TODO implement JwtRefreshKey.getExpireTime(...)");
		}

		@Override
		public String getInitVector() {
			// TODO implement JwtRefreshKey.getInitVector(...)
			throw new UnsupportedOperationException("TODO implement JwtRefreshKey.getInitVector(...)");
		}

		@Override
		public String getStartSalt() {
			// TODO implement JwtRefreshKey.getStartSalt(...)
			throw new UnsupportedOperationException("TODO implement JwtRefreshKey.getStartSalt(...)");
		}

		@Override
		public String getLace() {
			// TODO implement JwtRefreshKey.getLace(...)
			throw new UnsupportedOperationException("TODO implement JwtRefreshKey.getLace(...)");
		}

		@Override
		public String getEndSalt() {
			// TODO implement JwtRefreshKey.getEndSalt(...)
			throw new UnsupportedOperationException("TODO implement JwtRefreshKey.getEndSalt(...)");
		}

		@Override
		public Key getKey() {
			// TODO implement JwtRefreshKey.getKey(...)
			throw new UnsupportedOperationException("TODO implement JwtRefreshKey.getKey(...)");
		}
	}

	/*
	 * ================== JwtAuthorityRepository ===================
	 */

	@Override
	public List<JwtEncodeKey> retrieveJwtEncodeKeys(Instant activeAfter) {
		this.retrieveJwtEncodeKeysTime = activeAfter;
		return this.mockEncodeKeys;
	}

	@Override
	public void saveJwtEncodeKeys(JwtEncodeKey... encodeKeys) throws Exception {
		assertTrue("Should only save keys within cluster critical section", this.isWithinClusterCriticalSection);
		for (JwtEncodeKey encodeKey : encodeKeys) {
			this.mockEncodeKeys.add(encodeKey);
		}
	}

	@Override
	public List<JwtRefreshKey> retrieveJwtRefreshKeys(Instant activeAfter) {
		// TODO implement JwtAuthorityRepository.retrieveJwtRefreshKeys(...)
		throw new UnsupportedOperationException("TODO implement JwtAuthorityRepository.retrieveJwtRefreshKeys(...)");
	}

	@Override
	public void saveJwtRefreshKeys(JwtRefreshKey... refreshKeys) {
		// TODO implement JwtAuthorityRepository.saveJwtRefreshKey(...)
		throw new UnsupportedOperationException("TODO implement JwtAuthorityRepository.saveJwtRefreshKey(...)");
	}

	@Override
	public void doClusterCriticalSection(ClusterCriticalSection clusterCriticalSection) throws Exception {
		this.isWithinClusterCriticalSection = true;
		try {
			clusterCriticalSection.doClusterCriticalSection(this);
		} finally {
			this.isWithinClusterCriticalSection = false;
		}
	}

}