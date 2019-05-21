package net.officefloor.web.jwt.mock;

import java.io.IOException;
import java.util.Arrays;

import org.junit.runners.model.Statement;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.jwt.DefaultJwtChallengeSectionSource;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.mock.MockJwtAccessTokenRule;
import net.officefloor.web.jwt.role.JwtRoleCollector;
import net.officefloor.web.jwt.validate.JwtValidateKey;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;

/**
 * Tests the {@link MockJwtAccessTokenRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockJwtAuthorityRuleTest extends OfficeFrameTestCase {

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Indicates if {@link JwtValidateKeyCollector} was attempted. Should not as
	 * mocked out by the {@link MockJwtAccessTokenRule}.
	 */
	private static boolean isJwtValidateKeysCollected = false;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure using the {@link MockJwtAccessTokenRule} that can override the
	 * {@link JwtValidateKey} instances to create access tokens for testing the
	 * application.
	 */
	public void testOverrideJwtValidateKeys() throws Throwable {

		// Reset
		isJwtValidateKeysCollected = false;

		// Start mock server with wrapping rule
		MockJwtAccessTokenRule rule = new MockJwtAccessTokenRule();
		rule.apply(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
				compiler.mockHttpServer((server) -> MockJwtAuthorityRuleTest.this.server = server);
				compiler.web((context) -> {
					WebArchitect web = context.getWebArchitect();
					OfficeArchitect office = context.getOfficeArchitect();
					HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web,
							office, context.getOfficeSourceContext());

					// Configure JWT security
					HttpSecurityBuilder jwt = security.addHttpSecurity("JWT", JwtHttpSecuritySource.class.getName());
					jwt.addProperty(JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, Claims.class.getName());

					// Link JWT handling
					office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_KEYS.name()), context
							.addSection("RETREIVE_KEYS", RetrieveKeysSection.class).getOfficeSectionInput("service"));
					office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_ROLES.name()), context
							.addSection("RETREIVE_ROLES", RetrieveRolesSection.class).getOfficeSectionInput("service"));

					// Link JWT challenging
					OfficeSectionInput jwtChallenge = office
							.addOfficeSection("JWT_CHALLENGE", DefaultJwtChallengeSectionSource.class.getName(), null)
							.getOfficeSectionInput(JwtHttpSecuritySource.Flows.NO_JWT.name());
					office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.NO_JWT.name()), jwtChallenge);
					office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.INVALID_JWT.name()), jwtChallenge);
					office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.EXPIRED_JWT.name()), jwtChallenge);

					// Provide secured resource
					context.link(true, "/resource", SecuredResource.class);

					// Load security
					security.informWebArchitect();
				});
				MockJwtAuthorityRuleTest.this.officeFloor = compiler.compileAndOpenOfficeFloor();
			}
		}, null).evaluate();

		// Should have mocked JWT validate keys
		assertFalse("Should not retrieve JWT validate keys", isJwtValidateKeysCollected);

		// Ensure resource is secure
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/resource").secure(true));
		response.assertResponse(401, "");

		// Ensure can now access resource
		String accessToken = rule.createAccessToken(new Claims("test"));
		response = this.server.send(
				MockHttpServer.mockRequest("/resource").secure(true).header("authorization", "Bearer " + accessToken));
		response.assertResponse(200, "SECURED RESOURCE");

		// Ensure can use convenience method to access resource
		response = this.server
				.send(rule.authorize(new Claims("test"), MockHttpServer.mockRequest("/resource")).secure(true));
		response.assertResponse(200, "SECURED RESOURCE");

		// Should have mocked JWT validate keys
		assertFalse("After creating access tokens, should still not retrieve JWT validate keys",
				isJwtValidateKeysCollected);
	}

	public static class Claims {
		public String[] roles;

		public Claims() {
			// For JSON parsing
		}

		private Claims(String... roles) {
			this.roles = roles;
		}
	}

	public static class RetrieveKeysSection {
		public void service(@Parameter JwtValidateKeyCollector collector) {
			isJwtValidateKeysCollected = true;
			fail("Should not retrieve JWT validate keys as mocked");
		}
	}

	public static class RetrieveRolesSection {
		public void service(@Parameter JwtRoleCollector<Claims> collector) {
			collector.setRoles(Arrays.asList(collector.getClaims().roles));
		}
	}

	public static class SecuredResource {
		@HttpAccess(ifRole = "test")
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("SECURED RESOURCE");
		}
	}

}