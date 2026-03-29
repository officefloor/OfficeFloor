/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecurityExecuteContext;
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
		public void start(HttpSecurityExecuteContext<None> context) throws Exception {
			fail("Should not be required for loading specification");
		}

		@Override
		public HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, None> sourceHttpSecurity(
				HttpSecurityContext context) throws HttpException {
			return this;
		}

		@Override
		public void stop() {
			fail("Should not be required for loading specification");
		}

		/*
		 * ================== HttpSecurity =====================
		 */

		@Override
		public HttpAuthentication<HttpCredentials> createAuthentication(
				AuthenticationContext<HttpAccessControl, HttpCredentials> context) {
			fail("Should not be required for loading specification");
			return null;
		}

		@Override
		public boolean ratify(HttpCredentials credentials, RatifyContext<HttpAccessControl> context) {
			fail("Should not be required for loading specification");
			return false;
		}

		@Override
		public void authenticate(HttpCredentials credentials,
				AuthenticateContext<HttpAccessControl, None, None> context) {
			fail("Should not be required for loading specification");
		}

		@Override
		public void challenge(ChallengeContext<None, None> context) {
			fail("Should not be required for loading specification");
		}

		@Override
		public void logout(LogoutContext<None, None> context) {
			fail("Should not be required for loading specification");
		}
	}

}
