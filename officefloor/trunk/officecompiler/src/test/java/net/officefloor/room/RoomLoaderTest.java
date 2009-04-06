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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

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
	 * {@link SectionModel}.
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
	 * Ensure loads the {@link net.officefloor.model.section.SectionModel}.
	 */
	public void testLoadRoom() throws Exception {

		// Load the Room
		SectionModel room = this.roomLoader.loadRoom(this.configurationItem);

		// ===================================
		// Validate the Room
		// ===================================

		// Validate the sub rooms
		assertList(
				new String[] { "getId", "getDesk", "getRoom", "getX", "getY" },
				room.getSubSections(), new SubSectionModel("1",
						"TestDesk.desk.xml", null, null, null, null, null, 100,
						20),
				new SubSectionModel("2", null, "TestSubRoom.room.xml", null,
						null, null, null, 200, 30));

		// ===================================
		// Validate the sub room one
		// ===================================

		SubSectionModel subRoomOne = room.getSubSections().get(0);

		// Validate input flows
		assertList(new String[] { "getName", "getIsPublic" }, subRoomOne
				.getSubSectionInputs(), new SubSectionInputModel("input",
				Object.class.getName(), true, null));

		// Validate output flows
		assertList(new String[] { "getName" }, subRoomOne
				.getSubSectionOutputs(), new SubSectionOutputModel("output",
				Object.class.getName(), false));
		SubSectionOutputToSubSectionInputModel outputToInput = subRoomOne
				.getSubSectionOutputs().get(0).getSubSectionInput();
		assertProperties(new SubSectionOutputToSubSectionInputModel("2",
				"input", null, null), outputToInput, "getSubRoomName",
				"getInputFlowName");

		// Validate managed objects
		SubSectionObjectModel subRoomOneMo = subRoomOne.getSubSectionObjects()
				.get(0);
		assertList(new String[] { "getName", "getObjectType",
				"getExternalManagedObject" },
				subRoomOne.getSubSectionObjects(), new SubSectionObjectModel(
						"mo", "java.lang.String", subRoomOneMo
								.getExternalManagedObject()));
		assertProperties(new SubSectionObjectToExternalManagedObjectModel("mo",
				subRoomOneMo, room.getExternalManagedObjects().get(0)),
				subRoomOneMo.getExternalManagedObject(), "getName",
				"getManagedObject", "getExternalManagedObject");

		// ===================================
		// Validate the sub room two
		// ===================================

		SubSectionModel subRoomTwo = room.getSubSections().get(1);

		// Validate input flows
		assertList(new String[] { "getName", "getIsPublic" }, subRoomTwo
				.getSubSectionInputs(), new SubSectionInputModel("input",
				Object.class.getName(), false, null));

		// Validate output flows
		assertList(new String[] { "getName" }, subRoomTwo
				.getSubSectionOutputs(), new SubSectionOutputModel("output",
				Object.class.getName(), false));
		SubSectionOutputToExternalFlowModel outputToExternalFlow = subRoomTwo
				.getSubSectionOutputs().get(0).getExternalFlow();
		assertProperties(new SubSectionOutputToExternalFlowModel("flow", null,
				null), outputToExternalFlow, "getExternalFlowName");

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType",
				"getExternalManagedObject" },
				subRoomTwo.getSubSectionObjects(), new SubSectionObjectModel(
						"mo", "java.lang.String", null));

		// ===================================
		// Validate the external managed objects
		// ===================================

		assertList(new String[] { "getName", "getObjectType", "getX", "getY" },
				room.getExternalManagedObjects(),
				new ExternalManagedObjectModel("mo", "java.lang.String", null,
						10, 11));
		assertEquals("Incorrect sub room mo", subRoomOne.getSubSectionObjects()
				.get(0), room.getExternalManagedObjects().get(0)
				.getSubSectionObjects().get(0).getSubSectionObject());

		// ===================================
		// Validate the external flows
		// ===================================

		assertList(new String[] { "getName", "getX", "getY" }, room
				.getExternalFlows(), new ExternalFlowModel("flow", Object.class
				.getName(), null, 20, 21));
		assertEquals("Incorrect sub room output", subRoomTwo
				.getSubSectionOutputs().get(0), room.getExternalFlows().get(0)
				.getSubSectionOutputs().get(0).getSubSectionOutput());
	}

	/**
	 * Ensures able to load, store and load the {@link SectionModel} (round
	 * trip).
	 */
	public void testLoadAndStore() throws Exception {

		// Load the Room Model
		SectionModel room = this.roomLoader.loadRoom(this.configurationItem);

		// Store the Room
		File tempFile = File.createTempFile("TestRoom.room.xml", null);
		FileSystemConfigurationItem tempConfigItem = new FileSystemConfigurationItem(
				tempFile, null);
		this.roomLoader.storeRoom(room, tempConfigItem);

		// Reload the Room
		SectionModel reloadedRoom = this.roomLoader.loadRoom(tempConfigItem);

		// Validate round trip
		assertGraph(room, reloadedRoom,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}
}
