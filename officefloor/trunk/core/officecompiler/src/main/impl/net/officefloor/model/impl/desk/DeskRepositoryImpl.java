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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.DoubleKeyMap;
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
import net.officefloor.model.desk.DeskRepository;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
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
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link DeskRepository} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class DeskRepositoryImpl implements DeskRepository {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Initiate.
	 *
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public DeskRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ================== DeskRepository ==========================
	 */

	@Override
	public DeskModel retrieveDesk(ConfigurationItem configuration)
			throws Exception {

		// Load the desk from the configuration
		DeskModel desk = this.modelRepository.retrieve(new DeskModel(),
				configuration);

		// Create the set of managed object sources
		Map<String, DeskManagedObjectSourceModel> managedObjectSources = new HashMap<String, DeskManagedObjectSourceModel>();
		for (DeskManagedObjectSourceModel mos : desk
				.getDeskManagedObjectSources()) {
			managedObjectSources.put(mos.getDeskManagedObjectSourceName(), mos);
		}

		// Connect the managed objects to their managed object sources
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			DeskManagedObjectToDeskManagedObjectSourceModel conn = mo
					.getDeskManagedObjectSource();
			if (conn != null) {
				DeskManagedObjectSourceModel mos = managedObjectSources
						.get(conn.getDeskManagedObjectSourceName());
				if (mos != null) {
					conn.setDeskManagedObject(mo);
					conn.setDeskManagedObjectSource(mos);
					conn.connect();
				}
			}
		}

		// Create the set of external managed objects
		Map<String, ExternalManagedObjectModel> externalManagedObjects = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel mo : desk.getExternalManagedObjects()) {
			externalManagedObjects.put(mo.getExternalManagedObjectName(), mo);
		}

		// Connect the task objects to external managed objects
		for (WorkModel work : desk.getWorks()) {
			for (WorkTaskModel task : work.getWorkTasks()) {
				for (WorkTaskObjectModel taskObject : task.getTaskObjects()) {
					// Obtain the connection
					WorkTaskObjectToExternalManagedObjectModel conn = taskObject
							.getExternalManagedObject();
					if (conn != null) {
						// Obtain the external managed object
						ExternalManagedObjectModel extMo = externalManagedObjects
								.get(conn.getExternalManagedObjectName());
						if (extMo != null) {
							// Connect
							conn.setTaskObject(taskObject);
							conn.setExternalManagedObject(extMo);
							conn.connect();
						}
					}
				}
			}
		}

		// Create the set of managed objects
		Map<String, DeskManagedObjectModel> managedObjects = new HashMap<String, DeskManagedObjectModel>();
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			managedObjects.put(mo.getDeskManagedObjectName(), mo);
		}

		// Connect the task objects to managed objects
		for (WorkModel work : desk.getWorks()) {
			for (WorkTaskModel task : work.getWorkTasks()) {
				for (WorkTaskObjectModel object : task.getTaskObjects()) {
					WorkTaskObjectToDeskManagedObjectModel conn = object
							.getDeskManagedObject();
					if (conn != null) {
						DeskManagedObjectModel mo = managedObjects.get(conn
								.getDeskManagedObjectName());
						if (mo != null) {
							conn.setWorkTaskObject(object);
							conn.setDeskManagedObject(mo);
							conn.connect();
						}
					}
				}
			}
		}

		// Connect the dependencies to external managed objects
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			for (DeskManagedObjectDependencyModel dependency : mo
					.getDeskManagedObjectDependencies()) {
				DeskManagedObjectDependencyToExternalManagedObjectModel conn = dependency
						.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = externalManagedObjects
							.get(conn.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setDeskManagedObjectDependency(dependency);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Connect the dependencies to managed objects
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			for (DeskManagedObjectDependencyModel dependency : mo
					.getDeskManagedObjectDependencies()) {
				DeskManagedObjectDependencyToDeskManagedObjectModel conn = dependency
						.getDeskManagedObject();
				if (conn != null) {
					DeskManagedObjectModel dependentMo = managedObjects
							.get(conn.getDeskManagedObjectName());
					if (dependentMo != null) {
						conn.setDeskManagedObjectDependency(dependency);
						conn.setDeskManagedObject(dependentMo);
						conn.connect();
					}
				}
			}
		}

		// Create the set of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel flow : desk.getExternalFlows()) {
			externalFlows.put(flow.getExternalFlowName(), flow);
		}

		// Connect the managed object source flows to external flows
		for (DeskManagedObjectSourceModel mos : desk
				.getDeskManagedObjectSources()) {
			for (DeskManagedObjectSourceFlowModel mosFlow : mos
					.getDeskManagedObjectSourceFlows()) {
				DeskManagedObjectSourceFlowToExternalFlowModel conn = mosFlow
						.getExternalFlow();
				if (conn != null) {
					ExternalFlowModel extFlow = externalFlows.get(conn
							.getExternalFlowName());
					if (extFlow != null) {
						conn.setDeskManagedObjectSourceFlow(mosFlow);
						conn.setExternalFlow(extFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the task flows to external flow
		for (TaskModel task : desk.getTasks()) {
			for (TaskFlowModel flow : task.getTaskFlows()) {
				// Obtain the connection
				TaskFlowToExternalFlowModel conn = flow.getExternalFlow();
				if (conn != null) {
					// Obtain the external flow
					ExternalFlowModel extFlow = externalFlows.get(conn
							.getExternalFlowName());
					if (extFlow != null) {
						// Connect
						conn.setTaskFlow(flow);
						conn.setExternalFlow(extFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the tasks to next external flow
		for (TaskModel task : desk.getTasks()) {
			// Obtain the connection
			TaskToNextExternalFlowModel conn = task.getNextExternalFlow();
			if (conn != null) {
				// Obtain the external flow
				ExternalFlowModel extFlow = externalFlows.get(conn
						.getExternalFlowName());
				if (extFlow != null) {
					// Connect
					conn.setPreviousTask(task);
					conn.setNextExternalFlow(extFlow);
					conn.connect();
				}
			}
		}

		// Create the set of tasks
		Map<String, TaskModel> tasks = new HashMap<String, TaskModel>();
		for (TaskModel task : desk.getTasks()) {
			tasks.put(task.getTaskName(), task);
		}

		// Connect the managed object source flows to tasks
		for (DeskManagedObjectSourceModel mos : desk
				.getDeskManagedObjectSources()) {
			for (DeskManagedObjectSourceFlowModel mosFlow : mos
					.getDeskManagedObjectSourceFlows()) {
				DeskManagedObjectSourceFlowToTaskModel conn = mosFlow.getTask();
				if (conn != null) {
					TaskModel task = tasks.get(conn.getTaskName());
					if (task != null) {
						conn.setDeskManagedObjectSourceFlow(mosFlow);
						conn.setTask(task);
						conn.connect();
					}
				}
			}
		}

		// Connect the task flows to tasks
		for (TaskModel task : desk.getTasks()) {
			for (TaskFlowModel flow : task.getTaskFlows()) {
				// Obtain the connection
				TaskFlowToTaskModel conn = flow.getTask();
				if (conn != null) {
					// Obtain the task
					TaskModel flowTask = tasks.get(conn.getTaskName());
					if (flowTask != null) {
						// Connect
						conn.setTaskFlow(flow);
						conn.setTask(flowTask);
						conn.connect();
					}
				}
			}
		}

		// Connect the escalation to tasks
		for (TaskModel task : desk.getTasks()) {
			for (TaskEscalationModel escalation : task.getTaskEscalations()) {
				// Obtain the connection
				TaskEscalationToTaskModel conn = escalation.getTask();
				if (conn != null) {
					// Obtain the handling task
					TaskModel escalationTask = tasks.get(conn.getTaskName());
					if (escalationTask != null) {
						// Connect
						conn.setEscalation(escalation);
						conn.setTask(escalationTask);
						conn.connect();
					}
				}
			}
		}

		// Connect the escalation to external flows
		for (TaskModel task : desk.getTasks()) {
			for (TaskEscalationModel escalation : task.getTaskEscalations()) {
				// Obtain the connection
				TaskEscalationToExternalFlowModel conn = escalation
						.getExternalFlow();
				if (conn != null) {
					// Obtain the external flow
					ExternalFlowModel externalFlow = externalFlows.get(conn
							.getExternalFlowName());
					if (externalFlow != null) {
						// Connect
						conn.setTaskEscalation(escalation);
						conn.setExternalFlow(externalFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the work to initial task
		for (WorkModel work : desk.getWorks()) {
			// Obtain the connection
			WorkToInitialTaskModel conn = work.getInitialTask();
			if (conn != null) {
				// Obtain the initial task
				TaskModel task = tasks.get(conn.getInitialTaskName());
				if (task != null) {
					// Connect
					conn.setWork(work);
					conn.setInitialTask(task);
					conn.connect();
				}
			}
		}

		// Connect the task to its next task
		for (TaskModel previous : desk.getTasks()) {
			// Obtain the connection
			TaskToNextTaskModel conn = previous.getNextTask();
			if (conn != null) {
				// Obtain the next task
				TaskModel next = tasks.get(conn.getNextTaskName());
				if (next != null) {
					// Connect
					conn.setPreviousTask(previous);
					conn.setNextTask(next);
					conn.connect();
				}
			}
		}

		// Create the set of tasks
		DoubleKeyMap<String, String, WorkTaskModel> taskRegistry = new DoubleKeyMap<String, String, WorkTaskModel>();
		for (WorkModel deskWork : desk.getWorks()) {
			for (WorkTaskModel deskTask : deskWork.getWorkTasks()) {
				taskRegistry.put(deskWork.getWorkName(), deskTask
						.getWorkTaskName(), deskTask);
			}
		}

		// Connect the tasks to their work task
		for (TaskModel task : desk.getTasks()) {
			// Obtain the work task
			WorkTaskModel workTask = taskRegistry.get(task.getWorkName(), task
					.getWorkTaskName());
			if (workTask != null) {
				// Connect
				new WorkTaskToTaskModel(task, workTask).connect();
			}
		}

		// Return the desk
		return desk;
	}

	@Override
	public void storeDesk(DeskModel desk, ConfigurationItem configuration)
			throws Exception {

		// Specify managed object to its managed object source
		for (DeskManagedObjectSourceModel mos : desk
				.getDeskManagedObjectSources()) {
			for (DeskManagedObjectToDeskManagedObjectSourceModel conn : mos
					.getDeskManagedObjects()) {
				conn.setDeskManagedObjectSourceName(mos
						.getDeskManagedObjectSourceName());
			}
		}

		// Specify managed object source flow to its external flow
		for (ExternalFlowModel extFlow : desk.getExternalFlows()) {
			for (DeskManagedObjectSourceFlowToExternalFlowModel conn : extFlow
					.getDeskManagedObjectSourceFlows()) {
				conn.setExternalFlowName(extFlow.getExternalFlowName());
			}
		}

		// Specify managed object source flow to its task
		for (TaskModel task : desk.getTasks()) {
			for (DeskManagedObjectSourceFlowToTaskModel conn : task
					.getDeskManagedObjectSourceFlows()) {
				conn.setTaskName(task.getTaskName());
			}
		}

		// Specify task object to external managed object
		for (WorkModel work : desk.getWorks()) {
			for (WorkTaskModel workTask : work.getWorkTasks()) {
				for (WorkTaskObjectModel taskObject : workTask.getTaskObjects()) {
					WorkTaskObjectToExternalManagedObjectModel conn = taskObject
							.getExternalManagedObject();
					if (conn != null) {
						conn.setExternalManagedObjectName(conn
								.getExternalManagedObject()
								.getExternalManagedObjectName());
					}
				}
			}
		}

		// Specify task object to managed object
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			for (WorkTaskObjectToDeskManagedObjectModel conn : mo
					.getWorkTaskObjects()) {
				conn.setDeskManagedObjectName(mo.getDeskManagedObjectName());
			}
		}

		// Specify dependency to external managed object
		for (ExternalManagedObjectModel extMo : desk
				.getExternalManagedObjects()) {
			for (DeskManagedObjectDependencyToExternalManagedObjectModel conn : extMo
					.getDependentDeskManagedObjects()) {
				conn.setExternalManagedObjectName(extMo
						.getExternalManagedObjectName());
			}
		}

		// Specify dependency to managed object
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			for (DeskManagedObjectDependencyToDeskManagedObjectModel conn : mo
					.getDependentDeskManagedObjects()) {
				conn.setDeskManagedObjectName(mo.getDeskManagedObjectName());
			}
		}

		// Specify task flow to external flow
		for (TaskModel task : desk.getTasks()) {
			for (TaskFlowModel flow : task.getTaskFlows()) {
				TaskFlowToExternalFlowModel conn = flow.getExternalFlow();
				if (conn != null) {
					conn.setExternalFlowName(conn.getExternalFlow()
							.getExternalFlowName());
				}
			}
		}

		// Specify task next to external flow
		for (TaskModel task : desk.getTasks()) {
			TaskToNextExternalFlowModel conn = task.getNextExternalFlow();
			if (conn != null) {
				conn.setExternalFlowName(conn.getNextExternalFlow()
						.getExternalFlowName());
			}
		}

		// Specify task flow to task
		for (TaskModel task : desk.getTasks()) {
			for (TaskFlowModel flow : task.getTaskFlows()) {
				TaskFlowToTaskModel conn = flow.getTask();
				if (conn != null) {
					conn.setTaskName(conn.getTask().getTaskName());
				}
			}
		}

		// Specify task escalation to task
		for (TaskModel task : desk.getTasks()) {
			for (TaskEscalationModel taskEscalation : task.getTaskEscalations()) {
				TaskEscalationToTaskModel conn = taskEscalation.getTask();
				if (conn != null) {
					conn.setTaskName(conn.getTask().getTaskName());
				}
			}
		}

		// Specify task escalation to external flow
		for (TaskModel task : desk.getTasks()) {
			for (TaskEscalationModel taskEscalation : task.getTaskEscalations()) {
				TaskEscalationToExternalFlowModel conn = taskEscalation
						.getExternalFlow();
				if (conn != null) {
					conn.setExternalFlowName(conn.getExternalFlow()
							.getExternalFlowName());
				}
			}
		}

		// Specify initial task for work
		for (WorkModel work : desk.getWorks()) {
			WorkToInitialTaskModel conn = work.getInitialTask();
			if (conn != null) {
				conn.setInitialTaskName(conn.getInitialTask().getTaskName());
			}
		}

		// Specify next tasks
		for (TaskModel task : desk.getTasks()) {
			TaskToNextTaskModel conn = task.getNextTask();
			if (conn != null) {
				conn.setNextTaskName(conn.getNextTask().getTaskName());
			}
		}

		// Stores the desk
		this.modelRepository.store(desk, configuration);
	}

}