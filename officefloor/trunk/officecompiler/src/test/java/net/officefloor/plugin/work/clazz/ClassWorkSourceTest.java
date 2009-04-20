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
package net.officefloor.plugin.work.clazz;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.work.clazz.ClassTask;
import net.officefloor.plugin.work.clazz.ClassWork;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.work.clazz.Flow;

/**
 * Test the {@link ClassWorkSource}.
 * 
 * @author Daniel
 */
public class ClassWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensures specification correct.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensure able to load {@link WorkType} for the {@link ClassWorkSource}.
	 */
	@SuppressWarnings("unchecked")
	public void testWorkLoader() throws Exception {

		// Load the work type
		WorkType<ClassWork> workType = WorkLoaderUtil.loadWorkType(
				ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME, MockClass.class
						.getName());

		// Verify the work model
		ClassWork classWork = (ClassWork) workType.getWorkFactory()
				.createWork();
		assertTrue("Must create work ",
				classWork.getObject() instanceof MockClass);
		assertEquals("Incorrect number of tasks", 2,
				workType.getTaskTypes().length);

		// Obtain the tasks
		TaskType<ClassWork, Indexed, Indexed> taskOne = null;
		TaskType<ClassWork, Indexed, Indexed> taskTwo = null;
		for (TaskType task : workType.getTaskTypes()) {
			if ("anotherMethod".equals(task.getTaskName())) {
				taskOne = task;
			} else if ("taskMethod".equals(task.getTaskName())) {
				taskTwo = task;
			}
		}

		// Verify the first task
		assertEquals("Incorrect task name", "anotherMethod", taskOne
				.getTaskName());
		TaskFactory<ClassWork, Indexed, Indexed> taskFactoryOne = taskOne
				.getTaskFactoryManufacturer().createTaskFactory();
		ClassTask classTaskOne = (ClassTask) taskFactoryOne
				.createTask(classWork);
		assertEquals("Incorrect task method", "anotherMethod", classTaskOne
				.getMethod().getName());
		assertEquals("Incorrect number of objects", 0,
				taskOne.getObjectTypes().length);
		assertEquals("Incorrect number of flows", 0,
				taskOne.getFlowTypes().length);
		assertEquals("Incorrect number of escalations", 1, taskOne
				.getEscalationTypes().length);
		assertEquals("Incorrect escalation", SQLException.class, taskOne
				.getEscalationTypes()[0].getEscalationType());

		// Verify the second task
		assertEquals("Incorrect task name", "taskMethod", taskTwo.getTaskName());
		TaskFactory<ClassWork, Indexed, Indexed> taskFactoryTwo = taskTwo
				.getTaskFactoryManufacturer().createTaskFactory();
		ClassTask classTaskTwo = (ClassTask) taskFactoryTwo
				.createTask(classWork);
		assertEquals("Incorrect task method", "taskMethod", classTaskTwo
				.getMethod().getName());
		assertEquals("Incorrect number of objects", 1,
				taskTwo.getObjectTypes().length);
		TaskObjectType<?> objectType = taskTwo.getObjectTypes()[0];
		assertNull("Incorrect object managed object key", objectType.getKey());
		assertEquals("Incorrect object type", String.class, objectType
				.getObjectType());
		assertEquals("Incorrect number of flows", 3,
				taskTwo.getFlowTypes().length);
		TaskFlowType<?> flowType = taskTwo.getFlowTypes()[0];
		assertNull("Incorrect flow key", flowType.getKey());
		assertEquals("Incorrect flow index", 0, flowType.getIndex());
		assertEquals("Incorrect number of escalations", 1, taskTwo
				.getEscalationTypes().length);
		assertEquals("Incorrect escalation", IOException.class, taskTwo
				.getEscalationTypes()[0].getEscalationType());
	}

	/**
	 * Mock {@link Class} to load as {@link ClassWork}.
	 */
	public static class MockClass {

		public Object taskMethod(String parameter, Flow<?> flowSequential,
				Flow<Object> flowParallel, Flow<String> flowAsynchronous)
				throws IOException {
			return null;
		}

		public void anotherMethod() throws SQLException {
		}

		Object nonTaskMethod(Object parameter) {
			return null;
		}
	}

}