/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.integrate;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.scheme.FormHttpSecuritySource;
import net.officefloor.web.security.scheme.HttpCredentialsImpl;
import net.officefloor.web.security.store.MockCredentialStoreManagedObjectSource;

/**
 * Integrate the {@link FormHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FormHttpSecurityIntegrateTest extends AbstractHttpSecurityIntegrateTestCase {

	@Override
	protected HttpSecurityBuilder configureHttpSecurity(CompileWebContext context,
			HttpSecurityArchitect securityArchitect) {
		OfficeArchitect office = context.getOfficeArchitect();
		WebArchitect web = context.getWebArchitect();

		// Configure the HTTP Security
		HttpSecurityBuilder security = securityArchitect.addHttpSecurity("SECURITY",
				FormHttpSecuritySource.class.getName());
		security.addProperty(FormHttpSecuritySource.PROPERTY_REALM, "TestRealm");

		// Provide the form login page
		OfficeSection form = context.addSection("FORM", LoginPage.class);
		office.link(security.getOutput("form"), form.getOfficeSectionInput("form"));
		office.link(web.getHttpInput(false, "/login").getInput(), form.getOfficeSectionInput("login"));
		office.link(form.getOfficeSectionOutput("authenticate"), security.getAuthenticateInput());

		// Provide parameters for login form
		web.addHttpRequestObject(LoginForm.class, true);

		// Mock Credential Store
		office.addOfficeManagedObjectSource("CREDENTIAL_STORE", MockCredentialStoreManagedObjectSource.class.getName())
				.addOfficeManagedObject("CREDENTIAL_STORE", ManagedObjectScope.PROCESS);

		// Return the HTTP Security
		return security;
	}

	/**
	 * Login form parameters.
	 */
	public static class LoginForm implements Serializable {
		private static final long serialVersionUID = 1L;

		private String username;

		private String password;

		public void setUsername(String username) {
			this.username = username;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	/**
	 * Login page functionality for testing.
	 */
	public static class LoginPage {

		public void form(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("LOGIN");
		}

		@Next("authenticate")
		public HttpCredentials login(LoginForm form) {
			return new HttpCredentialsImpl(form.username, form.password);
		}
	}

	/**
	 * Ensure can integrate.
	 */
	public void testIntegration() throws Exception {

		// Should be returned the login page
		MockHttpResponse response = this.doRequest("/service", 200, "LOGIN");

		// Send back login credentials and get service page
		this.doRequest("/login?username=daniel&password=daniel", response, 200, "Serviced for daniel");
	}

	/**
	 * Ensure can go through multiple login attempts.
	 */
	public void testMultipleLoginAttempts() throws Exception {

		// Initiate triggering login
		MockHttpResponse response = this.doRequest("/service", 200, "LOGIN");

		// Should not login multiple times
		this.doRequest("/login", response, 200, "LOGIN");
		this.doRequest("/login?username=daniel", response, 200, "LOGIN");
		this.doRequest("/login?password=daniel", response, 200, "LOGIN");

		// Now log in
		this.doRequest("/login?username=daniel&password=daniel", response, 200, "Serviced for daniel");
	}

	/**
	 * Ensure can logout.
	 */
	public void testLogout() throws Exception {

		// Initiate triggering login
		MockHttpResponse response = this.doRequest("/service", 200, "LOGIN");

		// Send back login credentials and get service page
		this.doRequest("/login?username=daniel&password=daniel", response, 200, "Serviced for daniel");

		// Request again to ensure stay logged in
		this.doRequest("/service", response, 200, "Serviced for daniel");

		// Logout
		this.doRequest("/logout", response, 200, "LOGOUT");

		// Should require to log in (after the log out)
		this.doRequest("/service", response, 200, "LOGIN");
	}

}
