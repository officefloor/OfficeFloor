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
package net.officefloor.web.security.integrate;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.scheme.DigestHttpSecuritySource;
import net.officefloor.web.security.store.PasswordFileManagedObjectSource;

/**
 * Integrate the {@link DigestHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DigestHttpSecurityIntegrateTest extends AbstractHttpSecurityIntegrateTestCase {

	/**
	 * Realm.
	 */
	private static final String REALM = "TestRealm";

	@Override
	protected HttpSecurityBuilder configureHttpSecurity(CompileWebContext context,
			HttpSecurityArchitect securityArchitect) {

		// Configure the HTTP Security
		HttpSecurityBuilder security = securityArchitect.addHttpSecurity("SECURITY", DigestHttpSecuritySource.class);
		security.addProperty(DigestHttpSecuritySource.PROPERTY_REALM, REALM);
		security.addProperty(DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, "PrivateKey");

		// Obtain the password file
		String passwordFilePath;
		try {
			passwordFilePath = this.findFile(this.getClass(), "digest-password-file.txt").getAbsolutePath();
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Password File Credential Store
		OfficeManagedObjectSource passwordFileMos = context.getOfficeArchitect()
				.addOfficeManagedObjectSource("CREDENTIAL_STORE", PasswordFileManagedObjectSource.class.getName());
		passwordFileMos.addProperty(PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH, passwordFilePath);
		passwordFileMos.addOfficeManagedObject("CREDENTIAL_STORE", ManagedObjectScope.PROCESS);

		// Return the HTTP Security
		return security;
	}

	/**
	 * Ensure can integrate.
	 */
	public void testIntegration() throws Exception {

		// Should not authenticate (without credentials)
		this.doRequest("/service", 401, "");

		// Should authenticate with credentials
		MockHttpResponse init = this.doInit("/service");
		MockHttpResponse complete = this.doComplete("/service", init, "daniel", "password");
		complete.assertResponse(200, "Serviced for daniel");
	}

	/**
	 * Ensure can logout.
	 */
	public void testLogout() throws Exception {

		// Authenticate with credentials
		MockHttpResponse init = this.doInit("/service");
		MockHttpResponse complete = this.doComplete("/service", init, "daniel", "password");
		complete.assertResponse(200, "Serviced for daniel");

		// Request again to ensure stay logged in
		this.doRequest("service", init, 200, "Serviced for daniel");

		// Logout
		this.doRequest("logout", init, 200, "LOGOUT");

		// Should require to log in (after the log out)
		this.doRequest("service", init, 401, "");
	}

	/**
	 * Undertakes the initial digest request.
	 * 
	 * @param path
	 *            Path.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse doInit(String path) {

		// Undertake the request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest(path));

		// Ensure challenge
		assertEquals("Should be challenge", 401, response.getStatus().getStatusCode());

		// Return response
		return response;
	}

	/**
	 * Completes the digest request.
	 * 
	 * @param path
	 *            Path.
	 * @param init
	 *            Initialise digest {@link MockHttpResponse}.
	 * @param username
	 *            User name.
	 * @param password
	 *            Password.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse doComplete(String path, MockHttpResponse init, String username, String password) {

		// Obtain the init details

		// Undertake the request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest(path));

		// Return the response
		return response;
	}

}