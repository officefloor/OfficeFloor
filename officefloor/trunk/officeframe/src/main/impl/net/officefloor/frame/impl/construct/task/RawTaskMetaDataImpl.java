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
import java.util.Comparator;
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
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.impl.execute.task.TaskJob;
import net.officefloor.frame.impl.execute.task.TaskMetaDataImpl;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.TaskObjectConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw meta-data for a {@link Task}.
 * 
 * @author Daniel
 */
public class RawTaskMetaDataImpl<W extends Work, D extends Enum<D>, F extends Enum<F>>
		implements RawTaskMetaDataFactory, RawTaskMetaData<W, D, F> {

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
	private final TaskConfiguration<W, D, F> configuration;

	/**
	 * {@link TaskMetaDataImpl}.
	 */
	private final TaskMetaDataImpl<W, D, F> taskMetaData;

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
			TaskConfiguration<W, D, F> configuration,
			TaskMetaDataImpl<W, D, F> taskMetaData,
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
	public <w extends Work, d extends Enum<d>, f extends Enum<f>> RawTaskMetaData<w, d, f> constructRawTaskMetaData(
			TaskConfiguration<w, d, f> configuration, OfficeFloorIssues issues,
			RawWorkMetaData<w> rawWorkMetaData) {

		// Obtain the task name
		String taskName = configuration.getTaskName();
		if (ConstructUtil.isBlank(taskName)) {
			issues.addIssue(AssetType.WORK, rawWorkMetaData.getWorkName(),
					"Task added without name");
			return null; // no task name
		}

		// Obtain the task factory
		TaskFactory<w, d, f> taskFactory = configuration.getTaskFactory();
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

		// Keep track of all the required managed object indexes
		Set<ManagedObjectIndex> requiredManagedObjectIndexes = new HashSet<ManagedObjectIndex>();

		// Obtain the managed objects used directly by this task.
		// Also obtain the parameter type for the task if specified.
		TaskObjectConfiguration[] objectConfigurations = configuration
				.getObjectConfiguration();
		ManagedObjectIndex[] taskToWorkMoTranslations = new ManagedObjectIndex[objectConfigurations.length];
		Class<?> parameterType = null;
		for (int i = 0; i < objectConfigurations.length; i++) {
			TaskObjectConfiguration objectConfiguration = objectConfigurations[i];

			// Obtain the type of object required
			Class<?> objectType = objectConfiguration.getObjectType();
			if (objectType == null) {
				issues.addIssue(AssetType.TASK, taskName,
						"No type for object at index " + i);
				continue; // must have object type
			}

			// Determine if a parameter
			if (objectConfiguration.isParameter()) {
				// Parameter so use parameter index (note has no scope)
				taskToWorkMoTranslations[i] = new ManagedObjectIndexImpl(null,
						TaskJob.PARAMETER_INDEX);

				// Specify the parameter type
				if (parameterType == null) {
					// Specify as not yet set
					parameterType = objectType;

				} else {
					// Parameter already used, so use most specific type
					if (parameterType.isAssignableFrom(objectType)) {
						// Just linked object is more specific type
						parameterType = objectType;
					} else if (objectType.isAssignableFrom(parameterType)) {
						// Existing parameter type is more specific
					} else {
						// Parameter use is incompatible
						issues.addIssue(AssetType.TASK, taskName,
								"Incompatible parameter types ("
										+ parameterType.getName() + ", "
										+ objectType.getName() + ")");
					}
				}

				// Specified as parameter
				continue;
			}

			// Obtain the scope managed object name
			String scopeMoName = objectConfiguration
					.getScopeManagedObjectName();
			if (ConstructUtil.isBlank(scopeMoName)) {
				issues.addIssue(AssetType.TASK, taskName,
						"No name for managed object at index " + i);
				continue; // no managed object name
			}

			// Obtain the scope managed object
			RawBoundManagedObjectMetaData<?> scopeMo = rawWorkMetaData
					.getScopeManagedObjectMetaData(scopeMoName);
			if (scopeMo == null) {
				issues.addIssue(AssetType.TASK, taskName,
						"Can not find scope managed object '" + scopeMoName
								+ "'");
				continue; // no scope managed object
			}

			// Ensure the object from managed object is compatible
			Class<?> moObjectType = scopeMo.getRawManagedObjectMetaData()
					.getObjectType();
			if (!objectType.isAssignableFrom(moObjectType)) {
				issues.addIssue(AssetType.TASK, taskName, "Managed object "
						+ scopeMoName + " is incompatible (require="
						+ objectType.getName()
						+ ", object of managed object type="
						+ moObjectType.getName() + ")");
				continue; // incompatible managed object
			}

			// Specify index for task translation and load required indexes
			taskToWorkMoTranslations[i] = this.loadRequiredManagedObjects(
					scopeMo, requiredManagedObjectIndexes);
		}

		// Obtain the duties for this task
		TaskDutyAssociation<?>[] preTaskDuties = this
				.createTaskDutyAssociations(configuration
						.getPreTaskAdministratorDutyConfiguration(),
						rawWorkMetaData, issues, taskName, true,
						requiredManagedObjectIndexes);
		TaskDutyAssociation<?>[] postTaskDuties = this
				.createTaskDutyAssociations(configuration
						.getPostTaskAdministratorDutyConfiguration(),
						rawWorkMetaData, issues, taskName, false,
						requiredManagedObjectIndexes);

		// Create the listing of required managed object indexes
		ManagedObjectIndex[] requiredManagedObjects = new ManagedObjectIndex[requiredManagedObjectIndexes
				.size()];
		int i = 0;
		for (ManagedObjectIndex requiredManagedObject : requiredManagedObjectIndexes) {
			requiredManagedObjects[i++] = requiredManagedObject;
		}

		// Order to provide work, thread then process managed object loading.
		// This stops deadlock/starvation as retrieval will now be ordered.
		Arrays.sort(requiredManagedObjects,
				new Comparator<ManagedObjectIndex>() {
					@Override
					public int compare(ManagedObjectIndex a,
							ManagedObjectIndex b) {
						int comparison = a.getManagedObjectScope().ordinal()
								- b.getManagedObjectScope().ordinal();
						if (comparison == 0) {
							comparison = a.getIndexOfManagedObjectWithinScope()
									- b.getIndexOfManagedObjectWithinScope();
						}
						return comparison;
					}
				});

		// Create the task meta-data
		TaskMetaDataImpl<w, d, f> taskMetaData = new TaskMetaDataImpl<w, d, f>(
				taskName, taskFactory, parameterType, team,
				requiredManagedObjects, taskToWorkMoTranslations,
				preTaskDuties, postTaskDuties);

		// Return the raw task meta-data
		return new RawTaskMetaDataImpl<w, d, f>(taskName, configuration,
				taskMetaData, rawWorkMetaData);
	}

	/**
	 * Recursively loads all the {@link ManagedObjectIndex} instances for the
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param boundMo
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param requiredManagedObjectIndexes
	 *            {@link Set} maintaining the unique {@link ManagedObjectIndex}
	 *            instances required by the {@link Task}.
	 * @return {@link ManagedObjectIndex} of the input
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	private <d extends Enum<d>> ManagedObjectIndex loadRequiredManagedObjects(
			RawBoundManagedObjectMetaData<d> boundMo,
			Set<ManagedObjectIndex> requiredManagedObjectIndexes) {

		// Obtain the bound managed object index
		ManagedObjectIndex boundMoIndex = boundMo.getManagedObjectIndex();
		if (!requiredManagedObjectIndexes.contains(boundMoIndex)) {

			// Not yet required, so add and include all its dependencies
			requiredManagedObjectIndexes.add(boundMoIndex);
			RawBoundManagedObjectMetaData<?>[] dependencies = boundMo
					.getDependencies();
			if (dependencies != null) {
				for (RawBoundManagedObjectMetaData<?> dependency : dependencies) {
					this.loadRequiredManagedObjects(dependency,
							requiredManagedObjectIndexes);
				}
			}
		}

		// Return the managed object index for the bound managed object
		return boundMoIndex;
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
	 * @param requiredManagedObjectIndexes
	 *            {@link Set} maintaining the unique {@link ManagedObjectIndex}
	 *            instances required by the {@link Task}.
	 * @return {@link TaskDutyAssociation} instances.
	 */
	@SuppressWarnings("unchecked")
	private TaskDutyAssociation<?>[] createTaskDutyAssociations(
			TaskDutyConfiguration<?>[] configurations,
			RawWorkMetaData<?> rawWorkMetaData, OfficeFloorIssues issues,
			String taskName, boolean isPreNotPost,
			Set<ManagedObjectIndex> requiredManagedObjectIndexes) {

		// Create the listing of task duty associations
		List<TaskDutyAssociation<?>> taskDuties = new LinkedList<TaskDutyAssociation<?>>();
		for (TaskDutyConfiguration<?> duty : configurations) {

			// Obtain the scope administrator name
			String scopeAdminName = duty.getScopeAdministratorName();
			if (ConstructUtil.isBlank(scopeAdminName)) {
				issues.addIssue(AssetType.TASK, taskName,
						"No administrator name for "
								+ (isPreNotPost ? "pre" : "post")
								+ "-task at index " + taskDuties.size());
				continue; // no administrator name
			}

			// Obtain the scope administrator
			RawBoundAdministratorMetaData<?, ?> scopeAdmin = rawWorkMetaData
					.getScopeAdministratorMetaData(scopeAdminName);
			if (scopeAdmin == null) {
				issues.addIssue(AssetType.TASK, taskName,
						"Can not find scope administrator '" + scopeAdminName
								+ "'");
				continue; // no administrator
			}

			// Obtain the administrator index
			AdministratorIndex adminIndex = scopeAdmin.getAdministratorIndex();

			// Obtain the duty key
			Enum<?> dutyKey = duty.getDuty();
			if (dutyKey == null) {
				issues.addIssue(AssetType.TASK, taskName, "No duty key for "
						+ (isPreNotPost ? "pre" : "post") + "-task at index "
						+ taskDuties.size());
				continue; // no duty
			}

			// Load the required managed object indexes for the administrator
			for (RawBoundManagedObjectMetaData<?> administeredManagedObject : scopeAdmin
					.getAdministeredRawBoundManagedObjects()) {
				this.loadRequiredManagedObjects(administeredManagedObject,
						requiredManagedObjectIndexes);
			}

			// Create and add the task duty association
			TaskDutyAssociation<?> taskDutyAssociation = new TaskDutyAssociationImpl(
					adminIndex, dutyKey);
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
	public TaskMetaData<W, D, F> getTaskMetaData() {
		return this.taskMetaData;
	}

	@Override
	public void linkTasks(OfficeMetaDataLocator genericTaskLocator,
			WorkMetaData<W> workMetaData,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Create the work specific meta-data locator
		OfficeMetaDataLocator taskLocator = genericTaskLocator
				.createWorkSpecificOfficeMetaDataLocator(workMetaData);

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
			TaskMetaData<?, ?, ?> taskMetaData = ConstructUtil.getTaskMetaData(
					taskNodeReference, taskLocator, issues, AssetType.TASK,
					this.getTaskName(), "flow index " + i, false);
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

			// Create and add the flow meta-data
			flowMetaDatas[i] = ConstructUtil.newFlowMetaData(
					instigationStrategy, taskMetaData, assetManagerFactory,
					AssetType.TASK, assetName, "Flow" + i, issues);
		}

		// Obtain the next task in flow
		TaskNodeReference nextTaskNodeReference = this.configuration
				.getNextTaskInFlow();
		TaskMetaData<?, ?, ?> nextTaskInFlow = null;
		if (nextTaskNodeReference != null) {
			nextTaskInFlow = ConstructUtil.getTaskMetaData(
					nextTaskNodeReference, taskLocator, issues, AssetType.TASK,
					this.getTaskName(), "next task", false);
		}

		// Create the escalation procedure
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
			TaskMetaData<?, ?, ?> escalationTaskMetaData = ConstructUtil
					.getTaskMetaData(escalationReference, taskLocator, issues,
							AssetType.TASK, this.getTaskName(),
							"escalation index " + i, false);
			if (escalationTaskMetaData == null) {
				continue; // no escalation handler
			}

			// Create the escalation flow meta-data
			FlowMetaData<?> escalationFlowMetaData = ConstructUtil
					.newFlowMetaData(FlowInstigationStrategyEnum.PARALLEL,
							escalationTaskMetaData, assetManagerFactory,
							AssetType.TASK, assetName, "Escalation" + i, issues);

			// Create and add the escalation
			escalations[i] = new EscalationImpl(typeOfCause,
					escalationFlowMetaData);
		}
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(
				escalations);

		// Load the remaining state for the task meta-data
		this.taskMetaData.loadRemainingState(workMetaData, flowMetaDatas,
				nextTaskInFlow, escalationProcedure);
	}

}