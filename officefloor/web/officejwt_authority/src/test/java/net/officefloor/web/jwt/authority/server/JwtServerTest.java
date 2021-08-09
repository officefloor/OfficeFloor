/*-
 * #%L
 * JWT Authority
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.jwt.authority.server;

import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.jwt.DefaultJwtChallengeSectionSource;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.JwtHttpSecuritySource.Flows;
import net.officefloor.web.jwt.authority.AccessToken;
import net.officefloor.web.jwt.authority.JwtAuthority;
import net.officefloor.web.jwt.authority.JwtAuthorityManagedObjectSource;
import net.officefloor.web.jwt.authority.RefreshToken;
import net.officefloor.web.jwt.authority.combined.CombinedServerRetrieveValidateKeysSectionSource;
import net.officefloor.web.jwt.authority.jwks.JwksPublishSectionSource;
import net.officefloor.web.jwt.authority.repository.JwtAccessKey;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityRepository;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityRepository.RetrieveKeysContext;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityRepository.SaveKeysContext;
import net.officefloor.web.jwt.authority.repository.JwtRefreshKey;
import net.officefloor.web.jwt.jwks.JwksRetriever;
import net.officefloor.web.jwt.jwks.JwksSectionSource;
import net.officefloor.web.jwt.role.JwtRoleCollector;
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
	private OfficeFloor authorityOfficeFloor;

	/**
	 * {@link MockHttpServer} for {@link JwtAuthority}.
	 */
	private MockHttpServer authorityServer;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor resourceOfficeFloor;

	/**
	 * {@link MockHttpResponse} to obtain resource.
	 */
	private MockHttpServer resourceServer;

	/**
	 * {@link MockJwtAuthorityRepository}.
	 */
	private final MockJwtAuthorityRepository jwtAuthorityRepsoitory = new MockJwtAuthorityRepository();

	@Override
	protected void tearDown() throws Exception {
		if (this.authorityOfficeFloor != null) {
			this.authorityOfficeFloor.closeOfficeFloor();
		}
		if (this.resourceOfficeFloor != null) {
			this.resourceOfficeFloor.closeOfficeFloor();
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
			office.addOfficeManagedObjectSource("JWT_AUTHORITY_REPOSITORY", new Singleton(this.jwtAuthorityRepsoitory))
					.addOfficeManagedObject("JWT_AUTHORITY_REPOSITORY", ManagedObjectScope.THREAD);

			// Create JWT handlers
			OfficeSectionInput retrieveKeys = office.addOfficeSection("RETRIEVE_KEYS",
					CombinedServerRetrieveValidateKeysSectionSource.class.getName(), null)
					.getOfficeSectionInput(CombinedServerRetrieveValidateKeysSectionSource.INPUT);
			OfficeSectionInput retrieveRoles = context.addSection("RETRIEVE_ROLES", RetrieveRolesService.class)
					.getOfficeSectionInput("service");
			OfficeSection jwtChallenger = office.addOfficeSection("JWT_CHALLENGER",
					DefaultJwtChallengeSectionSource.class.getName(), null);

			// Add the JWT authentication
			HttpSecurityBuilder jwt = security.addHttpSecurity("JWT", JwtHttpSecuritySource.class.getName());
			jwt.addProperty(JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, Claims.class.getName());
			jwt.addContentType("application/json");
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_KEYS.name()), retrieveKeys);
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_ROLES.name()), retrieveRoles);
			for (Flows flow : new Flows[] { Flows.NO_JWT, Flows.INVALID_JWT, Flows.EXPIRED_JWT }) {
				office.link(jwt.getOutput(flow.name()), jwtChallenger.getOfficeSectionInput(flow.name()));
			}

			// Configure the login
			context.link(true, "/login", LoginService.class);

			// Configure the refresh of access token
			context.link(true, "/refresh", RefreshAccessTokenService.class);

			// Configure the resource
			context.link(true, "/resource", ResourceService.class);

			// Invoke web architect
			security.informWebArchitect();
		});
		this.authorityOfficeFloor = compiler.compileAndOpenOfficeFloor();

		// Test the server
		this.doJwtServerTest();
	}

	/**
	 * Ensure separate {@link JwtAuthority} server.
	 */
	public void testSeparateJwtAuthorityServer() throws Exception {

		// Compile the JWT authority server
		WebCompileOfficeFloor authorityCompiler = new WebCompileOfficeFloor();
		authorityCompiler.mockHttpServer((server) -> this.authorityServer = server);
		authorityCompiler.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();
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
			office.addOfficeManagedObjectSource("JWT_AUTHORITY_REPOSITORY", new Singleton(this.jwtAuthorityRepsoitory))
					.addOfficeManagedObject("JWT_AUTHORITY_REPOSITORY", ManagedObjectScope.THREAD);

			// Configure the login
			context.link(true, "/login", LoginService.class);

			// Configure the refresh of access token
			context.link(true, "/refresh", RefreshAccessTokenService.class);

			// Configure publishing the keys
			OfficeSection jwksPublish = office.addOfficeSection("JWKS_PUBLISH",
					JwksPublishSectionSource.class.getName(), null);
			HttpUrlContinuation keysPath = web.getHttpInput(true, "/keys");
			office.link(keysPath.getInput(), jwksPublish.getOfficeSectionInput(JwksPublishSectionSource.INPUT));

			// Invoke web architect
			security.informWebArchitect();
		});
		this.authorityOfficeFloor = authorityCompiler.compileAndOpenOfficeFloor();

		// Compile the resource server
		WebCompileOfficeFloor resourceCompiler = new WebCompileOfficeFloor();
		resourceCompiler.mockHttpServer((server) -> this.resourceServer = server);
		resourceCompiler.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			HttpSecurityArchitect security = HttpSecurityArchitectEmployer
					.employHttpSecurityArchitect(context.getWebArchitect(), office, context.getOfficeSourceContext());

			// Create JWT handlers
			OfficeSectionInput retrieveKeys = office
					.addOfficeSection("RETRIEVE_KEYS", JwksSectionSource.class.getName(), null)
					.getOfficeSectionInput(JwksSectionSource.INPUT);
			OfficeSectionInput retrieveRoles = context.addSection("RETRIEVE_ROLES", RetrieveRolesService.class)
					.getOfficeSectionInput("service");
			OfficeSection jwtChallenger = office.addOfficeSection("JWT_CHALLENGER",
					DefaultJwtChallengeSectionSource.class.getName(), null);

			// Add the JWT authentication
			HttpSecurityBuilder jwt = security.addHttpSecurity("JWT", JwtHttpSecuritySource.class.getName());
			jwt.addProperty(JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, Claims.class.getName());
			jwt.addContentType("application/json");
			office.link(jwt.getOutput(Flows.RETRIEVE_KEYS.name()), retrieveKeys);
			office.link(jwt.getOutput(Flows.RETRIEVE_ROLES.name()), retrieveRoles);
			for (Flows flow : new Flows[] { Flows.NO_JWT, Flows.INVALID_JWT, Flows.EXPIRED_JWT }) {
				office.link(jwt.getOutput(flow.name()), jwtChallenger.getOfficeSectionInput(flow.name()));
			}

			// Add the JWKS retriever
			JwksRetriever retriever = new JwksRetriever() {
				@Override
				public InputStream retrieveJwks() throws Exception {
					MockHttpResponse response = JwtServerTest.this.authorityServer
							.send(MockHttpServer.mockRequest("/keys").secure(true));
					if (response.getStatus().getStatusCode() != 200) {
						assertEquals("Should be successful: " + response.getEntity(null), 200,
								response.getStatus().getStatusCode());
					}
					return response.getEntity();
				}
			};
			office.addOfficeManagedObjectSource("JWKS_RETRIEVER", new Singleton(retriever))
					.addOfficeManagedObject("JWKS_RETRIEVER", ManagedObjectScope.THREAD);

			// Configure the resource
			context.link(true, "/resource", ResourceService.class);

			// Invoke web architect
			security.informWebArchitect();
		});
		this.resourceOfficeFloor = resourceCompiler.compileAndOpenOfficeFloor();

		// Test the server
		this.doJwtServerTest();
	}

	/**
	 * Ensure can load keys for server.
	 */
	public void testReuseKeysFromRepository() throws Exception {

		// Load the server (creates and saves keys)
		this.testSingleServer();

		// Ensure have keys
		assertEquals("Should have access keys", 2, this.jwtAuthorityRepsoitory.accessKeys.size());
		assertEquals("Should have refresh keys", 2, this.jwtAuthorityRepsoitory.refreshKeys.size());

		// Ensure can re-use keys (may create extra key on edge of time)
		this.testSingleServer();
		assertTrue("Should re-use access keys", this.jwtAuthorityRepsoitory.accessKeys.size() <= 3);
		assertTrue("Should re-use refresh keys", this.jwtAuthorityRepsoitory.refreshKeys.size() <= 3);
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
	public static class Identity {
		private String name;
	}

	@Data
	@RequiredArgsConstructor
	@AllArgsConstructor
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
			RefreshToken refreshToken = authority.createRefreshToken(identity);

			// Create the access token
			Claims claims = createClaims(identity);
			AccessToken accessToken = authority.createAccessToken(claims);

			// Provide tokens
			Tokens tokens = new Tokens();
			tokens.setRefreshToken(refreshToken.getToken());
			tokens.setAccessToken(accessToken.getToken());

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

	public static class RetrieveRolesService {
		public void service(@Parameter JwtRoleCollector<Claims> collector) {
			String[] roles = collector.getClaims().getRoles();
			for (int i = 0; i < roles.length; i++) {
				roles[i] = roles[i] + "_jwt";
			}
			collector.setRoles(Arrays.asList(roles));
		}
	}

	public static class RefreshAccessTokenService {
		public void service(RefreshAccessToken refresh, JwtAuthority<Identity> authority,
				ObjectResponse<RefreshedAccessToken> response) {
			Identity identity = authority.decodeRefreshToken(refresh.refreshToken);
			Claims claims = createClaims(identity);
			AccessToken accessToken = authority.createAccessToken(claims);
			response.send(new RefreshedAccessToken(accessToken.getToken()));
		}
	}

	private static class MockJwtAuthorityRepository implements JwtAuthorityRepository {

		private List<MockStoredJwtAccessKey> accessKeys = Collections.synchronizedList(new ArrayList<>());

		private List<MockStoredJwtRefreshKey> refreshKeys = Collections.synchronizedList(new ArrayList<>());

		@Override
		public List<JwtAccessKey> retrieveJwtAccessKeys(RetrieveKeysContext context) throws Exception {
			return this.accessKeys.stream().map((storedKey) -> new MockJwtAccessKey(storedKey, context))
					.collect(Collectors.toList());
		}

		@Override
		public void saveJwtAccessKeys(SaveKeysContext context, JwtAccessKey... accessKeys) throws Exception {
			for (JwtAccessKey accessKey : accessKeys) {
				this.accessKeys.add(new MockStoredJwtAccessKey(accessKey, context));
			}
		}

		@Override
		public List<JwtRefreshKey> retrieveJwtRefreshKeys(RetrieveKeysContext context) throws Exception {
			return this.refreshKeys.stream().map((storedKey) -> new MockJwtRefreshKey(storedKey, context))
					.collect(Collectors.toList());
		}

		@Override
		public void saveJwtRefreshKeys(SaveKeysContext context, JwtRefreshKey... refreshKeys) {
			for (JwtRefreshKey refreshKey : refreshKeys) {
				this.refreshKeys.add(new MockStoredJwtRefreshKey(refreshKey, context));
			}
		}
	}

	private static class MockStoredJwtAccessKey {

		private final long startTime;

		private final long expireTime;

		private final String publicKey;

		private final String privateKey;

		private MockStoredJwtAccessKey(JwtAccessKey accessKey, SaveKeysContext context) {
			this.startTime = accessKey.getStartTime();
			this.expireTime = accessKey.getExpireTime();
			this.publicKey = context.serialise(accessKey.getPublicKey());
			this.privateKey = context.serialise(accessKey.getPrivateKey());
		}
	}

	private static class MockJwtAccessKey implements JwtAccessKey {

		private final MockStoredJwtAccessKey storedKey;

		private final Key publicKey;

		private final Key privateKey;

		private MockJwtAccessKey(MockStoredJwtAccessKey storedKey, RetrieveKeysContext context) {
			this.storedKey = storedKey;
			this.publicKey = context.deserialise(storedKey.publicKey);
			this.privateKey = context.deserialise(storedKey.privateKey);
		}

		@Override
		public long getStartTime() {
			return this.storedKey.startTime;
		}

		@Override
		public long getExpireTime() {
			return this.storedKey.expireTime;
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

	private static class MockStoredJwtRefreshKey {

		private final long startTime;

		private final long expireTime;

		private final String initVector;

		private final String startSalt;

		private final String lace;

		private final String endSalt;

		private final String serialisedKey;

		private MockStoredJwtRefreshKey(JwtRefreshKey refreshKey, SaveKeysContext context) {
			this.startTime = refreshKey.getStartTime();
			this.expireTime = refreshKey.getExpireTime();
			this.initVector = refreshKey.getInitVector();
			this.startSalt = refreshKey.getStartSalt();
			this.lace = refreshKey.getLace();
			this.endSalt = refreshKey.getEndSalt();
			this.serialisedKey = context.serialise(refreshKey.getKey());
		}
	}

	private static class MockJwtRefreshKey implements JwtRefreshKey {

		private final MockStoredJwtRefreshKey storedKey;

		private final Key key;

		private MockJwtRefreshKey(MockStoredJwtRefreshKey storedKey, RetrieveKeysContext context) {
			this.storedKey = storedKey;
			this.key = context.deserialise(storedKey.serialisedKey);
		}

		@Override
		public long getStartTime() {
			return this.storedKey.startTime;
		}

		@Override
		public long getExpireTime() {
			return this.storedKey.expireTime;
		}

		@Override
		public String getInitVector() {
			return this.storedKey.initVector;
		}

		@Override
		public String getStartSalt() {
			return this.storedKey.startSalt;
		}

		@Override
		public String getLace() {
			return this.storedKey.lace;
		}

		@Override
		public String getEndSalt() {
			return this.storedKey.endSalt;
		}

		@Override
		public Key getKey() {
			return this.key;
		}
	}

}
