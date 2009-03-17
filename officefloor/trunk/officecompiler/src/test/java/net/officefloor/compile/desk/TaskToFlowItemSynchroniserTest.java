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
package net.officefloor.compile.desk;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.desk.TaskToFlowItemSynchroniser;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.task.TaskFactoryManufacturer;
import net.officefloor.model.work.TaskEscalationModel;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;

/**
 * Tests the {@link net.officefloor.compile.desk.TaskToFlowItemSynchroniser}.
 * 
 * @author Daniel
 */
//Provide do/undo synchronise
@Deprecated
public class TaskToFlowItemSynchroniserTest extends OfficeFrameTestCase {

	/**
	 * Ensure synchronises.
	 */
	@SuppressWarnings("unchecked")
	public void testTaskToFlowItemSynchroniser() throws Exception {

		// Create the Task
		TaskFlowModel taskFlowOne = new TaskFlowModel(null, 1, "Label One");
		TaskFlowModel taskFlowTwo = new TaskFlowModel(null, 2, "Label Two");
		TaskEscalationModel taskEscalationOne = new TaskEscalationModel(
				IOException.class.getName());
		TaskEscalationModel taskEscalationTwo = new TaskEscalationModel(
				SQLException.class.getName());
		TaskModel task = new TaskModel("TASK", this
				.createMock(TaskFactoryManufacturer.class), null, null,
				new TaskObjectModel[0], new TaskFlowModel[] { taskFlowOne,
						taskFlowTwo }, new TaskEscalationModel[] {
						taskEscalationOne, taskEscalationTwo });

		// Create the Flow Item
		FlowItemModel flowItem = new FlowItemModel();

		// Synchronise the task to the flow item
		TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(task, flowItem);

		// Validate task on flow item
		assertEquals("Incorrect task", task, flowItem.getTask());

		// Create the listing of expected flow item outputs
		List<FlowItemOutputModel> expectedOutputs = new LinkedList<FlowItemOutputModel>();
		expectedOutputs.add(new FlowItemOutputModel("1", "Label One",
				taskFlowOne, null, null));
		expectedOutputs.add(new FlowItemOutputModel("2", "Label Two",
				taskFlowTwo, null, null));

		// Validate outputs on flow item
		assertList(new String[] { "getId", "getLabel", "getTaskFlow" },
				flowItem.getOutputs(), expectedOutputs
						.toArray(new FlowItemOutputModel[0]));

		// Create the listing of expected flow item escalations
		List<FlowItemEscalationModel> expectedEscalations = new LinkedList<FlowItemEscalationModel>();
		expectedEscalations.add(new FlowItemEscalationModel(taskEscalationOne
				.getEscalationType(), taskEscalationOne, null, null));
		expectedEscalations.add(new FlowItemEscalationModel(taskEscalationTwo
				.getEscalationType(), taskEscalationTwo, null, null));

		// Validate escalations on flow item
		assertList(new String[] { "getEscalationType", "getTaskEscalation" },
				flowItem.getEscalations(), expectedEscalations
						.toArray(new FlowItemEscalationModel[0]));

		// Add task flow
		TaskFlowModel taskFlowThree = new TaskFlowModel(null, 3, "Label Three");
		task.addFlow(taskFlowThree);

		// Add escalation
		TaskEscalationModel taskEscalationThree = new TaskEscalationModel(
				NullPointerException.class.getName());
		task.addEscalation(taskEscalationThree);

		// Synchronise again the task to the flow item
		TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(task, flowItem);

		// Alter expected outputs
		expectedOutputs.add(new FlowItemOutputModel("3", "Label Three",
				taskFlowThree, null, null));

		// Validate flows synchronised onto flow item
		assertList(new String[] { "getId", "getLabel", "getTaskFlow" },
				flowItem.getOutputs(), expectedOutputs
						.toArray(new FlowItemOutputModel[0]));

		// Alter expected escalations
		expectedEscalations.add(new FlowItemEscalationModel(taskEscalationThree
				.getEscalationType(), taskEscalationThree, null, null));

		// Validate escalations synchronised onto flow item
		assertList(new String[] { "getEscalationType", "getTaskEscalation" },
				flowItem.getEscalations(), expectedEscalations
						.toArray(new FlowItemEscalationModel[0]));

		// Remove a task flow
		task.removeFlow(taskFlowTwo);

		// Remove a task escalation
		task.removeEscalation(taskEscalationTwo);

		// Synchronise again the task to the flow item
		TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(task, flowItem);

		// Alter expected outputs
		expectedOutputs.remove(1); // Second output

		// Validate flows synchronised onto flow item
		assertList(new String[] { "getId", "getLabel", "getTaskFlow" },
				flowItem.getOutputs(), expectedOutputs
						.toArray(new FlowItemOutputModel[0]));

		// Alter expected escalations
		expectedEscalations.remove(1); // Second escalation

		// Validate escalations synchronised onto flow item
		assertList(new String[] { "getEscalationType", "getTaskEscalation" },
				flowItem.getEscalations(), expectedEscalations
						.toArray(new FlowItemEscalationModel[0]));
	}
}
