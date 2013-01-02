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

import java.sql.Connection;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
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
import net.officefloor.model.desk.DeskChanges;
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
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.impl.desk.DeskRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the {@link DeskRepository}.
 *
 * @author Daniel Sagenschneider
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
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT", Connection.class.getName());
		desk.addExternalManagedObject(extMo);
		DeskManagedObjectSourceModel mos = new DeskManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Object.class
						.getName(), "0");
		desk.addDeskManagedObjectSource(mos);
		DeskManagedObjectSourceFlowModel mosFlow = new DeskManagedObjectSourceFlowModel(
				"MOS_FLOW", Object.class.getName());
		mos.addDeskManagedObjectSourceFlow(mosFlow);
		DeskManagedObjectModel mo = new DeskManagedObjectModel(
				"MANAGED_OBJECT", "THREAD");
		desk.addDeskManagedObject(mo);
		DeskManagedObjectDependencyModel dependency = new DeskManagedObjectDependencyModel(
				"DEPENDENCY", "THREAD");
		mo.addDeskManagedObjectDependency(dependency);
		WorkModel work = new WorkModel("WORK", "net.example.ExampleWorkSource");
		desk.addWork(work);
		WorkTaskModel workTask = new WorkTaskModel("WORK_TASK");
		work.addWorkTask(workTask);
		TaskModel task = new TaskModel("TASK", false, "WORK", "WORK_TASK",
				Object.class.getName());
		desk.addTask(task);
		WorkTaskObjectModel taskObject = new WorkTaskObjectModel();
		workTask.addTaskObject(taskObject);

		// managed object -> managed object source
		DeskManagedObjectToDeskManagedObjectSourceModel moToMos = new DeskManagedObjectToDeskManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE");
		mo.setDeskManagedObjectSource(moToMos);

		// managed object source flow -> external flow
		ExternalFlowModel extFlow_mosFlow = new ExternalFlowModel(
				"mosFlow - extFlow", String.class.getName());
		desk.addExternalFlow(extFlow_mosFlow);
		DeskManagedObjectSourceFlowToExternalFlowModel mosFlowToExtFlow = new DeskManagedObjectSourceFlowToExternalFlowModel(
				"mosFlow - extFlow");
		mosFlow.setExternalFlow(mosFlowToExtFlow);

		// managed object source flow -> task
		DeskManagedObjectSourceFlowToTaskModel mosFlowToTask = new DeskManagedObjectSourceFlowToTaskModel(
				"TASK");
		mosFlow.setTask(mosFlowToTask);

		// dependency -> external managed object
		DeskManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new DeskManagedObjectDependencyToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		dependency.setExternalManagedObject(dependencyToExtMo);

		// dependency -> managed object
		DeskManagedObjectDependencyToDeskManagedObjectModel dependencyToMo = new DeskManagedObjectDependencyToDeskManagedObjectModel(
				"MANAGED_OBJECT");
		dependency.setDeskManagedObject(dependencyToMo);

		// taskObject -> extMo
		WorkTaskObjectToExternalManagedObjectModel objectToExtMo = new WorkTaskObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		taskObject.setExternalManagedObject(objectToExtMo);

		// taskObject -> managed object
		WorkTaskObjectToDeskManagedObjectModel objectToMo = new WorkTaskObjectToDeskManagedObjectModel(
				"MANAGED_OBJECT");
		taskObject.setDeskManagedObject(objectToMo);

		// taskFlow -> extFlow
		TaskFlowModel taskFlow_extFlow = new TaskFlowModel();
		task.addTaskFlow(taskFlow_extFlow);
		ExternalFlowModel extFlow_taskFlow = new ExternalFlowModel(
				"taskFlow - extFlow", String.class.getName());
		desk.addExternalFlow(extFlow_taskFlow);
		TaskFlowToExternalFlowModel flowToExtFlow = new TaskFlowToExternalFlowModel(
				"taskFlow - extFlow", DeskChanges.SEQUENTIAL_LINK);
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
				DeskChanges.PARALLEL_LINK);
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

		// Ensure the managed object connected to its source
		assertEquals("mo <- mos", mo, moToMos.getDeskManagedObject());
		assertEquals("mo -> mos", mos, moToMos.getDeskManagedObjectSource());

		// Ensure managed object source flow connected to external flow
		assertEquals("mos flow <- external flow", mosFlow, mosFlowToExtFlow
				.getDeskManagedObjectSourceFlow());
		assertEquals("mos flow -> external flow", extFlow_mosFlow,
				mosFlowToExtFlow.getExternalFlow());

		// Ensure managed object source flow connected to task
		assertEquals("mos flow <- task", mosFlow, mosFlowToTask
				.getDeskManagedObjectSourceFlow());
		assertEquals("mos flow -> task", task, mosFlowToTask.getTask());

		// Ensure dependency connected to external managed object
		assertEquals("dependency <- external mo", dependency, dependencyToExtMo
				.getDeskManagedObjectDependency());
		assertEquals("dependency -> external mo", extMo, dependencyToExtMo
				.getExternalManagedObject());

		// Ensure dependency connected to managed object
		assertEquals("dependency <- mo", dependency, dependencyToMo
				.getDeskManagedObjectDependency());
		assertEquals("dependency -> mo", mo, dependencyToMo
				.getDeskManagedObject());

		// Ensure the external managed object connected
		assertEquals("taskObject <- extMo", taskObject, objectToExtMo
				.getTaskObject());
		assertEquals("taskObject -> extMo", extMo, objectToExtMo
				.getExternalManagedObject());

		// Ensure task object connected to managed object
		assertEquals("taskObject <- managed object", taskObject, objectToMo
				.getWorkTaskObject());
		assertEquals("taskObject -> managed object", mo, objectToMo
				.getDeskManagedObject());

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
		assertEquals("task <- workTask", workTask, task.getWorkTask()
				.getWorkTask());
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
		DeskManagedObjectSourceModel mos = new DeskManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Object.class
						.getName(), "0");
		desk.addDeskManagedObjectSource(mos);
		DeskManagedObjectSourceFlowModel mosFlow = new DeskManagedObjectSourceFlowModel(
				"MOS_FLOW", Object.class.getName());
		mos.addDeskManagedObjectSourceFlow(mosFlow);
		DeskManagedObjectModel mo = new DeskManagedObjectModel(
				"MANAGED_OBJECT", "THREAD");
		desk.addDeskManagedObject(mo);
		DeskManagedObjectDependencyModel dependency = new DeskManagedObjectDependencyModel(
				"DEPENDENCY", "THREAD");
		mo.addDeskManagedObjectDependency(dependency);
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

		// managed object -> managed object source
		DeskManagedObjectToDeskManagedObjectSourceModel moToMos = new DeskManagedObjectToDeskManagedObjectSourceModel();
		moToMos.setDeskManagedObject(mo);
		moToMos.setDeskManagedObjectSource(mos);
		moToMos.connect();

		// managed object source flow -> external flow
		DeskManagedObjectSourceFlowToExternalFlowModel mosFlowToExtFlow = new DeskManagedObjectSourceFlowToExternalFlowModel();
		mosFlowToExtFlow.setDeskManagedObjectSourceFlow(mosFlow);
		mosFlowToExtFlow.setExternalFlow(extFlow);
		mosFlowToExtFlow.connect();

		// managed object source flow -> task
		DeskManagedObjectSourceFlowToTaskModel mosFlowToTask = new DeskManagedObjectSourceFlowToTaskModel();
		mosFlowToTask.setDeskManagedObjectSourceFlow(mosFlow);
		mosFlowToTask.setTask(task);
		mosFlowToTask.connect();

		// dependency -> extMo
		DeskManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new DeskManagedObjectDependencyToExternalManagedObjectModel();
		dependencyToExtMo.setDeskManagedObjectDependency(dependency);
		dependencyToExtMo.setExternalManagedObject(extMo);
		dependencyToExtMo.connect();

		// dependency -> mo
		DeskManagedObjectDependencyToDeskManagedObjectModel dependencyToMo = new DeskManagedObjectDependencyToDeskManagedObjectModel();
		dependencyToMo.setDeskManagedObjectDependency(dependency);
		dependencyToMo.setDeskManagedObject(mo);
		dependencyToMo.connect();

		// taskObject -> extMo
		WorkTaskObjectToExternalManagedObjectModel objectToExtMo = new WorkTaskObjectToExternalManagedObjectModel();
		objectToExtMo.setTaskObject(taskObject);
		objectToExtMo.setExternalManagedObject(extMo);
		objectToExtMo.connect();

		// taskObject -> mo
		WorkTaskObjectToDeskManagedObjectModel objectToMo = new WorkTaskObjectToDeskManagedObjectModel();
		objectToMo.setWorkTaskObject(taskObject);
		objectToMo.setDeskManagedObject(mo);
		objectToMo.connect();

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
		assertEquals("mo - mos", "MANAGED_OBJECT_SOURCE", moToMos
				.getDeskManagedObjectSourceName());
		assertEquals("mosFlow - extFlow", "EXTERNAL_FLOW", mosFlowToExtFlow
				.getExternalFlowName());
		assertEquals("mosFlow - task", "TASK", mosFlowToTask.getTaskName());
		assertEquals("dependency - extMo", "EXTERNAL_MANAGED_OBJECT",
				dependencyToExtMo.getExternalManagedObjectName());
		assertEquals("dependency - mo", "MANAGED_OBJECT", dependencyToMo
				.getDeskManagedObjectName());
		assertEquals("taskObject - extMo", "EXTERNAL_MANAGED_OBJECT",
				objectToExtMo.getExternalManagedObjectName());
		assertEquals("taskObject - mo", "MANAGED_OBJECT", objectToMo
				.getDeskManagedObjectName());
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