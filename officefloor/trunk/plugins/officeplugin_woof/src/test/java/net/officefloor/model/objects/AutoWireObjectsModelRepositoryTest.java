/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.model.objects;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.service.PropertyFileModel;
import net.officefloor.model.service.PropertyModel;
import net.officefloor.model.service.ServiceManagedObjectModel;
import net.officefloor.model.service.ServiceManagedObjectTypeModel;
import net.officefloor.model.service.ServiceTeamModel;
import net.officefloor.model.service.ServicesModel;

/**
 * Tests the marshaling/unmarshaling of the {@link ServiceModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireObjectsModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link ServiceModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {
		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "Services.services.xml"), null);
	}

	/**
	 * Ensure retrieve the {@link ServiceModel}.
	 */
	public void testRetrieveServices() throws Exception {

		// Load the Services
		ModelRepository repository = new ModelRepositoryImpl();
		ServicesModel services = new ServicesModel();
		services = repository.retrieve(services, this.configurationItem);

		// ----------------------------------------
		// Validate the managed objects
		// ----------------------------------------
		assertList(new String[] { "getServiceManagedObjectName",
				"getManagedObjectSourceClassName", "getX", "getY" },
				services.getServiceManagedObjects(),
				new ServiceManagedObjectModel("MANAGED_OBJECT",
						"net.example.ExampleManagedObjectSource", null, null,
						null, 100, 101));
		ServiceManagedObjectModel managedObject = services
				.getServiceManagedObjects().get(0);
		assertList(new String[] { "getName", "getValue" },
				managedObject.getProperties(), new PropertyModel("MO_ONE",
						"VALUE_ONE"), new PropertyModel("MO_TWO", "VALUE_TWO"));
		assertList(new String[] { "getPath" },
				managedObject.getPropertyFiles(), new PropertyFileModel(
						"example/object.properties"));
		assertList(new String[] {}, managedObject.getObjectTypes(),
				new ServiceManagedObjectTypeModel("net.orm.Session"),
				new ServiceManagedObjectTypeModel("net.orm.SessionLocal"));

		// ----------------------------------------
		// Validate the teams
		// ----------------------------------------
		assertList(new String[] { "getServiceTeamName",
				"getTeamSourceClassName", "getX", "getY" },
				services.getServiceTeams(), new ServiceTeamModel("TEAM",
						"net.example.ExampleTeamSource", null, null, 200, 201));
		ServiceTeamModel team = services.getServiceTeams().get(0);
		assertList(new String[] { "getName", "getValue" },
				team.getProperties(),
				new PropertyModel("TEAM_ONE", "VALUE_ONE"), new PropertyModel(
						"TEAM_TWO", "VALUE_TWO"));
		assertList(new String[] { "getPath" }, team.getPropertyFiles(),
				new PropertyFileModel("example/team.properties"));
	}

	/**
	 * Ensure able to round trip storing and retrieving the
	 * {@link ServicesModel}.
	 */
	public void testRoundTripStoreRetrieveServices() throws Exception {

		// Load the services
		ModelRepository repository = new ModelRepositoryImpl();
		ServicesModel services = new ServicesModel();
		services = repository.retrieve(services, this.configurationItem);

		// Store the services
		MemoryConfigurationItem contents = new MemoryConfigurationItem();
		repository.store(services, contents);

		// Reload the services
		ServicesModel reloadedServices = new ServicesModel();
		reloadedServices = repository.retrieve(reloadedServices, contents);

		// Validate round trip
		assertGraph(services, reloadedServices,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}