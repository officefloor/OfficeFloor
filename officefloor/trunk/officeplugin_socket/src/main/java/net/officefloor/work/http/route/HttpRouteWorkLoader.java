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

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.AbstractWorkLoader;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkLoaderContext;

/**
 * {@link WorkLoader} to provide routing of HTTP requests.
 * 
 * @author Daniel
 */
public class HttpRouteWorkLoader extends AbstractWorkLoader {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.AbstractWorkLoader#loadSpecification(net.officefloor.work.AbstractWorkLoader.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// All entries are dynamic
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.WorkLoader#loadWork(net.officefloor.work.WorkLoaderContext)
	 */
	@Override
	public WorkModel<?> loadWork(WorkLoaderContext context) throws Exception {

		// Obtain mappings
		Properties properties = context.getProperties();

		// Create the patterns
		List<Pattern> patterns = new LinkedList<Pattern>();
		boolean isComplete = false;
		int index = 1;
		while (!isComplete) {
			// Obtain pattern
			String pattern = properties.getProperty(String.valueOf(index++));
			if (pattern == null) {
				// Complete as no further pattern
				isComplete = true;
			} else {
				// Add the pattern
				patterns.add(Pattern.compile(pattern));
			}
		}

		// Create the routing
		Pattern[] routing = patterns.toArray(new Pattern[0]);
		if (routing.length == 0) {
			throw new Exception("Must have at least one routing entry");
		}

		// Create the task to route
		HttpRouteTask task = new HttpRouteTask(routing);

		// Create the task for routing
		TaskModel<Indexed, Indexed> taskModel = new TaskModel<Indexed, Indexed>();
		taskModel.setTaskName("route");
		taskModel.setTaskFactoryManufacturer(task);
		taskModel.addObject(new TaskObjectModel<Indexed>(null,
				ServerHttpConnection.class.getName()));
		for (int i = 0; i <= routing.length; i++) {
			taskModel.addFlow(new TaskFlowModel<Indexed>(null, i));
		}

		// Create the work for routing
		WorkModel<HttpRouteTask> workModel = new WorkModel<HttpRouteTask>();
		workModel.setTypeOfWork(HttpRouteTask.class);
		workModel.setWorkFactory(task);
		workModel.addTask(taskModel);

		// Return the work model
		return workModel;
	}
}
