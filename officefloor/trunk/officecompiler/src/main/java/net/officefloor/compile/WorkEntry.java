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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.util.OFCU;

/**
 * {@link net.officefloor.frame.api.execute.Work} entry for the
 * {@link net.officefloor.frame.api.manage.Office}.
 * 
 * @author Daniel
 */
public class WorkEntry<W extends Work> extends
		AbstractEntry<WorkBuilder<W>, DeskWorkModel> {

	/**
	 * Process scope for the {@link ManagedObject}.
	 */
	public static final String MANAGED_OBJECT_SCOPE_PROCESS = "process";

	/**
	 * Work scope for the {@link ManagedObject}.
	 */
	public static final String MANAGED_OBJECT_SCOPE_WORK = "work";

	/**
	 * Listing of all {@link ManagedObject} scopes.
	 */
	public static final String[] MANAGED_OBJECT_SCOPES = new String[] {
			MANAGED_OBJECT_SCOPE_PROCESS, MANAGED_OBJECT_SCOPE_WORK };

	/**
	 * Loads the {@link WorkEntry}.
	 * 
	 * @param deskWork
	 *            {@link DeskWorkModel}.
	 * @param deskEntry
	 *            {@link DeskEntry} containing this {@link WorkEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return Loaded {@link WorkEntry}.
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public static <W extends Work> WorkEntry<W> loadWork(
			DeskWorkModel deskWork, DeskEntry deskEntry,
			OfficeFloorCompilerContext context) throws Exception {

		// Obtain the work (and its details)
		WorkModel work = OFCU.get(deskWork.getWork(),
				"No work on desk work ${0}", deskWork.getId());
		Class<W> typeOfWork = work.getTypeOfWork();

		// Create the work builder
		WorkBuilder<W> builder = context.getBuilderFactory().createWorkBuilder(
				typeOfWork);

		// Create the work entry
		WorkEntry<W> workEntry = new WorkEntry<W>(deskWork.getId(), deskWork,
				builder, deskEntry);

		// Create the registry of Desk Task instances
		Map<String, DeskTaskModel> tasks = new HashMap<String, DeskTaskModel>();
		for (DeskTaskModel task : deskWork.getTasks()) {
			tasks.put(task.getName(), task);
		}

		// Load flow items as tasks of work
		String workId = deskWork.getId();
		for (FlowItemModel flowItem : deskEntry.getModel().getFlowItems()) {
			// Check if flow item of the work
			if (workId.equals(flowItem.getWorkName())) {

				// Obtain the task for the flow item
				DeskTaskModel deskTask = tasks.get(flowItem.getTaskName());

				// Load the task entry
				TaskEntry taskEntry = TaskEntry.loadTask(flowItem, deskTask,
						workEntry, context);
				workEntry.taskMap.put(flowItem, taskEntry);
				workEntry.tasks.add(taskEntry);
			}
		}

		// Return the work entry
		return workEntry;
	}

	/**
	 * {@link DeskEntry}.
	 */
	private final DeskEntry deskEntry;

	/**
	 * {@link FlowItemModel} to {@link TaskEntry} map.
	 */
	private final ModelEntryMap<FlowItemModel, TaskEntry<?>> taskMap = new ModelEntryMap<FlowItemModel, TaskEntry<?>>();

	/**
	 * {@link TaskEntry} instances of this {@link WorkEntry}.
	 */
	private final List<TaskEntry<W>> tasks = new LinkedList<TaskEntry<W>>();

	/**
	 * Initiate.
	 * 
	 * @param workId
	 *            Id of the {@link Work}.
	 * @param builder
	 *            {@link WorkBuilder}.
	 * @param deskWork
	 *            {@link DeskWorkModel}.
	 * @param deskEntry
	 *            {@link DeskEntry}.
	 * @param room
	 *            {@link RoomEntry} containing this {@link WorkEntry}.
	 */
	public WorkEntry(String workId, DeskWorkModel workModel,
			WorkBuilder<W> builder, DeskEntry deskEntry) {
		super(workId, builder, workModel);
		this.deskEntry = deskEntry;
	}

	/**
	 * Obtains the {@link DeskEntry} for this {@link WorkEntry}.
	 * 
	 * @return {@link DeskEntry} for this {@link WorkEntry}.
	 */
	public DeskEntry getDeskEntry() {
		return this.deskEntry;
	}

	/**
	 * Obtains the {@link OfficeEntry} for this {@link WorkEntry}.
	 * 
	 * @return {@link OfficeEntry} for this {@link WorkEntry}.
	 */
	public OfficeEntry getOfficeEntry() {
		OfficeEntry officeEntry = null;
		RoomEntry roomEntry = this.deskEntry.getParentRoom();
		while (roomEntry != null) {
			officeEntry = roomEntry.getOffice();
			roomEntry = roomEntry.getParentRoom();
		}
		return officeEntry;
	}

	/**
	 * Creates the canonical work name that this {@link WorkModel} is registered
	 * under.
	 * 
	 * @return Canonical work name.
	 */
	public String getCanonicalWorkName() {
		// Create the canonical work name
		String canonicalWorkName = this.getId();
		final String SEPARATOR = ".";
		DeskEntry desk = this.getDeskEntry();
		canonicalWorkName = desk.getDeskName() + SEPARATOR + canonicalWorkName;
		RoomEntry roomEntry = desk.getParentRoom();
		while (roomEntry != null) {
			if (roomEntry.getParentRoom() != null) {
				// Not top room so include
				canonicalWorkName = roomEntry.getRoomName() + SEPARATOR
						+ canonicalWorkName;
			}
			roomEntry = roomEntry.getParentRoom();
		}

		// Return the canonical work name
		return canonicalWorkName;
	}

	/**
	 * Obtains the {@link TaskEntry} for the {@link FlowItemModel}.
	 * 
	 * @param flowItemModel
	 *            {@link FlowItemModel}.
	 * @return {@link TaskEntry}.
	 * @throws Exception
	 *             If not found.
	 */
	public TaskEntry<?> getTaskEntry(FlowItemModel flowItemModel)
			throws Exception {
		return this.getEntry(flowItemModel, this.taskMap, "No task '"
				+ flowItemModel.getId() + "' on work "
				+ this.getModel().getId() + " of desk "
				+ this.deskEntry.getId());
	}

	/**
	 * Builds the remaining aspects of the {@link Work}.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public void build() throws Exception {

		// Create the canonical work name
		String canonicalWorkName = this.getCanonicalWorkName();

		// Register this work with its office
		OfficeEntry officeEntry = this.getOfficeEntry();
		officeEntry.getBuilder().addWork(canonicalWorkName, this.getBuilder());

		// Obtain the work (and its details)
		DeskWorkModel deskWork = this.getModel();
		WorkModel work = deskWork.getWork();
		WorkFactory<W> workFactory = (WorkFactory<W>) work.getWorkFactory();
		FlowItemModel initialFlowItem = OFCU.get(deskWork.getInitialFlowItem(),
				"No initial flow for work ${0}", deskWork.getId())
				.getInitialFlowItem();

		// Load details of work
		this.getBuilder().setWorkFactory(workFactory);
		this.getBuilder().setInitialTask(initialFlowItem.getId());

		// Create the listing of external managed objects
		Set<ExternalManagedObjectModel> externalManagedObjects = new HashSet<ExternalManagedObjectModel>();
		for (TaskEntry task : this.tasks) {
			for (DeskTaskObjectModel taskObject : task.getDeskTaskModel()
					.getObjects()) {

				// Do not include parameters
				if (taskObject.getIsParameter()) {
					continue;
				}

				// Register the external managed object
				externalManagedObjects.add(OFCU.get(
						taskObject.getManagedObject(),
						"No managed object for task ${0} object ${1}",
						task.getId(), taskObject.getObjectType())
						.getManagedObject());
			}
		}

		// Build the managed objects
		for (ExternalManagedObjectModel externalManagedObject : externalManagedObjects) {
			this.buildManagedObject(externalManagedObject);
		}

		// Build the tasks
		for (TaskEntry taskEntry : this.tasks) {
			taskEntry.build();
		}
	}

	/**
	 * Builds the managed object.
	 * 
	 * @param deskMo
	 *            {@link ExternalManagedObjectModel}.
	 * @throws Exception
	 *             If fails.
	 */
	public void buildManagedObject(ExternalManagedObjectModel deskMo)
			throws Exception {

		// Obtain the external managed object name
		String externalMoName = deskMo.getName();

		// Obtain the desk sub room
		SubRoomModel subRoom = this.deskEntry.getParentRoom().getSubRoom(
				this.deskEntry);

		// Obtain the room containing the desk
		RoomEntry roomEntry = this.deskEntry.getParentRoom();

		// Obtain the office external managed object
		OfficeEntry officeEntry = null;
		while (roomEntry != null) {

			// Obtain the external managed object name on the desk sub room
			SubRoomManagedObjectModel subRoomMo = roomEntry
					.getSubRoomManagedObject(subRoom, externalMoName);

			// Obtain the external managed object name for the room
			externalMoName = subRoomMo.getExternalManagedObject()
					.getExternalManagedObject().getName();

			// Obtain parent room of room or office
			officeEntry = roomEntry.getOffice(); // obtain before changing
			roomEntry = roomEntry.getParentRoom();
		}

		// Obtain the office within the office floor
		OfficeFloorEntry officeFloorEntry = officeEntry.getOfficeFloorEntry();
		OfficeFloorOfficeModel office = officeFloorEntry
				.getOfficeFloorOfficeModel(officeEntry);

		// Obtain the office external managed object
		net.officefloor.model.office.ExternalManagedObjectModel officeExtMo = null;
		for (net.officefloor.model.office.ExternalManagedObjectModel mo : officeEntry
				.getModel().getExternalManagedObjects()) {
			if (externalMoName.equals(mo.getName())) {
				officeExtMo = mo;
			}
		}
		if (officeExtMo == null) {
			throw new Exception("Can not find external managed object '"
					+ externalMoName + "' of office " + officeEntry.getId());
		}

		// Build managed object based on its scope
		if (MANAGED_OBJECT_SCOPE_PROCESS.equals(officeExtMo.getScope())) {
			// Register the process managed object to this work
			officeEntry.getBuilder().addProcessManagedObject(
					"p:" + externalMoName, externalMoName);
			this.getBuilder().registerProcessManagedObject(deskMo.getName(),
					"p:" + externalMoName);

		} else if (MANAGED_OBJECT_SCOPE_WORK.equals(officeExtMo.getScope())) {
			// Register the work managed object to this work
			this.getBuilder().addWorkManagedObject(deskMo.getName(),
					externalMoName);

		} else {
			throw new Exception("Unknown scope '" + officeExtMo.getScope()
					+ "' for managed object '" + externalMoName
					+ "' of office '" + office.getId() + "'");
		}

		// Obtain office floor managed object
		OfficeManagedObjectModel officeMo = null;
		for (OfficeManagedObjectModel mo : office.getManagedObjects()) {
			if (officeExtMo.getName().equals(mo.getManagedObjectName())) {
				officeMo = mo;
			}
		}
		if (officeMo == null) {
			throw new Exception("Can not find managed object '"
					+ externalMoName + "' for office " + office.getId());
		}

		// Register the managed object to the office
		officeEntry.getBuilder().registerManagedObject(
				externalMoName,
				officeMo.getManagedObjectSource().getManagedObjectSource()
						.getId());
	}

}
