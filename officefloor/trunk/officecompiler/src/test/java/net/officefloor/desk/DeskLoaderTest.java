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
package net.officefloor.desk;

import java.io.File;

import net.officefloor.LoaderContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.mock.MockClass;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.DeskTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.desk.FlowItemOutputToExternalFlowModel;
import net.officefloor.repository.ModelRepository;
import net.officefloor.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.work.clazz.ClassWork;
import net.officefloor.work.clazz.ClassWorkLoader;

/**
 * Ensure able to load the {@link net.officefloor.model.desk.DeskModel}.
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
	protected void setUp() throws Exception {

		// Create the desk loader to test
		this.deskLoader = new DeskLoader(new LoaderContext(this.getClass()
				.getClassLoader()), new ModelRepository());

		// Specify location of configuration file
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "TestDesk.desk.xml"), null);
	}

	/**
	 * Ensure loads the {@link net.officefloor.model.desk.DeskModel}.
	 */
	public void testLoadDesk() throws Exception {

		// Load the Desk
		DeskModel desk = this.deskLoader.loadDesk(this.configurationItem);

		// ----------------------------------------
		// Validate the Work
		// ----------------------------------------

		// Validate work
		assertList(new String[] { "getId", "getLoader", "getConfiguration" },
				desk.getWorks(), new DeskWorkModel("work",
						ClassWorkLoader.class.getName(), MockClass.class
								.getName(), null, null, null));

		// Validate initial flow item
		assertEquals("Incorrect initial flow item id", "1", desk.getWorks()
				.get(0).getInitialFlowItem().getFlowItemId());

		// Validate tasks
		assertList(new String[] { "getName" }, desk.getWorks().get(0)
				.getTasks(), new DeskTaskModel("taskMethod", null, null, null),
				new DeskTaskModel("noLongerExists", null, null, null));

		// Validate task objects
		assertList(new String[] { "getObjectType", "getIsParameter" }, desk
				.getWorks().get(0).getTasks().get(0).getObjects(),
				new DeskTaskObjectModel("java.lang.String", false, null, null));

		// Validate task object to external managed object connections
		assertEquals("Incorrect external managed object name (for taskMethod)",
				"mo", desk.getWorks().get(0).getTasks().get(0).getObjects()
						.get(0).getManagedObject().getName());
		assertEquals(
				"Incorrect external managed object name (for noLongerExists)",
				"mo", desk.getWorks().get(0).getTasks().get(1).getObjects()
						.get(0).getManagedObject().getName());

		// Validate flow items
		assertList(new String[] { "getId", "getWorkName", "getTaskName",
				"getX", "getY" }, desk.getFlowItems(), new FlowItemModel("1",
				true, "work", "taskMethod", null, null, null, null, null, 100,
				200), new FlowItemModel("2", false, "work", "noLongerExists",
				null, null, null, null, null, 10, 20));

		// Validate outputs of first flow item
		FlowItemModel flowItemOne = desk.getFlowItems().get(0);
		assertList(new String[] { "getId" }, flowItemOne.getOutputs(),
				new FlowItemOutputModel("0", null, null, null),
				new FlowItemOutputModel("1", null, null, null));

		// Validate outputs on second flow item
		FlowItemModel flowItemTwo = desk.getFlowItems().get(1);
		assertList(new String[] { "getId" }, flowItemTwo.getOutputs(),
				new FlowItemOutputModel("FIRST_FLOW", null, null, null),
				new FlowItemOutputModel("SECOND_FLOW", null, null, null));

		// Validate link types of flows
		assertEquals("Incorrect link type (sequential)",
				DeskLoader.SEQUENTIAL_LINK_TYPE, flowItemOne.getOutputs()
						.get(0).getExternalFlow().getLinkType());
		assertEquals("Incorrect link type (parallel)",
				DeskLoader.PARALLEL_LINK_TYPE, flowItemOne.getOutputs().get(1)
						.getFlowItem().getLinkType());
		assertEquals("Incorrect link type (asynchronous)",
				DeskLoader.ASYNCHRONOUS_LINK_TYPE, flowItemTwo.getOutputs()
						.get(0).getExternalFlow().getLinkType());
		assertEquals("Incorrect link type (parallel)",
				DeskLoader.PARALLEL_LINK_TYPE, flowItemTwo.getOutputs().get(1)
						.getFlowItem().getLinkType());

		// ----------------------------------------
		// Validate the External Managed Objects
		// ----------------------------------------

		// Validate external managed objects
		assertList(new String[] { "getName", "getObjectType" }, desk
				.getExternalManagedObjects(), new ExternalManagedObjectModel(
				"mo", "java.lang.String", null));

		// Validate external managed object connections to work tasks
		assertList(
				new String[] { "getName" },
				desk.getExternalManagedObjects().get(0).getTaskObjects(),
				new DeskTaskObjectToExternalManagedObjectModel("mo", null, null),
				new DeskTaskObjectToExternalManagedObjectModel("mo", null, null));

		// ----------------------------------------
		// Validate the External Flows
		// ----------------------------------------

		// Validate external flows
		assertList(new String[] { "getName" }, desk.getExternalFlows(),
				new ExternalFlowModel("flow", null));

		// Validate external flow connections to flow items
		assertList(new String[] { "getName", "getLinkType" }, desk
				.getExternalFlows().get(0).getOutputs(),
				new FlowItemOutputToExternalFlowModel("flow", null, null,
						"Sequential"), new FlowItemOutputToExternalFlowModel(
						"flow", null, null, "Asynchronous"));
	}

	/**
	 * Ensure raw load and store (without the synchronisers).
	 */
	public void testLoadAndStore() throws Exception {

		// Load the Desk
		DeskModel desk = this.deskLoader.loadDesk(this.configurationItem);

		// Store the Desk
		FileSystemConfigurationItem tempFile = new FileSystemConfigurationItem(
				File.createTempFile("TestDesk.desk.xml", null), null);
		this.deskLoader.storeDesk(desk, tempFile);

		// Reload the Desk
		DeskModel reloadedDesk = this.deskLoader.loadDesk(tempFile);

		// Validate round trip
		assertGraph(desk, reloadedDesk);
	}

	/**
	 * TODO make below work on synchronising
	 */
	public void testTodo() throws Exception {
		// TODO make below work on synchronising

		fail("TODO synchronising put on loader");

		DeskModel desk = null;

		// Validate underlying work
		ClassWork classWork = (ClassWork) desk.getWorks().get(0).getWork()
				.getWorkFactory().createWork();
		assertTrue("Incorrect work", classWork.getObject() instanceof MockClass);

		// Validate underlying tasks
		assertNotNull("Must link in existing Task", desk.getFlowItems().get(0)
				.getTask());
		assertNull(
				"Do not link in missing task - required to relink to another task or delete",
				desk.getFlowItems().get(1).getTask());
	}
}
