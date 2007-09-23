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
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.work.TaskModel;

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
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public void build() throws Exception {

		// Obtain task and its details
		TaskModel task = this.deskTask.getTask();
		TaskFactory taskFactory = task.getTaskFactory();

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
	}
}