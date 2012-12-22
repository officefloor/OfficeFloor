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
package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.route.HttpRouteTask;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Initial {@link Task} to ensure appropriate conditions for rendering the
 * {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInitialTask
		extends
		AbstractSingleTask<HttpTemplateInitialTask, HttpTemplateInitialTask.Dependencies, HttpTemplateInitialTask.Flows> {

	/**
	 * Keys for the {@link HttpTemplateInitialTask} dependencies.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION, HTTP_APPLICATION_LOCATION, HTTP_SESSION
	}

	/**
	 * Keys for the {@link HttpTemplateInitialTask} flows.
	 */
	public static enum Flows {
		RENDER
	}

	/**
	 * URI path for the {@link HttpTemplate}.
	 */
	private final String templateUriPath;

	/**
	 * Indicates if a secure {@link ServerHttpConnection} is required.
	 */
	private final boolean isRequireSecure;

	/**
	 * Initiate.
	 * 
	 * @param templateUriPath
	 *            URI path for the {@link HttpTemplate}.
	 * @param isRequireSecure
	 *            Indicates if a secure {@link ServerHttpConnection} is
	 *            required.
	 */
	public HttpTemplateInitialTask(String templateUriPath,
			boolean isRequireSecure) {
		this.templateUriPath = templateUriPath;
		this.isRequireSecure = isRequireSecure;
	}

	/*
	 * ======================= Task ===============================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpTemplateInitialTask, Dependencies, Flows> context)
			throws IOException {

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		HttpApplicationLocation location = (HttpApplicationLocation) context
				.getObject(Dependencies.HTTP_APPLICATION_LOCATION);
		HttpSession session = (HttpSession) context
				.getObject(Dependencies.HTTP_SESSION);

		// Flag indicating if redirect is required
		boolean isRedirectRequired = false;

		// Determine if requires a secure connection
		if (this.isRequireSecure) {

			/*
			 * Request may have come in on another URL continuation which did
			 * not require a secure connection and is to now to render this HTTP
			 * template. Therefore trigger redirect for a secure connection.
			 * 
			 * Note that do not down grade to non-secure connection as already
			 * have the request and no need to close the existing secure
			 * connection and establish a new non-secure connection.
			 */
			boolean isConnectionSecure = connection.isSecure();
			if (!isConnectionSecure) {
				// Flag redirect for secure connection
				isRedirectRequired = true;
			}
		}

		// Determine if POST/redirect/GET pattern to be applied
		if (!isRedirectRequired) {
			// Request likely overridden to POST, so use client HTTP method
			String method = connection.getHttpMethod();
			if ("POST".equalsIgnoreCase(method)) {
				// Flag redirect for POST/redirect/GET pattern
				isRedirectRequired = true;
			}
		}

		// Undertake the redirect
		if (isRedirectRequired) {
			HttpRouteTask.doRedirect(this.templateUriPath,
					this.isRequireSecure, connection, location, session);
			return null; // redirected, do not render template
		}

		// Render the template
		context.doFlow(Flows.RENDER, null);
		return null;
	}

}