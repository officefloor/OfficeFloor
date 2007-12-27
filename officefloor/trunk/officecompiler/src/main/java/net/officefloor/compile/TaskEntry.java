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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemToNextExternalFlowModel;
import net.officefloor.model.desk.FlowItemToNextFlowItemModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.room.OutputFlowToExternalFlowModel;
import net.officefloor.model.room.OutputFlowToInputFlowModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.work.CompilerAwareTaskFactory;

/**
 * {@link net.officefloor.frame.api.execute.Task} for the
 * {@link net.officefloor.frame.api.execute.Work}.
 * 
 * @author Daniel
 */
public class TaskEntry<W extends Work> extends
		AbstractEntry<TaskBuilder<Object, W, Indexed, Indexed>, FlowItemModel> {

	/**
	 * Loads the {@link TaskEntry}.
	 * 
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 * @param deskTask
	 *            {@link DeskTaskModel}.
	 * @param workEntry
	 *            {@link WorkEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return Loaded {@link TaskEntry}.
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public static <W extends Work> TaskEntry loadTask(FlowItemModel flowItem,
			DeskTaskModel deskTask, WorkEntry<W> workEntry,
			OfficeFloorCompilerContext context) throws Exception {

		// Obtain the parameter type for the task
		Class parameterType = null;
		for (DeskTaskObjectModel taskObject : deskTask.getObjects()) {
			if (taskObject.getIsParameter()) {
				parameterType = context.getLoaderContext().obtainClass(
						taskObject.getObjectType());
			}
		}

		// Create the builder
		TaskBuilder<Object, W, Indexed, Indexed> taskBuilder = workEntry
				.getBuilder().addTask(flowItem.getId(), parameterType);

		// Create the task entry
		TaskEntry<W> taskEntry = new TaskEntry<W>(taskBuilder, flowItem,
				deskTask, workEntry);

		// Register the task entry
		context.getTaskRegistry().put(flowItem, taskEntry);

		// Return the task entry
		return taskEntry;
	}

	/**
	 * {@link DeskTaskModel}.
	 */
	private final DeskTaskModel deskTask;

	/**
	 * {@link WorkEntry} for this {@link TaskEntry}.
	 */
	private final WorkEntry<W> workEntry;

	/**
	 * Initiate.
	 * 
	 * @param builder
	 *            {@link TaskBuilder}.
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 * @param deskTask
	 *            {@link DeskTaskModel}.
	 * @param workEntry
	 *            {@link WorkEntry} for this {@link TaskEntry}.
	 */
	public TaskEntry(TaskBuilder<Object, W, Indexed, Indexed> builder,
			FlowItemModel flowItem, DeskTaskModel deskTask,
			WorkEntry<W> workEntry) {
		super(flowItem.getId(), builder, flowItem);
		this.deskTask = deskTask;
		this.workEntry = workEntry;
	}

	/**
	 * Obtains the {@link DeskTaskModel}.
	 * 
	 * @return {@link DeskTaskModel}.
	 */
	public DeskTaskModel getDeskTaskModel() {
		return this.deskTask;
	}

	/**
	 * Builds the {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @param compilerContext
	 *            {@link OfficeFloorCompilerContext}.
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public void build(OfficeFloorCompilerContext compilerContext)
			throws Exception {

		// Obtain task and its details
		TaskModel<?, ?> task = this.deskTask.getTask();
		TaskFactory taskFactory = task.getTaskFactory();

		// Initiate the task factory if necessary
		if (taskFactory instanceof CompilerAwareTaskFactory) {
			((CompilerAwareTaskFactory) taskFactory)
					.initialiseTaskFactory(this.deskTask);
		}

		// Load details of task
		this.getBuilder().setTaskFactory(taskFactory);

		// Obtain the office
		OfficeEntry officeEntry = this.workEntry.getOfficeEntry();

		// Obtain the office flow item for this task
		net.officefloor.model.office.FlowItemModel officeFlowItem = officeEntry
				.getFlowItemModel(this.workEntry.getDeskEntry().getId(), this
						.getId());

		// Set team for flow item
		String teamName = officeFlowItem.getTeam().getTeam().getName();
		this.getBuilder().setTeam(teamName);

		// Obtain the office floor team instance
		OfficeFloorEntry officeFloorEntry = officeEntry.getOfficeFloorEntry();
		OfficeTeamModel officeTeam = officeFloorEntry.getOfficeTeamModel(
				officeEntry.getId(), teamName);

		// Link team into office
		String teamId = officeTeam.getTeam().getTeam().getId();
		officeEntry.getBuilder().registerTeam(teamName, teamId);

		// Link in the managed objects
		int index = 0;
		for (DeskTaskObjectModel taskObject : this.deskTask.getObjects()) {

			// Do not include parameters
			if (taskObject.getIsParameter()) {
				continue;
			}

			// Link in the managed object
			this.getBuilder().linkManagedObject(index++,
					taskObject.getManagedObject().getName());
		}

		// Specify the next flow (from same desk)
		FlowItemModel flowItem = this.getModel();
		FlowItemToNextFlowItemModel nextFlowItem = flowItem.getNextFlowItem();
		if (nextFlowItem != null) {
			// Obtain the task entry for the next flow item
			TaskEntry<?> nextTask = compilerContext.getTaskRegistry().get(
					nextFlowItem.getNextFlowItem());

			// Register the next task
			if (this.workEntry == nextTask.workEntry) {
				// Same work
				this.getBuilder().setNextTaskInFlow(nextTask.getId());
			} else {
				// Different work
				this.getBuilder().setNextTaskInFlow(
						nextTask.workEntry.getCanonicalWorkName(),
						nextTask.getId());
			}
		}

		// Specify the next flow (from another desk)
		FlowItemToNextExternalFlowModel nextExternalFlow = flowItem
				.getNextExternalFlow();
		if (nextExternalFlow != null) {

			// Obtain the external flow name
			String externalFlowName = nextExternalFlow.getNextExternalFlow()
					.getName();

			// Obtain the desk containing the external flow name
			DeskEntry deskEntry = this.workEntry.getDeskEntry();
			String subRoomName = deskEntry.getDeskName();

			// Obtain room containing the desk
			RoomEntry roomEntry = deskEntry.getParentRoom();

			// Loop until reached room which starts linking down
			SubRoomOutputFlowModel outputFlow = null;
			while (externalFlowName != null) {

				// Obtain the output flow within the room
				outputFlow = roomEntry.getSubRoomOutputFlow(subRoomName,
						externalFlowName);

				// Follow flow
				OutputFlowToExternalFlowModel extConn = outputFlow
						.getExternalFlow();
				if (extConn != null) {
					// External flow (set details to find)
					externalFlowName = extConn.getExternalFlow().getName();
					subRoomName = roomEntry.getRoomName();

					// Obtain parent room to follow external flow
					roomEntry = roomEntry.getParentRoom();

				} else {
					// No longer going to external flow
					externalFlowName = null;
				}
			}

			// Linking to another room
			OutputFlowToInputFlowModel inConn = outputFlow.getInput();
			subRoomName = inConn.getSubRoomName();
			String inputFlowName = inConn.getInput().getName();

			// Find the desk
			deskEntry = null; // reset to find
			while (deskEntry == null) {

				// Obtain the sub room
				SubRoomModel subRoom = roomEntry.getSubRoom(subRoomName);

				// Obtain sub entry
				String entryId = subRoom.getRoom();
				if (entryId != null) {
					// Entry is a room

					// Obtain the sub room entry
					roomEntry = compilerContext.getRoomEntry().get(entryId);

					// TODO reduce coupling of room hierarchy.
					// Decode the sub room and input flow
					subRoomName = inputFlowName.split("-")[0];
					inputFlowName = inputFlowName.substring(subRoomName
							.length()
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

					// Obtain the input flow of the sub room
					inputFlowName = inputFlow.getName();

				} else {
					// Entry is a desk
					entryId = subRoom.getDesk();
					deskEntry = compilerContext.getDeskRegistry().get(entryId);
					if (deskEntry == null) {
						throw new Exception("No desk by Id '" + entryId + "'");
					}
				}
			}

			// Obtain the flow item on the desk
			flowItem = null;
			for (FlowItemModel fi : deskEntry.getModel().getFlowItems()) {
				if (inputFlowName.equals(fi.getId())) {
					flowItem = fi;
				}
			}

			// Obtain the work entry
			WorkEntry<?> workEntry = deskEntry.getWorkEntry(flowItem
					.getWorkName());
			String workName = workEntry.getCanonicalWorkName();

			// Register the next flow
			this.getBuilder().setNextTaskInFlow(workName, inputFlowName);
		}
	}
}
