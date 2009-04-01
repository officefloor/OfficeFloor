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
package net.officefloor.compile.desk;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.LoaderContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.mock.MockClass;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * Ensure able to load the {@link DeskModel}.
 * 
 * @author Daniel
 */
public class DeskLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link DeskLoader} being tested.
	 */
	private DeskLoader deskLoader;

	/**
	 * Access to configuration.
	 */
	private FileSystemConfigurationItem configurationItem;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Create the desk loader to test
		this.deskLoader = new DeskLoader(new LoaderContext(this.getClass()
				.getClassLoader()), new ModelRepositoryImpl());

		// Specify location of configuration file
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "TestDesk.desk.xml"), null);
	}

	/**
	 * Ensure loads the {@link DeskModel}.
	 */
	public void testLoadDesk() throws Exception {

		// Load the Desk
		DeskModel desk = this.deskLoader.loadDesk(this.configurationItem);

		// Validate the desk
		this.validateDesk(desk, false);
	}

	/**
	 * Ensures loads and synchronises the {@link DeskModel}.
	 */
	public void testLoadDeskAndSynchronise() throws Exception {

		// Load the Desk and synchronise
		DeskModel desk = this.deskLoader
				.loadDeskAndSynchronise(this.configurationItem);

		// Validate the desk
		this.validateDesk(desk, true);
	}

	/**
	 * Validates the {@link DeskModel}.
	 * 
	 * @param desk
	 *            {@link DeskModel}.
	 * @param isSynchronised
	 *            Flag indicating if synchronising.
	 */
	private void validateDesk(DeskModel desk, boolean isSynchronised) {

		// ----------------------------------------
		// Validate the Work
		// ----------------------------------------

		// Validate work
		assertList(new String[] { "getId", "getLoader", "getX", "getY" }, desk
				.getWorks(), new WorkModel("work", ClassWorkSource.class
				.getName(), null, null, null, 40, 41));

		// Validate properties of work
		WorkModel work = desk.getWorks().get(0);
		assertList(new String[] { "getName", "getValue" },
				work.getProperties(), new PropertyModel(
						ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
						MockClass.class.getName()));

		// Validate initial flow item
		assertEquals("Incorrect initial flow item id", "1", work
				.getInitialTask().getInitialTaskName());

		// ----------------------------------------
		// Validate the Tasks
		// ----------------------------------------

		// Validate tasks
		List<WorkTaskModel> tasks = new LinkedList<WorkTaskModel>();
		tasks.add(new WorkTaskModel("taskMethod", null, null));
		if (!isSynchronised) {
			tasks.add(new WorkTaskModel("noLongerExists", null, null));
		}
		tasks.add(new WorkTaskModel("anotherMethod", null, null));
		assertList(new String[] { "getName" }, desk.getWorks().get(0)
				.getWorkTasks(), tasks.toArray(new WorkTaskModel[0]));

		// Validate task objects
		assertList(new String[] { "getObjectType", "getIsParameter" }, desk
				.getWorks().get(0).getWorkTasks().get(0).getTaskObjects(),
				new WorkTaskObjectModel("key", "KEY", String.class.getName(),
						false, null));
		if (!isSynchronised) {
			assertList(new String[] { "getObjectType", "getIsParameter" }, desk
					.getWorks().get(0).getWorkTasks().get(1).getTaskObjects(),
					new WorkTaskObjectModel("key", "KEY", Integer.class
							.getName(), false, null), new WorkTaskObjectModel(
							"key", "KEY", String.class.getName(), true, null));
		}

		// Validate task object to external managed object connections
		assertEquals("Incorrect external managed object name (for taskMethod)",
				"mo", desk.getWorks().get(0).getWorkTasks().get(0)
						.getTaskObjects().get(0).getExternalManagedObject()
						.getExternalManagedObjectName());
		if (!isSynchronised) {
			assertEquals(
					"Incorrect external managed object name (for noLongerExists)",
					"mo", desk.getWorks().get(0).getWorkTasks().get(1)
							.getTaskObjects().get(0).getExternalManagedObject()
							.getExternalManagedObjectName());
		}

		// ----------------------------------------
		// Validate the Flow Items
		// ----------------------------------------

		// Validate flow items
		List<TaskModel> flowItems = new LinkedList<TaskModel>();
		flowItems
				.add(new TaskModel("1", true, "work", "taskMethod", null, null,
						null, null, null, null, null, null, null, null, 100,
						200));
		flowItems.add(new TaskModel("2", false, "work", "anotherMethod", null,
				null, null, null, null, null, null, null, null, null, 50, 500));
		if (!isSynchronised) {
			flowItems.add(new TaskModel("3", false, "work", "noLongerExists",
					null, null, null, null, null, null, null, null, null, null,
					10, 20));
		}
		assertList(new String[] { "getId", "getWorkName", "getTaskName",
				"getX", "getY" }, desk.getTasks(), flowItems
				.toArray(new TaskModel[0]));

		// Validate next external flow
		TaskModel flowItem = desk.getTasks().get(0);
		assertEquals("Incorrect next external flow", "flow", flowItem
				.getNextExternalFlow().getExternalFlowName());
		assertEquals("Incorrect previous flow of external flow", flowItem
				.getNextExternalFlow(), desk.getExternalFlows().get(0)
				.getPreviousTasks().get(0));

		// Validate next flow
		if (!isSynchronised) {
			flowItem = desk.getTasks().get(2);
			assertEquals("Incorrect next flow id", "1", flowItem.getNextTask()
					.getNextTaskName());
			assertEquals("Incorrect previous flow", flowItem.getNextTask(),
					desk.getTasks().get(0).getPreviousTasks().get(0));
		}

		// Validate outputs of first flow item
		TaskModel flowItemOne = desk.getTasks().get(0);
		assertList(new String[] { "getId", "getLabel" }, flowItemOne
				.getTaskFlows(), new TaskFlowModel("0", "First",
				"java.lang.Object", null, null), new TaskFlowModel("1", null,
				"java.lang.Object", null, null), new TaskFlowModel("2",
				"Third", "java.lang.Object", null, null));

		// Validate link types of the first flow item
		assertEquals("Incorrect link type (sequential)",
				DeskLoader.SEQUENTIAL_LINK_TYPE, flowItemOne.getTaskFlows()
						.get(0).getExternalFlow().getLinkType());
		assertEquals("Incorrect link type (parallel)",
				DeskLoader.PARALLEL_LINK_TYPE, flowItemOne.getTaskFlows()
						.get(1).getTask().getLinkType());
		if (!isSynchronised) {
			assertProperties(new TaskFlowToTaskModel("3", null, null,
					DeskLoader.ASYNCHRONOUS_LINK_TYPE), flowItemOne
					.getTaskFlows().get(2).getTask(), "getId", "getLinkType");
		} else {
			assertNull("Should lose link to non-existant task", flowItemOne
					.getTaskFlows().get(2).getTask());
		}

		// Validate escalations of first flow item
		List<TaskEscalationModel> flowItemOneEscalations = new LinkedList<TaskEscalationModel>();
		flowItemOneEscalations.add(new TaskEscalationModel(IOException.class
				.getName(), null, null));
		if (!isSynchronised) {
			flowItemOneEscalations.add(new TaskEscalationModel(
					NullPointerException.class.getName(), null, null));
		}
		assertList(new String[] { "getEscalationType" }, flowItemOne
				.getTaskEscalations(), flowItemOneEscalations
				.toArray(new TaskEscalationModel[0]));

		// Validate handler on first flow escalation
		assertNotNull("No handler for escalation", flowItemOne
				.getTaskEscalations().get(0).getTask());

		// Validate outputs of second flow item
		TaskModel flowItemTwo = desk.getTasks().get(1);
		assertEquals("Incorrect number of outputs on second flow", 0,
				flowItemTwo.getTaskFlows().size());

		// Validate escalations of second flow item
		List<TaskEscalationModel> flowItemTwoEscalations = new LinkedList<TaskEscalationModel>();
		if (isSynchronised) {
			flowItemTwoEscalations.add(new TaskEscalationModel(
					SQLException.class.getName(), null, null));
		}
		assertList(new String[] { "getEscalationType" }, flowItemTwo
				.getTaskEscalations(), flowItemTwoEscalations
				.toArray(new TaskEscalationModel[0]));

		// Validate escalation handling of second flow item
		assertEquals("Incorrect escalation being handled", flowItemOne
				.getTaskEscalations().get(0), flowItemTwo
				.getTaskEscalationInputs().get(0).getEscalation());

		if (!isSynchronised) {
			// Validate outputs on third flow item
			TaskModel flowItemThree = desk.getTasks().get(2);
			assertList(new String[] { "getId", "getLabel" }, flowItemThree
					.getTaskFlows(), new TaskFlowModel("FIRST_FLOW", null,
					"java.lang.Object", null, null), new TaskFlowModel(
					"SECOND_FLOW", "Another", "java.lang.Object", null, null));

			// Validate link types of the second flow item
			assertEquals("Incorrect link type (asynchronous)",
					DeskLoader.ASYNCHRONOUS_LINK_TYPE, flowItemThree
							.getTaskFlows().get(0).getExternalFlow()
							.getLinkType());
			assertEquals("Incorrect link type (parallel)",
					DeskLoader.PARALLEL_LINK_TYPE, flowItemThree.getTaskFlows()
							.get(1).getTask().getLinkType());

			// Validate escalations of third flow item
			assertList(new String[] { "getEscalationType" }, flowItemThree
					.getTaskEscalations(), new TaskEscalationModel(
					SQLException.class.getName(), null, null));

		}

		// ----------------------------------------
		// Validate the External Managed Objects
		// ----------------------------------------

		// Validate external managed objects
		assertList(new String[] { "getName", "getObjectType", "getX", "getY" },
				desk.getExternalManagedObjects(),
				new ExternalManagedObjectModel("mo", "java.lang.String", null,
						10, 11));

		// Validate external managed object connections to work tasks
		List<WorkTaskObjectToExternalManagedObjectModel> taskObjects = new LinkedList<WorkTaskObjectToExternalManagedObjectModel>();
		taskObjects.add(new WorkTaskObjectToExternalManagedObjectModel("mo",
				null, null));
		if (!isSynchronised) {
			taskObjects.add(new WorkTaskObjectToExternalManagedObjectModel(
					"mo", null, null));
		}
		assertList(new String[] { "getName" }, desk.getExternalManagedObjects()
				.get(0).getTaskObjects(), taskObjects
				.toArray(new WorkTaskObjectToExternalManagedObjectModel[0]));

		// ----------------------------------------
		// Validate the External Flows
		// ----------------------------------------

		// Validate external flows
		assertList(new String[] { "getName", "getX", "getY" }, desk
				.getExternalFlows(), new ExternalFlowModel("flow", null, null,
				null, null, 20, 21));

		// Validate external flow connections to flow items
		List<TaskFlowToExternalFlowModel> externalFlowLinks = new LinkedList<TaskFlowToExternalFlowModel>();
		externalFlowLinks.add(new TaskFlowToExternalFlowModel("flow", null,
				null, "Sequential"));
		if (!isSynchronised) {
			externalFlowLinks.add(new TaskFlowToExternalFlowModel("flow", null,
					null, "Asynchronous"));
		}
		assertList(new String[] { "getName", "getLinkType" }, desk
				.getExternalFlows().get(0).getTaskFlows(), externalFlowLinks
				.toArray(new TaskFlowToExternalFlowModel[0]));
	}

	/**
	 * Ensure raw load and store (without the synchronisers).
	 */
	public void testLoadAndStore() throws Exception {

		// Load the Desk
		DeskModel desk = this.deskLoader.loadDesk(this.configurationItem);

		// Store the Desk
		File tmpFile = File.createTempFile("TestDesk.desk.xml", null);
		FileSystemConfigurationItem tempFile = new FileSystemConfigurationItem(
				tmpFile, null);
		this.deskLoader.storeDesk(desk, tempFile);

		// Reload the Desk
		DeskModel reloadedDesk = this.deskLoader.loadDesk(tempFile);

		// Validate round trip
		assertGraph(desk, reloadedDesk,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}