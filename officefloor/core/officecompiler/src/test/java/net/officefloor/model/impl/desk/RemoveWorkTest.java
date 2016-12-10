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

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;

/**
 * Tests removing a {@link WorkModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveWorkTest extends AbstractDeskChangesTestCase {

	/**
	 * Specific setup file per test.
	 */
	public RemoveWorkTest() {
		super(true);
	}

	/**
	 * Ensure no {@link Change} for removing {@link WorkModel} not on the
	 * {@link DeskModel}.
	 */
	public void testRemoveWorkNotOnDesk() {
		// Attempt to remove work not on the desk
		WorkModel workNotOnDesk = new WorkModel("NOT ON DESK", null);
		Change<WorkModel> change = this.operations.removeWork(workNotOnDesk);
		this.assertChange(change, workNotOnDesk, "Remove work NOT ON DESK",
				false, "Work NOT ON DESK not on desk");
	}

	/**
	 * Ensure can remove {@link WorkModel} without any connections.
	 */
	public void testRemoveWorkWithNoConnections() {
		// Obtain the work and remove it
		WorkModel work = this.model.getWorks().get(0);
		Change<WorkModel> change = this.operations.removeWork(work);
		this.assertChange(change, work, "Remove work WORK", true);
	}

	/**
	 * Ensure can remove {@link WorkModel} with a {@link TaskModel}.
	 */
	public void testRemoveWorkWithATask() {
		// Obtain the work and remove it
		WorkModel work = this.model.getWorks().get(0);
		Change<WorkModel> change = this.operations.removeWork(work);
		this.assertChange(change, work, "Remove work WORK", true);
	}

	/**
	 * Ensure can remove {@link WorkModel} with a {@link TaskModel} while there
	 * are other {@link WorkModel} and {@link TaskModel} instances.
	 */
	public void testRemoveWorkWhenOtherWorkAndTasks() {
		// Obtain the work and remove it
		WorkModel work = this.model.getWorks().get(1);
		Change<WorkModel> change = this.operations.removeWork(work);
		this.assertChange(change, work, "Remove work WORK_B", true);
	}

	/**
	 * Ensure can remove {@link WorkModel} with {@link ConnectionModel}
	 * instances connected.
	 */
	public void testRemoveWorkWithConnections() {
		// Obtain the work and remove it
		WorkModel work = this.model.getWorks().get(0);
		Change<WorkModel> change = this.operations.removeWork(work);
		this.assertChange(change, work, "Remove work WORK_A", true);
	}

}