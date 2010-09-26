/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.officefloor.frame.impl.construct.task;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.duty.TaskDutyAssociationImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationFlowImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.impl.execute.task.TaskJob;
import net.officefloor.frame.impl.execute.task.TaskMetaDataImpl;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskEscalationConfiguration;
import net.officefloor.frame.internal.configuration.TaskFlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.TaskObjectConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw meta-data for a {@link Task}.
 * 
 * @author Daniel Sagenschneider
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
			final RawWorkMetaData<w> rawWorkMetaData) {

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

		// Keep track of all the required managed objects
		final Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects = new HashMap<ManagedObjectIndex, RawBoundManagedObjectMetaData>();

		// Obtain the managed objects used directly by this task.
		// Also obtain the parameter type for the task if specified.
		TaskObjectConfiguration<d>[] objectConfigurations = configuration
				.getObjectConfiguration();
		ManagedObjectIndex[] taskToWorkMoTranslations = new ManagedObjectIndex[objectConfigurations.length];
		Class<?> parameterType = null;
		NEXT_OBJECT: for (int i = 0; i < objectConfigurations.length; i++) {
			TaskObjectConfiguration<d> objectConfiguration = objectConfigurations[i];

			// Ensure have configuration
			if (objectConfiguration == null) {
				issues.addIssue(AssetType.TASK, taskName,
						"No object configuration at index " + i);
				continue NEXT_OBJECT; // must have configuration
			}

			// Obtain the type of object required
			Class<?> objectType = objectConfiguration.getObjectType();
			if (objectType == null) {
				issues.addIssue(AssetType.TASK, taskName,
						"No type for object at index " + i);
				continue NEXT_OBJECT; // must have object type
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
				continue NEXT_OBJECT;
			}

			// Obtain the scope managed object name
			String scopeMoName = objectConfiguration
					.getScopeManagedObjectName();
			if (ConstructUtil.isBlank(scopeMoName)) {
				issues.addIssue(AssetType.TASK, taskName,
						"No name for managed object at index " + i);
				continue NEXT_OBJECT; // no managed object name
			}

			// Obtain the scope managed object
			RawBoundManagedObjectMetaData scopeMo = rawWorkMetaData
					.getScopeManagedObjectMetaData(scopeMoName);
			if (scopeMo == null) {
				issues.addIssue(AssetType.TASK, taskName,
						"Can not find scope managed object '" + scopeMoName
								+ "'");
				continue NEXT_OBJECT; // no scope managed object
			}

			// Ensure the objects of all the managed objects are compatible
			boolean isCompatibleIssue = false;
			for (RawBoundManagedObjectInstanceMetaData<?> scopeMoInstance : scopeMo
					.getRawBoundManagedObjectInstanceMetaData()) {
				Class<?> moObjectType = scopeMoInstance
						.getRawManagedObjectMetaData().getObjectType();
				if (!objectType.isAssignableFrom(moObjectType)) {
					// Incompatible managed object
					isCompatibleIssue = true;
					issues.addIssue(AssetType.TASK, taskName, "Managed object "
							+ scopeMoName
							+ " is incompatible (require="
							+ objectType.getName()
							+ ", object of managed object type="
							+ moObjectType.getName()
							+ ", ManagedObjectSource="
							+ scopeMoInstance.getRawManagedObjectMetaData()
									.getManagedObjectName() + ")");
				}
			}
			if (isCompatibleIssue) {
				// Incompatible managed object
				continue NEXT_OBJECT;
			}

			// Specify index for task translation and load required indexes
			taskToWorkMoTranslations[i] = this.loadRequiredManagedObjects(
					scopeMo, requiredManagedObjects);
		}

		// Obtain the duties for this task
		TaskDutyAssociation<?>[] preTaskDuties = this
				.createTaskDutyAssociations(configuration
						.getPreTaskAdministratorDutyConfiguration(),
						rawWorkMetaData, issues, taskName, true,
						requiredManagedObjects);
		TaskDutyAssociation<?>[] postTaskDuties = this
				.createTaskDutyAssociations(configuration
						.getPostTaskAdministratorDutyConfiguration(),
						rawWorkMetaData, issues, taskName, false,
						requiredManagedObjects);

		// Create the required managed object indexes
		ManagedObjectIndex[] requiredManagedObjectIndexes = new ManagedObjectIndex[requiredManagedObjects
				.size()];
		int i = 0;
		for (ManagedObjectIndex requiredManagedObjectIndex : requiredManagedObjects
				.keySet()) {
			requiredManagedObjectIndexes[i++] = requiredManagedObjectIndex;
		}

		// Sort the required managed objects
		if (!this.sortRequiredManagedObjects(requiredManagedObjectIndexes,
				requiredManagedObjects, taskName, issues)) {
			// Must be able to sort to allow coordination
			return null;
		}

		// Create the task meta-data
		TaskMetaDataImpl<w, d, f> taskMetaData = new TaskMetaDataImpl<w, d, f>(
				taskName, taskFactory, parameterType, team,
				requiredManagedObjectIndexes, taskToWorkMoTranslations,
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
	 * @param requiredManagedObjects
	 *            Mapping of the required {@link ManagedObjectIndex} instances
	 *            by the {@link Task} to their respective
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @return {@link ManagedObjectIndex} of the input
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	private ManagedObjectIndex loadRequiredManagedObjects(
			RawBoundManagedObjectMetaData boundMo,
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects) {

		// Obtain the bound managed object index
		ManagedObjectIndex boundMoIndex = boundMo.getManagedObjectIndex();
		if (!requiredManagedObjects.containsKey(boundMoIndex)) {

			// Not yet required, so add and include all its dependencies
			requiredManagedObjects.put(boundMoIndex, boundMo);
			for (RawBoundManagedObjectInstanceMetaData<?> boundMoInstance : boundMo
					.getRawBoundManagedObjectInstanceMetaData()) {
				RawBoundManagedObjectMetaData[] dependencies = boundMoInstance
						.getDependencies();
				if (dependencies != null) {
					for (RawBoundManagedObjectMetaData dependency : dependencies) {
						this.loadRequiredManagedObjects(dependency,
								requiredManagedObjects);
					}
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
	 * @param requiredManagedObjects
	 *            Mapping of the required {@link ManagedObjectIndex} instances
	 *            by the {@link Task} to their respective
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @return {@link TaskDutyAssociation} instances.
	 */
	@SuppressWarnings("unchecked")
	private TaskDutyAssociation<?>[] createTaskDutyAssociations(
			TaskDutyConfiguration<?>[] configurations,
			RawWorkMetaData<?> rawWorkMetaData,
			OfficeFloorIssues issues,
			String taskName,
			boolean isPreNotPost,
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects) {

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
			DutyKey<?> dutyKey;
			Enum<?> key = duty.getDutyKey();
			if (key != null) {
				dutyKey = scopeAdmin.getDutyKey(key);
				if (dutyKey == null) {
					// Must have duty key
					issues.addIssue(AssetType.TASK, taskName, "No duty for "
							+ (isPreNotPost ? "pre" : "post")
							+ "-task at index " + taskDuties.size()
							+ " (duty key=" + key + ")");
					continue; // no duty
				}
			} else {
				// Ensure have duty name
				String dutyName = duty.getDutyName();
				if (ConstructUtil.isBlank(dutyName)) {
					issues.addIssue(AssetType.TASK, taskName,
							"No duty name/key for pre-task at index "
									+ taskDuties.size());
					continue; // must have means to identify duty
				}
				dutyKey = scopeAdmin.getDutyKey(dutyName);
				if (dutyKey == null) {
					// Must have duty key
					issues.addIssue(AssetType.TASK, taskName, "No duty for "
							+ (isPreNotPost ? "pre" : "post")
							+ "-task at index " + taskDuties.size()
							+ " (duty name=" + dutyName + ")");
					continue; // no duty
				}
			}

			// Load the required managed object indexes for the administrator
			for (RawBoundManagedObjectMetaData administeredManagedObject : scopeAdmin
					.getAdministeredRawBoundManagedObjects()) {
				this.loadRequiredManagedObjects(administeredManagedObject,
						requiredManagedObjects);
			}

			// Create and add the task duty association
			TaskDutyAssociation<?> taskDutyAssociation = new TaskDutyAssociationImpl(
					adminIndex, dutyKey);
			taskDuties.add(taskDutyAssociation);
		}

		// Return the task duty associations
		return taskDuties.toArray(new TaskDutyAssociation[0]);
	}

	/**
	 * <p>
	 * Sorts the required {@link ManagedObjectIndex} instances for the
	 * {@link Task} so that dependency {@link ManagedObject} instances are
	 * before the {@link ManagedObject} instances using them. In essence this is
	 * a topological sort so that dependencies are first.
	 * <p>
	 * This is necessary for coordinating so that dependencies are coordinated
	 * before the {@link ManagedObject} instances using them are coordinated.
	 * 
	 * @param requiredManagedObjectIndexes
	 *            Listing of required {@link ManagedObject} instances to be
	 *            sorted.
	 * @param requiredManagedObjects
	 *            Mapping of the {@link ManagedObjectIndex} to its
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param taskName
	 *            Name of {@link Task} to issues.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return <code>true</code> indicating that able to sort.
	 *         <code>false</code> indicates unable to sort, possible because of
	 *         cyclic dependencies.
	 */
	private boolean sortRequiredManagedObjects(
			ManagedObjectIndex[] requiredManagedObjectIndexes,
			final Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects,
			String taskName, OfficeFloorIssues issues) {

		// Initially sort by scope and index
		Arrays.sort(requiredManagedObjectIndexes,
				new Comparator<ManagedObjectIndex>() {
					@Override
					public int compare(ManagedObjectIndex a,
							ManagedObjectIndex b) {
						int value = a.getManagedObjectScope().ordinal()
								- b.getManagedObjectScope().ordinal();
						if (value == 0) {
							value = a.getIndexOfManagedObjectWithinScope()
									- b.getIndexOfManagedObjectWithinScope();
						}
						return value;
					}
				});

		// Create the set of dependencies for each required managed object
		final Map<ManagedObjectIndex, Set<ManagedObjectIndex>> dependencies = new HashMap<ManagedObjectIndex, Set<ManagedObjectIndex>>();
		for (ManagedObjectIndex index : requiredManagedObjectIndexes) {

			// Obtain the managed object for index
			RawBoundManagedObjectMetaData managedObject = requiredManagedObjects
					.get(index);

			// Load the dependencies
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> moDependencies = new HashMap<ManagedObjectIndex, RawBoundManagedObjectMetaData>();
			RawTaskMetaDataImpl.this.loadRequiredManagedObjects(managedObject,
					moDependencies);

			// Register the dependencies for the index
			dependencies.put(index, new HashSet<ManagedObjectIndex>(
					moDependencies.keySet()));
		}

		try {
			// Sort so dependencies are first (detecting cyclic dependencies)
			Arrays.sort(requiredManagedObjectIndexes,
					new Comparator<ManagedObjectIndex>() {
						@Override
						public int compare(ManagedObjectIndex a,
								ManagedObjectIndex b) {

							// Obtain the dependencies
							Set<ManagedObjectIndex> aDep = dependencies.get(a);
							Set<ManagedObjectIndex> bDep = dependencies.get(b);

							// Determine dependency relationship
							boolean isAdepB = bDep.contains(a);
							boolean isBdepA = aDep.contains(b);

							// Compare based on relationship
							if (isAdepB && isBdepA) {
								// Cyclic dependency
								String[] names = new String[2];
								names[0] = requiredManagedObjects.get(a)
										.getBoundManagedObjectName();
								names[1] = requiredManagedObjects.get(b)
										.getBoundManagedObjectName();
								Arrays.sort(names);
								throw new CyclicDependencyException(
										"Can not have cyclic dependencies ("
												+ names[0] + ", " + names[1]
												+ ")");
							} else if (isAdepB) {
								// A dependent on B, so B must come first
								return -1;
							} else if (isBdepA) {
								// B dependent on A, so A must come first
								return 1;
							} else {
								/*
								 * No dependency relationship. As the sorting
								 * only changes on differences (non 0 value)
								 * then need means to differentiate when no
								 * dependency relationship.
								 */

								// Least number of dependencies first
								int value = aDep.size() - bDep.size();
								if (value == 0) {
									// Same dependencies, so base on scope
									value = a.getManagedObjectScope().ordinal()
											- b.getManagedObjectScope()
													.ordinal();
									if (value == 0) {
										// Same scope, so arbitrary order
										value = a
												.getIndexOfManagedObjectWithinScope()
												- b
														.getIndexOfManagedObjectWithinScope();
									}
								}
								return value;
							}
						}
					});

		} catch (CyclicDependencyException ex) {
			// Register issue that cyclic dependency
			issues.addIssue(AssetType.TASK, taskName, ex.getMessage());

			// Not sorted as cyclic dependency
			return false;
		}

		// As here must be sorted
		return true;
	}

	/**
	 * Thrown to indicate a cyclic dependency.
	 */
	private static class CyclicDependencyException extends RuntimeException {

		/**
		 * Initiate.
		 * 
		 * @param message
		 *            Initiate with description for {@link OfficeFloorIssues}.
		 */
		public CyclicDependencyException(String message) {
			super(message);
		}
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
		TaskFlowConfiguration<F>[] flowConfigurations = this.configuration
				.getFlowConfiguration();
		FlowMetaData<?>[] flowMetaDatas = new FlowMetaData[flowConfigurations.length];
		for (int i = 0; i < flowMetaDatas.length; i++) {
			TaskFlowConfiguration<F> flowConfiguration = flowConfigurations[i];

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
		TaskEscalationConfiguration[] escalationConfigurations = this.configuration
				.getEscalations();
		EscalationFlow[] escalations = new EscalationFlow[escalationConfigurations.length];
		for (int i = 0; i < escalations.length; i++) {
			TaskEscalationConfiguration escalationConfiguration = escalationConfigurations[i];

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
			escalations[i] = new EscalationFlowImpl(typeOfCause,
					escalationFlowMetaData);
		}
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(
				escalations);

		// Load the remaining state for the task meta-data
		this.taskMetaData.loadRemainingState(workMetaData, flowMetaDatas,
				nextTaskInFlow, escalationProcedure);
	}

}