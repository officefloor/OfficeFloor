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
package net.officefloor.compile.impl.desk;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.change.Change;
import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkTaskModel;

/**
 * Tests adding a {@link TaskModel}.
 * 
 * @author Daniel
 */
public class AddTaskTest extends AbstractDeskOperationsTestCase {

	/**
	 * {@link WorkTaskModel}.
	 */
	private WorkTaskModel workTask;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.compile.impl.desk.AbstractDeskOperationsTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the work task model
		this.workTask = this.desk.getWorks().get(0).getWorkTasks().get(0);
	}

	/**
	 * Ensure can add {@link TaskModel} that uses indexing.
	 */
	public void testAddTaskWithoutKeys() {

		// Create the task type
		TaskType<?, ?, ?> task = this.constructTaskType("TASK",
				new TaskConstructor() {
					@Override
					public void construct(TaskTypeConstructor task) {
						task.addFlow(String.class, null);
						task.addFlow(Integer.class, null);
						task.addEscalation(SQLException.class);
						task.addEscalation(IOException.class);
						task.getBuilder().setReturnType(Double.class);
						// Should ignore objects
						task.addObject(Object.class, null);
					}
				});

		// Validate adding the task and reverting
		Change<TaskModel> change = this.operations.addTask("TASK",
				this.workTask, task);
		this.assertChange(change, null, "Add task TASK", true);
		change.apply();
		assertEquals("Ensure correct target", this.desk.getTasks().get(0),
				change.getTarget());
	}

	/**
	 * Ensure can add {@link TaskModel} that uses keys.
	 */
	public void testAddTaskWithKeys() {

		// Create the task type
		TaskType<?, ?, ?> task = this.constructTaskType("TASK",
				new TaskConstructor() {
					@Override
					public void construct(TaskTypeConstructor task) {
						// Can not have argument types for flows
						task.addFlow(null, TaskFlowKeys.ONE);
						task.addFlow(null, TaskFlowKeys.TWO);
						task.addEscalation(Exception.class);
					}
				});

		// Validate adding the task and reverting
		Change<TaskModel> change = this.operations.addTask("TASK",
				this.workTask, task);
		this.assertChange(change, null, "Add task TASK", true);
	}

	/**
	 * {@link TaskFlowModel} keys.
	 */
	private enum TaskFlowKeys {
		ONE, TWO
	}

	/**
	 * Ensure can add {@link TaskModel} that has {@link TaskFlowModel} instances
	 * with labels.
	 */
	public void testAddTaskWithLabels() {

		// Create the task type
		TaskType<?, ?, ?> task = this.constructTaskType("TASK",
				new TaskConstructor() {
					@Override
					public void construct(TaskTypeConstructor task) {
						task.addFlow(String.class, null).setLabel("FLOW_ONE");
						task.addFlow(Integer.class, null).setLabel("FLOW_TWO");
						task.addEscalation(Exception.class).setLabel(
								"ESCALATION");
					}
				});

		// Validate adding the task and reverting
		Change<TaskModel> change = this.operations.addTask("TASK",
				this.workTask, task);
		this.assertChange(change, null, "Add task TASK", true);
	}

	/**
	 * Ensure can add multiple {@link TaskModel} instances ensuring ordering of
	 * the {@link TaskModel} instances.
	 */
	public void testAddMultipleTasksEnsuringOrdering() {

		// Create the task type
		TaskType<?, ?, ?> taskB = this.constructTaskType("TASK_B", null);
		TaskType<?, ?, ?> taskA = this.constructTaskType("TASK_A", null);
		TaskType<?, ?, ?> taskC = this.constructTaskType("TASK_C", null);

		// Create the changes to add the tasks
		Change<TaskModel> changeB = this.operations.addTask("TASK_B",
				this.workTask, taskB);
		Change<TaskModel> changeA = this.operations.addTask("TASK_A",
				this.workTask, taskA);
		Change<TaskModel> changeC = this.operations.addTask("TASK_C",
				this.workTask, taskC);

		// Add the tasks and ensure ordering
		changeB.apply();
		changeA.apply();
		changeC.apply();
		this.validateDesk();

		// Revert
		changeC.revert();
		changeA.revert();
		changeB.revert();
		this.validateAsSetupDesk();
	}
}