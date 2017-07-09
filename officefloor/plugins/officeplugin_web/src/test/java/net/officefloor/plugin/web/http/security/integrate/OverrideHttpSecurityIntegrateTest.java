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

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.plugin.web.http.application.HttpSecuritySection;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.security.HttpAuthenticationRequiredException;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource;
import net.officefloor.plugin.web.http.security.scheme.MockHttpSecuritySource;

/**
 * <p>
 * Enables overriding the {@link HttpSecurityManagedObjectSource}.
 * <p>
 * This is typically to enable providing a <code>null</code>
 * {@link HttpSecurity} (rather than escalating
 * {@link HttpAuthenticationRequiredException}).
 * 
 * @author Daniel Sagenschneider
 */
public class OverrideHttpSecurityIntegrateTest extends
		AbstractHttpSecurityIntegrateTestCase {

	@Override
	protected HttpSecuritySection configureHttpSecurity(
			WebArchitect application) throws Exception {

		// Override the HTTP Security
		AutoWireObject object = application.addManagedObject(
				HttpSecurityManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSecurity.class));
		object.setTimeout(1000);
		object.addProperty(
				HttpSecurityManagedObjectSource.PROPERTY_HTTP_SECURITY_TYPE,
				HttpSecurity.class.getName());
		object.addProperty(
				HttpSecurityManagedObjectSource.PROPERTY_IS_ESCALATE_AUTHENTICATION_REQUIRED,
				String.valueOf(false));

		// Configure the HTTP Security
		HttpSecuritySection security = application
				.setHttpSecurity(MockHttpSecuritySource.class);

		// Return the HTTP Security
		return security;
	}

	/**
	 * Ensure can integrate.
	 */
	public void testIntegration() throws Exception {

		// Should service by providing null HTTP Security
		this.doRequest("service", 200, "Serviced for guest");
	}

}