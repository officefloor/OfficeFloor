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

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.route.HttpUrlContinuationTest;
import net.officefloor.plugin.web.http.security.impl.AbstractHttpSecuritySource;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
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
	@SuppressWarnings("unchecked")
	private final TaskContext<HttpSecurityWork, Dependencies, Flows> taskContext = this
			.createMock(TaskContext.class);

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
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpSecurityWorkSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Register the HTTP security source
		String key = HttpSecurityConfigurator
				.registerHttpSecuritySource(this.source);

		// Create the expected type
		WorkTypeBuilder<HttpSecurityWork> type = WorkLoaderUtil
				.createWorkTypeBuilder(new HttpSecurityWork(this.source));

		// Managed Object Authentication task
		TaskTypeBuilder<Indexed, None> moAuthenticateTask = type.addTaskType(
				"MANAGED_OBJECT_AUTHENTICATE",
				new HttpManagedObjectAuthenticateTask(), Indexed.class,
				None.class);
		moAuthenticateTask.addObject(TaskAuthenticateContext.class);
		moAuthenticateTask.addObject(CredentialStore.class);

		// Challenge task
		TaskTypeBuilder<Indexed, Indexed> challengeTask = type.addTaskType(
				"CHALLENGE", new HttpChallengeTask(), Indexed.class,
				Indexed.class);
		challengeTask.addObject(HttpAuthenticationRequiredException.class);
		challengeTask.addObject(ServerHttpConnection.class);
		challengeTask.addObject(HttpSession.class);
		challengeTask.addObject(HttpRequestState.class);
		challengeTask.addObject(CredentialStore.class);
		challengeTask.addFlow().setLabel("FAILURE");
		challengeTask.addFlow().setLabel("FORM_LOGIN_PAGE");

		// Application Authentication task
		TaskTypeBuilder<Indexed, HttpApplicationAuthenticateTask.Flows> appAuthenticationTask = type
				.addTaskType("APPLICATION_AUTHENTICATE",
						new HttpApplicationAuthenticateTask(), Indexed.class,
						HttpApplicationAuthenticateTask.Flows.class);
		appAuthenticationTask.addObject(HttpCredentials.class);
		appAuthenticationTask.addObject(ServerHttpConnection.class);
		appAuthenticationTask.addObject(HttpSession.class);
		appAuthenticationTask.addObject(HttpRequestState.class);
		appAuthenticationTask.addObject(CredentialStore.class);
		appAuthenticationTask.addFlow().setKey(
				HttpApplicationAuthenticateTask.Flows.ROUTE_REINSTATED_REQUEST);
		appAuthenticationTask.addFlow().setKey(
				HttpApplicationAuthenticateTask.Flows.FAILURE);

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
		this.doChallengeTest(false, null);
	}

	/**
	 * Ensure can undertake challenge requiring applicaiton specific behaviour
	 * (such as form based login).
	 */
	public void testChallengeWithApplicationSpecificBehaviour()
			throws Exception {
		this.doChallengeTest(true, null);
	}

	/**
	 * Ensure handle {@link IOException} on challenge.
	 */
	public void testChallengeIoException() throws Exception {
		IOException ioException = new IOException("TEST");
		this.source.ioException = ioException;
		this.doChallengeTest(false, ioException);
	}

	/**
	 * Ensure handle {@link RuntimeException} on challenge.
	 */
	public void testChallengeFailure() throws Exception {
		RuntimeException failure = new RuntimeException("TEST");
		this.source.failure = failure;
		this.doChallengeTest(false, failure);
	}

	/**
	 * Undertakes the challenge test.
	 * 
	 * @param isInvokeFlow
	 *            Indicates whether to invoke the flow.
	 * @param exception
	 *            {@link Exception} in undertaking challenge.
	 */
	private void doChallengeTest(boolean isInvokeFlow, Throwable exception)
			throws Exception {

		// Record saving request state
		HttpUrlContinuationTest.recordSaveRequest("CHALLENGE_STATE",
				this.connection, this.requestState, this.session, this);

		// Record challenge dependencies
		this.recordReturn(this.taskContext, this.taskContext.getObject(1),
				this.connection);
		this.recordReturn(this.taskContext, this.taskContext.getObject(2),
				this.session);
		this.recordReturn(this.taskContext, this.taskContext.getObject(3),
				this.store);

		// Determine if exception
		if (exception != null) {
			this.taskContext.doFlow(0, exception);
		}

		// Determine if application specific flow
		if (isInvokeFlow) {
			this.source.isDoChallengeFlow = true;
			this.taskContext.doFlow(1, null);
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
	public void testApplicationAuthenticate() throws Throwable {
		final HttpSecurity security = this.createMock(HttpSecurity.class);
		this.recordApplicationAuthenticationDependencies();
		this.source.security = security;
		HttpUrlContinuationTest.recordReinstateRequest(true, "CHALLENGE_STATE",
				this.connection, this.requestState, this.session, this);
		this.taskContext.doFlow(0, null);
		this.doApplicationAuthentication();
	}

	/**
	 * Ensure triggers challenge by throwing exception when not authenticated.
	 */
	public void testNotApplicationAuthenticate() throws Throwable {
		this.recordApplicationAuthenticationDependencies();
		try {
			this.doApplicationAuthentication();
			fail("Should not be successful");
		} catch (HttpAuthenticationRequiredException ex) {
			// Correct failure
		}
	}

	/**
	 * Ensure triggers failure flow if no request to reinstate after
	 * authenticating.
	 */
	public void testApplicationAuthenticateWithoutChallengeRequestState()
			throws Throwable {
		final HttpSecurity security = this.createMock(HttpSecurity.class);
		this.recordApplicationAuthenticationDependencies();
		this.source.security = security;
		HttpUrlContinuationTest.recordReinstateRequest(false,
				"CHALLENGE_STATE", this.connection, this.requestState,
				this.session, this);
		this.taskContext.doFlow(1, null);
		this.control(this.taskContext).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect flow index", expected[0], actual[0]);
				assertTrue("Incorrect cause", (actual[1] instanceof String));
				return true;
			}
		});
		this.doApplicationAuthentication();
	}

	/**
	 * Ensure triggers failure flow if {@link IOException} in authentication.
	 */
	public void testApplicationAuthenticateIoException() throws Throwable {
		final IOException exception = new IOException("TEST");
		this.recordApplicationAuthenticationDependencies();
		this.source.ioException = exception;
		this.taskContext.doFlow(1, exception);
		this.doApplicationAuthentication();
	}

	/**
	 * Ensure triggers failure flow in failure in authentication.
	 */
	public void testApplicationAuthenticateFailure() throws Throwable {
		final RuntimeException exception = new RuntimeException("TEST");
		this.recordApplicationAuthenticationDependencies();
		this.source.failure = exception;
		this.taskContext.doFlow(1, exception);
		this.doApplicationAuthentication();
	}

	/**
	 * Records obtaining dependencies from the {@link TaskContext} for
	 * application authentication.
	 */
	private void recordApplicationAuthenticationDependencies() {
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				this.credentials);
		this.recordReturn(this.taskContext, this.taskContext.getObject(1),
				this.connection);
		this.recordReturn(this.taskContext, this.taskContext.getObject(2),
				this.session);
		this.recordReturn(this.taskContext, this.taskContext.getObject(3),
				this.requestState);
		this.recordReturn(this.taskContext, this.taskContext.getObject(4),
				this.store);
	}

	/**
	 * Undertakes application authentication.
	 */
	private void doApplicationAuthentication() throws Throwable {
		this.replayMockObjects();
		try {
			Task<HttpSecurityWork, Dependencies, Flows> task = this
					.createTask("APPLICATION_AUTHENTICATE");
			task.doTask(this.taskContext);
		} finally {
			this.verifyMockObjects();
		}
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
		String key = HttpSecurityConfigurator
				.registerHttpSecuritySource(this.source);

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
			// Always enough information for authentication
			return true;
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
	}

}