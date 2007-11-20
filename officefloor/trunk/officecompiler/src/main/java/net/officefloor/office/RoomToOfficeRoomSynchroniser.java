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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeDeskModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomModel;

/**
 * Synchronises the {@link RoomModel} onto the {@link OfficeRoomModel}.
 * 
 * @author Daniel
 */
public class RoomToOfficeRoomSynchroniser {

	/**
	 * Synchronises the {@link RoomModel} onto the {@link OfficeRoomModel}.
	 * 
	 * @param roomId
	 *            Id of the {@link RoomModel}.
	 * @param room
	 *            {@link RoomModel}.
	 * @param officeRoom
	 *            {@link OfficeRoomModel}.
	 */
	public static void synchroniseRoomOntoOfficeRoom(String roomId,
			RoomModel room, OfficeRoomModel officeRoom) {

		// Specify the Id on the room
		officeRoom.setId(roomId);

		// Create the map of existing sub desks
		Map<String, OfficeDeskModel> existingDesks = new HashMap<String, OfficeDeskModel>();
		for (OfficeDeskModel existingDesk : officeRoom.getDesks()) {
			existingDesks.put(existingDesk.getId(), existingDesk);
		}

		// Create the map of existing sub rooms
		Map<String, OfficeRoomModel> existingRooms = new HashMap<String, OfficeRoomModel>();
		for (OfficeRoomModel existingRoom : officeRoom.getSubRooms()) {
			existingRooms.put(existingRoom.getId(), existingRoom);
		}

		// Synchronise the sub rooms
		for (SubRoomModel subRoom : room.getSubRooms()) {

			// Determine if a desk
			String subDeskId = subRoom.getDesk();
			if (subDeskId != null) {
				if (existingDesks.containsKey(subDeskId)) {
					// Remove from existing (not to remove later)
					existingDesks.remove(subDeskId);
				} else {
					// Add the desk as not existing
					OfficeDeskModel newDesk = new OfficeDeskModel(subDeskId,
							null);
					officeRoom.addDesk(newDesk);
				}
			} else {
				// Room
				String subRoomId = subRoom.getRoom();
				if (subRoomId != null) {
					if (existingRooms.containsKey(subRoomId)) {
						// Remove from existing (not to remove later)
						existingRooms.remove(subRoomId);
					} else {
						// Add the room as not existing
						OfficeRoomModel newRoom = new OfficeRoomModel(
								subRoomId, null, null);
						officeRoom.addSubRoom(newRoom);
					}
				}
			}
		}

		// Remove no longer existing desks
		for (OfficeDeskModel oldDesk : existingDesks.values()) {
			officeRoom.removeDesk(oldDesk);
		}

		// Remove no longer existing rooms
		for (OfficeRoomModel oldRoom : existingRooms.values()) {
			officeRoom.removeSubRoom(oldRoom);
		}
	}

	/**
	 * Synchronises the {@link RoomModel} onto the {@link OfficeModel}.
	 * 
	 * @param room
	 *            {@link RoomModel}.
	 * @param officeRoom
	 *            {@link OfficeRoomModel} for the input {@link RoomModel}.
	 * @param office
	 *            {@link OfficeModel}.
	 */
	public static void synchroniseRoomOntoOffice(RoomModel room,
			OfficeRoomModel officeRoom, OfficeModel office) {

		// Set the room on the office
		office.setRoom(officeRoom);

		// Create the map of existing managed objects
		Map<String, ExternalManagedObjectModel> existingMos = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel existingMo : office
				.getExternalManagedObjects()) {
			existingMos.put(existingMo.getName(), existingMo);
		}

		// Synchronise the managed objects
		for (net.officefloor.model.room.ExternalManagedObjectModel mo : room
				.getExternalManagedObjects()) {
			String moName = mo.getName();
			if (existingMos.containsKey(moName)) {
				// Remove managed object (so not removed later)
				existingMos.remove(moName);
			} else {
				// Add the new managed object
				ExternalManagedObjectModel newMo = new ExternalManagedObjectModel(
						moName, mo.getObjectType(), null);
				office.addExternalManagedObject(newMo);
			}
		}

		// Remove no longer existing managed objects
		for (ExternalManagedObjectModel oldMo : existingMos.values()) {
			office.removeExternalManagedObject(oldMo);
		}
	}

	/**
	 * Access via static methods.
	 */
	private RoomToOfficeRoomSynchroniser() {
	}
}
