/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.route.source;

import java.util.regex.Pattern;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.file.HttpFileUtil;
import net.officefloor.plugin.socket.server.http.file.InvalidHttpRequestUriException;

/**
 * {@link Task} for routing a {@link HttpRequest}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpRouteTask
		extends
		AbstractSingleTask<HttpRouteTask, HttpRouteTask.HttpRouteTaskDependencies, Indexed> {

	/**
	 * Dependencies for the {@link HttpRouteTask}.
	 */
	public static enum HttpRouteTaskDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link Pattern} match against paths to route the {@link HttpRequest}.
	 */
	private final Pattern[] routings;

	/**
	 * Initiate.
	 *
	 * @param routings
	 *            {@link Pattern} match against paths to route the
	 *            {@link HttpRequest}.
	 */
	public HttpRouteTask(Pattern[] routings) {
		this.routings = routings;
	}

	/*
	 * ======================== Task =================================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpRouteTask, HttpRouteTaskDependencies, Indexed> context)
			throws InvalidHttpRequestUriException {

		// Obtain the request to route it
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(0);
		HttpRequest request = connection.getHttpRequest();

		// Obtain the canonical path from request
		String path = request.getRequestURI();
		path = HttpFileUtil.transformToCanonicalPath(path);

		// Route to appropriate path
		for (int i = 0; i < this.routings.length; i++) {
			Pattern currentPattern = this.routings[i];

			// Determine if match
			if (currentPattern.matcher(path).matches()) {
				// Found match, route to path
				context.doFlow(i, null);

				// No further routing
				return null;
			}
		}

		// No route matched, send to default route
		context.doFlow(this.routings.length, null);

		// No parameter
		return null;
	}

}