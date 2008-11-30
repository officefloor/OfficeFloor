/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.work.http.route;

import java.util.regex.Pattern;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.AbstractSingleTaskWork;

/**
 * {@link Work} and {@link Task} for routing HTTP requests.
 * 
 * @author Daniel
 */
public class HttpRouteTask extends
		AbstractSingleTaskWork<Object, HttpRouteTask, Indexed, Indexed> {

	/**
	 * {@link Pattern} match against paths to route the {@link HttpRequest}.
	 */
	protected final Pattern[] routings;

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
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api
	 * .execute.TaskContext)
	 */
	@Override
	public Object doTask(
			TaskContext<Object, HttpRouteTask, Indexed, Indexed> context)
			throws Throwable {

		// Obtain the request to route it
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(0);
		HttpRequest request = connection.getHttpRequest();

		// Obtain the path from the request
		String path = request.getPath();
		if (path == null) {
			// Ensure path not null
			path = "";
		}
		int parameterStart = path.indexOf('?');
		if (parameterStart > 0) {
			// Obtain the path minus the parameters
			path = path.substring(0, parameterStart);
		}

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
