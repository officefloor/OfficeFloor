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

import java.sql.Connection;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;

/**
 * Tests the {@link DeskChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddWorkTest extends AbstractDeskChangesTestCase {

	/**
	 * Ensure can add {@link WorkModel} that uses indexing.
	 */
	public void testAddWorkWithoutKeys() {

		// Create the work type to add
		WorkType<?> work = this.constructWorkType(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				// First task
				TaskTypeConstructor taskOne = context.addTask("TASK_ONE");
				taskOne.addObject(String.class, null);
				taskOne.addObject(Integer.class, null);
				taskOne.addFlow(String.class, null); // ignored for work-task
				taskOne.addEscalation(Throwable.class); // ignored for work-task

				// Second task (for labels)
				TaskTypeConstructor taskTwo = context.addTask("TASK_TWO");
				taskTwo.addObject(Object.class, null).setLabel("LABEL");
			}
		});

		// Validate adding the work and reverting
		Change<WorkModel> change = this.operations
				.addWork("WORK", "net.example.ExampleWorkSource",
						new PropertyListImpl("name.one", "value.one",
								"name.two", "value.two"), work);
		this.assertChange(change, null, "Add work WORK", true);
		change.apply();
		assertEquals("Ensure correct target", this.model.getWorks().get(0),
				change.getTarget());
	}

	/**
	 * Ensure can add {@link WorkModel} that uses keys.
	 */
	public void testAddWorkWithKeys() {

		// Create the work type to add
		WorkType<?> work = this.constructWorkType(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				// First task
				TaskTypeConstructor taskOne = context.addTask("TASK_ONE");
				taskOne.addObject(String.class, TaskObjectKeys.ONE);
				taskOne.addObject(Integer.class, TaskObjectKeys.TWO);
				taskOne.addFlow(String.class, null); // ignored for work-task
				taskOne.addEscalation(Throwable.class); // ignored for work-task

				// Second task (for labels)
				TaskTypeConstructor taskTwo = context.addTask("TASK_TWO");
				taskTwo.addObject(Object.class, TaskObjectKeys.ONE).setLabel(
						"LABEL_ONE");
				taskTwo.addObject(Connection.class, TaskObjectKeys.TWO)
						.setLabel("LABEL_TWO");
			}
		});

		// Validate adding the work and reverting
		Change<WorkModel> change = this.operations.addWork("WORK",
				"net.example.ExampleWorkSource", new PropertyListImpl("name",
						"value"), work);
		this.assertChange(change, null, "Add work WORK", true);
	}

	/**
	 * Keys identifying {@link TaskObjectType} instances.
	 */
	private enum TaskObjectKeys {
		ONE, TWO
	}

	/**
	 * Ensure can add {@link WorkModel} specifying the tasks to be added.
	 */
	public void testAddWorkWithSubsetOfTasks() {

		// Create the work type to add
		WorkType<?> work = this.constructWorkType(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				// Add many tasks
				context.addTask("TASK_ONE");
				context.addTask("TASK_TWO");
				context.addTask("TASK_THREE");
				context.addTask("TASK_FOUR");
			}
		});

		// Validate adding the work and reverting
		Change<WorkModel> change = this.operations.addWork("WORK",
				"net.example.ExampleWorkSource", new PropertyListImpl(), work,
				"TASK_ONE", "TASK_THREE");
		this.assertChange(change, null, "Add work WORK", true);
	}

	/**
	 * Ensure can add {@link WorkModel} and that the {@link WorkTaskModel}
	 * instances are ordered by the {@link Task} name to make merging the XML
	 * files easier under SCM.
	 */
	public void testAddWorkEnsuringTaskOrdering() {

		// Create the work type to add
		WorkType<?> work = this.constructWorkType(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				// Add in wrong order
				context.addTask("D");
				context.addTask("A");
				context.addTask("C");
				context.addTask("B");
			}
		});

		// Validate adding the work and reverting
		Change<WorkModel> change = this.operations.addWork("WORK",
				"net.example.ExampleWorkSource", new PropertyListImpl(), work);
		this.assertChange(change, null, "Add work WORK", true);
	}

	/**
	 * Ensure can add multiple {@link WorkModel} instances ensuring ordering of
	 * the {@link WorkModel} instances.
	 */
	public void testAddMultipleWorkEnsuringWorkOrdering() {

		// Create the work type
		WorkType<?> work = this.constructWorkType(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				context.addTask("TASK");
			}
		});

		// Add work multiple times
		Change<WorkModel> changeB = this.operations.addWork("WORK_B",
				"net.example.ExampleWorkSource", new PropertyListImpl(), work);
		Change<WorkModel> changeA = this.operations.addWork("WORK_A",
				"net.example.ExampleWorkSource", new PropertyListImpl(), work);
		Change<WorkModel> changeC = this.operations.addWork("WORK_C",
				"net.example.ExampleWorkSource", new PropertyListImpl(), work);

		// Apply the changes
		changeB.apply();
		changeA.apply();
		changeC.apply();
		this.validateModel();

		// Ensure can revert changes (undo)
		changeC.revert();
		changeA.revert();
		changeB.revert();
		this.validateAsSetupModel();
	}

}