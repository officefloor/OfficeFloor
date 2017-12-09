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
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.impl.HttpAuthenticationRequiredException;
import net.officefloor.web.security.impl.HttpAccessControlManagedObjectSource;
import net.officefloor.web.security.scheme.MockChallengeHttpSecuritySource;

/**
 * <p>
 * Enables overriding the {@link HttpAccessControlManagedObjectSource}.
 * <p>
 * This is typically to enable providing a <code>null</code>
 * {@link HttpAccessControl} (rather than escalating
 * {@link HttpAuthenticationRequiredException}).
 * 
 * @author Daniel Sagenschneider
 */
public class OverrideHttpSecurityIntegrateTest extends AbstractHttpSecurityIntegrateTestCase {

	@Override
	protected HttpSecurityBuilder configureHttpSecurity(CompileWebContext context,
			HttpSecurityArchitect securityArchitect) {

		// Override the HTTP Security
		OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("SECURITY",
				HttpAccessControlManagedObjectSource.class.getName());
		mos.setTimeout(1000);
		mos.addProperty(HttpAccessControlManagedObjectSource.PROPERTY_ACCESS_CONTROL_TYPE, HttpAccessControl.class.getName());
		mos.addProperty(HttpAccessControlManagedObjectSource.PROPERTY_IS_ESCALATE_AUTHENTICATION_REQUIRED,
				String.valueOf(false));

		// Configure the HTTP Security
		HttpSecurityBuilder security = securityArchitect.addHttpSecurity("SECURITY",
				MockChallengeHttpSecuritySource.class);

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