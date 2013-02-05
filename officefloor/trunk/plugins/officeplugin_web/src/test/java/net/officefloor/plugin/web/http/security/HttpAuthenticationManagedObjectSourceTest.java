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

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.InvokedProcessServicer;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.security.HttpAuthenticationManagedObjectSource.Dependencies;
import net.officefloor.plugin.web.http.security.HttpAuthenticationManagedObjectSource.Flows;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link HttpAuthenticationManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationManagedObjectSourceTest extends
		OfficeFrameTestCase {

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
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session = this.createMock(HttpSession.class);

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(
						HttpAuthenticationManagedObjectSource.class,
						HttpAuthenticationManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY,
						"HTTP Security Source Key");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Register HTTP Security Source
		String key = HttpSecurityConfigurator.registerHttpSecuritySource(
				this.source, this.securityType);

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpAuthentication.class);
		type.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class, null);
		type.addDependency(Dependencies.HTTP_SESSION, HttpSession.class, null);
		type.addFlow(Flows.AUTHENTICATE, TaskAuthenticateContext.class, null,
				null);

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						HttpAuthenticationManagedObjectSource.class,
						HttpAuthenticationManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY,
						key);
	}

	/**
	 * Ensure can load immediately from {@link HttpSession}.
	 */
	public void testLoad_FromHttpSession() throws Throwable {
		final HttpSecurity security = this.createMock(HttpSecurity.class);
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.loadAuthentication(security, null, true, null, null);
		assertEquals("Incorrect HTTP security", security,
				authentication.getHttpSecurity());
	}

	/**
	 * Ensure can load without enough credentials for authentication.
	 */
	public void testLoad_WithoutEnoughForAuthentication() throws Throwable {
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.loadAuthentication(null, null, false, null, null);
		assertNull("Should not load HTTP security",
				authentication.getHttpSecurity());
	}

	/**
	 * Ensure propagate failure from ratify exception.
	 */
	public void testLoad_RatifyFailure() throws Throwable {
		RuntimeException failure = new RuntimeException("TEST");
		try {
			this.loadAuthentication(null, failure, false, null, null);
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
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.loadAuthentication(null, null, true, security, null);
		assertSame("Incorrect HTTP security", security,
				authentication.getHttpSecurity());
	}

	/**
	 * Ensure can load without authenticating.
	 */
	public void testLoad_NotAuthenticating() throws Throwable {
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.loadAuthentication(null, null, true, null, null);
		assertNull("Should not load HTTP security",
				authentication.getHttpSecurity());
	}

	/**
	 * Ensure can load providing an authentication {@link IOException}.
	 */
	public void testLoad_WithAuthenticatingIoException() throws Throwable {
		IOException exception = new IOException("TEST");
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.loadAuthentication(null, null, true, null, exception);
		try {
			authentication.getHttpSecurity();
			fail("Should not be successful");
		} catch (IOException ex) {
			assertSame("Incorrect exception", exception, ex);
		}
	}

	/**
	 * <p>
	 * Ensure can load providing an authentication failure.
	 * <p>
	 * Not likely case but API does allow for it, so ensure handle.
	 */
	public void testLoad_WithAuthenticatingCheckedException() throws Throwable {
		Exception failure = new Exception("TEST");
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.loadAuthentication(null, null, true, null, failure);
		try {
			authentication.getHttpSecurity();
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect exception",
					"Authentication error: TEST (java.lang.Exception)",
					ex.getMessage());
			assertSame("Incorrect cause", failure, ex.getCause());
		}
	}

	/**
	 * Ensure can load providing an authentication failure.
	 */
	public void testLoad_WithAuthenticatingFailure() throws Throwable {
		RuntimeException failure = new RuntimeException("TEST");
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.loadAuthentication(null, null, true, null, failure);
		try {
			authentication.getHttpSecurity();
			fail("Should not be successful");
		} catch (RuntimeException ex) {
			assertSame("Incorrect cause", failure, ex);
		}
	}

	/**
	 * Ensure can load providing an authentication error.
	 */
	public void testLoad_WithAuthenticatingError() throws Throwable {
		Error error = new Error("TEST");
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.loadAuthentication(null, null, true, null, error);
		try {
			authentication.getHttpSecurity();
			fail("Should not be successful");
		} catch (Error ex) {
			assertSame("Incorrect cause", error, ex);
		}
	}

	/**
	 * Ensure can manually obtain from {@link HttpSession}.
	 */
	public void testManual_FromHttpSession() throws Throwable {
		HttpSecurity security = this.createMock(HttpSecurity.class);
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.manualAuthentication(security, null, true, null, null);
		assertSame("Incorrect security", security,
				authentication.getHttpSecurity());
	}

	/**
	 * Ensure can attempt manual authentication without enough credentials.
	 */
	public void testManual_WithoutEnoughForAuthentication() throws Throwable {
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.manualAuthentication(null, null, false, null, null);
		assertNull("Should not load HTTP security",
				authentication.getHttpSecurity());
	}

	/**
	 * Ensure propagate failure from ratify exception.
	 */
	public void testManual_RatifyFailure() throws Throwable {
		RuntimeException failure = new RuntimeException("TEST");
		try {
			this.manualAuthentication(null, failure, false, null, null);
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
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.manualAuthentication(null, null, true, security, null);
		assertSame("Incorrect HTTP security", security,
				authentication.getHttpSecurity());
	}

	/**
	 * Ensure can attempt to manually authenticate.
	 */
	public void testManual_NotAuthenticating() throws Throwable {
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.manualAuthentication(null, null, true, null, null);
		assertNull("Should not load HTTP security",
				authentication.getHttpSecurity());
	}

	/**
	 * Ensure can manually attempt authentication with an {@link IOException}.
	 */
	public void testManual_WithAuthenticatingIoException() throws Throwable {
		IOException exception = new IOException("TEST");
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.manualAuthentication(null, null, true, null, exception);
		try {
			authentication.getHttpSecurity();
			fail("Should not be successful");
		} catch (IOException ex) {
			assertSame("Incorrect exception", exception, ex);
		}
	}

	/**
	 * <p>
	 * Ensure can manually attempt authentication with a failure.
	 * <p>
	 * Not likely case but API does allow for it, so ensure handle.
	 */
	public void testManual_WithAuthenticatingCheckedException()
			throws Throwable {
		Exception failure = new Exception("TEST");
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.manualAuthentication(null, null, true, null, failure);
		try {
			authentication.getHttpSecurity();
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect exception",
					"Authentication error: TEST (java.lang.Exception)",
					ex.getMessage());
			assertSame("Incorrect cause", failure, ex.getCause());
		}
	}

	/**
	 * Ensure can manually attempt authentication with a failure.
	 */
	public void testManual_WithAuthenticatingFailure() throws Throwable {
		RuntimeException failure = new RuntimeException("TEST");
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.manualAuthentication(null, null, true, null, failure);
		try {
			authentication.getHttpSecurity();
			fail("Should not be successful");
		} catch (RuntimeException ex) {
			assertSame("Incorrect cause", failure, ex);
		}
	}

	/**
	 * Ensure can manually attempt authentication with an error.
	 */
	public void testManual_WithAuthenticatingError() throws Throwable {
		Error error = new Error("TEST");
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
				.manualAuthentication(null, null, true, null, error);
		try {
			authentication.getHttpSecurity();
			fail("Should not be successful");
		} catch (Error ex) {
			assertSame("Incorrect cause", error, ex);
		}
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
	 * @return {@link HttpAuthentication}.
	 */
	private HttpAuthentication<HttpSecurity, HttpCredentials> loadAuthentication(
			HttpSecurity ratifiedSecurity, RuntimeException ratifyFailure,
			boolean isRatified, HttpSecurity authenticatedSecurity,
			Throwable authenticationFailure) throws Throwable {
		return this.doAuthentication(ratifiedSecurity, ratifyFailure,
				isRatified, authenticatedSecurity, authenticationFailure, null,
				null, null, false, null, null);
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
	 * @return {@link HttpAuthentication}.
	 */
	private HttpAuthentication<HttpSecurity, HttpCredentials> manualAuthentication(
			HttpSecurity ratifiedSecurity, RuntimeException ratifiedFailure,
			boolean isRatified, HttpSecurity authenticatedSecurity,
			Throwable authenticatedFailure) throws Throwable {
		HttpCredentials credentials = this.createMock(HttpCredentials.class);
		return this.doAuthentication(null, null, false, null, null,
				credentials, ratifiedSecurity, ratifiedFailure, isRatified,
				authenticatedSecurity, authenticatedFailure);
	}

	/**
	 * Undertakes the authentication.
	 */
	@SuppressWarnings("unchecked")
	private HttpAuthentication<HttpSecurity, HttpCredentials> doAuthentication(
			final HttpSecurity loadRatifiedSecurity,
			final RuntimeException loadRatifyFailure, boolean isLoadRatified,
			final HttpSecurity loadAuthenticatedSecurity,
			final Throwable loadAuthenticationFailure,
			final HttpCredentials credentials,
			final HttpSecurity manualRatifiedSecurity,
			final RuntimeException manualRatifyFailure,
			boolean isManuallyRatified, final HttpSecurity manualSecurity,
			final Throwable manualFailure) throws Throwable {

		final AsynchronousListener listener = this
				.createMock(AsynchronousListener.class);

		final ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();

		// Record ratifying ability to authenticate
		final HttpRatifyContext<HttpSecurity, HttpCredentials> loadRatifyContext = this
				.createMock(HttpRatifyContext.class);
		final HttpRatifyContext<HttpSecurity, HttpCredentials> manualRatifyContext = this
				.createMock(HttpRatifyContext.class);
		this.recordReturn(this.source, this.source.ratify(loadRatifyContext),
				isLoadRatified, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						HttpRatifyContext<HttpSecurity, HttpCredentials> context = (HttpRatifyContext<HttpSecurity, HttpCredentials>) actual[0];
						assertSame(
								"Incorrect connection",
								HttpAuthenticationManagedObjectSourceTest.this.connection,
								context.getConnection());
						assertSame(
								"Incorrect session",
								HttpAuthenticationManagedObjectSourceTest.this.session,
								context.getSession());
						if (expected[0] == loadRatifyContext) {
							// Validate loading ratification
							if (context.getCredentials() != null) {
								// Should be no credentials on load
								return false;
							}
							if (loadRatifyFailure != null) {
								throw loadRatifyFailure;
							}
							if (loadRatifiedSecurity != null) {
								context.setHttpSecurity(loadRatifiedSecurity);
							}
						} else {
							// Validate manual ratification
							if (context.getCredentials() != credentials) {
								// Incorrect credentials
								return false;
							}
							if (manualRatifyFailure != null) {
								throw manualRatifyFailure;
							}
							if (manualRatifiedSecurity != null) {
								context.setHttpSecurity(manualRatifiedSecurity);
							}
						}
						return true;
					}
				});

		// Trigger authentication if ratified (but no HTTP security)
		if (isLoadRatified && (loadRatifiedSecurity == null)) {

			// Start authentication
			listener.notifyStarted();

			// Confirm trigger authentication
			InvokedProcessServicer loadAuthenticateServicer = new InvokedProcessServicer() {
				@Override
				public void service(int processIndex, Object parameter,
						ManagedObject managedObject) throws Throwable {

					// Ensure context is correct
					TaskAuthenticateContext<HttpSecurity, HttpCredentials> context = (TaskAuthenticateContext<HttpSecurity, HttpCredentials>) parameter;
					assertSame(
							"Incorrect connection",
							HttpAuthenticationManagedObjectSourceTest.this.connection,
							context.getConnection());
					assertSame(
							"Incorrect session",
							HttpAuthenticationManagedObjectSourceTest.this.session,
							context.getSession());
					assertNull("Should be no credentials on loading",
							context.getCredentials());

					// Determine if authentication failure
					if (loadAuthenticationFailure != null) {
						context.setFailure(loadAuthenticationFailure);

					} else {
						// Provide the authenticated security
						context.setHttpSecurity(loadAuthenticatedSecurity);
					}
				}
			};
			loader.registerInvokeProcessServicer(Flows.AUTHENTICATE,
					loadAuthenticateServicer);

			// Authentication completing
			listener.notifyComplete();
		}

		// Determine if trigger authentication manually
		HttpAuthenticateRequest<HttpCredentials> request = null;
		if (credentials != null) {

			// Record manual authentication
			request = this.createMock(HttpAuthenticateRequest.class);
			this.recordReturn(request, request.getCredentials(), credentials);

			// Record ratifying (matching logic is above)
			this.recordReturn(this.source,
					this.source.ratify(manualRatifyContext), isManuallyRatified);

			// Determine if authentication
			boolean isUndertakeManualAuthentication = isManuallyRatified
					&& (manualRatifiedSecurity == null);
			if (!isUndertakeManualAuthentication) {
				// Not undertaking authentication, so flag request complete
				request.authenticationComplete();

			} else {
				// Trigger authentication
				listener.notifyStarted();

				// Confirm trigger authentication
				InvokedProcessServicer manualAuthenticateServicer = new InvokedProcessServicer() {
					@Override
					public void service(int processIndex, Object parameter,
							ManagedObject managedObject) throws Throwable {

						// Ensure context is correct
						TaskAuthenticateContext<HttpSecurity, HttpCredentials> context = (TaskAuthenticateContext<HttpSecurity, HttpCredentials>) parameter;
						assertSame(
								"Incorrect connection",
								HttpAuthenticationManagedObjectSourceTest.this.connection,
								context.getConnection());
						assertSame(
								"Incorrect session",
								HttpAuthenticationManagedObjectSourceTest.this.session,
								context.getSession());
						assertSame("Incorrect credentials", credentials,
								context.getCredentials());

						// Determine if manual failure
						if (manualFailure != null) {
							context.setFailure(manualFailure);

						} else {
							// Provide the authenticated security
							context.setHttpSecurity(manualSecurity);
						}
					}
				};
				loader.registerInvokeProcessServicer(Flows.AUTHENTICATE,
						manualAuthenticateServicer);

				// Authentication completing
				listener.notifyComplete();
				request.authenticationComplete();
			}
		}

		// Test
		this.replayMockObjects();

		// Register access to the HTTP Security Source
		String key = HttpSecurityConfigurator.registerHttpSecuritySource(
				this.source, this.securityType);
		loader.addProperty(
				HttpAuthenticationManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY,
				key);

		// Load the source
		HttpAuthenticationManagedObjectSource source = loader
				.loadManagedObjectSource(HttpAuthenticationManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setAsynchronousListener(listener);
		user.mapDependency(Dependencies.SERVER_HTTP_CONNECTION,
				HttpAuthenticationManagedObjectSourceTest.this.connection);
		user.mapDependency(Dependencies.HTTP_SESSION,
				HttpAuthenticationManagedObjectSourceTest.this.session);
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Obtain the HTTP authentication
		HttpAuthentication<HttpSecurity, HttpCredentials> authentication = (HttpAuthentication<HttpSecurity, HttpCredentials>) managedObject
				.getObject();

		// Determine if undertake manual authentication
		if (request != null) {
			authentication.authenticate(request);
		}

		// Verify mock objects
		this.verifyMockObjects();

		// Return the HTTP authentication
		return authentication;
	}
}