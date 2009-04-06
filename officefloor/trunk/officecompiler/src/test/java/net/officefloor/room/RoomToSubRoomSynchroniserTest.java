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

import java.sql.Connection;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;

/**
 * Tests the {@link net.officefloor.room.RoomToSubRoomSynchroniser}.
 * 
 * @author Daniel
 */
public class RoomToSubRoomSynchroniserTest extends OfficeFrameTestCase {

	/**
	 * Ensures correctly synchronises the
	 * {@link net.officefloor.model.section.SectionModel} onto the
	 * {@link net.officefloor.model.section.SubSectionModel}.
	 */
	public void testSynchroniseRoomOntoSubRoom() {

		// Create the Room Model
		SectionModel room = new SectionModel();
		room.addExternalManagedObject(new ExternalManagedObjectModel("MO-ONE",
				"java.lang.String", null));
		room.addExternalManagedObject(new ExternalManagedObjectModel("MO-TWO",
				"java.sql.Connection", null));
		room.addExternalFlow(new ExternalFlowModel("OF-ONE", null));
		room.addExternalFlow(new ExternalFlowModel("OF-TWO", null));

		// Add sub rooms
		SubSectionModel subRoomOne = new SubSectionModel("SR-ONE", "desk",
				null, null, null, null, null);
		subRoomOne.addSubSectionInput(new SubSectionInputModel("IF-ONE",
				Object.class.getName(), true, null, null));
		room.addSubSection(subRoomOne);
		SubSectionModel subRoomTwo = new SubSectionModel("SR-TWO", "desk",
				null, null, null, null, null);
		subRoomTwo.addSubSectionInput(new SubSectionInputModel("IF-TWO",
				Object.class.getName(), false, null));
		room.addSubSection(subRoomTwo);
		SubSectionModel subRoomThree = new SubSectionModel("SR-THREE", "desk",
				null, null, null, null, null);
		subRoomThree.addSubSectionInput(new SubSectionInputModel("IF-THREE",
				Object.class.getName(), true, null));
		room.addSubSection(subRoomThree);

		// Create the SubRoom
		SubSectionModel subRoom = new SubSectionModel();

		// Synchronise
		RoomToSubRoomSynchroniser.synchroniseRoomOntoSubRoom(room, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getSubSectionObjects(), new SubSectionObjectModel("MO-ONE",
				String.class.getName(), null), new SubSectionObjectModel(
				"MO-TWO", Connection.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getSubSectionInputs(), new SubSectionInputModel(
				"SR-ONE-IF-ONE", Object.class.getName(), false, null),
				new SubSectionInputModel("SR-THREE-IF-THREE", Object.class
						.getName(), false, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getSubSectionOutputs(),
				new SubSectionOutputModel("OF-ONE", Object.class.getName(),
						false), new SubSectionOutputModel("OF-TWO",
						Object.class.getName(), false));

		// Remove one of each from room
		room.removeExternalManagedObject(room.getExternalManagedObjects()
				.get(1));
		room.removeExternalFlow(room.getExternalFlows().get(1));
		room.removeSubSection(room.getSubSections().get(2));

		// Synchronise
		RoomToSubRoomSynchroniser.synchroniseRoomOntoSubRoom(room, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getSubSectionObjects(), new SubSectionObjectModel("MO-ONE",
				String.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getSubSectionInputs(), new SubSectionInputModel(
				"SR-ONE-IF-ONE", Object.class.getName(), false, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getSubSectionOutputs(),
				new SubSectionOutputModel("OF-ONE", Object.class.getName(),
						false));

		// Add one of each to room
		room.addExternalManagedObject(new ExternalManagedObjectModel(
				"MO-THREE", "java.lang.String", null));
		room.addExternalFlow(new ExternalFlowModel("OF-THREE", null));
		SubSectionModel subRoomFour = new SubSectionModel("SR-FOUR", "desk",
				null, null, null, null, null);
		subRoomFour.addSubSectionInput(new SubSectionInputModel("IF-FOUR",
				Object.class.getName(), true, null));
		room.addSubSection(subRoomFour);

		// Synchronise
		RoomToSubRoomSynchroniser.synchroniseRoomOntoSubRoom(room, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getSubSectionObjects(), new SubSectionObjectModel("MO-ONE",
				String.class.getName(), null), new SubSectionObjectModel(
				"MO-THREE", String.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getSubSectionInputs(), new SubSectionInputModel(
				"SR-ONE-IF-ONE", Object.class.getName(), false, null),
				new SubSectionInputModel("SR-FOUR-IF-FOUR", Object.class
						.getName(), false, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getSubSectionOutputs(),
				new SubSectionOutputModel("OF-ONE", Object.class.getName(),
						false), new SubSectionOutputModel("OF-THREE",
						Object.class.getName(), false));
	}
}
