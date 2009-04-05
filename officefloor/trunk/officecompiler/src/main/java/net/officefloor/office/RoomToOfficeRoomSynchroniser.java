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

import net.officefloor.compile.WorkEntry;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeDeskModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * Synchronises the {@link SectionModel} onto the {@link OfficeRoomModel}.
 * 
 * @author Daniel
 */
public class RoomToOfficeRoomSynchroniser {

	/**
	 * Synchronises the {@link SectionModel} onto the {@link OfficeRoomModel}.
	 * 
	 * @param roomId
	 *            Id of the {@link SectionModel}.
	 * @param roomName
	 *            Name of the {@link SectionModel}.
	 * @param room
	 *            {@link SectionModel}.
	 * @param officeRoom
	 *            {@link OfficeRoomModel}.
	 */
	public static void synchroniseRoomOntoOfficeRoom(String roomId,
			String roomName, SectionModel room, OfficeRoomModel officeRoom) {

		// Specify the details of the room
		officeRoom.setId(roomId);
		officeRoom.setName(roomName);

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
		for (SubSectionModel subRoom : room.getSubSections()) {

			// Obtain the sub room name
			String subRoomName = subRoom.getSubSectionName();

			// Determine if a desk
			String subDeskId = subRoom.getDeskLocation();
			if (subDeskId != null) {
				if (existingDesks.containsKey(subDeskId)) {
					// Remove from existing (not to remove later)
					existingDesks.remove(subDeskId);
				} else {
					// Add the desk as not existing
					OfficeDeskModel newDesk = new OfficeDeskModel(subDeskId,
							subRoomName, null);
					officeRoom.addDesk(newDesk);
				}
			} else {
				// Room
				String subRoomId = subRoom.getSectionLocation();
				if (subRoomId != null) {
					if (existingRooms.containsKey(subRoomId)) {
						// Remove from existing (not to remove later)
						existingRooms.remove(subRoomId);
					} else {
						// Add the room as not existing
						OfficeRoomModel newRoom = new OfficeRoomModel(
								subRoomId, subRoomName, null, null);
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
	 * Synchronises the {@link SectionModel} onto the {@link OfficeModel}.
	 * 
	 * @param room
	 *            {@link SectionModel}.
	 * @param officeRoom
	 *            {@link OfficeRoomModel} for the input {@link SectionModel}.
	 * @param office
	 *            {@link OfficeModel}.
	 */
	public static void synchroniseRoomOntoOffice(SectionModel room,
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
		int x = officeRoom.getX() + 200;
		int y = officeRoom.getY();
		for (net.officefloor.model.section.ExternalManagedObjectModel mo : room
				.getExternalManagedObjects()) {
			String moName = mo.getExternalManagedObjectName();
			if (existingMos.containsKey(moName)) {
				// Remove managed object (so not removed later)
				existingMos.remove(moName);
			} else {
				// Add the new managed object (default to work scope)
				ExternalManagedObjectModel newMo = new ExternalManagedObjectModel(
						moName, mo.getObjectType(),
						WorkEntry.MANAGED_OBJECT_SCOPE_WORK, null, x, y);
				office.addExternalManagedObject(newMo);
				y += 30;
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
