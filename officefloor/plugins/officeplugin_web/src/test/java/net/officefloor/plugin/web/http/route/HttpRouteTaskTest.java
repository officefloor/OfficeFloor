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
package net.officefloor.plugin.web.http.route;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.continuation.DuplicateHttpUrlContinuationException;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationDifferentiator;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.IncorrectHttpRequestContextPathException;
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteTaskDependencies;
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteTaskFlows;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link HttpRouteFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteTaskTest extends OfficeFrameTestCase {

	/**
	 * Records undertaking a redirect.
	 * 
	 * @param uriPath
	 *            URI path.
	 * @param isSecure
	 *            Indicates if requires secure.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param requestState
	 *            {@link HttpRequestState}.
	 * @param session
	 *            {@link HttpSession}.
	 * @param location
	 *            {@link HttpApplicationLocation}.
	 * @param test
	 *            {@link OfficeFrameTestCase}.
	 */
	public static void recordDoRedirect(String uriPath, boolean isSecure,
			ServerHttpConnection connection, HttpRequestState requestState,
			HttpSession session, HttpApplicationLocation location,
			OfficeFrameTestCase test) {
		try {

			final String redirectUrl = "http://redirect/context" + uriPath;
			final HttpResponse response = test.createMock(HttpResponse.class);
			final HttpHeader header = test.createMock(HttpHeader.class);

			// Record the redirect
			test.recordReturn(location,
					location.transformToClientPath(uriPath, isSecure),
					redirectUrl);
			HttpUrlContinuationTest.recordSaveRequest(
					"_OfficeFloorRedirectedRequest_", connection, requestState,
					session, test);
			test.recordReturn(connection, connection.getHttpResponse(),
					response);
			response.setStatus(303); // Status = See other
			test.recordReturn(
					response,
					response.addHeader("Location", redirectUrl
							+ HttpRouteFunction.REDIRECT_URI_SUFFIX), header);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Ensure issue if duplicate URI path within the {@link Office}.
	 */
	public void testDuplicateUri() throws Exception {

		// Record duplicate URI paths
		this.recordUrlContinuations("ONE", "/same/path", null, "TWO",
				"/same/path", null);

		// Test
		this.replayMockObjects();
		try {
			this.createHttpRouteTask();
			fail("Should not be successful");
		} catch (DuplicateHttpUrlContinuationException ex) {
			assertEquals(
					"Incorrect cause",
					"HTTP URL continuation path '/same/path' used for more than one Task",
					ex.getMessage());
		}
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if duplicate resolved URI path within the {@link Office}.
	 */
	public void testDuplicateResolvedUri() throws Exception {

		// Record duplicate URI paths
		this.recordUrlContinuations("ONE", "same/path", null, "TWO",
				"/same/../same/path", null);

		// Test
		this.replayMockObjects();
		try {
			this.createHttpRouteTask();
			fail("Should not be successful");
		} catch (DuplicateHttpUrlContinuationException ex) {
			assertEquals(
					"Incorrect cause",
					"HTTP URL continuation path '/same/path' used for more than one Task",
					ex.getMessage());
		}
		this.verifyMockObjects();
	}

	/**
	 * Ensure not handled if missing required context path.
	 */
	public void testNotHandledIfMissingContextPath() throws Exception {

		// Record missing context path
		this.recordUrlContinuations("ONE", "/path", null);
		this.recordDependencies();
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), "/path");
		this.location.transformToApplicationCanonicalPath("/path");
		this.control(this.location).setThrowable(
				new IncorrectHttpRequestContextPathException(
						HttpStatus.SC_NOT_FOUND, "Must have context path"));
		this.recordReturn(this.context,
				this.context.doFlow(HttpRouteTaskFlows.NOT_HANDLED, null), null);

		// Test
		this.replayMockObjects();
		HttpRouteFunction task = this.createHttpRouteTask();
		task.execute(this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure invokes unhandled flow on unhandled {@link HttpRequest}.
	 */
	public void testUnhandledNonSecureRequest() throws Throwable {
		this.doRouteTest("/unhandled", null, false, "task", "/unused",
				Boolean.TRUE);
	}

	/**
	 * Ensure invokes unhandled flow on unhandled {@link HttpRequest}.
	 */
	public void testUnhandledSecureRequest() throws Throwable {
		this.doRouteTest("/unhandled", null, true, "task", "/unused", null);
	}

	/**
	 * Ensure service {@link HttpRequest} as a secure
	 * {@link ServerHttpConnection}.
	 */
	public void testServiceAsSecure() throws Throwable {
		this.doRouteTest("/path", "task", true, "task", "/path", Boolean.TRUE);
	}

	/**
	 * Ensure service {@link HttpRequest} as not secure
	 * {@link ServerHttpConnection}.
	 */
	public void testServiceAsNotSecure() throws Throwable {
		this.doRouteTest("/path", "task", false, "task", "/path", Boolean.FALSE);
	}

	/**
	 * Ensure service {@link HttpRequest} as no secure configuration specified.
	 * This will typically be to service resources.
	 */
	public void testSecureServiceAsNotSpecified() throws Throwable {
		this.doRouteTest("/path", "task", true, "task", "/path", null);
	}

	/**
	 * Ensure service {@link HttpRequest} as no secure configuration specified.
	 * This will typically be to service resources.
	 */
	public void testNonSecureServiceAsNotSpecified() throws Throwable {
		this.doRouteTest("/path", "task", false, "task", "/path", null);
	}

	/**
	 * Ensure convert {@link HttpUrlContinuationDifferentiator} URI path to
	 * absolute path.
	 */
	public void testAbsolutePath() throws Throwable {
		this.doRouteTest("/path", "task", false, "task", "path", null);
	}

	/**
	 * Ensure convert {@link HttpUrlContinuationDifferentiator} URI path to
	 * canonical path.
	 */
	public void testCanonicalPath() throws Throwable {
		this.doRouteTest("/path", "task", false, "task", "/ignore/../path/",
				null);
	}

	/**
	 * Ensure can service root path.
	 */
	public void testRootPath() throws Throwable {
		this.doRouteTest("/", "task", false, "task", "/", null);
	}

	/**
	 * Ensure redirect {@link HttpRequest} if requires secure
	 * {@link ServerHttpConnection}.
	 */
	public void testRedirectAsNotSecure() throws Throwable {
		this.doRouteTest("/redirect", "redirect", false, "redirect",
				"/redirect", Boolean.TRUE);
	}

	/**
	 * Ensure redirect {@link HttpRequest} if not to be secure
	 * {@link ServerHttpConnection}.
	 */
	public void testRedirectAsSecure() throws Throwable {
		this.doRouteTest("/redirect", "redirect", true, "redirect",
				"/redirect", Boolean.FALSE);
	}

	/**
	 * Ensure services redirected {@link HttpRequest}.
	 */
	public void testServiceRedirectedRequest() throws Throwable {
		this.doRouteTest("/redirect" + HttpRouteFunction.REDIRECT_URI_SUFFIX,
				"redirect", true, "redirect", "/redirect", Boolean.TRUE);
	}

	/*
	 * =================== Helpers ============================
	 */

	/**
	 * {@link Office}.
	 */
	private final Office office = this.createMock(Office.class);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * {@link HttpApplicationLocation}.
	 */
	private final HttpApplicationLocation location = this
			.createMock(HttpApplicationLocation.class);

	/**
	 * {@link HttpRequestState}.
	 */
	private final HttpRequestState requestState = this
			.createMock(HttpRequestState.class);

	/**
	 * {@link HttpRouteFunction}.
	 */
	private final HttpSession session = this.createMock(HttpSession.class);

	/**
	 * {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionContext<HttpRouteFunction, HttpRouteTaskDependencies, HttpRouteTaskFlows> context = this
			.createMock(ManagedFunctionContext.class);

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * {@link UrlServicer} instances by their {@link ManagedFunction} name.
	 */
	private final Map<String, UrlServicer> urlServicers = new HashMap<String, UrlServicer>();

	/**
	 * Ensure routing of {@link HttpRequest}.
	 */
	private void doRouteTest(String uriPath, String expectedTaskName,
			boolean isConnectionSecure,
			Object... taskNameThenUriPathThenIsSecureGroupings)
			throws Throwable {

		final FlowFuture flowFuture = this.createMock(FlowFuture.class);

		// Record the office URL continuations
		this.recordUrlContinuations(taskNameThenUriPathThenIsSecureGroupings);

		// Obtain the URL servicer expected to service request
		UrlServicer urlServicer = null;
		if (expectedTaskName != null) {
			urlServicer = this.urlServicers.get(expectedTaskName);
			assertNotNull("Unknown expected task '" + expectedTaskName + "'",
					urlServicer);
		}

		// Record initial servicing
		this.recordInitialServicing(uriPath, isConnectionSecure);

		// Determine if expecting servicing
		boolean isRequireRedirect = false;
		if (urlServicer != null) {

			// Determine if require redirect (as not appropriately secure)
			if ((urlServicer.isSecure != null)
					&& (urlServicer.isSecure.booleanValue() != isConnectionSecure)) {

				// Require redirect
				isRequireRedirect = true;

				// Record the redirect
				recordDoRedirect(uriPath, urlServicer.isSecure.booleanValue(),
						this.connection, this.requestState, this.session,
						this.location, this);

				// Record initial servicing of redirect
				this.recordInitialServicing(uriPath,
						urlServicer.isSecure.booleanValue());
			}

			// Record servicing the request
			this.context.doFlow(expectedTaskName, expectedTaskName, null);

		} else {
			// Not handled URI path
			this.recordReturn(this.context,
					this.context.doFlow(HttpRouteTaskFlows.NOT_HANDLED, null),
					flowFuture);
		}

		// Test
		this.replayMockObjects();

		// Create task and undertake initial request
		HttpRouteFunction task = this.createHttpRouteTask();
		task.execute(this.context);

		// Undertake request for redirect (if necessary)
		if (isRequireRedirect) {
			task.execute(this.context);
		}

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Records obtaining the dependencies.
	 */
	private void recordDependencies() {
		this.recordReturn(this.context, this.context
				.getObject(HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(
				this.context,
				this.context
						.getObject(HttpRouteTaskDependencies.HTTP_APPLICATION_LOCATION),
				this.location);
		this.recordReturn(
				this.context,
				this.context.getObject(HttpRouteTaskDependencies.REQUEST_STATE),
				this.requestState);
		this.recordReturn(this.context,
				this.context.getObject(HttpRouteTaskDependencies.HTTP_SESSION),
				this.session);
	}

	/**
	 * Record the initial servicing of the {@link HttpRequest}.
	 * 
	 * @param uriPath
	 *            URI path.
	 * @param isConnectionSecure
	 *            Indicates whether the {@link ServerHttpConnection} is secure.
	 */
	private void recordInitialServicing(String uriPath,
			boolean isConnectionSecure) throws Exception {

		final String requestUri = "/context" + uriPath;

		// Record dependencies
		this.recordDependencies();

		// Record servicing request
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(),
				requestUri);
		if (requestUri.endsWith(HttpRouteFunction.REDIRECT_URI_SUFFIX)) {
			// Record servicing redirected request
			HttpUrlContinuationTest.recordReinstateRequest(true,
					"_OfficeFloorRedirectedRequest_", this.connection,
					this.requestState, this.session, this);
		}
		this.recordReturn(this.location,
				this.location.transformToApplicationCanonicalPath(requestUri),
				uriPath);
		this.recordReturn(this.connection, this.connection.isSecure(),
				isConnectionSecure);
	}

	/**
	 * Creates the {@link HttpRouteFunction}.
	 * 
	 * @return {@link HttpRouteFunction}.
	 */
	private HttpRouteFunction createHttpRouteTask() throws Exception {

		// Load the work source and create the task
		FunctionNamespaceType<HttpRouteFunction> workType = WorkLoaderUtil
				.loadWorkType(HttpRouteWorkSource.class);
		ManagedFunctionType<HttpRouteFunction, ?, ?> taskType = workType.getManagedFunctionTypes()[0];
		HttpRouteFunction workFactory = workType.getWorkFactory().createWork();

		// Make Office aware
		workFactory.setOffice(this.office);

		// Create the task
		ManagedFunction<HttpRouteFunction, ?, ?> task = taskType.getManagedFunctionFactory().createManagedFunction(
				workFactory);

		// Return the task
		return (HttpRouteFunction) task;
	}

	/**
	 * Records the URL continuation {@link FunctionManager} configuration.
	 * 
	 * @param taskNameThenUriPathThenIsSecureGroupings
	 *            Listing of the following sequence grouping of values:
	 *            <ol>
	 *            <li>{@link ManagedFunction} name</li>
	 *            <li>Application URI path</li>
	 *            <li>Is Secure flag</li>
	 *            </ol>
	 */
	private void recordUrlContinuations(
			Object... taskNameThenUriPathThenIsSecureGroupings)
			throws Exception {

		// Create the listing of URL servicers
		int urlServicerCount = (taskNameThenUriPathThenIsSecureGroupings.length / 3);
		String[] taskNames = new String[urlServicerCount];
		UrlServicer[] urlServicerList = new UrlServicer[urlServicerCount];

		// Create and register the URL servicers
		for (int i = 0; i < taskNameThenUriPathThenIsSecureGroupings.length; i += 3) {

			// Obtain the details for the current grouping
			String taskName = (String) taskNameThenUriPathThenIsSecureGroupings[i];
			String applicationUriPath = (String) taskNameThenUriPathThenIsSecureGroupings[i + 1];
			Boolean isSecure = (Boolean) taskNameThenUriPathThenIsSecureGroupings[i + 2];

			// Create the URL Servicer
			FunctionManager taskManager = this.createMock(FunctionManager.class);
			UrlServicer urlServicer = new UrlServicer(taskManager,
					applicationUriPath, isSecure);

			// Load the details for recording
			int index = i / 3;
			taskNames[index] = taskName;
			urlServicerList[index] = urlServicer;

			// Register the URL servicer
			this.urlServicers.put(taskName, urlServicer);
		}

		// Record the office work (using same work name as task name)
		this.recordReturn(this.office, this.office.getWorkNames(), taskNames);

		// Record the office URL continuations
		for (int i = 0; i < taskNames.length; i++) {
			String taskName = taskNames[i];
			UrlServicer urlServicer = urlServicerList[i];

			// Record the work
			WorkManager workManager = this.createMock(WorkManager.class);
			this.recordReturn(this.office,
					this.office.getWorkManager(taskName), workManager);

			// Record the task list
			final String NON_URL_CONTINUATION_TASK_NAME = "NonUrlContinuation";
			final String OTHER_DIFFERENTIATOR_TASK_NAME = "OtherDifferentiator";
			this.recordReturn(workManager, workManager.getTaskNames(),
					new String[] { NON_URL_CONTINUATION_TASK_NAME,
							OTHER_DIFFERENTIATOR_TASK_NAME, taskName });

			// Record the non URL continuation task
			FunctionManager taskManager = this.createMock(FunctionManager.class);
			this.recordReturn(workManager,
					workManager.getTaskManager(NON_URL_CONTINUATION_TASK_NAME),
					taskManager);
			this.recordReturn(taskManager, taskManager.getDifferentiator(),
					null);

			// Record the other differentiator task
			this.recordReturn(workManager,
					workManager.getTaskManager(OTHER_DIFFERENTIATOR_TASK_NAME),
					taskManager);
			this.recordReturn(taskManager, taskManager.getDifferentiator(),
					"NotUrlContinuation");

			// Record the URL continuation
			HttpUrlContinuationDifferentiator urlContinuation = this
					.createMock(HttpUrlContinuationDifferentiator.class);
			this.recordReturn(workManager,
					workManager.getTaskManager(taskName),
					urlServicer.taskManager);
			this.recordReturn(urlServicer.taskManager,
					urlServicer.taskManager.getDifferentiator(),
					urlContinuation);
			this.recordReturn(urlContinuation,
					urlContinuation.getApplicationUriPath(),
					urlServicer.applicationUriPath);
			urlContinuation.isSecure();
			this.control(urlContinuation).setReturnValue(urlServicer.isSecure);
		}
	}

	/**
	 * URL servicer.
	 */
	private static class UrlServicer {

		/**
		 * {@link FunctionManager}.
		 */
		public final FunctionManager taskManager;

		/**
		 * Application URI path.
		 */
		public final String applicationUriPath;

		/**
		 * Indicates if secure.
		 */
		public final Boolean isSecure;

		/**
		 * Initiate.
		 * 
		 * @param taskManager
		 *            {@link FunctionManager}.
		 * @param applicationUriPath
		 *            Application URI path.
		 * @param isSecure
		 *            Indicates if secure.
		 */
		public UrlServicer(FunctionManager taskManager, String applicationUriPath,
				Boolean isSecure) {
			this.taskManager = taskManager;
			this.applicationUriPath = applicationUriPath;
			this.isSecure = isSecure;
		}
	}

}