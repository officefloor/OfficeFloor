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

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.room.OutputFlowToExternalFlowModel;
import net.officefloor.model.room.OutputFlowToInputFlowModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;

/**
 * Utility class to obtain the {@link Flow} lines.
 * 
 * @author Daniel
 */
public class FlowLineUtil {

	/**
	 * Obtains the {@link LinkedFlow} for the {@link TaskModel}.
	 * 
	 * @param flowItem
	 *            {@link TaskModel}.
	 * @param deskEntry
	 *            {@link DeskEntry} containing the {@link TaskModel}.
	 * @return {@link LinkedFlow}.
	 * @throws Exception
	 *             If fails to obtain the {@link LinkedFlow}.
	 */
	public static LinkedFlow getLinkedFlow(TaskModel flowItem,
			DeskEntry deskEntry) throws Exception {

		// Obtain the target work entry
		WorkModel workModel = deskEntry
				.getWorkModel(flowItem.getWorkName());
		WorkEntry<?> workEntry = deskEntry.getWorkEntry(workModel);

		// Obtain the target task entry
		TaskEntry<?> taskEntry = workEntry.getTaskEntry(flowItem);

		// Return the linked flow
		return new LinkedFlow(flowItem, taskEntry, workEntry, deskEntry);
	}

	/**
	 * Obtains the {@link LinkedFlow} for the {@link ExternalFlowModel}.
	 * 
	 * @param externalFlow
	 *            {@link ExternalFlowModel}.
	 * @param deskEntry
	 *            {@link DeskEntry} containing the {@link ExternalFlowModel}.
	 * @return {@link LinkedFlow}.
	 * @throws Exception
	 *             If fails to obtain the {@link LinkedFlow}.
	 */
	public static LinkedFlow getLinkedFlow(ExternalFlowModel externalFlow,
			DeskEntry deskEntry) throws Exception {

		// Obtain room containing the desk
		RoomEntry roomEntry = deskEntry.getParentRoom();

		// Obtain the desk sub room
		SubRoomModel subRoom = roomEntry.getSubRoom(deskEntry);

		// Obtain the starting external flow name
		String externalFlowName = externalFlow.getExternalFlowName();

		// Loop until reached room which starts linking down
		SubRoomOutputFlowModel outputFlow = null;
		while (externalFlowName != null) {

			// Obtain the output flow within the room
			outputFlow = roomEntry.getSubRoomOutputFlow(subRoom,
					externalFlowName);

			// Follow flow
			OutputFlowToExternalFlowModel extConn = outputFlow
					.getExternalFlow();
			if (extConn != null) {
				// External flow (set details to find)
				externalFlowName = extConn.getExternalFlow().getName();

				// Obtain parent room to follow external flow
				subRoom = roomEntry.getParentRoom().getSubRoom(roomEntry);
				roomEntry = roomEntry.getParentRoom();

			} else {
				// No longer going to external flow
				externalFlowName = null;
			}
		}

		// Linking to another room
		OutputFlowToInputFlowModel inConn = outputFlow.getInput();
		SubRoomInputFlowModel inputFlow = inConn.getInput();

		// Return the linked flow
		return getLinkedFlow(roomEntry, inputFlow);
	}

	/**
	 * Searches down through rooms to desk to find the target
	 * {@link TaskModel}.
	 * 
	 * @param roomEntry
	 *            {@link RoomEntry}.
	 * @param subRoomName
	 *            Name of the {@link SubRoomModel}.
	 * @param inputFlowName
	 *            Name of the input flow on the {@link SubRoomModel}.
	 * @return {@link LinkedFlow}.
	 * @throws Exception
	 *             If fails to link.
	 */
	private static LinkedFlow getLinkedFlow(RoomEntry roomEntry,
			SubRoomInputFlowModel subRoomInputFlow) throws Exception {

		// Obtain the sub room for the input flow
		SubRoomModel subRoom = null;
		FOUND_SUB_ROOM: for (SubRoomModel r : roomEntry.getModel()
				.getSubRooms()) {
			for (SubRoomInputFlowModel i : r.getInputFlows()) {
				if (subRoomInputFlow == i) {
					subRoom = r;
					break FOUND_SUB_ROOM;
				}
			}
		}

		// Obtain the sub room name
		String subRoomName = subRoom.getId();

		// Obtain the input flow name
		String inputFlowName = subRoomInputFlow.getName();

		// Find the desk
		DeskEntry deskEntry = null;
		while (deskEntry == null) {

			// Obtain the sub room
			subRoom = roomEntry.getSubRoom(subRoomName);

			// Obtain sub entry
			String entryId = subRoom.getRoom();
			if (entryId != null) {
				// Entry is a room

				// Obtain the sub room entry
				roomEntry = roomEntry.getRoomEntry(subRoom);

				// TODO reduce coupling of room hierarchy.
				// Decode the sub room and input flow
				if (!inputFlowName.contains("-")) {
					throw new TODOException("Invalid input flow name '"
							+ inputFlowName + "' as must contain '-'");
				}
				subRoomName = inputFlowName.split("-")[0];
				inputFlowName = inputFlowName.substring(subRoomName.length()
						+ "-".length());

				// Obtain the sub room
				subRoom = roomEntry.getSubRoom(subRoomName);

				// Obtain the input flow
				SubRoomInputFlowModel inputFlow = null;
				for (SubRoomInputFlowModel iF : subRoom.getInputFlows()) {
					if (inputFlowName.equals(iF.getName())) {
						inputFlow = iF;
					}
				}
				if (inputFlow == null) {
					throw new TODOException("Can not find input flow '"
							+ inputFlowName + "' on sub room '" + subRoomName
							+ "' of room " + roomEntry.getId());
				}

				// Obtain the input flow of the sub room
				inputFlowName = inputFlow.getName();

			} else {
				// Entry is a desk
				deskEntry = roomEntry.getDeskEntry(subRoom);
				if (deskEntry == null) {
					throw new Exception("No desk '" + subRoom.getId()
							+ "' on room " + roomEntry.getId());
				}
			}
		}

		// Obtain the target flow item on the desk
		TaskModel flowItem = null;
		FOUND_FLOW_ITEM: for (TaskModel fi : deskEntry.getModel()
				.getTasks()) {
			if (inputFlowName.equals(fi.getTaskName())) {
				flowItem = fi;
				break FOUND_FLOW_ITEM;
			}
		}
		if (flowItem == null) {
			throw new TODOException("Can not find flow item '" + inputFlowName
					+ "' on desk " + deskEntry.getId());
		}

		// Return the target flow
		return getLinkedFlow(flowItem, deskEntry);
	}

	/**
	 * Details of the target {@link Flow}.
	 */
	public static class LinkedFlow {

		/**
		 * Target {@link TaskModel}.
		 */
		public final TaskModel flowItem;

		/**
		 * Target {@link TaskEntry}.
		 */
		public final TaskEntry<?> taskEntry;

		/**
		 * Target {@link WorkEntry}.
		 */
		public final WorkEntry<?> workEntry;

		/**
		 * Target {@link DeskEntry}.
		 */
		public final DeskEntry deskEntry;

		/**
		 * Initiate with target details.
		 * 
		 * @param flowItem
		 *            Target {@link TaskModel}.
		 * @param taskEntry
		 *            Target {@link TaskEntry}.
		 * @param workEntry
		 *            Target {@link WorkEntry}.
		 * @param deskEntry
		 *            Target {@link DeskEntry}.
		 */
		public LinkedFlow(TaskModel flowItem, TaskEntry<?> taskEntry,
				WorkEntry<?> workEntry, DeskEntry deskEntry) {
			this.flowItem = flowItem;
			this.taskEntry = taskEntry;
			this.workEntry = workEntry;
			this.deskEntry = deskEntry;
		}
	}

}
