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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;

/**
 * Synchronises the {@link net.officefloor.model.room.RoomModel} to the
 * {@link net.officefloor.model.room.SubRoomModel}.
 * 
 * @author Daniel
 */
public class RoomToSubRoomSynchroniser {

	/**
	 * Synchronises {@link RoomModel} onto the {@link SubRoomModel}.
	 * 
	 * @param room
	 *            {@link RoomModel}.
	 * @param subRoom
	 *            {@link SubRoomModel}.
	 */
	public static void synchroniseRoomOntoSubRoom(RoomModel room,
			SubRoomModel subRoom) {

		// Create the map of existing managed objects to their names
		Map<String, SubRoomManagedObjectModel> existingManagedObjects = new HashMap<String, SubRoomManagedObjectModel>();
		for (SubRoomManagedObjectModel mo : subRoom.getManagedObjects()) {
			existingManagedObjects.put(mo.getName(), mo);
		}

		// Add managed objects as per desk
		for (ExternalManagedObjectModel roomMo : room
				.getExternalManagedObjects()) {

			// Obtain the managed object
			SubRoomManagedObjectModel mo = existingManagedObjects.get(roomMo
					.getName());
			if (mo == null) {
				// Not exist therefore create and add
				mo = new SubRoomManagedObjectModel(roomMo.getName(), roomMo
						.getObjectType(), null);
				subRoom.addManagedObject(mo);
			}

			// Remove from existing list
			existingManagedObjects.remove(roomMo.getName());
		}

		// Remove no longer existing managed objects
		for (SubRoomManagedObjectModel mo : existingManagedObjects.values()) {
			subRoom.removeManagedObject(mo);
		}

		// Create the map of existing input flows to their names
		Map<String, SubRoomInputFlowModel> existingInputFlows = new HashMap<String, SubRoomInputFlowModel>();
		for (SubRoomInputFlowModel inFlow : subRoom.getInputFlows()) {
			existingInputFlows.put(inFlow.getName(), inFlow);
		}

		// Add input flows as per desk
		for (SubRoomModel roomSubRoom : room.getSubRooms()) {
			for (SubRoomInputFlowModel flow : roomSubRoom.getInputFlows()) {

				// Do not add non-public flow items
				if (!flow.getIsPublic()) {
					continue;
				}

				// Obtain the name for the input flow
				String inputFlowName = roomSubRoom.getId() + "-"
						+ flow.getName();

				// Obtain the input flow
				SubRoomInputFlowModel inFlow = existingInputFlows
						.get(inputFlowName);
				if (inFlow == null) {
					// Not exist therefore create and add
					inFlow = new SubRoomInputFlowModel(inputFlowName, flow
							.getIsPublic(), null);
					subRoom.addInputFlow(inFlow);
				}

				// Remove from existing list
				existingInputFlows.remove(inputFlowName);
			}
		}

		// Remove no longer existing input flows
		for (SubRoomInputFlowModel inFlow : existingInputFlows.values()) {
			subRoom.removeInputFlow(inFlow);
		}

		// Create the map of existing output flows to their names
		Map<String, SubRoomOutputFlowModel> existingOutputFlows = new HashMap<String, SubRoomOutputFlowModel>();
		for (SubRoomOutputFlowModel outFlow : subRoom.getOutputFlows()) {
			existingOutputFlows.put(outFlow.getName(), outFlow);
		}

		// Add output flows as per desk
		for (ExternalFlowModel flow : room.getExternalFlows()) {

			// Obtain the output flow
			SubRoomOutputFlowModel outFlow = existingOutputFlows.get(flow
					.getName());
			if (outFlow == null) {
				// Not exist therefore create and add
				outFlow = new SubRoomOutputFlowModel(flow.getName(), null, null);
				subRoom.addOutputFlow(outFlow);
			}
		}

		// Remove no longer existing output flows
		for (SubRoomOutputFlowModel outFlow : existingOutputFlows.values()) {
			subRoom.removeOutputFlow(outFlow);
		}
	}

	/**
	 * Access via static methods.
	 */
	private RoomToSubRoomSynchroniser() {
	}

}
