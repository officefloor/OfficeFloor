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

import java.io.IOException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.security.type.HttpSecurityTypeRunnable;
import net.officefloor.web.spi.security.HttpAuthenticateContext;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.web.spi.security.HttpLogoutContext;
import net.officefloor.web.spi.security.HttpRatifyContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
import net.officefloor.web.spi.security.HttpSecuritySourceMetaData;
import net.officefloor.web.spi.security.HttpSecuritySourceSpecification;

/**
 * Tests the {@link HttpSecurityTypeRunnable}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityTypeRunnableTest extends OfficeFrameTestCase {

	/**
	 * Ensure can load {@link HttpSecurityType}.
	 */
	public void testLoadType() throws Exception {

		// Obtain the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Load the type
		HttpSecurityType<?, ?, ?, ?> type = compiler.run(HttpSecurityTypeRunnable.class,
				BasicHttpSecuritySource.class.getName(), BasicHttpSecuritySource.PROPERTY_REALM, "TEST");

		// Ensure have type
		assertNotNull("Should have type", type);
		assertEquals("Incorrect security class", HttpAccessControl.class.getName(), type.getSecurityClass().getName());
	}

	/**
	 * Ensure can load {@link HttpSecurityType} via convenience method.
	 */
	public void testConvenienceLoadType() throws Exception {

		// Obtain the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Create the property list
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(BasicHttpSecuritySource.PROPERTY_REALM).setValue("TEST");

		// Load the type
		HttpSecurityType<?, ?, ?, ?> type = HttpSecurityTypeRunnable
				.loadHttpSecurityType(BasicHttpSecuritySource.class.getName(), properties, compiler);

		// Ensure have type
		assertNotNull("Should have type", type);
		assertEquals("Incorrect security class", HttpAccessControl.class.getName(), type.getSecurityClass().getName());
	}

	/**
	 * Ensure handles fail loading {@link HttpSecurityType}.
	 */
	public void testFailType() throws Exception {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record issue in loading type
		issues.recordIssue("Returned null ManagedObjectSourceMetaData");

		// Test
		this.replayMockObjects();

		// Compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Ensure not obtain properties
		HttpSecurityType<?, ?, ?, ?> type = compiler.run(HttpSecurityTypeRunnable.class,
				MockHttpSecuritySource.class.getName());
		assertNull("Should not have type", type);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link HttpSecuritySource} to test failing to obtain
	 * {@link HttpSecurityType}.
	 */
	@TestSource
	public static class MockHttpSecuritySource implements HttpSecuritySource<Object, Object, Indexed, Indexed> {

		/*
		 * =========== HttpSecuritySource ====================
		 */

		@Override
		public HttpSecuritySourceSpecification getSpecification() {
			fail("Should not be invoked for loading type");
			return null;
		}

		@Override
		public HttpSecuritySourceMetaData<Object, Object, Indexed, Indexed> init(HttpSecuritySourceContext context)
				throws Exception {
			// No meta-data causing type load failure
			return null;
		}

		@Override
		public boolean ratify(HttpRatifyContext<Object, Object> context) {
			fail("Should not be invoked for loading type");
			return false;
		}

		@Override
		public void authenticate(HttpAuthenticateContext<Object, Object, Indexed> context) throws IOException {
			fail("Should not be invoked for loading type");
		}

		@Override
		public void challenge(HttpChallengeContext<Indexed, Indexed> context) throws IOException {
			fail("Should not be invoked for loading type");
		}

		@Override
		public void logout(HttpLogoutContext<Indexed> context) throws IOException {
			fail("Should not be invoked for loading type");
		}
	}

}