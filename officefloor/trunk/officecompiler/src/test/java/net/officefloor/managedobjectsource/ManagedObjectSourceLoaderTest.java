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
package net.officefloor.managedobjectsource;

import java.util.Properties;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.impl.RawManagedObjectMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.managedobjectsource.TestManagedObjectSource.HandlerKey;
import net.officefloor.model.officefloor.ManagedObjectHandlerInstanceModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectTaskFlowModel;
import net.officefloor.model.officefloor.ManagedObjectTaskModel;
import net.officefloor.model.officefloor.ManagedObjectTeamModel;
import net.officefloor.model.officefloor.PropertyModel;

/**
 * Tests the {@link ManagedObjectSourceLoader}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensures loads.
	 */
	public void testLoadManagedObjectSource() throws Throwable {

		// Create the loader
		ManagedObjectSourceLoader loader = new ManagedObjectSourceLoader();

		// Provide the properties
		Properties properties = new Properties();
		properties.setProperty("property name", "property value");

		// Load the managed object source model
		final String MO_NAME = "test";
		ManagedObjectSourceModel model = loader.loadManagedObjectSource(
				MO_NAME, new TestManagedObjectSource(), properties, this
						.getClass().getClassLoader());

		// --------------------------------------------
		// Validate managed object source configuration
		// --------------------------------------------
		assertEquals("Incorrect managed object source class",
				TestManagedObjectSource.class.getName(), model.getSource());

		// Validate properties
		assertList(new String[] { "getName", "getValue" }, model
				.getProperties(), new PropertyModel("property name",
				"property value"));

		// Validate handlers
		assertList(new String[] { "getHandlerKey", "getHandlerType" }, model
				.getHandlers(), new ManagedObjectHandlerModel(
				HandlerKey.INDIRECT_HANDLER.name(), Handler.class.getName(),
				null), new ManagedObjectHandlerModel(HandlerKey.ADDED_HANDLER
				.name(), Handler.class.getName(), null));
		assertNull("Indirect handler should not have handler instance", model
				.getHandlers().get(0).getHandlerInstance());
		ManagedObjectHandlerInstanceModel handlerInstance = model.getHandlers()
				.get(1).getHandlerInstance();
		assertTrue("Handler instance is provide by managed object source",
				handlerInstance.getIsManagedObjectSourceProvided());
		assertList(new String[] { "getLinkProcessId", "getWorkName",
				"getTaskName" }, handlerInstance.getLinkProcesses(),
				new ManagedObjectHandlerLinkProcessModel("0", null, null),
				new ManagedObjectHandlerLinkProcessModel("1", MO_NAME
						+ ".handler-work", MO_NAME + ".handler-task"));

		// Validate tasks
		final String RECYCLE_WORK_NAME = MO_NAME + "."
				+ RawManagedObjectMetaData.MANAGED_OBJECT_CLEAN_UP_WORK_NAME;
		assertList(
				new String[] { "getWorkName", "getTaskName", "getTeamName" },
				model.getTasks(),
				new ManagedObjectTaskModel(RECYCLE_WORK_NAME, MO_NAME
						+ ".RECYCLE TASK ONE", null, null),
				new ManagedObjectTaskModel(RECYCLE_WORK_NAME, MO_NAME
						+ ".RECYCLE TASK TWO", MO_NAME + ".Recycle Team", null),
				new ManagedObjectTaskModel(MO_NAME + ".WORK",
						MO_NAME + ".TASK", null, null));

		// Validate task flows
		assertList(new String[] { "getFlowId", "getInitialWorkName",
				"getInitialTaskName" }, model.getTasks().get(0).getFlows(),
				new ManagedObjectTaskFlowModel("0", null, MO_NAME
						+ ".RECYCLE TASK TWO"), new ManagedObjectTaskFlowModel(
						"1", null, null));
		assertList(new String[] { "getFlowId", "getInitialWorkName",
				"getInitialTaskName" }, model.getTasks().get(1).getFlows(),
				new ManagedObjectTaskFlowModel("0", null, MO_NAME
						+ ".RECYCLE TASK ONE"));

		// Validate teams
		assertList(new String[] { "getTeamName" }, model.getTeams(),
				new ManagedObjectTeamModel(MO_NAME + ".Recycle Team"));
	}
}
