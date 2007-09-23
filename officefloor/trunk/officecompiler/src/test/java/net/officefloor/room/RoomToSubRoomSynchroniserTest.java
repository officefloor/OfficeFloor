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

import net.officefloor.frame.util.OfficeFrameTestCase;
import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;

/**
 * Tests the {@link net.officefloor.room.RoomToSubRoomSynchroniser}.
 * 
 * @author Daniel
 */
public class RoomToSubRoomSynchroniserTest extends OfficeFrameTestCase {

	/**
	 * Ensures correctly synchronises the
	 * {@link net.officefloor.model.room.RoomModel} onto the
	 * {@link net.officefloor.model.room.SubRoomModel}.
	 */
	public void testSynchroniseRoomOntoSubRoom() {

		// Create the Room Model
		RoomModel room = new RoomModel();
		room.addExternalManagedObject(new ExternalManagedObjectModel("MO-ONE",
				"java.lang.String", null));
		room.addExternalManagedObject(new ExternalManagedObjectModel("MO-TWO",
				"java.sql.Connection", null));
		room.addExternalFlow(new ExternalFlowModel("OF-ONE", null));
		room.addExternalFlow(new ExternalFlowModel("OF-TWO", null));

		// Add sub rooms
		SubRoomModel subRoomOne = new SubRoomModel("SR-ONE", "desk", null,
				null, null, null, null);
		subRoomOne
				.addInputFlow(new SubRoomInputFlowModel("IF-ONE", true, null));
		room.addSubRoom(subRoomOne);
		SubRoomModel subRoomTwo = new SubRoomModel("SR-TWO", "desk", null,
				null, null, null, null);
		subRoomTwo
				.addInputFlow(new SubRoomInputFlowModel("IF-TWO", false, null));
		room.addSubRoom(subRoomTwo);
		SubRoomModel subRoomThree = new SubRoomModel("SR-THREE", "desk", null,
				null, null, null, null);
		subRoomThree.addInputFlow(new SubRoomInputFlowModel("IF-THREE", true,
				null));
		room.addSubRoom(subRoomThree);

		// Create the SubRoom
		SubRoomModel subRoom = new SubRoomModel();

		// Synchronise
		RoomToSubRoomSynchroniser.synchroniseRoomOntoSubRoom(room, subRoom);

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
		assertEquals("Incorrect if one", "SR-ONE-IF-ONE", subRoom
				.getInputFlows().get(0).getName());
		assertEquals("Incorrect if two", "SR-THREE-IF-THREE", subRoom
				.getInputFlows().get(1).getName());

		// Validate output flow items
		assertEquals("Incorrect output flow count", 2, subRoom.getOutputFlows()
				.size());
		assertEquals("Incorrect of one", "OF-ONE", subRoom.getOutputFlows()
				.get(0).getName());
		assertEquals("Incorrect of two", "OF-TWO", subRoom.getOutputFlows()
				.get(1).getName());
	}
}
