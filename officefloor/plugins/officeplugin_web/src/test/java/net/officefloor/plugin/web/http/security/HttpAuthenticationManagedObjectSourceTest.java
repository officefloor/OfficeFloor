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
import net.officefloor.plugin.web.http.security.HttpAuthenticationManagedObjectSource.Dependencies;
import net.officefloor.plugin.web.http.security.HttpAuthenticationManagedObjectSource.Flows;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Tests the {@link HttpAuthenticationManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpSecuritySource}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpSecuritySource<HttpSecurity, HttpCredentials, Indexed, Indexed> source = this
			.createMock(HttpSecuritySource.class);

	/**
	 * {@link HttpSecurityType}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpSecurityType<HttpSecurity, HttpCredentials, Indexed, Indexed> securityType = this
			.createMock(HttpSecurityType.class);

	/**
	 * {@link HttpSecurityConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpSecurityConfiguration<HttpSecurity, HttpCredentials, Indexed, Indexed> securityConfiguration = this
			.createMock(HttpSecurityConfiguration.class);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);

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
		final HttpSecurity security = this.createMock(HttpSecurity.class);
		this.loadAuthentication(security, null, true, null, null, false, new SameCheck(security));
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
		final HttpSecurity security = this.createMock(HttpSecurity.class);
		this.loadAuthentication(null, null, true, security, null, false, new SameCheck(security));
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
		this.loadAuthentication(null, null, true, null, exception, false, new Check() {
			@Override
			public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
				try {
					authentication.getHttpSecurity();
					fail("Should not be successful");
				} catch (IOException ex) {
					assertSame("Incorrect exception", exception, ex);
				}
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
		this.loadAuthentication(null, null, true, null, failure, false, new Check() {
			@Override
			public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
				try {
					authentication.getHttpSecurity();
					fail("Should not be successful");
				} catch (IllegalStateException ex) {
					assertEquals("Incorrect exception", "Authentication error: TEST (java.lang.Exception)",
							ex.getMessage());
					assertSame("Incorrect cause", failure, ex.getCause());
				}
			}
		});
	}

	/**
	 * Ensure can load providing an authentication failure.
	 */
	public void testLoad_WithAuthenticatingFailure() throws Throwable {
		final RuntimeException failure = new RuntimeException("TEST");
		this.loadAuthentication(null, null, true, null, failure, false, new Check() {
			@Override
			public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
				try {
					authentication.getHttpSecurity();
					fail("Should not be successful");
				} catch (RuntimeException ex) {
					assertSame("Incorrect cause", failure, ex);
				}
			}
		});
	}

	/**
	 * Ensure can load providing an authentication error.
	 */
	public void testLoad_WithAuthenticatingError() throws Throwable {
		final Error error = new Error("TEST");
		this.loadAuthentication(null, null, true, null, error, false, new Check() {
			@Override
			public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
				try {
					authentication.getHttpSecurity();
					fail("Should not be successful");
				} catch (Error ex) {
					assertSame("Incorrect cause", error, ex);
				}
			}
		});
	}

	/**
	 * Ensure can manually obtain from {@link HttpSession}.
	 */
	public void testManual_FromHttpSession() throws Throwable {
		HttpSecurity security = this.createMock(HttpSecurity.class);
		this.manualAuthentication(security, null, true, null, null, false, new SameCheck(security));
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
		final HttpSecurity security = this.createMock(HttpSecurity.class);
		this.manualAuthentication(null, null, true, security, null, false, new SameCheck(security));
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
		this.manualAuthentication(null, null, true, null, exception, false, new Check() {
			@Override
			public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
				try {
					authentication.getHttpSecurity();
					fail("Should not be successful");
				} catch (IOException ex) {
					assertSame("Incorrect exception", exception, ex);
				}
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
		this.manualAuthentication(null, null, true, null, failure, false, new Check() {
			@Override
			public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
				try {
					authentication.getHttpSecurity();
					fail("Should not be successful");
				} catch (IllegalStateException ex) {
					assertEquals("Incorrect exception", "Authentication error: TEST (java.lang.Exception)",
							ex.getMessage());
					assertSame("Incorrect cause", failure, ex.getCause());
				}
			}
		});
	}

	/**
	 * Ensure can manually attempt authentication with a failure.
	 */
	public void testManual_WithAuthenticatingFailure() throws Throwable {
		final RuntimeException failure = new RuntimeException("TEST");
		this.manualAuthentication(null, null, true, null, failure, false, new Check() {
			@Override
			public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
				try {
					authentication.getHttpSecurity();
					fail("Should not be successful");
				} catch (RuntimeException ex) {
					assertSame("Incorrect cause", failure, ex);
				}
			}
		});
	}

	/**
	 * Ensure can manually attempt authentication with an error.
	 */
	public void testManual_WithAuthenticatingError() throws Throwable {
		final Error error = new Error("TEST");
		this.manualAuthentication(null, null, true, null, error, false, new Check() {
			@Override
			public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
				try {
					authentication.getHttpSecurity();
					fail("Should not be successful");
				} catch (Error ex) {
					assertSame("Incorrect cause", error, ex);
				}
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
	 *            {@link HttpSecurity} available from ratify.
	 * @param ratifyFailure
	 *            Failure in ratify.
	 * @param isRatified
	 *            Indicates if ratified.
	 * @param authenticatedSecurity
	 *            {@link HttpSecurity} from authentication.
	 * @param authenticationFailure
	 *            Failure in authentication.
	 * @param isRatifyOnObtainingHttpSecurity
	 *            Indicates that ratify will be invoked on obtaining the
	 *            {@link HttpSecurity}.
	 * @param check
	 *            {@link Check}.
	 */
	private void loadAuthentication(HttpSecurity ratifiedSecurity, RuntimeException ratifyFailure, boolean isRatified,
			HttpSecurity authenticatedSecurity, Throwable authenticationFailure,
			boolean isRatifyOnObtainingHttpSecurity, Check check) throws Throwable {
		this.doAuthentication(ratifiedSecurity, ratifyFailure, isRatified, authenticatedSecurity, authenticationFailure,
				null, null, null, false, null, null, false, null, null, isRatifyOnObtainingHttpSecurity, check);
	}

	/**
	 * Manually undertakes authentication.
	 * 
	 * @param ratifiedSecurity
	 *            {@link HttpSecurity} available from ratify.
	 * @param ratifyFailure
	 *            Failure in ratify.
	 * @param isRatified
	 *            Indicates if ratified.
	 * @param authenticatedSecurity
	 *            {@link HttpSecurity} from authentication.
	 * @param authenticationFailure
	 *            Failure in authentication.
	 * @param isRatifyOnObtainingHttpSecurity
	 *            Indicates that ratify will be invoked on obtaining the
	 *            {@link HttpSecurity}.
	 * @param check
	 *            {@link Check}.
	 */
	private void manualAuthentication(HttpSecurity ratifiedSecurity, RuntimeException ratifiedFailure,
			boolean isRatified, HttpSecurity authenticatedSecurity, Throwable authenticatedFailure,
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
	 *            {@link HttpSecurity}.
	 */
	private void logout(HttpLogoutRequest logoutRequest, Throwable logoutFailure,
			boolean isRatifyOnObtainingHttpSecurity) throws Throwable {
		HttpSecurity httpSecurity = this.createMock(HttpSecurity.class);
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
		void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable;
	}

	/**
	 * Ensures the HTTP Security is the same.
	 */
	private class SameCheck implements Check {

		/**
		 * {@link HttpSecurity}.
		 */
		private final HttpSecurity security;

		/**
		 * Initiate.
		 * 
		 * @param security
		 */
		public SameCheck(HttpSecurity security) {
			this.security = security;
		}

		/*
		 * =============== Check =================
		 */

		@Override
		public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
			assertSame("Incorrect HTTP security", this.security, authentication.getHttpSecurity());
		}
	}

	/**
	 * Ensures the HTTP Security is <code>null</code>.
	 */
	private class NullCheck implements Check {

		@Override
		public void check(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) throws Throwable {
			assertNull("Should not load HTTP security", authentication.getHttpSecurity());
		}
	}

	/**
	 * Undertakes the authentication.
	 */
	@SuppressWarnings("unchecked")
	private void doAuthentication(final HttpSecurity loadRatifiedSecurity, final RuntimeException loadRatifyFailure,
			boolean isLoadRatified, final HttpSecurity loadAuthenticatedSecurity,
			final Throwable loadAuthenticationFailure, final HttpCredentials credentials,
			final HttpSecurity manualRatifiedSecurity, final RuntimeException manualRatifyFailure,
			boolean isManuallyRatified, final HttpSecurity manualSecurity, final Throwable manualFailure,
			boolean isLogout, HttpLogoutRequest logoutRequest, final Throwable logoutFailure,
			boolean isRatifyOnObtainingHttpSecurity, Check check) throws Throwable {

		final AsynchronousContext async = this.createMock(AsynchronousContext.class);

		final ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();

		// Authentication request (if manually authenticating)
		HttpAuthenticateRequest<HttpCredentials> authenticationRequest = null;

		// Record ratifying ability to authenticate
		final HttpRatifyContext<HttpSecurity, HttpCredentials> loadRatifyContext = this
				.createMock(HttpRatifyContext.class);
		final HttpRatifyContext<HttpSecurity, HttpCredentials> manualRatifyContext = this
				.createMock(HttpRatifyContext.class);
		final HttpRatifyContext<HttpSecurity, HttpCredentials> obtainRatifyContext = this
				.createMock(HttpRatifyContext.class);
		AbstractMatcher ratifyMatcher = new AbstractMatcher() {

			/**
			 * Mock checks run multiple times so this flag allows only one
			 * specifying of HTTP Security on load.
			 */
			private boolean isRatifyLoad = true;

			/**
			 * Mock checks run multiple times so this flag allows only one
			 * specifying of HTTP Security on manual authenticate.
			 */
			private boolean isRatifyManual = true;

			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				HttpRatifyContext<HttpSecurity, HttpCredentials> context = (HttpRatifyContext<HttpSecurity, HttpCredentials>) actual[0];
				assertSame("Incorrect connection", HttpAuthenticationManagedObjectSourceTest.this.connection,
						context.getConnection());
				assertSame("Incorrect session", HttpAuthenticationManagedObjectSourceTest.this.session,
						context.getSession());
				HttpRatifyContext<HttpSecurity, HttpCredentials> expectedContext = (HttpRatifyContext<HttpSecurity, HttpCredentials>) expected[0];
				if (expectedContext == loadRatifyContext) {
					// Validate loading ratification
					if (context.getCredentials() != null) {
						// Should be no credentials on load
						return false;
					}

					// Only take action once
					if ((this.isRatifyLoad) && (loadRatifiedSecurity != null)) {
						context.setHttpSecurity(loadRatifiedSecurity);
						this.isRatifyLoad = false;
					}

				} else if (expectedContext == manualRatifyContext) {
					// Validate manual ratification
					if (context.getCredentials() != credentials) {
						// Incorrect credentials
						return false;
					}

					// Only take action once
					if ((this.isRatifyManual) && (manualRatifiedSecurity != null)) {
						context.setHttpSecurity(manualRatifiedSecurity);
						this.isRatifyManual = false;
					}

				} else if (expectedContext == obtainRatifyContext) {
					// Validate loading ratification
					if (context.getCredentials() != null) {
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
		this.source.ratify(loadRatifyContext);
		this.control(this.source).setDefaultMatcher(ratifyMatcher);
		if (loadRatifyFailure != null) {
			// Failure on ratify (no further authentication)
			this.control(this.source).setThrowable(loadRatifyFailure);

		} else {
			// Load ratify successful returns
			this.control(this.source).setReturnValue(isLoadRatified);

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
						FunctionAuthenticateContext<HttpSecurity, HttpCredentials> context = (FunctionAuthenticateContext<HttpSecurity, HttpCredentials>) parameter;
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
							context.setHttpSecurity(loadAuthenticatedSecurity);
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
				authenticationRequest = this.createMock(HttpAuthenticateRequest.class);
				this.recordReturn(authenticationRequest, authenticationRequest.getCredentials(), credentials);

				// Record ratifying (matching logic is above)
				this.source.ratify(manualRatifyContext);
				if (manualRatifyFailure != null) {
					this.control(this.source).setThrowable(manualRatifyFailure);

					// Authentication completed immediately
					authenticationRequest.authenticationComplete();

				} else {
					// Manual ratify successful returns
					this.control(this.source).setReturnValue(isManuallyRatified);

					// Determine if authentication
					boolean isUndertakeManualAuthentication = isManuallyRatified && (manualRatifiedSecurity == null);
					if (!isUndertakeManualAuthentication) {
						// Not undertaking authentication, flag request complete
						authenticationRequest.authenticationComplete();

					} else {
						// Trigger authentication
						async.start(null);

						// Confirm trigger authentication
						InvokedProcessServicer manualAuthenticateServicer = new InvokedProcessServicer() {
							@Override
							public void service(int processIndex, Object parameter, ManagedObject managedObject)
									throws Throwable {

								// Ensure context is correct
								FunctionAuthenticateContext<HttpSecurity, HttpCredentials> context = (FunctionAuthenticateContext<HttpSecurity, HttpCredentials>) parameter;
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
									context.setHttpSecurity(manualSecurity);
								}
							}
						};
						loader.registerInvokeProcessServicer(Flows.AUTHENTICATE, manualAuthenticateServicer);

						// Authentication completing
						async.complete(null);
						authenticationRequest.authenticationComplete();
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
				this.recordReturn(this.source, this.source.ratify(obtainRatifyContext), false);
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
			HttpAuthentication<HttpSecurity, HttpCredentials> authentication = (HttpAuthentication<HttpSecurity, HttpCredentials>) managedObject
					.getObject();

			// Determine if undertake manual authentication
			if (authenticationRequest != null) {
				authentication.authenticate(authenticationRequest);
			}

			// Determine if undertake logout
			if (isLogout) {

				// Ensure logged in
				assertNotNull("Should be logged in", authentication.getHttpSecurity());

				// Log out
				authentication.logout(logoutRequest);
				assertNull("Should be logged out", authentication.getHttpSecurity());
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