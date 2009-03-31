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
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomEscalationModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;
import net.officefloor.room.RoomLoader;

/**
 * {@link net.officefloor.model.room.RoomModel} entry for the
 * {@link net.officefloor.frame.api.manage.Office}.
 * 
 * @author Daniel
 */
public class RoomEntry extends AbstractEntry<Object, RoomModel> {

	/**
	 * Loads the {@link RoomEntry} parented by the {@link OfficeEntry}.
	 * 
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the {@link RoomModel}.
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
	 *            Name of the {@link RoomModel}.
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the {@link RoomModel}.
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
		RoomModel roomModel = new RoomLoader(context.getModelRepository())
				.loadRoom(configurationItem);

		// Create the room entry
		RoomEntry roomEntry;
		if (parentOfficeEntry != null) {
			roomEntry = new RoomEntry(configurationItem.getId(), roomModel,
					parentOfficeEntry);
		} else {
			roomEntry = new RoomEntry(configurationItem.getId(), roomName,
					roomModel, parentRoomEntry);
		}

		// Register the sub rooms / desks
		for (SubRoomModel subRoom : roomModel.getSubRooms()) {
			// Check if sub room
			String subRoomName = subRoom.getId();
			String subRoomId = subRoom.getRoom();
			String deskId = subRoom.getDesk();
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
	 * Name of the {@link RoomModel}.
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
	 * {@link SubRoomModel} room to {@link RoomEntry}.
	 */
	protected final ModelEntryMap<SubRoomModel, RoomEntry> roomMap = new ModelEntryMap<SubRoomModel, RoomEntry>();

	/**
	 * {@link SubRoomModel} desk to {@link DeskEntry}.
	 */
	protected final ModelEntryMap<SubRoomModel, DeskEntry> deskMap = new ModelEntryMap<SubRoomModel, DeskEntry>();

	/**
	 * Initiate as the room of the office.
	 * 
	 * @param roomId
	 *            Id of the {@link RoomModel}.
	 * @param room
	 *            {@link RoomModel}.
	 * @param office
	 *            {@link OfficeEntry}.
	 */
	public RoomEntry(String roomId, RoomModel room, OfficeEntry office) {
		super(roomId, null, room);
		this.roomName = "OFFICE ROOM";
		this.parentRoom = null;
		this.office = office;
	}

	/**
	 * Initiate as a sub room of another room.
	 * 
	 * @param roomId
	 *            Id of the {@link RoomModel}.
	 * @param roomName
	 *            Name of the {@link RoomModel}.
	 * @param room
	 *            {@link RoomModel}.
	 * @param parentRoom
	 *            Parent {@link RoomEntry}.
	 */
	public RoomEntry(String roomId, String roomName, RoomModel room,
			RoomEntry parentRoom) {
		super(roomId, null, room);
		this.roomName = roomName;
		this.parentRoom = parentRoom;
		this.office = null;
	}

	/**
	 * Obtains the name of the {@link RoomModel}.
	 * 
	 * @return Name of the {@link RoomModel}.
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
	 * Obtains the {@link SubRoomModel} for the {@link RoomEntry}.
	 * 
	 * @param roomEntry
	 *            {@link RoomEntry}.
	 * @return {@link SubRoomModel}.
	 * @throws Exception
	 *             If no {@link SubRoomModel}.
	 */
	public SubRoomModel getSubRoom(RoomEntry roomEntry) throws Exception {
		return this.getModel(roomEntry, this.roomMap, "No sub room '"
				+ roomEntry.getRoomName() + "' on room " + this.getId());
	}

	/**
	 * Obtains the {@link SubRoomModel} for the {@link DeskEntry}.
	 * 
	 * @param deskEntry
	 *            {@link DeskEntry}.
	 * @return {@link SubRoomModel}.
	 * @throws Exception
	 *             If no {@link SubRoomModel}.
	 */
	public SubRoomModel getSubRoom(DeskEntry deskEntry) throws Exception {
		return this.getModel(deskEntry, this.deskMap, "No desk '"
				+ deskEntry.getDeskName() + "' on room " + this.getId());
	}

	/**
	 * Obtains the {@link SubRoomModel} by its name.
	 * 
	 * @param subRoomName
	 *            Name of the {@link SubRoomModel}.
	 * @return {@link SubRoomModel}.
	 * @throws Exception
	 *             If no {@link SubRoomModel}.
	 */
	public SubRoomModel getSubRoom(String subRoomName) throws Exception {
		// Obtains the sub room
		for (SubRoomModel subRoom : this.getModel().getSubRooms()) {
			if (subRoomName.equals(subRoom.getId())) {
				return subRoom;
			}
		}

		// If here not found
		throw new Exception("No sub room '" + subRoomName + "' on room "
				+ this.getId());
	}

	/**
	 * Obtains the {@link RoomEntry} for the {@link SubRoomModel} of this
	 * {@link RoomEntry}.
	 * 
	 * @param roomModel
	 *            {@link SubRoomModel}.
	 * @return {@link RoomEntry} for the {@link SubRoomModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public RoomEntry getRoomEntry(SubRoomModel roomModel) throws Exception {
		return this.getEntry(roomModel, this.roomMap, "No room '"
				+ roomModel.getId() + "' on room " + this.getId());
	}

	/**
	 * Obtains the {@link DeskEntry} for the {@link SubRoomModel} of this
	 * {@link RoomEntry}.
	 * 
	 * @param deskModel
	 *            {@link SubRoomModel}.
	 * @return {@link DeskEntry} for the {@link SubRoomModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public DeskEntry getDeskEntry(SubRoomModel deskModel) throws Exception {
		return this.getEntry(deskModel, this.deskMap, "No desk '"
				+ deskModel.getId() + "' on room " + this.getId());
	}

	/**
	 * Obtains the {@link SubRoomManagedObjectModel} on the {@link SubRoomModel}
	 * .
	 * 
	 * @param subRoom
	 *            {@link SubRoomModel}.
	 * @param managedObjectName
	 *            Name of the {@link SubRoomManagedObjectModel}.
	 * @return {@link SubRoomManagedObjectModel}.
	 * @throws Exception
	 *             If no {@link SubRoomManagedObjectModel}.
	 */
	public SubRoomManagedObjectModel getSubRoomManagedObject(
			SubRoomModel subRoom, String managedObjectName) throws Exception {

		// Obtain the sub room managed object
		for (SubRoomManagedObjectModel subRoomMo : subRoom.getManagedObjects()) {
			if (managedObjectName.equals(subRoomMo.getName())) {
				return subRoomMo;
			}
		}

		// Not found if here
		throw new Exception("Can not find managed object '" + managedObjectName
				+ "' for sub room '" + subRoom.getId() + "'");
	}

	/**
	 * Obtains the {@link SubRoomOutputFlowModel} on the {@link SubRoomModel}.
	 * 
	 * @param subRoom
	 *            {@link SubRoomModel}.
	 * @param outputFlowName
	 *            Output flow name on the sub room.
	 * @return {@link SubRoomOutputFlowModel}.
	 * @throws Exception
	 *             If output flow does not exist.
	 */
	public SubRoomOutputFlowModel getSubRoomOutputFlow(SubRoomModel subRoom,
			String outputFlowName) throws Exception {

		// Obtain the output flow
		for (SubRoomOutputFlowModel outputFlow : subRoom.getOutputFlows()) {
			if (outputFlowName.equals(outputFlow.getName())) {
				return outputFlow;
			}
		}

		// Not found if here
		throw new Exception("Unknown output flow '" + outputFlowName
				+ "' on sub room '" + subRoom.getId() + "' of room '"
				+ this.getId() + "'");
	}

	/**
	 * Obtains the {@link SubRoomEscalationModel} on the {@link SubRoomModel}.
	 * 
	 * @param subRoom
	 *            {@link SubRoomModel}.
	 * @param escalationName
	 *            Escalation name on the sub room.
	 * @return {@link SubRoomEscalationModel}.
	 * @throws Exception
	 *             If escalation does not exist.
	 */
	public SubRoomEscalationModel getSubRoomEscalation(SubRoomModel subRoom,
			String escalationName) throws Exception {

		// Obtain the escalation
		for (SubRoomEscalationModel escalation : subRoom.getEscalations()) {
			if (escalationName.equals(escalation.getName())) {
				return escalation;
			}
		}

		// Not found if here
		throw new Exception("Unknown escalation '" + escalationName
				+ "' on sub room '" + subRoom.getId() + "' of room '"
				+ this.getId() + "'");
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
		for (SubRoomModel subRoom : this.getModel().getSubRooms()) {
			if (subRoom.getRoom() != null) {
				// Recursively load rooms of the room
				RoomEntry roomEntry = this.roomMap.getEntry(subRoom);
				if (roomEntry == null) {
					throw new Exception("Unknown room '" + subRoom.getId()
							+ "' [" + subRoom.getRoom() + "] on room "
							+ this.getId());
				}
				roomEntry.loadDeskEntries(desks);
			} else {
				// Load the desk entry for return
				DeskEntry deskEntry = this.deskMap.getEntry(subRoom);
				if (deskEntry == null) {
					throw new Exception("Unknown desk '" + subRoom.getId()
							+ "' [" + subRoom.getDesk() + "] on room "
							+ this.getId());
				}
				desks.add(deskEntry);
			}
		}
	}

}
