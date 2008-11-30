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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.WorkLoaderUtil;

/**
 * Tests the {@link HttpFileWorkLoader}.
 * 
 * @author Daniel
 */
public class HttpFileWorkLoaderTest extends OfficeFrameTestCase {

	/**
	 * Validates the {@link WorkModel} loaded from the
	 * {@link HttpFileWorkLoader}.
	 */
	public void testLoad() throws Exception {

		// Load the work
		WorkModel<?> actualWork = WorkLoaderUtil
				.loadWork(HttpFileWorkLoader.class);

		// Create the http file task
		HttpFileTask httpFileTask = new HttpFileTask();

		// Create the expected work
		WorkModel<HttpFileTask> expectedWork = new WorkModel<HttpFileTask>();
		expectedWork.setWorkFactory(httpFileTask);
		expectedWork.setTypeOfWork(HttpFileTask.class);

		// Create the expected task
		TaskModel<Indexed, None> task = new TaskModel<Indexed, None>();
		task.setTaskName("file");
		task.setTaskFactoryManufacturer(httpFileTask);
		expectedWork.addTask(task);

		// Reference the HTTP connection
		TaskObjectModel<Indexed> object = new TaskObjectModel<Indexed>();
		object.setObjectType(ServerHttpConnection.class.getName());
		task.addObject(object);

		// Verify correctly loaded
		WorkLoaderUtil.assertWorkModelMatch(expectedWork, actualWork);
	}
}
