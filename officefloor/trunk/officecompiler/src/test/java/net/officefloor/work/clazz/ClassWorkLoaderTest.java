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
package net.officefloor.work.clazz;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.util.OfficeFrameTestCase;
import net.officefloor.mock.MockClass;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.work.WorkLoaderContext;

/**
 * Test the {@link net.officefloor.work.clazz.ClassWorkLoader}.
 * 
 * @author Daniel
 */
public class ClassWorkLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to load work.
	 */
	@SuppressWarnings("unchecked")
	public void testWorkLoader() throws Exception {

		// Replay
		this.replayMockObjects();

		// Create the work loader context
		WorkLoaderContext context = new WorkLoaderContext() {

			public String getConfiguration() {
				return MockClass.class.getName();
			}

			public ConfigurationContext getConfigurationContext() {
				return null;
			}

			public ClassLoader getClassLoader() {
				return this.getClass().getClassLoader();
			}
		};

		// Load the work
		WorkModel work = new ClassWorkLoader().loadWork(context);

		// Verify functionality
		this.verifyMockObjects();

		// Verify the work model
		ClassWork classWork = (ClassWork) work.getWorkFactory().createWork();
		assertTrue("Must create work ",
				classWork.getObject() instanceof MockClass);
		assertEquals("Incorrect number of tasks", 1, work.getTasks().size());
		TaskModel<Indexed, Indexed> task = work.getTasks().get(0);
		assertEquals("Incorrect task name", "taskMethod", task.getTaskName());
		ClassTask classTask = (ClassTask) task.getTaskFactory().createTask(
				classWork);
		assertEquals("Incorrect task method", "taskMethod", classTask.method
				.getName());
		assertEquals("Incorrect number of objects", 1, task.getObjects().size());
		TaskObjectModel taskObject = task.getObjects().get(0);
		assertNull("Incorrect object managed object key", taskObject
				.getManagedObjectKey());
		assertEquals("Incorrect object type", Object.class.getName(),
				taskObject.getObjectType());
		assertEquals("Incorrect number of flows", 1, task.getFlows().size());
		TaskFlowModel taskFlow = task.getFlows().get(0);
		assertNull("Incorrect flow key", taskFlow.getFlowKey());
		assertEquals("Incorrect flow index", 0, taskFlow.getFlowIndex());
	}

}
