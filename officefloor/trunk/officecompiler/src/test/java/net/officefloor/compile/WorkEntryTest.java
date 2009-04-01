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
package net.officefloor.compile;

import net.officefloor.compile.impl.work.WorkTypeImpl;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.room.RoomModel;

/**
 * Tests the {@link WorkEntry}.
 * 
 * @author Daniel
 */
public class WorkEntryTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link WorkBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private WorkBuilder<Work> workBuilder = this.createMock(WorkBuilder.class);

	/**
	 * Mock {@link OfficeBuilder}.
	 */
	private OfficeBuilder officeBuilder = this.createMock(OfficeBuilder.class);
	
	/**
	 * Mock {@link WorkFactory}.
	 */
	@SuppressWarnings("unchecked")
	private WorkFactory<Work> workFactory = this.createMock(WorkFactory.class);

	/**
	 * Ensures can load {@link Work} with no initial {@link Task}.
	 */
	public void testNoInitialTask() throws Exception {

		// Record mock objects
		this.officeBuilder.addWork("DESK.WORK", this.workFactory);

		// Replay mock objects
		this.replayMockObjects();

		// Create the office entry
		OfficeModel office = new OfficeModel();
		OfficeEntry officeEntry = new OfficeEntry("OFFICE",
				this.officeBuilder, office, null);

		// Create the room entry
		RoomModel room = new RoomModel();
		RoomEntry roomEntry = new RoomEntry("ROOM", room, officeEntry);

		// Create the desk work
		WorkTypeImpl<Work> work = new WorkTypeImpl<Work>();
		work.setWorkFactory(this.workFactory);
		WorkModel deskWork = new WorkModel();
		deskWork.setWorkName("WORK");
		deskWork.setInitialTask(null);	// no initial task

		// Create the desk entry
		DeskModel desk = new DeskModel();
		DeskEntry deskEntry = new DeskEntry("DESK_ID", "DESK", desk,
				roomEntry);

		// Create the office floor compiler context
		OfficeFloorCompilerContext context = new OfficeFloorCompilerContext(
				null, null, null);

		// Load the work
		WorkEntry<?> workEntry = WorkEntry.loadWork(deskWork, deskEntry,
				context);

		// Build the work
		workEntry.build();

		// Verify mock objects
		this.verifyMockObjects();
	}

}
