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
package net.officefloor.plugin.web.http.security;

import org.easymock.AbstractMatcher;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource.Dependencies;

/**
 * Tests the {@link HttpSecurityManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpAuthentication}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpAuthentication<HttpSecurity, Void> authentication = this
			.createMock(HttpAuthentication.class);

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				HttpSecurityManagedObjectSource.class,
				HttpSecurityManagedObjectSource.PROPERTY_HTTP_SECURITY_TYPE,
				"HTTP Security Type");
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpSecurity.class);
		type.addDependency(Dependencies.HTTP_AUTHENTICATION,
				HttpAuthentication.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpSecurityManagedObjectSource.class,
				HttpSecurityManagedObjectSource.PROPERTY_HTTP_SECURITY_TYPE,
				HttpSecurity.class.getName());
	}

	/**
	 * Ensure not loading HTTP security as waiting on authentication.
	 */
	public void testLoadWaitingOnAuthentication() throws Throwable {
		final HttpSecurity authenticationSecurity = this
				.createMock(HttpSecurity.class);
		HttpSecurity loadedSecurity = this.loadHttpSecurity(
				authenticationSecurity, false, false);
		assertNull("Should not yet have security", loadedSecurity);
	}

	/**
	 * Ensure can load as authenticated.
	 */
	public void testLoadAuthenticated() throws Throwable {
		final HttpSecurity authenticationSecurity = this
				.createMock(HttpSecurity.class);
		HttpSecurity loadedSecurity = this.loadHttpSecurity(
				authenticationSecurity, true, false);
		assertSame("Should be same security", authenticationSecurity,
				loadedSecurity);
	}

	/**
	 * Ensure escalation as not authenticated.
	 */
	public void testLoadNotAuthenticated() throws Throwable {
		try {
			this.loadHttpSecurity(null, true, false);
			fail("Should not be successful");
		} catch (HttpAuthenticationRequiredException ex) {
			// Should not be authenticated
		}
	}

	/**
	 * Ensure can configure to just provides <code>null</code> HTTP Security if
	 * not authenticated (rather than escalating).
	 */
	public void testLoadNotAuthenticatedAllowingNull() throws Throwable {
		HttpSecurity security = this.loadHttpSecurity(null, true, true);
		assertNull("Should not have security", security);
	}

	/**
	 * Loads the HTTP security.
	 * 
	 * @param httpSecurity
	 *            {@link HttpSecurity} to be returned from
	 *            {@link HttpAuthentication}.
	 * @param isAuthenticatedImmediately
	 *            Indicates if the HTTP security is authenticated immediately.
	 * @param isAllowNullHttpSecurity
	 *            Indicates whether will allow <code>null</code> HTTP Security.
	 * @return {@link HttpSecurity} from {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	private HttpSecurity loadHttpSecurity(HttpSecurity httpSecurity,
			final boolean isAuthenticatedImmediately,
			boolean isAllowNullHttpSecurity) throws Throwable {

		final AsynchronousListener listener = this
				.createMock(AsynchronousListener.class);
		final HttpAuthenticateRequest<Void> request = this
				.createMock(HttpAuthenticateRequest.class);

		// Record authenticating
		listener.notifyStarted();
		this.authentication.authenticate(request);
		this.control(authentication).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {

				// Ensure always have request (for asynchronous listener)
				HttpAuthenticateRequest<Void> actualRequest = (HttpAuthenticateRequest<Void>) actual[0];
				assertNotNull(
						"Should always have request to wrap asynchronous listener functionality",
						actualRequest);

				// Trigger that completed authentication
				if (isAuthenticatedImmediately) {
					actualRequest.authenticationComplete();
				}

				// As here, matched
				return true;
			}
		});
		if (isAuthenticatedImmediately) {
			this.recordReturn(this.authentication,
					this.authentication.getHttpSecurity(), httpSecurity);
			listener.notifyComplete();
		}

		// Test
		this.replayMockObjects();
		try {

			// Load the source
			ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
			loader.addProperty(
					HttpSecurityManagedObjectSource.PROPERTY_HTTP_SECURITY_TYPE,
					HttpSecurity.class.getName());
			if (isAllowNullHttpSecurity) {
				loader.addProperty(
						HttpSecurityManagedObjectSource.PROPERTY_IS_ESCALATE_AUTHENTICATION_REQUIRED,
						String.valueOf(false));
			}
			HttpSecurityManagedObjectSource source = loader
					.loadManagedObjectSource(HttpSecurityManagedObjectSource.class);

			// Source the managed object
			ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
			user.setAsynchronousListener(listener);
			user.mapDependency(Dependencies.HTTP_AUTHENTICATION,
					this.authentication);
			ManagedObject managedObject = user.sourceManagedObject(source,
					false);

			// Obtain and return the HTTP security
			if (isAuthenticatedImmediately) {
				// Obtain and return the HTTP security
				HttpSecurity security = (HttpSecurity) managedObject
						.getObject();
				return security;

			} else {
				// Still authenticating, so no HTTP security
				return null;
			}

		} catch (Throwable ex) {
			// Verify mock objects
			this.verifyMockObjects();

			// Propagate
			throw ex;
		}
	}

}