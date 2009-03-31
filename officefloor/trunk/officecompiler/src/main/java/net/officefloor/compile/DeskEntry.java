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

import net.officefloor.compile.desk.DeskLoader;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.FlowItemModel;
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
		DeskModel deskModel = new DeskLoader(context.getLoaderContext(),
				context.getModelRepository())
				.loadDeskAndSynchronise(configurationItem);

		// Create the desk entry
		DeskEntry deskEntry = new DeskEntry(deskId, deskName, deskModel,
				roomEntry);

		// Load the work
		for (DeskWorkModel deskWork : deskModel.getWorks()) {
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
	 * {@link DeskWorkModel} to {@link DeskEntry} map.
	 */
	protected final ModelEntryMap<DeskWorkModel, WorkEntry<?>> workMap = new ModelEntryMap<DeskWorkModel, WorkEntry<?>>();

	/**
	 * {@link FlowItemModel} to {@link TaskEntry} map.
	 */
	protected final ModelEntryMap<FlowItemModel, TaskEntry<?>> taskMap = new ModelEntryMap<FlowItemModel, TaskEntry<?>>();

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
	 * Obtains the {@link DeskWorkModel} for the name of the
	 * {@link DeskWorkModel}.
	 * 
	 * @param workName
	 *            Name of the {@link DeskWorkModel}.
	 * @return {@link DeskWorkModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public DeskWorkModel getWorkModel(String workName) throws Exception {

		// Obtain the work model
		for (DeskWorkModel workModel : this.getModel().getWorks()) {
			if (workName.equals(workModel.getId())) {
				return workModel;
			}
		}

		// If here not found
		throw new Exception("No work '" + workName + "' on desk "
				+ this.getId());
	}

	/**
	 * Obtains the {@link WorkEntry} for the {@link DeskWorkModel}.
	 * 
	 * @param workModel
	 *            {@link DeskWorkModel}.
	 * @return {@link WorkEntry}.
	 * @throws Exception
	 *             If not found.
	 */
	public WorkEntry<?> getWorkEntry(DeskWorkModel workModel) throws Exception {
		return this.getEntry(workModel, this.workMap, "No work '"
				+ workModel.getId() + "' on desk " + this.getId());
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
		return this.getEntry(flowItemModel, this.taskMap, "No flow item '"
				+ flowItemModel.getId() + "' on desk " + this.getId());
	}

	/**
	 * Registers the {@link TaskEntry} with this {@link DeskEntry}.
	 * 
	 * @param flowItemModel
	 *            {@link FlowItemModel}.
	 * @param taskEntry
	 *            {@link TaskEntry}.
	 */
	protected void registerTask(FlowItemModel flowItemModel,
			TaskEntry<?> taskEntry) {
		this.taskMap.put(flowItemModel, taskEntry);
	}

}
