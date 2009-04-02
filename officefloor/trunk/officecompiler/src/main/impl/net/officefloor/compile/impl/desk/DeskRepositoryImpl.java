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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.desk.DeskRepository;
import net.officefloor.model.desk.DeskModel;
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
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.util.DoubleKeyMap;

/**
 * {@link DeskRepository} implementation.
 * 
 * @author Daniel
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

		// Create the set of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel flow : desk.getExternalFlows()) {
			externalFlows.put(flow.getExternalFlowName(), flow);
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