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
package net.officefloor.model.impl.desk;

import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.TaskModel;

/**
 * Tests removing a {@link TaskModel} from a {@link DeskModel}.
 * 
 * @author Daniel
 */
public class RemoveTaskTest extends AbstractDeskOperationsTestCase {

	/**
	 * Initiate to use specific setup {@link DeskModel}.
	 */
	public RemoveTaskTest() {
		super(true);
	}

	/**
	 * Tests attempting to remove a {@link TaskModel} not on the
	 * {@link DeskModel}.
	 */
	public void testRemoveTaskNotOnDesk() {
		TaskModel task = new TaskModel("NOT_ON_DESK", false, "WORK",
				"WORK_TASK", null);
		Change<TaskModel> change = this.operations.removeTask(task);
		this.assertChange(change, task, "Remove task NOT_ON_DESK", false,
				"Task NOT_ON_DESK not on desk");
	}

	/**
	 * Ensure can remove the {@link TaskModel} from the {@link DeskModel} when
	 * other {@link TaskModel} instances on the {@link DeskModel}.
	 */
	public void testRemoveTaskWhenOtherTasks() {
		TaskModel task = this.model.getTasks().get(1);
		Change<TaskModel> change = this.operations.removeTask(task);
		this.assertChange(change, task, "Remove task TASK_B", true);
	}

	/**
	 * Ensure can remove the connected {@link TaskModel} from the
	 * {@link DeskModel}.
	 */
	public void testRemoveTaskWithConnections() {
		TaskModel task = this.model.getTasks().get(0);
		Change<TaskModel> change = this.operations.removeTask(task);
		this.assertChange(change, task, "Remove task TASK_A", true);
	}

}