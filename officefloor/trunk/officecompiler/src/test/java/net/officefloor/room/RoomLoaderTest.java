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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.room.EscalationToInputFlowModel;
import net.officefloor.model.room.ExternalEscalationModel;
import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.ManagedObjectToExternalManagedObjectModel;
import net.officefloor.model.room.OutputFlowToExternalFlowModel;
import net.officefloor.model.room.OutputFlowToInputFlowModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomEscalationModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;

/**
 * Tests the {@link net.officefloor.room.RoomLoader}.
 * 
 * @author Daniel
 */
public class RoomLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link RoomLoader} being tested.
	 */
	private RoomLoader roomLoader;

	/**
	 * {@link net.net.officefloor.model.repository.ConfigurationItem} to the
	 * {@link RoomModel}.
	 */
	private FileSystemConfigurationItem configurationItem;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {

		// Create Room Loader to test
		this.roomLoader = new RoomLoader();

		// Obtain the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "TestRoom.room.xml"), null);
	}

	/**
	 * Ensure loads the {@link net.officefloor.model.room.RoomModel}.
	 */
	public void testLoadRoom() throws Exception {

		// Load the Room
		RoomModel room = this.roomLoader.loadRoom(this.configurationItem);

		// ===================================
		// Validate the Room
		// ===================================

		// Validate the sub rooms
		assertList(
				new String[] { "getId", "getDesk", "getRoom", "getX", "getY" },
				room.getSubRooms(), new SubRoomModel("1", "TestDesk.desk.xml",
						null, null, null, null, null, 100, 20),
				new SubRoomModel("2", null, "TestSubRoom.room.xml", null, null,
						null, null, 200, 30));

		// ===================================
		// Validate the sub room one
		// ===================================

		SubRoomModel subRoomOne = room.getSubRooms().get(0);

		// Validate input flows
		assertList(new String[] { "getName", "getIsPublic" }, subRoomOne
				.getInputFlows(), new SubRoomInputFlowModel("input", true,
				null, null));

		// Validate output flows
		assertList(new String[] { "getName" }, subRoomOne.getOutputFlows(),
				new SubRoomOutputFlowModel("output", null, null));
		OutputFlowToInputFlowModel outputToInput = subRoomOne.getOutputFlows()
				.get(0).getInput();
		assertProperties(new OutputFlowToInputFlowModel("2", "input", null,
				null), outputToInput, "getSubRoomName", "getInputFlowName");

		// Validate managed objects
		SubRoomManagedObjectModel subRoomOneMo = subRoomOne.getManagedObjects()
				.get(0);
		assertList(new String[] { "getName", "getObjectType",
				"getExternalManagedObject" }, subRoomOne.getManagedObjects(),
				new SubRoomManagedObjectModel("mo", "java.lang.String",
						subRoomOneMo.getExternalManagedObject()));
		assertProperties(new ManagedObjectToExternalManagedObjectModel("mo",
				subRoomOneMo, room.getExternalManagedObjects().get(0)),
				subRoomOneMo.getExternalManagedObject(), "getName",
				"getManagedObject", "getExternalManagedObject");

		// Validate escalations
		assertList(new String[] { "getName", "getEscalationType" }, subRoomOne
				.getEscalations(), new SubRoomEscalationModel("escalation",
				SQLException.class.getName(), null, null));

		// ===================================
		// Validate the sub room two
		// ===================================

		SubRoomModel subRoomTwo = room.getSubRooms().get(1);

		// Validate input flows
		assertList(new String[] { "getName", "getIsPublic" }, subRoomTwo
				.getInputFlows(), new SubRoomInputFlowModel("input", false,
				null, null));

		// Validate output flows
		assertList(new String[] { "getName" }, subRoomTwo.getOutputFlows(),
				new SubRoomOutputFlowModel("output", null, null));
		OutputFlowToExternalFlowModel outputToExternalFlow = subRoomTwo
				.getOutputFlows().get(0).getExternalFlow();
		assertProperties(new OutputFlowToExternalFlowModel("flow", null, null),
				outputToExternalFlow, "getExternalFlowName");

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType",
				"getExternalManagedObject" }, subRoomTwo.getManagedObjects(),
				new SubRoomManagedObjectModel("mo", "java.lang.String", null));

		// Validate escalations
		assertList(new String[] { "getName", "getEscalationType" }, subRoomTwo
				.getEscalations(), new SubRoomEscalationModel("escalation",
				IOException.class.getName(), null, null));
		EscalationToInputFlowModel expectedEscalationToInput = subRoomOne
				.getInputFlows().get(0).getEscalations().get(0);
		EscalationToInputFlowModel actualEscalationToInput = subRoomTwo
				.getEscalations().get(0).getInputFlow();
		assertEquals("Incorrect escalation being handled",
				expectedEscalationToInput, actualEscalationToInput);

		// ===================================
		// Validate the external managed objects
		// ===================================

		assertList(new String[] { "getName", "getObjectType", "getX", "getY" },
				room.getExternalManagedObjects(),
				new ExternalManagedObjectModel("mo", "java.lang.String", null,
						10, 11));
		assertEquals("Incorrect sub room mo", subRoomOne.getManagedObjects()
				.get(0), room.getExternalManagedObjects().get(0)
				.getSubRoomManagedObjects().get(0).getManagedObject());

		// ===================================
		// Validate the external flows
		// ===================================

		assertList(new String[] { "getName", "getX", "getY" }, room
				.getExternalFlows(),
				new ExternalFlowModel("flow", null, 20, 21));
		assertEquals("Incorrect sub room output", subRoomTwo.getOutputFlows()
				.get(0), room.getExternalFlows().get(0).getOutputs().get(0)
				.getOutput());

		// ===================================
		// Validate the external escalations
		// ===================================

		assertList(new String[] { "getName", "getEscalationType", "getX",
				"getY" }, room.getExternalEscalations(),
				new ExternalEscalationModel("escalation", SQLException.class
						.getName(), null, 30, 31));
		SubRoomEscalationModel expectedEscalation = subRoomOne.getEscalations()
				.get(0);
		SubRoomEscalationModel actualEscalation = room.getExternalEscalations()
				.get(0).getSubRoomEscalations().get(0).getEscalation();
		assertEquals("Incorrect sub room escalation", expectedEscalation,
				actualEscalation);
	}

	/**
	 * Ensures able to load, store and load the {@link RoomModel} (round trip).
	 */
	public void testLoadAndStore() throws Exception {

		// Load the Room Model
		RoomModel room = this.roomLoader.loadRoom(this.configurationItem);

		// Store the Room
		File tempFile = File.createTempFile("TestRoom.room.xml", null);
		FileSystemConfigurationItem tempConfigItem = new FileSystemConfigurationItem(
				tempFile, null);
		this.roomLoader.storeRoom(room, tempConfigItem);

		// Reload the Room
		RoomModel reloadedRoom = this.roomLoader.loadRoom(tempConfigItem);

		// Validate round trip
		assertGraph(room, reloadedRoom,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}
}
