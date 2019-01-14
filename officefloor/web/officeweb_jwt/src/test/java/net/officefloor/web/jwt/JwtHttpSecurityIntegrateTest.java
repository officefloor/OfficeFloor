package net.officefloor.web.jwt;

import java.security.KeyPair;
import java.util.function.Consumer;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.jwt.JwtHttpSecuritySource.Flows;
import net.officefloor.web.jwt.authority.JwtAuthority;
import net.officefloor.web.jwt.spi.decode.JwtDecodeCollector;
import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.scheme.MockHttpRatifyContext;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Integrate tests the {@link JwtHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtHttpSecurityIntegrateTest extends OfficeFrameTestCase {

	/**
	 * {@link KeyPair} for testing.
	 */
	private static final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

	/**
	 * Path for server to echo the claims back to client.
	 */
	private static final String ECHO_CLAIMS_PATH = "/claims";

	/**
	 * Default {@link JwtDecodeCollector} handler.
	 */
	private static final Consumer<JwtDecodeCollector> DEFAULT_JWT_DECODE_COLLECTOR = (collector) -> {
		collector.setKeys(10_000, new JwtDecodeKey(keyPair.getPublic()));
	};

	/**
	 * {@link JwtDecodeCollector} handler to use in provide {@link JwtDecodeKey}
	 * instances.
	 */
	private static Consumer<JwtDecodeCollector> jwtDecodeCollectorHandler = DEFAULT_JWT_DECODE_COLLECTOR;

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure handle no JWT.
	 */
	public void testNoJwt() throws Exception {
		this.loadServer(null);

		// Ensure unauthorized (as no JWT token for access control)
		this.server.send(MockHttpServer.mockRequest(ECHO_CLAIMS_PATH))
				.assertResponse(HttpStatus.UNAUTHORIZED.getStatusCode(), "");
	}

	@FunctionalInterface
	private static interface ServerConfigurer {
		void configure(CompileWebContext context, HttpSecurityBuilder jwt);
	}

	/**
	 * Loads the {@link MockHttpServer}.
	 * 
	 * @param loader {@link ServerConfigurer}.
	 */
	private void loadServer(ServerConfigurer loader) throws Exception {

		// Compile the server
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.mockHttpServer((server) -> this.server = server);
		compiler.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Load the JWT security
			HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(
					context.getWebArchitect(), context.getOfficeArchitect(), context.getOfficeSourceContext());
			HttpSecurityBuilder jwt = security.addHttpSecurity("JWT", JwtHttpSecuritySource.class.getName());
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_KEYS.name()), context
					.addSection("COLLECTOR", JwtDecodeKeyCollectorServicer.class).getOfficeSectionInput("service"));

			// Load the server configuration
			if (loader != null) {
				loader.configure(context, jwt);
			}

			// Configure echo claims
			context.link(false, ECHO_CLAIMS_PATH, EchoClaimsSection.class);

			security.informWebArchitect();
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
	}

	public static class EchoClaimsSection {
		public void service(JwtHttpAccessControl<?> accessControl, ObjectResponse<Object> response) {
			Object claims = accessControl.getClaims();
			response.send(claims);
		}
	}

	public static class JwtDecodeKeyCollectorServicer {
		public void service(@Parameter JwtDecodeCollector collector) {
			jwtDecodeCollectorHandler.accept(collector);
		}
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

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(JwtHttpSecuritySource.class);

		// Obtain the JWT authentication
		AuthenticationContext<HttpAccessControl, Void> authenticationContext = HttpSecurityLoaderUtil
				.createAuthenticationContext(connection, security, (context) -> {

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