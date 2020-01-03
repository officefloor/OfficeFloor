package net.officefloor.web.security.type;

import java.io.Serializable;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
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
		HttpSecurityType<?, ?, ?, ?, ?> type = compiler.run(HttpSecurityTypeRunnable.class,
				BasicHttpSecuritySource.class.getName(), BasicHttpSecuritySource.PROPERTY_REALM, "TEST");

		// Ensure have type
		assertNotNull("Should have type", type);
		assertEquals("Incorrect authentication class", HttpAuthentication.class, type.getAuthenticationType());
		assertEquals("Incorrect access control class", HttpAccessControl.class, type.getAccessControlType());
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
		HttpSecurityType<?, ?, ?, ?, ?> type = HttpSecurityTypeRunnable
				.loadHttpSecurityType(BasicHttpSecuritySource.class.getName(), properties, compiler);

		// Ensure have type
		assertNotNull("Should have type", type);
		assertEquals("Incorrect authentication class", HttpAuthentication.class, type.getAuthenticationType());
		assertEquals("Incorrect access control class", HttpAccessControl.class, type.getAccessControlType());
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
		HttpSecurityType<?, ?, ?, ?, ?> type = compiler.run(HttpSecurityTypeRunnable.class,
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
	public static class MockHttpSecuritySource
			implements HttpSecuritySource<Object, Serializable, Object, Indexed, Indexed>,
			HttpSecurity<Object, Serializable, Object, Indexed, Indexed> {

		/*
		 * =========== HttpSecuritySource ====================
		 */

		@Override
		public HttpSecuritySourceSpecification getSpecification() {
			fail("Should not be invoked for loading type");
			return null;
		}

		@Override
		public HttpSecuritySourceMetaData<Object, Serializable, Object, Indexed, Indexed> init(
				HttpSecuritySourceContext context) throws Exception {
			// No meta-data causing type load failure
			return null;
		}

		@Override
		public void start(HttpSecurityExecuteContext<Indexed> context) throws Exception {
			// Nothing to start
		}

		@Override
		public HttpSecurity<Object, Serializable, Object, Indexed, Indexed> sourceHttpSecurity(
				HttpSecurityContext context) throws HttpException {
			return this;
		}

		@Override
		public void stop() {
			// Nothing to stop
		}

		/*
		 * ============ HttpSecurity ==========================
		 */

		@Override
		public Object createAuthentication(AuthenticationContext<Serializable, Object> context) {
			fail("Should not be invoked for loading type");
			return null;
		}

		@Override
		public boolean ratify(Object credentials, RatifyContext<Serializable> context) {
			fail("Should not be invoked for loading type");
			return false;
		}

		@Override
		public void authenticate(Object credentials, AuthenticateContext<Serializable, Indexed, Indexed> context) {
			fail("Should not be invoked for loading type");
		}

		@Override
		public void challenge(ChallengeContext<Indexed, Indexed> context) {
			fail("Should not be invoked for loading type");
		}

		@Override
		public void logout(LogoutContext<Indexed, Indexed> context) {
			fail("Should not be invoked for loading type");
		}
	}

}