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

import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.TaskModel;

/**
 * Tests renaming the {@link TaskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RenameTaskTest extends AbstractDeskChangesTestCase {

	/**
	 * Ensures handles {@link TaskModel} not being on the {@link DeskModel}.
	 */
	public void testRenameTaskNotOnDesk() {
		TaskModel task = new TaskModel("NOT_ON_DESK", false, "WORK",
				"WORK_TASK", null);
		Change<TaskModel> change = this.operations.renameTask(task, "NEW_NAME");
		this.assertChange(change, task, "Rename task NOT_ON_DESK to NEW_NAME",
				false, "Task NOT_ON_DESK not on desk");
	}

	/**
	 * Ensure can rename the {@link TaskModel}.
	 */
	public void testRenameTask() {
		TaskModel task = this.model.getTasks().get(0);
		Change<TaskModel> change = this.operations.renameTask(task, "NEW_NAME");
		this.assertChange(change, task, "Rename task OLD_NAME to NEW_NAME",
				true);
	}

	/**
	 * Ensures on renaming the {@link TaskModel} that order is maintained.
	 */
	public void testRenameTaskCausingTaskOrderChange() {
		this.useTestSetupModel();
		TaskModel task = this.model.getTasks().get(0);
		Change<TaskModel> change = this.operations.renameTask(task, "TASK_C");
		this.assertChange(change, task, "Rename task TASK_A to TASK_C", true);
	}
}