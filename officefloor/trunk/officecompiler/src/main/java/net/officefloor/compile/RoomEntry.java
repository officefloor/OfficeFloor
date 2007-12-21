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

import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.repository.ConfigurationItem;
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
				RoomEntry.loadRoom(subRoomName, context
						.getConfigurationContext().getConfigurationItem(
								subRoomId), roomEntry, null, context);

			} else if (deskId != null) {
				// Desk
				DeskEntry.loadDesk(deskId, subRoomName,
						context.getConfigurationContext().getConfigurationItem(
								deskId), roomEntry, context);

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
	 * Obtains the {@link SubRoomModel} for the input room id.
	 * 
	 * @param roomId
	 *            Room Id.
	 * @return {@link SubRoomModel}.
	 * @throws Exception
	 *             If sub room does not exist.
	 */
	public SubRoomModel getSubRoom(String roomId) throws Exception {
		for (SubRoomModel subRoom : this.getModel().getSubRooms()) {
			if (roomId.equals(subRoom.getRoom())) {
				return subRoom;
			}
		}

		// Not found if here
		throw new Exception("Unknown sub room '" + roomId + "' on room '"
				+ this.getId() + "'");
	}

	/**
	 * Obtains the {@link SubRoomModel} for the input desk id.
	 * 
	 * @param deskId
	 *            Desk Id.
	 * @return {@link SubRoomModel}.
	 * @throws Exception
	 *             If desk does not exist.
	 */
	public SubRoomModel getDesk(String deskId) throws Exception {
		for (SubRoomModel subRoom : this.getModel().getSubRooms()) {
			if (deskId.equals(subRoom.getDesk())) {
				return subRoom;
			}
		}

		// Not found if here
		throw new Exception("Unknown desk '" + deskId + "' on room '"
				+ this.getId() + "'");
	}

}
