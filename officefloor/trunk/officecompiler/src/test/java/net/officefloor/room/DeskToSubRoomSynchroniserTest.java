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
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;

/**
 * Tests the {@link net.officefloor.room.DeskToSubRoomSynchroniser}.
 * 
 * @author Daniel
 */
public class DeskToSubRoomSynchroniserTest extends OfficeFrameTestCase {

	/**
	 * Ensures correctly synchronises the
	 * {@link net.officefloor.model.desk.DeskModel} onto the
	 * {@link net.officefloor.model.section.SubSectionModel}.
	 */
	public void testSynchroniseDeskOntoSubRoom() {

		// Create the DeskModel
		DeskModel desk = new DeskModel();
		desk.addExternalManagedObject(new ExternalManagedObjectModel("MO-ONE",
				"java.lang.String", null));
		desk.addExternalManagedObject(new ExternalManagedObjectModel("MO-TWO",
				"java.sql.Connection", null));
		desk.addTask(new TaskModel("IF-ONE", true, "work", "task",
				"java.lang.Object", null, null, null, null, null, null, null,
				null, null));
		desk.addTask(new TaskModel("IF-TWO", false, "work", "task",
				"java.lang.Object", null, null, null, null, null, null, null,
				null, null));
		desk.addTask(new TaskModel("IF-THREE", true, "work", "task",
				"java.lang.Object", null, null, null, null, null, null, null,
				null, null));
		desk.addExternalFlow(new ExternalFlowModel("OF-ONE", null, null, null,
				null));
		desk.addExternalFlow(new ExternalFlowModel("OF-TWO", null, null, null,
				null));
		// TODO correct test to handle
		desk.addExternalFlow(new ExternalFlowModel("ES-ONE", null, null, null,
				null)); // SQLException.class.getName()
		desk.addExternalFlow(new ExternalFlowModel("ES-TWO", null, null, null,
				null)); // IOException.class.getName()

		// Create the SubRoom
		SubSectionModel subRoom = new SubSectionModel();

		// Synchronise
		DeskToSubRoomSynchroniser.synchroniseDeskOntoSubRoom(desk, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getSubSectionObjects(), new SubSectionObjectModel("MO-ONE",
				String.class.getName(), null), new SubSectionObjectModel(
				"MO-TWO", Connection.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getSubSectionInputs(), new SubSectionInputModel("IF-ONE",
				Object.class.getName(), false, null), new SubSectionInputModel(
				"IF-THREE", Object.class.getName(), false, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getSubSectionOutputs(),
				new SubSectionOutputModel("OF-ONE", Object.class.getName()),
				new SubSectionOutputModel("OF-TWO", Object.class.getName()));

		// Remove one of each from desk
		desk.removeExternalManagedObject(desk.getExternalManagedObjects()
				.get(1));
		desk.removeTask(desk.getTasks().get(2));
		desk.removeExternalFlow(desk.getExternalFlows().get(1));

		// Synchronise again
		DeskToSubRoomSynchroniser.synchroniseDeskOntoSubRoom(desk, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getSubSectionObjects(), new SubSectionObjectModel("MO-ONE",
				String.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getSubSectionInputs(), new SubSectionInputModel("IF-ONE",
				Object.class.getName(), false, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getSubSectionOutputs(),
				new SubSectionOutputModel("OF-ONE", Object.class.getName(),
						null, null));

		// Add one of each to desk
		desk.addExternalManagedObject(new ExternalManagedObjectModel(
				"MO-THREE", "java.lang.Integer", null));
		desk.addTask(new TaskModel("IF-FOUR", false, "work", "task",
				"java.lang.String", null, null, null, null, null, null, null,
				null, null));
		desk.addTask(new TaskModel("IF-FIVE", true, "work", "task",
				"java.lang.String", null, null, null, null, null, null, null,
				null, null));
		desk.addExternalFlow(new ExternalFlowModel("OF-THREE", null, null,
				null, null));

		// Synchronise again
		DeskToSubRoomSynchroniser.synchroniseDeskOntoSubRoom(desk, subRoom);

		// Validate managed objects
		assertList(new String[] { "getName", "getObjectType" }, subRoom
				.getSubSectionObjects(), new SubSectionObjectModel("MO-ONE",
				String.class.getName(), null), new SubSectionObjectModel(
				"MO-THREE", Integer.class.getName(), null));

		// Validate input flow items
		assertList(new String[] { "getName", "getIsPublic" }, subRoom
				.getSubSectionInputs(), new SubSectionInputModel("IF-ONE",
				Object.class.getName(), false, null), new SubSectionInputModel(
				"IF-FIVE", Object.class.getName(), false, null));

		// Validate output flow items
		assertList(new String[] { "getName" }, subRoom.getSubSectionOutputs(),
				new SubSectionOutputModel("OF-ONE", Object.class.getName()),
				new SubSectionOutputModel("OF-THREE", Object.class.getName()));
	}
}
