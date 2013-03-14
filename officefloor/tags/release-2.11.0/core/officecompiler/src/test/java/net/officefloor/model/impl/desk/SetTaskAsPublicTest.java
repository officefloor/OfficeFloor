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
 * Tests setting the {@link TaskModel} as public.
 * 
 * @author Daniel Sagenschneider
 */
public class SetTaskAsPublicTest extends AbstractDeskChangesTestCase {

	/**
	 * Public {@link TaskModel}.
	 */
	private TaskModel publicTask;

	/**
	 * Private {@link TaskModel}.
	 */
	private TaskModel privateTask;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.compile.impl.desk.AbstractDeskOperationsTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the public and private tasks
		this.publicTask = this.model.getTasks().get(0);
		this.privateTask = this.model.getTasks().get(1);
	}

	/**
	 * Ensure no change if the {@link TaskModel} not on the {@link DeskModel}.
	 */
	public void testTaskNotOnDesk() {
		TaskModel task = new TaskModel("TASK", false, "WORK", "WORK_TASK", null);
		Change<TaskModel> change = this.operations.setTaskAsPublic(true, task);
		this.assertChange(change, task, "Set task TASK public", false,
				"Task TASK not on desk");
	}

	/**
	 * Ensures can set a {@link TaskModel} to be public.
	 */
	public void testTaskPublic() {
		Change<TaskModel> change = this.operations.setTaskAsPublic(true,
				this.privateTask);
		this.assertChange(change, this.privateTask, "Set task PRIVATE public",
				true);
	}

	/**
	 * Ensures can set a {@link TaskModel} to be private.
	 */
	public void testSetTaskPrivate() {
		Change<TaskModel> change = this.operations.setTaskAsPublic(false,
				this.publicTask);
		this.assertChange(change, this.publicTask, "Set task PUBLIC private",
				true);
	}

}