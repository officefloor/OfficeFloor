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
package net.officefloor.compile.test.archive;

import net.officefloor.compile.spi.work.source.TaskFactoryManufacturer;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;

/**
 * Tests the {@link net.officefloor.compile.test.archive.TaskToDeskTaskSynchroniser}.
 * 
 * @author Daniel
 */
// Provide do/undo synchronise
@Deprecated
public class TaskToDeskTaskSynchroniserTest extends OfficeFrameTestCase {

	/**
	 * Ensure synchronises.
	 */

	@SuppressWarnings("unchecked")
	public void testTaskToDeskTaskSynchroniser() {

		// Create the Task
		TaskObjectModel taskObjectOne = new TaskObjectModel(null,
				"java.lang.String");
		TaskObjectModel taskObjectTwo = new TaskObjectModel(null,
				"java.lang.Integer");
		TaskModel task = new TaskModel("TASK", this
				.createMock(TaskFactoryManufacturer.class), null, null,
				new TaskObjectModel[] { taskObjectOne, taskObjectTwo }, null,
				null);

		// Create the Flow Item
		DeskTaskModel deskTask = new DeskTaskModel();

		// Synchronise the task to the flow item
		TaskToDeskTaskSynchroniser.synchroniseTaskOntoDeskTask(task, deskTask);

		// Validate outputs on flow item
		assertEquals("Incorrect task", task, deskTask.getTask());
		assertEquals("Incorrect number of objects", 2, deskTask.getObjects()
				.size());
		assertEquals("Incorrect task object one", taskObjectOne, deskTask
				.getObjects().get(0).getTaskObject());
		assertEquals("Incorrect task object two", taskObjectTwo, deskTask
				.getObjects().get(1).getTaskObject());

		// Add task object
		TaskObjectModel taskObjectThree = new TaskObjectModel(null,
				"java.lang.Boolean");
		task.addObject(taskObjectThree);

		// Synchronise again the task to the flow item
		TaskToDeskTaskSynchroniser.synchroniseTaskOntoDeskTask(task, deskTask);

		// Validate synchronised onto desk task
		assertEquals("Incorrect number of objects", 3, deskTask.getObjects()
				.size());
		assertEquals("Incorrect task object three", taskObjectThree, deskTask
				.getObjects().get(2).getTaskObject());

		// Remove the task object
		task.removeObject(taskObjectTwo);

		// Synchronise again the task to the flow item
		TaskToDeskTaskSynchroniser.synchroniseTaskOntoDeskTask(task, deskTask);

		// Validate correct task object removed
		assertEquals("Incorrect number of objects", 2, deskTask.getObjects()
				.size());
		assertEquals("Incorrect first task object", taskObjectOne, deskTask
				.getObjects().get(0).getTaskObject());
		assertEquals("Incorrect second task object", taskObjectThree, deskTask
				.getObjects().get(1).getTaskObject());
	}

}
