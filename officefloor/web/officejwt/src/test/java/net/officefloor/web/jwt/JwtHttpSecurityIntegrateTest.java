/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.MockClockFactory;
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
import net.officefloor.web.jwt.role.JwtRoleCollector;
import net.officefloor.web.jwt.validate.JwtValidateKey;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;
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
public class JwtHttpSecurityIntegrateTest {

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
	 * Path to server to inject JWT claims.
	 */
	private static final String INJECT_PATH = "/inject";

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Default {@link JwtValidateKeyCollector} handler.
	 */
	private static final Consumer<JwtValidateKeyCollector> DEFAULT_JWT_DECODE_COLLECTOR = (collector) -> {
		collector.setKeys(new JwtValidateKey(keyPair.getPublic()));
	};

	/**
	 * {@link JwtValidateKeyCollector} handler to use in provide
	 * {@link JwtValidateKey} instances.
	 */
	private static Consumer<JwtValidateKeyCollector> jwtDecodeCollectorHandler = DEFAULT_JWT_DECODE_COLLECTOR;

	/**
	 * Current time in seconds since Epoch.
	 */
	private static final long CURRENT_TIME = 50000;

	/**
	 * {@link MockClockFactory} to enable specifying time.
	 */
	private final MockClockFactory clockFactory = new MockClockFactory(CURRENT_TIME);

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@BeforeEach
	public void setUp() throws Exception {
		// Reset default JWT decode collector
		jwtDecodeCollectorHandler = DEFAULT_JWT_DECODE_COLLECTOR;
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure handle no JWT.
	 */
	@Test
	public void noJwt() throws Exception {
		this.doJwtTest(null, HttpStatus.UNAUTHORIZED, "NO JWT");
	}

	/**
	 * Ensure ignore other security challenge responses.
	 */
	@Test
	public void nonBearerAuthorization() throws Exception {
		this.doJwtTest("Basic " + Base64.getEncoder().encodeToString("daniel:password".getBytes()),
				HttpStatus.UNAUTHORIZED, "NO JWT");
	}

	/**
	 * Ensure timeout on no {@link JwtValidateKey} instances.
	 */
	@Test
	public void timeoutOnNoKeys() throws Exception {
		jwtDecodeCollectorHandler = (collector) -> {
		};
		String errorEntity = JacksonHttpObjectResponderFactory
				.getEntity(new HttpException(new TimeoutException("Server timed out loading JWT keys")), mapper);
		this.doJwtTest("Bearer NO.KEYS.AVAILBLE", HttpStatus.SERVICE_UNAVAILABLE, errorEntity,
				JwtHttpSecuritySource.PROEPRTY_STARTUP_TIMEOUT, "0");
	}

	/**
	 * Ensure handle invalid JWT.
	 */
	@Test
	public void invalidJwt() throws Exception {
		this.doJwtTest("Bearer INVALID", HttpStatus.UNAUTHORIZED, "INVALID JWT");
	}

	/**
	 * Ensure invalid if can not parse claims.
	 */
	@Test
	public void invalidParseClaimsJwt() throws Exception {
		this.doJwtTest("Bearer HEADER.CLAIMS.SIGNATURE", HttpStatus.UNAUTHORIZED, "INVALID JWT");
	}

	/**
	 * Ensure invalid if can not parse header.
	 */
	@Test
	public void invalidParseHeaderJwt() throws Exception {
		String claims = Base64.getUrlEncoder().encodeToString("{}".getBytes());
		this.doJwtTest("Bearer HEADER." + claims + ".SIGNATURE", HttpStatus.UNAUTHORIZED, "INVALID JWT");
	}

	/**
	 * Ensure invalid signature.
	 */
	@Test
	public void invalidSignatureJwt() throws Exception {
		String token = Jwts.builder().signWith(keyPair.getPrivate()).claim("sub", "Daniel").compact();
		String parts[] = token.split("\\.");
		assertEquals(3, parts.length, "Invalid test, as invalid token");
		token = parts[0] + "." + parts[1] + "." + Base64.getUrlEncoder().encodeToString("invalid".getBytes());
		this.doJwtTest("Bearer " + token, HttpStatus.UNAUTHORIZED, "INVALID JWT");
	}

	/**
	 * Ensure handle invalid JWT as not before in the future.
	 */
	@Test
	public void notBeforeJwt() throws Exception {
		this.doInvalidJwtTest(new MockClaims().setNbf(currentTimeOffset(3, TimeUnit.SECONDS)), "INVALID JWT");
	}

	/**
	 * Ensure handle expired JWT.
	 */
	@Test
	public void expiredJwt() throws Exception {
		this.doInvalidJwtTest(new MockClaims().setExp(currentTimeOffset(-3, TimeUnit.SECONDS)), "EXPIRED JWT");
	}

	/**
	 * Ensure can parse valid JWT.
	 */
	@Test
	public void validJwt() throws Exception {
		this.doValidJwtTest(new MockClaims().setSub("Daniel").setExp(currentTimeOffset(5, TimeUnit.MINUTES)));
	}

	/**
	 * Ensure valid <code>nbf</code> with clock skew.
	 */
	@Test
	public void validNotBeforeWithClockSkew() throws Exception {
		this.doValidJwtTest(new MockClaims().setNbf(currentTimeOffset(2, TimeUnit.SECONDS)));
	}

	/**
	 * Ensure valid <code>exp</code> with clock skew.
	 */
	@Test
	public void validExpiryWithClockSkew() throws Exception {
		this.doValidJwtTest(new MockClaims().setExp(currentTimeOffset(-2, TimeUnit.SECONDS)));
	}

	/**
	 * Ensure invalid JWT as {@link JwtValidateKey} is old.
	 */
	@Test
	public void invalidDueToOldKey() throws Exception {
		this.doInvalidDecodeKeyTest(-20, -3);
	}

	/**
	 * Ensure invalid JWT as {@link JwtValidateKey} is too new.
	 */
	@Test
	public void invalidDueToNewKey() throws Exception {
		this.doInvalidDecodeKeyTest(3, 20);
	}

	/**
	 * Ensure valid JWT as {@link JwtValidateKey} is old but within clock skew.
	 */
	@Test
	public void validDueToOldKeyButWithinClockSkew() throws Exception {
		this.doValidDecodeKeyTest(-20, -2);
	}

	/**
	 * Ensure invalid JWT as {@link JwtValidateKey} is too new but within clock
	 * skew.
	 */
	@Test
	public void validDueToNewKeyButWithinClockSkew() throws Exception {
		this.doValidDecodeKeyTest(2, 20);
	}

	/**
	 * Ensure handle <code>null</code> {@link JwtValidateKey}.
	 */
	@Test
	public void nullDecodeKey() throws Exception {
		jwtDecodeCollectorHandler = (collector) -> {
			collector.setKeys(null, null, new JwtValidateKey(keyPair.getPublic()), null, null);
		};
		this.doValidJwtTest(new MockClaims().setSub("Daniel"));
	}

	/**
	 * Ensure can use multiple active {@link JwtValidateKey} instances.
	 */
	@Test
	public void multipleDecodeKeysActive() throws Exception {

		// Create two sets of keys
		KeyPair keyPairOne = Keys.keyPairFor(SignatureAlgorithm.RS256);
		KeyPair keyPairTwo = Keys.keyPairFor(SignatureAlgorithm.RS256);
		assertNotEquals("Invalid test, as public keys the same", keyPairOne.getPublic(), keyPairTwo.getPublic());
		jwtDecodeCollectorHandler = (collector) -> {
			collector.setKeys(new JwtValidateKey(keyPairOne.getPublic()), new JwtValidateKey(keyPairTwo.getPublic()));
		};

		// Start the server
		this.loadServer(null);

		// No JWT so unauthorised
		this.server.send(MockHttpServer.mockRequest(ECHO_CLAIMS_PATH))
				.assertResponse(HttpStatus.UNAUTHORIZED.getStatusCode(), "NO JWT");

		// Undertake JWT key test
		String expectedResponse = mapper.writeValueAsString(new MockClaims().setSub("Daniel"));
		Consumer<Key> assertValid = (key) -> {
			String token = Jwts.builder().signWith(keyPairOne.getPrivate()).claim("sub", "Daniel").compact();
			MockHttpRequestBuilder request = MockHttpServer.mockRequest(ECHO_CLAIMS_PATH);
			request.header("Authorization", "Bearer " + token);
			this.server.send(request).assertResponse(HttpStatus.OK.getStatusCode(), expectedResponse);
		};

		// Ensure valid for both keys
		assertValid.accept(keyPairOne.getPublic());
		assertValid.accept(keyPairTwo.getPublic());
	}

	/**
	 * Ensure access role enforced.
	 */
	@Test
	public void role() throws Exception {

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
				JacksonHttpObjectResponderFactory.getEntity(new HttpException(HttpStatus.FORBIDDEN), mapper));

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

		protected String role;

		public String getRole() {
			return this.role;
		}

		public RoleClaims setRole(String role) {
			this.role = role;
			return this;
		}
	}

	/**
	 * Ensure the JWT claims object is available for dependency injection.
	 */
	@Test
	public void injectJwtClaims() throws Exception {

		// Start server
		this.loadServer(null);

		// Validate able to inject claims
		String token = createJwtToken(new MockClaims().setSub("Daniel"), mockClaimsDecorator);
		MockHttpRequestBuilder request = MockHttpServer.mockRequest(INJECT_PATH);
		request.header("Authorization", "Bearer " + token);
		this.server.send(request).assertResponse(HttpStatus.OK.getStatusCode(), "INJECT: Daniel");
	}

	/**
	 * Creates the {@link JwtValidateKey}.
	 * 
	 * @param startSecondsOffset Seconds offset from current time for start time.
	 * @param secondsToExpire    Seconds offset from current time for expire time.
	 * @param key                {@link Key}.
	 * @return {@link JwtValidateKey}.
	 */
	private JwtValidateKey createJwtDecodeKey(long startSecondsOffset, long secondsToExpire, Key key) {
		long startTime = CURRENT_TIME + startSecondsOffset;
		long expireTime = CURRENT_TIME + secondsToExpire;
		return new JwtValidateKey(startTime, expireTime, key);
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
	 * Obtains the current time offset.
	 * 
	 * @param offset Offset on the current time.
	 * @param unit   {@link TimeUnit}.
	 * @return Current time offset.
	 */
	private static long currentTimeOffset(long offset, TimeUnit unit) {
		return Instant.ofEpochSecond(CURRENT_TIME).plusSeconds(unit.toSeconds(offset)).getEpochSecond();
	}

	/**
	 * Mock claims {@link Class}.
	 */
	public static class MockClaims extends RoleClaims {

		private String sub;

		private Long nbf;

		private Long exp;

		public String getSub() {
			return this.sub;
		}

		public MockClaims setSub(String sub) {
			this.sub = sub;
			return this;
		}

		public Long getNbf() {
			return this.nbf;
		}

		public MockClaims setNbf(Long nbf) {
			this.nbf = nbf;
			return this;
		}

		public Long getExp() {
			return this.exp;
		}

		public MockClaims setExp(Long exp) {
			this.exp = exp;
			return this;
		}
	}

	/**
	 * Ensure valid JWT with valid {@link JwtValidateKey}.
	 * 
	 * @param keyStartOffset  {@link JwtValidateKey} start seconds offset to current
	 *                        time.
	 * @param keyExpireOffset {@link JwtValidateKey} expire seconds offset to
	 *                        current time.
	 */
	private void doValidDecodeKeyTest(long keyStartOffset, long keyExpireOffset) throws Exception {
		jwtDecodeCollectorHandler = (collector) -> {
			collector.setKeys(createJwtDecodeKey(keyStartOffset, keyExpireOffset, keyPair.getPublic()));
		};
		this.doValidJwtTest(new MockClaims().setSub("Daniel"));
	}

	/**
	 * Ensure invalid JWT due to no {@link JwtValidateKey} matching time window.
	 * 
	 * @param keyStartOffset  {@link JwtValidateKey} start seconds offset to current
	 *                        time.
	 * @param keyExpireOffset {@link JwtValidateKey} expire seconds offset to
	 *                        current time.
	 */
	private void doInvalidDecodeKeyTest(long keyStartOffset, long keyExpireOffset) throws Exception {

		final MockClaims claims = new MockClaims().setSub("Daniel");

		// Ensure valid with default collector (valid key)
		this.doValidJwtTest(claims);

		// Ensure with outside window JWT that invalid
		jwtDecodeCollectorHandler = (collector) -> {
			collector.setKeys(createJwtDecodeKey(keyStartOffset, keyExpireOffset, keyPair.getPublic()));
		};
		this.doInvalidJwtTest(claims, "INVALID JWT");
	}

	/**
	 * Decorates the {@link JwtBuilder} for the {@link MockClaims}.
	 */
	private static final BiConsumer<MockClaims, JwtBuilder> mockClaimsDecorator = (claims, builder) -> {
		if (claims.sub != null) {
			builder.setSubject(claims.sub);
		}
		if (claims.exp != null) {
			builder.setExpiration(getDate(claims.exp));
		}
		if (claims.nbf != null) {
			builder.setNotBefore(getDate(claims.nbf));
		}
		if (claims.role != null) {
			builder.claim("role", claims.role);
		}
	};

	/**
	 * Convenience method to test invalid JWT with parsing JWT.
	 * 
	 * @param claims {@link MockClaims}.
	 * @param reason Invalid reason.
	 */
	private void doInvalidJwtTest(MockClaims claims, String reason) throws Exception {
		String token = this.createJwtToken(claims, mockClaimsDecorator);
		this.doJwtTest("Bearer " + token, HttpStatus.UNAUTHORIZED, reason);
	}

	/**
	 * Convenience method to test valid JWT with {@link MockClaims}.
	 * 
	 * @param claims {@link MockClaims}.
	 */
	private void doValidJwtTest(MockClaims mockClaims) throws Exception {
		this.doValidJwtTest(mockClaims, mockClaimsDecorator);
	}

	/**
	 * Undertakes a valid JWT test.
	 * 
	 * @param claims    Claims expected for response.
	 * @param decorator Decorates the {@link JwtBuilder} with the JWT to send.
	 */
	private <C> void doValidJwtTest(C claims, BiConsumer<C, JwtBuilder> decorator) throws Exception {
		String token = this.createJwtToken(claims, decorator);
		String response = mapper.writeValueAsString(claims);
		this.doJwtTest("Bearer " + token, HttpStatus.OK, response);
	}

	/**
	 * Creates the JWT.
	 * 
	 * @param claims    Claims for JWT.
	 * @param decorator Decorates the {@link JwtBuilder} with the JWT to send.
	 * @return JWT.
	 */
	private <C> String createJwtToken(C claims, BiConsumer<C, JwtBuilder> decorator) {
		JwtBuilder builder = Jwts.builder().signWith(keyPair.getPrivate());
		decorator.accept(claims, builder);
		return builder.compact();
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

		// Close existing server (if one created)
		if (this.officeFloor != null) {
			this.officeFloor.close();
		}

		// Compile the server
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.getOfficeFloorCompiler().setClockFactory(this.clockFactory);
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
			if (!isClaimsClassConfigured) {
				context.link(false, INJECT_PATH, InjectClaimsSection.class);
			}

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

	public static class InjectClaimsSection {
		public void service(MockClaims claims, ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("INJECT: " + claims.getSub());
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
		public void service(@Parameter JwtValidateKeyCollector collector) {
			jwtDecodeCollectorHandler.accept(collector);
		}
	}

	public static class JwtRoleCollectorServicer {
		public void service(@Parameter JwtRoleCollector<RoleClaims> collector) {
			collector.setRoles(Arrays.asList(collector.getClaims().getRole()));
		}
	}

}
