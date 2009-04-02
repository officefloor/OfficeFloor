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

import net.officefloor.compile.desk.DeskRepository;
import net.officefloor.compile.impl.desk.DeskRepositoryImpl;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link net.officefloor.model.desk.DeskModel} entry for the
 * {@link net.officefloor.frame.api.manage.Office}.
 * 
 * @author Daniel
 */
public class DeskEntry extends AbstractEntry<Object, DeskModel> {

	/**
	 * Loads the {@link DeskEntry}.
	 * 
	 * @param deskId
	 *            Id of the {@link DeskModel}.
	 * @param deskName
	 *            Name of the {@link DeskModel}.
	 * @param configurationItem
	 *            {@link ConfigurationItem}.
	 * @param roomEntry
	 *            {@link RoomEntry} containing this {@link DeskEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return Loaded {@link DeskEntry}.
	 * @throws Exception
	 *             If fails.
	 */
	public static DeskEntry loadDesk(String deskId, String deskName,
			ConfigurationItem configurationItem, RoomEntry roomEntry,
			OfficeFloorCompilerContext context) throws Exception {

		// Load the desk model
		DeskRepository deskRepository = new DeskRepositoryImpl(new ModelRepositoryImpl());
		DeskModel deskModel = deskRepository.retrieveDesk(configurationItem);

		// Create the desk entry
		DeskEntry deskEntry = new DeskEntry(deskId, deskName, deskModel,
				roomEntry);

		// Load the work
		for (WorkModel deskWork : deskModel.getWorks()) {
			WorkEntry<?> workEntry = WorkEntry.loadWork(deskWork, deskEntry,
					context);
			deskEntry.workMap.put(deskWork, workEntry);
		}

		// Return the desk entry
		return deskEntry;
	}

	/**
	 * Name of the {@link DeskModel}.
	 */
	private final String deskName;

	/**
	 * Parent {@link RoomEntry}.
	 */
	private final RoomEntry parentRoom;

	/**
	 * {@link WorkModel} to {@link DeskEntry} map.
	 */
	protected final ModelEntryMap<WorkModel, WorkEntry<?>> workMap = new ModelEntryMap<WorkModel, WorkEntry<?>>();

	/**
	 * {@link TaskModel} to {@link TaskEntry} map.
	 */
	protected final ModelEntryMap<TaskModel, TaskEntry<?>> taskMap = new ModelEntryMap<TaskModel, TaskEntry<?>>();

	/**
	 * Initiate.
	 * 
	 * @param deskId
	 *            Id of the {@link DeskModel}.
	 * @param Name
	 *            of the {@link DeskModel}.
	 * @param desk
	 *            {@link DeskModel}.
	 * @param parentRoom
	 *            Parent {@link RoomEntry}.
	 */
	public DeskEntry(String deskId, String deskName, DeskModel desk,
			RoomEntry parentRoom) {
		super(deskId, null, desk);
		this.deskName = deskName;
		this.parentRoom = parentRoom;
	}

	/**
	 * Obtains the name of the {@link DeskModel}.
	 * 
	 * @return Name of the {@link DeskModel}.
	 */
	public String getDeskName() {
		return this.deskName;
	}

	/**
	 * Obtains the parent {@link RoomEntry} for this {@link DeskEntry}.
	 * 
	 * @return Parent {@link RoomEntry} for this {@link DeskEntry}.
	 */
	public RoomEntry getParentRoom() {
		return this.parentRoom;
	}

	/**
	 * Obtains the {@link WorkModel} for the name of the
	 * {@link WorkModel}.
	 * 
	 * @param workName
	 *            Name of the {@link WorkModel}.
	 * @return {@link WorkModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public WorkModel getWorkModel(String workName) throws Exception {

		// Obtain the work model
		for (WorkModel workModel : this.getModel().getWorks()) {
			if (workName.equals(workModel.getWorkName())) {
				return workModel;
			}
		}

		// If here not found
		throw new Exception("No work '" + workName + "' on desk "
				+ this.getId());
	}

	/**
	 * Obtains the {@link WorkEntry} for the {@link WorkModel}.
	 * 
	 * @param workModel
	 *            {@link WorkModel}.
	 * @return {@link WorkEntry}.
	 * @throws Exception
	 *             If not found.
	 */
	public WorkEntry<?> getWorkEntry(WorkModel workModel) throws Exception {
		return this.getEntry(workModel, this.workMap, "No work '"
				+ workModel.getWorkName() + "' on desk " + this.getId());
	}

	/**
	 * Obtains the {@link TaskEntry} for the {@link TaskModel}.
	 * 
	 * @param flowItemModel
	 *            {@link TaskModel}.
	 * @return {@link TaskEntry}.
	 * @throws Exception
	 *             If not found.
	 */
	public TaskEntry<?> getTaskEntry(TaskModel flowItemModel)
			throws Exception {
		return this.getEntry(flowItemModel, this.taskMap, "No flow item '"
				+ flowItemModel.getTaskName() + "' on desk " + this.getId());
	}

	/**
	 * Registers the {@link TaskEntry} with this {@link DeskEntry}.
	 * 
	 * @param flowItemModel
	 *            {@link TaskModel}.
	 * @param taskEntry
	 *            {@link TaskEntry}.
	 */
	protected void registerTask(TaskModel flowItemModel,
			TaskEntry<?> taskEntry) {
		this.taskMap.put(flowItemModel, taskEntry);
	}

}
