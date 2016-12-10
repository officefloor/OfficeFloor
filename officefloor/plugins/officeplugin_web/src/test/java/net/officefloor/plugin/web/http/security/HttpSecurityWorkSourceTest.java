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

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.route.HttpUrlContinuationTest;
import net.officefloor.plugin.web.http.security.impl.AbstractHttpSecuritySource;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.type.HttpSecurityLoaderUtil;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link HttpSecurityWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link HttpSecuritySource}.
	 */
	private final MockHttpSecuritySource source = new MockHttpSecuritySource(
			this);

	/**
	 * {@link TaskContext}.
	 */
	@SuppressWarnings("rawtypes")
	private final TaskContext taskContext = this.createMock(TaskContext.class);

	/**
	 * {@link TaskAuthenticateContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskAuthenticateContext<HttpSecurity, HttpCredentials> authenticateContext = this
			.createMock(TaskAuthenticateContext.class);

	/**
	 * {@link HttpCredentials}.
	 */
	private final HttpCredentials credentials = this
			.createMock(HttpCredentials.class);

	/**
	 * {@link HttpAuthentication}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpAuthentication<HttpSecurity, HttpCredentials> authentication = this
			.createMock(HttpAuthentication.class);

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
	 * {@link CredentialStore}.
	 */
	private final CredentialStore store = this
			.createMock(CredentialStore.class);

	/**
	 * {@link HttpRequestState}.
	 */
	private final HttpRequestState requestState = this
			.createMock(HttpRequestState.class);

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity security = this.createMock(HttpSecurity.class);

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpSecurityWorkSource.class,
				HttpSecurityWorkSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY,
				"HTTP Security Source Key");
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Register the HTTP security source
		HttpSecurityType<HttpSecurity, HttpCredentials, Dependencies, Flows> securityType = HttpSecurityLoaderUtil
				.loadHttpSecurityType(this.source);
		String key = HttpSecurityConfigurator.registerHttpSecuritySource(
				this.source, securityType);

		// Create the expected type
		WorkTypeBuilder<HttpSecurityWork> type = WorkLoaderUtil
				.createWorkTypeBuilder(new HttpSecurityWork(this.source));

		// Managed Object Authentication task
		TaskTypeBuilder<Indexed, None> moAuthenticateTask = type.addTaskType(
				"MANAGED_OBJECT_AUTHENTICATE",
				new ManagedObjectHttpAuthenticateTask(), Indexed.class,
				None.class);
		moAuthenticateTask.addObject(TaskAuthenticateContext.class).setLabel(
				"TASK_AUTHENTICATE_CONTEXT");
		moAuthenticateTask.addObject(CredentialStore.class).setLabel(
				"DEPENDENCY_CREDENTIAL_STORE");

		// Managed Object Logout task
		TaskTypeBuilder<Indexed, None> moLogoutTask = type.addTaskType(
				"MANAGED_OBJECT_LOGOUT", new ManagedObjectHttpLogoutTask(),
				Indexed.class, None.class);
		moLogoutTask.addObject(TaskLogoutContext.class).setLabel(
				"TASK_LOGOUT_CONTEXT");
		moLogoutTask.addObject(CredentialStore.class).setLabel(
				"DEPENDENCY_CREDENTIAL_STORE");

		// Challenge task
		TaskTypeBuilder<Indexed, Indexed> challengeTask = type.addTaskType(
				"CHALLENGE", new HttpChallengeTask(), Indexed.class,
				Indexed.class);
		challengeTask.addObject(HttpAuthenticationRequiredException.class)
				.setLabel("HTTP_AUTHENTICATION_REQUIRED_EXCEPTION");
		challengeTask.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		challengeTask.addObject(HttpSession.class).setLabel("HTTP_SESSION");
		challengeTask.addObject(HttpRequestState.class).setLabel(
				"HTTP_REQUEST_STATE");
		challengeTask.addObject(CredentialStore.class).setLabel(
				"DEPENDENCY_CREDENTIAL_STORE");
		TaskFlowTypeBuilder<Indexed> challengeFailureFlow = challengeTask
				.addFlow();
		challengeFailureFlow.setArgumentType(Throwable.class);
		challengeFailureFlow.setLabel("FAILURE");
		TaskFlowTypeBuilder<Indexed> challengeLoginFlow = challengeTask
				.addFlow();
		challengeLoginFlow.setArgumentType(Void.class);
		challengeLoginFlow.setLabel("FLOW_FORM_LOGIN_PAGE");

		// Start Application Authentication task
		TaskTypeBuilder<StartApplicationHttpAuthenticateTask.Dependencies, StartApplicationHttpAuthenticateTask.Flows> startTask = type
				.addTaskType(
						"START_APPLICATION_AUTHENTICATE",
						new StartApplicationHttpAuthenticateTask(),
						StartApplicationHttpAuthenticateTask.Dependencies.class,
						StartApplicationHttpAuthenticateTask.Flows.class);
		startTask.addObject(HttpCredentials.class).setKey(
				StartApplicationHttpAuthenticateTask.Dependencies.CREDENTIALS);
		startTask
				.addObject(HttpAuthentication.class)
				.setKey(StartApplicationHttpAuthenticateTask.Dependencies.HTTP_AUTHENTICATION);
		TaskFlowTypeBuilder<StartApplicationHttpAuthenticateTask.Flows> startFailureFlow = startTask
				.addFlow();
		startFailureFlow
				.setKey(StartApplicationHttpAuthenticateTask.Flows.FAILURE);
		startFailureFlow.setArgumentType(Throwable.class);

		// Complete Application Authentication Task
		TaskTypeBuilder<CompleteApplicationHttpAuthenticateTask.Dependencies, CompleteApplicationHttpAuthenticateTask.Flows> completeTask = type
				.addTaskType(
						"COMPLETE_APPLICATION_AUTHENTICATE",
						new CompleteApplicationHttpAuthenticateTask(),
						CompleteApplicationHttpAuthenticateTask.Dependencies.class,
						CompleteApplicationHttpAuthenticateTask.Flows.class);
		completeTask
				.addObject(HttpAuthentication.class)
				.setKey(CompleteApplicationHttpAuthenticateTask.Dependencies.HTTP_AUTHENTICATION);
		completeTask
				.addObject(ServerHttpConnection.class)
				.setKey(CompleteApplicationHttpAuthenticateTask.Dependencies.SERVER_HTTP_CONNECTION);
		completeTask
				.addObject(HttpSession.class)
				.setKey(CompleteApplicationHttpAuthenticateTask.Dependencies.HTTP_SESSION);
		completeTask
				.addObject(HttpRequestState.class)
				.setKey(CompleteApplicationHttpAuthenticateTask.Dependencies.REQUEST_STATE);
		TaskFlowTypeBuilder<CompleteApplicationHttpAuthenticateTask.Flows> completeFailureFlow = completeTask
				.addFlow();
		completeFailureFlow
				.setKey(CompleteApplicationHttpAuthenticateTask.Flows.FAILURE);
		completeFailureFlow.setArgumentType(Throwable.class);

		// Validate type
		WorkType<HttpSecurityWork> work = WorkLoaderUtil.validateWorkType(type,
				HttpSecurityWorkSource.class,
				HttpSecurityWorkSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY, key);
		assertSame("Incorrect HTTP Security Source", this.source, work
				.getWorkFactory().createWork().getHttpSecuritySource());
	}

	/**
	 * Ensure can undertake authentication for the
	 * {@link HttpAuthenticationManagedObjectSource}.
	 */
	public void testManagedObjectAuthenticate() throws Throwable {
		final HttpSecurity security = this.createMock(HttpSecurity.class);
		this.recordManagedObjectAuthenticationDependencies();
		this.source.security = security;
		this.authenticateContext.setHttpSecurity(security);
		this.doManagedObjectAuthentication();
	}

	/**
	 * Ensure flags that not authenticated for the
	 * {@link HttpAuthenticationManagedObjectSource}.
	 */
	public void testNotManagedObjectAuthenticate() {
		this.recordManagedObjectAuthenticationDependencies();
		this.authenticateContext.setHttpSecurity(null);
		this.doManagedObjectAuthentication();
	}

	/**
	 * Ensure informs of {@link IOException} for the
	 * {@link HttpAuthenticationManagedObjectSource}.
	 */
	public void testManagedObjectAuthenticateIoException() {
		final IOException exception = new IOException("TEST");
		this.recordManagedObjectAuthenticationDependencies();
		this.source.ioException = exception;
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
			Task<HttpSecurityWork, Dependencies, Flows> task = this
					.createTask("MANAGED_OBJECT_AUTHENTICATE");
			task.doTask(this.taskContext);
		} catch (Throwable ex) {
			throw fail(ex);
		}
		this.verifyMockObjects();
	}

	/**
	 * Records obtaining dependencies from the {@link TaskAuthenticateContext}
	 * and {@link TaskContext}.
	 */
	private void recordManagedObjectAuthenticationDependencies() {
		this.recordReturn(this.taskContext, this.taskContext.getWork(),
				new HttpSecurityWork(this.source));
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				this.authenticateContext);
		this.recordReturn(this.authenticateContext,
				this.authenticateContext.getCredentials(), this.credentials);
		this.recordReturn(this.authenticateContext,
				this.authenticateContext.getConnection(), this.connection);
		this.recordReturn(this.authenticateContext,
				this.authenticateContext.getSession(), this.session);
		this.recordReturn(this.taskContext, this.taskContext.getObject(1),
				this.store);
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
	public void testChallengeWithApplicationSpecificBehaviour()
			throws Exception {
		this.doChallengeTest(true, true, null);
	}

	/**
	 * Ensure can undertake challenging again requiring application specific
	 * behaviour (such as form based login).
	 */
	public void testChallengeAgainWithApplicationSpecificBehaviour()
			throws Exception {
		this.doChallengeTest(false, true, null);
	}

	/**
	 * Ensure handle {@link IOException} on challenge.
	 */
	public void testChallengeIoException() throws Exception {
		IOException ioException = new IOException("TEST");
		this.source.ioException = ioException;
		this.doChallengeTest(true, false, ioException);
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
	private void doChallengeTest(boolean isSaveRequest, boolean isInvokeFlow,
			Throwable exception) throws Exception {

		final FlowFuture future = this.createMock(FlowFuture.class);

		// Record initial challenge dependencies
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				new HttpAuthenticationRequiredException(isSaveRequest));
		this.recordReturn(this.taskContext, this.taskContext.getObject(1),
				this.connection);
		this.recordReturn(this.taskContext, this.taskContext.getObject(2),
				this.session);
		this.recordReturn(this.taskContext, this.taskContext.getObject(3),
				this.requestState);

		// Record saving request state (if saving request)
		if (isSaveRequest) {
			HttpUrlContinuationTest.recordSaveRequest(
					"CHALLENGE_REQUEST_MOMENTO", this.connection,
					this.requestState, this.session, this);
		}

		// Record remaining challenge dependencies
		this.recordReturn(this.taskContext, this.taskContext.getWork(),
				new HttpSecurityWork(this.source));
		this.recordReturn(this.taskContext, this.taskContext.getObject(4),
				this.store);

		// Determine if exception
		if (exception != null) {
			this.recordReturn(this.taskContext,
					this.taskContext.doFlow(0, exception), future);
		}

		// Determine if application specific flow
		if (isInvokeFlow) {
			this.source.isDoChallengeFlow = true;
			this.recordReturn(this.taskContext,
					this.taskContext.doFlow(1, null), future);
		}

		// Undertake challenge
		this.replayMockObjects();
		try {
			Task<HttpSecurityWork, Dependencies, Flows> task = this
					.createTask("CHALLENGE");
			task.doTask(this.taskContext);
		} catch (Throwable ex) {
			throw fail(ex);
		}
		this.verifyMockObjects();
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
	public void testStartApplicationAuthenticateWithoutCredentials()
			throws Throwable {
		this.doStartApplicationAuthentication(null, null);
	}

	/**
	 * Ensure can handle starting authentication failure.
	 */
	public void testStartApplicationAuthenticateFailure() throws Throwable {
		this.doStartApplicationAuthentication(this.credentials,
				new RuntimeException("TEST"));
	}

	/**
	 * Ensure can handle starting authentication error.
	 */
	public void testStartApplicationAuthenticateError() throws Throwable {
		this.doStartApplicationAuthentication(this.credentials, new Error(
				"TEST"));
	}

	/**
	 * Undertakes starting application authentication.
	 */
	@SuppressWarnings("unchecked")
	private void doStartApplicationAuthentication(
			final HttpCredentials credentials, Throwable failure)
			throws Throwable {

		final FlowFuture future = this.createMock(FlowFuture.class);

		// Record obtaining dependencies
		this.recordReturn(
				this.taskContext,
				this.taskContext
						.getObject(StartApplicationHttpAuthenticateTask.Dependencies.CREDENTIALS),
				credentials);
		this.recordReturn(
				this.taskContext,
				this.taskContext
						.getObject(StartApplicationHttpAuthenticateTask.Dependencies.HTTP_AUTHENTICATION),
				this.authentication);

		// Record starting authentication
		this.authentication.authenticate(null);
		this.control(this.authentication).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				HttpAuthenticateRequest<HttpCredentials> request = (HttpAuthenticateRequest<HttpCredentials>) actual[0];
				assertEquals("Incorrect credentials", credentials,
						request.getCredentials());
				request.authenticationComplete(); // should not do anything
				return true;
			}
		});

		// Determine if failure in starting authentication
		if (failure != null) {
			// Record failure
			this.control(this.authentication).setThrowable(failure);
			this.recordReturn(this.taskContext, this.taskContext
					.doFlow(StartApplicationHttpAuthenticateTask.Flows.FAILURE,
							failure), future);
		}

		// Test
		this.replayMockObjects();
		try {
			Task<HttpSecurityWork, Dependencies, Flows> task = this
					.createTask("START_APPLICATION_AUTHENTICATE");
			task.doTask(this.taskContext);
		} finally {
			this.verifyMockObjects();
		}
	}

	/**
	 * Ensure reinstates request on successful authentication.
	 */
	public void testCompleteApplicationAuthenticated() throws Throwable {
		this.doCompleteApplicationAuthentication(this.security, null, true);
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
	public void testCompleteApplicationAuthenticationIoException()
			throws Throwable {
		this.doCompleteApplicationAuthentication(null, new IOException("TEST"),
				false);
	}

	/**
	 * Ensure triggers flow for failure.
	 */
	public void testCompleteApplicationAuthenticationFailure() throws Throwable {
		this.doCompleteApplicationAuthentication(null, new RuntimeException(
				"TEST"), false);
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
	public void testCompleteApplicationAuthenticationWithNoContinuation()
			throws Throwable {
		this.doCompleteApplicationAuthentication(this.security, null, false);
	}

	/**
	 * Undertakes starting application authentication.
	 */
	@SuppressWarnings("unchecked")
	private void doCompleteApplicationAuthentication(
			final HttpSecurity security, Throwable failure,
			boolean isRequestReinstated) throws Throwable {

		final FlowFuture future = this.createMock(FlowFuture.class);

		// Record obtaining dependencies
		this.recordReturn(
				this.taskContext,
				this.taskContext
						.getObject(CompleteApplicationHttpAuthenticateTask.Dependencies.HTTP_AUTHENTICATION),
				this.authentication);

		// Record obtaining security
		if (failure != null) {
			// Throw and handle the failure
			this.authentication.getHttpSecurity();
			this.control(this.authentication).setThrowable(failure);
			this.recordReturn(this.taskContext, this.taskContext.doFlow(
					CompleteApplicationHttpAuthenticateTask.Flows.FAILURE,
					failure), future);

		} else {
			// Return the security
			this.recordReturn(this.authentication,
					this.authentication.getHttpSecurity(), security);

			// Handle authenticated
			if (security != null) {

				// Reinstate request
				this.recordReturn(
						this.taskContext,
						this.taskContext
								.getObject(CompleteApplicationHttpAuthenticateTask.Dependencies.SERVER_HTTP_CONNECTION),
						this.connection);
				this.recordReturn(
						this.taskContext,
						this.taskContext
								.getObject(CompleteApplicationHttpAuthenticateTask.Dependencies.HTTP_SESSION),
						this.session);
				this.recordReturn(
						this.taskContext,
						this.taskContext
								.getObject(CompleteApplicationHttpAuthenticateTask.Dependencies.REQUEST_STATE),
						this.requestState);
				HttpUrlContinuationTest.recordReinstateRequest(
						isRequestReinstated,
						HttpSecurityWork.ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO,
						this.connection, this.requestState, this.session, this);
				if (!isRequestReinstated) {
					this.recordReturn(
							this.taskContext,
							this.taskContext
									.doFlow(CompleteApplicationHttpAuthenticateTask.Flows.FAILURE,
											null),
							future,
							new TypeMatcher(
									CompleteApplicationHttpAuthenticateTask.Flows.class,
									HttpAuthenticationContinuationException.class));
				}
			}
		}

		// Test
		HttpAuthenticationRequiredException exception = null;
		this.replayMockObjects();
		try {
			Task<HttpSecurityWork, Dependencies, Flows> task = this
					.createTask("COMPLETE_APPLICATION_AUTHENTICATE");
			task.doTask(this.taskContext);
		} catch (HttpAuthenticationRequiredException ex) {
			exception = ex;
		} finally {
			this.verifyMockObjects();
		}

		// Ensure propagated authentication required if no security
		if ((security == null) && (failure == null)) {
			assertNotNull("Should be authentication required exception",
					exception);
			assertFalse("Should not save request for further challenges",
					exception.isSaveRequest());
		}
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {
		final HttpLogoutRequest request = this
				.createMock(HttpLogoutRequest.class);
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
		final HttpLogoutRequest request = this
				.createMock(HttpLogoutRequest.class);
		IOException ioException = new IOException("TEST");
		this.source.ioException = ioException;
		this.doLogoutTest(request, ioException);
	}

	/**
	 * Ensure handle {@link RuntimeException} on log out.
	 */
	public void testLogoutFailure() throws Exception {
		final HttpLogoutRequest request = this
				.createMock(HttpLogoutRequest.class);
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
	private void doLogoutTest(HttpLogoutRequest logoutRequest,
			Throwable exception) throws Exception {

		final TaskLogoutContext logoutContext = this
				.createMock(TaskLogoutContext.class);

		// Record initial challenge dependencies
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				logoutContext);
		this.recordReturn(logoutContext, logoutContext.getHttpLogoutRequest(),
				logoutRequest);
		this.recordReturn(logoutContext, logoutContext.getConnection(),
				this.connection);
		this.recordReturn(logoutContext, logoutContext.getSession(),
				this.session);

		// Record remaining challenge dependencies
		this.recordReturn(this.taskContext, this.taskContext.getWork(),
				new HttpSecurityWork(this.source));
		this.recordReturn(this.taskContext, this.taskContext.getObject(1),
				this.store);

		// Flag logout complete (if have request)
		if (logoutRequest != null) {
			logoutRequest.logoutComplete(exception);
		}

		// Undertake challenge
		this.replayMockObjects();
		try {
			Task<HttpSecurityWork, Dependencies, Flows> task = this
					.createTask("MANAGED_OBJECT_LOGOUT");
			task.doTask(this.taskContext);
		} catch (Throwable ex) {
			throw fail(ex);
		}
		this.verifyMockObjects();
	}

	/**
	 * Creates the {@link Task}.
	 * 
	 * @param taskName
	 *            Name of the {@link Task} to create.
	 * @return {@link Task}.
	 */
	@SuppressWarnings("unchecked")
	private Task<HttpSecurityWork, Dependencies, Flows> createTask(
			String taskName) {

		// Register the HTTP security source
		HttpSecurityType<HttpSecurity, HttpCredentials, Dependencies, Flows> securityType = HttpSecurityLoaderUtil
				.loadHttpSecurityType(this.source);
		String key = HttpSecurityConfigurator.registerHttpSecuritySource(
				this.source, securityType);

		// Load the work type
		WorkType<HttpSecurityWork> work = WorkLoaderUtil.loadWorkType(
				HttpSecurityWorkSource.class,
				HttpSecurityWorkSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY, key);

		// Find the task type
		TaskType<HttpSecurityWork, ?, ?> task = null;
		for (TaskType<HttpSecurityWork, ?, ?> check : work.getTaskTypes()) {
			if (taskName.equals(check.getTaskName())) {
				task = check;
			}
		}
		assertNotNull("Should have task " + taskName, task);

		// Create and return the task
		return (Task<HttpSecurityWork, Dependencies, Flows>) task
				.getTaskFactory()
				.createTask(work.getWorkFactory().createWork());
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
	private static class MockHttpSecuritySource
			extends
			AbstractHttpSecuritySource<HttpSecurity, HttpCredentials, Dependencies, Flows> {

		/**
		 * Access to {@link HttpSecurityWorkSourceTest}.
		 */
		private final HttpSecurityWorkSourceTest testCase;

		/**
		 * {@link HttpSecurity} to return.
		 */
		public HttpSecurity security = null;

		/**
		 * {@link IOException} to throw.
		 */
		public IOException ioException = null;

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
		 *            {@link HttpSecurityWorkSourceTest}.
		 */
		public MockHttpSecuritySource(HttpSecurityWorkSourceTest testCase) {
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
				MetaDataContext<HttpSecurity, HttpCredentials, Dependencies, Flows> context)
				throws Exception {
			context.setSecurityClass(HttpSecurity.class);
			context.setCredentialsClass(HttpCredentials.class);
			context.addDependency(Dependencies.CREDENTIAL_STORE,
					CredentialStore.class);
			context.addFlow(Flows.FORM_LOGIN_PAGE, null);
		}

		@Override
		public boolean ratify(
				HttpRatifyContext<HttpSecurity, HttpCredentials> context) {
			fail("Should not be required for tasks");
			return false;
		}

		@Override
		public void authenticate(
				HttpAuthenticateContext<HttpSecurity, HttpCredentials, Dependencies> context)
				throws IOException {

			// Ensure can obtain dependencies
			assertSame("Incorrect credentials", this.testCase.credentials,
					context.getCredentials());
			assertSame("Incorrect connection", this.testCase.connection,
					context.getConnection());
			assertSame("Incorrect session", this.testCase.session,
					context.getSession());
			assertSame("Incorrect dependency", this.testCase.store,
					context.getObject(Dependencies.CREDENTIAL_STORE));

			// Determine if failures
			if (this.ioException != null) {
				throw this.ioException;
			}
			if (this.failure != null) {
				throw this.failure;
			}

			// Load the HTTP security
			if (this.security != null) {
				context.setHttpSecurity(this.security);
			}
		}

		@Override
		public void challenge(HttpChallengeContext<Dependencies, Flows> context)
				throws IOException {

			// Ensure can obtain dependencies
			assertSame("Incorrect connection", this.testCase.connection,
					context.getConnection());
			assertSame("Incorrect session", this.testCase.session,
					context.getSession());
			assertSame("Incorrect dependency", this.testCase.store,
					context.getObject(Dependencies.CREDENTIAL_STORE));

			// Determine if failures
			if (this.ioException != null) {
				throw this.ioException;
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
		public void logout(HttpLogoutContext<Dependencies> context)
				throws IOException {

			// Ensure can obtain dependencies
			assertSame("Incorrect connection", this.testCase.connection,
					context.getConnection());
			assertSame("Incorrect session", this.testCase.session,
					context.getSession());
			assertSame("Incorrect dependency", this.testCase.store,
					context.getObject(Dependencies.CREDENTIAL_STORE));

			// Determine if failures
			if (this.ioException != null) {
				throw this.ioException;
			}
			if (this.failure != null) {
				throw this.failure;
			}
		}
	}

}