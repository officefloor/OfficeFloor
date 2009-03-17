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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import net.officefloor.compile.impl.work.source.WorkLoaderContextImpl;
import net.officefloor.compile.spi.work.source.WorkLoaderContext;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.mock.MockClass;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;

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
		Properties properties = new Properties();
		properties.setProperty(ClassWorkLoader.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		WorkLoaderContext context = new WorkLoaderContextImpl(
				new String[] { ClassWorkLoader.CLASS_NAME_PROPERTY_NAME },
				properties, this.getClass().getClassLoader());

		// Load the work
		WorkModel<ClassWork> work = new ClassWorkLoader().loadWork(context);

		// Verify functionality
		this.verifyMockObjects();

		// Verify the work model
		ClassWork classWork = (ClassWork) work.getWorkFactory().createWork();
		assertTrue("Must create work ",
				classWork.getObject() instanceof MockClass);
		assertEquals("Incorrect number of tasks", 2, work.getTasks().size());

		// Obtain the tasks
		TaskModel<?, ?> taskOne = null;
		TaskModel<?, ?> taskTwo = null;
		for (TaskModel<?, ?> task : work.getTasks()) {
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
		assertEquals("Incorrect number of escalations", 1, taskOne
				.getEscalations().size());
		assertEquals("Incorrect escalation", SQLException.class.getName(),
				taskOne.getEscalations().get(0).getEscalationType());

		// Verify the second task
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
		assertEquals("Incorrect number of escalations", 1, taskTwo
				.getEscalations().size());
		assertEquals("Incorrect escalation", IOException.class.getName(),
				taskTwo.getEscalations().get(0).getEscalationType());
	}

}
