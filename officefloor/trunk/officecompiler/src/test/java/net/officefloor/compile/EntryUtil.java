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

import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomEscalationModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomModel;

/**
 * Utility class for entries.
 * 
 * @author Daniel
 */
public class EntryUtil {

	/**
	 * Creates a {@link RoomEntry}.
	 * 
	 * @param roomName
	 *            Name of the {@link RoomModel}.
	 * @param parentRoomEntry
	 *            Parent {@link RoomEntry}.
	 * @return {@link RoomEntry}.
	 */
	public static RoomEntry createRoomEntry(String roomName,
			RoomEntry parentRoomEntry) {

		// Obtain the room id
		String roomId = "ROOM_ID__" + roomName;

		// Create the room entry
		RoomModel room = new RoomModel();
		RoomEntry roomEntry = new RoomEntry(roomId, roomName, room,
				parentRoomEntry);

		// Link into parent room (if provided)
		if (parentRoomEntry != null) {

			// Create the sub room for the room
			SubRoomModel subRoom = new SubRoomModel();
			subRoom.setId(roomName);
			subRoom.setRoom(roomId);

			// Link into parent room
			parentRoomEntry.getModel().addSubRoom(subRoom);
			parentRoomEntry.roomMap.put(subRoom, roomEntry);
		}

		// Return the room entry
		return roomEntry;
	}

	/**
	 * Creates a {@link DeskEntry}.
	 * 
	 * @param deskName
	 *            Name of the {@link DeskModel}.
	 * @param parentRoomEntry
	 *            Parent {@link RoomEntry}.
	 * @return {@link DeskEntry}.
	 */
	public static DeskEntry createDeskEntry(String deskName,
			RoomEntry parentRoomEntry) {

		// Obtain the desk id
		String deskId = "DESK_ID__" + deskName;

		// Create the desk entry
		DeskModel desk = new DeskModel();
		DeskEntry deskEntry = new DeskEntry(deskId, deskName, desk,
				parentRoomEntry);

		// Link into parent room (if provided)
		if (parentRoomEntry != null) {

			// Create the sub room for the desk
			SubRoomModel subRoom = new SubRoomModel();
			subRoom.setId(deskName);
			subRoom.setDesk(deskId);

			// Link into parent room
			parentRoomEntry.getModel().addSubRoom(subRoom);
			parentRoomEntry.deskMap.put(subRoom, deskEntry);
		}

		// Return the desk entry
		return deskEntry;
	}

	/**
	 * Creates a {@link WorkEntry}.
	 * 
	 * @param workId
	 *            Id.
	 * @param workType
	 *            Type of {@link Work}.
	 * @param parentDeskEntry
	 *            Parent {@link DeskEntry}.
	 * @return {@link WorkEntry}.
	 */
	public static <W extends Work> WorkEntry<W> createWorkEntry(String workId,
			Class<W> workType, DeskEntry parentDeskEntry) {

		// Create the work entry
		WorkModel deskWork = new WorkModel();
		deskWork.setWorkName(workId);
		WorkEntry<W> workEntry = new WorkEntry<W>(workId, deskWork, null,
				parentDeskEntry);

		// Link into parent desk
		parentDeskEntry.getModel().addWork(deskWork);
		parentDeskEntry.workMap.put(deskWork, workEntry);

		// Return the work entry
		return workEntry;
	}

	/**
	 * Creates the {@link TaskEntry}.
	 * 
	 * @param flowItemId
	 *            Id.
	 * @param parentWorkEntry
	 *            Parent {@link WorkEntry}.
	 * @return {@link TaskEntry}.
	 */
	public static <W extends Work> TaskEntry<W> createTaskEntry(
			String flowItemId, WorkEntry<W> parentWorkEntry) {

		// Create the desk task
		WorkTaskModel deskTask = new WorkTaskModel();

		// Return with desk task
		return createTaskEntry(flowItemId, deskTask, parentWorkEntry);
	}

	/**
	 * Creates the {@link TaskEntry}, linking to an existing
	 * {@link WorkTaskModel}.
	 * 
	 * @param flowItemId
	 *            Id.
	 * @param deskTask
	 *            {@link WorkTaskModel}.
	 * @param parentWorkEntry
	 *            Parent {@link WorkEntry}.
	 * @return {@link TaskEntry}.
	 */
	public static <W extends Work> TaskEntry<W> createTaskEntry(
			String flowItemId, WorkTaskModel deskTask,
			WorkEntry<W> parentWorkEntry) {

		// Create the flow item
		TaskModel flowItem = new TaskModel();
		flowItem.setTaskName(flowItemId);
		flowItem.setWorkName(parentWorkEntry.getModel().getWorkName());

		// Create the task entry
		TaskEntry<W> task = new TaskEntry<W>(null, flowItem, deskTask,
				parentWorkEntry, null);

		// Link the flow item to the task on the parent work
		parentWorkEntry.getDeskEntry().getModel().addTask(flowItem);
		parentWorkEntry.taskMap.put(flowItem, task);

		// Return the task entry
		return task;
	}

	/**
	 * Creates the {@link TaskEscalationModel}.
	 * 
	 * @param escalationType
	 *            Type of escalation.
	 * @param parentTaskEntry
	 *            Parent {@link TaskEntry}.
	 * @return {@link TaskEscalationModel}.
	 */
	public static <W extends Work> TaskEscalationModel createFlowItemEscalation(
			Class<? extends Throwable> escalationType,
			TaskEntry<W> parentTaskEntry) {

		// Create the escalation
		TaskEscalationModel flowItemEscalation = new TaskEscalationModel();
		flowItemEscalation.setEscalationType(escalationType.getName());

		// Add the escalation to the task
		parentTaskEntry.getModel().addTaskEscalation(flowItemEscalation);

		// Return the flow item escalation
		return flowItemEscalation;
	}

	/**
	 * All access via static methods.
	 */
	private EntryUtil() {
	}

	/**
	 * Creates the {@link net.officefloor.model.room.ExternalEscalationModel}
	 * for the {@link RoomModel}.
	 * 
	 * @param escalationName
	 *            Name of
	 *            {@link net.officefloor.model.room.ExternalEscalationModel}.
	 * @param escalationType
	 *            Type of escalation.
	 * @param parentRoomEntry
	 *            Parent {@link RoomEntry}.
	 * @return {@link net.officefloor.model.room.ExternalEscalationModel}.
	 */
	public static net.officefloor.model.room.ExternalEscalationModel createExternalEscalation(
			String escalationName, Class<? extends Throwable> escalationType,
			RoomEntry parentRoomEntry) {

		// Create the external escalation
		net.officefloor.model.room.ExternalEscalationModel externalEscalation = new net.officefloor.model.room.ExternalEscalationModel();
		externalEscalation.setName(escalationName);
		externalEscalation.setEscalationType(escalationType.getName());

		// Add the escalation to the room
		parentRoomEntry.getModel().addExternalEscalation(externalEscalation);

		// Return the external escalation
		return externalEscalation;
	}

	/**
	 * Creates the {@link SubRoomEscalationModel}.
	 * 
	 * @param externalEscalation
	 *            {@link net.officefloor.model.room.ExternalEscalationModel} of
	 *            the {@link RoomModel}.
	 * @param roomEscalating
	 *            {@link RoomEntry} of the {@link RoomModel}.
	 * @return {@link SubRoomEscalationModel}.
	 * @throws Exception
	 *             If fails to add {@link SubRoomEscalationModel}.
	 */
	public static SubRoomEscalationModel createSubRoomEscalation(
			net.officefloor.model.room.ExternalEscalationModel externalEscalation,
			RoomEntry roomEscalating) throws Exception {

		// Create the sub room escalation
		SubRoomEscalationModel subRoomEscalation = new SubRoomEscalationModel();
		subRoomEscalation.setName(externalEscalation.getName());
		subRoomEscalation.setEscalationType(externalEscalation
				.getEscalationType());

		// Add escalation to sub room for room
		RoomEntry parentRoomEntry = roomEscalating.getParentRoom();
		SubRoomModel subRoom = parentRoomEntry.getSubRoom(roomEscalating);
		subRoom.addEscalation(subRoomEscalation);

		// Return the sub room escalation
		return subRoomEscalation;
	}

	/**
	 * Creates the {@link SubRoomInputFlowModel}.
	 * 
	 * @param flowItem
	 *            {@link TaskModel} being made available as a
	 *            {@link SubRoomInputFlowModel}.
	 * @param parentDeskEntry
	 *            Parent {@link DeskEntry} of the {@link TaskModel}.
	 * @return {@link SubRoomInputFlowModel}.
	 * @throws Exception
	 *             If fails to add {@link SubRoomInputFlowModel}.
	 */
	public static <W extends Work> SubRoomInputFlowModel createSubRoomInputFlow(
			TaskModel flowItem, DeskEntry parentDeskEntry) throws Exception {

		// Flag the flow item as public
		flowItem.setIsPublic(true);

		// Create the sub room input flow
		SubRoomInputFlowModel subRoomInputFlow = new SubRoomInputFlowModel();
		subRoomInputFlow.setName(flowItem.getTaskName());

		// Add the input flow to sub room for desk
		RoomEntry roomEntry = parentDeskEntry.getParentRoom();
		SubRoomModel subRoom = roomEntry.getSubRoom(parentDeskEntry);
		subRoom.addInputFlow(subRoomInputFlow);

		// Return the sub room input flow
		return subRoomInputFlow;
	}

	/**
	 * Creates the {@link SubRoomInputFlowModel}.
	 * 
	 * @param inputFlow
	 *            {@link SubRoomInputFlowModel} being made available on a
	 *            containing {@link RoomModel}.
	 * @param deskEntry
	 *            {@link DeskEntry} of the {@link SubRoomInputFlowModel}.
	 * @return {@link SubRoomInputFlowModel}.
	 * @throws Exception
	 *             If fails to add {@link SubRoomInputFlowModel}.
	 */
	public static SubRoomInputFlowModel createSubRoomInputFlow(
			SubRoomInputFlowModel inputFlow, DeskEntry deskEntry)
			throws Exception {

		// Flag the input flow as public
		inputFlow.setIsPublic(true);

		// Create the name for the input flow
		String inputFlowName = deskEntry.getDeskName() + "-"
				+ inputFlow.getName();

		// Create the sub room input flow
		SubRoomInputFlowModel subRoomInputFlow = new SubRoomInputFlowModel();
		subRoomInputFlow.setName(inputFlowName);

		// Add the input flow to sub room for room containing the desk
		RoomEntry roomEntry = deskEntry.getParentRoom();
		RoomEntry parentRoomEntry = roomEntry.getParentRoom();
		SubRoomModel subRoom = parentRoomEntry.getSubRoom(roomEntry);
		subRoom.addInputFlow(subRoomInputFlow);

		// Return the sub room input flow
		return subRoomInputFlow;
	}

}
