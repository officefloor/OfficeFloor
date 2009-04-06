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
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.section.SubSectionModel;

/**
 * Tests loading a {@link net.officefloor.model.section.SubSectionModel}.
 * 
 * @author Daniel
 */
public class SubRoomLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to load a {@link net.officefloor.model.desk.DeskModel}
	 * {@link SubSectionModel}.
	 */
	public void testLoadDeskSubRoom() throws Exception {

		// Create the Room Loader
		RoomLoader roomLoader = new RoomLoader();

		// Obtain the configuration to the desk
		ConfigurationItem configItem = new FileSystemConfigurationItem(this
				.findFile(this.getClass(), "TestSubRoom.desk.xml"), null);

		// Load the Desk as a Sub Room
		SubSectionModel subRoom = roomLoader.loadSubRoom(configItem);

		// Validate the Sub Room
		assertNull("Room should not be specified", subRoom.getSectionLocation());
		assertNotNull("Desk must be specified", subRoom.getSectionSourceClassName());

		// Validate synchronised
		assertTrue("Must have managed objects", subRoom.getSubSectionObjects()
				.size() > 0);
	}

	/**
	 * Ensure able to load a {@link net.officefloor.model.section.SectionModel}
	 * {@link SubSectionModel}.
	 */
	public void testLoadRoomSubRoom() throws Exception {

		// Create the Room Loader
		RoomLoader roomLoader = new RoomLoader();

		// Obtain the configuration to the room
		ConfigurationItem configItem = new FileSystemConfigurationItem(this
				.findFile(this.getClass(), "TestRoom.room.xml"), null);

		// Load the Room as a Sub Room
		SubSectionModel subRoom = roomLoader.loadSubRoom(configItem);

		// Validate the Sub Room
		assertNotNull("Room must be specified", subRoom.getSectionLocation());
		assertNull("Desk should not be specified", subRoom.getSectionSourceClassName());

		// Validate synchronised
		assertTrue("Must have managed objects", subRoom.getSubSectionObjects()
				.size() > 0);
	}

}
