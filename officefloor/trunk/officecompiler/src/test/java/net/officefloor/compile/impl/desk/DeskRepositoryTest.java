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

import java.sql.Connection;

import org.easymock.AbstractMatcher;

import net.officefloor.compile.desk.DeskOperations;
import net.officefloor.compile.desk.DeskRepository;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
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
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the {@link DeskRepository}.
 * 
 * @author Daniel
 */
public class DeskRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this
			.createMock(ModelRepository.class);

	/**
	 * {@link ConfigurationItem}.
	 */
	private final ConfigurationItem configurationItem = this
			.createMock(ConfigurationItem.class);

	/**
	 * {@link DeskRepository} to be tested.
	 */
	private final DeskRepositoryImpl deskRepository = new DeskRepositoryImpl(
			this.modelRepository);

	/**
	 * Ensures on retrieving a {@link DeskModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveDesk() throws Exception {

		// Create the raw desk to be connected
		DeskModel desk = new DeskModel();
		WorkModel work = new WorkModel("WORK", "net.example.ExampleWorkSource");
		desk.addWork(work);
		WorkTaskModel workTask = new WorkTaskModel("WORK_TASK");
		work.addWorkTask(workTask);
		TaskModel task = new TaskModel("TASK", false, "WORK", "WORK_TASK",
				Object.class.getName());
		desk.addTask(task);

		// taskObject -> extMo
		WorkTaskObjectModel taskObject = new WorkTaskObjectModel();
		workTask.addTaskObject(taskObject);
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"taskObject - extMo", Connection.class.getName());
		desk.addExternalManagedObject(extMo);
		WorkTaskObjectToExternalManagedObjectModel objectToExtMo = new WorkTaskObjectToExternalManagedObjectModel(
				"taskObject - extMo");
		taskObject.setExternalManagedObject(objectToExtMo);

		// taskFlow -> extFlow
		TaskFlowModel taskFlow_extFlow = new TaskFlowModel();
		task.addTaskFlow(taskFlow_extFlow);
		ExternalFlowModel extFlow_taskFlow = new ExternalFlowModel(
				"taskFlow - extFlow", String.class.getName());
		desk.addExternalFlow(extFlow_taskFlow);
		TaskFlowToExternalFlowModel flowToExtFlow = new TaskFlowToExternalFlowModel(
				"taskFlow - extFlow", DeskOperations.SEQUENTIAL_LINK);
		taskFlow_extFlow.setExternalFlow(flowToExtFlow);

		// next -> extFlow
		ExternalFlowModel extFlow_next = new ExternalFlowModel(
				"next - extFlow", Integer.class.getName());
		desk.addExternalFlow(extFlow_next);
		TaskToNextExternalFlowModel nextToExtFlow = new TaskToNextExternalFlowModel(
				"next - extFlow");
		task.setNextExternalFlow(nextToExtFlow);

		// taskFlow -> task
		TaskFlowModel taskFlow_task = new TaskFlowModel();
		task.addTaskFlow(taskFlow_task);
		TaskModel task_taskFlow = new TaskModel("flow - task", false, "work",
				"work_task", Object.class.getName());
		desk.addTask(task_taskFlow);
		TaskFlowToTaskModel flowToTask = new TaskFlowToTaskModel("flow - task",
				DeskOperations.PARALLEL_LINK);
		taskFlow_task.setTask(flowToTask);

		// taskEscalation -> task
		TaskEscalationModel taskEscalation_task = new TaskEscalationModel();
		task.addTaskEscalation(taskEscalation_task);
		TaskModel task_taskEscalation = new TaskModel("escalation - task",
				false, "WORK", "WORK_TASK", Object.class.getName());
		desk.addTask(task_taskEscalation);
		TaskEscalationToTaskModel escalationToTask = new TaskEscalationToTaskModel(
				"escalation - task");
		taskEscalation_task.setTask(escalationToTask);

		// taskEscalation -> extFlow
		TaskEscalationModel taskEscalation_extFlow = new TaskEscalationModel();
		task.addTaskEscalation(taskEscalation_extFlow);
		ExternalFlowModel extFlow_taskEscalation = new ExternalFlowModel(
				"escalation - extFlow", Throwable.class.getName());
		desk.addExternalFlow(extFlow_taskEscalation);
		TaskEscalationToExternalFlowModel escalationToExtFlow = new TaskEscalationToExternalFlowModel(
				"escalation - extFlow");
		taskEscalation_extFlow.setExternalFlow(escalationToExtFlow);

		// work -> initial task
		WorkToInitialTaskModel workToInitialTask = new WorkToInitialTaskModel(
				"TASK");
		work.setInitialTask(workToInitialTask);

		// next -> task
		TaskModel task_next = new TaskModel("next - task", false, "work",
				"work_task", Integer.class.getName());
		desk.addTask(task_next);
		TaskToNextTaskModel nextToTask = new TaskToNextTaskModel("next - task");
		task.setNextTask(nextToTask);

		// Record retrieving the desk
		this.recordReturn(this.modelRepository, this.modelRepository.retrieve(
				null, this.configurationItem), desk, new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertTrue("Must be desk model", actual[0] instanceof DeskModel);
				assertEquals("Incorrect configuration item",
						DeskRepositoryTest.this.configurationItem, actual[1]);
				return true;
			}
		});

		// Retrieve the desk
		this.replayMockObjects();
		DeskModel retrievedDesk = this.deskRepository
				.retrieveDesk(this.configurationItem);
		this.verifyMockObjects();
		assertEquals("Incorrect desk", desk, retrievedDesk);

		// Ensure the external managed object connected
		assertEquals("taskObject <- extMo", taskObject, objectToExtMo
				.getTaskObject());
		assertEquals("taskObject -> extMo", extMo, objectToExtMo
				.getExternalManagedObject());

		// Ensure the external flow connected
		assertEquals("taskFlow <- extFlow", taskFlow_extFlow, flowToExtFlow
				.getTaskFlow());
		assertEquals("taskFlow -> extFlow", extFlow_taskFlow, flowToExtFlow
				.getExternalFlow());

		// Ensure the next external flow connected
		assertEquals("next -> extFlow", task, nextToExtFlow.getPreviousTask());
		assertEquals("next <- extFlow", extFlow_next, nextToExtFlow
				.getNextExternalFlow());

		// Ensure flow to task connected
		assertEquals("taskFlow <- task", taskFlow_task, flowToTask
				.getTaskFlow());
		assertEquals("taskFlow -> task", task_taskFlow, flowToTask.getTask());

		// Ensure escalation to task connected
		assertEquals("taskEscalation <- task", taskEscalation_task,
				escalationToTask.getEscalation());
		assertEquals("taskEscalation -> task", task_taskEscalation,
				escalationToTask.getTask());

		// Ensure escalation to external flow connected
		assertEquals("taskEscalation <- extFlow", taskEscalation_extFlow,
				escalationToExtFlow.getTaskEscalation());
		assertEquals("taskEscalation -> extFlow", extFlow_taskEscalation,
				escalationToExtFlow.getExternalFlow());

		// Ensure work to initial task connected
		assertEquals("work <- initial task", work, workToInitialTask.getWork());
		assertEquals("work -> initial task", task, workToInitialTask
				.getInitialTask());

		// Ensure the next task connected
		assertEquals("next <- task", task, nextToTask.getPreviousTask());
		assertEquals("next -> task", task_next, nextToTask.getNextTask());

		// Ensure the tasks are connected to their work tasks
		assertEquals("task <- workTask", workTask, task.getTask().getWorkTask());
		assertEquals("task -> workTask", task, workTask.getTasks().get(0)
				.getTask());
	}

	/**
	 * Ensures on storing a {@link DeskModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreDesk() throws Exception {

		// Create the desk (without connections)
		DeskModel desk = new DeskModel();
		WorkModel work = new WorkModel("WORK", "net.example.ExampleWorkSource");
		desk.addWork(work);
		WorkTaskModel workTask = new WorkTaskModel("WORK_TASK");
		work.addWorkTask(workTask);
		WorkTaskObjectModel taskObject = new WorkTaskObjectModel("OBJECT",
				null, Object.class.getName(), false);
		workTask.addTaskObject(taskObject);
		TaskModel task = new TaskModel("TASK", false, "WORK", "WORK_TASK",
				Object.class.getName());
		desk.addTask(task);
		TaskFlowModel taskFlow = new TaskFlowModel("FLOW", null, String.class
				.getName());
		task.addTaskFlow(taskFlow);
		TaskEscalationModel taskEscalation = new TaskEscalationModel(
				Exception.class.getName());
		task.addTaskEscalation(taskEscalation);
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT", Object.class.getName());
		desk.addExternalManagedObject(extMo);
		ExternalFlowModel extFlow = new ExternalFlowModel("EXTERNAL_FLOW",
				Object.class.getName());
		desk.addExternalFlow(extFlow);

		// taskObject -> extMo
		WorkTaskObjectToExternalManagedObjectModel objectToExtMo = new WorkTaskObjectToExternalManagedObjectModel();
		objectToExtMo.setTaskObject(taskObject);
		objectToExtMo.setExternalManagedObject(extMo);
		objectToExtMo.connect();

		// taskFlow -> extFlow
		TaskFlowToExternalFlowModel flowToExtFlow = new TaskFlowToExternalFlowModel();
		flowToExtFlow.setTaskFlow(taskFlow);
		flowToExtFlow.setExternalFlow(extFlow);
		flowToExtFlow.connect();

		// next -> extFlow
		TaskToNextExternalFlowModel nextToExtFlow = new TaskToNextExternalFlowModel();
		nextToExtFlow.setPreviousTask(task);
		nextToExtFlow.setNextExternalFlow(extFlow);
		nextToExtFlow.connect();

		// taskFlow -> task
		TaskFlowToTaskModel flowToTask = new TaskFlowToTaskModel();
		flowToTask.setTaskFlow(taskFlow);
		flowToTask.setTask(task);
		flowToTask.connect();

		// taskEscalation -> task
		TaskEscalationToTaskModel escalationToTask = new TaskEscalationToTaskModel();
		escalationToTask.setEscalation(taskEscalation);
		escalationToTask.setTask(task);
		escalationToTask.connect();

		// taskEscalation -> extFlow
		TaskEscalationToExternalFlowModel escalationToExtFlow = new TaskEscalationToExternalFlowModel();
		escalationToExtFlow.setTaskEscalation(taskEscalation);
		escalationToExtFlow.setExternalFlow(extFlow);
		escalationToExtFlow.connect();

		// work -> initial task
		WorkToInitialTaskModel workToInitialTask = new WorkToInitialTaskModel();
		workToInitialTask.setWork(work);
		workToInitialTask.setInitialTask(task);
		workToInitialTask.connect();

		// next -> task
		TaskToNextTaskModel nextToTask = new TaskToNextTaskModel();
		nextToTask.setPreviousTask(task);
		nextToTask.setNextTask(task);
		nextToTask.connect();

		// Record storing the desk
		this.modelRepository.store(desk, this.configurationItem);

		// Store the desk
		this.replayMockObjects();
		this.deskRepository.storeDesk(desk, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("taskObject - extMo", "EXTERNAL_MANAGED_OBJECT",
				objectToExtMo.getExternalManagedObjectName());
		assertEquals("taskFlow - extFlow", "EXTERNAL_FLOW", flowToExtFlow
				.getExternalFlowName());
		assertEquals("next - extFlow", "EXTERNAL_FLOW", nextToExtFlow
				.getExternalFlowName());
		assertEquals("flow - task", "TASK", flowToTask.getTaskName());
		assertEquals("escalation - task", "TASK", escalationToTask
				.getTaskName());
		assertEquals("escalation - extFlow", "EXTERNAL_FLOW",
				escalationToExtFlow.getExternalFlowName());
		assertEquals("work - initial task", "TASK", workToInitialTask
				.getInitialTaskName());
		assertEquals("next - task", "TASK", nextToTask.getNextTaskName());
	}
}