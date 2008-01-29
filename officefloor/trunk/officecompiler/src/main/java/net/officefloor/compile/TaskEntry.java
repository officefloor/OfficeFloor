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

import java.util.Deque;
import java.util.LinkedList;

import net.officefloor.LoaderContext;
import net.officefloor.desk.DeskLoader;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToFlowItemModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.desk.FlowItemOutputToExternalFlowModel;
import net.officefloor.model.desk.FlowItemOutputToFlowItemModel;
import net.officefloor.model.desk.FlowItemToNextExternalFlowModel;
import net.officefloor.model.desk.FlowItemToNextFlowItemModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.OfficeDeskModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.room.OutputFlowToExternalFlowModel;
import net.officefloor.model.room.OutputFlowToInputFlowModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.util.OFCU;
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
				deskTask, workEntry, context.getLoaderContext());

		// Register the task entry
		workEntry.getDeskEntry().registerTask(flowItem, taskEntry);

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
	 * {@link LoaderContext} for loading necessary classes.
	 */
	private final LoaderContext loaderContext;

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
	 * @param loaderContext
	 *            {@link LoaderContext}.
	 */
	public TaskEntry(TaskBuilder<Object, W, Indexed, Indexed> builder,
			FlowItemModel flowItem, DeskTaskModel deskTask,
			WorkEntry<W> workEntry, LoaderContext loaderContext) {
		super(flowItem.getId(), builder, flowItem);
		this.deskTask = deskTask;
		this.workEntry = workEntry;
		this.loaderContext = loaderContext;
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
	 * Obtains the {@link ExternalTeamModel} of the {@link OfficeModel} for this
	 * {@link TaskEntry}.
	 * 
	 * @return {@link ExternalTeamModel} for this {@link TaskEntry}.
	 * @throws Exception
	 *             If fail to find {@link ExternalTeamModel}.
	 */
	public ExternalTeamModel getOfficeTeamModel() throws Exception {

		// Create the hierarchy of desk/room names
		Deque<String> hierarchy = new LinkedList<String>();
		DeskEntry deskEntry = this.workEntry.getDeskEntry();
		hierarchy.push(deskEntry.getDeskName());
		RoomEntry roomEntry = deskEntry.getParentRoom();
		OfficeEntry officeEntry = null;
		while (roomEntry != null) {
			hierarchy.push(roomEntry.getRoomName());
			officeEntry = roomEntry.getOffice();
			roomEntry = roomEntry.getParentRoom();
		}

		// Obtain the external team on the office
		OfficeRoomModel officeRoom = null;
		OfficeDeskModel officeDesk = null;
		while (!hierarchy.isEmpty()) {

			// Obtain the next item down in the hierarchy
			String itemName = hierarchy.pop();

			// Specify based on location
			if (officeDesk != null) {
				// Hierarchy should be empty when have desk
				throw new Exception("Hierarchy of office "
						+ officeEntry.getId() + " is out of sync for work "
						+ this.workEntry.getCanonicalWorkName() + " [task "
						+ this.getId() + "]");
			} else if (officeRoom == null) {
				// Top level room
				officeRoom = officeEntry.getModel().getRoom();
			} else {
				// Find the sub room by the hierarchy
				OfficeRoomModel childRoom = null;
				for (OfficeRoomModel subRoom : officeRoom.getSubRooms()) {
					if (itemName.equals(subRoom.getName())) {
						childRoom = subRoom;
					}
				}

				// Handle based on whether a room
				if (childRoom != null) {
					// Child is a room
					officeRoom = childRoom;
				} else {
					// Not a room therefore must be a desk
					for (OfficeDeskModel subRoom : officeRoom.getDesks()) {
						if (itemName.equals(subRoom.getName())) {
							officeDesk = subRoom;
						}
					}

					// Ensure have the desk
					if (officeDesk == null) {
						throw new Exception("Hierarchy of office "
								+ officeEntry.getId()
								+ " is out of sync for work "
								+ this.workEntry.getCanonicalWorkName()
								+ " [task " + this.getId() + "]");
					}
				}
			}
		}

		// Have office desk so find task on it
		net.officefloor.model.office.FlowItemModel officeFlowItem = null;
		for (net.officefloor.model.office.FlowItemModel of : officeDesk
				.getFlowItems()) {
			if (this.getModel().getId().equals(of.getId())) {
				officeFlowItem = of;
			}
		}
		if (officeFlowItem == null) {
			throw new Exception("No corresponding flow item "
					+ this.getModel().getId() + " on office desk "
					+ officeDesk.getName() + " of office "
					+ officeEntry.getId());
		}

		// Obtain the external office team
		ExternalTeamModel officeTeam = OFCU.get(officeFlowItem.getTeam(),
				"No team for ${0}", officeFlowItem.getName()).getTeam();

		// Return the external office team
		return officeTeam;
	}

	/**
	 * Builds the {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public void build() throws Exception {

		// Obtain task and its details
		TaskModel<?, ?> task = this.deskTask.getTask();
		TaskFactory taskFactory = task.getTaskFactoryManufacturer()
				.createTaskFactory();

		// Initiate the task factory if necessary
		if (taskFactory instanceof CompilerAwareTaskFactory) {
			((CompilerAwareTaskFactory) taskFactory).initialiseTaskFactory(this
					.getModel());
		}

		// Load details of task
		this.getBuilder().setTaskFactory(taskFactory);

		// Set team for flow item
		ExternalTeamModel team = this.getOfficeTeamModel();
		String teamName = team.getName();
		this.getBuilder().setTeam(teamName);

		// Obtain the office
		OfficeEntry officeEntry = this.workEntry.getOfficeEntry();

		// Obtain the office floor team instance
		OfficeFloorEntry officeFloorEntry = officeEntry.getOfficeFloorEntry();
		OfficeTeamModel officeTeam = officeFloorEntry.getOfficeTeamModel(
				officeEntry, teamName);

		// Link team into office
		String teamId = officeTeam.getTeam().getTeam().getId();
		officeEntry.getBuilder().registerTeam(teamName, teamId);

		// Link in the managed objects
		this.linkManagedObjects();

		// Link in the flows
		this.linkFlows();

		// Link in the next flow
		this.linkNextFlow();

		// Link in the escalations
		this.linkEscalations();
	}

	/**
	 * Links in the {@link ManagedObject} instances.
	 */
	private void linkManagedObjects() {
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
	}

	/**
	 * Links in the flows.
	 * 
	 * @throws Exception
	 *             If fails to link the flows.
	 */
	private void linkFlows() throws Exception {

		// Specify the linked flows
		int flowIndex = 0;
		for (FlowItemOutputModel flowItemOutput : this.getModel().getOutputs()) {

			// Make the flow index accessible to anonymous class
			final int accessibleFlowIndex = flowIndex;

			// Flag as must be linked
			boolean isLinked = false;

			// Obtain the linked flow (from same desk)
			FlowItemOutputToFlowItemModel linkedFlowItem = flowItemOutput
					.getFlowItem();
			if (linkedFlowItem != null) {

				// Obtain the flow instigation strategy
				final FlowInstigationStrategyEnum strategy = DeskLoader
						.getFlowInstigationStrategyEnum(linkedFlowItem
								.getLinkType());

				// Link in the flow
				this.linkFlow(linkedFlowItem.getFlowItem(), new FlowLinker() {
					@Override
					public void linkFlow(String workName, String taskName) {
						if (workName == null) {
							// On same work
							TaskEntry.this.getBuilder().linkFlow(
									accessibleFlowIndex, taskName, strategy);
						} else {
							// Different work
							TaskEntry.this.getBuilder().linkFlow(
									accessibleFlowIndex, workName, taskName,
									strategy);
						}
					}
				});

				// Linked
				isLinked = true;
			}

			// Obtain the linked flow (from another desk)
			FlowItemOutputToExternalFlowModel linkedExternalFlow = flowItemOutput
					.getExternalFlow();
			if (linkedExternalFlow != null) {

				// Obtain the flow instigation strategy
				final FlowInstigationStrategyEnum strategy = DeskLoader
						.getFlowInstigationStrategyEnum(linkedFlowItem
								.getLinkType());

				// Link in the flow
				this.linkFlow(linkedExternalFlow.getExternalFlow(),
						new FlowLinker() {
							@Override
							public void linkFlow(String workName,
									String taskName) {
								TaskEntry.this.getBuilder().linkFlow(
										accessibleFlowIndex, workName,
										taskName, strategy);
							}
						});

				// Linked
				isLinked = true;
			}

			// Ensure linked
			if (!isLinked) {
				throw new Exception("Flow item output "
						+ flowItemOutput.getId() + " on flow item "
						+ this.getId() + " not linked to a flow");
			}

			// Increment flow index for next iteration
			flowIndex++;
		}
	}

	/**
	 * Links in the next flow.
	 * 
	 * @throws Exception
	 *             If fails to link in next flow.
	 */
	private void linkNextFlow() throws Exception {

		// Specify the next flow (from same desk)
		FlowItemToNextFlowItemModel nextFlowItem = this.getModel()
				.getNextFlowItem();
		if (nextFlowItem != null) {
			// Link flow
			this.linkFlow(nextFlowItem.getNextFlowItem(), new FlowLinker() {
				@Override
				public void linkFlow(String workName, String taskName) {
					if (workName == null) {
						// On same work
						TaskEntry.this.getBuilder().setNextTaskInFlow(taskName);
					} else {
						// Different work
						TaskEntry.this.getBuilder().setNextTaskInFlow(workName,
								taskName);
					}
				}
			});
		}

		// Specify the next flow (from another desk)
		FlowItemToNextExternalFlowModel nextExternalFlow = this.getModel()
				.getNextExternalFlow();
		if (nextExternalFlow != null) {
			// Link the flow
			this.linkFlow(nextExternalFlow.getNextExternalFlow(),
					new FlowLinker() {
						@Override
						public void linkFlow(String workName, String taskName) {
							// Register the next flow
							TaskEntry.this.getBuilder().setNextTaskInFlow(
									workName, taskName);
						}
					});
		}
	}

	/**
	 * Links in the escalations.
	 * 
	 * @throws Exception
	 *             If fails to link in handling of escalations.
	 */
	private void linkEscalations() throws Exception {

		// Link in handling of each escalation
		for (FlowItemEscalationModel flowItemEscalation : this.getModel()
				.getEscalations()) {

			// Flag as must be linked
			boolean isLinked = false;

			// Obtain the handling (from same desk)
			FlowItemEscalationToFlowItemModel handlingFlowItem = flowItemEscalation
					.getEscalationHandler();
			if (handlingFlowItem != null) {

				// Obtain the escalation type
				String escalationTypeName = flowItemEscalation
						.getEscalationType();
				final Class<? extends Throwable> escalationType = this.loaderContext
						.obtainClass(escalationTypeName, Throwable.class);

				// Link in the flow
				this.linkFlow(handlingFlowItem.getHandler(), new FlowLinker() {
					@Override
					public void linkFlow(String workName, String taskName) {
						if (workName == null) {
							// Handled by same work
							TaskEntry.this.getBuilder().addEscalation(
									escalationType, true, taskName);
						} else {
							// Handled by another work
							TaskEntry.this.getBuilder().addEscalation(
									escalationType, true, workName, taskName);
						}
					}
				});

				// Linked
				isLinked = true;
			}

			// Ensure linked
			if (!isLinked) {
				throw new Exception("Escalation "
						+ flowItemEscalation.getEscalationType()
						+ " on flow item " + this.getId() + " not handled");
			}
		}
	}

	/**
	 * Interface called to link flows.
	 */
	private interface FlowLinker {

		/**
		 * Invoked to link the flow.
		 * 
		 * @param workName
		 *            Name of the {@link Work}. <code>null</code> if same
		 *            work as this {@link TaskEntry}.
		 * @param taskName
		 *            Name of the {@link Task}.
		 */
		void linkFlow(String workName, String taskName);
	}

	/**
	 * Links to {@link FlowItemModel} in same {@link DeskModel}.
	 * 
	 * @param targetFlowItem
	 *            Target {@link FlowItemModel}.
	 * @param flowLinker
	 *            {@link FlowLinker}.
	 * @throws Exception
	 *             If fails to link.
	 */
	private void linkFlow(FlowItemModel targetFlowItem, FlowLinker flowLinker)
			throws Exception {

		// Obtain the task entry for the target flow item
		TaskEntry<?> nextTask = this.workEntry.getDeskEntry().getTaskEntry(
				targetFlowItem);

		// Register the next task
		if (this.workEntry == nextTask.workEntry) {
			// Same work
			flowLinker.linkFlow(null, nextTask.getId());
		} else {
			// Different work
			flowLinker.linkFlow(nextTask.workEntry.getCanonicalWorkName(),
					nextTask.getId());
		}
	}

	/**
	 * Links {@link ExternalFlowModel} in another {@link DeskModel}.
	 * 
	 * @param targetExternalFlow
	 *            Target {@link ExternalFlowModel}.
	 * @param flowLinker
	 *            {@link FlowLinker}.
	 * @throws Exception
	 *             If fails to link.
	 */
	private void linkFlow(ExternalFlowModel targetExternalFlow,
			FlowLinker flowLinker) throws Exception {

		// Obtain the external flow name
		String externalFlowName = targetExternalFlow.getName();

		// Obtain the desk containing the external flow name
		DeskEntry deskEntry = this.workEntry.getDeskEntry();

		// Obtain room containing the desk
		RoomEntry roomEntry = deskEntry.getParentRoom();

		// Obtain the desk sub room
		SubRoomModel subRoom = roomEntry.getSubRoom(deskEntry);

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
		String subRoomName = inConn.getSubRoomName();
		String inputFlowName = inConn.getInput().getName();

		// Find the desk
		deskEntry = null; // reset to find
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

		// Obtain the flow item on the desk
		FlowItemModel flowItem = null;
		for (FlowItemModel fi : deskEntry.getModel().getFlowItems()) {
			if (inputFlowName.equals(fi.getId())) {
				flowItem = fi;
			}
		}

		// Obtain the work entry
		DeskWorkModel workModel = deskEntry
				.getWorkModel(flowItem.getWorkName());
		WorkEntry<?> workEntry = deskEntry.getWorkEntry(workModel);
		String workName = workEntry.getCanonicalWorkName();

		// Link the flow
		flowLinker.linkFlow(workName, inputFlowName);
	}

}
