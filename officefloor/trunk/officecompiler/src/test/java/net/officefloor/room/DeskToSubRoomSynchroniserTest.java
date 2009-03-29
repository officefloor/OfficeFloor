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
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalEscalationModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.room.SubRoomEscalationModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;

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
				null, null, null, null, null, null, null, null, null));
		desk.addFlowItem(new FlowItemModel("IF-TWO", false, "work", "task",
				null, null, null, null, null, null, null, null, null));
		desk.addFlowItem(new FlowItemModel("IF-THREE", true, "work", "task",
				null, null, null, null, null, null, null, null, null));
		desk.addExternalFlow(new ExternalFlowModel("OF-ONE", null, null));
		desk.addExternalFlow(new ExternalFlowModel("OF-TWO", null, null));
		desk.addExternalEscalation(new ExternalEscalationModel("ES-ONE",
				SQLException.class.getName(), null));
		desk.addExternalEscalation(new ExternalEscalationModel("ES-TWO",
				IOException.class.getName(), null));

		// Create the SubRoom
		SubRoomModel subRoom = new SubRoomModel();

		// Synchronise
		DeskToSubRoomSynchroniser.synchroniseDeskOntoSubRoom(desk, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getManagedObjects(), new SubRoomManagedObjectModel("MO-ONE",
				String.class.getName(), null), new SubRoomManagedObjectModel(
				"MO-TWO", Connection.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getInputFlows(), new SubRoomInputFlowModel("IF-ONE", false,
				null, null), new SubRoomInputFlowModel("IF-THREE", false, null,
				null));

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

		// Remove one of each from desk
		desk.removeExternalManagedObject(desk.getExternalManagedObjects()
				.get(1));
		desk.removeFlowItem(desk.getFlowItems().get(2));
		desk.removeExternalFlow(desk.getExternalFlows().get(1));
		desk.removeExternalEscalation(desk.getExternalEscalations().get(1));

		// Synchronise again
		DeskToSubRoomSynchroniser.synchroniseDeskOntoSubRoom(desk, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getManagedObjects(), new SubRoomManagedObjectModel("MO-ONE",
				String.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getInputFlows(), new SubRoomInputFlowModel("IF-ONE", false,
				null, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getOutputFlows(),
				new SubRoomOutputFlowModel("OF-ONE", null, null));

		// Validate the escalations
		assertList(new String[] { "getName", "getEscalationType" }, subRoom
				.getEscalations(), new SubRoomEscalationModel("ES-ONE",
				SQLException.class.getName(), null, null));

		// Add one of each to desk
		desk.addExternalManagedObject(new ExternalManagedObjectModel(
				"MO-THREE", "java.lang.Integer", null));
		desk.addFlowItem(new FlowItemModel("IF-FOUR", false, "work", "task",
				null, null, null, null, null, null, null, null, null));
		desk.addFlowItem(new FlowItemModel("IF-FIVE", true, "work", "task",
				null, null, null, null, null, null, null, null, null));
		desk.addExternalFlow(new ExternalFlowModel("OF-THREE", null, null));
		desk.addExternalEscalation(new ExternalEscalationModel("ES-THREE",
				"java.lang.NullPointerException", null));

		// Synchronise again
		DeskToSubRoomSynchroniser.synchroniseDeskOntoSubRoom(desk, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getManagedObjects(), new SubRoomManagedObjectModel("MO-ONE",
				String.class.getName(), null), new SubRoomManagedObjectModel(
				"MO-THREE", Integer.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getInputFlows(), new SubRoomInputFlowModel("IF-ONE", false,
				null, null), new SubRoomInputFlowModel("IF-FIVE", false, null,
				null));

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
