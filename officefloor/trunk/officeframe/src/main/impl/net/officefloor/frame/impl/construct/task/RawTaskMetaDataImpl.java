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
package net.officefloor.frame.impl.construct.task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.duty.TaskDutyAssociationImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.impl.execute.task.TaskMetaDataImpl;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawWorkAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawWorkManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.construct.TaskMetaDataLocator;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw meta-data for a {@link Task}.
 * 
 * @author Daniel
 */
public class RawTaskMetaDataImpl<P, W extends Work, M extends Enum<M>, F extends Enum<F>>
		implements RawTaskMetaDataFactory, RawTaskMetaData<P, W, M, F> {

	/**
	 * Obtains the {@link RawTaskMetaDataFactory}.
	 * 
	 * @return {@link RawTaskMetaDataFactory}.
	 */
	@SuppressWarnings("unchecked")
	public static RawTaskMetaDataFactory getFactory() {
		return new RawTaskMetaDataImpl(null, null, null, null);
	}

	/**
	 * Name of the {@link Task}.
	 */
	private final String taskName;

	/**
	 * {@link TaskConfiguration}.
	 */
	private final TaskConfiguration<P, W, M, F> configuration;

	/**
	 * {@link TaskMetaDataImpl}.
	 */
	private final TaskMetaDataImpl<P, W, M, F> taskMetaData;

	/**
	 * {@link RawWorkMetaData}.
	 */
	private final RawWorkMetaData<W> rawWorkMetaData;

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param configuration
	 *            {@link TaskConfiguration}.
	 * @param taskMetaData
	 *            {@link TaskMetaDataImpl}.
	 * @param rawWorkMetaData
	 *            {@link RawWorkMetaData}.
	 */
	private RawTaskMetaDataImpl(String taskName,
			TaskConfiguration<P, W, M, F> configuration,
			TaskMetaDataImpl<P, W, M, F> taskMetaData,
			RawWorkMetaData<W> rawWorkMetaData) {
		this.taskName = taskName;
		this.configuration = configuration;
		this.taskMetaData = taskMetaData;
		this.rawWorkMetaData = rawWorkMetaData;
	}

	/*
	 * =============== RawTaskMetaDataFactory ==============================
	 */

	@Override
	public <p, w extends Work, m extends Enum<m>, f extends Enum<f>> RawTaskMetaData<p, w, m, f> constructRawTaskMetaData(
			TaskConfiguration<p, w, m, f> configuration,
			OfficeFloorIssues issues, RawWorkMetaData<w> rawWorkMetaData) {

		// Obtain the task name
		String taskName = configuration.getTaskName();
		if (ConstructUtil.isBlank(taskName)) {
			issues.addIssue(AssetType.WORK, rawWorkMetaData.getWorkName(),
					"Task added without name");
			return null; // no task name
		}

		// Obtain the task factory
		TaskFactory<p, w, m, f> taskFactory = configuration.getTaskFactory();
		if (taskFactory == null) {
			issues.addIssue(AssetType.TASK, taskName, "No "
					+ TaskFactory.class.getSimpleName() + " provided");
			return null; // no task factory
		}

		// Obtain the team responsible for the task
		String officeTeamName = configuration.getOfficeTeamName();
		if (ConstructUtil.isBlank(officeTeamName)) {
			issues.addIssue(AssetType.TASK, taskName,
					"No team name provided for task");
			return null; // no team name
		}
		RawOfficeMetaData rawOfficeMetaData = rawWorkMetaData
				.getRawOfficeMetaData();
		Team team = rawOfficeMetaData.getTeams().get(officeTeamName);
		if (team == null) {
			issues.addIssue(AssetType.TASK, taskName, "Unknown team '"
					+ officeTeamName + "' responsible for task");
			return null; // no team
		}

		// Obtain the managed objects used directly by this task
		List<RawWorkManagedObjectMetaData> taskMos = new LinkedList<RawWorkManagedObjectMetaData>();
		for (TaskManagedObjectConfiguration mo : configuration
				.getManagedObjectConfiguration()) {

			// Obtain the work bound managed object name
			String workManagedObjectName = mo.getWorkManagedObjectName();
			if (ConstructUtil.isBlank(workManagedObjectName)) {
				issues
						.addIssue(AssetType.TASK, taskName,
								"No name for managed object at index "
										+ taskMos.size());
				continue; // no managed object name
			}

			// Construct the work bound managed object
			RawWorkManagedObjectMetaData workMo = rawWorkMetaData
					.constructRawWorkManagedObjectMetaData(
							workManagedObjectName, issues);
			if (workMo == null) {
				continue; // no work managed object
			}

			// Add the work managed object
			taskMos.add(workMo);
		}

		// Obtain indexes of all required managed objects
		int[] taskToWorkMoTranslations = new int[taskMos.size()];
		int taskToWorkMoIndex = 0;
		Set<Integer> requiredManagedObjectIndexes = new HashSet<Integer>();
		for (RawWorkManagedObjectMetaData workMo : taskMos) {

			// Obtain the work managed object index
			int workManagedObjectIndex = workMo.getWorkManagedObjectIndex();

			// Specify work managed object index of task translation
			taskToWorkMoTranslations[taskToWorkMoIndex++] = workManagedObjectIndex;

			// Add the index of the work managed object
			requiredManagedObjectIndexes
					.add(new Integer(workManagedObjectIndex));

			// Add the indexes for all dependencies
			for (RawWorkManagedObjectMetaData dependencyMo : workMo
					.getDependencies()) {
				requiredManagedObjectIndexes.add(new Integer(dependencyMo
						.getWorkManagedObjectIndex()));
			}
		}
		Integer[] requiredManagedObjectIntegers = requiredManagedObjectIndexes
				.toArray(new Integer[0]);
		int[] requiredManagedObjects = new int[requiredManagedObjectIntegers.length];
		for (int i = 0; i < requiredManagedObjects.length; i++) {
			requiredManagedObjects[i] = requiredManagedObjectIntegers[i]
					.intValue();
		}
		Arrays.sort(requiredManagedObjects); // deterministic order for testing

		// Obtain the duties for this task
		TaskDutyAssociation<?>[] preTaskDuties = this
				.createTaskDutyAssociations(configuration
						.getPreTaskAdministratorDutyConfiguration(),
						rawWorkMetaData, issues, taskName, true);
		TaskDutyAssociation<?>[] postTaskDuties = this
				.createTaskDutyAssociations(configuration
						.getPostTaskAdministratorDutyConfiguration(),
						rawWorkMetaData, issues, taskName, false);

		// Create the task meta-data
		TaskMetaDataImpl<p, w, m, f> taskMetaData = new TaskMetaDataImpl<p, w, m, f>(
				taskName, taskFactory, team, requiredManagedObjects,
				taskToWorkMoTranslations, preTaskDuties, postTaskDuties);

		// Return the raw task meta-data
		return new RawTaskMetaDataImpl<p, w, m, f>(taskName, configuration,
				taskMetaData, rawWorkMetaData);
	}

	/**
	 * Creates the {@link TaskDutyAssociation} instances.
	 * 
	 * @param configurations
	 *            {@link TaskDutyConfiguration} instances.
	 * @param rawWorkMetaData
	 *            {@link RawWorkMetaData}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param isPreNotPost
	 *            Flag indicating if pre {@link Task}.
	 * @return {@link TaskDutyAssociation} instances.
	 */
	@SuppressWarnings("unchecked")
	private TaskDutyAssociation<?>[] createTaskDutyAssociations(
			TaskDutyConfiguration<?>[] configurations,
			RawWorkMetaData<?> rawWorkMetaData, OfficeFloorIssues issues,
			String taskName, boolean isPreNotPost) {

		// Create the listing of task duty associations
		List<TaskDutyAssociation<?>> taskDuties = new LinkedList<TaskDutyAssociation<?>>();
		for (TaskDutyConfiguration<?> duty : configurations) {

			// Obtain the work bound administrator name
			String workAdminName = duty.getWorkAdministratorName();
			if (ConstructUtil.isBlank(workAdminName)) {
				issues.addIssue(AssetType.TASK, taskName,
						"No administrator name for "
								+ (isPreNotPost ? "pre" : "post")
								+ "-task at index " + taskDuties.size());
				continue; // no administrator name
			}

			// Construct the work bound administrator
			RawWorkAdministratorMetaData workAdmin = rawWorkMetaData
					.constructRawWorkAdministratorMetaData(workAdminName,
							issues);
			if (workAdmin == null) {
				continue; // no work administrator
			}

			// Obtain the duty key
			Enum<?> dutyKey = duty.getDuty();
			if (dutyKey == null) {
				issues.addIssue(AssetType.TASK, taskName, "No duty key for "
						+ (isPreNotPost ? "pre" : "post") + "-task at index "
						+ taskDuties.size());
				continue; // no duty
			}

			// Create and add the task duty association
			TaskDutyAssociation<?> taskDutyAssociation = new TaskDutyAssociationImpl(
					workAdmin.getWorkAdministratorIndex(), dutyKey);
			taskDuties.add(taskDutyAssociation);
		}

		// Return the task duty associations
		return taskDuties.toArray(new TaskDutyAssociation[0]);
	}

	/*
	 * ============== RawTaskMetaData ==================================
	 */

	@Override
	public String getTaskName() {
		return this.taskName;
	}

	@Override
	public RawWorkMetaData<W> getRawWorkMetaData() {
		return this.rawWorkMetaData;
	}

	@Override
	public TaskMetaData<P, W, M, F> getTaskMetaData() {
		return this.taskMetaData;
	}

	@Override
	public void linkTasks(TaskMetaDataLocator genericTaskLocator,
			WorkMetaData<W> workMetaData,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Create the work specific task meta-data locator
		TaskMetaDataLocator taskLocator = genericTaskLocator
				.createWorkSpecificTaskMetaDataLocator(workMetaData);

		// Obtain the work name and create the asset name
		String workName = workMetaData.getWorkName();
		String assetName = workName + "." + this.getTaskName();

		// Obtain the listing of flow meta-data
		FlowConfiguration[] flowConfigurations = this.configuration
				.getFlowConfiguration();
		FlowMetaData<?>[] flowMetaDatas = new FlowMetaData[flowConfigurations.length];
		for (int i = 0; i < flowMetaDatas.length; i++) {
			FlowConfiguration flowConfiguration = flowConfigurations[i];

			// Obtain the task reference
			TaskNodeReference taskNodeReference = flowConfiguration
					.getInitialTask();
			if (taskNodeReference == null) {
				issues.addIssue(AssetType.TASK, this.getTaskName(),
						"No task referenced for flow index " + i);
				continue; // no reference task for flow
			}

			// Obtain the task meta-data
			TaskMetaData<?, ?, ?, ?> taskMetaData = ConstructUtil
					.getTaskMetaData(taskNodeReference, taskLocator, issues,
							AssetType.TASK, this.getTaskName(), "flow index "
									+ i, false);
			if (taskMetaData == null) {
				continue; // no initial task for flow
			}

			// Obtain the flow instigation strategy
			FlowInstigationStrategyEnum instigationStrategy = flowConfiguration
					.getInstigationStrategy();
			if (instigationStrategy == null) {
				issues.addIssue(AssetType.TASK, this.getTaskName(),
						"No instigation strategy provided for flow index " + i);
				continue; // no instigation strategy
			}

			// Provide asset manager for instigation of flow
			AssetManager flowAssetManager = assetManagerFactory
					.createAssetManager(AssetType.TASK, assetName, "Flow" + i,
							issues);

			// Create and add the flow meta-data
			flowMetaDatas[i] = this.newFlowMetaData(instigationStrategy,
					taskMetaData, flowAssetManager);
		}

		// Obtain the next task in flow
		TaskNodeReference nextTaskNodeReference = this.configuration
				.getNextTaskInFlow();
		TaskMetaData<?, ?, ?, ?> nextTaskInFlow = null;
		if (nextTaskNodeReference != null) {
			nextTaskInFlow = ConstructUtil.getTaskMetaData(
					nextTaskNodeReference, taskLocator, issues, AssetType.TASK,
					this.getTaskName(), "next task", false);
		}

		// Create the escalation procedure
		EscalationProcedure officeFloorEscalationProcedure = this.rawWorkMetaData
				.getRawOfficeMetaData().getRawOfficeFloorMetaData()
				.getEscalationProcedure();
		EscalationConfiguration[] escalationConfigurations = this.configuration
				.getEscalations();
		Escalation[] escalations = new Escalation[escalationConfigurations.length];
		for (int i = 0; i < escalations.length; i++) {
			EscalationConfiguration escalationConfiguration = escalationConfigurations[i];

			// Obtain the type of cause
			Class<? extends Throwable> typeOfCause = escalationConfiguration
					.getTypeOfCause();
			if (typeOfCause == null) {
				issues.addIssue(AssetType.TASK, this.getTaskName(),
						"No escalation type for escalation index 0");
				continue; // no escalation type
			}

			// Obtain the escalation handler
			TaskNodeReference escalationReference = escalationConfiguration
					.getTaskNodeReference();
			if (escalationReference == null) {
				issues.addIssue(AssetType.TASK, this.getTaskName(),
						"No task referenced for escalation index 0");
				continue; // no escalation handler referenced
			}
			TaskMetaData<?, ?, ?, ?> escalationTaskMetaData = ConstructUtil
					.getTaskMetaData(escalationReference, taskLocator, issues,
							AssetType.TASK, this.getTaskName(),
							"escalation index " + i, false);
			if (escalationTaskMetaData == null) {
				continue; // no escalation handler
			}

			// Provide asset manager for instigation of escalation
			AssetManager escalationAssetManager = assetManagerFactory
					.createAssetManager(AssetType.TASK, assetName, "Escalation"
							+ i, issues);

			// Create and add the escalation
			escalations[i] = new EscalationImpl(typeOfCause, true, this
					.newFlowMetaData(FlowInstigationStrategyEnum.SEQUENTIAL,
							escalationTaskMetaData, escalationAssetManager));
		}
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(
				officeFloorEscalationProcedure, escalations);

		// Load the remaining state for the task meta-data
		this.taskMetaData.loadRemainingState(workMetaData, flowMetaDatas,
				nextTaskInFlow, escalationProcedure);
	}

	/**
	 * Creates a new {@link FlowMetaData}.
	 * 
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param taskMetaData
	 *            {@link TaskMetaData}.
	 * @param assetManager
	 *            {@link AssetManager}.
	 * @return New {@link FlowMetaData}.
	 */
	private <w extends Work> FlowMetaData<w> newFlowMetaData(
			FlowInstigationStrategyEnum instigationStrategy,
			TaskMetaData<?, w, ?, ?> taskMetaData, AssetManager assetManager) {
		return new FlowMetaDataImpl<w>(FlowInstigationStrategyEnum.SEQUENTIAL,
				taskMetaData, assetManager);
	}

}