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
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.filesystem.FileSystemConfigurationItem;

/**
 * Tests loading a {@link net.officefloor.model.room.SubRoomModel}.
 * 
 * @author Daniel
 */
public class SubRoomLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to load a {@link net.officefloor.model.desk.DeskModel}
	 * {@link SubRoomModel}.
	 */
	public void testLoadDeskSubRoom() throws Exception {

		// Create the Room Loader
		RoomLoader roomLoader = new RoomLoader();

		// Obtain the configuration to the desk
		ConfigurationItem configItem = new FileSystemConfigurationItem(this
				.findFile(this.getClass(), "TestSubRoom.desk.xml"), null);

		// Load the Desk as a Sub Room
		SubRoomModel subRoom = roomLoader.loadSubRoom(configItem);

		// Validate the Sub Room
		assertNull("Room should not be specified", subRoom.getRoom());
		assertNotNull("Desk must be specified", subRoom.getDesk());

		// Validate synchronised
		assertTrue("Must have managed objects", subRoom.getManagedObjects()
				.size() > 0);
	}

	/**
	 * Ensure able to load a {@link net.officefloor.model.room.RoomModel}
	 * {@link SubRoomModel}.
	 */
	public void testLoadRoomSubRoom() throws Exception {

		// Create the Room Loader
		RoomLoader roomLoader = new RoomLoader();

		// Obtain the configuration to the room
		ConfigurationItem configItem = new FileSystemConfigurationItem(this
				.findFile(this.getClass(), "TestRoom.room.xml"), null);

		// Load the Room as a Sub Room
		SubRoomModel subRoom = roomLoader.loadSubRoom(configItem);

		// Validate the Sub Room
		assertNotNull("Room must be specified", subRoom.getRoom());
		assertNull("Desk should not be specified", subRoom.getDesk());

		// Validate synchronised
		assertTrue("Must have managed objects", subRoom.getManagedObjects()
				.size() > 0);
	}

}
