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
package net.officefloor.compile.test.archive;

import net.officefloor.frame.test.ListItemMatcher;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.WorkModel;

/**
 * Tests the {@link net.officefloor.compile.test.archive.WorkToDeskWorkSynchroniser}.
 * 
 * @author Daniel
 */
// Provide do/undo synchronise
@Deprecated
public class WorkToDeskWorkSynchroniserTest extends OfficeFrameTestCase {

	/**
	 * Ensure synchronises.
	 */
	@SuppressWarnings("unchecked")
	public void testWorkToDeskWorkSynchronise() {

		// Create the work
		WorkType<?> work = new WorkType(null, null,
				new TaskModel[] {
						new TaskModel("taskOne", null, null, null, null, null,
								null),
						new TaskModel("taskTwo", null, null, null, null, null,
								null),
						new TaskModel("taskThree", null, null, null, null,
								null, null) });

		// Create the desk work
		DeskWorkModel deskWork = new DeskWorkModel("work", "loader", null,
				null, new DeskTaskModel[] {
						new DeskTaskModel("taskTwo", null, null, null),
						new DeskTaskModel("taskNotExist", null, null, null) },
				null);

		// Synchronise the work
		WorkToDeskWorkSynchroniser.synchroniseWorkOntoDeskWork(work, deskWork);

		// Validate all tasks loaded
		assertList(new String[] { "getName" }, deskWork.getTasks(),
				new DeskTaskModel("taskTwo", null, null, null),
				new DeskTaskModel("taskOne", null, null, null),
				new DeskTaskModel("taskThree", null, null, null));
		assertList(new ListItemMatcher<DeskTaskModel>() {
			public void match(int index, DeskTaskModel expected,
					DeskTaskModel actual) {
				assertNotNull(
						"Item " + index + " must have an underlying Task",
						actual.getTask());
			}
		}, deskWork.getTasks(), new DeskTaskModel(), new DeskTaskModel(),
				new DeskTaskModel());

		// Add another task
		work.addTask(new TaskModel("taskFour", null, null, null, null, null,
				null));

		// Synchronise the work again
		WorkToDeskWorkSynchroniser.synchroniseWorkOntoDeskWork(work, deskWork);

		// Validate task synchronised
		assertEquals("Incorrect task added", "taskFour", deskWork.getTasks()
				.get(3).getName());

		// Remove a task (taskThree)
		work.removeTask(work.getTasks().get(2));

		// Synchronise the work again
		WorkToDeskWorkSynchroniser.synchroniseWorkOntoDeskWork(work, deskWork);

		// Validate synchronised
		assertList(new String[] { "getName" }, deskWork.getTasks(),
				new DeskTaskModel("taskTwo", null, null, null),
				new DeskTaskModel("taskOne", null, null, null),
				new DeskTaskModel("taskFour", null, null, null));
	}
}
