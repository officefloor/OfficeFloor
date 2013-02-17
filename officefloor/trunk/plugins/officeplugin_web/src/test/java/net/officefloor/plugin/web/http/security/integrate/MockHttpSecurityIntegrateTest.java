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

import net.officefloor.plugin.web.http.application.HttpSecurityAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.security.scheme.MockHttpSecuritySource;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;

/**
 * Integrate tests the {@link MockHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpSecurityIntegrateTest extends
		AbstractHttpSecurityIntegrateTestCase {

	@Override
	protected HttpSecurityAutoWireSection configureHttpSecurity(
			WebAutoWireApplication application) throws Exception {

		// Configure the HTTP Security
		HttpSecurityAutoWireSection security = application
				.setHttpSecurity(MockHttpSecuritySource.class);

		// Return the HTTP Security
		return security;
	}

	/**
	 * Ensure can integrate.
	 */
	public void testIntegration() throws Exception {

		// Should not authenticate (without credentials)
		this.doRequest("service", 401, "");

		// Should authenticate with credentials
		this.getHttpClient()
				.getCredentialsProvider()
				.setCredentials(new AuthScope(null, -1, "Test"),
						new UsernamePasswordCredentials("daniel", "daniel"));
		this.doRequest("service", 200, "Serviced for daniel");
	}

	/**
	 * Ensure can logout.
	 */
	public void testLogout() throws Exception {

		// Authenticate with credentials
		this.getHttpClient()
				.getCredentialsProvider()
				.setCredentials(new AuthScope(null, -1, "Test"),
						new UsernamePasswordCredentials("daniel", "daniel"));
		this.doRequest("service", 200, "Serviced for daniel");

		// Clear login details
		this.getHttpClient().getCredentialsProvider().clear();

		// Request again to ensure stay logged in
		this.doRequest("service", 200, "Serviced for daniel");

		// Logout
		this.doRequest("logout", 200, "LOGOUT");

		// Should require to log in (after the log out)
		this.doRequest("service", 401, "");
	}

}