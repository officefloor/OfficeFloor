package net.officefloor.web.jwt.authority;

import java.security.Key;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Date;
import java.util.List;
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
public class JwtAuthorityTest extends OfficeFrameTestCase implements JwtAuthorityRepository {

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
	 * Ensure able to inject {@link JwtAuthority}.
	 */
	public void testEnsureAuthorityAvailable() {
		boolean isAvailable = this.doAuthorityTest((authority) -> authority != null);
		assertTrue("JWT authority should be available", isAvailable);
	}

	/**
	 * Ensure able to generate JWT.
	 */
	public void testCreateJwt() {
		MockClaims claims = new MockClaims();
		String accessToken = this.doAuthorityTest((authority) -> authority.createAccessToken(claims));
		claims.assertAccessToken(accessToken, keyPair.getPublic());
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.close();
		}
	}

	/**
	 * Undertakes test with the {@link JwtAuthority}.
	 * 
	 * @param testLogic Logic on the {@link JwtAuthority}.
	 * @return Result of test logic.
	 */
	public <T> T doAuthorityTest(Function<JwtAuthority<MockClaims>, T> testLogic) {
		try {

			// Compile the server
			WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
			compiler.getOfficeFloorCompiler().setClockFactory(this.clockFactory);
			compiler.mockHttpServer((server) -> this.server = server);
			compiler.web((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();

				// JWT Authority registered through extension

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
		public long nbf = mockCurrentTime;
		public long exp = mockCurrentTime + (20 * 60);

		public void assertAccessToken(String accessToken, Key key) {
			Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(accessToken).getBody();
			assertEquals("Incorrect subject", this.sub, claims.getSubject());
			assertEquals("Incorrect not before", JwtAuthorityTest.getDate(this.nbf), claims.getNotBefore());
			assertEquals("Incorrect expiry", JwtAuthorityTest.getDate(this.exp), claims.getExpiration());
		}
	}

	/**
	 * Obtains the {@link Date} from JWT time in seconds from Epoch.
	 * 
	 * @param timeInSeconds Time in seconds from Epoch.
	 * @return {@link Date}.
	 */
	private static Date getDate(Long timeInSeconds) {
		return timeInSeconds == null ? new Date(0) : new Date(timeInSeconds * 1000);
	}

	/*
	 * ================== JwtAuthorityRepository ===================
	 */

	@Override
	public List<JwtEncodeKey> retrieveJwtEncodeKeys(Instant currentTime) {
		// TODO implement JwtAuthorityRepository.retrieveJwtEncodeKeys(...)
		throw new UnsupportedOperationException("TODO implement JwtAuthorityRepository.retrieveJwtEncodeKeys(...)");
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