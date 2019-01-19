package net.officefloor.web.jwt;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.jwt.spi.decode.JwtDecodeCollector;
import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;
import net.officefloor.web.jwt.spi.role.JwtRoleCollector;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.spi.security.HttpSecuritySource;

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
	 * Path for server to check role.
	 */
	private static final String ROLE_PATH = "/role";

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Default {@link JwtDecodeCollector} handler.
	 */
	private static final Consumer<JwtDecodeCollector> DEFAULT_JWT_DECODE_COLLECTOR = (collector) -> {
		collector.setKeys(new JwtDecodeKey(keyPair.getPublic()));
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
	protected void setUp() throws Exception {
		// Reset default JWT decode collector
		jwtDecodeCollectorHandler = DEFAULT_JWT_DECODE_COLLECTOR;
	}

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
		this.doJwtTest(null, HttpStatus.UNAUTHORIZED, "NO JWT");
	}

	/**
	 * Ensure ignore other security challenge responses.
	 */
	public void testNonBearerAuthorization() throws Exception {
		this.doJwtTest("Basic " + Base64.getEncoder().encodeToString("daniel:password".getBytes()),
				HttpStatus.UNAUTHORIZED, "NO JWT");
	}

	/**
	 * Ensure timeout on no {@link JwtDecodeKey} instances.
	 */
	public void testTimeoutOnNoKeys() throws Exception {
		jwtDecodeCollectorHandler = (collector) -> {
		};
		String errorEntity = JacksonHttpObjectResponderFactory
				.getEntity(new HttpException(new TimeoutException("Server timed out loading JWT keys")));
		this.doJwtTest("Bearer NO.KEYS.AVAILBLE", HttpStatus.SERVICE_UNAVAILABLE, errorEntity,
				JwtHttpSecuritySource.PROEPRTY_STARTUP_TIMEOUT, "0");
	}

	/**
	 * Ensure handle invalid JWT.
	 */
	public void testInvalidJwt() throws Exception {
		this.doJwtTest("Bearer INVALID", HttpStatus.UNAUTHORIZED, "INVALID JWT");
	}

	/**
	 * Ensure handle invalid JWT as not before in the future.
	 */
	public void testNotBeforeJwt() throws Exception {
		long nbf = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
		String token = Jwts.builder().setNotBefore(new Date(nbf)).signWith(keyPair.getPrivate()).compact();
		this.doJwtTest("Bearer " + token, HttpStatus.UNAUTHORIZED, "INVALID JWT");
	}

	/**
	 * Ensure handle expired JWT.
	 */
	public void testExpiredJwt() throws Exception {
		long exp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
		String token = Jwts.builder().setExpiration(new Date(exp)).signWith(keyPair.getPrivate()).compact();
		this.doJwtTest("Bearer " + token, HttpStatus.UNAUTHORIZED, "EXPIRED JWT");
	}

	/**
	 * Ensure can parse valid JWT.
	 */
	public void testValidJwt() throws Exception {
		MockClaims claims = new MockClaims("Daniel");
		String token = Jwts.builder().setSubject(claims.getSub()).setExpiration(getDate(claims.getExp()))
				.signWith(keyPair.getPrivate()).compact();
		String response = mapper.writeValueAsString(claims);
		this.doJwtTest("Bearer " + token, HttpStatus.OK, response);
	}

	/**
	 * Ensure valid <code>nbf</code> with clock skew.
	 */
	public void testValidNotBeforeWithClockSkew() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure valid <code>exp</code> with clock skew.
	 */
	public void testValidExpiryWithClockSkew() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure invalid JWT as {@link JwtDecodeKey} is old.
	 */
	public void testInvalidDueToOldKey() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure invalid JWT as {@link JwtDecodeKey} is too new.
	 */
	public void testInvalidDueToNewKey() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure valid JWT as {@link JwtDecodeKey} is old but within clock skew.
	 */
	public void testValidDueToOldKeyButWithinClockSkew() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure invalid JWT as {@link JwtDecodeKey} is too new but within clock skew.
	 */
	public void testValidDueToNewKeyButWithinClockSkew() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure access role enforced.
	 */
	public void testRole() throws Exception {

		// Start the server
		this.loadServer(null, JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, RoleClaims.class.getName());

		// No JWT so unauthorised
		this.server.send(MockHttpServer.mockRequest(ROLE_PATH)).assertResponse(HttpStatus.UNAUTHORIZED.getStatusCode(),
				"NO JWT");

		// JWT for other role
		String otherToken = Jwts.builder().signWith(keyPair.getPrivate()).claim("role", "other").compact();
		MockHttpRequestBuilder request = MockHttpServer.mockRequest(ROLE_PATH);
		request.header("Authorization", "Bearer " + otherToken);
		this.server.send(request).assertResponse(HttpStatus.FORBIDDEN.getStatusCode(),
				JacksonHttpObjectResponderFactory.getEntity(new HttpException(HttpStatus.FORBIDDEN)));

		// JWT for role
		String roleToken = Jwts.builder().signWith(keyPair.getPrivate()).claim("role", "allow").compact();
		request = MockHttpServer.mockRequest(ROLE_PATH);
		request.header("Authorization", "Bearer " + roleToken);
		this.server.send(request).assertResponse(HttpStatus.OK.getStatusCode(), "ROLE");
	}

	/**
	 * Role claims.
	 */
	public static class RoleClaims {

		private String role;

		public String getRole() {
			return this.role;
		}

		public void setRole(String role) {
			this.role = role;
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

	/**
	 * Mock claims {@link Class}.
	 */
	public static class MockClaims extends RoleClaims {

		private String sub;

		private Long nbf;

		private Long exp;

		public MockClaims() {
		}

		private MockClaims(String sub) {
			// Time measure in seconds
			this(sub, null, (System.currentTimeMillis() + (20 * 60 * 1000)) / 1000);
		}

		private MockClaims(String sub, Long nbf, Long exp) {
			this.sub = sub;
			this.nbf = nbf;
			this.exp = exp;
		}

		public String getSub() {
			return this.sub;
		}

		public void setSub(String sub) {
			this.sub = sub;
		}

		public Long getNbf() {
			return this.nbf;
		}

		public void setNbf(Long nbf) {
			this.nbf = nbf;
		}

		public Long getExp() {
			return this.exp;
		}

		public void setExp(Long exp) {
			this.exp = exp;
		}
	}

	/**
	 * Undertakes the JWT test.
	 * 
	 * @param authorizationHeaderValue <code>Authorization</code> {@link HttpHeader}
	 *                                 value. May be <code>null</code> to not send
	 *                                 header.
	 * @param expectedStatus           Expected {@link HttpStatus}.
	 * @param expectedEntity           Expected entity content.
	 * @param httpSecuirtyProperties   {@link HttpSecuritySource} {@link Property}
	 *                                 name/value pairs.
	 */
	private void doJwtTest(String authorizationHeaderValue, HttpStatus expectedStatus, String expectedEntity,
			String... httpSecurityProperties) throws Exception {

		// Start the server
		this.loadServer(null, httpSecurityProperties);

		// Build the request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest(ECHO_CLAIMS_PATH);
		if (authorizationHeaderValue != null) {
			request.header("Authorization", authorizationHeaderValue);
		}

		// Ensure appropriate response
		this.server.send(request).assertResponse(expectedStatus.getStatusCode(), expectedEntity);
	}

	@FunctionalInterface
	private static interface ServerConfigurer {
		void configure(CompileWebContext context, HttpSecurityBuilder jwt);
	}

	/**
	 * Loads the {@link MockHttpServer}.
	 * 
	 * @param loader                 {@link ServerConfigurer}.
	 * @param httpSecuirtyProperties {@link HttpSecuritySource} {@link Property}
	 *                               name/value pairs.
	 */
	private void loadServer(ServerConfigurer loader, String... httpSecuirtyProperties) throws Exception {

		// Compile the server
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.mockHttpServer((server) -> this.server = server);
		compiler.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Load the JWT security
			HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(
					context.getWebArchitect(), context.getOfficeArchitect(), context.getOfficeSourceContext());
			HttpSecurityBuilder jwt = security.addHttpSecurity("JWT", JwtHttpSecuritySource.class.getName());
			boolean isClaimsClassConfigured = false;
			for (int i = 0; i < httpSecuirtyProperties.length; i += 2) {
				String name = httpSecuirtyProperties[i];
				String value = httpSecuirtyProperties[i + 1];
				jwt.addProperty(name, value);
				if (JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS.equals(name)) {
					isClaimsClassConfigured = true;
				}
			}
			if (!isClaimsClassConfigured) {
				jwt.addProperty(JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, MockClaims.class.getName());
			}
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_KEYS.name()),
					context.addSection("DECODE_COLLECTOR", JwtDecodeKeyCollectorServicer.class)
							.getOfficeSectionInput("service"));
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_ROLES.name()), context
					.addSection("ROLE_COLLECTOR", JwtRoleCollectorServicer.class).getOfficeSectionInput("service"));

			// Configure the JWT challenges
			OfficeSection challengeSection = context.addSection("NO_JWT", JwtChallengeSection.class);
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.NO_JWT.name()),
					challengeSection.getOfficeSectionInput("noJwt"));
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.INVALID_JWT.name()),
					challengeSection.getOfficeSectionInput("invalidJwt"));
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.EXPIRED_JWT.name()),
					challengeSection.getOfficeSectionInput("expiredJwt"));

			// Load the server configuration
			if (loader != null) {
				loader.configure(context, jwt);
			}

			// Configure servicing
			context.link(false, ECHO_CLAIMS_PATH, EchoClaimsSection.class);
			context.link(false, ROLE_PATH, RoleSection.class);

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

	public static class RoleSection {
		@HttpAccess(ifAllRoles = "allow")
		public void service(ServerHttpConnection connection) throws Exception {
			connection.getResponse().getEntityWriter().write("ROLE");
		}
	}

	public static class JwtChallengeSection {
		public void noJwt(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("NO JWT");
		}

		public void invalidJwt(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("INVALID JWT");
		}

		public void expiredJwt(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("EXPIRED JWT");
		}
	}

	public static class JwtDecodeKeyCollectorServicer {
		public void service(@Parameter JwtDecodeCollector collector) {
			jwtDecodeCollectorHandler.accept(collector);
		}
	}

	public static class JwtRoleCollectorServicer {
		public void service(@Parameter JwtRoleCollector<RoleClaims> collector) {
			collector.setRoles(Arrays.asList(collector.getClaims().getRole()));
		}
	}

}