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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.WorkType;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;

/**
 * Tests refactoring the {@link WorkModel} to a {@link WorkType} via the
 * {@link DeskChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorWorkTest extends AbstractRefactorWorkTest {

	/**
	 * Tests renaming the {@link WorkModel}.
	 */
	public void testRenameWork() {
		this.refactor_workName("NEW_NAME");
		this.doRefactor();
	}

	/**
	 * Tests changing the {@link WorkSource} for the {@link WorkModel}.
	 */
	public void testChangeWorkSourceClass() {
		this.refactor_workSourceClassName("net.another.AnotherWorkSource");
		this.doRefactor();
	}

	/**
	 * Tests changing the {@link PropertyList} for the {@link WorkModel}.
	 */
	public void testChangeProperties() {
		this.refactor_addProperty("ANOTHER_NAME", "ANOTHER_VALUE");
		this.doRefactor();
	}

	/**
	 * Tests removing a {@link WorkTaskModel}.
	 */
	public void testRemoveWorkTask() {
		this.refactor_mapTask("TASK_B", "TASK_B");
		this.refactor_includeTasks("TASK_B");
		this.doRefactor(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				context.addTask("TASK_B");
			}
		});
	}

	/**
	 * Tests removing a {@link WorkTaskModel} with connections.
	 */
	public void testRemoveWorkTaskWithConnections() {
		this.refactor_mapTask("TASK_B", "TASK_B");
		this.refactor_includeTasks("TASK_B");
		this.doRefactor(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				context.addTask("TASK_B");
			}
		});
	}

	/**
	 * Tests not including the {@link WorkTaskModel}.
	 */
	public void testNotIncludeWorkTask() {
		this.refactor_mapTask("TASK_A", "TASK_A");
		this.refactor_mapTask("TASK_B", "TASK_B");
		this.refactor_includeTasks("TASK_B"); // not include TASK_A
		this.doRefactor(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				// Task A
				TaskTypeConstructor taskA = context.addTask("TASK_A");
				taskA.addFlow(Object.class, null).setLabel("FLOW_A");
				taskA.addFlow(Object.class, null).setLabel("FLOW_B");
				taskA.addEscalation(SQLException.class);
				taskA.addEscalation(IOException.class);
				taskA.addObject(Object.class, null).setLabel("OBJECT");

				// Task B
				context.addTask("TASK_B");
			}
		});
	}

	/**
	 * Tests refactoring the {@link WorkTaskModel} and {@link TaskModel}.
	 */
	public void testRefactorWorkTask() {
		this.refactor_mapTask("TASK_NEW", "TASK_OLD");
		this.refactor_includeTasks("TASK_NEW");
		this.doRefactor(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				TaskTypeConstructor task = context.addTask("TASK_NEW");
				task.getBuilder().setReturnType(String.class);
			}
		});
	}

	/**
	 * Keys.
	 */
	public static enum Key {
		KEY_ONE, KEY_TWO
	}

	/**
	 * Tests refactoring the {@link WorkTaskObjectModel} instances.
	 */
	public void testRefactorWorkTaskObjects() {
		this.refactor_mapTask("TASK", "TASK");
		this.refactor_mapObject("TASK", "CHANGE_DETAILS", "CHANGE_DETAILS");
		this.refactor_mapObject("TASK", "RENAME_NEW", "RENAME_OLD");
		this.refactor_mapObject("TASK", "REORDER_A", "REORDER_A");
		this.refactor_mapObject("TASK", "REORDER_B", "REORDER_B");
		this.doRefactor(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				TaskTypeConstructor task = context.addTask("TASK");
				task.addObject(String.class, Key.KEY_TWO).setLabel(
						"CHANGE_DETAILS");
				task.addObject(Integer.class, null).setLabel("ADDED");
				task.addObject(String.class, null).setLabel("RENAME_NEW");
				task.addObject(Double.class, null).setLabel("REORDER_B");
				task.addObject(Float.class, null).setLabel("REORDER_A");
			}
		});
	}

	/**
	 * Tests refactoring the {@link TaskFlowModel} instances.
	 */
	public void testRefactorTaskFlows() {
		this.refactor_mapTask("WORK_TASK", "WORK_TASK");
		this.refactor_mapFlow("TASK", "CHANGE_DETAILS", "CHANGE_DETAILS");
		this.refactor_mapFlow("TASK", "RENAME_NEW", "RENAME_OLD");
		this.refactor_mapFlow("TASK", "REORDER_A", "REORDER_A");
		this.refactor_mapFlow("TASK", "REORDER_B", "REORDER_B");
		this.doRefactor(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				TaskTypeConstructor task = context.addTask("WORK_TASK");
				task.getBuilder().setReturnType(Long.class);
				task.addFlow(Byte.class, Key.KEY_TWO)
						.setLabel("CHANGE_DETAILS");
				task.addFlow(String.class, null).setLabel("ADDED");
				task.addFlow(Integer.class, null).setLabel("RENAME_NEW");
				task.addFlow(Double.class, null).setLabel("REORDER_B");
				task.addFlow(Float.class, null).setLabel("REORDER_A");
			}
		});
	}

	/**
	 * Tests refactoring the {@link TaskEscalationModel} instances.
	 */
	public void testRefactorTaskEscalations() {
		this.refactor_mapTask("WORK_TASK", "WORK_TASK");
		this.refactor_mapEscalation("TASK", RuntimeException.class.getName(),
				NullPointerException.class.getName());
		this.refactor_mapEscalation("TASK", Exception.class.getName(),
				Exception.class.getName());
		this.refactor_mapEscalation("TASK", Error.class.getName(), Error.class
				.getName());
		this.doRefactor(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				TaskTypeConstructor task = context.addTask("WORK_TASK");
				task.addEscalation(RuntimeException.class);
				task.addEscalation(SQLException.class);
				task.addEscalation(Error.class);
				task.addEscalation(Exception.class);
			}
		});
	}

}