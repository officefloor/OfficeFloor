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

import net.officefloor.compile.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.TaskModel;

/**
 * Tests setting the {@link TaskModel} as public.
 * 
 * @author Daniel
 */
public class SetTaskAsPublicTest extends AbstractDeskOperationsTestCase {

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
		this.publicTask = this.desk.getTasks().get(0);
		this.privateTask = this.desk.getTasks().get(1);
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