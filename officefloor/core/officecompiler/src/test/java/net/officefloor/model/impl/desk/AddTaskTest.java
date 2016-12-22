/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.model.impl.desk;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;

/**
 * Tests adding a {@link TaskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddTaskTest extends AbstractDeskChangesTestCase {

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
		this.workTask = this.model.getWorks().get(0).getWorkTasks().get(0);
	}

	/**
	 * Ensure can not apply change if {@link WorkTaskModel} is not on the
	 * {@link DeskModel} (specifically a {@link WorkModel} of the
	 * {@link DeskModel}).
	 */
	public void testWorkTaskNotOnDesk() {

		// Create the work task not on the desk
		WorkTaskModel workTask = new WorkTaskModel("NOT_ON_DESK");

		// Validate not able to add task if work task not on desk
		ManagedFunctionType<?, ?, ?> taskType = this
				.constructTaskType("NOT_ON_DESK", null);
		Change<TaskModel> change = this.operations.addTask("TASK", workTask,
				taskType);
		this.assertChange(change, null, "Add task TASK", false,
				"Work task NOT_ON_DESK not on desk");
		assertNotNull("Must have target", change.getTarget());
	}

	/**
	 * Ensures can not apply change if the {@link WorkTaskModel} name does not
	 * match the {@link ManagedFunctionType} name.
	 */
	public void testWorkTaskNameMismatch() {

		// Create a task type with mismatched name
		ManagedFunctionType<?, ?, ?> taskType = this.constructTaskType(
				"MISMATCH_TASK_NAME", null);

		// Validate not able to add task if work task name mismatch
		Change<TaskModel> change = this.operations.addTask("TASK",
				this.workTask, taskType);
		this
				.assertChange(change, null, "Add task TASK", false,
						"Task type MISMATCH_TASK_NAME does not match work task WORK_TASK");
		assertNotNull("Must have target", change.getTarget());
	}

	/**
	 * Ensure can add {@link TaskModel} that uses indexing.
	 */
	public void testAddTaskWithoutKeys() {

		// Create the task type
		ManagedFunctionType<?, ?, ?> task = this.constructTaskType("WORK_TASK",
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
		assertEquals("Ensure correct target", this.model.getTasks().get(0),
				change.getTarget());
	}

	/**
	 * Ensure can add {@link TaskModel} that uses keys.
	 */
	public void testAddTaskWithKeys() {

		// Create the task type
		ManagedFunctionType<?, ?, ?> task = this.constructTaskType("WORK_TASK",
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
		ManagedFunctionType<?, ?, ?> task = this.constructTaskType("WORK_TASK",
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
		ManagedFunctionType<?, ?, ?> taskType = this.constructTaskType("WORK_TASK", null);

		// Create the changes to add the tasks
		Change<TaskModel> changeB = this.operations.addTask("TASK_B",
				this.workTask, taskType);
		Change<TaskModel> changeA = this.operations.addTask("TASK_A",
				this.workTask, taskType);
		Change<TaskModel> changeC = this.operations.addTask("TASK_C",
				this.workTask, taskType);

		// Add the tasks and ensure ordering
		changeB.apply();
		changeA.apply();
		changeC.apply();
		this.validateModel();

		// Revert
		changeC.revert();
		changeA.revert();
		changeB.revert();
		this.validateAsSetupModel();
	}
}