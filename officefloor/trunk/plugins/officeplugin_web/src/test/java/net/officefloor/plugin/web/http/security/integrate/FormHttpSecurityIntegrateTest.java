/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.security.integrate;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpSecurityAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.scheme.FormHttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.HttpCredentialsImpl;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.store.MockCredentialStoreManagedObjectSource;

/**
 * Integrate the {@link FormHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FormHttpSecurityIntegrateTest extends
		AbstractHttpSecurityIntegrateTestCase {

	@Override
	protected HttpSecurityAutoWireSection configureHttpSecurity(
			WebAutoWireApplication application) throws Exception {

		// Configure the HTTP Security
		HttpSecurityAutoWireSection security = application
				.setHttpSecurity(FormHttpSecuritySource.class);
		security.addProperty(FormHttpSecuritySource.PROPERTY_REALM, "TestRealm");

		// Provide the form login page
		AutoWireSection form = application.addSection("FORM",
				ClassSectionSource.class.getName(), LoginPage.class.getName());
		application.link(security, "FORM_LOGIN_PAGE", form, "form");
		application.linkUri("login", form, "login");
		application.link(form, "authenticate", security, "Authenticate");

		// Provide parameters for login form
		application.addHttpRequestObject(LoginForm.class, true);

		// Mock Credential Store
		application.addManagedObject(
				MockCredentialStoreManagedObjectSource.class.getName(), null,
				new AutoWire(CredentialStore.class));

		// Return the HTTP Security
		return security;
	}

	/**
	 * Login form parameters.
	 */
	public static class LoginForm implements Serializable {

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
			connection.getHttpResponse().getEntityWriter().write("LOGIN");
		}

		@NextTask("authenticate")
		public HttpCredentials login(LoginForm form) {
			return new HttpCredentialsImpl(form.username, form.password);
		}
	}

	/**
	 * Ensure can integrate.
	 */
	public void testIntegration() throws Exception {

		// Should be returned the login page
		this.doRequest("service", 200, "LOGIN");

		// Send back login credentials and get service page
		this.doRequest("login?username=daniel&password=daniel", 200,
				"Serviced for daniel");
	}

}