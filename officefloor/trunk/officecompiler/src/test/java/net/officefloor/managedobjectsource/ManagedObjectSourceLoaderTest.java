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

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.impl.RawManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.managedobjectsource.TestContextManagedObjectSource.HandlerKey;
import net.officefloor.model.officefloor.ManagedObjectDependencyModel;
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
	 * Ensures loads the {@link ManagedObjectSourceModel} from the
	 * {@link ManagedObjectSourceMetaData}.
	 */
	public void testLoadManagedObjectSourceFromMetaData() throws Throwable {

		// Create the loader
		ManagedObjectSourceLoader loader = new ManagedObjectSourceLoader();

		// Default timeout
		final long DEFAULT_TIMEOUT = 10;

		// Load the managed object source model
		ManagedObjectSourceModel model = loader.loadManagedObjectSource("test",
				new TestMetaDataManagedObjectSource(), new Properties(),
				DEFAULT_TIMEOUT, this.getClass().getClassLoader());

		// --------------------------------------------
		// Validate managed object source model
		// --------------------------------------------
		assertEquals("Incorrect managed object source class",
				TestMetaDataManagedObjectSource.class.getName(), model
						.getSource());
		assertEquals("Incorrect managed object default timeout", String
				.valueOf(DEFAULT_TIMEOUT), model.getDefaultTimeout());

		// Validate no properties
		assertList(new String[] { "getName", "getValue" }, model
				.getProperties());

		// Validate dependencies
		assertList(
				new String[] { "getDependencyKey", "getDependencyType" },
				model.getDependencies(),
				new ManagedObjectDependencyModel(
						TestMetaDataManagedObjectSource.DependencyKey.DEPENDENCY
								.name(), Connection.class.getName()));

		// Validate handlers
		assertList(new String[] { "getHandlerKey", "getHandlerKeyClass",
				"getHandlerType" }, model.getHandlers(),
				new ManagedObjectHandlerModel(
						TestMetaDataManagedObjectSource.HandlerKey.HANDLER
								.name(),
						TestMetaDataManagedObjectSource.HandlerKey.class
								.getName(), Handler.class.getName(), null));
	}

	/**
	 * Ensures loads the {@link ManagedObjectSourceModel} by configuration on
	 * the {@link ManagedObjectSourceContext}.
	 */
	public void testLoadManagedObjectSourceFromContext() throws Throwable {

		// Create the loader
		ManagedObjectSourceLoader loader = new ManagedObjectSourceLoader();

		// Default timeout
		final long DEFAULT_TIMEOUT = 10;

		// Provide the properties
		Properties properties = new Properties();
		properties.setProperty("property name", "property value");

		// Load the managed object source model
		ManagedObjectSourceModel model = loader.loadManagedObjectSource("test",
				new TestContextManagedObjectSource(), properties,
				DEFAULT_TIMEOUT, this.getClass().getClassLoader());

		// --------------------------------------------
		// Validate managed object source model
		// --------------------------------------------
		assertEquals("Incorrect managed object source class",
				TestContextManagedObjectSource.class.getName(), model
						.getSource());
		assertEquals("Incorrect managed object default timeout", String
				.valueOf(DEFAULT_TIMEOUT), model.getDefaultTimeout());

		// Validate properties
		assertList(new String[] { "getName", "getValue" }, model
				.getProperties(), new PropertyModel("property name",
				"property value"));

		// Validate handlers
		assertList(new String[] { "getHandlerKey", "getHandlerKeyClass",
				"getHandlerType" }, model.getHandlers(),
				new ManagedObjectHandlerModel(HandlerKey.INDIRECT_HANDLER
						.name(), HandlerKey.class.getName(), Handler.class
						.getName(), null), new ManagedObjectHandlerModel(
						HandlerKey.ADDED_HANDLER.name(), HandlerKey.class
								.getName(), Handler.class.getName(), null));
		Map<String, ManagedObjectHandlerModel> handlers = new HashMap<String, ManagedObjectHandlerModel>();
		for (ManagedObjectHandlerModel handler : model.getHandlers()) {
			handlers.put(handler.getHandlerKey(), handler);
		}
		assertNull("Indirect handler should not have handler instance",
				handlers.get("INDIRECT_HANDLER").getHandlerInstance());
		ManagedObjectHandlerInstanceModel handlerInstance = handlers.get(
				"ADDED_HANDLER").getHandlerInstance();
		assertTrue("Handler instance is provide by managed object source",
				handlerInstance.getIsManagedObjectSourceProvided());
		assertList(
				new String[] { "getLinkProcessId", "getWorkName", "getTaskName" },
				handlerInstance.getLinkProcesses(),
				new ManagedObjectHandlerLinkProcessModel("0", null, null, null),
				new ManagedObjectHandlerLinkProcessModel("1", "handler-work",
						"handler-task", null));

		// Validate tasks
		final String RECYCLE_WORK_NAME = RawManagedObjectMetaData.MANAGED_OBJECT_CLEAN_UP_WORK_NAME;
		assertList("getTaskName", new String[] { "getWorkName", "getTaskName",
				"getTeamName" }, model.getTasks(), new ManagedObjectTaskModel(
				RECYCLE_WORK_NAME, "RECYCLE TASK ONE", null, null),
				new ManagedObjectTaskModel(RECYCLE_WORK_NAME,
						"RECYCLE TASK TWO", "Recycle Team", null),
				new ManagedObjectTaskModel("WORK", "TASK", null, null));

		// Validate task flows
		assertList(new String[] { "getFlowId", "getInitialWorkName",
				"getInitialTaskName" }, model.getTasks().get(0).getFlows(),
				new ManagedObjectTaskFlowModel("0", null, "RECYCLE TASK TWO"),
				new ManagedObjectTaskFlowModel("1", null, null));
		assertList(new String[] { "getFlowId", "getInitialWorkName",
				"getInitialTaskName" }, model.getTasks().get(1).getFlows(),
				new ManagedObjectTaskFlowModel("0", null, "RECYCLE TASK ONE"));

		// Validate teams
		assertList(new String[] { "getTeamName" }, model.getTeams(),
				new ManagedObjectTeamModel("Recycle Team", null));
	}
}
