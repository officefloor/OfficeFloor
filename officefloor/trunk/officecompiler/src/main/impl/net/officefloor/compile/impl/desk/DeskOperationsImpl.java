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
package net.officefloor.compile.impl.desk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.change.Change;
import net.officefloor.compile.desk.DeskOperations;
import net.officefloor.compile.impl.change.AbstractChange;
import net.officefloor.compile.impl.change.NoChange;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.TaskObjectType;
import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.ConnectionModel;
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
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;

/**
 * {@link DeskOperations} implementation.
 * 
 * @author Daniel
 */
public class DeskOperationsImpl implements DeskOperations {

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
	 * {@link DeskModel}.
	 */
	private final DeskModel desk;

	/**
	 * Initiate.
	 * 
	 * @param desk
	 *            {@link DeskModel}.
	 */
	public DeskOperationsImpl(DeskModel desk) {
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
		for (Property property : properties.getPropertyList()) {
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

			// Add the work task model
			WorkTaskModel workTask = new WorkTaskModel(taskName);
			work.addWorkTask(workTask);

			// Add the task object models
			for (TaskObjectType<?> taskObjectType : taskType.getObjectTypes()) {
				Enum<?> key = taskObjectType.getKey();
				WorkTaskObjectModel taskObject = new WorkTaskObjectModel(
						taskObjectType.getObjectName(), (key == null ? null
								: key.name()), taskObjectType.getObjectType()
								.getName(), false);
				workTask.addTaskObject(taskObject);
			}
		}

		// Ensure work task models in sorted order
		DeskOperationsImpl.sortWorkTaskModels(work.getWorkTasks());

		// Return the change to add the work
		return new AbstractChange<WorkModel>(work, "Add work " + workName) {
			@Override
			public void apply() {
				// Add the work (ensuring in sorted order)
				DeskOperationsImpl.this.desk.addWork(work);
				DeskOperationsImpl.this.sortWorkModels();
			}

			@Override
			public void revert() {
				DeskOperationsImpl.this.desk.removeWork(work);
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

					// Remove object connections
					for (WorkTaskObjectModel taskObject : workTask
							.getTaskObjects()) {
						WorkTaskObjectToExternalManagedObjectModel conn = taskObject
								.getExternalManagedObject();
						if (conn != null) {
							conn.remove();
							connectionList.add(conn);
						}
					}

					// Remove task connections
					for (WorkTaskToTaskModel taskConn : workTask.getTasks()) {
						TaskModel task = taskConn.getTask();

						// Remove input connections (copy to stop concurrent)
						for (TaskToNextTaskModel conn : new ArrayList<TaskToNextTaskModel>(
								task.getPreviousTasks())) {
							conn.remove();
							connectionList.add(conn);
						}
						for (TaskFlowToTaskModel conn : new ArrayList<TaskFlowToTaskModel>(
								task.getTaskFlowInputs())) {
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
							TaskFlowToExternalFlowModel connExtFlow = flow
									.getExternalFlow();
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
						for (TaskEscalationModel escalation : task
								.getTaskEscalations()) {
							TaskEscalationToTaskModel connTask = escalation
									.getTask();
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
				}
				this.connections = connectionList
						.toArray(new ConnectionModel[0]);

				// Remove the associated tasks (storing for revert)
				List<TaskModel> taskList = new LinkedList<TaskModel>();
				for (WorkTaskModel workTask : workModel.getWorkTasks()) {
					for (WorkTaskToTaskModel conn : workTask.getTasks()) {
						TaskModel task = conn.getTask();

						// Remove task and store for revert
						DeskOperationsImpl.this.desk.removeTask(task);
						taskList.add(task);
					}
				}
				this.tasks = taskList.toArray(new TaskModel[0]);

				// Remove the work
				DeskOperationsImpl.this.desk.removeWork(workModel);
			}

			@Override
			public void revert() {
				// Add the work (ensuring in sorted order)
				DeskOperationsImpl.this.desk.addWork(workModel);
				DeskOperationsImpl.this.sortWorkModels();

				// Add the tasks (in reverse order, ensuring sorted)
				for (int i = (this.tasks.length - 1); i >= 0; i--) {
					DeskOperationsImpl.this.desk.addTask(this.tasks[i]);
				}
				DeskOperationsImpl.this.sortTaskModels();

				// Reconnect connections
				for (ConnectionModel connection : this.connections) {
					connection.connect();
				}
			}
		};
	}

	@Override
	public Change<ExternalFlowModel> addExternalFlow(String externalFlowName,
			String argumentType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.addExternalFlow");
	}

	@Override
	public Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String argumentType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.addExternalManagedObject");
	}

	@Override
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<TaskModel> addTask(
			String taskName, WorkTaskModel workTaskModel,
			TaskType<W, D, F> taskType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.addTask");
	}

	@Override
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> addWorkTask(
			WorkModel workModel, TaskType<W, D, F> taskType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.addWorkTask");
	}

	@Override
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> conformTask(
			WorkTaskModel taskModel, TaskType<W, D, F> taskType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.conformTask");
	}

	@Override
	public <W extends Work> Change<WorkModel> conformWork(WorkModel workModel,
			WorkType<W> workType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.conformWork");
	}

	@Override
	public Change<ExternalFlowModel> removeExternalFlow(
			ExternalFlowModel externalFlow) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.removeExternalFlow");
	}

	@Override
	public Change<ExternalManagedObjectModel> removeExternalManagedObject(
			ExternalManagedObjectModel externalManagedObject) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.removeExternalManagedObject");
	}

	@Override
	public Change<TaskModel> removeTask(TaskModel taskModel) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.removeTask");
	}

	@Override
	public Change<WorkTaskModel> removeWorkTask(WorkModel workModel,
			WorkTaskModel taskModel) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.removeWorkTask");
	}

	@Override
	public Change<TaskModel> renameTask(TaskModel taskModel, String newTaskName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.renameTask");
	}

	@Override
	public Change<WorkModel> renameWork(WorkModel workModel, String newWorkName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.renameWork");
	}

	@Override
	public Change<WorkTaskModel> setObjectAsParameter(boolean isParameter,
			String objectName, WorkTaskModel workTaskModel) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.setObjectAsParameter");
	}

	@Override
	public Change<TaskModel> setTaskAsPublic(boolean isPublic,
			TaskModel taskModel) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.setTaskAsPublic");
	}

}