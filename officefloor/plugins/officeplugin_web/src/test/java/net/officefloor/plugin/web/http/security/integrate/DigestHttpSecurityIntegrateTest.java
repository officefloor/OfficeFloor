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

import org.apache.http.client.CredentialsProvider;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.plugin.web.http.application.HttpSecuritySection;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.security.scheme.DigestHttpSecuritySource;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.store.PasswordFileManagedObjectSource;

/**
 * Integrate the {@link DigestHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DigestHttpSecurityIntegrateTest extends
		AbstractHttpSecurityIntegrateTestCase {

	/**
	 * Realm.
	 */
	private static final String REALM = "TestRealm";

	@Override
	protected HttpSecuritySection configureHttpSecurity(
			WebArchitect application) throws Exception {

		// Configure the HTTP Security
		HttpSecuritySection security = application
				.setHttpSecurity(DigestHttpSecuritySource.class);
		security.addProperty(DigestHttpSecuritySource.PROPERTY_REALM, REALM);
		security.addProperty(DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY,
				"PrivateKey");

		// Password File Credential Store
		String passwordFilePath = this.findFile(this.getClass(),
				"digest-password-file.txt").getAbsolutePath();
		AutoWireObject passwordFile = application.addManagedObject(
				PasswordFileManagedObjectSource.class.getName(), null,
				new AutoWire(CredentialStore.class));
		passwordFile.addProperty(
				PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH,
				passwordFilePath);

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
		this.useCredentials(REALM, "Digest", "daniel", "password");
		this.doRequest("service", 200, "Serviced for daniel");
	}

	/**
	 * Ensure can logout.
	 */
	public void testLogout() throws Exception {

		// Authenticate with credentials
		CredentialsProvider provider = this.useCredentials(REALM, "Digest",
				"daniel", "password");
		this.doRequest("service", 200, "Serviced for daniel");

		// Clear login details
		provider.clear();

		// Request again to ensure stay logged in
		this.doRequest("service", 200, "Serviced for daniel");

		// Logout
		this.doRequest("logout", 200, "LOGOUT");

		// Should require to log in (after the log out)
		this.doRequest("service", 401, "");
	}

}