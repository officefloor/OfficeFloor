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

import net.officefloor.compile.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.TaskModel;

/**
 * Tests renaming the {@link TaskModel}.
 * 
 * @author Daniel
 */
public class RenameTaskTest extends AbstractDeskOperationsTestCase {

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
		TaskModel task = this.desk.getTasks().get(0);
		Change<TaskModel> change = this.operations.renameTask(task, "NEW_NAME");
		this.assertChange(change, task, "Rename task OLD_NAME to NEW_NAME",
				true);
	}

	/**
	 * Ensures on renaming the {@link TaskModel} that order is maintained.
	 */
	public void testRenameTaskCausingTaskOrderChange() {
		this.useTestSetupDesk();
		TaskModel task = this.desk.getTasks().get(0);
		Change<TaskModel> change = this.operations.renameTask(task, "TASK_C");
		this.assertChange(change, task, "Rename task TASK_A to TASK_C", true);
	}
}