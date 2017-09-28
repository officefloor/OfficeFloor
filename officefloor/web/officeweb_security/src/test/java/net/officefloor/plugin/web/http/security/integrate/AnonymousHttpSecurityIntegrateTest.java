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

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.plugin.web.http.security.AnonymousHttpAuthenticationManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpAuthentication;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource;
import net.officefloor.plugin.web.http.test.CompileWebContext;
import net.officefloor.web.state.HttpSecuritySection;

/**
 * Enables overriding the {@link HttpSecurityManagedObjectSource} and use the
 * {@link HttpAuthentication} via the
 * {@link AnonymousHttpAuthenticationManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AnonymousHttpSecurityIntegrateTest extends AbstractHttpSecurityIntegrateTestCase {

	@Override
	protected HttpSecuritySection configureHttpSecurity(CompileWebContext context) {

		// Override the HTTP Security
		OfficeManagedObjectSource object = context.getOfficeArchitect().addOfficeManagedObjectSource("SECURITY",
				new HttpSecurityManagedObjectSource());
		object.setTimeout(1000);
		object.addProperty(HttpSecurityManagedObjectSource.PROPERTY_HTTP_SECURITY_TYPE, HttpSecurity.class.getName());
		object.addProperty(HttpSecurityManagedObjectSource.PROPERTY_IS_ESCALATE_AUTHENTICATION_REQUIRED,
				String.valueOf(false));

		// No HTTP security configured
		return null;
	}

	/**
	 * Ensure can integrate.
	 */
	public void testIntegration() throws Exception {

		// Should service by providing null HTTP Security
		this.doRequest("service", 200, "Serviced for guest");
	}

	/**
	 * Ensure logout does nothing.
	 */
	public void testLogout() throws Exception {

		// Should service by providing null HTTP Security
		this.doRequest("service", 200, "Serviced for guest");

		// Undertake logout
		this.doRequest("logout", 200, "LOGOUT");

		// Should again service by providing null HTTP Security
		this.doRequest("service", 200, "Serviced for guest");
	}

}