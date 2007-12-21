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
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
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

		// Register the work entry
		context.getWorkRegistry().put(workEntry.getId(), workEntry);

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

				// Register the task entry with the work entry
				workEntry.registerTask(flowItem.getId(), taskEntry);
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
	 * {@link TaskEntry} instances of this {@link WorkEntry}.
	 */
	private final Map<String, TaskEntry<W>> tasks = new HashMap<String, TaskEntry<W>>();

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
	 * Registers the {@link TaskEntry}.
	 * 
	 * @param taskName
	 *            Name of the {@link net.officefloor.frame.api.execute.Task}.
	 * @param taskEntry
	 *            {@link TaskEntry}.
	 */
	public void registerTask(String taskName, TaskEntry<W> taskEntry) {
		this.tasks.put(taskName, taskEntry);
	}

	/**
	 * Obtains the registry of the {@link TaskEntry} instances for this
	 * {@link WorkEntry}.
	 * 
	 * @return {@link TaskEntry} registry.
	 */
	public Map<String, TaskEntry<W>> getTaskRegistry() {
		return this.tasks;
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
	 * Builds the remaining aspects of the {@link Work}.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public void build() throws Exception {

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
		for (TaskEntry task : this.tasks.values()) {
			for (DeskTaskObjectModel taskObject : task.getDeskTaskModel()
					.getObjects()) {

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
		for (TaskEntry taskEntry : this.tasks.values()) {
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

		// Obtain desk room Id
		String deskRoomId = this.deskEntry.getId();

		// Obtain the office external managed object
		OfficeEntry officeEntry = null;
		boolean isDesk = true;
		RoomEntry roomEntry = this.deskEntry.getParentRoom();
		while (roomEntry != null) {

			// Obtain the sub room for the desk/room
			SubRoomModel subRoom = null;
			if (isDesk) {
				subRoom = roomEntry.getDesk(deskRoomId);
				isDesk = false; // no longer a desk
			} else {
				subRoom = roomEntry.getSubRoom(deskRoomId);
			}

			// Obtain the sub room managed object
			SubRoomManagedObjectModel subRoomMo = null;
			for (SubRoomManagedObjectModel srMo : subRoom.getManagedObjects()) {
				if (externalMoName.equals(srMo.getName())) {
					subRoomMo = srMo;
				}
			}
			if (subRoomMo == null) {
				throw new Exception("Can not find managed object '"
						+ externalMoName + "' for sub room '" + deskRoomId
						+ "'");
			}

			// Obtain the external managed object name for the room
			externalMoName = subRoomMo.getExternalManagedObject()
					.getExternalManagedObject().getName();

			// Obtain parent room of room or office
			officeEntry = roomEntry.getOffice(); // obtain before changing
			roomEntry = roomEntry.getParentRoom();
		}

		// Obtain the office floor of the office
		OfficeFloorEntry officeFloorEntry = officeEntry.getOfficeFloorEntry();

		// Obtain office from office floor
		OfficeFloorOfficeModel office = officeFloorEntry
				.getOfficeFloorOfficeModel(officeEntry.getId());

		// Obtain office managed object
		OfficeManagedObjectModel officeMo = null;
		for (OfficeManagedObjectModel mo : office.getManagedObjects()) {
			if (externalMoName.equals(mo.getManagedObjectName())) {
				officeMo = mo;
			}
		}
		if (officeMo == null) {
			throw new Exception("Can not find managed object '"
					+ externalMoName + "' for office " + office.getId());
		}

		// TODO remove
		officeMo.setScope("process");

		// Build managed object based on its scope
		if ("process".equals(officeMo.getScope())) {
			// Register the process managed object to this work
			officeEntry.getBuilder().addProcessManagedObject(
					"p:" + externalMoName, externalMoName);
			this.getBuilder().registerProcessManagedObject(deskMo.getName(),
					"p:" + externalMoName);

		} else if ("work".equals(officeMo.getScope())) {
			// Register the work managed object to this work
			this.getBuilder().addWorkManagedObject(deskMo.getName(),
					externalMoName);

		} else {
			throw new Exception("Unknown scope '" + officeMo.getScope()
					+ "' for managed object '" + externalMoName
					+ "' of office '" + office.getId() + "'");
		}

		// Register the managed object to the office
		officeEntry.getBuilder().registerManagedObject(
				externalMoName,
				officeMo.getManagedObjectSource().getManagedObjectSource()
						.getId());
	}

}
