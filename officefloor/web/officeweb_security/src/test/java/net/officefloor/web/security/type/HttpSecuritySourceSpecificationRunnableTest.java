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
package net.officefloor.web.security.type;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.web.spi.security.HttpAuthenticateContext;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.web.spi.security.HttpCredentials;
import net.officefloor.web.spi.security.HttpLogoutContext;
import net.officefloor.web.spi.security.HttpRatifyContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
import net.officefloor.web.spi.security.HttpSecuritySourceMetaData;
import net.officefloor.web.spi.security.HttpSecuritySourceSpecification;

/**
 * Tests the {@link HttpSecuritySourceSpecificationRunnable}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySourceSpecificationRunnableTest extends OfficeFrameTestCase {

	/**
	 * Ensure can load specification.
	 */
	public void testLoadSpecification() throws Exception {

		// Compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Ensure obtain properties
		PropertyList properties = compiler.run(HttpSecuritySourceSpecificationRunnable.class,
				BasicHttpSecuritySource.class.getName());

		// Ensure properties are correct
		assertNotNull("Should have properties", properties);
		String[] names = properties.getPropertyNames();
		assertEquals("Incorrect number of properties", 1, names.length);
		assertEquals("Incorrect property", BasicHttpSecuritySource.PROPERTY_REALM, names[0]);
	}

	/**
	 * Ensure can load specification via convenience method.
	 */
	public void testConvenienceLoadSpecification() throws Exception {

		// Compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Load via convenience method
		PropertyList properties = HttpSecuritySourceSpecificationRunnable
				.loadSpecification(BasicHttpSecuritySource.class.getName(), compiler);

		// Ensure properties are correct
		assertNotNull("Should have properties", properties);
		String[] names = properties.getPropertyNames();
		assertEquals("Incorrect number of properties", 1, names.length);
		assertEquals("Incorrect property", BasicHttpSecuritySource.PROPERTY_REALM, names[0]);
	}

	/**
	 * Ensure notifies of issue in loading specification.
	 */
	public void testFailSpecification() throws Exception {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record issue in obtaining specification
		issues.recordIssue("No ManagedObjectSourceSpecification returned from "
				+ HttpSecurityManagedObjectAdapterSource.class.getName());

		// Test
		this.replayMockObjects();

		// Compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Ensure not obtain properties
		PropertyList properties = compiler.run(HttpSecuritySourceSpecificationRunnable.class,
				MockHttpSecuritySource.class.getName());
		assertNull("Should not have properties", properties);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link HttpSecuritySource} that fails loading specification.
	 */
	@TestSource
	public static class MockHttpSecuritySource implements
			HttpSecuritySource<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, None>,
			HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, None> {

		/*
		 * =============== HttpSecuritySource =================
		 */

		@Override
		public HttpSecuritySourceSpecification getSpecification() {
			return null;
		}

		@Override
		public HttpSecuritySourceMetaData<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, None> init(
				HttpSecuritySourceContext context) throws Exception {
			fail("Should not be required for loading specification");
			return null;
		}

		@Override
		public HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, None> sourceHttpSecurity(
				HttpSecurityContext context) throws HttpException {
			return this;
		}

		/*
		 * ================== HttpSecurity =====================
		 */

		@Override
		public HttpAuthentication<HttpCredentials> createAuthentication() {
			fail("Should not be required for loading specification");
			return null;
		}

		@Override
		public boolean ratify(HttpCredentials credentials, HttpRatifyContext<HttpAccessControl> context) {
			fail("Should not be required for loading specification");
			return false;
		}

		@Override
		public void authenticate(HttpCredentials credentials,
				HttpAuthenticateContext<HttpAccessControl, None> context) {
			fail("Should not be required for loading specification");
		}

		@Override
		public void challenge(HttpChallengeContext<None, None> context) {
			fail("Should not be required for loading specification");
		}

		@Override
		public void logout(HttpLogoutContext<None> context) {
			fail("Should not be required for loading specification");
		}
	}

}