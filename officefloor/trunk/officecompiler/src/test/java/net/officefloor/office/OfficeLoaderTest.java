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
package net.officefloor.office;

import java.io.File;

import net.officefloor.compile.WorkEntry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToManagedObjectModel;
import net.officefloor.model.office.DutyFlowModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.FlowItemToPostAdministratorDutyModel;
import net.officefloor.model.office.FlowItemToPreAdministratorDutyModel;
import net.officefloor.model.office.OfficeDeskModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.office.PropertyModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.filesystem.FileSystemConfigurationContext;
import net.officefloor.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.room.RoomLoader;

/**
 * Tests the {@link net.officefloor.office.OfficeLoader}.
 * 
 * @author Daniel
 */
public class OfficeLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeLoader} to test.
	 */
	private OfficeLoader officeLoader;

	/**
	 * {@link ConfigurationItem}.
	 */
	private ConfigurationItem configurationItem;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Create the Office Loader
		this.officeLoader = new OfficeLoader();

		// Obtain the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "TestOffice.office.xml"), null);
	}

	/**
	 * Ensure loads the {@link net.officefloor.model.office.OfficeModel}.
	 */
	public void testLoadOffice() throws Exception {

		// Load the office
		OfficeModel office = this.officeLoader
				.loadOffice(this.configurationItem);

		// ----------------------------------------
		// Validate the Office
		// ----------------------------------------

		// Teams
		assertList(new String[] { "getName" }, office.getExternalTeams(),
				new ExternalTeamModel("TEAM ONE", null), new ExternalTeamModel(
						"TEAM TWO", null));

		// Managed Objects
		assertList(new String[] { "getName", "getObjectType", "getScope" },
				office.getExternalManagedObjects(),
				new ExternalManagedObjectModel("MO", "java.lang.String",
						WorkEntry.MANAGED_OBJECT_SCOPE_PROCESS, null));

		// Top level room
		assertEquals("Incorrect room id", "Room.room.xml", office.getRoom()
				.getId());
		assertEquals("Incorrect room name", "OFFICE ROOM", office.getRoom()
				.getName());

		// Second level Rooms
		assertList(new String[] { "getId", "getName" }, office.getRoom()
				.getSubRooms(), new OfficeRoomModel("SubRoomOne.room.xml",
				"RoomA", null, null), new OfficeRoomModel(
				"SubRoomTwo.room.xml", "RoomB", null, null));

		// Second level Desk
		assertList(new String[] { "getId", "getName" }, office.getRoom()
				.getDesks(),
				new OfficeDeskModel("Desk.desk.xml", "DeskC", null));
		OfficeDeskModel desk = office.getRoom().getDesks().get(0);
		assertList(new String[] { "getId", "getWorkName", "getTaskName" }, desk
				.getFlowItems(), new FlowItemModel("3", "WorkC",
				"FLOW ITEM ONE", null, null, null, null), new FlowItemModel(
				"4", "WorkD", "FLOW ITEM TWO", null, null, null, null));

		// Third level Room
		assertList(new String[] { "getId", "getName" }, office.getRoom()
				.getSubRooms().get(0).getSubRooms(), new OfficeRoomModel(
				"SubSubRoom.room.xml", "SubRoomA", null, null));

		// Third level Desk
		assertList(new String[] { "getId", "getName" }, office.getRoom()
				.getSubRooms().get(0).getDesks(), new OfficeDeskModel(
				"SubDesk.desk.xml", "SubDeskA", null));
		OfficeDeskModel subDesk = office.getRoom().getSubRooms().get(0)
				.getDesks().get(0);
		assertList(new String[] { "getId", "getWorkName", "getTaskName" },
				subDesk.getFlowItems(), new FlowItemModel("1", "WorkA", "FI1",
						null, null, null, null), new FlowItemModel("2",
						"WorkB", "FI2", null, null, null, null));

		// Flow items of third level desk
		FlowItemModel flowItemOne = subDesk.getFlowItems().get(0);
		assertEquals("Incorrect team name for first flow item", "TEAM ONE",
				flowItemOne.getTeam().getTeamName());
		assertList(new String[] { "getAdministratorId", "getDutyKey" },
				flowItemOne.getPreAdminDutys(),
				new FlowItemToPreAdministratorDutyModel("1", "KeyOne", null,
						null), new FlowItemToPreAdministratorDutyModel("1",
						"KeyTwo", null, null));
		assertEquals("Incorrect post admin for first flow item", 0, flowItemOne
				.getPostAdminDutys().size());
		FlowItemModel flowItemTwo = subDesk.getFlowItems().get(1);
		assertEquals("Incorrect team name for second flow item", "TEAM TWO",
				flowItemTwo.getTeam().getTeamName());
		assertEquals("Incorrect pre admin for second flow item", 0, flowItemTwo
				.getPreAdminDutys().size());
		assertList(new String[] { "getAdministratorId", "getDutyKey" },
				flowItemTwo.getPostAdminDutys(),
				new FlowItemToPostAdministratorDutyModel("1", "KeyOne", null,
						null), new FlowItemToPostAdministratorDutyModel("1",
						"KeyTwo", null, null));

		// Administrator
		assertList(new String[] { "getId", "getSource", "getX", "getY" },
				office.getAdministrators(), new AdministratorModel("1",
						"net.officefloor.admin.TestAdminSource", null, null,
						null, 100, 200));
		AdministratorModel administrator = office.getAdministrators().get(0);
		assertList(new String[] { "getName", "getValue" }, administrator
				.getProperties(), new PropertyModel("prop name", "prop value"));
		assertList(new String[] { "getName" }, administrator
				.getManagedObjects(), new AdministratorToManagedObjectModel(
				"MO", null, null));
		assertList(new String[] { "getKey", "getFlowKeys" }, administrator
				.getDuties(), new DutyModel("KeyOne",
				"net.officefloor.admin.TestDutyOneFlowKeys", null, null, null),
				new DutyModel("KeyTwo", null, null, null, null));
		DutyModel duty = administrator.getDuties().get(0);
		assertList(new String[] { "getKey" }, duty.getFlows(),
				new DutyFlowModel("FlowOne", null), new DutyFlowModel(
						"FlowTwo", null));
		DutyFlowModel dutyFlow = duty.getFlows().get(0);
		assertEquals("Incorrect duty flow item id", "1", dutyFlow.getFlowItem()
				.getFlowItemId());
	}

	/**
	 * Ensures round trip in loading and storing.
	 */
	public void testLoadAndStore() throws Exception {

		// Load the Office
		OfficeModel office = this.officeLoader
				.loadOffice(this.configurationItem);

		// Store the Desk
		File file = File.createTempFile("TestOffice.office.xml", null);
		FileSystemConfigurationItem tempFile = new FileSystemConfigurationItem(
				file, null);
		this.officeLoader.storeOffice(office, tempFile);

		// Reload the Office
		OfficeModel reloadedOffice = this.officeLoader.loadOffice(tempFile);

		// Validate round trip
		assertGraph(office, reloadedOffice, "getAdministrators",
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

	/**
	 * Ensures recursively loads the room, its sub-rooms and desks.
	 */
	public void testLoadOfficeRoom() throws Exception {

		final String PARENT_ROOM_FILE_NAME = "TestRoom.room.xml";

		// Obtain the parent room file
		File parentRoomFile = this.findFile(this.getClass(),
				PARENT_ROOM_FILE_NAME);

		// Obtain the configuration of the room
		ConfigurationContext context = new FileSystemConfigurationContext(
				parentRoomFile.getParentFile());
		ConfigurationItem roomConfigItem = context
				.getConfigurationItem(PARENT_ROOM_FILE_NAME);

		// Load the raw room
		RoomLoader roomLoader = new RoomLoader();
		RoomModel rawRoom = roomLoader.loadRoom(roomConfigItem);

		// Load the office room
		OfficeRoomModel actualRoom = this.officeLoader.loadOfficeRoom(
				roomConfigItem.getId(), "OFFICE ROOM", rawRoom, context, this
						.getClass().getClassLoader());

		// Create the expected office room
		FlowItemModel[] flowItems = new FlowItemModel[] {
				new FlowItemModel("1", "workA", "taskMethod", null, null, null,
						null),
				new FlowItemModel("2", "workB", "noLongerExists", null, null,
						null, null) };
		OfficeDeskModel[] desks = new OfficeDeskModel[] { new OfficeDeskModel(
				"TestDesk.desk.xml", "1", flowItems) };
		OfficeRoomModel expectedRoom = new OfficeRoomModel(
				PARENT_ROOM_FILE_NAME, "OFFICE ROOM",
				new OfficeRoomModel[] { new OfficeRoomModel(
						"TestSubRoom.room.xml", "2", null, desks) }, desks);

		// Validate the room
		assertGraph(expectedRoom, actualRoom);
	}
}
