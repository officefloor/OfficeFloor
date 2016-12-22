/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.model.impl.desk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.DeskManagedObjectDependencyModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyToDeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToTaskModel;
import net.officefloor.model.desk.DeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskManagedObjectToDeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToTaskModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.TaskToNextExternalFlowModel;
import net.officefloor.model.desk.TaskToNextTaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToDeskManagedObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.DisconnectChange;
import net.officefloor.model.impl.change.NoChange;

/**
 * {@link DeskChanges} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class DeskChangesImpl implements DeskChanges {

	/**
	 * Obtains the link type name for the {@link FlowInstigationStrategyEnum}.
	 *
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @return Link type name for the {@link FlowInstigationStrategyEnum}.
	 */
	public static String getFlowInstigationStrategyLink(
			FlowInstigationStrategyEnum instigationStrategy) {

		// Ensure have instigation strategy
		if (instigationStrategy == null) {
			return null;
		}

		// Return instigation strategy link type
		switch (instigationStrategy) {
		case SEQUENTIAL:
			return DeskChanges.SEQUENTIAL_LINK;
		case PARALLEL:
			return DeskChanges.PARALLEL_LINK;
		case ASYNCHRONOUS:
			return DeskChanges.ASYNCHRONOUS_LINK;
		default:
			throw new IllegalStateException("Unknown instigation strategy "
					+ instigationStrategy);
		}
	}

	/**
	 * <p>
	 * Sorts the {@link WorkModel} instances.
	 * <p>
	 * This enable easier merging of configuration under SCM.
	 *
	 * @param workModels
	 *            {@link WorkTaskModel} instances.
	 */
	public static void sortWorkModels(List<WorkModel> workModels) {
		Collections.sort(workModels, new Comparator<WorkModel>() {
			@Override
			public int compare(WorkModel a, WorkModel b) {
				return a.getWorkName().compareTo(b.getWorkName());
			}
		});
	}

	/**
	 * <p>
	 * Sorts the {@link WorkTaskModel} instances.
	 * <p>
	 * This enables easier merging of configuration under SCM.
	 *
	 * @param workTaskModels
	 *            {@link WorkTaskModel} instances.
	 */
	public static void sortWorkTaskModels(List<WorkTaskModel> workTaskModels) {
		Collections.sort(workTaskModels, new Comparator<WorkTaskModel>() {
			@Override
			public int compare(WorkTaskModel a, WorkTaskModel b) {
				return a.getWorkTaskName().compareTo(b.getWorkTaskName());
			}
		});
	}

	/**
	 * Sorts the {@link WorkTaskToTaskModel} connections.
	 *
	 * @param workTaskToTaskConnections
	 *            {@link WorkTaskToTaskModel} instances.
	 */
	public static void sortWorkTaskToTaskConnections(
			List<WorkTaskToTaskModel> workTaskToTaskConnections) {
		Collections.sort(workTaskToTaskConnections,
				new Comparator<WorkTaskToTaskModel>() {
					@Override
					public int compare(WorkTaskToTaskModel a,
							WorkTaskToTaskModel b) {
						return a.getTask().getTaskName().compareTo(
								b.getTask().getTaskName());
					}
				});
	}

	/**
	 * <p>
	 * Sorts the {@link TaskModel} instances.
	 * <p>
	 * This enable easier merging of configuration under SCM.
	 *
	 * @param taskModels
	 *            {@link TaskModel} instances.
	 */
	public static void sortTaskModels(List<TaskModel> taskModels) {
		Collections.sort(taskModels, new Comparator<TaskModel>() {
			@Override
			public int compare(TaskModel a, TaskModel b) {
				return a.getTaskName().compareTo(b.getTaskName());
			}
		});
	}

	/**
	 * <p>
	 * Sorts the {@link ExternalFlowModel} instances.
	 * <p>
	 * This enables easier merging of configuration under SCM.
	 *
	 * @param externalFlows
	 *            {@link ExternalFlowModel} instances.
	 */
	public static void sortExternalFlows(List<ExternalFlowModel> externalFlows) {
		Collections.sort(externalFlows, new Comparator<ExternalFlowModel>() {
			@Override
			public int compare(ExternalFlowModel a, ExternalFlowModel b) {
				return a.getExternalFlowName().compareTo(
						b.getExternalFlowName());
			}
		});
	}

	/**
	 * <p>
	 * Sorts the {@link ExternalManagedObjectModel} instances.
	 * <p>
	 * This enables easier merging of configuration under SCM.
	 *
	 * @param externalManagedObjects
	 *            {@link ExternalManagedObjectModel} instances.
	 */
	public static void sortExternalManagedObjects(
			List<ExternalManagedObjectModel> externalManagedObjects) {
		Collections.sort(externalManagedObjects,
				new Comparator<ExternalManagedObjectModel>() {
					@Override
					public int compare(ExternalManagedObjectModel a,
							ExternalManagedObjectModel b) {
						return a.getExternalManagedObjectName().compareTo(
								b.getExternalManagedObjectName());
					}
				});
	}

	/**
	 * Obtains the text name identifying the {@link ManagedObjectScope}.
	 *
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 * @return Text name for the {@link ManagedObjectScope}.
	 */
	public static String getManagedObjectScope(ManagedObjectScope scope) {

		// Ensure have scope
		if (scope == null) {
			return null;
		}

		// Return the text of the scope
		switch (scope) {
		case PROCESS:
			return PROCESS_MANAGED_OBJECT_SCOPE;
		case THREAD:
			return THREAD_MANAGED_OBJECT_SCOPE;
		case WORK:
			return WORK_MANAGED_OBJECT_SCOPE;
		default:
			throw new IllegalStateException("Unknown scope " + scope);
		}
	}

	/**
	 * {@link DeskModel}.
	 */
	private final DeskModel desk;

	/**
	 * Initiate.
	 *
	 * @param desk
	 *            {@link DeskModel}.
	 */
	public DeskChangesImpl(DeskModel desk) {
		this.desk = desk;
	}

	/**
	 * Sorts the {@link WorkModel} instances.
	 */
	protected void sortWorkModels() {
		sortWorkModels(this.desk.getWorks());
	}

	/**
	 * Sorts the {@link TaskModel} instances.
	 */
	protected void sortTaskModels() {
		sortTaskModels(this.desk.getTasks());
	}

	/**
	 * Sorts the {@link ExternalFlowModel} instances.
	 */
	protected void sortExternalFlows() {
		sortExternalFlows(this.desk.getExternalFlows());
	}

	/**
	 * Sorts the {@link ExternalManagedObjectModel} instances.
	 */
	protected void sortExternalManagedObjects() {
		sortExternalManagedObjects(this.desk.getExternalManagedObjects());
	}

	/**
	 * Creates a {@link WorkTaskModel} for a {@link ManagedFunctionType}.
	 *
	 * @param taskType
	 *            {@link ManagedFunctionType}.
	 * @return {@link WorkTaskModel} for the {@link ManagedFunctionType}.
	 */
	private WorkTaskModel createWorkTaskModel(ManagedFunctionType<?, ?, ?> taskType) {

		// Create the work task model
		WorkTaskModel workTask = new WorkTaskModel(taskType.getFunctionName());

		// Add the task object models
		for (ManagedFunctionObjectType<?> taskObjectType : taskType.getObjectTypes()) {
			Enum<?> key = taskObjectType.getKey();
			WorkTaskObjectModel taskObject = new WorkTaskObjectModel(
					taskObjectType.getObjectName(), (key == null ? null : key
							.name()), taskObjectType.getObjectType().getName(),
					false);
			workTask.addTaskObject(taskObject);
		}

		// Return the work task model
		return workTask;
	}

	/**
	 * Removes the connections to the {@link TaskModel} (except to its
	 * {@link WorkTaskModel}).
	 *
	 * @param task
	 *            {@link TaskModel}.
	 * @param connectionList
	 *            Listing to add removed {@link ConnectionModel} instances.
	 */
	private void removeTaskConnections(TaskModel task,
			List<ConnectionModel> connectionList) {

		// Remove input connections (copy to stop concurrent)
		for (TaskToNextTaskModel conn : new ArrayList<TaskToNextTaskModel>(task
				.getPreviousTasks())) {
			conn.remove();
			connectionList.add(conn);
		}
		for (TaskFlowToTaskModel conn : new ArrayList<TaskFlowToTaskModel>(task
				.getTaskFlowInputs())) {
			conn.remove();
			connectionList.add(conn);
		}
		for (TaskEscalationToTaskModel conn : new ArrayList<TaskEscalationToTaskModel>(
				task.getTaskEscalationInputs())) {
			conn.remove();
			connectionList.add(conn);
		}

		// Remove flow connections
		for (TaskFlowModel flow : task.getTaskFlows()) {
			this.removeTaskFlowConnections(flow, connectionList);
		}

		// Remove next connections
		TaskToNextTaskModel connNextTask = task.getNextTask();
		if (connNextTask != null) {
			connNextTask.remove();
			connectionList.add(connNextTask);
		}
		TaskToNextExternalFlowModel connNextExtFlow = task
				.getNextExternalFlow();
		if (connNextExtFlow != null) {
			connNextExtFlow.remove();
			connectionList.add(connNextExtFlow);
		}

		// Remove escalation connections
		for (TaskEscalationModel escalation : task.getTaskEscalations()) {
			this.removeTaskEscalationConnections(escalation, connectionList);
		}
	}

	/**
	 * Removes the connections to the {@link TaskFlowModel}.
	 *
	 * @param taskFlow
	 *            {@link TaskFlowModel}.
	 * @param connectionList
	 *            Listing to add the removed {@link ConnectionModel} instances.
	 */
	private void removeTaskFlowConnections(TaskFlowModel taskFlow,
			List<ConnectionModel> connectionList) {

		// Remove connection to task
		TaskFlowToTaskModel connTask = taskFlow.getTask();
		if (connTask != null) {
			connTask.remove();
			connectionList.add(connTask);
		}

		// Remove connection to external flow
		TaskFlowToExternalFlowModel connExtFlow = taskFlow.getExternalFlow();
		if (connExtFlow != null) {
			connExtFlow.remove();
			connectionList.add(connExtFlow);
		}
	}

	/**
	 * Removes the connections to the {@link TaskEscalationModel}.
	 *
	 * @param taskEscalation
	 *            {@link TaskEscalationModel}.
	 * @param connectionList
	 *            Listing to add the removed {@link ConnectionModel} instances.
	 */
	private void removeTaskEscalationConnections(
			TaskEscalationModel taskEscalation,
			List<ConnectionModel> connectionList) {

		// Remove connection to task
		TaskEscalationToTaskModel connTask = taskEscalation.getTask();
		if (connTask != null) {
			connTask.remove();
			connectionList.add(connTask);
		}

		// Remove connection to external flow
		TaskEscalationToExternalFlowModel connExtFlow = taskEscalation
				.getExternalFlow();
		if (connExtFlow != null) {
			connExtFlow.remove();
			connectionList.add(connExtFlow);
		}
	}

	/**
	 * Removes the connections to the {@link WorkTaskModel} and its associated
	 * {@link TaskModel} instances.
	 *
	 * @param workTask
	 *            {@link WorkTaskModel}.
	 * @param connectionList
	 *            Listing to add the removed {@link ConnectionModel} instances.
	 */
	private void removeWorkTaskConnections(WorkTaskModel workTask,
			List<ConnectionModel> connectionList) {

		// Remove object connections
		for (WorkTaskObjectModel taskObject : workTask.getTaskObjects()) {
			this.removeWorkTaskObjectConnections(taskObject, connectionList);
		}

		// Remove task connections (copy to stop concurrent)
		for (WorkTaskToTaskModel taskConn : new ArrayList<WorkTaskToTaskModel>(
				workTask.getTasks())) {
			TaskModel task = taskConn.getTask();
			this.removeTaskConnections(task, connectionList);
		}
	}

	/**
	 * Removes the connections to the {@link WorkTaskObjectModel}.
	 *
	 * @param workTaskObject
	 *            {@link WorkTaskObjectModel}.
	 * @param connectionList
	 *            Listing to add the removed {@link ConnectionModel} instances.
	 */
	private void removeWorkTaskObjectConnections(
			WorkTaskObjectModel workTaskObject,
			List<ConnectionModel> connectionList) {

		// Remove connection to external managed object
		WorkTaskObjectToExternalManagedObjectModel conn = workTaskObject
				.getExternalManagedObject();
		if (conn != null) {
			conn.remove();
			connectionList.add(conn);
		}
	}

	/**
	 * Removes the {@link TaskModel} instances associated to the
	 * {@link WorkTaskModel}.
	 *
	 * @param workTask
	 *            {@link WorkTaskModel}.
	 * @param taskList
	 *            Listing to add the removed {@link TaskModel} instances.
	 */
	private void removeWorkTask(WorkTaskModel workTask, List<TaskModel> taskList) {
		for (WorkTaskToTaskModel conn : workTask.getTasks()) {
			TaskModel task = conn.getTask();

			// Remove task and store for revert
			DeskChangesImpl.this.desk.removeTask(task);
			taskList.add(task);
		}
	}

	/*
	 * ==================== DeskOperations =================================
	 */

	@Override
	public <W extends Work> Change<WorkModel> addWork(String workName,
			String workSourceClassName, PropertyList properties,
			FunctionNamespaceType<W> workType, String... taskNames) {

		// Create the work model for the work type
		final WorkModel work = new WorkModel(workName, workSourceClassName);

		// Add the properties to source the work again
		for (Property property : properties) {
			work.addProperty(new PropertyModel(property.getName(), property
					.getValue()));
		}

		// Create the set of task names to include
		Set<String> includeTaskNames = new HashSet<String>();
		for (String taskName : taskNames) {
			includeTaskNames.add(taskName);
		}

		// Add the work task models
		for (ManagedFunctionType<?, ?, ?> taskType : workType.getManagedFunctionTypes()) {

			// Determine if include the task type
			String taskName = taskType.getFunctionName();
			if ((includeTaskNames.size() > 0)
					&& (!includeTaskNames.contains(taskName))) {
				// Task to not be included
				continue;
			}

			// Create and add the work task model
			WorkTaskModel workTask = DeskChangesImpl.this
					.createWorkTaskModel(taskType);
			work.addWorkTask(workTask);
		}

		// Ensure work task models in sorted order
		DeskChangesImpl.sortWorkTaskModels(work.getWorkTasks());

		// Return the change to add the work
		return new AbstractChange<WorkModel>(work, "Add work " + workName) {
			@Override
			public void apply() {
				// Add the work (ensuring in sorted order)
				DeskChangesImpl.this.desk.addWork(work);
				DeskChangesImpl.this.sortWorkModels();
			}

			@Override
			public void revert() {
				DeskChangesImpl.this.desk.removeWork(work);
			}
		};
	}

	@Override
	public Change<WorkModel> removeWork(final WorkModel workModel) {

		// Ensure the work is on the desk
		boolean isOnDesk = false;
		for (WorkModel work : this.desk.getWorks()) {
			if (work == workModel) {
				isOnDesk = true;
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not remove
			return new NoChange<WorkModel>(workModel, "Remove work "
					+ workModel.getWorkName(), "Work "
					+ workModel.getWorkName() + " not on desk");
		}

		// Return change to remove the work
		return new AbstractChange<WorkModel>(workModel, "Remove work "
				+ workModel.getWorkName()) {

			/**
			 * {@link TaskModel} instances associated to {@link WorkModel}.
			 */
			private TaskModel[] tasks;

			/**
			 * {@link ConnectionModel} instances associated to the
			 * {@link WorkModel}.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove connections to work and its tasks
				List<ConnectionModel> connectionList = new LinkedList<ConnectionModel>();
				for (WorkTaskModel workTask : workModel.getWorkTasks()) {
					DeskChangesImpl.this.removeWorkTaskConnections(workTask,
							connectionList);
				}
				this.connections = connectionList
						.toArray(new ConnectionModel[0]);

				// Remove the associated tasks (storing for revert)
				List<TaskModel> taskList = new LinkedList<TaskModel>();
				for (WorkTaskModel workTask : workModel.getWorkTasks()) {
					DeskChangesImpl.this.removeWorkTask(workTask, taskList);
				}
				this.tasks = taskList.toArray(new TaskModel[0]);

				// Remove the work
				DeskChangesImpl.this.desk.removeWork(workModel);
			}

			@Override
			public void revert() {
				// Add the work (ensuring in sorted order)
				DeskChangesImpl.this.desk.addWork(workModel);
				DeskChangesImpl.this.sortWorkModels();

				// Add the tasks (in reverse order, ensuring sorted)
				for (int i = (this.tasks.length - 1); i >= 0; i--) {
					DeskChangesImpl.this.desk.addTask(this.tasks[i]);
				}
				DeskChangesImpl.this.sortTaskModels();

				// Reconnect connections
				for (ConnectionModel connection : this.connections) {
					connection.connect();
				}
			}
		};
	}

	@Override
	public Change<WorkModel> renameWork(final WorkModel workModel,
			final String newWorkName) {

		// Ensure the work is on the desk
		boolean isOnDesk = false;
		for (WorkModel work : this.desk.getWorks()) {
			if (work == workModel) {
				isOnDesk = true;
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not remove
			return new NoChange<WorkModel>(workModel, "Rename work "
					+ workModel.getWorkName() + " to " + newWorkName, "Work "
					+ workModel.getWorkName() + " not on desk");
		}

		// Store the old name for reverting
		final String oldWorkName = workModel.getWorkName();

		// Return change to rename work
		return new AbstractChange<WorkModel>(workModel, "Rename work "
				+ workModel.getWorkName() + " to " + newWorkName) {
			@Override
			public void apply() {
				// Rename and ensure work in sorted order
				workModel.setWorkName(newWorkName);
				DeskChangesImpl.this.sortWorkModels();
			}

			@Override
			public void revert() {
				// Revert to old name, ensuring work sorted
				workModel.setWorkName(oldWorkName);
				DeskChangesImpl.this.sortWorkModels();
			}
		};
	}

	@Override
	public <W extends Work> Change<WorkModel> refactorWork(
			final WorkModel workModel, final String workName,
			final String workSourceClassName, PropertyList properties,
			FunctionNamespaceType<W> workType, Map<String, String> workTaskNameMapping,
			Map<String, Map<String, String>> workTaskToObjectNameMapping,
			Map<String, Map<String, String>> taskToFlowNameMapping,
			Map<String, Map<String, String>> taskToEscalationTypeMapping,
			String... taskNames) {

		// Create the list to contain all refactor changes
		final List<Change<?>> refactor = new LinkedList<Change<?>>();

		// ------------------- Details of WorkModel --------------------

		// Add change to rename the work
		final String existingWorkName = workModel.getWorkName();
		refactor.add(new AbstractChange<WorkModel>(workModel, "Rename work") {
			@Override
			public void apply() {
				workModel.setWorkName(workName);
			}

			@Override
			public void revert() {
				workModel.setWorkName(existingWorkName);
			}
		});

		// Add change for work class source name
		final String existingWorkSourceClassName = workModel
				.getWorkSourceClassName();
		refactor.add(new AbstractChange<WorkModel>(workModel,
				"Change WorkSource class") {
			@Override
			public void apply() {
				workModel.setWorkSourceClassName(workSourceClassName);
			}

			@Override
			public void revert() {
				workModel.setWorkSourceClassName(existingWorkSourceClassName);
			}
		});

		// Add change to the properties
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(
				workModel.getProperties());
		final List<PropertyModel> newProperties = new LinkedList<PropertyModel>();
		for (Property property : properties) {
			newProperties.add(new PropertyModel(property.getName(), property
					.getValue()));
		}
		refactor.add(new AbstractChange<WorkModel>(workModel,
				"Change work properties") {
			@Override
			public void apply() {
				for (PropertyModel property : existingProperties) {
					workModel.removeProperty(property);
				}
				for (PropertyModel property : newProperties) {
					workModel.addProperty(property);
				}
			}

			@Override
			public void revert() {
				for (PropertyModel property : newProperties) {
					workModel.removeProperty(property);
				}
				for (PropertyModel property : existingProperties) {
					workModel.addProperty(property);
				}
			}
		});

		// ---------------- WorkTaskModel / TaskModel --------------------

		// Create the map of existing work tasks to their names
		Map<String, WorkTaskModel> existingWorkTasks = new HashMap<String, WorkTaskModel>();
		for (WorkTaskModel workTask : workModel.getWorkTasks()) {
			existingWorkTasks.put(workTask.getWorkTaskName(), workTask);
		}

		// Create the set of tasks to include
		Set<String> includeTaskNames = new HashSet<String>(Arrays
				.asList(taskNames));

		// Refactor tasks
		ManagedFunctionType<?, ?, ?>[] taskTypes = workType.getManagedFunctionTypes();
		List<WorkTaskModel> targetTaskList = new LinkedList<WorkTaskModel>();
		for (int t = 0; t < taskTypes.length; t++) {
			ManagedFunctionType<?, ?, ?> taskType = taskTypes[t];

			// Obtain the details of the task type
			final String workTaskName = taskType.getFunctionName();
			Class<?> returnClass = taskType.getReturnType();
			final String returnTypeName = (returnClass == null ? null
					: returnClass.getName());

			// Determine if include the task
			if ((includeTaskNames.size() > 0)
					&& (!(includeTaskNames.contains(workTaskName)))) {
				continue; // task filtered from being included
			}

			// Obtain the work task for task type (may need to create)
			WorkTaskModel findWorkTask = this.getExistingItem(workTaskName,
					workTaskNameMapping, existingWorkTasks);
			final WorkTaskModel workTask = ((findWorkTask == null) ? new WorkTaskModel(
					workTaskName)
					: findWorkTask);
			targetTaskList.add(workTask);

			// Refactor details of work task (and tasks)
			final String existingWorkTaskName = workTask.getWorkTaskName();
			refactor.add(new AbstractChange<WorkTaskModel>(workTask,
					"Refactor work task") {

				/**
				 * Existing return types for the {@link TaskModel} instances.
				 */
				private Map<TaskModel, String> existingReturnTypes = new HashMap<TaskModel, String>();

				@Override
				public void apply() {
					// Specify new task name
					workTask.setWorkTaskName(workTaskName);
					for (WorkTaskToTaskModel conn : workTask.getTasks()) {
						TaskModel task = conn.getTask();
						task.setWorkTaskName(workTaskName);
						this.existingReturnTypes
								.put(task, task.getReturnType());
						task.setReturnType(returnTypeName);
					}
				}

				@Override
				public void revert() {
					// Revert to existing task name
					workTask.setWorkTaskName(existingWorkTaskName);
					for (WorkTaskToTaskModel conn : workTask.getTasks()) {
						TaskModel task = conn.getTask();
						task.setWorkTaskName(existingWorkTaskName);
						task.setReturnType(this.existingReturnTypes.get(task));
					}
				}
			});

			// ------------------- WorkTaskObjectModel --------------------

			// Work task to be refactored, so obtain object name mappings
			Map<String, String> objectTargetToExisting = workTaskToObjectNameMapping
					.get(workTaskName);
			if (objectTargetToExisting == null) {
				// Provide default empty map
				objectTargetToExisting = new HashMap<String, String>(0);
			}

			// Create the map of existing work task objects to their names
			Map<String, WorkTaskObjectModel> existingWorkTaskObjects = new HashMap<String, WorkTaskObjectModel>();
			for (WorkTaskObjectModel workTaskObject : workTask.getTaskObjects()) {
				existingWorkTaskObjects.put(workTaskObject.getObjectName(),
						workTaskObject);
			}

			// Obtain the objects in order as per type
			ManagedFunctionObjectType<?>[] objectTypes = taskType.getObjectTypes();
			final WorkTaskObjectModel[] targetObjectOrder = new WorkTaskObjectModel[objectTypes.length];
			for (int o = 0; o < objectTypes.length; o++) {
				ManagedFunctionObjectType<?> objectType = objectTypes[o];

				// Obtain the details of the object type
				final String objectName = objectType.getObjectName();
				Enum<?> objectKey = objectType.getKey();
				final String objectKeyName = (objectKey == null ? null
						: objectKey.name());
				Class<?> objectClass = objectType.getObjectType();
				final String objectTypeName = (objectClass == null ? null
						: objectClass.getName());

				// Obtain the object for object type (may need to create)
				WorkTaskObjectModel findWorkTaskObject = this.getExistingItem(
						objectName, objectTargetToExisting,
						existingWorkTaskObjects);
				final WorkTaskObjectModel workTaskObject = ((findWorkTaskObject == null) ? new WorkTaskObjectModel(
						objectName, objectKeyName, objectTypeName, false)
						: findWorkTaskObject);
				targetObjectOrder[o] = workTaskObject;

				// Refactor details of object
				final String existingObjectName = workTaskObject
						.getObjectName();
				final String existingKeyName = workTaskObject.getKey();
				final String existingTypeName = workTaskObject.getObjectType();
				refactor.add(new AbstractChange<WorkTaskObjectModel>(
						workTaskObject, "Refactor work task object") {
					@Override
					public void apply() {
						workTaskObject.setObjectName(objectName);
						workTaskObject.setKey(objectKeyName);
						workTaskObject.setObjectType(objectTypeName);
					}

					@Override
					public void revert() {
						workTaskObject.setObjectName(existingObjectName);
						workTaskObject.setKey(existingKeyName);
						workTaskObject.setObjectType(existingTypeName);
					}
				});
			}

			// Obtain the existing object order
			final WorkTaskObjectModel[] existingObjectOrder = workTask
					.getTaskObjects().toArray(new WorkTaskObjectModel[0]);

			// Add changes to disconnect existing objects to be removed
			Set<WorkTaskObjectModel> targetObjects = new HashSet<WorkTaskObjectModel>(
					Arrays.asList(targetObjectOrder));
			for (WorkTaskObjectModel existingObject : existingObjectOrder) {
				if (!(targetObjects.contains(existingObject))) {
					// Add change to disconnect object
					final WorkTaskObjectModel taskObject = existingObject;
					refactor.add(new DisconnectChange<WorkTaskObjectModel>(
							existingObject) {
						@Override
						protected void populateRemovedConnections(
								List<ConnectionModel> connList) {
							DeskChangesImpl.this
									.removeWorkTaskObjectConnections(
											taskObject, connList);
						}
					});
				}
			}

			// Add change to order the refactored objects
			refactor.add(new AbstractChange<WorkTaskModel>(workTask,
					"Refactor objects of work task") {
				@Override
				public void apply() {
					// Remove existing objects, add target objects
					for (WorkTaskObjectModel object : existingObjectOrder) {
						workTask.removeTaskObject(object);
					}
					for (WorkTaskObjectModel object : targetObjectOrder) {
						workTask.addTaskObject(object);
					}
				}

				@Override
				public void revert() {
					// Remove the target objects, add back existing
					for (WorkTaskObjectModel object : targetObjectOrder) {
						workTask.removeTaskObject(object);
					}
					for (WorkTaskObjectModel object : existingObjectOrder) {
						workTask.addTaskObject(object);
					}
				}
			});

			// ---------------------- TaskModel ------------------------

			// Refactor the tasks of the work task
			for (WorkTaskToTaskModel workTaskToTask : workTask.getTasks()) {

				// Ensure have task for connection
				final TaskModel task = workTaskToTask.getTask();
				if (task == null) {
					continue; // must have task
				}

				// Obtain details of task
				String taskName = task.getTaskName();

				// --------------- TaskFlowModel ------------------------

				// Task to be refactored, so obtain flow name mappings
				Map<String, String> flowTargetToExisting = taskToFlowNameMapping
						.get(taskName);
				if (flowTargetToExisting == null) {
					// Provide default empty map
					flowTargetToExisting = new HashMap<String, String>(0);
				}

				// Create the map of existing task flows to their names
				Map<String, TaskFlowModel> existingTaskFlows = new HashMap<String, TaskFlowModel>();
				for (TaskFlowModel taskFlow : task.getTaskFlows()) {
					existingTaskFlows.put(taskFlow.getFlowName(), taskFlow);
				}

				// Obtain the flows in order of type
				ManagedFunctionFlowType<?>[] flowTypes = taskType.getFlowTypes();
				final TaskFlowModel[] targetFlowOrder = new TaskFlowModel[flowTypes.length];
				for (int f = 0; f < targetFlowOrder.length; f++) {
					ManagedFunctionFlowType<?> flowType = flowTypes[f];

					// Obtain the details of the flow type
					final String flowName = flowType.getFlowName();
					Enum<?> flowKey = flowType.getKey();
					final String flowKeyName = (flowKey == null ? null
							: flowKey.name());
					Class<?> argumentType = flowType.getArgumentType();
					final String argumentTypeName = (argumentType == null ? null
							: argumentType.getName());

					// Obtain the flow for flow type (may need to create)
					TaskFlowModel findTaskFlow = this.getExistingItem(flowName,
							flowTargetToExisting, existingTaskFlows);
					final TaskFlowModel taskFlow = ((findTaskFlow == null) ? new TaskFlowModel(
							flowName, flowKeyName, argumentTypeName)
							: findTaskFlow);
					targetFlowOrder[f] = taskFlow;

					// Refactor details of flow
					final String existingFlowName = taskFlow.getFlowName();
					final String existingFlowKeyName = taskFlow.getKey();
					final String existingArgumentTypeName = taskFlow
							.getArgumentType();
					refactor.add(new AbstractChange<TaskFlowModel>(taskFlow,
							"Refactor task flow") {
						@Override
						public void apply() {
							taskFlow.setFlowName(flowName);
							taskFlow.setKey(flowKeyName);
							taskFlow.setArgumentType(argumentTypeName);
						}

						@Override
						public void revert() {
							taskFlow.setFlowName(existingFlowName);
							taskFlow.setKey(existingFlowKeyName);
							taskFlow.setArgumentType(existingArgumentTypeName);
						}
					});
				}

				// Obtain the existing flow order
				final TaskFlowModel[] existingFlowOrder = task.getTaskFlows()
						.toArray(new TaskFlowModel[0]);

				// Add changes to disconnect existing flows to be removed
				Set<TaskFlowModel> targetFlows = new HashSet<TaskFlowModel>(
						Arrays.asList(targetFlowOrder));
				for (TaskFlowModel existingTaskFlow : existingFlowOrder) {
					if (!(targetFlows.contains(existingTaskFlow))) {
						// Add change to disconnect flow
						final TaskFlowModel taskFlow = existingTaskFlow;
						refactor.add(new DisconnectChange<TaskFlowModel>(
								taskFlow) {
							@Override
							protected void populateRemovedConnections(
									List<ConnectionModel> connList) {
								DeskChangesImpl.this.removeTaskFlowConnections(
										taskFlow, connList);
							}
						});
					}
				}

				// Add change to order the refactored flows
				refactor.add(new AbstractChange<TaskModel>(task,
						"Refactor task flows") {
					@Override
					public void apply() {
						// Remove existing flows, add target flows
						for (TaskFlowModel flow : existingFlowOrder) {
							task.removeTaskFlow(flow);
						}
						for (TaskFlowModel flow : targetFlowOrder) {
							task.addTaskFlow(flow);
						}
					}

					@Override
					public void revert() {
						// Remove target flows, add back existing flows
						for (TaskFlowModel flow : targetFlowOrder) {
							task.removeTaskFlow(flow);
						}
						for (TaskFlowModel flow : existingFlowOrder) {
							task.addTaskFlow(flow);
						}
					}
				});

				// --------------- TaskEscalationModel ------------------

				// Task to be refactored, so obtain escalation name mappings
				Map<String, String> escalationTargetToExisting = taskToEscalationTypeMapping
						.get(taskName);
				if (escalationTargetToExisting == null) {
					// Provide default empty map
					escalationTargetToExisting = new HashMap<String, String>(0);
				}

				// Create the map of existing task escalations to their names
				Map<String, TaskEscalationModel> existingTaskEscalations = new HashMap<String, TaskEscalationModel>();
				for (TaskEscalationModel taskEscalation : task
						.getTaskEscalations()) {
					existingTaskEscalations.put(taskEscalation
							.getEscalationType(), taskEscalation);
				}

				// Obtain the escalations in order of type
				ManagedFunctionEscalationType[] escalationTypes = taskType
						.getEscalationTypes();
				final TaskEscalationModel[] targetEscalationOrder = new TaskEscalationModel[escalationTypes.length];
				for (int e = 0; e < targetEscalationOrder.length; e++) {
					ManagedFunctionEscalationType escalationType = escalationTypes[e];

					// Obtain details of the escalation type
					final String escalationTypeName = escalationType
							.getEscalationType().getName();

					// Obtain the escalation for escalation type (may create)
					TaskEscalationModel findTaskEscalation = this
							.getExistingItem(escalationTypeName,
									escalationTargetToExisting,
									existingTaskEscalations);
					final TaskEscalationModel taskEscalation = ((findTaskEscalation == null) ? new TaskEscalationModel(
							escalationTypeName)
							: findTaskEscalation);
					targetEscalationOrder[e] = taskEscalation;

					// Refactor details of escalation
					final String existingEscalationTypeName = taskEscalation
							.getEscalationType();
					refactor.add(new AbstractChange<TaskEscalationModel>(
							taskEscalation, "Refactor task escalation") {
						@Override
						public void apply() {
							taskEscalation
									.setEscalationType(escalationTypeName);
						}

						@Override
						public void revert() {
							taskEscalation
									.setEscalationType(existingEscalationTypeName);
						}
					});
				}

				// Obtain the existing escalation order
				final TaskEscalationModel[] existingEscalationOrder = task
						.getTaskEscalations().toArray(
								new TaskEscalationModel[0]);

				// Add changes to disconnect existing escalations to be removed
				Set<TaskEscalationModel> targetEscalations = new HashSet<TaskEscalationModel>(
						Arrays.asList(targetEscalationOrder));
				for (TaskEscalationModel existingEscalation : existingEscalationOrder) {
					if (!(targetEscalations.contains(existingEscalation))) {
						// Add change to disconnect escalation
						final TaskEscalationModel taskEscalation = existingEscalation;
						refactor.add(new DisconnectChange<TaskEscalationModel>(
								taskEscalation) {
							@Override
							protected void populateRemovedConnections(
									List<ConnectionModel> connList) {
								DeskChangesImpl.this
										.removeTaskEscalationConnections(
												taskEscalation, connList);
							}
						});
					}
				}

				// Add change to order the refactored escalations
				refactor.add(new AbstractChange<TaskModel>(task,
						"Refactor task escalations") {
					@Override
					public void apply() {
						// Remove existing escalations, add target escalations
						for (TaskEscalationModel escalation : existingEscalationOrder) {
							task.removeTaskEscalation(escalation);
						}
						for (TaskEscalationModel escalation : targetEscalationOrder) {
							task.addTaskEscalation(escalation);
						}
					}

					@Override
					public void revert() {
						// Remove target escalations, add back existing
						for (TaskEscalationModel escalation : targetEscalationOrder) {
							task.removeTaskEscalation(escalation);
						}
						for (TaskEscalationModel escalation : existingEscalationOrder) {
							task.addTaskEscalation(escalation);
						}
					}
				});
			}
		}

		// ------------ WorkTaskModel / TaskModel (continued) ----------------

		// Obtain the target work task order
		final WorkTaskModel[] targetTaskOrder = targetTaskList
				.toArray(new WorkTaskModel[0]);

		// Obtain existing work task order
		final WorkTaskModel[] existingTaskOrder = workModel.getWorkTasks()
				.toArray(new WorkTaskModel[0]);

		// Add changes to disconnect existing tasks to be removed
		Set<WorkTaskModel> targetTasks = new HashSet<WorkTaskModel>(Arrays
				.asList(targetTaskOrder));
		for (WorkTaskModel existingTask : existingTaskOrder) {
			if (!(targetTasks.contains(existingTask))) {
				final WorkTaskModel workTask = existingTask;

				// Add change to disconnect work task (and its tasks)
				refactor.add(new DisconnectChange<WorkTaskModel>(workTask) {
					@Override
					protected void populateRemovedConnections(
							List<ConnectionModel> connList) {
						DeskChangesImpl.this.removeWorkTaskConnections(
								workTask, connList);
					}
				});

				// Add change to remove tasks of work task
				refactor.add(new AbstractChange<WorkTaskModel>(workTask,
						"Remove tasks of work task") {

					/**
					 * Removed {@link TaskModel} instances.
					 */
					private List<TaskModel> tasks;

					@Override
					public void apply() {
						this.tasks = new LinkedList<TaskModel>();
						DeskChangesImpl.this.removeWorkTask(workTask,
								this.tasks);
					}

					@Override
					public void revert() {
						// Add back the task models
						for (TaskModel task : this.tasks) {
							DeskChangesImpl.this.desk.addTask(task);
						}
					}
				});
			}
		}

		// Add change to order the new tasks
		refactor.add(new AbstractChange<WorkModel>(workModel,
				"Refactor tasks of work") {
			@Override
			public void apply() {
				// Remove existing tasks, add target tasks
				for (WorkTaskModel task : existingTaskOrder) {
					workModel.removeWorkTask(task);
				}
				for (WorkTaskModel task : targetTaskOrder) {
					workModel.addWorkTask(task);
				}
			}

			@Override
			public void revert() {
				// Remove the target tasks, add back existing
				for (WorkTaskModel task : targetTaskOrder) {
					workModel.removeWorkTask(task);
				}
				for (WorkTaskModel task : existingTaskOrder) {
					workModel.addWorkTask(task);
				}
			}
		});

		// Return change to do all the refactoring
		return new AbstractChange<WorkModel>(workModel, "Refactor work") {
			@Override
			public void apply() {
				for (Change<?> change : refactor) {
					change.apply();
				}
			}

			@Override
			public void revert() {
				// Revert changes in reverse order as applied
				for (int i = (refactor.size() - 1); i >= 0; i--) {
					Change<?> change = refactor.get(i);
					change.revert();
				}
			}
		};
	}

	/**
	 * Obtains the existing item for the target name.
	 *
	 * @param targetItemName
	 *            Target item name.
	 * @param targetToExistingName
	 *            Mapping of target item name to existing item name.
	 * @param existingNameToItem
	 *            Mapping of existing item name to the existing item.
	 */
	private <T> T getExistingItem(String targetItemName,
			Map<String, String> targetToExistingName,
			Map<String, T> existingNameToItem) {

		// Obtain the existing item name
		String existingItemName = targetToExistingName.get(targetItemName);
		if (existingItemName != null) {
			// Have existing name, so return existing item by name
			return existingNameToItem.get(existingItemName);
		} else {
			// No existing name, so no existing item
			return null;
		}
	}

	@Override
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> addWorkTask(
			final WorkModel workModel, ManagedFunctionType<W, D, F> taskType) {

		// Ensure the work task is not already added
		String taskName = taskType.getFunctionName();
		for (WorkTaskModel workTask : workModel.getWorkTasks()) {
			if (taskName.equals(taskName)) {
				// Task already added
				return new NoChange<WorkTaskModel>(workTask, "Add work task "
						+ taskName, "Task " + taskName
						+ " already added to work " + workModel.getWorkName());
			}
		}

		// Create the work task model
		final WorkTaskModel workTask = DeskChangesImpl.this
				.createWorkTaskModel(taskType);

		// Return the add work task change
		return new AbstractChange<WorkTaskModel>(workTask, "Add work task "
				+ taskName) {
			@Override
			public void apply() {
				// Add work task (ensuring work tasks sorted)
				workModel.addWorkTask(workTask);
				DeskChangesImpl.sortWorkTaskModels(workModel.getWorkTasks());
			}

			@Override
			public void revert() {
				// Remove work task (should already be sorted)
				workModel.removeWorkTask(workTask);
			}
		};
	}

	@Override
	public Change<WorkTaskModel> removeWorkTask(final WorkModel work,
			final WorkTaskModel workTask) {

		// Ensure work task on work
		boolean isOnWork = false;
		for (WorkTaskModel workTaskModel : work.getWorkTasks()) {
			if (workTaskModel == workTask) {
				isOnWork = true;
			}
		}
		if (!isOnWork) {
			// Work task not on work
			return new NoChange<WorkTaskModel>(workTask, "Remove work task "
					+ workTask.getWorkTaskName(), "Work task "
					+ workTask.getWorkTaskName() + " not on work "
					+ work.getWorkName());
		}

		// Return the remove work task change
		return new AbstractChange<WorkTaskModel>(workTask, "Remove work task "
				+ workTask.getWorkTaskName()) {

			/**
			 * Removed {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			/**
			 * Removed {@link TaskModel} instances.
			 */
			private TaskModel[] tasks;

			@Override
			public void apply() {
				// Remove the connections
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				DeskChangesImpl.this.removeWorkTaskConnections(workTask,
						connList);
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the tasks of the work task
				List<TaskModel> taskList = new LinkedList<TaskModel>();
				DeskChangesImpl.this.removeWorkTask(workTask, taskList);
				this.tasks = taskList.toArray(new TaskModel[0]);

				// Remove the work task
				work.removeWorkTask(workTask);
			}

			@Override
			public void revert() {
				// Add the work task (ensuring sorted)
				work.addWorkTask(workTask);
				DeskChangesImpl.sortWorkTaskModels(work.getWorkTasks());

				// Add the tasks (in reverse order, ensuring sorted)
				for (int i = (this.tasks.length - 1); i >= 0; i--) {
					DeskChangesImpl.this.desk.addTask(this.tasks[i]);
				}
				DeskChangesImpl.this.sortTaskModels();

				// Reconnect connections
				for (ConnectionModel connection : this.connections) {
					connection.connect();
				}
			}
		};
	}

	@Override
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<TaskModel> addTask(
			String taskName, final WorkTaskModel workTask,
			ManagedFunctionType<W, D, F> taskType) {

		// Create the task model
		Class<?> returnType = taskType.getReturnType();
		final TaskModel task = new TaskModel(taskName, false, null, workTask
				.getWorkTaskName(), (returnType != null ? returnType.getName()
				: null));
		for (ManagedFunctionFlowType<?> flowType : taskType.getFlowTypes()) {
			Enum<?> key = flowType.getKey();
			Class<?> argumentType = flowType.getArgumentType();
			TaskFlowModel taskFlow = new TaskFlowModel(flowType.getFlowName(),
					(key != null ? key.name() : null),
					(argumentType != null ? argumentType.getName() : null));
			task.addTaskFlow(taskFlow);
		}
		for (ManagedFunctionEscalationType escalationType : taskType.getEscalationTypes()) {
			TaskEscalationModel taskEscalation = new TaskEscalationModel(
					escalationType.getEscalationType().getName());
			task.addTaskEscalation(taskEscalation);
		}

		// Ensure the work task is on the desk and obtain its work
		WorkModel work = null;
		for (WorkModel workModel : this.desk.getWorks()) {
			for (WorkTaskModel workTaskModel : workModel.getWorkTasks()) {
				if (workTaskModel == workTask) {
					// On the desk
					work = workModel;
				}
			}
		}
		if (work == null) {
			// Work task not on desk so can not add task
			return new NoChange<TaskModel>(task, "Add task " + taskName,
					"Work task " + workTask.getWorkTaskName() + " not on desk");
		}

		// Specify the work name of the task (now that have work)
		task.setWorkName(work.getWorkName());

		// Ensure the work task for the task type
		if (!workTask.getWorkTaskName().equals(taskType.getFunctionName())) {
			// Not correct task type for the work task
			return new NoChange<TaskModel>(task, "Add task " + taskName,
					"Task type " + taskType.getFunctionName()
							+ " does not match work task "
							+ workTask.getWorkTaskName());
		}

		// Create the connection from work task to task
		final WorkTaskToTaskModel conn = new WorkTaskToTaskModel(task, workTask);

		// Return the change to add the task
		return new AbstractChange<TaskModel>(task, "Add task " + taskName) {
			@Override
			public void apply() {
				// Add the task ensuring ordering
				DeskChangesImpl.this.desk.addTask(task);
				DeskChangesImpl.this.sortTaskModels();

				// Connect work task to the task (ensuring ordering)
				conn.connect();
				DeskChangesImpl.sortWorkTaskToTaskConnections(workTask
						.getTasks());
			}

			@Override
			public void revert() {
				// Disconnect work task from the task
				conn.remove();

				// Remove task (should maintain ordering)
				DeskChangesImpl.this.desk.removeTask(task);
			}
		};
	}

	@Override
	public Change<TaskModel> removeTask(final TaskModel task) {

		// Ensure the task is on the desk
		boolean isOnDesk = false;
		for (TaskModel taskModel : this.desk.getTasks()) {
			if (task == taskModel) {
				isOnDesk = true; // task on desk
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not remove it
			return new NoChange<TaskModel>(task, "Remove task "
					+ task.getTaskName(), "Task " + task.getTaskName()
					+ " not on desk");
		}

		// Create change to remove the task
		return new AbstractChange<TaskModel>(task, "Remove task "
				+ task.getTaskName()) {

			/**
			 * {@link ConnectionModel} instances removed.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {
				// Remove connections to the task
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				DeskChangesImpl.this.removeTaskConnections(task, connList);

				// Remove connection to work task
				WorkTaskToTaskModel workTaskConn = task.getWorkTask();
				if (workTaskConn != null) {
					workTaskConn.remove();
					connList.add(workTaskConn);
				}

				// Store for revert
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the task (should maintain order)
				DeskChangesImpl.this.desk.removeTask(task);
			}

			@Override
			public void revert() {
				// Add task back in (ensuring order)
				DeskChangesImpl.this.desk.addTask(task);
				DeskChangesImpl.this.sortTaskModels();

				// Reconnect connections
				for (ConnectionModel conn : this.connections) {
					conn.connect();
				}

				// Ensure work task connections sorted by task name
				DeskChangesImpl.sortWorkTaskToTaskConnections(task
						.getWorkTask().getWorkTask().getTasks());
			}
		};
	}

	@Override
	public Change<TaskModel> renameTask(final TaskModel task,
			final String newTaskName) {

		// Ensure the task is on the desk
		boolean isOnDesk = false;
		for (TaskModel taskModel : DeskChangesImpl.this.desk.getTasks()) {
			if (task == taskModel) {
				isOnDesk = true; // task on desk
			}
		}
		if (!isOnDesk) {
			// Can not remove task as not on desk
			return new NoChange<TaskModel>(task, "Rename task "
					+ task.getTaskName() + " to " + newTaskName, "Task "
					+ task.getTaskName() + " not on desk");
		}

		// Maintain old task name for revert
		final String oldTaskName = task.getTaskName();

		// Return rename change
		return new AbstractChange<TaskModel>(task, "Rename task " + oldTaskName
				+ " to " + newTaskName) {
			@Override
			public void apply() {
				// Rename task (ensuring ordering)
				task.setTaskName(newTaskName);
				DeskChangesImpl.this.sortTaskModels();
				DeskChangesImpl.sortWorkTaskToTaskConnections(task
						.getWorkTask().getWorkTask().getTasks());
			}

			@Override
			public void revert() {
				// Revert to old task name (ensuring ordering)
				task.setTaskName(oldTaskName);
				DeskChangesImpl.this.sortTaskModels();
				DeskChangesImpl.sortWorkTaskToTaskConnections(task
						.getWorkTask().getWorkTask().getTasks());
			}
		};
	}

	@Override
	public Change<WorkTaskObjectModel> setObjectAsParameter(
			boolean isParameter, final WorkTaskObjectModel taskObject) {

		// Ensure the task object on the desk
		boolean isOnDesk = false;
		for (WorkModel work : DeskChangesImpl.this.desk.getWorks()) {
			for (WorkTaskModel workTask : work.getWorkTasks()) {
				for (WorkTaskObjectModel taskObjectModel : workTask
						.getTaskObjects()) {
					if (taskObject == taskObjectModel) {
						isOnDesk = true; // on the desk
					}
				}
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not set as parameter
			return new NoChange<WorkTaskObjectModel>(taskObject,
					"Set task object " + taskObject.getObjectName() + " as "
							+ (isParameter ? "a parameter" : "an object"),
					"Task object " + taskObject.getObjectName()
							+ " not on desk");
		}

		// Return the appropriate change
		if (isParameter) {
			// Return change to set as parameter
			final WorkTaskObjectToExternalManagedObjectModel conn = taskObject
					.getExternalManagedObject();
			return new AbstractChange<WorkTaskObjectModel>(taskObject,
					"Set task object " + taskObject.getObjectName()
							+ " as a parameter") {
				@Override
				public void apply() {
					// Remove possible connection to external managed object
					if (conn != null) {
						conn.remove();
					}

					// Flag as parameter
					taskObject.setIsParameter(true);
				}

				@Override
				public void revert() {
					// Flag as object
					taskObject.setIsParameter(false);

					// Reconnect to possible external managed object
					if (conn != null) {
						conn.connect();
					}
				}
			};
		} else {
			// Return change to set as object
			return new AbstractChange<WorkTaskObjectModel>(taskObject,
					"Set task object " + taskObject.getObjectName()
							+ " as an object") {
				@Override
				public void apply() {
					// Flag as object (no connection as parameter)
					taskObject.setIsParameter(false);
				}

				@Override
				public void revert() {
					// Flag back as parameter
					taskObject.setIsParameter(true);
				}
			};
		}
	}

	@Override
	public Change<TaskModel> setTaskAsPublic(final boolean isPublic,
			final TaskModel task) {

		// Ensure task on desk
		boolean isOnDesk = false;
		for (TaskModel taskModel : DeskChangesImpl.this.desk.getTasks()) {
			if (task == taskModel) {
				isOnDesk = true; // task on desk
			}
		}
		if (!isOnDesk) {
			// Task not on desk so can not make public
			return new NoChange<TaskModel>(task, "Set task "
					+ task.getTaskName() + (isPublic ? " public" : " private"),
					"Task " + task.getTaskName() + " not on desk");
		}

		// Return the change
		return new AbstractChange<TaskModel>(task, "Set task "
				+ task.getTaskName() + (isPublic ? " public" : " private")) {
			@Override
			public void apply() {
				// Specify public/private
				task.setIsPublic(isPublic);
			}

			@Override
			public void revert() {
				// Revert public/private
				task.setIsPublic(!isPublic);
			}
		};
	}

	@Override
	public Change<ExternalFlowModel> addExternalFlow(String externalFlowName,
			String argumentType) {

		// Create the external flow
		final ExternalFlowModel externalFlow = new ExternalFlowModel(
				externalFlowName, argumentType);

		// Return change to add external flow
		return new AbstractChange<ExternalFlowModel>(externalFlow,
				"Add external flow " + externalFlowName) {
			@Override
			public void apply() {
				// Add external flow (ensuring ordering)
				DeskChangesImpl.this.desk.addExternalFlow(externalFlow);
				DeskChangesImpl.this.sortExternalFlows();
			}

			@Override
			public void revert() {
				// Remove external flow (should maintain order)
				DeskChangesImpl.this.desk.removeExternalFlow(externalFlow);
			}
		};
	}

	@Override
	public Change<ExternalFlowModel> removeExternalFlow(
			final ExternalFlowModel externalFlow) {

		// Ensure external flow on desk
		boolean isOnDesk = false;
		for (ExternalFlowModel externalFlowModel : DeskChangesImpl.this.desk
				.getExternalFlows()) {
			if (externalFlow == externalFlowModel) {
				isOnDesk = true; // on the desk
			}
		}
		if (!isOnDesk) {
			// No change as external flow not on desk
			return new NoChange<ExternalFlowModel>(externalFlow,
					"Remove external flow "
							+ externalFlow.getExternalFlowName(),
					"External flow " + externalFlow.getExternalFlowName()
							+ " not on desk");
		}

		// Return change to remove external flow
		return new AbstractChange<ExternalFlowModel>(externalFlow,
				"Remove external flow " + externalFlow.getExternalFlowName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {
				// Remove the connections to the external flows
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				for (TaskFlowToExternalFlowModel conn : new ArrayList<TaskFlowToExternalFlowModel>(
						externalFlow.getTaskFlows())) {
					conn.remove();
					connList.add(conn);
				}
				for (TaskToNextExternalFlowModel conn : new ArrayList<TaskToNextExternalFlowModel>(
						externalFlow.getPreviousTasks())) {
					conn.remove();
					connList.add(conn);
				}
				for (TaskEscalationToExternalFlowModel conn : new ArrayList<TaskEscalationToExternalFlowModel>(
						externalFlow.getTaskEscalations())) {
					conn.remove();
					connList.add(conn);
				}
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the external flow (should maintain order)
				DeskChangesImpl.this.desk.removeExternalFlow(externalFlow);
			}

			@Override
			public void revert() {
				// Add the external flow back (ensure ordering)
				DeskChangesImpl.this.desk.addExternalFlow(externalFlow);
				DeskChangesImpl.this.sortExternalFlows();

				// Reconnect connections
				for (ConnectionModel conn : this.connections) {
					conn.connect();
				}
			}
		};
	}

	@Override
	public Change<ExternalFlowModel> renameExternalFlow(
			final ExternalFlowModel externalFlow,
			final String newExternalFlowName) {

		// TODO test this method (renameExternalFlow)

		// Obtain the old name
		final String oldExternalFlowName = externalFlow.getExternalFlowName();

		// Return change to rename the external flow
		return new AbstractChange<ExternalFlowModel>(externalFlow,
				"Rename external flow to " + newExternalFlowName) {
			@Override
			public void apply() {
				externalFlow.setExternalFlowName(newExternalFlowName);
			}

			@Override
			public void revert() {
				externalFlow.setExternalFlowName(oldExternalFlowName);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String objectType) {

		// Create the external managed object
		final ExternalManagedObjectModel externalMo = new ExternalManagedObjectModel(
				externalManagedObjectName, objectType);

		// Return the change to add external managed object
		return new AbstractChange<ExternalManagedObjectModel>(externalMo,
				"Add external managed object " + externalManagedObjectName) {
			@Override
			public void apply() {
				// Add external managed object (ensure ordering)
				DeskChangesImpl.this.desk.addExternalManagedObject(externalMo);
				DeskChangesImpl.this.sortExternalManagedObjects();
			}

			@Override
			public void revert() {
				// Remove external managed object (should maintain order)
				DeskChangesImpl.this.desk
						.removeExternalManagedObject(externalMo);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> removeExternalManagedObject(
			final ExternalManagedObjectModel externalManagedObject) {

		// Ensure external managed object on desk
		boolean isOnDesk = false;
		for (ExternalManagedObjectModel externalManagedObjectModel : DeskChangesImpl.this.desk
				.getExternalManagedObjects()) {
			if (externalManagedObject == externalManagedObjectModel) {
				isOnDesk = true; // on the desk
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not remove it
			return new NoChange<ExternalManagedObjectModel>(
					externalManagedObject, "Remove external managed object "
							+ externalManagedObject
									.getExternalManagedObjectName(),
					"External managed object "
							+ externalManagedObject
									.getExternalManagedObjectName()
							+ " not on desk");
		}

		// Return change to remove the external managed object
		return new AbstractChange<ExternalManagedObjectModel>(
				externalManagedObject, "Remove external managed object "
						+ externalManagedObject.getExternalManagedObjectName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {
				// Remove the connections to the external managed object
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				for (WorkTaskObjectToExternalManagedObjectModel conn : new ArrayList<WorkTaskObjectToExternalManagedObjectModel>(
						externalManagedObject.getTaskObjects())) {
					conn.remove();
					connList.add(conn);
				}
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the external managed object (should maintain order)
				DeskChangesImpl.this.desk
						.removeExternalManagedObject(externalManagedObject);
			}

			@Override
			public void revert() {
				// Add back the external managed object (ensure ordering)
				DeskChangesImpl.this.desk
						.addExternalManagedObject(externalManagedObject);
				DeskChangesImpl.this.sortExternalManagedObjects();

				// Reconnect connections
				for (ConnectionModel conn : this.connections) {
					conn.connect();
				}
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> renameExternalManagedObject(
			final ExternalManagedObjectModel externalManagedObject,
			final String newExternalManagedObjectName) {

		// TODO test this method (renameExternalManagedObject)

		// Obtain the old name
		final String oldExternalManagedObjectName = externalManagedObject
				.getExternalManagedObjectName();

		// Return change to rename the external managed object
		return new AbstractChange<ExternalManagedObjectModel>(
				externalManagedObject, "Rename external managed object to "
						+ newExternalManagedObjectName) {
			@Override
			public void apply() {
				externalManagedObject
						.setExternalManagedObjectName(newExternalManagedObjectName);
			}

			@Override
			public void revert() {
				externalManagedObject
						.setExternalManagedObjectName(oldExternalManagedObjectName);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectSourceModel> addDeskManagedObjectSource(
			String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties,
			long timeout, ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addDeskManagedObjectSource)

		// Create the managed object source
		final DeskManagedObjectSourceModel managedObjectSource = new DeskManagedObjectSourceModel(
				managedObjectSourceName, managedObjectSourceClassName,
				managedObjectType.getObjectClass().getName(), String
						.valueOf(timeout));
		for (Property property : properties) {
			managedObjectSource.addProperty(new PropertyModel(property
					.getName(), property.getValue()));
		}

		// Add the flows for the managed object source
		for (ManagedObjectFlowType<?> flow : managedObjectType.getFlowTypes()) {
			managedObjectSource
					.addDeskManagedObjectSourceFlow(new DeskManagedObjectSourceFlowModel(
							flow.getFlowName(), flow.getArgumentType()
									.getName()));
		}

		// Return the change to add the managed object source
		return new AbstractChange<DeskManagedObjectSourceModel>(
				managedObjectSource, "Add managed object source") {
			@Override
			public void apply() {
				DeskChangesImpl.this.desk
						.addDeskManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				DeskChangesImpl.this.desk
						.removeDeskManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectSourceModel> removeDeskManagedObjectSource(
			final DeskManagedObjectSourceModel managedObjectSource) {

		// TODO test this method (removeDeskManagedObjectSource)

		// Return change to remove the managed object source
		return new AbstractChange<DeskManagedObjectSourceModel>(
				managedObjectSource, "Remove managed object source") {
			@Override
			public void apply() {
				DeskChangesImpl.this.desk
						.removeDeskManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				DeskChangesImpl.this.desk
						.addDeskManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectSourceModel> renameDeskManagedObjectSource(
			final DeskManagedObjectSourceModel managedObjectSource,
			final String newManagedObjectSourceName) {

		// TODO test this method (renameDeskManagedObjectSource)

		// Obtain the old managed object source name
		final String oldManagedObjectSourceName = managedObjectSource
				.getDeskManagedObjectSourceName();

		// Return change to rename the managed object source
		return new AbstractChange<DeskManagedObjectSourceModel>(
				managedObjectSource, "Rename managed object source to "
						+ newManagedObjectSourceName) {
			@Override
			public void apply() {
				managedObjectSource
						.setDeskManagedObjectSourceName(newManagedObjectSourceName);
			}

			@Override
			public void revert() {
				managedObjectSource
						.setDeskManagedObjectSourceName(oldManagedObjectSourceName);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectModel> addDeskManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope,
			DeskManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addDeskManagedObject)

		// Create the managed object
		final DeskManagedObjectModel managedObject = new DeskManagedObjectModel(
				managedObjectName, getManagedObjectScope(managedObjectScope));

		// Add the dependencies for the managed object
		for (ManagedObjectDependencyType<?> dependency : managedObjectType
				.getDependencyTypes()) {
			managedObject
					.addDeskManagedObjectDependency(new DeskManagedObjectDependencyModel(
							dependency.getDependencyName(), dependency
									.getDependencyType().getName()));
		}

		// Create connection to the managed object source
		final DeskManagedObjectToDeskManagedObjectSourceModel conn = new DeskManagedObjectToDeskManagedObjectSourceModel();
		conn.setDeskManagedObject(managedObject);
		conn.setDeskManagedObjectSource(managedObjectSource);

		// Return change to add the managed object
		return new AbstractChange<DeskManagedObjectModel>(managedObject,
				"Add managed object") {
			@Override
			public void apply() {
				DeskChangesImpl.this.desk.addDeskManagedObject(managedObject);
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
				DeskChangesImpl.this.desk
						.removeDeskManagedObject(managedObject);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectModel> removeDeskManagedObject(
			final DeskManagedObjectModel managedObject) {

		// TODO test this method (removeDeskManagedObject)

		// Return change to remove the managed object
		return new AbstractChange<DeskManagedObjectModel>(managedObject,
				"Remove managed object") {
			@Override
			public void apply() {
				DeskChangesImpl.this.desk
						.removeDeskManagedObject(managedObject);
			}

			@Override
			public void revert() {
				DeskChangesImpl.this.desk.addDeskManagedObject(managedObject);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectModel> renameDeskManagedObject(
			final DeskManagedObjectModel managedObject,
			final String newManagedObjectName) {

		// TODO test this method (renameDeskManagedObject)

		// Obtain the old managed object name
		final String oldManagedObjectName = managedObject
				.getDeskManagedObjectName();

		// Return change to rename the managed object
		return new AbstractChange<DeskManagedObjectModel>(managedObject,
				"Rename managed object to " + newManagedObjectName) {
			@Override
			public void apply() {
				managedObject.setDeskManagedObjectName(newManagedObjectName);
			}

			@Override
			public void revert() {
				managedObject.setDeskManagedObjectName(oldManagedObjectName);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectModel> rescopeDeskManagedObject(
			final DeskManagedObjectModel managedObject,
			final ManagedObjectScope newManagedObjectScope) {

		// TODO test this method (rescopeDeskManagedObject)

		// Obtain the new scope text
		final String newScope = getManagedObjectScope(newManagedObjectScope);

		// OBtain the old managed object scope
		final String oldScope = managedObject.getManagedObjectScope();

		// Return change to re-scope the managed object
		return new AbstractChange<DeskManagedObjectModel>(managedObject,
				"Rescope managed object to " + newScope) {
			@Override
			public void apply() {
				managedObject.setManagedObjectScope(newScope);
			}

			@Override
			public void revert() {
				managedObject.setManagedObjectScope(oldScope);
			}
		};
	}

	@Override
	public Change<WorkTaskObjectToExternalManagedObjectModel> linkWorkTaskObjectToExternalManagedObject(
			WorkTaskObjectModel workTaskObject,
			ExternalManagedObjectModel externalManagedObject) {

		// TODO test this method (linkWorkTaskObjectToExternalManagedObject)

		// Create the connection
		final WorkTaskObjectToExternalManagedObjectModel conn = new WorkTaskObjectToExternalManagedObjectModel();
		conn.setTaskObject(workTaskObject);
		conn.setExternalManagedObject(externalManagedObject);

		// Return the change
		return new AbstractChange<WorkTaskObjectToExternalManagedObjectModel>(
				conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<WorkTaskObjectToExternalManagedObjectModel> removeWorkTaskObjectToExternalManagedObject(
			final WorkTaskObjectToExternalManagedObjectModel objectToExternalManagedObject) {

		// TODO test this method (removeWorkTaskObjectToExternalManagedObject)

		// Return change to remove object to external managed object
		return new AbstractChange<WorkTaskObjectToExternalManagedObjectModel>(
				objectToExternalManagedObject,
				"Remove object to external managed object") {
			@Override
			public void apply() {
				objectToExternalManagedObject.remove();
			}

			@Override
			public void revert() {
				objectToExternalManagedObject.connect();
			}
		};
	}

	@Override
	public Change<WorkTaskObjectToDeskManagedObjectModel> linkWorkTaskObjectToDeskManagedObject(
			WorkTaskObjectModel workTaskObject,
			DeskManagedObjectModel managedObject) {

		// TODO test this method (linkWorkTaskObjectToDeskManagedObject)

		// Create the connection
		final WorkTaskObjectToDeskManagedObjectModel conn = new WorkTaskObjectToDeskManagedObjectModel();
		conn.setWorkTaskObject(workTaskObject);
		conn.setDeskManagedObject(managedObject);

		// Return change to add connection
		return new AbstractChange<WorkTaskObjectToDeskManagedObjectModel>(conn,
				"Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<WorkTaskObjectToDeskManagedObjectModel> removeWorkTaskObjectToDeskManagedObject(
			final WorkTaskObjectToDeskManagedObjectModel workTaskObjectToManagedObject) {

		// TODO test this method (removeWorkTaskObjectToDeskManagedObject)

		// Return change to remove connection
		return new AbstractChange<WorkTaskObjectToDeskManagedObjectModel>(
				workTaskObjectToManagedObject, "Remove") {
			@Override
			public void apply() {
				workTaskObjectToManagedObject.remove();
			}

			@Override
			public void revert() {
				workTaskObjectToManagedObject.connect();
			}
		};
	}

	@Override
	public Change<TaskFlowToTaskModel> linkTaskFlowToTask(
			TaskFlowModel taskFlow, TaskModel task,
			FlowInstigationStrategyEnum instigationStrategy) {

		// TODO test this method (linkTaskFlowToTask)

		// Create the connection
		final TaskFlowToTaskModel conn = new TaskFlowToTaskModel();
		conn.setTaskFlow(taskFlow);
		conn.setTask(task);
		conn.setLinkType(getFlowInstigationStrategyLink(instigationStrategy));

		// Return the change
		return new AbstractChange<TaskFlowToTaskModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<TaskFlowToTaskModel> removeTaskFlowToTask(
			final TaskFlowToTaskModel taskFlowToTask) {

		// TODO test this method (removeTaskFlowToTask)

		// Return the change
		return new AbstractChange<TaskFlowToTaskModel>(taskFlowToTask, "Remove") {
			@Override
			public void apply() {
				taskFlowToTask.remove();
			}

			@Override
			public void revert() {
				taskFlowToTask.connect();
			}
		};
	}

	@Override
	public Change<TaskFlowToExternalFlowModel> linkTaskFlowToExternalFlow(
			TaskFlowModel taskFlow, ExternalFlowModel externalFlow,
			FlowInstigationStrategyEnum instigationStrategy) {

		// TODO test this method (linkTaskFlowToExternalFlow)

		// Create the connection
		final TaskFlowToExternalFlowModel conn = new TaskFlowToExternalFlowModel();
		conn.setTaskFlow(taskFlow);
		conn.setExternalFlow(externalFlow);
		conn.setLinkType(getFlowInstigationStrategyLink(instigationStrategy));

		// Return the change
		return new AbstractChange<TaskFlowToExternalFlowModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<TaskFlowToExternalFlowModel> removeTaskFlowToExternalFlow(
			final TaskFlowToExternalFlowModel taskFlowToExternalFlow) {

		// TODO test this method (removeTaskFlowToExternalFlow)

		// Return the change
		return new AbstractChange<TaskFlowToExternalFlowModel>(
				taskFlowToExternalFlow, "Remove") {
			@Override
			public void apply() {
				taskFlowToExternalFlow.remove();
			}

			@Override
			public void revert() {
				taskFlowToExternalFlow.connect();
			}
		};
	}

	@Override
	public Change<TaskToNextTaskModel> linkTaskToNextTask(TaskModel task,
			TaskModel nextTask) {

		// TODO test this method (linkTaskToNextTask)

		// Create the connection
		final TaskToNextTaskModel conn = new TaskToNextTaskModel();
		conn.setPreviousTask(task);
		conn.setNextTask(nextTask);

		// Return the change
		return new AbstractChange<TaskToNextTaskModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<TaskToNextTaskModel> removeTaskToNextTask(
			final TaskToNextTaskModel taskToNextTask) {

		// TODO test this method (removeTaskToNextTaskModel)

		// Return the change
		return new AbstractChange<TaskToNextTaskModel>(taskToNextTask, "Remove") {
			@Override
			public void apply() {
				taskToNextTask.remove();
			}

			@Override
			public void revert() {
				taskToNextTask.connect();
			}
		};
	}

	@Override
	public Change<TaskToNextExternalFlowModel> linkTaskToNextExternalFlow(
			TaskModel task, ExternalFlowModel nextExternalFlow) {

		// TODO test this method (linkTaskToNextExternalFlow)

		// Create the connection
		final TaskToNextExternalFlowModel conn = new TaskToNextExternalFlowModel();
		conn.setPreviousTask(task);
		conn.setNextExternalFlow(nextExternalFlow);

		// Return the change
		return new AbstractChange<TaskToNextExternalFlowModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<TaskToNextExternalFlowModel> removeTaskToNextExternalFlow(
			final TaskToNextExternalFlowModel taskToNextExternalFlow) {

		// TODO test this method (removeTaskToNextExternalFlow)

		// Return the change
		return new AbstractChange<TaskToNextExternalFlowModel>(
				taskToNextExternalFlow, "Remove") {
			@Override
			public void apply() {
				taskToNextExternalFlow.remove();
			}

			@Override
			public void revert() {
				taskToNextExternalFlow.connect();
			}
		};
	}

	@Override
	public Change<TaskEscalationToTaskModel> linkTaskEscalationToTask(
			TaskEscalationModel taskEscalation, TaskModel task) {

		// TODO test this method (linkTaskEscalationToTask)

		// Create the connection
		final TaskEscalationToTaskModel conn = new TaskEscalationToTaskModel();
		conn.setEscalation(taskEscalation);
		conn.setTask(task);

		// Return the change
		return new AbstractChange<TaskEscalationToTaskModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<TaskEscalationToTaskModel> removeTaskEscalationToTask(
			final TaskEscalationToTaskModel taskEscalationToTask) {

		// TODO test this method (removeTaskEscalationToTask)

		// Return the change
		return new AbstractChange<TaskEscalationToTaskModel>(
				taskEscalationToTask, "Remove") {
			@Override
			public void apply() {
				taskEscalationToTask.remove();
			}

			@Override
			public void revert() {
				taskEscalationToTask.connect();
			}
		};
	}

	@Override
	public Change<TaskEscalationToExternalFlowModel> linkTaskEscalationToExternalFlow(
			TaskEscalationModel taskEscalation, ExternalFlowModel externalFlow) {

		// TODO test this method (linkTaskEscalationToExternalFlow)

		// Create the connection
		final TaskEscalationToExternalFlowModel conn = new TaskEscalationToExternalFlowModel();
		conn.setTaskEscalation(taskEscalation);
		conn.setExternalFlow(externalFlow);

		// Return the change
		return new AbstractChange<TaskEscalationToExternalFlowModel>(conn,
				"Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<TaskEscalationToExternalFlowModel> removeTaskEscalationToExternalFlow(
			final TaskEscalationToExternalFlowModel taskEscalationToExternalFlow) {

		// TODO test this method (removeTaskEscalationToExternalFlow)

		// Return the change
		return new AbstractChange<TaskEscalationToExternalFlowModel>(
				taskEscalationToExternalFlow, "Remove") {
			@Override
			public void apply() {
				taskEscalationToExternalFlow.remove();
			}

			@Override
			public void revert() {
				taskEscalationToExternalFlow.connect();
			}
		};
	}

	@Override
	public Change<WorkToInitialTaskModel> linkWorkToInitialTask(WorkModel work,
			TaskModel initialTask) {

		// TODO test this method (linkWorkToInitialTask)

		// Create the connection
		final WorkToInitialTaskModel conn = new WorkToInitialTaskModel();
		conn.setWork(work);
		conn.setInitialTask(initialTask);

		// Return the change
		return new AbstractChange<WorkToInitialTaskModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<WorkToInitialTaskModel> removeWorkToInitialTask(
			final WorkToInitialTaskModel workToInitialTask) {

		// TODO test this method (removeWorkToInitialTask)

		// Return the change
		return new AbstractChange<WorkToInitialTaskModel>(workToInitialTask,
				"Remove") {
			@Override
			public void apply() {
				workToInitialTask.remove();
			}

			@Override
			public void revert() {
				workToInitialTask.connect();
			}
		};
	}

	@Override
	public Change<DeskManagedObjectSourceFlowToTaskModel> linkDeskManagedObjectSourceFlowToTask(
			DeskManagedObjectSourceFlowModel managedObjectSourceFlow,
			TaskModel task) {

		// TODO test this method (linkDeskManagedObjectSourceFlowToTask)

		// Create the connection
		final DeskManagedObjectSourceFlowToTaskModel conn = new DeskManagedObjectSourceFlowToTaskModel();
		conn.setDeskManagedObjectSourceFlow(managedObjectSourceFlow);
		conn.setTask(task);

		// Return change to add connection
		return new AbstractChange<DeskManagedObjectSourceFlowToTaskModel>(conn,
				"Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<DeskManagedObjectSourceFlowToTaskModel> removeDeskManagedObjectSourceFlowToTask(
			final DeskManagedObjectSourceFlowToTaskModel managedObjectSourceFlowToTask) {

		// TODO test this method (removeDeskManagedObjectSourceFlowToTask)

		// Return change to remove connection
		return new AbstractChange<DeskManagedObjectSourceFlowToTaskModel>(
				managedObjectSourceFlowToTask, "Remove") {
			@Override
			public void apply() {
				managedObjectSourceFlowToTask.remove();
			}

			@Override
			public void revert() {
				managedObjectSourceFlowToTask.connect();
			}
		};
	}

	@Override
	public Change<DeskManagedObjectSourceFlowToExternalFlowModel> linkDeskManagedObjectSourceFlowToExternalFlow(
			DeskManagedObjectSourceFlowModel managedObjectSourceFlow,
			ExternalFlowModel externalFlow) {

		// TODO test this method (linkDeskManagedObjectSourceFlowToExternalFlow)

		// Create the connection
		final DeskManagedObjectSourceFlowToExternalFlowModel conn = new DeskManagedObjectSourceFlowToExternalFlowModel();
		conn.setDeskManagedObjectSourceFlow(managedObjectSourceFlow);
		conn.setExternalFlow(externalFlow);

		// Return change to add connection
		return new AbstractChange<DeskManagedObjectSourceFlowToExternalFlowModel>(
				conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<DeskManagedObjectSourceFlowToExternalFlowModel> removeDeskManagedObjectSourceFlowToExternalFlow(
			final DeskManagedObjectSourceFlowToExternalFlowModel managedObjectSourceFlowToExternalFlow) {

		// TODO test (removeDeskManagedObjectSourceFlowToExternalFlow)

		// Return change to remove connection
		return new AbstractChange<DeskManagedObjectSourceFlowToExternalFlowModel>(
				managedObjectSourceFlowToExternalFlow, "Remove") {
			@Override
			public void apply() {
				managedObjectSourceFlowToExternalFlow.remove();
			}

			@Override
			public void revert() {
				managedObjectSourceFlowToExternalFlow.connect();
			}
		};
	}

	@Override
	public Change<DeskManagedObjectDependencyToDeskManagedObjectModel> linkDeskManagedObjectDependencyToDeskManagedObject(
			DeskManagedObjectDependencyModel dependency,
			DeskManagedObjectModel managedObject) {

		// TODO test (linkDeskManagedObjectDependencyToDeskManagedObject)

		// Create the connection
		final DeskManagedObjectDependencyToDeskManagedObjectModel conn = new DeskManagedObjectDependencyToDeskManagedObjectModel();
		conn.setDeskManagedObjectDependency(dependency);
		conn.setDeskManagedObject(managedObject);

		// Return change to add connection
		return new AbstractChange<DeskManagedObjectDependencyToDeskManagedObjectModel>(
				conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<DeskManagedObjectDependencyToDeskManagedObjectModel> removeDeskManagedObjectDependencyToDeskManagedObject(
			final DeskManagedObjectDependencyToDeskManagedObjectModel dependencyToManagedObject) {

		// TODO test (removeDeskManagedObjectDependencyToDeskManagedObject)

		// Return change to remove connection
		return new AbstractChange<DeskManagedObjectDependencyToDeskManagedObjectModel>(
				dependencyToManagedObject, "Remove") {
			@Override
			public void apply() {
				dependencyToManagedObject.remove();
			}

			@Override
			public void revert() {
				dependencyToManagedObject.connect();
			}
		};
	}

	@Override
	public Change<DeskManagedObjectDependencyToExternalManagedObjectModel> linkDeskManagedObjectDependencyToExternalManagedObject(
			DeskManagedObjectDependencyModel dependency,
			ExternalManagedObjectModel externalManagedObject) {

		// TODO test (linkDeskManagedObjectDependencyToExternalManagedObject)

		// Create the connection
		final DeskManagedObjectDependencyToExternalManagedObjectModel conn = new DeskManagedObjectDependencyToExternalManagedObjectModel();
		conn.setDeskManagedObjectDependency(dependency);
		conn.setExternalManagedObject(externalManagedObject);

		// Return change to add connection
		return new AbstractChange<DeskManagedObjectDependencyToExternalManagedObjectModel>(
				conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<DeskManagedObjectDependencyToExternalManagedObjectModel> removeDeskManagedObjectDependencyToExternalManagedObject(
			final DeskManagedObjectDependencyToExternalManagedObjectModel dependencyToExternalManagedObject) {

		// TODO test (removeDeskManagedObjectDependencyToExternalManagedObject)

		// Return change to remove connection
		return new AbstractChange<DeskManagedObjectDependencyToExternalManagedObjectModel>(
				dependencyToExternalManagedObject, "Remove") {
			@Override
			public void apply() {
				dependencyToExternalManagedObject.remove();
			}

			@Override
			public void revert() {
				dependencyToExternalManagedObject.connect();
			}
		};
	}

}