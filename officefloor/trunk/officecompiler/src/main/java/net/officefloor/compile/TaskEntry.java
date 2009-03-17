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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.AdministrationLine.ManagedObjectUnderAdministration;
import net.officefloor.compile.work.CompilerAwareTaskFactory;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.work.TaskModel;

/**
 * {@link Task} for the {@link Work}.
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
	 * Indexes of the {@link ManagedObject} instances used by this {@link Task}.
	 */
	private final Map<ExternalManagedObjectModel, Integer> managedObjectIndexes = new HashMap<ExternalManagedObjectModel, Integer>();

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
	 * Obtains the {@link WorkEntry}.
	 * 
	 * @return {@link WorkEntry}.
	 */
	public WorkEntry<W> getWorkEntry() {
		return this.workEntry;
	}

	/**
	 * Builds the {@link Task}.
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

		// Create the line for the task
		TaskLine<W> line = new TaskLine<W>(this.getModel(), this);

		// Set team for flow item
		String teamName = line.officeExternalTeam.getName();
		this.getBuilder().setTeam(teamName);

		// Link team into office
		String teamId = line.team.getId();
		line.officeEntry.getBuilder().registerTeam(teamName, teamId);

		// Link in the managed objects.
		// Must be done before duties as populates the initial listing of
		// managed object indexes.
		this.linkManagedObjects();

		// Link in the flows
		this.linkFlows();

		// Link in the next flow
		this.linkNextFlow();

		// Link in the escalations
		this.linkEscalations();

		// Link the duties
		this.linkDuties(line);
	}

	/**
	 * Links in the {@link ManagedObject} instances.
	 * 
	 * @throws BuildException
	 *             If fails to link in the {@link ManagedObject} instances.
	 */
	private void linkManagedObjects() throws BuildException {
		// Bind the task objects in order
		for (DeskTaskObjectModel taskObject : this.deskTask.getObjects()) {

			// Do not include parameters
			if (taskObject.getIsParameter()) {
				continue;
			}

			// Obtain the work managed object
			ExternalManagedObjectModel workManagedObject = taskObject
					.getManagedObject().getManagedObject();

			// Link in the managed object
			this.linkManagedObject(workManagedObject);
		}
	}

	/**
	 * Links the {@link ManagedObject} to be available for this {@link Task}.
	 * 
	 * @param workManagedObject
	 *            {@ink ExternalManagedObjectModel}.
	 * @throws BuildException
	 *             If fails to link {@link ManagedObject} to this {@link Task}.
	 */
	private void linkManagedObject(ExternalManagedObjectModel workManagedObject)
			throws BuildException {

		// Determine if already linked
		if (this.managedObjectIndexes.containsKey(workManagedObject)) {
			// Already linked
			return;
		}

		// Not linked, so determine index and link
		int index = this.managedObjectIndexes.size();
		this.managedObjectIndexes.put(workManagedObject, new Integer(index));

		// Link in the managed object
		String workManagedObjectName = workManagedObject.getName();
		this.getBuilder().linkManagedObject(index, workManagedObjectName);
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

			// Create the flow output line
			FlowOutputLine line = new FlowOutputLine(flowItemOutput, this);

			// Obtain the work and task names of linked flow
			String workName = line.targetWorkEntry.getCanonicalWorkName();
			String taskName = line.targetFlowItem.getId();

			// Link in the output flow item
			if (line.isSameWork()) {
				// Same work
				this.getBuilder().linkFlow(flowIndex, taskName,
						line.flowInstigationStrategy);
			} else {
				// Different work
				this.getBuilder().linkFlow(flowIndex, workName, taskName,
						line.flowInstigationStrategy);
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

		// Create the line for next flow item
		FlowNextLine line = new FlowNextLine(this.getModel(), this);

		// Determine if next flow item
		if (line.targetFlowItem == null) {
			// No next flow item
			return;
		}

		// Obtain the work and task names of linked flow
		String workName = line.targetWorkEntry.getCanonicalWorkName();
		String taskName = line.targetFlowItem.getId();

		// Link in the output flow item
		if (line.isSameWork()) {
			// Same work
			this.getBuilder().setNextTaskInFlow(taskName);
		} else {
			// Different work
			this.getBuilder().setNextTaskInFlow(workName, taskName);
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

			// Obtain the escalation type
			String escalationTypeName = flowItemEscalation.getEscalationType();
			Class<? extends Throwable> escalationType = this.loaderContext
					.obtainClass(escalationTypeName, Throwable.class);

			// Create the line for escalation
			FlowEscalationLine line = new FlowEscalationLine(
					flowItemEscalation, this);

			// Link in the escalation (if specific escalation handler)
			if (!line.isHandledByTopLevelEscalation()) {

				// Obtain the work and task names handling escalation
				String workName = line.targetWorkEntry.getCanonicalWorkName();
				String taskName = line.targetFlowItem.getId();

				// Link in escalation handler
				if (line.isSameWork()) {
					// Same work
					this.getBuilder().addEscalation(escalationType, true,
							taskName);
				} else {
					// Different work
					this.getBuilder().addEscalation(escalationType, true,
							workName, taskName);
				}
			}
		}
	}

	/**
	 * Links in the {@link Duty} instances to this {@link Task}.
	 * 
	 * @param taskLine
	 *            {@link TaskLine} for this {@link Task}.
	 * @throws Exception
	 *             If fails to link in {@link Duty} instances.
	 */
	@SuppressWarnings("unchecked")
	private void linkDuties(TaskLine<W> taskLine) throws Exception {

		// Create the administration lines
		AdministrationLine<W>[] adminLines = AdministrationLine
				.createAdministrationLines(taskLine);

		// Build the administration
		for (AdministrationLine<W> adminLine : adminLines) {

			// Link the administrator to work
			String administratorId = adminLine.administrator.getId();
			String workAdministratorName = "work:" + administratorId;
			AdministrationBuilder adminBuilder = this.workEntry.getBuilder()
					.registerAdministration(workAdministratorName,
							administratorId);

			// Create the listing of work managed objects to administer
			List<String> workManagedObjectNames = new LinkedList<String>();
			for (ManagedObjectUnderAdministration<W> managedObject : adminLine.managedObjects) {

				// Obtain the work managed object name
				String workManagedObjectName;
				if (managedObject.deskTaskObject != null) {
					// Already being used by task
					workManagedObjectName = managedObject.deskManagedObject
							.getName();

				} else if (managedObject.deskManagedObject != null) {
					// Linked to work but not task
					workManagedObjectName = managedObject.deskManagedObject
							.getName();

					// Link managed object to this task
					this.linkManagedObject(managedObject.deskManagedObject);

				} else {
					// Work not using managed object. Include if process bound
					workManagedObjectName = taskLine.workEntry
							.bindProcessBoundManagedObject(managedObject.officeManagedObject);
				}

				// Add the work managed object name for administration
				workManagedObjectNames.add(workManagedObjectName);
			}

			// Link in the managed objects under administration
			adminBuilder.setManagedObjects(workManagedObjectNames
					.toArray(new String[0]));

			// Obtain the duty key
			Enum dutyKey = adminLine.dutyKey;

			// Link in as pre/post duty
			if (adminLine.isPreNotPost) {
				this.getBuilder().linkPreTaskAdministration(
						workAdministratorName, dutyKey);
			} else {
				this.getBuilder().linkPostTaskAdministration(
						workAdministratorName, dutyKey);
			}
		}
	}

}
