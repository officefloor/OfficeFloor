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

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.mock.MockWebApp;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.impl.CompleteApplicationHttpAuthenticateFunction;
import net.officefloor.web.security.impl.FunctionAuthenticateContext;
import net.officefloor.web.security.impl.FunctionLogoutContext;
import net.officefloor.web.security.impl.HttpAuthenticationManagedObjectSource;
import net.officefloor.web.security.impl.HttpAuthenticationRequiredException;
import net.officefloor.web.security.impl.HttpChallengeFunction;
import net.officefloor.web.security.impl.HttpSecurityConfiguration;
import net.officefloor.web.security.impl.HttpSecurityManagedFunctionSource;
import net.officefloor.web.security.impl.ManagedObjectHttpAuthenticateFunction;
import net.officefloor.web.security.impl.ManagedObjectHttpLogoutFunction;
import net.officefloor.web.security.impl.StartApplicationHttpAuthenticateFunction;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * Tests the {@link HttpSecurityManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link HttpSecuritySource}.
	 */
	private final MockHttpSecuritySource source = new MockHttpSecuritySource(this);

	/**
	 * {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("rawtypes")
	private final ManagedFunctionContext functionContext = this.createMock(ManagedFunctionContext.class);

	/**
	 * {@link FunctionAuthenticateContext}.
	 */
	@SuppressWarnings("unchecked")
	private final FunctionAuthenticateContext<HttpAccessControl, HttpCredentials> authenticateContext = this
			.createMock(FunctionAuthenticateContext.class);

	/**
	 * {@link HttpCredentials}.
	 */
	private final HttpCredentials credentials = this.createMock(HttpCredentials.class);

	/**
	 * {@link HttpAuthentication}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpAuthentication<HttpCredentials> authentication = this.createMock(HttpAuthentication.class);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = MockHttpServer.mockConnection();

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session = MockWebApp.mockSession(this.connection);

	/**
	 * {@link CredentialStore}.
	 */
	private final CredentialStore store = this.createMock(CredentialStore.class);

	/**
	 * {@link HttpRequestState}.
	 */
	private final HttpRequestState requestState = MockWebApp.mockRequestState(this.connection);

	/**
	 * {@link HttpAccessControl}.
	 */
	private final HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(new HttpSecurityManagedFunctionSource(null));
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Load the security configuration
		HttpSecurityConfiguration<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> securityConfiguration = HttpSecurityLoaderUtil
				.loadHttpSecurityConfiguration(this.source);

		// Create the expected type
		FunctionNamespaceBuilder type = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Managed Object Authentication function
		ManagedFunctionTypeBuilder<Indexed, None> moAuthenticateFunction = type.addManagedFunctionType(
				"MANAGED_OBJECT_AUTHENTICATE", new ManagedObjectHttpAuthenticateFunction(this.source), Indexed.class,
				None.class);
		moAuthenticateFunction.addObject(FunctionAuthenticateContext.class).setLabel("FUNCTION_AUTHENTICATE_CONTEXT");
		moAuthenticateFunction.addObject(CredentialStore.class).setLabel("DEPENDENCY_CREDENTIAL_STORE");

		// Managed Object Logout function
		ManagedFunctionTypeBuilder<Indexed, None> moLogoutFunction = type.addManagedFunctionType(
				"MANAGED_OBJECT_LOGOUT", new ManagedObjectHttpLogoutFunction(this.source), Indexed.class, None.class);
		moLogoutFunction.addObject(FunctionLogoutContext.class).setLabel("FUNCTION_LOGOUT_CONTEXT");
		moLogoutFunction.addObject(CredentialStore.class).setLabel("DEPENDENCY_CREDENTIAL_STORE");

		// Challenge function
		ManagedFunctionTypeBuilder<Indexed, Indexed> challengeFunction = type.addManagedFunctionType("CHALLENGE",
				new HttpChallengeFunction(this.source), Indexed.class, Indexed.class);
		challengeFunction.addObject(HttpAuthenticationRequiredException.class)
				.setLabel("HTTP_AUTHENTICATION_REQUIRED_EXCEPTION");
		challengeFunction.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
		challengeFunction.addObject(HttpSession.class).setLabel("HTTP_SESSION");
		challengeFunction.addObject(HttpRequestState.class).setLabel("HTTP_REQUEST_STATE");
		challengeFunction.addObject(CredentialStore.class).setLabel("DEPENDENCY_CREDENTIAL_STORE");
		ManagedFunctionFlowTypeBuilder<Indexed> challengeFailureFlow = challengeFunction.addFlow();
		challengeFailureFlow.setArgumentType(Throwable.class);
		challengeFailureFlow.setLabel("FAILURE");
		ManagedFunctionFlowTypeBuilder<Indexed> challengeLoginFlow = challengeFunction.addFlow();
		challengeLoginFlow.setArgumentType(Void.class);
		challengeLoginFlow.setLabel("FLOW_FORM_LOGIN_PAGE");

		// Start Application Authentication function
		ManagedFunctionTypeBuilder<StartApplicationHttpAuthenticateFunction.Dependencies, StartApplicationHttpAuthenticateFunction.Flows> startFunction = type
				.addManagedFunctionType("START_APPLICATION_AUTHENTICATE",
						new StartApplicationHttpAuthenticateFunction(),
						StartApplicationHttpAuthenticateFunction.Dependencies.class,
						StartApplicationHttpAuthenticateFunction.Flows.class);
		startFunction.addObject(HttpCredentials.class)
				.setKey(StartApplicationHttpAuthenticateFunction.Dependencies.CREDENTIALS);
		startFunction.addObject(HttpAuthentication.class)
				.setKey(StartApplicationHttpAuthenticateFunction.Dependencies.HTTP_AUTHENTICATION);
		ManagedFunctionFlowTypeBuilder<StartApplicationHttpAuthenticateFunction.Flows> startFailureFlow = startFunction
				.addFlow();
		startFailureFlow.setKey(StartApplicationHttpAuthenticateFunction.Flows.FAILURE);
		startFailureFlow.setArgumentType(Throwable.class);

		// Complete Application Authentication function
		ManagedFunctionTypeBuilder<CompleteApplicationHttpAuthenticateFunction.Dependencies, CompleteApplicationHttpAuthenticateFunction.Flows> completeFunction = type
				.addManagedFunctionType("COMPLETE_APPLICATION_AUTHENTICATE",
						new CompleteApplicationHttpAuthenticateFunction(),
						CompleteApplicationHttpAuthenticateFunction.Dependencies.class,
						CompleteApplicationHttpAuthenticateFunction.Flows.class);
		completeFunction.addObject(HttpAuthentication.class)
				.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.HTTP_AUTHENTICATION);
		completeFunction.addObject(HttpSession.class)
				.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.HTTP_SESSION);
		completeFunction.addObject(HttpRequestState.class)
				.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.REQUEST_STATE);
		ManagedFunctionFlowTypeBuilder<CompleteApplicationHttpAuthenticateFunction.Flows> completeFailureFlow = completeFunction
				.addFlow();
		completeFailureFlow.setKey(CompleteApplicationHttpAuthenticateFunction.Flows.FAILURE);
		completeFailureFlow.setArgumentType(Throwable.class);

		// Validate type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(type,
				new HttpSecurityManagedFunctionSource(securityConfiguration));
	}

	/**
	 * Ensure can undertake authentication for the
	 * {@link HttpAuthenticationManagedObjectSource}.
	 */
	public void testManagedObjectAuthenticate() throws Throwable {
		final HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);
		this.recordManagedObjectAuthenticationDependencies();
		this.source.accessControl = accessControl;
		this.authenticateContext.setAccessControl(accessControl);
		this.doManagedObjectAuthentication();
	}

	/**
	 * Ensure flags that not authenticated for the
	 * {@link HttpAuthenticationManagedObjectSource}.
	 */
	public void testNotManagedObjectAuthenticate() {
		this.recordManagedObjectAuthenticationDependencies();
		this.authenticateContext.setAccessControl(null);
		this.doManagedObjectAuthentication();
	}

	/**
	 * Ensure informs of {@link IOException} for the
	 * {@link HttpAuthenticationManagedObjectSource}.
	 */
	public void testManagedObjectAuthenticateHttpException() {
		final HttpException exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null, "TEST");
		this.recordManagedObjectAuthenticationDependencies();
		this.source.httpException = exception;
		this.authenticateContext.setFailure(exception);
		this.doManagedObjectAuthentication();
	}

	/**
	 * Ensure informs of {@link RuntimeException} for the
	 * {@link HttpAuthenticationManagedObjectSource}.
	 */
	public void testManagedObjectAuthenticateFailure() {
		final RuntimeException exception = new RuntimeException("TEST");
		this.recordManagedObjectAuthenticationDependencies();
		this.source.failure = exception;
		this.authenticateContext.setFailure(exception);
		this.doManagedObjectAuthentication();
	}

	/**
	 * Undertakes authentication for the
	 * {@link HttpAuthenticationManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	private void doManagedObjectAuthentication() {
		this.replayMockObjects();
		try {
			ManagedFunction<Dependencies, Flows> task = this.createFunction("MANAGED_OBJECT_AUTHENTICATE");
			task.execute(this.functionContext);
		} catch (Throwable ex) {
			throw fail(ex);
		}
		this.verifyMockObjects();
	}

	/**
	 * Records obtaining dependencies from the
	 * {@link FunctionAuthenticateContext} and {@link ManagedFunctionContext}.
	 */
	private void recordManagedObjectAuthenticationDependencies() {
		this.recordReturn(this.functionContext, this.functionContext.getObject(0), this.authenticateContext);
		this.recordReturn(this.authenticateContext, this.authenticateContext.getCredentials(), this.credentials);
		this.recordReturn(this.authenticateContext, this.authenticateContext.getConnection(), this.connection);
		this.recordReturn(this.authenticateContext, this.authenticateContext.getSession(), this.session);
		this.recordReturn(this.functionContext, this.functionContext.getObject(1), this.store);
	}

	/**
	 * Ensure can undertake challenge not requiring application specific
	 * functionality.
	 */
	public void testChallenge() throws Exception {
		this.doChallengeTest(true, false, null);
	}

	/**
	 * Ensure can undertake another challenge for same authentication.
	 */
	public void testChallengeAgainForAuthentication() throws Exception {
		this.doChallengeTest(false, false, null);
	}

	/**
	 * Ensure can undertake challenge requiring application specific behaviour
	 * (such as form based login).
	 */
	public void testChallengeWithApplicationSpecificBehaviour() throws Exception {
		this.doChallengeTest(true, true, null);
	}

	/**
	 * Ensure can undertake challenging again requiring application specific
	 * behaviour (such as form based login).
	 */
	public void testChallengeAgainWithApplicationSpecificBehaviour() throws Exception {
		this.doChallengeTest(false, true, null);
	}

	/**
	 * Ensure handle {@link HttpException} on challenge.
	 */
	public void testChallengeHttpException() throws Exception {
		HttpException httpException = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null, "TEST");
		this.source.httpException = httpException;
		this.doChallengeTest(true, false, httpException);
	}

	/**
	 * Ensure handle {@link RuntimeException} on challenge.
	 */
	public void testChallengeFailure() throws Exception {
		RuntimeException failure = new RuntimeException("TEST");
		this.source.failure = failure;
		this.doChallengeTest(true, false, failure);
	}

	/**
	 * Ensure handle {@link RuntimeException} on challenging again for
	 * authentication.
	 */
	public void testChallengeAgainFailure() throws Exception {
		RuntimeException failure = new RuntimeException("TEST");
		this.source.failure = failure;
		this.doChallengeTest(false, false, failure);
	}

	/**
	 * Undertakes the challenge test.
	 * 
	 * @param isSaveRequest
	 *            Indicates if saving request.
	 * @param isInvokeFlow
	 *            Indicates whether to invoke the flow.
	 * @param exception
	 *            {@link Exception} in undertaking challenge.
	 */
	@SuppressWarnings("unchecked")
	private void doChallengeTest(boolean isSaveRequest, boolean isInvokeFlow, Throwable exception) throws Exception {

		// Record initial challenge dependencies
		this.recordReturn(this.functionContext, this.functionContext.getObject(0),
				new HttpAuthenticationRequiredException(isSaveRequest));
		this.recordReturn(this.functionContext, this.functionContext.getObject(1), this.connection);
		this.recordReturn(this.functionContext, this.functionContext.getObject(2), this.session);
		this.recordReturn(this.functionContext, this.functionContext.getObject(3), this.requestState);

		// Record remaining challenge dependencies
		this.recordReturn(this.functionContext, this.functionContext.getObject(4), this.store);

		// Determine if exception
		if (exception != null) {
			this.functionContext.doFlow(0, exception, null);
		}

		// Determine if application specific flow
		if (isInvokeFlow) {
			this.source.isDoChallengeFlow = true;
			this.functionContext.doFlow(1, null, null);
		}

		// Undertake challenge
		this.replayMockObjects();
		try {
			ManagedFunction<Dependencies, Flows> function = this.createFunction("CHALLENGE");
			function.execute(this.functionContext);
		} catch (Throwable ex) {
			throw fail(ex);
		}
		this.verifyMockObjects();

		// Record saving request state (if saving request)
		if (isSaveRequest) {
			assertNotNull("Should have saved request state",
					this.session.getAttribute(HttpChallengeFunction.ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO));
		}

	}

	/**
	 * Ensure can undertake application authentication.
	 */
	public void testStartApplicationAuthenticate() throws Throwable {
		this.doStartApplicationAuthentication(this.credentials, null);
	}

	/**
	 * Ensure can undertake application authentication without credentials.
	 */
	public void testStartApplicationAuthenticateWithoutCredentials() throws Throwable {
		this.doStartApplicationAuthentication(null, null);
	}

	/**
	 * Ensure can handle starting authentication failure.
	 */
	public void testStartApplicationAuthenticateFailure() throws Throwable {
		this.doStartApplicationAuthentication(this.credentials, new RuntimeException("TEST"));
	}

	/**
	 * Ensure can handle starting authentication error.
	 */
	public void testStartApplicationAuthenticateError() throws Throwable {
		this.doStartApplicationAuthentication(this.credentials, new Error("TEST"));
	}

	/**
	 * Undertakes starting application authentication.
	 */
	@SuppressWarnings("unchecked")
	private void doStartApplicationAuthentication(final HttpCredentials credentials, Throwable failure)
			throws Throwable {

		// Record obtaining dependencies
		this.recordReturn(this.functionContext,
				this.functionContext.getObject(StartApplicationHttpAuthenticateFunction.Dependencies.CREDENTIALS),
				credentials);
		this.recordReturn(this.functionContext,
				this.functionContext
						.getObject(StartApplicationHttpAuthenticateFunction.Dependencies.HTTP_AUTHENTICATION),
				this.authentication);

		// Record starting authentication
		this.authentication.authenticate(null, null);
		this.control(this.authentication).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect credentials", credentials, actual[0]);
				HttpAuthenticateCallback request = (HttpAuthenticateCallback) actual[1];
				request.authenticationComplete(); // should not do anything
				return true;
			}
		});

		// Determine if failure in starting authentication
		if (failure != null) {
			// Record failure
			this.control(this.authentication).setThrowable(failure);
			this.functionContext.doFlow(StartApplicationHttpAuthenticateFunction.Flows.FAILURE, failure, null);
		}

		// Test
		this.replayMockObjects();
		try {
			ManagedFunction<Dependencies, Flows> function = this.createFunction("START_APPLICATION_AUTHENTICATE");
			function.execute(this.functionContext);
		} finally {
			this.verifyMockObjects();
		}
	}

	/**
	 * Ensure reinstates request on successful authentication.
	 */
	public void testCompleteApplicationAuthenticated() throws Throwable {
		this.doCompleteApplicationAuthentication(this.accessControl, null, true);
	}

	/**
	 * Ensure triggers challenge by throwing exception when not authenticated.
	 */
	public void testCompleteApplicationNotAuthenticated() throws Throwable {
		this.doCompleteApplicationAuthentication(null, null, false);
	}

	/**
	 * Ensure triggers flow for {@link IOException}.
	 */
	public void testCompleteApplicationAuthenticationHttpException() throws Throwable {
		this.doCompleteApplicationAuthentication(null,
				new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null, "TEST"), false);
	}

	/**
	 * Ensure triggers flow for failure.
	 */
	public void testCompleteApplicationAuthenticationFailure() throws Throwable {
		this.doCompleteApplicationAuthentication(null, new RuntimeException("TEST"), false);
	}

	/**
	 * Ensure triggers flow for error.
	 */
	public void testCompleteApplicationAuthenticationError() throws Throwable {
		this.doCompleteApplicationAuthentication(null, new Error("TEST"), false);
	}

	/**
	 * Ensure triggers flow for no continuation.
	 */
	public void testCompleteApplicationAuthenticationWithNoContinuation() throws Throwable {
		this.doCompleteApplicationAuthentication(this.accessControl, null, false);
	}

	/**
	 * Undertakes starting application authentication.
	 */
	@SuppressWarnings("unchecked")
	private void doCompleteApplicationAuthentication(final HttpAccessControl accessControl, Throwable failure,
			boolean isRequestReinstated) throws Throwable {

		// Record obtaining dependencies
		this.recordReturn(this.functionContext,
				this.functionContext
						.getObject(CompleteApplicationHttpAuthenticateFunction.Dependencies.HTTP_AUTHENTICATION),
				this.authentication);

		// Record obtaining security
		if (failure != null) {
			// Throw and handle the failure
			this.authentication.getAccessControl();
			this.control(this.authentication).setThrowable(failure);
			this.functionContext.doFlow(CompleteApplicationHttpAuthenticateFunction.Flows.FAILURE, failure, null);

		} else {
			// Return the access control
			this.recordReturn(this.authentication, this.authentication.getAccessControl(), accessControl);

			// Handle authenticated
			if (accessControl != null) {

				// Reinstate request
				this.recordReturn(this.functionContext, this.functionContext.getObject(
						CompleteApplicationHttpAuthenticateFunction.Dependencies.HTTP_SESSION), this.session);
				this.recordReturn(this.functionContext,
						this.functionContext
								.getObject(CompleteApplicationHttpAuthenticateFunction.Dependencies.REQUEST_STATE),
						this.requestState);
				if (!isRequestReinstated) {
					this.functionContext.doFlow(CompleteApplicationHttpAuthenticateFunction.Flows.FAILURE, null, null);
					this.control(this.functionContext)
							.setMatcher(new TypeMatcher(CompleteApplicationHttpAuthenticateFunction.Flows.class,
									HttpAuthenticationContinuationException.class, null));
				} else {
					this.session.setAttribute(HttpChallengeFunction.ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO,
							HttpRequestStateManagedObjectSource.exportHttpRequestState(this.requestState));
				}
			}
		}

		// Test
		HttpAuthenticationRequiredException exception = null;
		this.replayMockObjects();
		try {
			ManagedFunction<Dependencies, Flows> function = this.createFunction("COMPLETE_APPLICATION_AUTHENTICATE");
			function.execute(this.functionContext);
		} catch (HttpAuthenticationRequiredException ex) {
			exception = ex;
		} finally {
			this.verifyMockObjects();
		}

		// Ensure propagated authentication required if no security
		if ((accessControl == null) && (failure == null)) {
			assertNotNull("Should be authentication required exception", exception);
			assertFalse("Should not save request for further challenges", exception.isSaveRequest());
		}
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {
		final HttpLogoutRequest request = this.createMock(HttpLogoutRequest.class);
		this.doLogoutTest(request, null);
	}

	/**
	 * Ensure can log out without {@link HttpLogoutRequest}.
	 */
	public void testLogoutWithoutRequest() throws Exception {
		this.doLogoutTest(null, null);
	}

	/**
	 * Ensure handle {@link IOException} on log out.
	 */
	public void testLogoutIoException() throws Exception {
		final HttpLogoutRequest request = this.createMock(HttpLogoutRequest.class);
		HttpException httpException = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null, "TEST");
		this.source.httpException = httpException;
		this.doLogoutTest(request, httpException);
	}

	/**
	 * Ensure handle {@link RuntimeException} on log out.
	 */
	public void testLogoutFailure() throws Exception {
		final HttpLogoutRequest request = this.createMock(HttpLogoutRequest.class);
		RuntimeException failure = new RuntimeException("TEST");
		this.source.failure = failure;
		this.doLogoutTest(request, failure);
	}

	/**
	 * Ensure handle {@link RuntimeException} on log out without
	 * {@link HttpLogoutRequest}.
	 */
	public void testLogoutFailureWithoutRequest() throws Exception {
		RuntimeException failure = new RuntimeException("TEST");
		this.source.failure = failure;
		this.doLogoutTest(null, failure);
	}

	/**
	 * Undertakes the logout test.
	 * 
	 * @param exception
	 *            {@link Exception} in undertaking challenge.
	 */
	@SuppressWarnings("unchecked")
	private void doLogoutTest(HttpLogoutRequest logoutRequest, Throwable exception) throws Exception {

		final FunctionLogoutContext logoutContext = this.createMock(FunctionLogoutContext.class);

		// Record initial challenge dependencies
		this.recordReturn(this.functionContext, this.functionContext.getObject(0), logoutContext);
		this.recordReturn(logoutContext, logoutContext.getHttpLogoutRequest(), logoutRequest);
		this.recordReturn(logoutContext, logoutContext.getConnection(), this.connection);
		this.recordReturn(logoutContext, logoutContext.getSession(), this.session);

		// Record remaining challenge dependencies
		this.recordReturn(this.functionContext, this.functionContext.getObject(1), this.store);

		// Flag logout complete (if have request)
		if (logoutRequest != null) {
			logoutRequest.logoutComplete(exception);
		}

		// Undertake challenge
		this.replayMockObjects();
		try {
			ManagedFunction<Dependencies, Flows> function = this.createFunction("MANAGED_OBJECT_LOGOUT");
			function.execute(this.functionContext);
		} catch (Throwable ex) {
			throw fail(ex);
		}
		this.verifyMockObjects();
	}

	/**
	 * Creates the {@link ManagedFunction}.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction} to create.
	 * @return {@link ManagedFunction}.
	 */
	@SuppressWarnings("unchecked")
	private ManagedFunction<Dependencies, Flows> createFunction(String functionName) throws Throwable {

		// Create the HTTP security configuration
		HttpSecurityConfiguration<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> securityConfiguration = HttpSecurityLoaderUtil
				.loadHttpSecurityConfiguration(this.source);

		// Load the namespace type
		FunctionNamespaceType namespace = ManagedFunctionLoaderUtil
				.loadManagedFunctionType(new HttpSecurityManagedFunctionSource(securityConfiguration));

		// Find the function type
		ManagedFunctionType<?, ?> function = null;
		for (ManagedFunctionType<?, ?> check : namespace.getManagedFunctionTypes()) {
			if (functionName.equals(check.getFunctionName())) {
				function = check;
			}
		}
		assertNotNull("Should have function " + functionName, function);

		// Create and return the function
		return (ManagedFunction<Dependencies, Flows>) function.getManagedFunctionFactory().createManagedFunction();
	}

	/**
	 * Dependency keys.
	 */
	private static enum Dependencies {
		CREDENTIAL_STORE
	}

	/**
	 * Flow keys.
	 */
	private static enum Flows {
		FORM_LOGIN_PAGE
	}

	/**
	 * Mock {@link HttpSecuritySource} for testing.
	 */
	private static class MockHttpSecuritySource extends
			AbstractHttpSecuritySource<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows>
			implements
			HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> {

		/**
		 * Access to {@link HttpSecurityManagedFunctionSourceTest}.
		 */
		private final HttpSecurityManagedFunctionSourceTest testCase;

		/**
		 * {@link HttpAccessControl} to return.
		 */
		public HttpAccessControl accessControl = null;

		/**
		 * {@link HttpException} to throw.
		 */
		public HttpException httpException = null;

		/**
		 * Failure to throw.
		 */
		public RuntimeException failure = null;

		/**
		 * Indicates whether to trigger the challenge flow.
		 */
		private boolean isDoChallengeFlow = false;

		/**
		 * Initiate.
		 * 
		 * @param testCase
		 *            {@link HttpSecurityManagedFunctionSourceTest}.
		 */
		public MockHttpSecuritySource(HttpSecurityManagedFunctionSourceTest testCase) {
			this.testCase = testCase;
		}

		/*
		 * ======================= HttpSecuritySource ==========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No properties required
		}

		@Override
		protected void loadMetaData(
				MetaDataContext<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> context)
				throws Exception {
			context.setAccessControlClass(HttpAccessControl.class);
			context.setCredentialsClass(HttpCredentials.class);
			context.addDependency(Dependencies.CREDENTIAL_STORE, CredentialStore.class);
			context.addFlow(Flows.FORM_LOGIN_PAGE, null);
		}

		@Override
		public HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> sourceHttpSecurity(
				HttpSecurityContext context) throws HttpException {
			return this;
		}

		/*
		 * ========================= HttpSecurity ==============================
		 */

		@Override
		public HttpAuthentication<HttpCredentials> createAuthentication() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean ratify(HttpCredentials credentials, HttpRatifyContext<HttpAccessControl> context) {
			fail("Should not be required for tasks");
			return false;
		}

		@Override
		public void authenticate(HttpCredentials credentials,
				HttpAuthenticateContext<HttpAccessControl, Dependencies> context) {

			// Ensure can obtain dependencies
			assertSame("Incorrect credentials", this.testCase.credentials, credentials);
			assertSame("Incorrect connection", this.testCase.connection, context.getConnection());
			assertSame("Incorrect session", this.testCase.session, context.getSession());
			assertSame("Incorrect dependency", this.testCase.store, context.getObject(Dependencies.CREDENTIAL_STORE));

			// Determine if failures
			if (this.httpException != null) {
				throw this.httpException;
			}
			if (this.failure != null) {
				throw this.failure;
			}

			// Load the HTTP security
			if (this.accessControl != null) {
				context.setAccessControl(this.accessControl);
			}
		}

		@Override
		public void challenge(HttpChallengeContext<Dependencies, Flows> context) {

			// Ensure can obtain dependencies
			assertSame("Incorrect connection", this.testCase.connection, context.getConnection());
			assertSame("Incorrect session", this.testCase.session, context.getSession());
			assertSame("Incorrect dependency", this.testCase.store, context.getObject(Dependencies.CREDENTIAL_STORE));

			// Determine if failures
			if (this.httpException != null) {
				throw this.httpException;
			}
			if (this.failure != null) {
				throw this.failure;
			}

			// Determine if undertake flow challenge
			if (this.isDoChallengeFlow) {
				context.doFlow(Flows.FORM_LOGIN_PAGE);
			}
		}

		@Override
		public void logout(HttpLogoutContext<Dependencies> context) {

			// Ensure can obtain dependencies
			assertSame("Incorrect connection", this.testCase.connection, context.getConnection());
			assertSame("Incorrect session", this.testCase.session, context.getSession());
			assertSame("Incorrect dependency", this.testCase.store, context.getObject(Dependencies.CREDENTIAL_STORE));

			// Determine if failures
			if (this.httpException != null) {
				throw this.httpException;
			}
			if (this.failure != null) {
				throw this.failure;
			}
		}
	}

}