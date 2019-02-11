package net.officefloor.web.jwt.authority.server;

import static org.junit.Assert.assertNotEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.Qualifier;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.authority.JwtAuthority;
import net.officefloor.web.jwt.authority.JwtAuthorityManagedObjectSource;
import net.officefloor.web.jwt.repository.JwtAccessKey;
import net.officefloor.web.jwt.repository.JwtAuthorityRepository;
import net.officefloor.web.jwt.repository.JwtRefreshKey;
import net.officefloor.web.jwt.role.JwtRoleCollector;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.web.security.store.MockCredentialStoreManagedObjectSource;

/**
 * Ensure able have {@link JwtAuthority} and {@link JwtHttpSecuritySource}
 * available from single server.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtServerTest extends OfficeFrameTestCase {

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link MockHttpServer} for {@link JwtAuthority}.
	 */
	private MockHttpServer authorityServer;

	/**
	 * {@link MockHttpResponse} to obtain resource.
	 */
	private MockHttpServer resourceServer;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure single server able to provide {@link JwtAuthority} and
	 * {@link JwtHttpSecuritySource}.
	 */
	public void testSingleServer() throws Exception {

		// Compile web application
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.mockHttpServer((server) -> {
			this.authorityServer = server;
			this.resourceServer = server;
		});
		compiler.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			HttpSecurityArchitect security = HttpSecurityArchitectEmployer
					.employHttpSecurityArchitect(context.getWebArchitect(), office, context.getOfficeSourceContext());

			// Add the basic authentication
			security.addHttpSecurity(BASIC.class.getName(), BasicHttpSecuritySource.class.getName())
					.addProperty(BasicHttpSecuritySource.PROPERTY_REALM, "secure");

			// Add the mock credentials store for basic authentication
			office.addOfficeManagedObjectSource("CREDENTIALS", MockCredentialStoreManagedObjectSource.class.getName())
					.addOfficeManagedObject("CREDENTIALS", ManagedObjectScope.THREAD);

			// Add the JWT authority
			OfficeManagedObjectSource jwtAuthoritySource = office.addOfficeManagedObjectSource("JWT_AUTHORITY",
					JwtAuthorityManagedObjectSource.class.getName());
			jwtAuthoritySource.addProperty(JwtAuthorityManagedObjectSource.PROPERTY_IDENTITY_CLASS,
					Identity.class.getName());
			jwtAuthoritySource.addOfficeManagedObject("JWT_AUTHORITY", ManagedObjectScope.THREAD);

			// Add mock JWT authority repository
			office.addOfficeManagedObjectSource("JWT_AUTHORITY_REPOSITORY",
					new Singleton(new MockJwtAuthorityRepository()))
					.addOfficeManagedObject("JWT_AUTHORITY_REPOSITORY", ManagedObjectScope.THREAD);

			// Create JWT handlers
			OfficeSectionInput retrieveKeys = context.addSection("RETRIEVE_KEYS", SingleServerRetrieveKeysService.class)
					.getOfficeSectionInput("service");
			OfficeSectionInput retrieveRoles = context.addSection("RETRIEVE_ROLES", RetrieveRolesService.class)
					.getOfficeSectionInput("service");
			OfficeSectionInput noJwt = context.addSection("NO_JWT", NoJwtService.class)
					.getOfficeSectionInput("service");

			// Add the JWT authentication
			HttpSecurityBuilder jwt = security.addHttpSecurity("JWT", JwtHttpSecuritySource.class.getName());
			jwt.addProperty(JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, Claims.class.getName());
			jwt.addContentType("application/json");
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_KEYS.name()), retrieveKeys);
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_ROLES.name()), retrieveRoles);
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.NO_JWT.name()), noJwt);
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.INVALID_JWT.name()), noJwt);
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.EXPIRED_JWT.name()), noJwt);

			// Configure the login
			context.link(true, "/login", LoginService.class);

			// Configure the refresh of access token
			context.link(true, "/refresh", RefreshAccessTokenService.class);

			// Configure the resource
			context.link(true, "/resource", ResourceService.class);

			// Invoke web architect
			security.informWebArchitect();
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();

		// Test the server
		this.doJwtServerTest();
	}

	/**
	 * Undertakes test the JWT secured server.
	 */
	private void doJwtServerTest() throws Exception {

		// Ensure no access to resource
		MockHttpResponse response = this.resourceServer.send(MockHttpServer.mockRequest("/resource").secure(true));
		response.assertResponse(HttpStatus.UNAUTHORIZED.getStatusCode(), "");

		// Undertake login
		response = this.authorityServer.send(MockHttpServer.mockRequest("/login").secure(true).header("Authorization",
				BasicHttpSecuritySource.createAuthorizationHttpHeaderValue("test", "test")));
		String responseEntity = response.getEntity(null);
		assertEquals("Should login: " + responseEntity, 200, response.getStatus().getStatusCode());
		Tokens tokens = mapper.readValue(responseEntity, Tokens.class);

		// Ensure can access resource with access token
		Consumer<String> validateAccessToken = (accessToken) -> {
			try {
				MockHttpResponse validateResponse = this.resourceServer
						.send(MockHttpServer.mockRequest("/resource").secure(true).header("Authorization",
								JwtHttpSecuritySource.AUTHENTICATION_SCHEME_BEARER + " " + accessToken));
				String validateResponseEntity = validateResponse.getEntity(null);
				assertEquals("Should access resource: " + validateResponseEntity, 200,
						validateResponse.getStatus().getStatusCode());
				Resource resource = mapper.readValue(validateResponseEntity, Resource.class);
				assertEquals("Incorrect resource", "Hello JWT secured World", resource.getSecuredValue());
			} catch (Exception ex) {
				throw fail(ex);
			}
		};
		validateAccessToken.accept(tokens.accessToken);

		// Ensure can refresh access token
		response = this.authorityServer
				.send(MockHttpServer.mockRequest("/refresh").secure(true).header("Content-Type", "application/json")
						.entity(mapper.writeValueAsString(new RefreshAccessToken(tokens.refreshToken))));
		responseEntity = response.getEntity(null);
		assertEquals("Should refresh access token: " + responseEntity, 200, response.getStatus().getStatusCode());
		RefreshedAccessToken refreshed = mapper.readValue(responseEntity, RefreshedAccessToken.class);
		assertNotEquals("Should be new access token", tokens.accessToken, refreshed.accessToken);

		// Ensure can use new access token
		validateAccessToken.accept(refreshed.accessToken);
	}

	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface BASIC {
	}

	@Data
	public static class Tokens {
		private String refreshToken;
		private String accessToken;
	}

	@Data
	@RequiredArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Identity {
		private String name;
	}

	@Data
	@RequiredArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Claims {
		private String[] roles;
		private int random; // trigger different access tokens
	}

	@Data
	@RequiredArgsConstructor
	@AllArgsConstructor
	public static class Resource {
		private String securedValue;
	}

	@Data
	@RequiredArgsConstructor
	@AllArgsConstructor
	@HttpObject
	public static class RefreshAccessToken {
		private String refreshToken;
	}

	@Data
	@RequiredArgsConstructor
	@AllArgsConstructor
	public static class RefreshedAccessToken {
		private String accessToken;
	}

	private static Claims createClaims(Identity identity) {
		return new Claims(new String[] { identity.getName() }, ThreadLocalRandom.current().nextInt());
	}

	public static class LoginService {
		public void service(@BASIC HttpAccessControl accessControl, JwtAuthority<Identity> authority,
				ObjectResponse<Tokens> response) {

			// Obtain the authenticated user
			String name = accessControl.getPrincipal().getName();

			// Create the refresh token
			Identity identity = new Identity(name);
			String refreshToken = authority.createRefreshToken(identity);

			// Create the access token
			Claims claims = createClaims(identity);
			String accessToken = authority.createAccessToken(claims);

			// Provide tokens
			Tokens tokens = new Tokens();
			tokens.setRefreshToken(refreshToken);
			tokens.setAccessToken(accessToken);

			// Send response
			response.send(tokens);
		}
	}

	public static class ResourceService {
		@HttpAccess(ifRole = "test_jwt")
		public void service(ObjectResponse<Resource> response) {
			response.send(new Resource("Hello JWT secured World"));
		}
	}

	public static class SingleServerRetrieveKeysService {
		public void service(@Parameter JwtValidateKeyCollector collector, JwtAuthority<Identity> authority) {
			collector.setKeys(authority.getActiveJwtValidateKeys());
		}
	}

	public static class RetrieveRolesService {
		public void service(@Parameter JwtRoleCollector<Claims> collector) {
			String[] roles = collector.getClaims().getRoles();
			for (int i = 0; i < roles.length; i++) {
				roles[i] = roles[i] + "_jwt";
			}
			collector.setRoles(Arrays.asList(roles));
		}
	}

	public static class NoJwtService {
		public void service() {
			// nothing required as status already set
		}
	}

	public static class RefreshAccessTokenService {
		public void service(RefreshAccessToken refresh, JwtAuthority<Identity> authority,
				ObjectResponse<RefreshedAccessToken> response) {
			Identity identity = authority.decodeRefreshToken(refresh.refreshToken);
			Claims claims = createClaims(identity);
			String accessToken = authority.createAccessToken(claims);
			response.send(new RefreshedAccessToken(accessToken));
		}
	}

	private static class MockJwtAuthorityRepository implements JwtAuthorityRepository {

		private List<JwtAccessKey> accessKeys = Collections.synchronizedList(new ArrayList<>());

		private List<JwtRefreshKey> refreshKeys = Collections.synchronizedList(new ArrayList<>());

		@Override
		public List<JwtAccessKey> retrieveJwtAccessKeys(Instant activeAfter) throws Exception {
			return this.accessKeys;
		}

		@Override
		public void saveJwtAccessKeys(JwtAccessKey... accessKeys) throws Exception {
			this.accessKeys.addAll(Arrays.asList(accessKeys));
		}

		@Override
		public List<JwtRefreshKey> retrieveJwtRefreshKeys(Instant activeAfter) throws Exception {
			return this.refreshKeys;
		}

		@Override
		public void saveJwtRefreshKeys(JwtRefreshKey... refreshKeys) {
			this.refreshKeys.addAll(Arrays.asList(refreshKeys));
		}
	}

}