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
package net.officefloor.plugin.web.http.security.type;

import java.io.IOException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpChallengeContext;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.HttpLogoutContext;
import net.officefloor.plugin.web.http.security.HttpRatifyContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceMetaData;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceSpecification;
import net.officefloor.plugin.web.http.security.scheme.BasicHttpSecuritySource;

/**
 * Tests the {@link HttpSecuritySourceSpecificationRunnable}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySourceSpecificationRunnableTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure can load specification.
	 */
	public void testLoadSpecification() throws Exception {

		// Compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);

		// Ensure obtain properties
		PropertyList properties = compiler.run(
				HttpSecuritySourceSpecificationRunnable.class,
				BasicHttpSecuritySource.class.getName());

		// Ensure properties are correct
		assertNotNull("Should have properties", properties);
		String[] names = properties.getPropertyNames();
		assertEquals("Incorrect number of properties", 1, names.length);
		assertEquals("Incorrect property",
				BasicHttpSecuritySource.PROPERTY_REALM, names[0]);
	}

	/**
	 * Ensure can load specification via convenience method.
	 */
	public void testConvenienceLoadSpecification() throws Exception {

		// Compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);

		// Load via convenience method
		PropertyList properties = HttpSecuritySourceSpecificationRunnable
				.loadSpecification(BasicHttpSecuritySource.class.getName(),
						compiler);

		// Ensure properties are correct
		assertNotNull("Should have properties", properties);
		String[] names = properties.getPropertyNames();
		assertEquals("Incorrect number of properties", 1, names.length);
		assertEquals("Incorrect property",
				BasicHttpSecuritySource.PROPERTY_REALM, names[0]);
	}

	/**
	 * Ensure notifies of issue in loading specification.
	 */
	public void testFailSpecification() throws Exception {

		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Record issue in obtaining specification
		issues.addIssue(
				null,
				null,
				AssetType.MANAGED_OBJECT,
				null,
				"No ManagedObjectSourceSpecification returned from "
						+ HttpSecurityManagedObjectAdapterSource.class
								.getName());

		// Test
		this.replayMockObjects();

		// Compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Ensure not obtain properties
		PropertyList properties = compiler.run(
				HttpSecuritySourceSpecificationRunnable.class,
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
			HttpSecuritySource<HttpSecurity, HttpCredentials, None, None> {

		/*
		 * =============== HttpSecuritySource =================
		 */

		@Override
		public HttpSecuritySourceSpecification getSpecification() {
			return null;
		}

		@Override
		public void init(HttpSecuritySourceContext context) throws Exception {
			fail("Should not be required for loading specification");
		}

		@Override
		public HttpSecuritySourceMetaData<HttpSecurity, HttpCredentials, None, None> getMetaData() {
			fail("Should not be required for loading specification");
			return null;
		}

		@Override
		public boolean ratify(
				HttpRatifyContext<HttpSecurity, HttpCredentials> context) {
			fail("Should not be required for loading specification");
			return false;
		}

		@Override
		public void authenticate(
				HttpAuthenticateContext<HttpSecurity, HttpCredentials, None> context)
				throws IOException {
			fail("Should not be required for loading specification");
		}

		@Override
		public void challenge(HttpChallengeContext<None, None> context)
				throws IOException {
			fail("Should not be required for loading specification");
		}

		@Override
		public void logout(HttpLogoutContext<None> context) throws IOException {
			fail("Should not be required for loading specification");
		}
	}

}