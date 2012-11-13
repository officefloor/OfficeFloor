/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.secure;

import java.util.Set;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokenAdapter;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserImpl;

/**
 * {@link Task} for ensuring appropriately secure {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecureTask
		extends
		AbstractSingleTask<HttpSecureTask, HttpSecureTask.HttpSecureTaskDependencies, HttpSecureTask.HttpSecureTaskFlows> {

	/**
	 * Dependencies for the {@link HttpSecureTask}.
	 */
	public static enum HttpSecureTaskDependencies {
		SERVER_HTTP_CONNECTION, HTTP_APPLICATION_LOCATION, HTTP_SESSION
	}

	/**
	 * Flows for the {@link HttpSecureTask}.
	 */
	public static enum HttpSecureTaskFlows {
		SERVICE
	}

	/**
	 * Secure paths.
	 */
	private final Set<String> securePaths;

	/**
	 * Initiate.
	 * 
	 * @param securePaths
	 *            Secure paths.
	 */
	public HttpSecureTask(Set<String> securePaths) {
		this.securePaths = securePaths;
	}

	/*
	 * ======================== Task ==============================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpSecureTask, HttpSecureTaskDependencies, HttpSecureTaskFlows> context)
			throws Throwable {

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpSecureTaskDependencies.SERVER_HTTP_CONNECTION);
		HttpApplicationLocation location = (HttpApplicationLocation) context
				.getObject(HttpSecureTaskDependencies.HTTP_APPLICATION_LOCATION);
		HttpSession session = (HttpSession) context
				.getObject(HttpSecureTaskDependencies.HTTP_SESSION);

		// Obtain the canonical path from request
		String path = connection.getHttpRequest().getRequestURI();
		path = location.transformToApplicationCanonicalPath(path);

		// Obtain the uri path only
		RequestPathHandler handler = new RequestPathHandler();
		new HttpRequestTokeniserImpl().tokeniseRequestURI(path, handler);
		path = handler.path;

		// Determine if required to be secure connection
		boolean isRequireSecure = this.securePaths.contains(path);

		// Determine if secure connection
		boolean isConnectionSecure = connection.isSecure();

		// Determine if may service request
		if (isRequireSecure == isConnectionSecure) {
			// Appropriately secure so service
			context.doFlow(HttpSecureTaskFlows.SERVICE, null);
			return null;
		}

		// Determine the redirect URL
		String redirectUrl = location.transformToClientPath(path,
				isRequireSecure);

		// Send redirect for making secure
		HttpResponse response = connection.getHttpResponse();
		response.setStatus(HttpStatus.SC_SEE_OTHER);
		response.addHeader("Location", redirectUrl);
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