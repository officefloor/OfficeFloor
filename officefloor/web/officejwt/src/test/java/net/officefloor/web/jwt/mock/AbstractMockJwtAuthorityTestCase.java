/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt.mock;

import java.io.IOException;
import java.util.Arrays;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.JUnitAgnosticAssert;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.jwt.DefaultJwtChallengeSectionSource;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.role.JwtRoleCollector;
import net.officefloor.web.jwt.validate.JwtValidateKey;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;

/**
 * Tests the {@link AbstractMockJwtAccessTokenJUnit}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractMockJwtAuthorityTestCase {

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * Indicates if {@link JwtValidateKeyCollector} was attempted. Should not as
	 * mocked out by the {@link MockJwtAccessTokenRule}.
	 */
	private static boolean isJwtValidateKeysCollected = false;

	/**
	 * Obtains the {@link AbstractMockJwtAccessTokenJUnit} under test.
	 * 
	 * @return {@link AbstractMockJwtAccessTokenJUnit} under test.
	 */
	protected abstract AbstractMockJwtAccessTokenJUnit getJwtAccessToken();

	/**
	 * Ensure using the {@link MockJwtAccessTokenRule} that can override the
	 * {@link JwtValidateKey} instances to create access tokens for testing the
	 * application.
	 */
	public void overrideJwtValidateKeys() throws Throwable {

		// Reset
		isJwtValidateKeysCollected = false;

		// Start mock server with wrapping rule
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.mockHttpServer((server) -> AbstractMockJwtAuthorityTestCase.this.server = server);
		compiler.web((context) -> {
			WebArchitect web = context.getWebArchitect();
			OfficeArchitect office = context.getOfficeArchitect();
			HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web, office,
					context.getOfficeSourceContext());

			// Configure JWT security
			HttpSecurityBuilder jwt = security.addHttpSecurity("JWT", JwtHttpSecuritySource.class.getName());
			jwt.addProperty(JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, Claims.class.getName());

			// Link JWT handling
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_KEYS.name()),
					context.addSection("RETREIVE_KEYS", RetrieveKeysSection.class).getOfficeSectionInput("service"));
			office.link(jwt.getOutput(JwtHttpSecuritySource.Flows.RETRIEVE_ROLES.name()),
					context.addSection("RETREIVE_ROLES", RetrieveRolesSection.class).getOfficeSectionInput("service"));

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
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Should have mocked JWT validate keys
			JUnitAgnosticAssert.assertFalse(isJwtValidateKeysCollected, "Should not retrieve JWT validate keys");

			// Ensure resource is secure
			MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/resource").secure(true));
			response.assertResponse(401, "");

			// Ensure can now access resource
			String accessToken = this.getJwtAccessToken().createAccessToken(new Claims("test"));
			response = this.server.send(MockHttpServer.mockRequest("/resource").secure(true).header("authorization",
					"Bearer " + accessToken));
			response.assertResponse(200, "SECURED RESOURCE");

			// Ensure can use convenience method to access resource
			response = this.server.send(this.getJwtAccessToken()
					.authorize(new Claims("test"), MockHttpServer.mockRequest("/resource")).secure(true));
			response.assertResponse(200, "SECURED RESOURCE");

			// Should have mocked JWT validate keys
			JUnitAgnosticAssert.assertFalse(isJwtValidateKeysCollected,
					"After creating access tokens, should still not retrieve JWT validate keys");
		}
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
			JUnitAgnosticAssert.fail("Should not retrieve JWT validate keys as mocked");
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
