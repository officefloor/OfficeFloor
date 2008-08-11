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
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.execute.WorkContext;
import net.officefloor.model.task.TaskFactoryManufacturer;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;

/**
 * {@link Work} and {@link Task} for routing HTTP requests.
 * 
 * @author Daniel
 */
public class HttpRouteTask implements WorkFactory<HttpRouteTask>, Work,
		TaskFactoryManufacturer,
		TaskFactory<Object, HttpRouteTask, Indexed, Indexed>,
		Task<Object, HttpRouteTask, Indexed, Indexed> {

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
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkFactory#createWork()
	 */
	@Override
	public HttpRouteTask createWork() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Work#setWorkContext(net.officefloor.frame.api.execute.WorkContext)
	 */
	@Override
	public void setWorkContext(WorkContext context) throws Exception {
		// Not need context
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.task.TaskFactoryManufacturer#createTaskFactory()
	 */
	@Override
	public TaskFactory<?, ?, ?, ?> createTaskFactory() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskFactory#createTask(net.officefloor.frame.api.execute.Work)
	 */
	@Override
	public Task<Object, HttpRouteTask, Indexed, Indexed> createTask(
			HttpRouteTask work) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
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
		int parameterStart = path.indexOf('?');
		if (parameterStart > 0) {
			// Obtain the path minus the parameters
			path = path.substring(0, (parameterStart - 1));
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
