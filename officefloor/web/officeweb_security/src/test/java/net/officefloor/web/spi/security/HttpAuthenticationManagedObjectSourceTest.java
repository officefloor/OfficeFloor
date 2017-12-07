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
package net.officefloor.web.spi.security;

import java.io.IOException;

import org.easymock.AbstractMatcher;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.InvokedProcessServicer;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.impl.FunctionAuthenticateContext;
import net.officefloor.web.security.impl.FunctionLogoutContext;
import net.officefloor.web.security.impl.HttpAuthenticationManagedObjectSource;
import net.officefloor.web.security.impl.HttpAuthenticationManagedObjectSource.Dependencies;
import net.officefloor.web.security.impl.HttpAuthenticationManagedObjectSource.Flows;
import net.officefloor.web.security.impl.HttpSecurityConfiguration;
import net.officefloor.web.security.type.HttpSecurityConfigurationImpl;
import net.officefloor.web.session.HttpSession;

/**
 * Tests the {@link HttpAuthenticationManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpSecurity}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Indexed, Indexed> security = this
			.createMock(HttpSecurity.class);

	/**
	 * {@link HttpSecurityConfiguration}.
	 */
	private final HttpSecurityConfiguration<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Indexed, Indexed> securityConfiguration = new HttpSecurityConfigurationImpl<>(
			this.security, null, null);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = MockHttpServer.mockConnection();

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session = this.createMock(HttpSession.class);

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(new HttpAuthenticationManagedObjectSource(this.securityConfiguration));
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpAuthentication.class);
		type.setInput(true);
		type.addDependency(Dependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class, null);
		type.addDependency(Dependencies.HTTP_SESSION, HttpSession.class, null);
		type.addFlow(Flows.AUTHENTICATE, FunctionAuthenticateContext.class);
		type.addFlow(Flows.LOGOUT, FunctionLogoutContext.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				new HttpAuthenticationManagedObjectSource(this.securityConfiguration));
	}

	/**
	 * Ensure can load immediately from {@link HttpSession}.
	 */
	public void testLoad_FromHttpSession() throws Throwable {
		final HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);
		this.loadAuthentication(accessControl, null, true, null, null, false, new SameCheck(accessControl));
	}

	/**
	 * Ensure can load without enough credentials for authentication.
	 */
	public void testLoad_WithoutEnoughForAuthentication() throws Throwable {
		this.loadAuthentication(null, null, false, null, null, true, new NullCheck());
	}

	/**
	 * Ensure propagate failure from ratify exception.
	 */
	public void testLoad_RatifyFailure() throws Throwable {
		RuntimeException failure = new RuntimeException("TEST");
		try {
			this.loadAuthentication(null, failure, false, null, null, false, null);
			fail("Should not be successful");
		} catch (RuntimeException ex) {
			assertSame("Incorrect failure", failure, ex);
		}
	}

	/**
	 * Ensure can load with authentication.
	 */
	public void testLoad_Authenticating() throws Throwable {
		final HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);
		this.loadAuthentication(null, null, true, accessControl, null, false, new SameCheck(accessControl));
	}

	/**
	 * Ensure can load without authenticating.
	 */
	public void testLoad_NotAuthenticating() throws Throwable {
		this.loadAuthentication(null, null, true, null, null, true, new NullCheck());
	}

	/**
	 * Ensure can load providing an authentication {@link IOException}.
	 */
	public void testLoad_WithAuthenticatingIoException() throws Throwable {
		final IOException exception = new IOException("TEST");
		this.loadAuthentication(null, null, true, null, exception, false, (authentication) -> {
			try {
				authentication.getAccessControl();
				fail("Should not be successful");
			} catch (HttpException ex) {
				assertSame("Incorrect exception", exception, ex.getCause());
			}
		});

	}

	/**
	 * <p>
	 * Ensure can load providing an authentication failure.
	 * <p>
	 * Not likely case but API does allow for it, so ensure handle.
	 */
	public void testLoad_WithAuthenticatingCheckedException() throws Throwable {
		final Exception failure = new Exception("TEST");
		this.loadAuthentication(null, null, true, null, failure, false, (authentication) -> {
			try {
				authentication.getAccessControl();
				fail("Should not be successful");
			} catch (IllegalStateException ex) {
				assertEquals("Incorrect exception", "Authentication error: TEST (java.lang.Exception)",
						ex.getMessage());
				assertSame("Incorrect cause", failure, ex.getCause());
			}
		});
	}

	/**
	 * Ensure can load providing an authentication failure.
	 */
	public void testLoad_WithAuthenticatingFailure() throws Throwable {
		final RuntimeException failure = new RuntimeException("TEST");
		this.loadAuthentication(null, null, true, null, failure, false, (authentication) -> {
			try {
				authentication.getAccessControl();
				fail("Should not be successful");
			} catch (RuntimeException ex) {
				assertSame("Incorrect cause", failure, ex);
			}
		});
	}

	/**
	 * Ensure can load providing an authentication error.
	 */
	public void testLoad_WithAuthenticatingError() throws Throwable {
		final Error error = new Error("TEST");
		this.loadAuthentication(null, null, true, null, error, false, (authentication) -> {
			try {
				authentication.getAccessControl();
				fail("Should not be successful");
			} catch (Error ex) {
				assertSame("Incorrect cause", error, ex);
			}
		});
	}

	/**
	 * Ensure can manually obtain from {@link HttpSession}.
	 */
	public void testManual_FromHttpSession() throws Throwable {
		HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);
		this.manualAuthentication(accessControl, null, true, null, null, false, new SameCheck(accessControl));
	}

	/**
	 * Ensure can attempt manual authentication without enough credentials.
	 */
	public void testManual_WithoutEnoughForAuthentication() throws Throwable {
		this.manualAuthentication(null, null, false, null, null, true, new NullCheck());
	}

	/**
	 * Ensure propagate failure from ratify exception.
	 */
	public void testManual_RatifyFailure() throws Throwable {
		RuntimeException failure = new RuntimeException("TEST");
		try {
			this.manualAuthentication(null, failure, false, null, null, false, null);
			fail("Should not be successful");
		} catch (RuntimeException ex) {
			assertSame("Incorrect failure", failure, ex);
		}
	}

	/**
	 * Ensure can manually authentication.
	 */
	public void testManual_Authenticating() throws Throwable {
		final HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);
		this.manualAuthentication(null, null, true, accessControl, null, false, new SameCheck(accessControl));
	}

	/**
	 * Ensure can attempt to manually authenticate.
	 */
	public void testManual_NotAuthenticating() throws Throwable {
		this.manualAuthentication(null, null, true, null, null, true, new NullCheck());
	}

	/**
	 * Ensure can manually attempt authentication with an {@link IOException}.
	 */
	public void testManual_WithAuthenticatingIoException() throws Throwable {
		final IOException exception = new IOException("TEST");
		this.manualAuthentication(null, null, true, null, exception, false, (authentication) -> {
			try {
				authentication.getAccessControl();
				fail("Should not be successful");
			} catch (HttpException ex) {
				assertSame("Incorrect exception", exception, ex.getCause());
			}
		});
	}

	/**
	 * <p>
	 * Ensure can manually attempt authentication with a failure.
	 * <p>
	 * Not likely case but API does allow for it, so ensure handle.
	 */
	public void testManual_WithAuthenticatingCheckedException() throws Throwable {
		final Exception failure = new Exception("TEST");
		this.manualAuthentication(null, null, true, null, failure, false, (authentication) -> {
			try {
				authentication.getAccessControl();
				fail("Should not be successful");
			} catch (IllegalStateException ex) {
				assertEquals("Incorrect exception", "Authentication error: TEST (java.lang.Exception)",
						ex.getMessage());
				assertSame("Incorrect cause", failure, ex.getCause());
			}
		});
	}

	/**
	 * Ensure can manually attempt authentication with a failure.
	 */
	public void testManual_WithAuthenticatingFailure() throws Throwable {
		final RuntimeException failure = new RuntimeException("TEST");
		this.manualAuthentication(null, null, true, null, failure, false, (authentication) -> {
			try {
				authentication.getAccessControl();
				fail("Should not be successful");
			} catch (RuntimeException ex) {
				assertSame("Incorrect cause", failure, ex);
			}
		});
	}

	/**
	 * Ensure can manually attempt authentication with an error.
	 */
	public void testManual_WithAuthenticatingError() throws Throwable {
		final Error error = new Error("TEST");
		this.manualAuthentication(null, null, true, null, error, false, (authentication) -> {
			try {
				authentication.getAccessControl();
				fail("Should not be successful");
			} catch (Error ex) {
				assertSame("Incorrect cause", error, ex);
			}
		});
	}

	/**
	 * Ensure can successfully logout.
	 */
	public void testLogout_Successfully() throws Throwable {
		HttpLogoutRequest request = this.createMock(HttpLogoutRequest.class);
		this.logout(request, null, true);
	}

	/**
	 * Ensure can successfully logout without {@link HttpLogoutRequest}.
	 */
	public void testLogout_SuccessfulWithoutRequest() throws Throwable {
		this.logout(null, null, true);
	}

	/**
	 * Ensure can report failure in logout.
	 */
	public void testLogout_Failure() throws Throwable {
		HttpLogoutRequest request = this.createMock(HttpLogoutRequest.class);
		this.logout(request, new Exception("TEST"), true);
	}

	/**
	 * Ensure can ignore logout failure.
	 */
	public void testLogout_FailureWithoutRequest() throws Throwable {
		this.logout(null, new Exception("TEST"), true);
	}

	/**
	 * Loads authentication.
	 * 
	 * @param ratifiedSecurity
	 *            {@link HttpAccessControl} available from ratify.
	 * @param ratifyFailure
	 *            Failure in ratify.
	 * @param isRatified
	 *            Indicates if ratified.
	 * @param authenticatedSecurity
	 *            {@link HttpAccessControl} from authentication.
	 * @param authenticationFailure
	 *            Failure in authentication.
	 * @param isRatifyOnObtainingHttpSecurity
	 *            Indicates that ratify will be invoked on obtaining the
	 *            {@link HttpAccessControl}.
	 * @param check
	 *            {@link Check}.
	 */
	private void loadAuthentication(HttpAccessControl ratifiedSecurity, RuntimeException ratifyFailure,
			boolean isRatified, HttpAccessControl authenticatedSecurity, Throwable authenticationFailure,
			boolean isRatifyOnObtainingHttpSecurity, Check check) throws Throwable {
		this.doAuthentication(ratifiedSecurity, ratifyFailure, isRatified, authenticatedSecurity, authenticationFailure,
				null, null, null, false, null, null, false, null, null, isRatifyOnObtainingHttpSecurity, check);
	}

	/**
	 * Manually undertakes authentication.
	 * 
	 * @param ratifiedSecurity
	 *            {@link HttpAccessControl} available from ratify.
	 * @param ratifyFailure
	 *            Failure in ratify.
	 * @param isRatified
	 *            Indicates if ratified.
	 * @param authenticatedSecurity
	 *            {@link HttpAccessControl} from authentication.
	 * @param authenticationFailure
	 *            Failure in authentication.
	 * @param isRatifyOnObtainingHttpSecurity
	 *            Indicates that ratify will be invoked on obtaining the
	 *            {@link HttpAccessControl}.
	 * @param check
	 *            {@link Check}.
	 */
	private void manualAuthentication(HttpAccessControl ratifiedSecurity, RuntimeException ratifiedFailure,
			boolean isRatified, HttpAccessControl authenticatedSecurity, Throwable authenticatedFailure,
			boolean isRatifyOnObtainingHttpSecurity, Check check) throws Throwable {
		HttpCredentials credentials = this.createMock(HttpCredentials.class);
		this.doAuthentication(null, null, false, null, null, credentials, ratifiedSecurity, ratifiedFailure, isRatified,
				authenticatedSecurity, authenticatedFailure, false, null, null, isRatifyOnObtainingHttpSecurity, check);
	}

	/**
	 * Undertakes logout.
	 * 
	 * @param logoutRequest
	 *            {@link HttpLogoutRequest}.
	 * @param logoutFailure
	 *            Failure in logging out.
	 * @param isRatifyOnObtainingHttpSecurity
	 *            Indicates that ratify will be invoked on obtaining the
	 *            {@link HttpAccessControl}.
	 */
	private void logout(HttpLogoutRequest logoutRequest, Throwable logoutFailure,
			boolean isRatifyOnObtainingHttpSecurity) throws Throwable {
		HttpAccessControl httpSecurity = this.createMock(HttpAccessControl.class);
		this.doAuthentication(httpSecurity, null, true, null, null, null, null, null, true, null, null, true,
				logoutRequest, logoutFailure, isRatifyOnObtainingHttpSecurity, null);
	}

	/**
	 * Enables undertaking a check before verifying the mock objects.
	 */
	private interface Check {

		/**
		 * Implement to undertake a check before verifying the mock objects.
		 * 
		 * @param authentication
		 *            {@link HttpAuthentication}.
		 */
		void check(HttpAuthentication<HttpAccessControl> authentication) throws Throwable;
	}

	/**
	 * Ensures the access control is the same.
	 */
	private class SameCheck implements Check {

		/**
		 * {@link HttpAccessControl}.
		 */
		private final HttpAccessControl accessControl;

		/**
		 * Initiate.
		 * 
		 * @param accessControl
		 *            Access control.
		 */
		public SameCheck(HttpAccessControl accessControl) {
			this.accessControl = accessControl;
		}

		/*
		 * =============== Check =================
		 */

		@Override
		public void check(HttpAuthentication<HttpAccessControl> authentication) throws Throwable {
			assertSame("Incorrect access control", this.accessControl, authentication.getAccessControl());
		}
	}

	/**
	 * Ensures the access control is <code>null</code>.
	 */
	private class NullCheck implements Check {

		@Override
		public void check(HttpAuthentication<HttpAccessControl> authentication) throws Throwable {
			assertNull("Should not load access control", authentication.getAccessControl());
		}
	}

	/**
	 * Undertakes the authentication.
	 */
	@SuppressWarnings("unchecked")
	private void doAuthentication(final HttpAccessControl loadRatifiedSecurity,
			final RuntimeException loadRatifyFailure, boolean isLoadRatified,
			final HttpAccessControl loadAuthenticatedSecurity, final Throwable loadAuthenticationFailure,
			final HttpCredentials credentials, final HttpAccessControl manualRatifiedSecurity,
			final RuntimeException manualRatifyFailure, boolean isManuallyRatified,
			final HttpAccessControl manualSecurity, final Throwable manualFailure, boolean isLogout,
			HttpLogoutRequest logoutRequest, final Throwable logoutFailure, boolean isRatifyOnObtainingHttpSecurity,
			Check check) throws Throwable {

		final AsynchronousContext async = this.createMock(AsynchronousContext.class);

		final ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();

		// Authentication callback (if manually authenticating)
		HttpAuthenticateCallback authenticationCallback = null;

		// Record ratifying ability to authenticate
		final HttpRatifyContext<HttpAccessControl> loadRatifyContext = this.createMock(HttpRatifyContext.class);
		final HttpRatifyContext<HttpAccessControl> manualRatifyContext = this.createMock(HttpRatifyContext.class);
		final HttpRatifyContext<HttpAccessControl> obtainRatifyContext = this.createMock(HttpRatifyContext.class);
		AbstractMatcher ratifyMatcher = new AbstractMatcher() {

			/**
			 * Mock checks run multiple times so this flag allows only one
			 * specifying of HTTP Access Control on load.
			 */
			private boolean isRatifyLoad = true;

			/**
			 * Mock checks run multiple times so this flag allows only one
			 * specifying of HTTP Security on manual authenticate.
			 */
			private boolean isRatifyManual = true;

			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				HttpCredentials credentialsArgument = (HttpCredentials) actual[0];
				HttpRatifyContext<HttpAccessControl> contextArgument = (HttpRatifyContext<HttpAccessControl>) actual[1];
				assertSame("Incorrect connection", HttpAuthenticationManagedObjectSourceTest.this.connection,
						contextArgument.getConnection());
				assertSame("Incorrect session", HttpAuthenticationManagedObjectSourceTest.this.session,
						contextArgument.getSession());
				HttpRatifyContext<HttpAccessControl> expectedContext = (HttpRatifyContext<HttpAccessControl>) expected[0];
				if (expectedContext == loadRatifyContext) {
					// Validate loading ratification
					if (credentials != null) {
						// Should be no credentials on load
						return false;
					}

					// Only take action once
					if ((this.isRatifyLoad) && (loadRatifiedSecurity != null)) {
						contextArgument.setAccessControl(loadRatifiedSecurity);
						this.isRatifyLoad = false;
					}

				} else if (expectedContext == manualRatifyContext) {
					// Validate manual ratification
					if (credentialsArgument != credentials) {
						// Incorrect credentials
						return false;
					}

					// Only take action once
					if ((this.isRatifyManual) && (manualRatifiedSecurity != null)) {
						contextArgument.setAccessControl(manualRatifiedSecurity);
						this.isRatifyManual = false;
					}

				} else if (expectedContext == obtainRatifyContext) {
					// Validate loading ratification
					if (credentials != null) {
						// No credentials on obtaining HTTP Security
						return false;
					}

				} else {
					fail("Unkonwn expected ratify context");
				}
				return true;
			}
		};

		// Undertake load ratify
		this.security.ratify(null, loadRatifyContext);
		this.control(this.security).setDefaultMatcher(ratifyMatcher);
		if (loadRatifyFailure != null) {
			// Failure on ratify (no further authentication)
			this.control(this.security).setThrowable(loadRatifyFailure);

		} else {
			// Load ratify successful returns
			this.control(this.security).setReturnValue(isLoadRatified);

			// Trigger authentication if ratified (but no HTTP security)
			if (isLoadRatified && (loadRatifiedSecurity == null)) {

				// Start authentication
				async.start(null);

				// Confirm trigger authentication
				InvokedProcessServicer loadAuthenticateServicer = new InvokedProcessServicer() {
					@Override
					public void service(int processIndex, Object parameter, ManagedObject managedObject)
							throws Throwable {

						// Ensure context is correct
						FunctionAuthenticateContext<HttpAccessControl, HttpCredentials> context = (FunctionAuthenticateContext<HttpAccessControl, HttpCredentials>) parameter;
						assertSame("Incorrect connection", HttpAuthenticationManagedObjectSourceTest.this.connection,
								context.getConnection());
						assertSame("Incorrect session", HttpAuthenticationManagedObjectSourceTest.this.session,
								context.getSession());
						assertNull("Should be no credentials on loading", context.getCredentials());

						// Determine if authentication failure
						if (loadAuthenticationFailure != null) {
							context.setFailure(loadAuthenticationFailure);

						} else {
							// Provide the authenticated security
							context.setAccessControl(loadAuthenticatedSecurity);
						}
					}
				};
				loader.registerInvokeProcessServicer(Flows.AUTHENTICATE, loadAuthenticateServicer);

				// Authentication completing
				async.complete(null);
			}

			// Determine if trigger authentication manually
			if (credentials != null) {

				// Record manual authentication
				authenticationCallback = this.createMock(HttpAuthenticateCallback.class);

				// Record ratifying (matching logic is above)
				this.security.ratify(credentials, manualRatifyContext);
				if (manualRatifyFailure != null) {
					this.control(this.security).setThrowable(manualRatifyFailure);

					// Authentication completed immediately
					authenticationCallback.authenticationComplete();

				} else {
					// Manual ratify successful returns
					this.control(this.security).setReturnValue(isManuallyRatified);

					// Determine if authentication
					boolean isUndertakeManualAuthentication = isManuallyRatified && (manualRatifiedSecurity == null);
					if (!isUndertakeManualAuthentication) {
						// Not undertaking authentication, flag request complete
						authenticationCallback.authenticationComplete();

					} else {
						// Trigger authentication
						async.start(null);

						// Confirm trigger authentication
						InvokedProcessServicer manualAuthenticateServicer = new InvokedProcessServicer() {
							@Override
							public void service(int processIndex, Object parameter, ManagedObject managedObject)
									throws Throwable {

								// Ensure context is correct
								FunctionAuthenticateContext<HttpAccessControl, HttpCredentials> context = (FunctionAuthenticateContext<HttpAccessControl, HttpCredentials>) parameter;
								assertSame("Incorrect connection",
										HttpAuthenticationManagedObjectSourceTest.this.connection,
										context.getConnection());
								assertSame("Incorrect session", HttpAuthenticationManagedObjectSourceTest.this.session,
										context.getSession());
								assertSame("Incorrect credentials", credentials, context.getCredentials());

								// Determine if manual failure
								if (manualFailure != null) {
									context.setFailure(manualFailure);

								} else {
									// Provide the authenticated security
									context.setAccessControl(manualSecurity);
								}
							}
						};
						loader.registerInvokeProcessServicer(Flows.AUTHENTICATE, manualAuthenticateServicer);

						// Authentication completing
						async.complete(null);
						authenticationCallback.authenticationComplete();
					}
				}
			}

			// Determine if logout
			if (isLogout) {

				// Record logout (if provided request)
				if (logoutRequest != null) {
					logoutRequest.logoutComplete(logoutFailure);
				}

				// Provide servicer to handle logout
				final HttpLogoutRequest expectedLogoutRequest = logoutRequest;
				InvokedProcessServicer logoutServicer = new InvokedProcessServicer() {
					@Override
					public void service(int processIndex, Object parameter, ManagedObject managedObject)
							throws Throwable {

						// Ensure correct parameter
						FunctionLogoutContext context = (FunctionLogoutContext) parameter;
						assertSame("Incorrect connection", HttpAuthenticationManagedObjectSourceTest.this.connection,
								context.getConnection());
						assertSame("Incorrect session", HttpAuthenticationManagedObjectSourceTest.this.session,
								context.getSession());
						HttpLogoutRequest actualLogoutRequest = context.getHttpLogoutRequest();
						assertSame("Incorrect logout request", expectedLogoutRequest, actualLogoutRequest);

						// Flag logout complete (if provided logout request)
						if (actualLogoutRequest != null) {
							actualLogoutRequest.logoutComplete(logoutFailure);
						}
					}
				};
				loader.registerInvokeProcessServicer(Flows.LOGOUT, logoutServicer);
			}

			// Determine if ratify on obtaining HTTP Security
			if (isRatifyOnObtainingHttpSecurity) {
				this.recordReturn(this.security, this.security.ratify(null, obtainRatifyContext), false);
			}
		}

		// Test
		this.replayMockObjects();
		try {

			// Load the source
			HttpAuthenticationManagedObjectSource source = new HttpAuthenticationManagedObjectSource(null);

			// Source the managed object
			ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
			user.setAsynchronousListener(async);
			user.mapDependency(Dependencies.SERVER_HTTP_CONNECTION,
					HttpAuthenticationManagedObjectSourceTest.this.connection);
			user.mapDependency(Dependencies.HTTP_SESSION, HttpAuthenticationManagedObjectSourceTest.this.session);
			ManagedObject managedObject = user.sourceManagedObject(source);

			// Obtain the HTTP authentication
			HttpAuthentication<HttpAccessControl> authentication = (HttpAuthentication<HttpAccessControl>) managedObject
					.getObject();

			// Determine if undertake manual authentication
			if (authenticationCallback != null) {
				authentication.authenticate(null, authenticationCallback);
			}

			// Determine if undertake logout
			if (isLogout) {

				// Ensure logged in
				assertNotNull("Should be logged in", authentication.getAccessControl());

				// Log out
				authentication.logout(logoutRequest);
				assertNull("Should be logged out", authentication.getAccessControl());
			}

			// Determine if check
			if (check != null) {
				check.check(authentication);
			}

		} finally {
			// Verify mock objects
			this.verifyMockObjects();
		}
	}

}