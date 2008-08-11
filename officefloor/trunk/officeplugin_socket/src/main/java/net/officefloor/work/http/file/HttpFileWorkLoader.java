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
package net.officefloor.work.http.file;

import java.io.File;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.AbstractWorkLoader;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkLoaderContext;

/**
 * {@link WorkLoader} to provide {@link File} content HTTP responses.
 * 
 * @author Daniel
 */
public class HttpFileWorkLoader extends AbstractWorkLoader {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.AbstractWorkLoader#loadSpecification(net.officefloor.work.AbstractWorkLoader.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specific configuration
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.WorkLoader#loadWork(net.officefloor.work.WorkLoaderContext)
	 */
	@Override
	public WorkModel<?> loadWork(WorkLoaderContext context) throws Exception {
		
		// Create the task to return a file
		HttpFileTask task = new HttpFileTask();

		// Create the task for routing
		TaskModel<Indexed, Indexed> taskModel = new TaskModel<Indexed, Indexed>();
		taskModel.setTaskName("file");
		taskModel.setTaskFactoryManufacturer(task);
		taskModel.addObject(new TaskObjectModel<Indexed>(null,
				ServerHttpConnection.class.getName()));

		// Create the work for routing
		WorkModel<HttpFileTask> workModel = new WorkModel<HttpFileTask>();
		workModel.setTypeOfWork(HttpFileTask.class);
		workModel.setWorkFactory(task);
		workModel.addTask(taskModel);

		// Return the work model
		return workModel;
	}

}
