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
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;

/**
 * Tests removing a {@link WorkTaskModel} from a {@link WorkModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveWorkTaskTest extends AbstractDeskChangesTestCase {

	/**
	 * {@link WorkModel}.
	 */
	private WorkModel work;

	/**
	 * Initiate to use specific setup {@link DeskModel}.
	 */
	public RemoveWorkTaskTest() {
		super(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.compile.impl.desk.AbstractDeskOperationsTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the work and work task model
		this.work = this.model.getWorks().get(0);
	}

	/**
	 * Tests attempting to remove a {@link WorkTaskModel} not on the
	 * {@link WorkModel}.
	 */
	public void testRemoveWorkTaskNotOnWork() {
		WorkTaskModel workTask = new WorkTaskModel("NOT_ON_WORK");
		Change<WorkTaskModel> change = this.operations.removeWorkTask(
				this.work, workTask);
		this.assertChange(change, workTask, "Remove work task NOT_ON_WORK",
				false, "Work task NOT_ON_WORK not on work WORK");
	}

	/**
	 * Ensure can remove the {@link WorkTaskModel} from the {@link WorkModel}
	 * when other {@link WorkTaskModel} instances on the {@link WorkModel}.
	 */
	public void testRemoveWorkTaskWhenOtherWorkTasks() {
		WorkTaskModel workTask = this.work.getWorkTasks().get(1);
		Change<WorkTaskModel> change = this.operations.removeWorkTask(
				this.work, workTask);
		this.assertChange(change, workTask, "Remove work task TASK_B", true);
	}

	/**
	 * Ensure can remove the connected {@link WorkTaskModel} from the
	 * {@link WorkModel}.
	 */
	public void testRemoveWorkTaskWithConnections() {
		WorkTaskModel workTask = this.work.getWorkTasks().get(0);
		Change<WorkTaskModel> change = this.operations.removeWorkTask(
				this.work, workTask);
		this.assertChange(change, workTask, "Remove work task WORK_TASK_A",
				true);
	}

}