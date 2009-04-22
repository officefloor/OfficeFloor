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
package net.officefloor.plugin.work.http.route;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.work.http.route.HttpRouteTask.HttpRouteTaskDependencies;

/**
 * {@link WorkSource} to provide routing of HTTP requests.
 * 
 * @author Daniel
 */
public class HttpRouteWorkSource extends AbstractWorkSource<HttpRouteTask> {

	/**
	 * Property prefix for a routing entry.
	 */
	public static final String ROUTE_PROPERTY_PREFIX = "route.";

	/*
	 * ================== AbstractWorkSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// All entries are dynamic
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpRouteTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Iterate over the mappings creating the routings
		List<String> routeNames = new LinkedList<String>();
		List<Pattern> routePatterns = new LinkedList<Pattern>();
		for (String name : context.getPropertyNames()) {

			// Ignore if not starts with routing prefix
			if (!name.startsWith(ROUTE_PROPERTY_PREFIX)) {
				continue;
			}

			// Obtain the route name, by stripping off the prefix
			String routeName = name.substring(ROUTE_PROPERTY_PREFIX.length());

			// Obtain the pattern for the route
			String patternText = context.getProperty(name);
			Pattern pattern = Pattern.compile(patternText);

			// Add details
			routeNames.add(routeName);
			routePatterns.add(pattern);
		}

		// Ensure have at least one routing
		if (routePatterns.size() == 0) {
			throw new Exception("Must have at least one routing entry");
		}

		// Create the task to route
		HttpRouteTask task = new HttpRouteTask(routePatterns
				.toArray(new Pattern[0]));

		// Define the task
		workTypeBuilder.setWorkFactory(task);
		TaskTypeBuilder<HttpRouteTaskDependencies, Indexed> taskBuilder = workTypeBuilder
				.addTaskType("route", task, HttpRouteTaskDependencies.class,
						Indexed.class);
		taskBuilder.addObject(ServerHttpConnection.class).setKey(
				HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION);

		// Create the routes for the task
		for (String routeName : routeNames) {
			// Flow for each routing entry
			taskBuilder.addFlow().setLabel(routeName);
		}

		// Create the default flow (if not match any routes)
		taskBuilder.addFlow().setLabel("default");
	}

}