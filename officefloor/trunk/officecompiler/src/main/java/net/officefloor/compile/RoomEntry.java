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
package net.officefloor.compile;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.room.RoomLoader;

/**
 * {@link net.officefloor.model.section.SectionModel} entry for the
 * {@link net.officefloor.frame.api.manage.Office}.
 * 
 * @author Daniel
 */
public class RoomEntry extends AbstractEntry<Object, SectionModel> {

	/**
	 * Loads the {@link RoomEntry} parented by the {@link OfficeEntry}.
	 * 
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the {@link SectionModel}.
	 * @param officeEntry
	 *            Parent {@link OfficeEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return Loaded {@link RoomEntry}.
	 * @throws Exception
	 *             If fails.
	 */
	public static RoomEntry loadRoom(ConfigurationItem configurationItem,
			OfficeEntry officeEntry, OfficeFloorCompilerContext context)
			throws Exception {
		return loadRoom("OFFICE ROOM", configurationItem, null, officeEntry,
				context);
	}

	/**
	 * Loads the {@link RoomEntry} parented by another {@link RoomEntry}.
	 * 
	 * @param roomName
	 *            Name of the {@link SectionModel}.
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the {@link SectionModel}.
	 * @param parentRoomEntry
	 *            Parent {@link RoomEntry}.
	 * @param parentOfficeEntry
	 *            Parent {@link OfficeEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return Loaded {@link RoomEntry}.
	 * @throws Exception
	 *             If fails.
	 */
	private static RoomEntry loadRoom(String roomName,
			ConfigurationItem configurationItem, RoomEntry parentRoomEntry,
			OfficeEntry parentOfficeEntry, OfficeFloorCompilerContext context)
			throws Exception {

		// Load the room
		SectionModel roomModel = new RoomLoader(context.getModelRepository())
				.loadRoom(configurationItem);

		// Create the room entry
		RoomEntry roomEntry;
		if (parentOfficeEntry != null) {
			roomEntry = new RoomEntry(configurationItem.getLocation(),
					roomModel, parentOfficeEntry);
		} else {
			roomEntry = new RoomEntry(configurationItem.getLocation(),
					roomName, roomModel, parentRoomEntry);
		}

		// Register the sub rooms / desks
		for (SubSectionModel subRoom : roomModel.getSubSections()) {
			// Check if sub room
			String subRoomName = subRoom.getSubSectionName();
			String subRoomId = subRoom.getSectionLocation();
			String deskId = subRoom.getSectionSourceClassName();
			if (subRoomId != null) {
				// Sub Room
				RoomEntry subRoomEntry = RoomEntry.loadRoom(subRoomName,
						context.getConfigurationContext().getConfigurationItem(
								subRoomId), roomEntry, null, context);
				roomEntry.roomMap.put(subRoom, subRoomEntry);

			} else if (deskId != null) {
				// Desk
				DeskEntry deskEntry = DeskEntry.loadDesk(deskId, subRoomName,
						context.getConfigurationContext().getConfigurationItem(
								deskId), roomEntry, context);
				roomEntry.deskMap.put(subRoom, deskEntry);

			} else {
				// Unknown sub room type
				throw new Exception(
						"Unknown sub room type (must be either sub room or desk)");
			}
		}

		// Return the room entry
		return roomEntry;
	}

	/**
	 * Name of the {@link SectionModel}.
	 */
	private final String roomName;

	/**
	 * Parent {@link RoomEntry}.
	 */
	private final RoomEntry parentRoom;

	/**
	 * {@link OfficeEntry} if this is the top level {@link RoomEntry}.
	 */
	private final OfficeEntry office;

	/**
	 * {@link SubSectionModel} room to {@link RoomEntry}.
	 */
	protected final ModelEntryMap<SubSectionModel, RoomEntry> roomMap = new ModelEntryMap<SubSectionModel, RoomEntry>();

	/**
	 * {@link SubSectionModel} desk to {@link DeskEntry}.
	 */
	protected final ModelEntryMap<SubSectionModel, DeskEntry> deskMap = new ModelEntryMap<SubSectionModel, DeskEntry>();

	/**
	 * Initiate as the room of the office.
	 * 
	 * @param roomId
	 *            Id of the {@link SectionModel}.
	 * @param room
	 *            {@link SectionModel}.
	 * @param office
	 *            {@link OfficeEntry}.
	 */
	public RoomEntry(String roomId, SectionModel room, OfficeEntry office) {
		super(roomId, null, room);
		this.roomName = "OFFICE ROOM";
		this.parentRoom = null;
		this.office = office;
	}

	/**
	 * Initiate as a sub room of another room.
	 * 
	 * @param roomId
	 *            Id of the {@link SectionModel}.
	 * @param roomName
	 *            Name of the {@link SectionModel}.
	 * @param room
	 *            {@link SectionModel}.
	 * @param parentRoom
	 *            Parent {@link RoomEntry}.
	 */
	public RoomEntry(String roomId, String roomName, SectionModel room,
			RoomEntry parentRoom) {
		super(roomId, null, room);
		this.roomName = roomName;
		this.parentRoom = parentRoom;
		this.office = null;
	}

	/**
	 * Obtains the name of the {@link SectionModel}.
	 * 
	 * @return Name of the {@link SectionModel}.
	 */
	public String getRoomName() {
		return this.roomName;
	}

	/**
	 * Obtains the parent {@link RoomEntry}.
	 * 
	 * @return Parent {@link RoomEntry}.
	 */
	public RoomEntry getParentRoom() {
		return this.parentRoom;
	}

	/**
	 * Obtains the {@link OfficeEntry}.
	 * 
	 * @return {@link OfficeEntry}.
	 */
	public OfficeEntry getOffice() {
		return this.office;
	}

	/**
	 * Obtains the {@link SubSectionModel} for the {@link RoomEntry}.
	 * 
	 * @param roomEntry
	 *            {@link RoomEntry}.
	 * @return {@link SubSectionModel}.
	 * @throws Exception
	 *             If no {@link SubSectionModel}.
	 */
	public SubSectionModel getSubRoom(RoomEntry roomEntry) throws Exception {
		return this.getModel(roomEntry, this.roomMap, "No sub room '"
				+ roomEntry.getRoomName() + "' on room " + this.getId());
	}

	/**
	 * Obtains the {@link SubSectionModel} for the {@link DeskEntry}.
	 * 
	 * @param deskEntry
	 *            {@link DeskEntry}.
	 * @return {@link SubSectionModel}.
	 * @throws Exception
	 *             If no {@link SubSectionModel}.
	 */
	public SubSectionModel getSubRoom(DeskEntry deskEntry) throws Exception {
		return this.getModel(deskEntry, this.deskMap, "No desk '"
				+ deskEntry.getDeskName() + "' on room " + this.getId());
	}

	/**
	 * Obtains the {@link SubSectionModel} by its name.
	 * 
	 * @param subRoomName
	 *            Name of the {@link SubSectionModel}.
	 * @return {@link SubSectionModel}.
	 * @throws Exception
	 *             If no {@link SubSectionModel}.
	 */
	public SubSectionModel getSubRoom(String subRoomName) throws Exception {
		// Obtains the sub room
		for (SubSectionModel subRoom : this.getModel().getSubSections()) {
			if (subRoomName.equals(subRoom.getSubSectionName())) {
				return subRoom;
			}
		}

		// If here not found
		throw new Exception("No sub room '" + subRoomName + "' on room "
				+ this.getId());
	}

	/**
	 * Obtains the {@link RoomEntry} for the {@link SubSectionModel} of this
	 * {@link RoomEntry}.
	 * 
	 * @param roomModel
	 *            {@link SubSectionModel}.
	 * @return {@link RoomEntry} for the {@link SubSectionModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public RoomEntry getRoomEntry(SubSectionModel roomModel) throws Exception {
		return this.getEntry(roomModel, this.roomMap, "No room '"
				+ roomModel.getSubSectionName() + "' on room " + this.getId());
	}

	/**
	 * Obtains the {@link DeskEntry} for the {@link SubSectionModel} of this
	 * {@link RoomEntry}.
	 * 
	 * @param deskModel
	 *            {@link SubSectionModel}.
	 * @return {@link DeskEntry} for the {@link SubSectionModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public DeskEntry getDeskEntry(SubSectionModel deskModel) throws Exception {
		return this.getEntry(deskModel, this.deskMap, "No desk '"
				+ deskModel.getSubSectionName() + "' on room " + this.getId());
	}

	/**
	 * Obtains the {@link SubSectionObjectModel} on the {@link SubSectionModel}
	 * .
	 * 
	 * @param subRoom
	 *            {@link SubSectionModel}.
	 * @param managedObjectName
	 *            Name of the {@link SubSectionObjectModel}.
	 * @return {@link SubSectionObjectModel}.
	 * @throws Exception
	 *             If no {@link SubSectionObjectModel}.
	 */
	public SubSectionObjectModel getSubRoomManagedObject(
			SubSectionModel subRoom, String managedObjectName) throws Exception {

		// Obtain the sub room managed object
		for (SubSectionObjectModel subRoomMo : subRoom.getSubSectionObjects()) {
			if (managedObjectName.equals(subRoomMo.getSubSectionObjectName())) {
				return subRoomMo;
			}
		}

		// Not found if here
		throw new Exception("Can not find managed object '" + managedObjectName
				+ "' for sub room '" + subRoom.getSubSectionName() + "'");
	}

	/**
	 * Obtains the {@link SubSectionOutputModel} on the {@link SubSectionModel}.
	 * 
	 * @param subRoom
	 *            {@link SubSectionModel}.
	 * @param outputFlowName
	 *            Output flow name on the sub room.
	 * @return {@link SubSectionOutputModel}.
	 * @throws Exception
	 *             If output flow does not exist.
	 */
	public SubSectionOutputModel getSubRoomOutputFlow(SubSectionModel subRoom,
			String outputFlowName) throws Exception {

		// Obtain the output flow
		for (SubSectionOutputModel outputFlow : subRoom.getSubSectionOutputs()) {
			if (outputFlowName.equals(outputFlow.getSubSectionOutputName())) {
				return outputFlow;
			}
		}

		// Not found if here
		throw new Exception("Unknown output flow '" + outputFlowName
				+ "' on sub room '" + subRoom.getSubSectionName()
				+ "' of room '" + this.getId() + "'");
	}

	/**
	 * Obtains the listing of {@link DeskEntry} instances for this
	 * {@link RoomEntry}.
	 * 
	 * @return Listing of {@link DeskEntry} instances for this {@link RoomEntry}
	 *         .
	 * @throws Exception
	 *             If a {@link RoomEntry} or {@link DeskEntry} can not be found.
	 */
	public DeskEntry[] getDeskEntries() throws Exception {
		List<DeskEntry> desks = new LinkedList<DeskEntry>();
		this.loadDeskEntries(desks);
		return desks.toArray(new DeskEntry[0]);
	}

	/**
	 * Loads the {@link DeskEntry} instances for this {@link RoomEntry}.
	 * 
	 * @param desks
	 *            Listing to append the {@link DeskEntry} instances.
	 * @throws Exception
	 *             If a {@link RoomEntry} or {@link DeskEntry} can not be found.
	 */
	private void loadDeskEntries(List<DeskEntry> desks) throws Exception {
		for (SubSectionModel subRoom : this.getModel().getSubSections()) {
			if (subRoom.getSectionLocation() != null) {
				// Recursively load rooms of the room
				RoomEntry roomEntry = this.roomMap.getEntry(subRoom);
				if (roomEntry == null) {
					throw new Exception("Unknown room '"
							+ subRoom.getSubSectionName() + "' ["
							+ subRoom.getSectionLocation() + "] on room "
							+ this.getId());
				}
				roomEntry.loadDeskEntries(desks);
			} else {
				// Load the desk entry for return
				DeskEntry deskEntry = this.deskMap.getEntry(subRoom);
				if (deskEntry == null) {
					throw new Exception("Unknown desk '"
							+ subRoom.getSubSectionName() + "' ["
							+ subRoom.getSectionSourceClassName() + "] on room "
							+ this.getId());
				}
				desks.add(deskEntry);
			}
		}
	}

}
