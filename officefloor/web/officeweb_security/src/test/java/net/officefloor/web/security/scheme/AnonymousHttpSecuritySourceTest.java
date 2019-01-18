/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.scheme;

import java.io.IOException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Tests the {@link AnonymousHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AnonymousHttpSecuritySourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(AnonymousHttpSecuritySource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(HttpAuthentication.class);
		type.setAccessControlClass(HttpAccessControl.class);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, AnonymousHttpSecuritySource.class);
	}

	/**
	 * Ensure can ratify always provides {@link HttpAccessControl}.
	 */
	public void testRatify() throws IOException {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<>();

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(AnonymousHttpSecuritySource.class);

		// Undertake ratify
		assertTrue("Should indicate that may attempt to authenticate", security.ratify(null, ratifyContext));
		HttpAccessControl accessControl = ratifyContext.getAccessControl();
		assertNotNull("Should always provide access control", accessControl);
		assertEquals("Incorrect authentication scheme", "Anonymous", accessControl.getAuthenticationScheme());
		assertEquals("Incorrect principal", "anonymous", accessControl.getPrincipal().getName());
		assertTrue("Always in role", accessControl.inRole("role"));

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {

		final MockHttpLogoutContext<None, None> logoutContext = new MockHttpLogoutContext<>();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(AnonymousHttpSecuritySource.class);

		// Logout (should be no operation as always logged in anonymously)
		security.logout(logoutContext);
	}

}