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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.room.ExternalEscalationModel;
import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomEscalationModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;

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
		subRoomOne.addInputFlow(new SubRoomInputFlowModel("IF-ONE", true, null,
				null));
		room.addSubRoom(subRoomOne);
		SubRoomModel subRoomTwo = new SubRoomModel("SR-TWO", "desk", null,
				null, null, null, null);
		subRoomTwo.addInputFlow(new SubRoomInputFlowModel("IF-TWO", false,
				null, null));
		room.addSubRoom(subRoomTwo);
		SubRoomModel subRoomThree = new SubRoomModel("SR-THREE", "desk", null,
				null, null, null, null);
		subRoomThree.addInputFlow(new SubRoomInputFlowModel("IF-THREE", true,
				null, null));
		room.addSubRoom(subRoomThree);

		// Add escalations
		room.addExternalEscalation(new ExternalEscalationModel("ES-ONE",
				"java.sql.SQLException", null));
		room.addExternalEscalation(new ExternalEscalationModel("ES-TWO",
				"java.io.IOException", null));

		// Create the SubRoom
		SubRoomModel subRoom = new SubRoomModel();

		// Synchronise
		RoomToSubRoomSynchroniser.synchroniseRoomOntoSubRoom(room, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getManagedObjects(), new SubRoomManagedObjectModel("MO-ONE",
				String.class.getName(), null), new SubRoomManagedObjectModel(
				"MO-TWO", Connection.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getInputFlows(), new SubRoomInputFlowModel("SR-ONE-IF-ONE",
				false, null, null), new SubRoomInputFlowModel(
				"SR-THREE-IF-THREE", false, null, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getOutputFlows(),
				new SubRoomOutputFlowModel("OF-ONE", null, null),
				new SubRoomOutputFlowModel("OF-TWO", null, null));

		// Validate the escalations
		assertList(new String[] { "getName", "getEscalationType" }, subRoom
				.getEscalations(), new SubRoomEscalationModel("ES-ONE",
				SQLException.class.getName(), null, null),
				new SubRoomEscalationModel("ES-TWO", IOException.class
						.getName(), null, null));

		// Remove one of each from room
		room.removeExternalManagedObject(room.getExternalManagedObjects()
				.get(1));
		room.removeExternalFlow(room.getExternalFlows().get(1));
		room.removeSubRoom(room.getSubRooms().get(2));
		room.removeExternalEscalation(room.getExternalEscalations().get(1));

		// Synchronise
		RoomToSubRoomSynchroniser.synchroniseRoomOntoSubRoom(room, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getManagedObjects(), new SubRoomManagedObjectModel("MO-ONE",
				String.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getInputFlows(), new SubRoomInputFlowModel("SR-ONE-IF-ONE",
				false, null, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getOutputFlows(),
				new SubRoomOutputFlowModel("OF-ONE", null, null));

		// Validate the escalations
		assertList(new String[] { "getName", "getEscalationType" }, subRoom
				.getEscalations(), new SubRoomEscalationModel("ES-ONE",
				SQLException.class.getName(), null, null));

		// Add one of each to room
		room.addExternalManagedObject(new ExternalManagedObjectModel(
				"MO-THREE", "java.lang.String", null));
		room.addExternalFlow(new ExternalFlowModel("OF-THREE", null));
		SubRoomModel subRoomFour = new SubRoomModel("SR-FOUR", "desk", null,
				null, null, null, null);
		subRoomFour.addInputFlow(new SubRoomInputFlowModel("IF-FOUR", true,
				null, null));
		room.addSubRoom(subRoomFour);
		room.addExternalEscalation(new ExternalEscalationModel("ES-THREE",
				"java.lang.NullPointerException", null));

		// Synchronise
		RoomToSubRoomSynchroniser.synchroniseRoomOntoSubRoom(room, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getManagedObjects(), new SubRoomManagedObjectModel("MO-ONE",
				String.class.getName(), null), new SubRoomManagedObjectModel(
				"MO-THREE", String.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getInputFlows(), new SubRoomInputFlowModel("SR-ONE-IF-ONE",
				false, null, null), new SubRoomInputFlowModel(
				"SR-FOUR-IF-FOUR", false, null, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getOutputFlows(),
				new SubRoomOutputFlowModel("OF-ONE", null, null),
				new SubRoomOutputFlowModel("OF-THREE", null, null));

		// Validate the escalations
		assertList(new String[] { "getName", "getEscalationType" }, subRoom
				.getEscalations(), new SubRoomEscalationModel("ES-ONE",
				SQLException.class.getName(), null, null),
				new SubRoomEscalationModel("ES-THREE",
						NullPointerException.class.getName(), null, null));
	}
}
