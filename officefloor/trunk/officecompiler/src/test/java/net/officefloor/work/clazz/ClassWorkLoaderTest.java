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
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
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
		assertEquals("Incorrect number of tasks", 2, work.getTasks().size());

		// Obtain the tasks
		TaskModel<Indexed, Indexed> taskOne = null;
		TaskModel<Indexed, Indexed> taskTwo = null;
		for (TaskModel<Indexed, Indexed> task : work.getTasks()) {
			if ("anotherMethod".equals(task.getTaskName())) {
				taskOne = task;
			} else if ("taskMethod".equals(task.getTaskName())) {
				taskTwo = task;
			}
		}

		// Verify the first task
		assertEquals("Incorrect task name", "anotherMethod", taskOne
				.getTaskName());
		TaskFactory taskFactoryOne = taskOne.getTaskFactoryManufacturer()
				.createTaskFactory();
		ClassTask classTaskOne = (ClassTask) taskFactoryOne
				.createTask(classWork);
		assertEquals("Incorrect task method", "anotherMethod",
				classTaskOne.method.getName());
		assertEquals("Incorrect number of objects", 0, taskOne.getObjects()
				.size());
		assertEquals("Incorrect number of flows", 0, taskOne.getFlows().size());

		// Verify the first task
		assertEquals("Incorrect task name", "taskMethod", taskTwo.getTaskName());
		TaskFactory taskFactoryTwo = taskTwo.getTaskFactoryManufacturer()
				.createTaskFactory();
		ClassTask classTaskTwo = (ClassTask) taskFactoryTwo
				.createTask(classWork);
		assertEquals("Incorrect task method", "taskMethod", classTaskTwo.method
				.getName());
		assertEquals("Incorrect number of objects", 1, taskTwo.getObjects()
				.size());
		TaskObjectModel taskObject = taskTwo.getObjects().get(0);
		assertNull("Incorrect object managed object key", taskObject
				.getManagedObjectKey());
		assertEquals("Incorrect object type", String.class.getName(),
				taskObject.getObjectType());
		assertEquals("Incorrect number of flows", 3, taskTwo.getFlows().size());
		TaskFlowModel taskFlow = taskTwo.getFlows().get(0);
		assertNull("Incorrect flow key", taskFlow.getFlowKey());
		assertEquals("Incorrect flow index", 0, taskFlow.getFlowIndex());
	}

}
