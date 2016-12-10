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

import net.officefloor.compile.work.TaskType;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;

/**
 * Tests adding a {@link WorkTaskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddWorkTaskTest extends AbstractDeskChangesTestCase {

	/**
	 * {@link WorkModel}.
	 */
	private WorkModel work;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.compile.impl.desk.AbstractDeskOperationsTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the work model
		this.work = this.model.getWorks().get(0);
	}

	/**
	 * Ensure can add the {@link WorkTaskModel} that has
	 * {@link WorkTaskObjectModel} instances indexed.
	 */
	public void testAddWorkTaskWithoutKeys() {

		// Create the task type
		TaskType<?, ?, ?> task = this.constructTaskType("TASK",
				new TaskConstructor() {
					@Override
					public void construct(TaskTypeConstructor task) {
						task.addObject(Integer.class, null);
						task.addObject(String.class, null);
						// Should ignore flows and escalations
						task.addFlow(Object.class, null);
						task.addEscalation(Throwable.class);
					}
				});

		// Validate adding the work task and reverting
		Change<WorkTaskModel> change = this.operations.addWorkTask(this.work,
				task);
		this.assertChange(change, null, "Add work task TASK", true);
		change.apply();
		assertEquals("Ensure correct target", this.work.getWorkTasks().get(0),
				change.getTarget());
	}

	/**
	 * Ensure can add the {@link WorkTaskModel} that has
	 * {@link WorkTaskObjectModel} instances keyed.
	 */
	public void testAddWorkTaskWithKeys() {

		// Create the task type
		TaskType<?, ?, ?> task = this.constructTaskType("TASK",
				new TaskConstructor() {
					@Override
					public void construct(TaskTypeConstructor task) {
						task.addObject(Integer.class, TaskObjectKeys.ONE);
						task.addObject(String.class, TaskObjectKeys.TWO);
					}
				});

		// Validate adding the work task and reverting
		Change<WorkTaskModel> change = this.operations.addWorkTask(this.work,
				task);
		this.assertChange(change, null, "Add work task TASK", true);
	}

	/**
	 * {@link WorkTaskObjectModel} keys.
	 */
	private enum TaskObjectKeys {
		ONE, TWO
	}

	/**
	 * Ensure can add the {@link WorkTaskModel} that has
	 * {@link WorkTaskObjectModel} instances with labels.
	 */
	public void testAddWorkTaskWithLabels() {

		// Create the task type
		TaskType<?, ?, ?> task = this.constructTaskType("TASK",
				new TaskConstructor() {
					@Override
					public void construct(TaskTypeConstructor task) {
						task.addObject(Integer.class, null).setLabel(
								"OBJECT_ONE");
						task.addObject(String.class, null).setLabel(
								"OBJECT_TWO");
					}
				});

		// Validate adding the work task and reverting
		Change<WorkTaskModel> change = this.operations.addWorkTask(this.work,
				task);
		this.assertChange(change, null, "Add work task TASK", true);
	}

	/**
	 * Ensure can add the {@link WorkTaskModel} that has
	 * {@link WorkTaskObjectModel} instances with labels.
	 */
	public void testAddMultipleWorkTasksEnsuringOrdering() {

		// Create the task type
		TaskType<?, ?, ?> taskB = this.constructTaskType("TASK_B", null);
		TaskType<?, ?, ?> taskA = this.constructTaskType("TASK_A", null);
		TaskType<?, ?, ?> taskC = this.constructTaskType("TASK_C", null);

		// Create the changes to add the work tasks
		Change<WorkTaskModel> changeB = this.operations.addWorkTask(this.work,
				taskB);
		Change<WorkTaskModel> changeA = this.operations.addWorkTask(this.work,
				taskA);
		Change<WorkTaskModel> changeC = this.operations.addWorkTask(this.work,
				taskC);

		// Add the work tasks and ensure ordering
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

	/**
	 * Ensure can add the {@link TaskType} only once to the {@link WorkModel}.
	 */
	public void testAddWorkTaskOnlyOnceForWork() {

		// Create the task type and have it added
		TaskType<?, ?, ?> task = this.constructTaskType("TASK", null);
		this.operations.addWorkTask(this.work, task).apply();

		// Create the change to add the task
		Change<WorkTaskModel> change = this.operations.addWorkTask(this.work,
				task);
		this.assertChange(change, null, "Add work task TASK", false,
				"Task TASK already added to work WORK");
	}

}