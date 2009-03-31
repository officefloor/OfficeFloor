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
import net.officefloor.compile.desk.DeskLoader;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.mock.MockClass;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.DeskTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.ExternalEscalationModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToExternalEscalationModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.desk.FlowItemOutputToExternalFlowModel;
import net.officefloor.model.desk.FlowItemOutputToFlowItemModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.filesystem.FileSystemConfigurationItem;
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
				.getWorks(), new DeskWorkModel("work", ClassWorkSource.class
				.getName(), null, null, null, 40, 41));

		// Validate properties of work
		DeskWorkModel work = desk.getWorks().get(0);
		assertList(new String[] { "getName", "getValue" },
				work.getProperties(), new PropertyModel(
						ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
						MockClass.class.getName()));

		// Validate initial flow item
		assertEquals("Incorrect initial flow item id", "1", work
				.getInitialFlowItem().getFlowItemId());

		// ----------------------------------------
		// Validate the Tasks
		// ----------------------------------------

		// Validate tasks
		List<DeskTaskModel> tasks = new LinkedList<DeskTaskModel>();
		tasks.add(new DeskTaskModel("taskMethod", null, null));
		if (!isSynchronised) {
			tasks.add(new DeskTaskModel("noLongerExists", null, null));
		}
		tasks.add(new DeskTaskModel("anotherMethod", null, null));
		assertList(new String[] { "getName" }, desk.getWorks().get(0)
				.getTasks(), tasks.toArray(new DeskTaskModel[0]));

		// Validate task objects
		assertList(new String[] { "getObjectType", "getIsParameter" }, desk
				.getWorks().get(0).getTasks().get(0).getObjects(),
				new DeskTaskObjectModel(String.class.getName(), false, null));
		if (!isSynchronised) {
			assertList(new String[] { "getObjectType", "getIsParameter" }, desk
					.getWorks().get(0).getTasks().get(1).getObjects(),
					new DeskTaskObjectModel(Integer.class.getName(), false,
							null), new DeskTaskObjectModel(String.class
							.getName(), true, null));
		}

		// Validate task object to external managed object connections
		assertEquals("Incorrect external managed object name (for taskMethod)",
				"mo", desk.getWorks().get(0).getTasks().get(0).getObjects()
						.get(0).getManagedObject().getName());
		if (!isSynchronised) {
			assertEquals(
					"Incorrect external managed object name (for noLongerExists)",
					"mo", desk.getWorks().get(0).getTasks().get(1).getObjects()
							.get(0).getManagedObject().getName());
		}

		// ----------------------------------------
		// Validate the Flow Items
		// ----------------------------------------

		// Validate flow items
		List<FlowItemModel> flowItems = new LinkedList<FlowItemModel>();
		flowItems.add(new FlowItemModel("1", true, "work", "taskMethod", null,
				null, null, null, null, null, null, null, null, 100, 200));
		flowItems.add(new FlowItemModel("2", false, "work", "anotherMethod",
				null, null, null, null, null, null, null, null, null, 50, 500));
		if (!isSynchronised) {
			flowItems.add(new FlowItemModel("3", false, "work",
					"noLongerExists", null, null, null, null, null, null, null,
					null, null, 10, 20));
		}
		assertList(new String[] { "getId", "getWorkName", "getTaskName",
				"getX", "getY" }, desk.getFlowItems(), flowItems
				.toArray(new FlowItemModel[0]));

		// Validate next external flow
		FlowItemModel flowItem = desk.getFlowItems().get(0);
		assertEquals("Incorrect next external flow", "flow", flowItem
				.getNextExternalFlow().getExternalFlowName());
		assertEquals("Incorrect previous flow of external flow", flowItem
				.getNextExternalFlow(), desk.getExternalFlows().get(0)
				.getPreviousFlowItems().get(0));

		// Validate next flow
		if (!isSynchronised) {
			flowItem = desk.getFlowItems().get(2);
			assertEquals("Incorrect next flow id", "1", flowItem
					.getNextFlowItem().getId());
			assertEquals("Incorrect previous flow", flowItem.getNextFlowItem(),
					desk.getFlowItems().get(0).getPreviousFlowItems().get(0));
		}

		// Validate outputs of first flow item
		FlowItemModel flowItemOne = desk.getFlowItems().get(0);
		assertList(new String[] { "getId", "getLabel" }, flowItemOne
				.getOutputs(),
				new FlowItemOutputModel("0", "First", null, null),
				new FlowItemOutputModel("1", null, null, null),
				new FlowItemOutputModel("2", "Third", null, null));

		// Validate link types of the first flow item
		assertEquals("Incorrect link type (sequential)",
				DeskLoader.SEQUENTIAL_LINK_TYPE, flowItemOne.getOutputs()
						.get(0).getExternalFlow().getLinkType());
		assertEquals("Incorrect link type (parallel)",
				DeskLoader.PARALLEL_LINK_TYPE, flowItemOne.getOutputs().get(1)
						.getFlowItem().getLinkType());
		if (!isSynchronised) {
			assertProperties(new FlowItemOutputToFlowItemModel("3", null, null,
					DeskLoader.ASYNCHRONOUS_LINK_TYPE), flowItemOne
					.getOutputs().get(2).getFlowItem(), "getId", "getLinkType");
		} else {
			assertNull("Should lose link to non-existant task", flowItemOne
					.getOutputs().get(2).getFlowItem());
		}

		// Validate escalations of first flow item
		List<FlowItemEscalationModel> flowItemOneEscalations = new LinkedList<FlowItemEscalationModel>();
		flowItemOneEscalations.add(new FlowItemEscalationModel(
				IOException.class.getName(), null, null));
		if (!isSynchronised) {
			flowItemOneEscalations.add(new FlowItemEscalationModel(
					NullPointerException.class.getName(), null, null));
		}
		assertList(new String[] { "getEscalationType" }, flowItemOne
				.getEscalations(), flowItemOneEscalations
				.toArray(new FlowItemEscalationModel[0]));

		// Validate handler on first flow escalation
		assertNotNull("No handler for escalation", flowItemOne.getEscalations()
				.get(0).getEscalationHandler());

		// Validate outputs of second flow item
		FlowItemModel flowItemTwo = desk.getFlowItems().get(1);
		assertEquals("Incorrect number of outputs on second flow", 0,
				flowItemTwo.getOutputs().size());

		// Validate escalations of second flow item
		List<FlowItemEscalationModel> flowItemTwoEscalations = new LinkedList<FlowItemEscalationModel>();
		if (isSynchronised) {
			flowItemTwoEscalations.add(new FlowItemEscalationModel(
					SQLException.class.getName(), null, null));
		}
		assertList(new String[] { "getEscalationType" }, flowItemTwo
				.getEscalations(), flowItemTwoEscalations
				.toArray(new FlowItemEscalationModel[0]));

		// Validate escalation handling of second flow item
		assertEquals("Incorrect escalation being handled", flowItemOne
				.getEscalations().get(0), flowItemTwo.getHandledEscalations()
				.get(0).getEscalation());

		if (!isSynchronised) {
			// Validate outputs on third flow item
			FlowItemModel flowItemThree = desk.getFlowItems().get(2);
			assertList(new String[] { "getId", "getLabel" }, flowItemThree
					.getOutputs(), new FlowItemOutputModel("FIRST_FLOW", null,
					null, null), new FlowItemOutputModel("SECOND_FLOW",
					"Another", null, null));

			// Validate link types of the second flow item
			assertEquals("Incorrect link type (asynchronous)",
					DeskLoader.ASYNCHRONOUS_LINK_TYPE, flowItemThree
							.getOutputs().get(0).getExternalFlow()
							.getLinkType());
			assertEquals("Incorrect link type (parallel)",
					DeskLoader.PARALLEL_LINK_TYPE, flowItemThree.getOutputs()
							.get(1).getFlowItem().getLinkType());

			// Validate escalations of third flow item
			assertList(new String[] { "getEscalationType" }, flowItemThree
					.getEscalations(), new FlowItemEscalationModel(
					SQLException.class.getName(), null, null));

			// Validate external escalation connection
			assertProperties(new FlowItemEscalationToExternalEscalationModel(
					"escalation", null, desk.getExternalEscalations().get(0)),
					flowItemThree.getEscalations().get(0)
							.getExternalEscalation(), "getName",
					"getExternalEscalation");
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
		List<DeskTaskObjectToExternalManagedObjectModel> taskObjects = new LinkedList<DeskTaskObjectToExternalManagedObjectModel>();
		taskObjects.add(new DeskTaskObjectToExternalManagedObjectModel("mo",
				null, null));
		if (!isSynchronised) {
			taskObjects.add(new DeskTaskObjectToExternalManagedObjectModel(
					"mo", null, null));
		}
		assertList(new String[] { "getName" }, desk.getExternalManagedObjects()
				.get(0).getTaskObjects(), taskObjects
				.toArray(new DeskTaskObjectToExternalManagedObjectModel[0]));

		// ----------------------------------------
		// Validate the External Flows
		// ----------------------------------------

		// Validate external flows
		assertList(new String[] { "getName", "getX", "getY" }, desk
				.getExternalFlows(), new ExternalFlowModel("flow", null, null,
				20, 21));

		// Validate external flow connections to flow items
		List<FlowItemOutputToExternalFlowModel> externalFlowLinks = new LinkedList<FlowItemOutputToExternalFlowModel>();
		externalFlowLinks.add(new FlowItemOutputToExternalFlowModel("flow",
				null, null, "Sequential"));
		if (!isSynchronised) {
			externalFlowLinks.add(new FlowItemOutputToExternalFlowModel("flow",
					null, null, "Asynchronous"));
		}
		assertList(new String[] { "getName", "getLinkType" }, desk
				.getExternalFlows().get(0).getOutputs(), externalFlowLinks
				.toArray(new FlowItemOutputToExternalFlowModel[0]));

		// ----------------------------------------
		// Validate the External Escalations
		// ----------------------------------------

		// Validate external escalations
		assertList(new String[] { "getName", "getEscalationType", "getX",
				"getY" }, desk.getExternalEscalations(),
				new ExternalEscalationModel("escalation", SQLException.class
						.getName(), null, 30, 31));

		// Validate external escalation connection to flow item escalations
		if (isSynchronised) {
			assertEquals("Incorrect external escalation connection number", 0,
					desk.getExternalEscalations().get(0)
							.getHandledEscalations().size());
		} else {
			assertList(new String[] { "getName" }, desk
					.getExternalEscalations().get(0).getHandledEscalations(),
					new FlowItemEscalationToExternalEscalationModel(
							"escalation", null, null));
		}
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