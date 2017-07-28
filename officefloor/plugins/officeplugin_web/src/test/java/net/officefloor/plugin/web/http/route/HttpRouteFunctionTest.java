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

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.OfficeAwareManagedFunctionFactory;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
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
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteFunctionDependencies;
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteFunctionFlows;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link HttpRouteFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteFunctionTest extends OfficeFrameTestCase {

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
	public static void recordDoRedirect(String uriPath, boolean isSecure, ServerHttpConnection connection,
			HttpRequestState requestState, HttpSession session, HttpApplicationLocation location,
			OfficeFrameTestCase test) {
		try {

			final String redirectUrl = "http://redirect/context" + uriPath;
			final HttpResponse response = test.createMock(HttpResponse.class);
			final HttpHeader header = test.createMock(HttpHeader.class);

			// Record the redirect
			test.recordReturn(location, location.transformToClientPath(uriPath, isSecure), redirectUrl);
			HttpUrlContinuationTest.recordSaveRequest("_OfficeFloorRedirectedRequest_", connection, requestState,
					session, test);
			test.recordReturn(connection, connection.getHttpResponse(), response);
			response.setStatus(303); // Status = See other
			test.recordReturn(response,
					response.addHeader("Location", redirectUrl + HttpRouteFunction.REDIRECT_URI_SUFFIX), header);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Ensure issue if duplicate URI path within the {@link Office}.
	 */
	public void testDuplicateUri() throws Throwable {

		// Record duplicate URI paths
		this.recordUrlContinuations("ONE", "/same/path", null, "TWO", "/same/path", null);

		// Test
		this.replayMockObjects();
		try {
			this.createHttpRouteFunction();
			fail("Should not be successful");
		} catch (DuplicateHttpUrlContinuationException ex) {
			assertEquals("Incorrect cause", "HTTP URL continuation path '/same/path' used for more than one Task",
					ex.getMessage());
		}
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if duplicate resolved URI path within the {@link Office}.
	 */
	public void testDuplicateResolvedUri() throws Throwable {

		// Record duplicate URI paths
		this.recordUrlContinuations("ONE", "same/path", null, "TWO", "/same/../same/path", null);

		// Test
		this.replayMockObjects();
		try {
			this.createHttpRouteFunction();
			fail("Should not be successful");
		} catch (DuplicateHttpUrlContinuationException ex) {
			assertEquals("Incorrect cause", "HTTP URL continuation path '/same/path' used for more than one Task",
					ex.getMessage());
		}
		this.verifyMockObjects();
	}

	/**
	 * Ensure not handled if missing required context path.
	 */
	public void testNotHandledIfMissingContextPath() throws Throwable {

		// Record missing context path
		this.recordUrlContinuations("ONE", "/path", null);
		this.recordDependencies();
		this.recordReturn(this.connection, this.connection.getHttpRequest(), this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), "/path");
		this.location.transformToApplicationCanonicalPath("/path");
		this.control(this.location).setThrowable(
				new IncorrectHttpRequestContextPathException(HttpStatus.SC_NOT_FOUND, "Must have context path"));
		this.context.doFlow(HttpRouteFunctionFlows.NOT_HANDLED, null, null);

		// Test
		this.replayMockObjects();
		HttpRouteFunction task = this.createHttpRouteFunction();
		task.execute(this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure invokes unhandled flow on unhandled {@link HttpRequest}.
	 */
	public void testUnhandledNonSecureRequest() throws Throwable {
		this.doRouteTest("/unhandled", null, false, "task", "/unused", Boolean.TRUE);
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
		this.doRouteTest("/path", "task", false, "task", "/ignore/../path/", null);
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
		this.doRouteTest("/redirect", "redirect", false, "redirect", "/redirect", Boolean.TRUE);
	}

	/**
	 * Ensure redirect {@link HttpRequest} if not to be secure
	 * {@link ServerHttpConnection}.
	 */
	public void testRedirectAsSecure() throws Throwable {
		this.doRouteTest("/redirect", "redirect", true, "redirect", "/redirect", Boolean.FALSE);
	}

	/**
	 * Ensure services redirected {@link HttpRequest}.
	 */
	public void testServiceRedirectedRequest() throws Throwable {
		this.doRouteTest("/redirect" + HttpRouteFunction.REDIRECT_URI_SUFFIX, "redirect", true, "redirect", "/redirect",
				Boolean.TRUE);
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
	private final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);

	/**
	 * {@link HttpApplicationLocation}.
	 */
	private final HttpApplicationLocation location = this.createMock(HttpApplicationLocation.class);

	/**
	 * {@link HttpRequestState}.
	 */
	private final HttpRequestState requestState = this.createMock(HttpRequestState.class);

	/**
	 * {@link HttpRouteFunction}.
	 */
	private final HttpSession session = this.createMock(HttpSession.class);

	/**
	 * {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionContext<HttpRouteFunctionDependencies, HttpRouteFunctionFlows> context = this
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
	private void doRouteTest(String uriPath, String expectedTaskName, boolean isConnectionSecure,
			Object... taskNameThenUriPathThenIsSecureGroupings) throws Throwable {

		// Record the office URL continuations
		this.recordUrlContinuations(taskNameThenUriPathThenIsSecureGroupings);

		// Obtain the URL servicer expected to service request
		UrlServicer urlServicer = null;
		if (expectedTaskName != null) {
			urlServicer = this.urlServicers.get(expectedTaskName);
			assertNotNull("Unknown expected task '" + expectedTaskName + "'", urlServicer);
		}

		// Record initial servicing
		this.recordInitialServicing(uriPath, isConnectionSecure);

		// Determine if expecting servicing
		boolean isRequireRedirect = false;
		if (urlServicer != null) {

			// Determine if require redirect (as not appropriately secure)
			if ((urlServicer.isSecure != null) && (urlServicer.isSecure.booleanValue() != isConnectionSecure)) {

				// Require redirect
				isRequireRedirect = true;

				// Record the redirect
				recordDoRedirect(uriPath, urlServicer.isSecure.booleanValue(), this.connection, this.requestState,
						this.session, this.location, this);

				// Record initial servicing of redirect
				this.recordInitialServicing(uriPath, urlServicer.isSecure.booleanValue());
			}

			// Record servicing the request
			this.context.doFlow(expectedTaskName, expectedTaskName, null);

		} else {
			// Not handled URI path
			this.context.doFlow(HttpRouteFunctionFlows.NOT_HANDLED, null, null);
		}

		// Test
		this.replayMockObjects();

		// Create task and undertake initial request
		HttpRouteFunction task = this.createHttpRouteFunction();
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
		this.recordReturn(this.context, this.context.getObject(HttpRouteFunctionDependencies.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.context, this.context.getObject(HttpRouteFunctionDependencies.HTTP_APPLICATION_LOCATION),
				this.location);
		this.recordReturn(this.context, this.context.getObject(HttpRouteFunctionDependencies.REQUEST_STATE),
				this.requestState);
		this.recordReturn(this.context, this.context.getObject(HttpRouteFunctionDependencies.HTTP_SESSION),
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
	private void recordInitialServicing(String uriPath, boolean isConnectionSecure) throws Exception {

		final String requestUri = "/context" + uriPath;

		// Record dependencies
		this.recordDependencies();

		// Record servicing request
		this.recordReturn(this.connection, this.connection.getHttpRequest(), this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), requestUri);
		if (requestUri.endsWith(HttpRouteFunction.REDIRECT_URI_SUFFIX)) {
			// Record servicing redirected request
			HttpUrlContinuationTest.recordReinstateRequest(true, "_OfficeFloorRedirectedRequest_", this.connection,
					this.requestState, this.session, this);
		}
		this.recordReturn(this.location, this.location.transformToApplicationCanonicalPath(requestUri), uriPath);
		this.recordReturn(this.connection, this.connection.isSecure(), isConnectionSecure);
	}

	/**
	 * Creates the {@link HttpRouteFunction}.
	 * 
	 * @return {@link HttpRouteFunction}.
	 */
	private HttpRouteFunction createHttpRouteFunction() throws Throwable {

		// Load the managed function source and create the function
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil
				.loadManagedFunctionType(HttpRouteManagedFunctionSource.class);
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];

		// Make Office aware
		ManagedFunctionFactory<?, ?> factory = functionType.getManagedFunctionFactory();
		assertTrue("Should be Office aware", factory instanceof OfficeAwareManagedFunctionFactory);
		((OfficeAwareManagedFunctionFactory<?, ?>) factory).setOffice(office);

		// Create the function
		ManagedFunction<?, ?> function = factory.createManagedFunction();

		// Return the task
		return (HttpRouteFunction) function;
	}

	/**
	 * Records the URL continuation {@link FunctionManager} configuration.
	 * 
	 * @param functionNameThenUriPathThenIsSecureGroupings
	 *            Listing of the following sequence grouping of values:
	 *            <ol>
	 *            <li>{@link ManagedFunction} name</li>
	 *            <li>Application URI path</li>
	 *            <li>Is Secure flag</li>
	 *            </ol>
	 */
	private void recordUrlContinuations(Object... functionNameThenUriPathThenIsSecureGroupings) throws Exception {

		// Create the listing of URL servicers
		int urlServicerCount = (functionNameThenUriPathThenIsSecureGroupings.length / 3);
		String[] functionNames = new String[urlServicerCount];
		UrlServicer[] urlServicerList = new UrlServicer[urlServicerCount];

		// Create and register the URL servicers
		for (int i = 0; i < functionNameThenUriPathThenIsSecureGroupings.length; i += 3) {

			// Obtain the details for the current grouping
			String functionName = (String) functionNameThenUriPathThenIsSecureGroupings[i];
			String applicationUriPath = (String) functionNameThenUriPathThenIsSecureGroupings[i + 1];
			Boolean isSecure = (Boolean) functionNameThenUriPathThenIsSecureGroupings[i + 2];

			// Create the URL Servicer
			FunctionManager functionManager = this.createMock(FunctionManager.class);
			UrlServicer urlServicer = new UrlServicer(functionManager, applicationUriPath, isSecure);

			// Load the details for recording
			int index = i / 3;
			functionNames[index] = functionName;
			urlServicerList[index] = urlServicer;

			// Register the URL servicer
			this.urlServicers.put(functionName, urlServicer);
		}

		// Record the office URL continuations
		for (int i = 0; i < functionNames.length; i++) {
			String functionName = functionNames[i];
			UrlServicer urlServicer = urlServicerList[i];

			// Record the function
			FunctionManager functionManager = this.createMock(FunctionManager.class);
			this.recordReturn(this.office, this.office.getFunctionManager(functionName), functionManager);

			// Record the task list
			final String NON_URL_CONTINUATION_FUNCTION_NAME = "NonUrlContinuation";
			final String OTHER_DIFFERENTIATOR_FUNCTION_NAME = "OtherDifferentiator";
			this.recordReturn(functionManager, this.office.getFunctionNames(), new String[] {
					NON_URL_CONTINUATION_FUNCTION_NAME, OTHER_DIFFERENTIATOR_FUNCTION_NAME, functionName });

			// Record the non URL continuation task
			FunctionManager taskManager = this.createMock(FunctionManager.class);
			this.recordReturn(functionManager, this.office.getFunctionManager(NON_URL_CONTINUATION_FUNCTION_NAME),
					taskManager);
			this.recordReturn(taskManager, taskManager.getDifferentiator(), null);

			// Record the other differentiator task
			this.recordReturn(functionManager, this.office.getFunctionManager(OTHER_DIFFERENTIATOR_FUNCTION_NAME),
					taskManager);
			this.recordReturn(taskManager, taskManager.getDifferentiator(), "NotUrlContinuation");

			// Record the URL continuation
			HttpUrlContinuationDifferentiator urlContinuation = this
					.createMock(HttpUrlContinuationDifferentiator.class);
			this.recordReturn(functionManager, this.office.getFunctionManager(functionName),
					urlServicer.functionManager);
			this.recordReturn(urlServicer.functionManager, urlServicer.functionManager.getDifferentiator(),
					urlContinuation);
			this.recordReturn(urlContinuation, urlContinuation.getApplicationUriPath(), urlServicer.applicationUriPath);
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
		public final FunctionManager functionManager;

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
		public UrlServicer(FunctionManager taskManager, String applicationUriPath, Boolean isSecure) {
			this.functionManager = taskManager;
			this.applicationUriPath = applicationUriPath;
			this.isSecure = isSecure;
		}
	}

}