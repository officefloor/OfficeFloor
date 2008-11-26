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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.DeskWorkToFlowItemModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemModel;
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
	 * Mapping of the
	 * {@link net.officefloor.model.office.ExternalManagedObjectModel} to the
	 * local {@link Work} name of the {@link ManagedObject}.
	 */
	private final Map<net.officefloor.model.office.ExternalManagedObjectModel, String> officeMoToWorkMoName = new HashMap<net.officefloor.model.office.ExternalManagedObjectModel, String>();

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

		// Determine if an initial flow
		FlowItemModel initialFlowItem = null;
		DeskWorkToFlowItemModel initialFlowConnection = deskWork
				.getInitialFlowItem();
		if (initialFlowConnection != null) {
			initialFlowItem = initialFlowConnection.getInitialFlowItem();
		}

		// Load details of work
		this.getBuilder().setWorkFactory(workFactory);
		if (initialFlowItem != null) {
			this.getBuilder().setInitialTask(initialFlowItem.getId());
		}

		// Create the unique set of desk external managed objects of this work
		Map<ExternalManagedObjectModel, ManagedObjectLine<W>> externalManagedObjects = new HashMap<ExternalManagedObjectModel, ManagedObjectLine<W>>();
		for (TaskEntry task : this.tasks) {
			for (DeskTaskObjectModel taskObject : task.getDeskTaskModel()
					.getObjects()) {

				// Do not include parameters
				if (taskObject.getIsParameter()) {
					continue;
				}

				// Create the managed object line
				ManagedObjectLine<W> line = new ManagedObjectLine<W>(
						taskObject, this);

				// Add the line
				externalManagedObjects
						.put(line.deskExternalManagedObject, line);
			}
		}

		// Build the unique set of managed objects for this work.
		// Must be done before tasks as registers work managed object names.
		for (ManagedObjectLine<W> line : externalManagedObjects.values()) {
			this.buildManagedObject(line);
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
	public void buildManagedObject(ManagedObjectLine<W> line) throws Exception {

		// Obtain the name that tasks may use
		String workMoName = line.deskExternalManagedObject.getName();

		// Obtain the name known on the office
		String officeMoName = line.officeExternalManagedObject.getName();

		// Build managed object based on its scope
		String moScope = line.officeExternalManagedObject.getScope();
		if (MANAGED_OBJECT_SCOPE_PROCESS.equals(moScope)) {
			// Register the process managed object to this work
			this.bindProcessBoundManagedObject(workMoName,
					line.officeExternalManagedObject);

		} else if (MANAGED_OBJECT_SCOPE_WORK.equals(moScope)) {
			// Register the work managed object to this work
			this.getBuilder().addWorkManagedObject(workMoName, officeMoName);

		} else {
			throw new Exception("Unknown scope '" + moScope
					+ "' for managed object '" + officeMoName + "' of office '"
					+ line.officeEntry.getId() + "'");
		}

		// Register the office managed object to local work name
		this.officeMoToWorkMoName.put(line.officeExternalManagedObject,
				workMoName);

		// Register the managed object to the office
		line.officeEntry.getBuilder().registerManagedObject(officeMoName,
				line.managedObjectSource.getId());
	}

	/**
	 * <p>
	 * Binds a {@link ProcessState} bound {@link ManagedObject} to this
	 * {@link Work}.
	 * <p>
	 * This is available for additional {@link ProcessState} bound
	 * {@link ManagedObject} instances to be available should additional
	 * functionality such as administration be required on them.
	 * 
	 * @param workManagedObjectName
	 *            Name that the {@link ManagedObject} is accessible by
	 *            {@link Task} instances of the {@link Work}.
	 * @param officeManagedObject
	 *            {@link net.officefloor.model.office.ExternalManagedObjectModel}
	 *            .
	 * @throws Exception
	 *             If fails to bind.
	 */
	public void bindProcessBoundManagedObject(
			String workManagedObjectName,
			net.officefloor.model.office.ExternalManagedObjectModel officeManagedObject)
			throws Exception {

		// Obtain the office managed object name
		String officeManagedObjectName = officeManagedObject.getName();
		String processLinkedName = "p:" + officeManagedObjectName;

		// Obtain the office entry
		OfficeEntry officeEntry = this.getOfficeEntry();

		// Register the process managed object within this office
		officeEntry.getBuilder().addProcessManagedObject(processLinkedName,
				officeManagedObjectName);

		// Register the process managed object to this work
		this.getBuilder().registerProcessManagedObject(workManagedObjectName,
				processLinkedName);
	}

	/**
	 * <p>
	 * Binds a {@link ProcessState} bound {@link ManagedObject} to this
	 * {@link Work} that is not being used by a {@link Task}. Likely will be
	 * involved in other functionality such as administration.
	 * <p>
	 * A {@link Work} name is generated for the {@link ManagedObject} to make it
	 * accessible within the {@link Work}.
	 * 
	 * @param officeManagedObject
	 *            {@link net.officefloor.model.office.ExternalManagedObjectModel}
	 *            .
	 * @return Name of {@link ManagedObject} local to the {@link Work}.
	 * @throws Exception
	 *             If fails to bind {@link ManagedObject}.
	 */
	public String bindProcessBoundManagedObject(
			net.officefloor.model.office.ExternalManagedObjectModel officeManagedObject)
			throws Exception {

		// Obtain local work managed object name
		String workManagedObjectName = this.officeMoToWorkMoName
				.get(officeManagedObject);
		if (workManagedObjectName == null) {
			// Generate name as new managed object
			Collection<String> usedNames = this.officeMoToWorkMoName.values();
			String prefix = "NecessaryProcessManagedObject";
			int suffix = 1;
			workManagedObjectName = prefix;
			while (usedNames.contains(workManagedObjectName)) {
				workManagedObjectName = prefix + String.valueOf(suffix++);
			}
		}

		// Bind the managed object
		this.bindProcessBoundManagedObject(workManagedObjectName,
				officeManagedObject);

		// Return the local work managed object name
		return workManagedObjectName;
	}
}
