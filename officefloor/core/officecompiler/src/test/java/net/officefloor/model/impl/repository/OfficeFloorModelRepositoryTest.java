/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.model.impl.repository;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.FileSystemConfigurationContext;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorExecutionStrategyModel;
import net.officefloor.model.officefloor.OfficeFloorExecutiveModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectPoolModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceExecutionStrategyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceExecutionStrategyToOfficeFloorExecutionStrategyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFunctionDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFunctionDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceStartAfterOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceStartBeforeOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorManagedObjectPoolModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.TypeQualificationModel;
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
		File configurationFile = this.findFile(this.getClass(), "TestOfficeFloor.officefloor.xml");
		this.configurationItem = FileSystemConfigurationContext.createWritableConfigurationItem(configurationFile);
	}

	/**
	 * Ensure retrieve the {@link OfficeFloorModel}.
	 */
	public void testRetrieveOfficeFloor() throws Exception {

		// Load the OfficeFloor
		ModelRepository repository = new ModelRepositoryImpl();
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		repository.retrieve(officeFloor, this.configurationItem);

		// ---------------------------------------
		// Validate the OfficeFloor attributes
		// ---------------------------------------
		assertProperties(officeFloor, new OfficeFloorModel(true, true), "getIsAutoWireObjects", "getIsAutoWireTeams");

		// ---------------------------------------
		// Validate the OfficeFloor suppliers
		// ---------------------------------------
		assertList(new String[] { "getOfficeFloorSupplierName", "getSupplierSourceClassName", "getX", "getY" },
				officeFloor.getOfficeFloorSuppliers(),
				new OfficeFloorSupplierModel("SUPPLIER", "net.example.ExampleSupplierSource", 100, 101));
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
						"net.example.ExampleManagedObjectSource", "net.orm.Session", "10", 200, 201),
				new OfficeFloorManagedObjectSourceModel("SUPPLIED_MANAGED_OBJECT_SOURCE", null, "net.orm.Session", null,
						210, 211));
		List<OfficeFloorManagedObjectSourceModel> moSources = officeFloor.getOfficeFloorManagedObjectSources();

		// Validate the sourced managed object source
		OfficeFloorManagedObjectSourceModel sourcedMoSource = moSources.get(0);
		assertOfficeFloorManagedObjectSource(sourcedMoSource);
		assertNull("Should not have supplier link as sourced", sourcedMoSource.getOfficeFloorSupplier());

		// Validate linked to managed object pool
		assertProperties(new OfficeFloorManagedObjectSourceToOfficeFloorManagedObjectPoolModel("MANAGED_OBJECT_POOL"),
				sourcedMoSource.getOfficeFloorManagedObjectPool(), "getOfficeFloorManagedObjectPoolName");

		// Validate the supplied managed object source
		OfficeFloorManagedObjectSourceModel suppliedMoSource = moSources.get(1);
		assertOfficeFloorManagedObjectSource(suppliedMoSource);
		assertProperties(
				new OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel("SUPPLIER", "QUALIFIER",
						"net.orm.SpecificSession"),
				suppliedMoSource.getOfficeFloorSupplier(), "getOfficeFloorSupplierName", "getQualifier", "getType");

		// Validate the start before
		assertList(new String[] { "getOfficeFloorManagedObjectSourceName", "getManagedObjectType" },
				sourcedMoSource.getStartBeforeEarliers(),
				new OfficeFloorManagedObjectSourceStartBeforeOfficeFloorManagedObjectSourceModel(
						"SUPPLIED_MANAGED_OBJECT_SOURCE", null),
				new OfficeFloorManagedObjectSourceStartBeforeOfficeFloorManagedObjectSourceModel(null,
						"net.orm.Session"));

		// Validate the start after
		assertList(new String[] { "getOfficeFloorManagedObjectSourceName", "getManagedObjectType" },
				suppliedMoSource.getStartAfterLaters(),
				new OfficeFloorManagedObjectSourceStartAfterOfficeFloorManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
						null),
				new OfficeFloorManagedObjectSourceStartAfterOfficeFloorManagedObjectSourceModel(null,
						"net.orm.Session"));

		// ----------------------------------------
		// Validate the OfficeFloor input managed objects
		// ----------------------------------------
		assertList(new String[] { "getOfficeFloorInputManagedObjectName", "getObjectType", "getX", "getY" },
				officeFloor.getOfficeFloorInputManagedObjects(),
				new OfficeFloorInputManagedObjectModel("INPUT_MANAGED_OBJECT", "net.orm.Session", 300, 301));
		OfficeFloorInputManagedObjectModel inputMo = officeFloor.getOfficeFloorInputManagedObjects().get(0);
		assertProperties(
				new OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel("MANAGED_OBJECT_SOURCE"),
				inputMo.getBoundOfficeFloorManagedObjectSource(), "getOfficeFloorManagedObjectSourceName");
		assertList(new String[] { "getQualifier", "getType" }, inputMo.getTypeQualifications(),
				new TypeQualificationModel("QUALIFIED", "java.http.InputRequest"),
				new TypeQualificationModel(null, "java.http.GenericRequest"));

		// ----------------------------------------
		// Validate the OfficeFloor managed objects
		// ----------------------------------------
		assertList(new String[] { "getOfficeFloorManagedObjectName", "getManagedObjectScope", "getX", "getY" },
				officeFloor.getOfficeFloorManagedObjects(),
				new OfficeFloorManagedObjectModel("MANAGED_OBJECT_ONE", "THREAD", 400, 401),
				new OfficeFloorManagedObjectModel("MANAGED_OBJECT_TWO", "PROCESS", 410, 411));
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

		// ----------------------------------------------
		// Validate the OfficeFloor managed object pools
		// ----------------------------------------------
		assertList(
				new String[] { "getOfficeFloorManagedObjectPoolName", "getManagedObjectPoolSourceClassName", "getX",
						"getY" },
				officeFloor.getOfficeFloorManagedObjectPools(), new OfficeFloorManagedObjectPoolModel(
						"MANAGED_OBJECT_POOL", "net.example.ExampleManagedObjectPoolSource", 500, 501));
		OfficeFloorManagedObjectPoolModel pool = officeFloor.getOfficeFloorManagedObjectPools().get(0);
		assertList(new String[] { "getName", "getValue" }, pool.getProperties(),
				new PropertyModel("POOL_ONE", "VALUE_ONE"), new PropertyModel("POOL_TWO", "VALUE_TWO"));

		// ----------------------------------------
		// Validate the OfficeFloor executive
		// ----------------------------------------
		OfficeFloorExecutiveModel executive = officeFloor.getOfficeFloorExecutive();
		assertProperties(new OfficeFloorExecutiveModel("net.example.ExampleExecutiveSource", 600, 601), executive,
				"getExecutiveSourceClassName", "getX", "getY");
		assertList(new String[] { "getName", "getValue" }, executive.getProperties(),
				new PropertyModel("EXECUTION_ONE", "VALUE_ONE"), new PropertyModel("EXECUTION_TWO", "VALUE_TWO"));
		assertList(new String[] { "getExecutionStrategyName" }, executive.getExecutionStrategies(),
				new OfficeFloorExecutionStrategyModel("EXECUTION_STRATEGY"));

		// ----------------------------------------
		// Validate the OfficeFloor teams
		// ----------------------------------------
		assertList(
				new String[] { "getOfficeFloorTeamName", "getTeamSize", "getTeamSourceClassName",
						"getRequestNoTeamOversight", "getX", "getY" },
				officeFloor.getOfficeFloorTeams(),
				new OfficeFloorTeamModel("TEAM", 50, "net.example.ExampleTeamSource", true, 700, 701));
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
				officeFloor.getDeployedOffices(),
				new DeployedOfficeModel("OFFICE", "net.example.ExampleOfficeSource", "OFFICE_LOCATION", 800, 801));
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
	 * Asserts common details of the {@link OfficeFloorManagedObjectSourceModel} (by
	 * source and supplied).
	 * 
	 * @param moSource {@link OfficeFloorManagedObjectSourceModel}.
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

		// Function managed object and dependencies
		assertList(new String[] { "getOfficeFloorManagedObjectSourceFunctionDependencyName", "getDependencyType" },
				moSource.getOfficeFloorManagedObjectSourceFunctionDependencies(),
				new OfficeFloorManagedObjectSourceFunctionDependencyModel("FUNCTION_DEPENDENCY",
						"java.net.URLConnection"));
		OfficeFloorManagedObjectSourceFunctionDependencyModel functionDependency = moSource
				.getOfficeFloorManagedObjectSourceFunctionDependencies().get(0);
		assertProperties(
				new OfficeFloorManagedObjectSourceFunctionDependencyToOfficeFloorManagedObjectModel(
						"MANAGED_OBJECT_THREE"),
				functionDependency.getOfficeFloorManagedObject(), "getOfficeFloorManagedObjectName");

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

		// Execution Strategies
		assertList(new String[] { "getOfficeFloorManagedObjectSourceExecutionStrategyName" },
				moSource.getOfficeFloorManagedObjectSourceExecutionStrategies(),
				new OfficeFloorManagedObjectSourceExecutionStrategyModel("MO_EXECUTION_STRATEGY"));
		OfficeFloorManagedObjectSourceExecutionStrategyModel mosExecutionStrategy = moSource
				.getOfficeFloorManagedObjectSourceExecutionStrategies().get(0);
		assertProperties(
				new OfficeFloorManagedObjectSourceExecutionStrategyToOfficeFloorExecutionStrategyModel(
						"EXECUTION_STRATEGY"),
				mosExecutionStrategy.getOfficeFloorExecutionStrategy(), "getOfficeFloorExecutionStrategyName");
	}

	/**
	 * Ensure able to round trip storing and retrieving the
	 * {@link OfficeFloorModel}.
	 */
	public void testRoundTripStoreRetrieveOfficeFloor() throws Exception {

		// Load the Office Floor
		ModelRepository repository = new ModelRepositoryImpl();
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		repository.retrieve(officeFloor, this.configurationItem);

		// Store the OfficeFloor
		WritableConfigurationItem configuration = MemoryConfigurationContext.createWritableConfigurationItem("test");
		repository.store(officeFloor, configuration);

		// Reload the Office Floor
		OfficeFloorModel reloadedOfficeFloor = new OfficeFloorModel();
		repository.retrieve(reloadedOfficeFloor, configuration);

		// Validate round trip
		assertGraph(officeFloor, reloadedOfficeFloor, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}
