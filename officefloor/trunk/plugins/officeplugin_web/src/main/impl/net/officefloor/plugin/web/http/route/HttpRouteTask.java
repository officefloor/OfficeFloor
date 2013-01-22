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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeAwareWorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.continuation.DuplicateHttpUrlContinuationException;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationDifferentiator;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationMangedObject;
import net.officefloor.plugin.web.http.location.IncorrectHttpRequestContextPathException;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokenAdapter;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserImpl;

/**
 * {@link Task} for routing {@link HttpRequest} instances and ensuring
 * appropriately secure {@link ServerHttpConnection} for servicing the
 * {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteTask
		extends
		AbstractSingleTask<HttpRouteTask, HttpRouteTask.HttpRouteTaskDependencies, HttpRouteTask.HttpRouteTaskFlows>
		implements OfficeAwareWorkFactory<HttpRouteTask> {

	/**
	 * <p>
	 * Undertakes a redirect.
	 * <p>
	 * This may be utilised by any {@link Task} with a
	 * {@link HttpUrlContinuationDifferentiator} to trigger a redirect that will
	 * have the {@link HttpRequest} state appropriately managed across the
	 * redirect by the {@link HttpRouteTask}.
	 * 
	 * @param applicationUriPath
	 *            Application URI path.
	 * @param isSecure
	 *            Whether a secure {@link ServerHttpConnection} is required.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param location
	 *            {@link HttpApplicationLocation}.
	 * @param requestState
	 *            {@link HttpRequestState}.
	 * @param session
	 *            {@link HttpSession}.
	 * @throws IOException
	 *             If unable to undertake redirect.
	 */
	public static void doRedirect(String applicationUriPath, boolean isSecure,
			ServerHttpConnection connection, HttpApplicationLocation location,
			HttpRequestState requestState, HttpSession session)
			throws IOException {

		// Require redirect, so determine the redirect URL
		String redirectUrl = location.transformToClientPath(applicationUriPath,
				isSecure);

		// Save the request state
		HttpUrlContinuation.saveRequest(SESSION_REDIRECTED_REQUEST, connection,
				requestState, session);

		// Send redirect
		HttpResponse response = connection.getHttpResponse();
		response.setStatus(HttpStatus.SC_SEE_OTHER);
		response.addHeader("Location", redirectUrl + REDIRECT_URI_SUFFIX);
	}

	/**
	 * Dependencies for the {@link HttpRouteTask}.
	 */
	public static enum HttpRouteTaskDependencies {
		SERVER_HTTP_CONNECTION, HTTP_APPLICATION_LOCATION, REQUEST_STATE, HTTP_SESSION
	}

	/**
	 * Flows for the {@link HttpRouteTask}.
	 */
	public static enum HttpRouteTaskFlows {
		NOT_HANDLED
	}

	/**
	 * Suffix on the redirect {@link HttpRequest} URI to indicate a redirect has
	 * occurred.
	 */
	public static final String REDIRECT_URI_SUFFIX = "?ofr";

	/**
	 * {@link HttpSession} attribute name to obtain the redirected
	 * {@link HttpRequest} state momento.
	 */
	private static final String SESSION_REDIRECTED_REQUEST = "_OfficeFloorRedirectedRequest_";

	/**
	 * URL continuations by application URI path.
	 */
	private Map<String, HttpUrlContinuation> urlContinuations;

	/*
	 * ================ OfficeAwareWorkFactory ====================
	 */

	@Override
	public void setOffice(Office office) throws Exception {

		// Obtain the URL continuations
		Map<String, HttpUrlContinuation> continuations = new HashMap<String, HttpUrlContinuation>();

		// Interrogate the Office for secure URI paths
		for (String workName : office.getWorkNames()) {
			WorkManager work = office.getWorkManager(workName);
			for (String taskName : work.getTaskNames()) {
				TaskManager task = work.getTaskManager(taskName);

				// Determine if secure URI differentiator
				Object differentiator = task.getDifferentiator();
				if (differentiator instanceof HttpUrlContinuationDifferentiator) {
					HttpUrlContinuationDifferentiator urlContinuationDifferentiator = (HttpUrlContinuationDifferentiator) differentiator;

					// Obtain the details of the continuation
					String applicationUriPath = urlContinuationDifferentiator
							.getApplicationUriPath();
					Boolean isSecure = urlContinuationDifferentiator.isSecure();

					// Must have application URI path
					if (applicationUriPath == null) {
						continue;
					}

					// Transform to absoluate canoncial path
					applicationUriPath = (applicationUriPath.startsWith("/") ? applicationUriPath
							: "/" + applicationUriPath);
					applicationUriPath = HttpApplicationLocationMangedObject
							.transformToCanonicalPath(applicationUriPath);

					// Ensure only one application URI path registered
					if (continuations.containsKey(applicationUriPath)) {
						throw new DuplicateHttpUrlContinuationException(
								"HTTP URL continuation path '"
										+ applicationUriPath
										+ "' used for more than one Task");
					}

					// Register the URL continuation
					continuations.put(applicationUriPath,
							new HttpUrlContinuation(workName, taskName,
									isSecure));
				}
			}
		}

		// Specify the URL continuations
		this.urlContinuations = continuations;
	}

	/*
	 * ======================== Task ==============================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpRouteTask, HttpRouteTaskDependencies, HttpRouteTaskFlows> context)
			throws InvalidHttpRequestUriException,
			HttpRequestTokeniseException, IOException, UnknownWorkException,
			UnknownTaskException, InvalidParameterTypeException {

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION);
		HttpApplicationLocation location = (HttpApplicationLocation) context
				.getObject(HttpRouteTaskDependencies.HTTP_APPLICATION_LOCATION);
		HttpRequestState requestState = (HttpRequestState) context
				.getObject(HttpRouteTaskDependencies.REQUEST_STATE);
		HttpSession session = (HttpSession) context
				.getObject(HttpRouteTaskDependencies.HTTP_SESSION);

		// Determine if redirect
		String path = connection.getHttpRequest().getRequestURI();
		if (path.endsWith(REDIRECT_URI_SUFFIX)) {
			// Redirect, so load previous request (if available)
			HttpUrlContinuation.reinstateRequest(SESSION_REDIRECTED_REQUEST,
					connection, requestState, session);
		}

		// Obtain the canonical path from request
		try {
			path = location.transformToApplicationCanonicalPath(path);
		} catch (IncorrectHttpRequestContextPathException ex) {
			// Missing context path, so not handled
			context.doFlow(HttpRouteTaskFlows.NOT_HANDLED, null);
			return null;
		}

		// Obtain the URI path only
		RequestPathHandler handler = new RequestPathHandler();
		new HttpRequestTokeniserImpl().tokeniseRequestURI(path, handler);
		path = handler.path;

		// Determine if secure connection
		boolean isConnectionSecure = connection.isSecure();

		// Obtain the URL continuation
		HttpUrlContinuation continuation = this.urlContinuations.get(path);
		if (continuation == null) {
			// Not handled request
			context.doFlow(HttpRouteTaskFlows.NOT_HANDLED, null);
			return null;
		}

		// Determine if require secure redirect
		Boolean isRequireSecure = continuation.isSecure();
		if ((isRequireSecure != null)
				&& (isRequireSecure.booleanValue() != isConnectionSecure)) {

			// Undertake the redirect
			doRedirect(path, isRequireSecure.booleanValue(), connection,
					location, requestState, session);
			return null;
		}

		// Service the request
		context.doFlow(continuation.getWorkName(), continuation.getTaskName(),
				null);
		return null;
	}

	/**
	 * {@link HttpRequestTokenAdapter} to obtain the {@link HttpRequest} path.
	 */
	private static class RequestPathHandler extends HttpRequestTokenAdapter {

		/**
		 * {@link HttpRequest} path.
		 */
		public String path = null;

		/*
		 * ===================== HttpRequestTokenAdapter =================
		 */

		@Override
		public void handlePath(String path) throws HttpRequestTokeniseException {
			this.path = path;
		}
	}

}