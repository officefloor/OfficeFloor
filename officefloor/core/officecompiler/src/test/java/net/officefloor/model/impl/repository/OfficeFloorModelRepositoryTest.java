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
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.TypeQualificationModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshaling/unmarshaling of the {@link OfficeFloorModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link OfficeFloorModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {

		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(
				this.findFile(this.getClass(), "TestOfficeFloor.officefloor.xml"), null);
	}

	/**
	 * Ensure retrieve the {@link OfficeFloorModel}.
	 */
	public void testRetrieveOfficeFloor() throws Exception {

		// Load the Office Floor
		ModelRepository repository = new ModelRepositoryImpl();
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		officeFloor = repository.retrieve(officeFloor, this.configurationItem);

		// ---------------------------------------
		// Validate the OfficeFloor attributes
		// ---------------------------------------
		assertProperties(officeFloor, new OfficeFloorModel(true, true), "getIsAutoWireObjects", "getIsAutoWireTeams");

		// ---------------------------------------
		// Validate the OfficeFloor suppliers
		// ---------------------------------------
		assertList(new String[] { "getOfficeFloorSupplierName", "getSupplierSourceClassName", "getX", "getY" },
				officeFloor.getOfficeFloorSuppliers(),
				new OfficeFloorSupplierModel("SUPPLIER", "net.example.ExampleSupplierSource", null, null, 0, 1));
		OfficeFloorSupplierModel supplier = officeFloor.getOfficeFloorSuppliers().get(0);
		assertList(new String[] { "getName", "getValue" }, supplier.getProperties(),
				new PropertyModel("SUPPLIER_ONE", "VALUE_ONE"), new PropertyModel("SUPPLIER_TWO", "VALUE_TWO"));

		// ----------------------------------------
		// Validate the OfficeFloor managed object sources
		// ----------------------------------------
		assertList(
				new String[] { "getOfficeFloorManagedObjectSourceName", "getManagedObjectSourceClassName",
						"getObjectType", "getTimeout", "getX", "getY" },
				officeFloor.getOfficeFloorManagedObjectSources(),
				new OfficeFloorManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
						"net.example.ExampleManagedObjectSource", "net.orm.Session", "10", null, null, null, null, null,
						null, null, null, null, 100, 101),
				new OfficeFloorManagedObjectSourceModel("SUPPLIED_MANAGED_OBJECT_SOURCE", null, "net.orm.Session", null,
						null, null, null, null, null, null, null, null, null, 110, 111));
		List<OfficeFloorManagedObjectSourceModel> moSources = officeFloor.getOfficeFloorManagedObjectSources();

		// Validate the sourced managed object source
		OfficeFloorManagedObjectSourceModel sourcedMoSource = moSources.get(0);
		assertOfficeFloorManagedObjectSource(sourcedMoSource);
		assertNull("Should not have supplier link as sourced", sourcedMoSource.getOfficeFloorSupplier());

		// Validate the supplied managed object source
		OfficeFloorManagedObjectSourceModel suppliedMoSource = moSources.get(1);
		assertOfficeFloorManagedObjectSource(suppliedMoSource);
		assertProperties(
				new OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel("SUPPLIER", "QUALIFIER",
						"net.orm.SpecificSession"),
				suppliedMoSource.getOfficeFloorSupplier(), "getOfficeFloorSupplierName", "getAutoWireQualifier",
				"getAutoWireType");

		// ----------------------------------------
		// Validate the OfficeFloor input managed objects
		// ----------------------------------------
		assertList(new String[] { "getOfficeFloorInputManagedObjectName", "getObjectType", "getX", "getY" },
				officeFloor.getOfficeFloorInputManagedObjects(), new OfficeFloorInputManagedObjectModel(
						"INPUT_MANAGED_OBJECT", "net.orm.Session", null, null, null, null, 200, 201));
		OfficeFloorInputManagedObjectModel inputMo = officeFloor.getOfficeFloorInputManagedObjects().get(0);
		assertProperties(
				new OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel("MANAGED_OBJECT_SOURCE"),
				inputMo.getBoundOfficeFloorManagedObjectSource(), "getOfficeFloorManagedObjectSourceName");

		// ----------------------------------------
		// Validate the OfficeFloor managed objects
		// ----------------------------------------
		assertList(new String[] { "getOfficeFloorManagedObjectName", "getManagedObjectScope", "getX", "getY" },
				officeFloor.getOfficeFloorManagedObjects(),
				new OfficeFloorManagedObjectModel("MANAGED_OBJECT_ONE", "THREAD", null, null, null, null, null, null,
						300, 301),
				new OfficeFloorManagedObjectModel("MANAGED_OBJECT_TWO", "PROCESS", null, null, null, null, null, null,
						310, 311));
		OfficeFloorManagedObjectModel mo = officeFloor.getOfficeFloorManagedObjects().get(0);

		// Link to managed object source
		assertProperties(new OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel("MANAGED_OBJECT_SOURCE"),
				mo.getOfficeFloorManagedObjectSource(), "getOfficeFloorManagedObjectSourceName");

		// Type qualifications
		assertList(new String[] { "getQualifier", "getType" }, mo.getTypeQualifications(),
				new TypeQualificationModel("QUALIFIED", "java.sql.SpecificConnection"),
				new TypeQualificationModel(null, "java.sql.GenericConnection"));

		// Dependencies
		assertList(new String[] { "getOfficeFloorManagedObjectDependencyName", "getDependencyType" },
				mo.getOfficeFloorManagedObjectDependencies(),
				new OfficeFloorManagedObjectDependencyModel("DEPENDENCY_ONE", Connection.class.getName()),
				new OfficeFloorManagedObjectDependencyModel("DEPENDENCY_TWO", "net.orm.Session"));
		OfficeFloorManagedObjectDependencyModel dependencyOne = mo.getOfficeFloorManagedObjectDependencies().get(0);
		assertProperties(new OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel("MANAGED_OBJECT_TWO"),
				dependencyOne.getOfficeFloorManagedObject(), "getOfficeFloorManagedObjectName");
		OfficeFloorManagedObjectDependencyModel dependencyTwo = mo.getOfficeFloorManagedObjectDependencies().get(1);
		assertProperties(
				new OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel("INPUT_MANAGED_OBJECT"),
				dependencyTwo.getOfficeFloorInputManagedObject(), "getOfficeFloorInputManagedObjectName");

		// ----------------------------------------
		// Validate the OfficeFloor teams
		// ----------------------------------------
		assertList(new String[] { "getOfficeFloorTeamName", "getTeamSourceClassName", "getX", "getY" },
				officeFloor.getOfficeFloorTeams(),
				new OfficeFloorTeamModel("TEAM", "net.example.ExampleTeamSource", null, null, null, null, 400, 401));
		OfficeFloorTeamModel team = officeFloor.getOfficeFloorTeams().get(0);
		assertList(new String[] { "getName", "getValue" }, team.getProperties(),
				new PropertyModel("TEAM_ONE", "VALUE_ONE"), new PropertyModel("TEAM_TWO", "VALUE_TWO"));
		assertList(new String[] { "getQualifier", "getType" }, team.getTypeQualifications(),
				new TypeQualificationModel("QUALIFIED", "java.sql.SpecificStatement"),
				new TypeQualificationModel(null, "java.sql.GenericStatement"));

		// ----------------------------------------
		// Validate the deployed offices
		// ----------------------------------------
		assertList(
				new String[] { "getDeployedOfficeName", "getOfficeSourceClassName", "getOfficeLocation", "getX",
						"getY" },
				officeFloor.getDeployedOffices(), new DeployedOfficeModel("OFFICE", "net.example.ExampleOfficeSource",
						"OFFICE_LOCATION", null, null, null, null, null, 400, 401));
		DeployedOfficeModel office = officeFloor.getDeployedOffices().get(0);
		assertList(new String[] { "getName", "getValue" }, office.getProperties(),
				new PropertyModel("OFFICE_ONE", "VALUE_ONE"), new PropertyModel("OFFICE_TWO", "VALUE_TWO"));

		// Deployed office objects
		assertList(new String[] { "getDeployedOfficeObjectName", "getObjectType" }, office.getDeployedOfficeObjects(),
				new DeployedOfficeObjectModel("OBJECT_A", "net.orm.Session"),
				new DeployedOfficeObjectModel("OBJECT_B", "net.orm.Session"));
		DeployedOfficeObjectModel officeObjectA = office.getDeployedOfficeObjects().get(0);
		assertProperties(new DeployedOfficeObjectToOfficeFloorManagedObjectModel("MANAGED_OBJECT"),
				officeObjectA.getOfficeFloorManagedObject(), "getOfficeFloorManagedObjectName");
		DeployedOfficeObjectModel officeObjectB = office.getDeployedOfficeObjects().get(1);
		assertProperties(new DeployedOfficeObjectToOfficeFloorInputManagedObjectModel("INPUT_MANAGED_OBJECT"),
				officeObjectB.getOfficeFloorInputManagedObject(), "getOfficeFloorInputManagedObjectName");

		// Deployed office inputs
		assertList(new String[] { "getSectionName", "getSectionInputName", "getParameterType" },
				office.getDeployedOfficeInputs(),
				new DeployedOfficeInputModel("SECTION", "INPUT", Integer.class.getName()));

		// Deployed office team
		assertList(new String[] { "getDeployedOfficeTeamName" }, office.getDeployedOfficeTeams(),
				new DeployedOfficeTeamModel("OFFICE_TEAM"));
		DeployedOfficeTeamModel officeTeam = office.getDeployedOfficeTeams().get(0);
		assertProperties(officeTeam.getOfficeFloorTeam(), new DeployedOfficeTeamToOfficeFloorTeamModel("TEAM"),
				"getOfficeFloorTeamName");
	}

	/**
	 * Asserts common details of the {@link OfficeFloorManagedObjectSourceModel}
	 * (by source and supplied).
	 * 
	 * @param moSource
	 *            {@link OfficeFloorManagedObjectSourceModel}.
	 */
	private static void assertOfficeFloorManagedObjectSource(OfficeFloorManagedObjectSourceModel moSource) {

		// Properties
		assertList(new String[] { "getName", "getValue" }, moSource.getProperties(),
				new PropertyModel("MO_ONE", "VALUE_ONE"), new PropertyModel("MO_TWO", "VALUE_TWO"));

		// Managing office
		assertProperties(new OfficeFloorManagedObjectSourceToDeployedOfficeModel("OFFICE"),
				moSource.getManagingOffice(), "getManagingOfficeName");

		// Input managed object and dependencies
		assertProperties(new OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel("INPUT_MANAGED_OBJECT"),
				moSource.getOfficeFloorInputManagedObject(), "getOfficeFloorInputManagedObjectName");
		assertList(new String[] { "getOfficeFloorManagedObjectSourceInputDependencyName", "getDependencyType" },
				moSource.getOfficeFloorManagedObjectSourceInputDependencies(),
				new OfficeFloorManagedObjectSourceInputDependencyModel("INPUT_DEPENDENCY", "java.sql.Connection"));
		OfficeFloorManagedObjectSourceInputDependencyModel inputDependency = moSource
				.getOfficeFloorManagedObjectSourceInputDependencies().get(0);
		assertProperties(
				new OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel("MANAGED_OBJECT_TWO"),
				inputDependency.getOfficeFloorManagedObject(), "getOfficeFloorManagedObjectName");

		// Flows
		assertList(new String[] { "getOfficeFloorManagedObjectSourceFlowName", "getArgumentType" },
				moSource.getOfficeFloorManagedObjectSourceFlows(),
				new OfficeFloorManagedObjectSourceFlowModel("FLOW", Integer.class.getName()));
		OfficeFloorManagedObjectSourceFlowModel flow = moSource.getOfficeFloorManagedObjectSourceFlows().get(0);
		assertProperties(new OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel("OFFICE", "SECTION", "INPUT"),
				flow.getDeployedOfficeInput(), "getDeployedOfficeName", "getSectionName", "getSectionInputName");

		// Teams
		assertList(new String[] { "getOfficeFloorManagedObjectSourceTeamName" },
				moSource.getOfficeFloorManagedObjectSourceTeams(),
				new OfficeFloorManagedObjectSourceTeamModel("MO_TEAM"));
		OfficeFloorManagedObjectSourceTeamModel mosTeam = moSource.getOfficeFloorManagedObjectSourceTeams().get(0);
		assertProperties(new OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel("TEAM"),
				mosTeam.getOfficeFloorTeam(), "getOfficeFloorTeamName");
	}

	/**
	 * Ensure able to round trip storing and retrieving the
	 * {@link OfficeFloorModel}.
	 */
	public void testRoundTripStoreRetrieveOfficeFloor() throws Exception {

		// Load the Office Floor
		ModelRepository repository = new ModelRepositoryImpl();
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		officeFloor = repository.retrieve(officeFloor, this.configurationItem);

		// Store the Office Floor
		MemoryConfigurationItem contents = new MemoryConfigurationItem();
		repository.store(officeFloor, contents);

		// Reload the Office Floor
		OfficeFloorModel reloadedOfficeFloor = new OfficeFloorModel();
		reloadedOfficeFloor = repository.retrieve(reloadedOfficeFloor, contents);

		// Validate round trip
		assertGraph(officeFloor, reloadedOfficeFloor, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}