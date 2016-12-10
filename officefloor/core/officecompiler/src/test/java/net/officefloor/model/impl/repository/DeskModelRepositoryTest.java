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
package net.officefloor.model.impl.repository;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
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
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshalling/unmarshalling of the {@link DeskModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeskModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link DeskModel}.
	 */
	private ConfigurationItem configurationItem;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "TestDesk.desk.xml"), null);
	}

	/**
	 * Ensure retrieve the {@link DeskModel}.
	 */
	public void testRetrieveDesk() throws Exception {

		// Load the Desk
		ModelRepository repository = new ModelRepositoryImpl();
		DeskModel desk = new DeskModel();
		desk = repository.retrieve(desk, this.configurationItem);

		// ----------------------------------------
		// Validate the External Managed Objects
		// ----------------------------------------
		assertList(new String[] { "getExternalManagedObjectName",
				"getObjectType", "getX", "getY" }, desk
				.getExternalManagedObjects(), new ExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT", "java.lang.String", null, null, 100,
				101));

		// ----------------------------------------
		// Validate the Managed Object Sources
		// ----------------------------------------
		assertList(new String[] { "getDeskManagedObjectSourceName",
				"getManagedObjectSourceClassName", "getObjectType",
				"getTimeout", "getX", "getY" }, desk
				.getDeskManagedObjectSources(),
				new DeskManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
						"net.example.ExampleManagedObjectSource",
						"net.orm.Session", "10", null, null, null, 200, 201));
		DeskManagedObjectSourceModel mos = desk.getDeskManagedObjectSources()
				.get(0);
		assertList(new String[] { "getName", "getValue" }, mos.getProperties(),
				new PropertyModel("MO_ONE", "VALUE_ONE"), new PropertyModel(
						"MO_TWO", "VALUE_TWO"));
		assertList(new String[] { "getDeskManagedObjectSourceFlowName",
				"getArgumentType" }, mos.getDeskManagedObjectSourceFlows(),
				new DeskManagedObjectSourceFlowModel("FLOW_ONE", String.class
						.getName()), new DeskManagedObjectSourceFlowModel(
						"FLOW_TWO", Integer.class.getName()));
		DeskManagedObjectSourceFlowModel flowOne = mos
				.getDeskManagedObjectSourceFlows().get(0);
		assertProperties(new DeskManagedObjectSourceFlowToExternalFlowModel(
				"flow"), flowOne.getExternalFlow(), "getExternalFlowName");
		DeskManagedObjectSourceFlowModel flowTwo = mos
				.getDeskManagedObjectSourceFlows().get(1);
		assertProperties(new DeskManagedObjectSourceFlowToTaskModel("taskOne"),
				flowTwo.getTask(), "getTaskName");

		// ----------------------------------------
		// Validate the Managed Objects
		// ----------------------------------------
		assertList(new String[] { "getDeskManagedObjectName",
				"getManagedObjectScope", "getX", "getY" }, desk
				.getDeskManagedObjects(), new DeskManagedObjectModel(
				"MANAGED_OBJECT_ONE", "THREAD", null, null, null, null, 300,
				301), new DeskManagedObjectModel("MANAGED_OBJECT_TWO",
				"PROCESS", null, null, null, null, 310, 311));
		DeskManagedObjectModel mo = desk.getDeskManagedObjects().get(0);
		assertProperties(new DeskManagedObjectToDeskManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE"), mo.getDeskManagedObjectSource(),
				"getDeskManagedObjectSourceName");
		assertList(new String[] { "getDeskManagedObjectDependencyName",
				"getDependencyType" }, mo.getDeskManagedObjectDependencies(),
				new DeskManagedObjectDependencyModel("DEPENDENCY_ONE",
						Object.class.getName()),
				new DeskManagedObjectDependencyModel("DEPENDENCY_TWO",
						Connection.class.getName()));
		DeskManagedObjectDependencyModel dependencyOne = mo
				.getDeskManagedObjectDependencies().get(0);
		assertProperties(
				new DeskManagedObjectDependencyToExternalManagedObjectModel(
						"EXTERNAL_MANAGED_OBJECT"), dependencyOne
						.getExternalManagedObject(),
				"getExternalManagedObjectName");
		DeskManagedObjectDependencyModel dependencyTwo = mo
				.getDeskManagedObjectDependencies().get(1);
		assertProperties(
				new DeskManagedObjectDependencyToDeskManagedObjectModel(
						"MANAGED_OBJECT_TWO"), dependencyTwo
						.getDeskManagedObject(), "getDeskManagedObject");

		// ----------------------------------------
		// Validate the External Flows
		// ----------------------------------------
		assertList(new String[] { "getExternalFlowName", "getArgumentType",
				"getX", "getY" }, desk.getExternalFlows(),
				new ExternalFlowModel("flow", "java.lang.Object", null, null,
						null, null, 400, 401), new ExternalFlowModel(
						"escalation", "java.lang.Throwable", null, null, null,
						null, 410, 411));

		// ----------------------------------------
		// Validate the Work
		// ----------------------------------------
		assertList(new String[] { "getWorkName", "getWorkSourceClassName",
				"getX", "getY" }, desk.getWorks(), new WorkModel("work",
				"net.example.ExampleWorkSource", null, null, null, 500, 501));
		WorkModel work = desk.getWorks().get(0);

		// Validate properties of work
		assertList(new String[] { "getName", "getValue" },
				work.getProperties(), new PropertyModel("property.one",
						"VALUE_ONE"), new PropertyModel("property.two",
						"VALUE_TWO"));

		// Validate initial task
		assertEquals("Incorrect initial task", "taskOne", work.getInitialTask()
				.getInitialTaskName());

		// Validate work tasks
		List<WorkTaskModel> workTasks = new LinkedList<WorkTaskModel>();
		workTasks.add(new WorkTaskModel("workTaskOne"));
		workTasks.add(new WorkTaskModel("workTaskTwo"));
		workTasks.add(new WorkTaskModel("workTaskThree"));
		workTasks.add(new WorkTaskModel("workTaskFour"));
		assertList(new String[] { "getWorkTaskName" }, desk.getWorks().get(0)
				.getWorkTasks(), workTasks.toArray(new WorkTaskModel[0]));
		WorkTaskModel workTaskOne = work.getWorkTasks().get(0);
		WorkTaskModel workTaskTwo = work.getWorkTasks().get(1);
		WorkTaskModel workTaskThree = work.getWorkTasks().get(2);
		WorkTaskModel workTaskFour = work.getWorkTasks().get(3);

		// Validate objects on tasks
		String[] objectValidate = new String[] { "getObjectName", "getKey",
				"getObjectType", "getIsParameter" };
		assertList(
				objectValidate,
				workTaskOne.getTaskObjects(),
				new WorkTaskObjectModel("ONE", "ONE", "java.lang.String", false));
		assertList(objectValidate, workTaskTwo.getTaskObjects(),
				new WorkTaskObjectModel("0", null, "java.lang.Integer", true),
				new WorkTaskObjectModel("1", null, "java.lang.String", false),
				new WorkTaskObjectModel("2", null, "net.orm.Session", false));
		assertList(objectValidate, workTaskThree.getTaskObjects(),
				new WorkTaskObjectModel("parameter", null,
						"java.lang.Throwable", true));
		assertList(objectValidate, workTaskFour.getTaskObjects());

		// Validate task object connections
		assertProperties(new WorkTaskObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT"), workTaskOne.getTaskObjects().get(0)
				.getExternalManagedObject(), "getExternalManagedObjectName");
		assertNull(workTaskTwo.getTaskObjects().get(0)
				.getExternalManagedObject());
		assertProperties(new WorkTaskObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT"), workTaskTwo.getTaskObjects().get(1)
				.getExternalManagedObject(), "getExternalManagedObjectName");
		assertNull(workTaskThree.getTaskObjects().get(0)
				.getExternalManagedObject());
		assertProperties(new WorkTaskObjectToDeskManagedObjectModel(
				"MANAGED_OBJECT"), workTaskTwo.getTaskObjects().get(2)
				.getDeskManagedObject(), "getDeskManagedObjectName");

		// ----------------------------------------
		// Validate the Tasks
		// ----------------------------------------
		List<TaskModel> tasks = new LinkedList<TaskModel>();
		tasks.add(new TaskModel("taskOne", true, "work", "workTaskOne",
				"java.lang.Integer", null, null, null, null, null, null, null,
				null, null, null, 600, 601));
		tasks.add(new TaskModel("taskTwo", false, "work", "workTaskTwo", null,
				null, null, null, null, null, null, null, null, null, null,
				610, 611));
		tasks.add(new TaskModel("taskThree", false, "work", "workTaskThree",
				"java.lang.Integer", null, null, null, null, null, null, null,
				null, null, null, 620, 621));
		tasks.add(new TaskModel("taskFour", false, "work", "workTaskFour",
				null, null, null, null, null, null, null, null, null, null,
				null, 630, 631));
		assertList(new String[] { "getTaskName", "getWorkName",
				"getWorkTaskName", "getReturnType", "getX", "getY" }, desk
				.getTasks(), tasks.toArray(new TaskModel[0]));
		TaskModel taskOne = desk.getTasks().get(0);
		TaskModel taskTwo = desk.getTasks().get(1);
		TaskModel taskThree = desk.getTasks().get(2);
		TaskModel taskFour = desk.getTasks().get(3);

		// Validate the flows (keyed and indexed)
		String[] flowValidation = new String[] { "getFlowName", "getKey",
				"getArgumentType" };
		assertList(flowValidation, taskOne.getTaskFlows(), new TaskFlowModel(
				"First", "ONE", "java.lang.Double"), new TaskFlowModel(
				"Second", "TWO", "java.lang.Integer"), new TaskFlowModel(
				"Third", "THREE", null));
		assertEquals(0, taskTwo.getTaskFlows().size());
		assertList(flowValidation, taskThree.getTaskFlows(), new TaskFlowModel(
				"0", null, "java.lang.Integer"), new TaskFlowModel("1", null,
				"java.lang.Double"));
		assertEquals(0, taskFour.getTaskFlows().size());

		// Validate the flow connections
		TaskFlowModel taskOneFirst = taskOne.getTaskFlows().get(0);
		assertProperties(new TaskFlowToExternalFlowModel("flow",
				DeskChanges.SEQUENTIAL_LINK), taskOneFirst.getExternalFlow(),
				"getExternalFlowName", "getLinkType");
		assertNull(taskOneFirst.getTask());
		TaskFlowModel taskOneSecond = taskOne.getTaskFlows().get(1);
		assertNull(taskOneSecond.getExternalFlow());
		assertProperties(new TaskFlowToTaskModel("taskTwo",
				DeskChanges.PARALLEL_LINK), taskOneSecond.getTask(),
				"getTaskName", "getLinkType");

		// Validate next flows
		assertProperties(new TaskToNextTaskModel("taskTwo"), taskOne
				.getNextTask(), "getNextTaskName");
		assertNull(taskOne.getNextExternalFlow());
		assertNull(taskTwo.getNextTask());
		assertProperties(new TaskToNextExternalFlowModel("flow"), taskTwo
				.getNextExternalFlow(), "getExternalFlowName");

		// Validate escalations
		String[] escalationValidate = new String[] { "getEscalationType" };
		assertList(escalationValidate, taskOne.getTaskEscalations(),
				new TaskEscalationModel("java.io.IOException"),
				new TaskEscalationModel("java.sql.SQLException"),
				new TaskEscalationModel("java.lang.NullPointerException"));
		assertList(escalationValidate, taskTwo.getTaskEscalations());

		// Validate escalation connections
		TaskEscalationModel taskOneIO = taskOne.getTaskEscalations().get(0);
		assertProperties(taskOneIO.getTask(), new TaskEscalationToTaskModel(
				"taskThree"), "getTaskName");
		assertNull(taskOneIO.getExternalFlow());
		TaskEscalationModel taskOneSQL = taskOne.getTaskEscalations().get(1);
		assertNull(taskOneSQL.getTask());
		assertProperties(taskOneSQL.getExternalFlow(),
				new TaskEscalationToExternalFlowModel("escalation"),
				"getExternalFlowName");
		TaskEscalationModel taskOneNull = taskOne.getTaskEscalations().get(2);
		assertNull(taskOneNull.getTask());
		assertNull(taskOneNull.getExternalFlow());
	}

	/**
	 * Ensure able to round trip storing and retrieving the {@link DeskModel}.
	 */
	public void testRoundTripStoreRetrieveDesk() throws Exception {

		// Load the Desk
		ModelRepository repository = new ModelRepositoryImpl();
		DeskModel desk = new DeskModel();
		desk = repository.retrieve(desk, this.configurationItem);

		// Store the Desk
		MemoryConfigurationItem contents = new MemoryConfigurationItem();
		repository.store(desk, contents);

		// Reload the Desk
		DeskModel reloadedDesk = new DeskModel();
		reloadedDesk = repository.retrieve(reloadedDesk, contents);

		// Validate round trip
		assertGraph(desk, reloadedDesk,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}