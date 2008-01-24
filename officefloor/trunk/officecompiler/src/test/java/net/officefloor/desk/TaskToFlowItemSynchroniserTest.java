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
package net.officefloor.desk;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.task.TaskFactoryManufacturer;
import net.officefloor.model.work.TaskEscalationModel;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;

/**
 * Tests the {@link net.officefloor.desk.TaskToFlowItemSynchroniser}.
 * 
 * @author Daniel
 */
public class TaskToFlowItemSynchroniserTest extends OfficeFrameTestCase {

	/**
	 * Ensure synchronises.
	 */
	@SuppressWarnings("unchecked")
	public void testTaskToFlowItemSynchroniser() throws Exception {

		// Create the Task
		TaskFlowModel taskFlowOne = new TaskFlowModel(null, 1);
		TaskFlowModel taskFlowTwo = new TaskFlowModel(null, 2);
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

		// Validate outputs on flow item
		assertEquals("Incorrect number of outputs", 2, flowItem.getOutputs()
				.size());
		assertEquals("Incorrect task flow one", taskFlowOne, flowItem
				.getOutputs().get(0).getTaskFlow());
		assertEquals("Incorrect task flow two", taskFlowTwo, flowItem
				.getOutputs().get(1).getTaskFlow());

		// Validate escalations on flow item
		assertEquals("Incorrect number of escalations", 2, flowItem
				.getEscalations().size());
		assertEquals("Incorrect task escalation one", taskEscalationOne,
				flowItem.getEscalations().get(0).getTaskEscalation());
		assertEquals("Incorrect task escalation one type", taskEscalationOne
				.getEscalationType(), flowItem.getEscalations().get(0)
				.getEscalationType());
		assertEquals("Incorrect task escalation two", taskEscalationTwo,
				flowItem.getEscalations().get(1).getTaskEscalation());
		assertEquals("Incorrect task escalation two type", taskEscalationTwo
				.getEscalationType(), flowItem.getEscalations().get(1)
				.getEscalationType());

		// Add task flow
		TaskFlowModel taskFlowThree = new TaskFlowModel(null, 3);
		task.addFlow(taskFlowThree);

		// Add escalation
		TaskEscalationModel taskEscalationThree = new TaskEscalationModel(
				NullPointerException.class.getName());
		task.addEscalation(taskEscalationThree);

		// Synchronise again the task to the flow item
		TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(task, flowItem);

		// Validate flows synchronised onto flow item
		assertEquals("Incorrect number of outputs", 3, flowItem.getOutputs()
				.size());
		assertEquals("Incorrect task flow three", taskFlowThree, flowItem
				.getOutputs().get(2).getTaskFlow());

		// Validate escalations synchronised onto flow item
		assertEquals("Incorrect number of escalations", 3, flowItem
				.getOutputs().size());
		assertEquals("Incorrect task escalation three", taskEscalationThree,
				flowItem.getEscalations().get(2).getTaskEscalation());

		// Remove a task flow
		task.removeFlow(taskFlowTwo);

		// Remove a task escalation
		task.removeEscalation(taskEscalationTwo);

		// Synchronise again the task to the flow item
		TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(task, flowItem);

		// Validate correct task flow removed
		assertEquals("Incorrect number of outputs", 2, flowItem.getOutputs()
				.size());
		assertEquals("Incorrect first task flow", taskFlowOne, flowItem
				.getOutputs().get(0).getTaskFlow());
		assertEquals("Incorrect second task flow", taskFlowThree, flowItem
				.getOutputs().get(1).getTaskFlow());

		// Validate correct escalation removed
		assertEquals("Incorrect number of escalations", 2, flowItem
				.getEscalations().size());
		assertEquals("Incorrect first task escalation", taskEscalationOne,
				flowItem.getEscalations().get(0).getTaskEscalation());
		assertEquals("Incorrect second task escalation", taskEscalationThree,
				flowItem.getEscalations().get(1).getTaskEscalation());
	}

}
