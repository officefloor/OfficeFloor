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
package net.officefloor.room;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;

/**
 * Tests the {@link net.officefloor.room.DeskToSubRoomSynchroniser}.
 * 
 * @author Daniel
 */
public class DeskToSubRoomSynchroniserTest extends OfficeFrameTestCase {

	/**
	 * Ensures correctly synchronises the
	 * {@link net.officefloor.model.desk.DeskModel} onto the
	 * {@link net.officefloor.model.room.SubRoomModel}.
	 */
	public void testSynchroniseDeskOntoSubRoom() {

		// Create the DeskModel
		DeskModel desk = new DeskModel();
		desk.addExternalManagedObject(new ExternalManagedObjectModel("MO-ONE",
				"java.lang.String", null));
		desk.addExternalManagedObject(new ExternalManagedObjectModel("MO-TWO",
				"java.sql.Connection", null));
		desk.addFlowItem(new FlowItemModel("IF-ONE", true, "work", "task",
				null, null, null, null, null, null, null, null, null, null));
		desk.addFlowItem(new FlowItemModel("IF-TWO", false, "work", "task",
				null, null, null, null, null, null, null, null, null, null));
		desk.addFlowItem(new FlowItemModel("IF-THREE", true, "work", "task",
				null, null, null, null, null, null, null, null, null, null));
		desk.addExternalFlow(new ExternalFlowModel("OF-ONE", null, null));
		desk.addExternalFlow(new ExternalFlowModel("OF-TWO", null, null));

		// Create the SubRoom
		SubRoomModel subRoom = new SubRoomModel();

		// Synchronise
		DeskToSubRoomSynchroniser.synchroniseDeskOntoSubRoom(desk, subRoom);

		// Validate managed objects
		assertEquals("Incorrect mo count", 2, subRoom.getManagedObjects()
				.size());
		SubRoomManagedObjectModel moOne = subRoom.getManagedObjects().get(0);
		assertEquals("Incorrect mo one name", "MO-ONE", moOne.getName());
		assertEquals("Incorrect mo one type", "java.lang.String", moOne
				.getObjectType());
		SubRoomManagedObjectModel moTwo = subRoom.getManagedObjects().get(1);
		assertEquals("Incorrect mo two name", "MO-TWO", moTwo.getName());
		assertEquals("Incorrect mo two type", "java.sql.Connection", moTwo
				.getObjectType());

		// Validate input flow items
		assertEquals("Incorrect input flow count", 2, subRoom.getInputFlows()
				.size());
		assertEquals("Incorrect if one", "IF-ONE", subRoom.getInputFlows().get(
				0).getName());
		assertEquals("Incorrect if two", "IF-THREE", subRoom.getInputFlows()
				.get(1).getName());

		// Validate output flow items
		assertEquals("Incorrect output flow count", 2, subRoom.getOutputFlows()
				.size());
		assertEquals("Incorrect of one", "OF-ONE", subRoom.getOutputFlows()
				.get(0).getName());
		assertEquals("Incorrect of two", "OF-TWO", subRoom.getOutputFlows()
				.get(1).getName());
	}
}
