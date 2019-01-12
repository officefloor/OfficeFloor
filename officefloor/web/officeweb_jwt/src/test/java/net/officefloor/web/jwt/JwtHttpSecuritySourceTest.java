package net.officefloor.web.jwt;

import java.security.KeyPair;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.jwt.JwtHttpSecuritySource.Flows;
import net.officefloor.web.jwt.authority.JwtAuthority;
import net.officefloor.web.jwt.spi.decode.JwtDecodeCollector;
import net.officefloor.web.mock.MockWebApp;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.scheme.MockHttpRatifyContext;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Tests the {@link JwtHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("unchecked")
public class JwtHttpSecuritySourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(JwtHttpSecuritySource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(HttpAuthentication.class);
		type.setAccessControlClass(JwtHttpAccessControl.class);
		type.setInput(true);
		type.addFlow(JwtHttpSecuritySource.Flows.RETRIEVE_KEYS, JwtDecodeCollector.class);

		// Validate the type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, JwtHttpSecuritySource.class);
	}

	/**
	 * Ensure handle no JWT.
	 */
	public void testNoJwt() {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<>();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(JwtHttpSecuritySource.class);

		// Undertake ratify
		assertFalse("Should not need to authenticate, as always ratify JWT", security.ratify(null, ratifyContext));
		assertNull("Should not obtain JWT", ratifyContext.getAccessControl());
	}

	/**
	 * Ensure handle invalid JWT.
	 */
	public void testInvalidJwt() {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<>(
				"Bearer INVALID JWT");

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(JwtHttpSecuritySource.class);

		// Undertake ratify
		assertFalse("Should not need to authenticate, as always ratify JWT", security.ratify(null, ratifyContext));
		assertNull("Should not obtain JWT", ratifyContext.getAccessControl());
	}

	/**
	 * Ensure can parse valid JWT.
	 */
	public void testValidJwt() {

		// Create the key pair
		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

		// Setup valid JWT
		String token = Jwts.builder().setSubject("Daniel").signWith(keyPair.getPrivate()).compact();
		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<>("Bearer " + token);

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(JwtHttpSecuritySource.class);

		// Should have access control
		assertFalse("Should not need to authenticate, as always ratify JWT", security.ratify(null, ratifyContext));
		JwtHttpAccessControl<MockClaims> access = (JwtHttpAccessControl<MockClaims>) ratifyContext.getAccessControl();
		assertNotNull("Should have access control for valid JWT", access);

		// Ensure correct claims
		MockClaims claims = access.getClaims();
		assertEquals("Incorrect subject", "Daniel", claims.getSubject());
	}

	/**
	 * Ensure can create JWT.
	 */
	public void testCreateJwt() throws Throwable {

		// Create the key pair
		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

		// Create the mocks
		MockServerHttpConnection connection = MockHttpServer.mockConnection();
		HttpSession session = MockWebApp.mockSession(connection);

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(JwtHttpSecuritySource.class);

		// Obtain the JWT authentication
		AuthenticationContext<HttpAccessControl, Void> authenticationContext = HttpSecurityLoaderUtil
				.createAuthenticationContext(connection, session, security, (context) -> {

				});
		HttpAuthentication<Void> authentication = security.createAuthentication(authenticationContext);

		// TODO load the JwtAuthority
		JwtAuthority<MockClaims> authority = null;

		// Create the JWT
		String jwt = authority.createJwt(new MockClaims());

		// Validate the JWT is correct
		String subject = Jwts.parser().setSigningKey(keyPair.getPublic()).parseClaimsJws(jwt).getBody().getSubject();
		assertEquals("Incorrect subject", "Daniel", subject);
	}

	public static class MockClaims {
		private String subject;

		public String getSubject() {
			return this.subject;
		}
	}

}