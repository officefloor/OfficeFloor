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
package net.officefloor.model.impl.desk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskChanges;
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
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.NoChange;

/**
 * {@link DeskChanges} implementation.
 * 
 * @author Daniel
 */
public class DeskChangesImpl implements DeskChanges {

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
	 * Creates a {@link WorkTaskModel} for a {@link TaskType}.
	 * 
	 * @param taskType
	 *            {@link TaskType}.
	 * @return {@link WorkTaskModel} for the {@link TaskType}.
	 */
	private WorkTaskModel createWorkTaskModel(TaskType<?, ?, ?> taskType) {

		// Create the work task model
		WorkTaskModel workTask = new WorkTaskModel(taskType.getTaskName());

		// Add the task object models
		for (TaskObjectType<?> taskObjectType : taskType.getObjectTypes()) {
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
			TaskFlowToTaskModel connTask = flow.getTask();
			if (connTask != null) {
				connTask.remove();
				connectionList.add(connTask);
			}
			TaskFlowToExternalFlowModel connExtFlow = flow.getExternalFlow();
			if (connExtFlow != null) {
				connExtFlow.remove();
				connectionList.add(connExtFlow);
			}
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
			TaskEscalationToTaskModel connTask = escalation.getTask();
			if (connTask != null) {
				connTask.remove();
				connectionList.add(connTask);
			}
			TaskEscalationToExternalFlowModel connExtFlow = escalation
					.getExternalFlow();
			if (connExtFlow != null) {
				connExtFlow.remove();
				connectionList.add(connExtFlow);
			}
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
			WorkTaskObjectToExternalManagedObjectModel conn = taskObject
					.getExternalManagedObject();
			if (conn != null) {
				conn.remove();
				connectionList.add(conn);
			}
		}

		// Remove task connections (copy to stop concurrent)
		for (WorkTaskToTaskModel taskConn : new ArrayList<WorkTaskToTaskModel>(
				workTask.getTasks())) {
			TaskModel task = taskConn.getTask();
			this.removeTaskConnections(task, connectionList);
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
			WorkType<W> workType, String... taskNames) {

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
		for (TaskType<?, ?, ?> taskType : workType.getTaskTypes()) {

			// Determine if include the task type
			String taskName = taskType.getTaskName();
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
	public <W extends Work> Change<WorkModel> conformWork(WorkModel workModel,
			WorkType<W> workType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.conformWork");
	}

	@Override
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> addWorkTask(
			final WorkModel workModel, TaskType<W, D, F> taskType) {

		// Ensure the work task is not already added
		String taskName = taskType.getTaskName();
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
			TaskType<W, D, F> taskType) {

		// Create the task model
		Class<?> returnType = taskType.getReturnType();
		final TaskModel task = new TaskModel(taskName, false, null, workTask
				.getWorkTaskName(), (returnType != null ? returnType.getName()
				: null));
		for (TaskFlowType<?> flowType : taskType.getFlowTypes()) {
			Enum<?> key = flowType.getKey();
			Class<?> argumentType = flowType.getArgumentType();
			TaskFlowModel taskFlow = new TaskFlowModel(flowType.getFlowName(),
					(key != null ? key.name() : null),
					(argumentType != null ? argumentType.getName() : null));
			task.addTaskFlow(taskFlow);
		}
		for (TaskEscalationType escalationType : taskType.getEscalationTypes()) {
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
		if (!workTask.getWorkTaskName().equals(taskType.getTaskName())) {
			// Not correct task type for the work task
			return new NoChange<TaskModel>(task, "Add task " + taskName,
					"Task type " + taskType.getTaskName()
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
				workTaskConn.remove();
				connList.add(workTaskConn);

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
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> conformTask(
			WorkTaskModel taskModel, TaskType<W, D, F> taskType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.conformTask");
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
	public Change<TaskFlowToTaskModel> linkTaskFlowToTask(
			TaskFlowModel taskFlow, TaskModel task,
			FlowInstigationStrategyEnum instigationStrategy) {

		// TODO test this method (linkTaskFlowToTask)

		// Create the connection
		final TaskFlowToTaskModel conn = new TaskFlowToTaskModel();
		conn.setTaskFlow(taskFlow);
		conn.setTask(task);
		switch (instigationStrategy) {
		case SEQUENTIAL:
			conn.setLinkType(DeskChanges.SEQUENTIAL_LINK);
			break;
		case PARALLEL:
			conn.setLinkType(DeskChanges.PARALLEL_LINK);
			break;
		case ASYNCHRONOUS:
			conn.setLinkType(DeskChanges.ASYNCHRONOUS_LINK);
			break;
		default:
			throw new IllegalStateException("Unknown instigation strategy "
					+ instigationStrategy);
		}

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

}