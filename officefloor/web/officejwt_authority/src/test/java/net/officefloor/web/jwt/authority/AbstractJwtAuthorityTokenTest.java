/*-
 * #%L
 * JWT Authority
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.jwt.authority.key.AesCipherFactory;
import net.officefloor.web.jwt.authority.key.AesSynchronousKeyFactory;
import net.officefloor.web.jwt.authority.key.AsynchronousKeyFactory;
import net.officefloor.web.jwt.authority.key.CipherFactory;
import net.officefloor.web.jwt.authority.key.Rsa256AynchronousKeyFactory;
import net.officefloor.web.jwt.authority.key.SynchronousKeyFactory;
import net.officefloor.web.jwt.authority.repository.JwtAccessKey;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityRepository;
import net.officefloor.web.jwt.authority.repository.JwtRefreshKey;

/**
 * Abstract test functionality for {@link JwtAuthority}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJwtAuthorityTokenTest extends OfficeFrameTestCase implements JwtAuthorityRepository {

	/**
	 * UTF8 {@link Charset}.
	 */
	protected static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * {@link KeyPair} for testing.
	 */
	protected static final KeyPair keyPair;

	/**
	 * {@link KeyPair} for testing.
	 */
	protected static final KeyPair secondKeyPair;

	/**
	 * Refresh {@link Key} for testing.
	 */
	protected static final Key refreshKey;

	/**
	 * Refresh {@link Key} for testing.
	 */
	protected static final Key secondRefreshKey;

	static {
		KeyPair pairOne = null;
		KeyPair pairTwo = null;
		Key keyOne = null;
		Key keyTwo = null;
		try {

			// Create the key pairs
			AsynchronousKeyFactory keyPairFactory = new Rsa256AynchronousKeyFactory();
			pairOne = keyPairFactory.createAsynchronousKeyPair();
			pairTwo = keyPairFactory.createAsynchronousKeyPair();

			// Create the keys
			SynchronousKeyFactory keyFactory = new AesSynchronousKeyFactory();
			keyOne = keyFactory.createSynchronousKey();
			keyTwo = keyFactory.createSynchronousKey();

		} catch (Exception ex) {
			fail(ex);
		}
		keyPair = pairOne;
		secondKeyPair = pairTwo;
		refreshKey = keyOne;
		secondRefreshKey = keyTwo;
	}

	/**
	 * Mock current time in seconds since Epoch.
	 */
	protected static final long mockCurrentTime = 40000;

	/**
	 * Mock {@link JwtRefreshKey}.
	 */
	protected static final JwtRefreshKey mockJwtRefreshKey = new MockJwtRefreshKey(mockCurrentTime, refreshKey);

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Mock {@link CipherFactory}.
	 */
	protected static CipherFactory mockCipherFactory = new AesCipherFactory();

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
	 * Indicates if within cluster critical section.
	 */
	private final ThreadLocal<Boolean> isWithinClusterCriticalSection = new ThreadLocal<>();

	/**
	 * Time requested for retrieving {@link JwtRefreshKey} instances.
	 */
	protected Long retrieveJwtRefreshKeysTime = null;

	/**
	 * Mock {@link JwtRefreshKey} instances for testing.
	 */
	protected List<JwtRefreshKey> mockRefreshKeys = new ArrayList<>(Arrays.asList(mockJwtRefreshKey,
			new MockJwtRefreshKey(
					mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_REFRESH_KEY_EXPIRATION_PERIOD
							- (JwtAuthorityManagedObjectSource.MINIMUM_REFRESH_KEY_OVERLAP_PERIODS
									* JwtAuthorityManagedObjectSource.DEFAULT_REFRESH_TOKEN_EXPIRATION_PERIOD),
					secondRefreshKey)));

	/**
	 * Time requested for retrieving {@link JwtAccessKey} instances.
	 */
	protected Long retrieveJwtAccessKeysTime = null;

	/**
	 * Mock {@link JwtAccessKey} instances for testing.
	 */
	protected List<JwtAccessKey> mockAccessKeys = new ArrayList<>(
			Arrays.asList(new MockJwtAccessKey(mockCurrentTime, keyPair),
					new MockJwtAccessKey(
							mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD
									- (JwtAuthorityManagedObjectSource.MINIMUM_ACCESS_KEY_OVERLAP_PERIODS
											* JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD),
							secondKeyPair)));

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.close();
		}
	}

	/**
	 * Creates the access token.
	 * 
	 * @return Created {@link AccessToken}.
	 */
	protected AccessToken createAccessToken() {
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
	 * @return Created {@link RefreshToken}.
	 */
	protected RefreshToken createRefreshToken() {
		return this.doAuthorityTest((authority) -> authority.createRefreshToken(this.identity));
	}

	/**
	 * Asserts that refresh token can not be created.
	 * 
	 * @param expectedCause Expected cause.
	 */
	protected void assertInvalidRefreshToken(String expectedCause) {
		try {
			this.createRefreshToken();
			fail("Should not be successful");
		} catch (RefreshTokenException ex) {
			assertEquals("Incorrect cause", expectedCause, ex.getMessage());
		}
	}

	/**
	 * Decodes the refresh token.
	 * 
	 * @param refreshToken Refresh token.
	 * @return Decoded {@link MockIdentity}.
	 */
	protected MockIdentity decodeRefreshToken(String refreshToken) {
		return this.doAuthorityTest((authority) -> authority.decodeRefreshToken(refreshToken));
	}

	/**
	 * Asserts invalid refresh token for decoding.
	 * 
	 * @param adjust Adjusts the {@link MockJwtRefreshKey} to create invalid refresh
	 *               token.
	 */
	protected void assertInvalidDecodeRefreshToken(Consumer<MockJwtRefreshKey> adjust) {
		MockJwtRefreshKey key = new MockJwtRefreshKey(mockJwtRefreshKey);
		adjust.accept(key);
		String refreshToken = key.createRefreshToken(this.identity, mockCipherFactory);
		assertInvalidDecodeRefreshToken(refreshToken);
	}

	/**
	 * Asserts invalid refresh token for decoding
	 * 
	 * @param refreshToken Refresh token.
	 */
	protected void assertInvalidDecodeRefreshToken(String refreshToken) {
		try {
			this.decodeRefreshToken(refreshToken);
			fail("Should not be successful");
		} catch (RefreshTokenException ex) {
			assertEquals("Incorrect status", HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
			assertEquals("Incorrect cause",
					IllegalArgumentException.class.getName() + ": Unable to decode refresh token", ex.getMessage());
		}
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
			} else if (ex instanceof RefreshTokenException) {
				throw (RefreshTokenException) ex;
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
				boolean isIdentityClassSpecified = false;
				for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
					String name = propertyNameValuePairs[i];
					String value = propertyNameValuePairs[i + 1];
					jwtAuthoritySource.addProperty(name, value);
					if (JwtAuthorityManagedObjectSource.PROPERTY_IDENTITY_CLASS.equals(name)) {
						isIdentityClassSpecified = true;
					}
				}
				if (!isIdentityClassSpecified) {
					jwtAuthoritySource.addProperty(JwtAuthorityManagedObjectSource.PROPERTY_IDENTITY_CLASS,
							MockIdentity.class.getName());
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
		public void assertRefreshToken(String refreshToken, JwtRefreshKey refreshKey, CipherFactory cipherFactory) {
			MockIdentity token;
			try {
				String identityJson = JwtAuthorityManagedObjectSource.decrypt(refreshKey.getKey(),
						refreshKey.getInitVector().getBytes(UTF8), refreshKey.getStartSalt().getBytes(UTF8),
						refreshKey.getLace().getBytes(UTF8), refreshKey.getEndSalt().getBytes(UTF8), refreshToken,
						cipherFactory);
				token = mapper.readValue(identityJson, MockIdentity.class);
			} catch (Exception ex) {
				throw fail(ex);
			}
			this.assertEquals(token);
		}

		@JsonIgnore
		public void assertEquals(MockIdentity decoded) {
			Assert.assertEquals("Incorrect id", this.id, decoded.id);
			Assert.assertEquals("Incorrect name", this.name, decoded.name);
			Assert.assertEquals("Incorrect not before", this.nbf, decoded.nbf);
			Assert.assertEquals("Incorrect expiry", this.exp, decoded.exp);
		}

		@JsonIgnore
		public String getInvalidRefreshTokenCause() {
			return IllegalStateException.class.getName() + ": No key available for encoding (nbf: "
					+ getDateText(this.nbf) + ", exp: " + getDateText(this.exp) + ")";
		}
	}

	public static class MockClaims {
		public String sub = "Daniel";
		public Long nbf = mockCurrentTime;
		public Long exp = mockCurrentTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_TOKEN_EXPIRATION_PERIOD;

		@JsonIgnore
		public void assertAccessToken(String accessToken, Key key, long currentTimeInSeconds) {
			Claims claims = Jwts.parserBuilder().setSigningKey(key)
					.setClock(() -> new Date(currentTimeInSeconds * 1000)).build().parseClaimsJws(accessToken)
					.getBody();
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
	 * Mock {@link JwtAccessKey}.
	 */
	private static class MockJwtAccessKey implements JwtAccessKey {

		private final long startTime;

		private final long expireTime;

		private final Key privateKey;

		private final Key publicKey;

		private MockJwtAccessKey(long startTime, KeyPair keyPair) {
			this(startTime, startTime + JwtAuthorityManagedObjectSource.DEFAULT_ACCESS_KEY_EXPIRATION_PERIOD,
					keyPair.getPrivate(), keyPair.getPublic());
		}

		private MockJwtAccessKey(long startTime, long expireTime, Key privateKey, Key publicKey) {
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
	protected static class MockJwtRefreshKey implements JwtRefreshKey {

		private final long startTime;

		private final long expireTime;

		protected String initVector;

		protected String startSalt;

		protected String lace;

		protected String endSalt;

		protected Key key;

		private MockJwtRefreshKey(long startTime, Key key) {
			this(startTime, startTime + JwtAuthorityManagedObjectSource.DEFAULT_REFRESH_KEY_EXPIRATION_PERIOD,
					"initinitinitinit", "start", "lace", "end", key);
		}

		private MockJwtRefreshKey(long startTime, long expireTime, String initVector, String startSalt, String lace,
				String endSalt, Key key) {
			this.startTime = startTime;
			this.expireTime = expireTime;
			this.initVector = initVector;
			this.startSalt = startSalt;
			this.lace = lace;
			this.endSalt = endSalt;
			this.key = key;
		}

		protected MockJwtRefreshKey(JwtRefreshKey refreshKey) {
			this.startTime = refreshKey.getStartTime();
			this.expireTime = refreshKey.getExpireTime();
			this.initVector = refreshKey.getInitVector();
			this.startSalt = refreshKey.getStartSalt();
			this.lace = refreshKey.getLace();
			this.endSalt = refreshKey.getEndSalt();
			this.key = refreshKey.getKey();
		}

		protected String createRefreshToken(Object identity, CipherFactory cipherFactory) {
			try {
				String payload = mapper.writeValueAsString(identity);
				return JwtAuthorityManagedObjectSource.encrypt(this.key, this.initVector.getBytes(UTF8),
						this.startSalt.getBytes(UTF8), this.lace.getBytes(UTF8), this.endSalt.getBytes(UTF8), payload,
						cipherFactory);
			} catch (Exception ex) {
				throw fail(ex);
			}
		}

		/*
		 * ==================== JwtRefreshKey =====================
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

	/*
	 * ================== JwtAuthorityRepository ===================
	 */

	@Override
	public List<JwtAccessKey> retrieveJwtAccessKeys(RetrieveKeysContext context) {
		this.retrieveJwtAccessKeysTime = context.getActiveAfter();
		return this.mockAccessKeys;
	}

	@Override
	public void saveJwtAccessKeys(SaveKeysContext context, JwtAccessKey... accessKeys) throws Exception {
		assertTrue("Should only save keys within cluster critical section", this.isWithinClusterCriticalSection.get());
		for (JwtAccessKey accessKey : accessKeys) {
			this.mockAccessKeys.add(accessKey);
		}
	}

	@Override
	public List<JwtRefreshKey> retrieveJwtRefreshKeys(RetrieveKeysContext context) {
		this.retrieveJwtRefreshKeysTime = context.getActiveAfter();
		return this.mockRefreshKeys;
	}

	@Override
	public void saveJwtRefreshKeys(SaveKeysContext context, JwtRefreshKey... refreshKeys) {
		assertTrue("Should only save keys within cluster critical section", this.isWithinClusterCriticalSection.get());
		for (JwtRefreshKey refreshKey : refreshKeys) {
			this.mockRefreshKeys.add(refreshKey);
		}
	}

	@Override
	public void doClusterCriticalSection(ClusterCriticalSection clusterCriticalSection) throws Exception {
		this.isWithinClusterCriticalSection.set(true);
		try {
			clusterCriticalSection.doClusterCriticalSection(this);
		} finally {
			this.isWithinClusterCriticalSection.remove();
		}
	}

}
