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
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.DeskManagedObjectDependencyModel;
import net.officefloor.model.section.DeskManagedObjectDependencyToDeskManagedObjectModel;
import net.officefloor.model.section.DeskManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.DeskManagedObjectModel;
import net.officefloor.model.section.DeskManagedObjectSourceFlowModel;
import net.officefloor.model.section.DeskManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.DeskManagedObjectSourceFlowToFunctionModel;
import net.officefloor.model.section.DeskManagedObjectSourceModel;
import net.officefloor.model.section.DeskManagedObjectToDeskManagedObjectSourceModel;
import net.officefloor.model.section.DeskModel;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToDeskManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.PropertyModel;

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

	@Override
	protected void setUp() throws Exception {

		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(this.getClass(), "TestDesk.desk.xml"),
				null);
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
		assertList(new String[] { "getExternalManagedObjectName", "getObjectType", "getX", "getY" },
				desk.getExternalManagedObjects(),
				new ExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT", "java.lang.String", null, null, 100, 101));

		// ----------------------------------------
		// Validate the Managed Object Sources
		// ----------------------------------------
		assertList(
				new String[] { "getDeskManagedObjectSourceName", "getManagedObjectSourceClassName", "getObjectType",
						"getTimeout", "getX", "getY" },
				desk.getDeskManagedObjectSources(), new DeskManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
						"net.example.ExampleManagedObjectSource", "net.orm.Session", "10", null, null, null, 200, 201));
		DeskManagedObjectSourceModel mos = desk.getDeskManagedObjectSources().get(0);
		assertList(new String[] { "getName", "getValue" }, mos.getProperties(),
				new PropertyModel("MO_ONE", "VALUE_ONE"), new PropertyModel("MO_TWO", "VALUE_TWO"));
		assertList(new String[] { "getDeskManagedObjectSourceFlowName", "getArgumentType" },
				mos.getDeskManagedObjectSourceFlows(),
				new DeskManagedObjectSourceFlowModel("FLOW_ONE", String.class.getName()),
				new DeskManagedObjectSourceFlowModel("FLOW_TWO", Integer.class.getName()));
		DeskManagedObjectSourceFlowModel flowOne = mos.getDeskManagedObjectSourceFlows().get(0);
		assertProperties(new DeskManagedObjectSourceFlowToExternalFlowModel("flow"), flowOne.getExternalFlow(),
				"getExternalFlowName");
		DeskManagedObjectSourceFlowModel flowTwo = mos.getDeskManagedObjectSourceFlows().get(1);
		assertProperties(new DeskManagedObjectSourceFlowToFunctionModel("functionOne"), flowTwo.getFunction(),
				"getFunctionName");

		// ----------------------------------------
		// Validate the Managed Objects
		// ----------------------------------------
		assertList(new String[] { "getDeskManagedObjectName", "getManagedObjectScope", "getX", "getY" },
				desk.getDeskManagedObjects(),
				new DeskManagedObjectModel("MANAGED_OBJECT_ONE", "THREAD", null, null, null, null, 300, 301),
				new DeskManagedObjectModel("MANAGED_OBJECT_TWO", "PROCESS", null, null, null, null, 310, 311));
		DeskManagedObjectModel mo = desk.getDeskManagedObjects().get(0);
		assertProperties(new DeskManagedObjectToDeskManagedObjectSourceModel("MANAGED_OBJECT_SOURCE"),
				mo.getDeskManagedObjectSource(), "getDeskManagedObjectSourceName");
		assertList(new String[] { "getDeskManagedObjectDependencyName", "getDependencyType" },
				mo.getDeskManagedObjectDependencies(),
				new DeskManagedObjectDependencyModel("DEPENDENCY_ONE", Object.class.getName()),
				new DeskManagedObjectDependencyModel("DEPENDENCY_TWO", Connection.class.getName()));
		DeskManagedObjectDependencyModel dependencyOne = mo.getDeskManagedObjectDependencies().get(0);
		assertProperties(new DeskManagedObjectDependencyToExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT"),
				dependencyOne.getExternalManagedObject(), "getExternalManagedObjectName");
		DeskManagedObjectDependencyModel dependencyTwo = mo.getDeskManagedObjectDependencies().get(1);
		assertProperties(new DeskManagedObjectDependencyToDeskManagedObjectModel("MANAGED_OBJECT_TWO"),
				dependencyTwo.getDeskManagedObject(), "getDeskManagedObject");

		// ----------------------------------------
		// Validate the External Flows
		// ----------------------------------------
		assertList(new String[] { "getExternalFlowName", "getArgumentType", "getX", "getY" }, desk.getExternalFlows(),
				new ExternalFlowModel("flow", "java.lang.Object", null, null, null, null, 400, 401),
				new ExternalFlowModel("escalation", "java.lang.Throwable", null, null, null, null, 410, 411));

		// ----------------------------------------
		// Validate the Function Namespace
		// ----------------------------------------
		assertList(new String[] { "getFunctionNamespaceName", "getManagedFunctionSourceClassName", "getX", "getY" },
				desk.getFunctionNamespaces(), new FunctionNamespaceModel("namespace",
						"net.example.ExampleManagedFunctionSource", null, null, 500, 501));
		FunctionNamespaceModel namespace = desk.getFunctionNamespaces().get(0);

		// Validate properties of namespace
		assertList(new String[] { "getName", "getValue" }, namespace.getProperties(),
				new PropertyModel("property.one", "VALUE_ONE"), new PropertyModel("property.two", "VALUE_TWO"));

		// Validate managed functions
		List<ManagedFunctionModel> managedFunctions = new LinkedList<ManagedFunctionModel>();
		managedFunctions.add(new ManagedFunctionModel("managedFunctionOne"));
		managedFunctions.add(new ManagedFunctionModel("managedFunctionTwo"));
		managedFunctions.add(new ManagedFunctionModel("managedFunctionThree"));
		managedFunctions.add(new ManagedFunctionModel("managedFunctionFour"));
		assertList(new String[] { "getManagedFunctionName" }, desk.getFunctionNamespaces().get(0).getManagedFunctions(),
				managedFunctions.toArray(new ManagedFunctionModel[0]));
		ManagedFunctionModel managedFunctionOne = namespace.getManagedFunctions().get(0);
		ManagedFunctionModel managedFunctionTwo = namespace.getManagedFunctions().get(1);
		ManagedFunctionModel managedFunctionThree = namespace.getManagedFunctions().get(2);
		ManagedFunctionModel managedFunctionFour = namespace.getManagedFunctions().get(3);

		// Validate objects on functions
		String[] objectValidate = new String[] { "getObjectName", "getKey", "getObjectType", "getIsParameter" };
		assertList(objectValidate, managedFunctionOne.getManagedFunctionObjects(),
				new ManagedFunctionObjectModel("ONE", "ONE", "java.lang.String", false));
		assertList(objectValidate, managedFunctionTwo.getManagedFunctionObjects(),
				new ManagedFunctionObjectModel("0", null, "java.lang.Integer", true),
				new ManagedFunctionObjectModel("1", null, "java.lang.String", false),
				new ManagedFunctionObjectModel("2", null, "net.orm.Session", false));
		assertList(objectValidate, managedFunctionThree.getManagedFunctionObjects(),
				new ManagedFunctionObjectModel("parameter", null, "java.lang.Throwable", true));
		assertList(objectValidate, managedFunctionFour.getManagedFunctionObjects());

		// Validate function object connections
		assertProperties(new ManagedFunctionObjectToExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT"),
				managedFunctionOne.getManagedFunctionObjects().get(0).getExternalManagedObject(),
				"getExternalManagedObjectName");
		assertNull(managedFunctionTwo.getManagedFunctionObjects().get(0).getExternalManagedObject());
		assertProperties(new ManagedFunctionObjectToExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT"),
				managedFunctionTwo.getManagedFunctionObjects().get(1).getExternalManagedObject(),
				"getExternalManagedObjectName");
		assertNull(managedFunctionThree.getManagedFunctionObjects().get(0).getExternalManagedObject());
		assertProperties(new ManagedFunctionObjectToDeskManagedObjectModel("MANAGED_OBJECT"),
				managedFunctionTwo.getManagedFunctionObjects().get(2).getDeskManagedObject(),
				"getDeskManagedObjectName");

		// ----------------------------------------
		// Validate the Functions
		// ----------------------------------------
		List<FunctionModel> functions = new LinkedList<FunctionModel>();
		functions.add(new FunctionModel("functionOne", true, "namespace", "managedFunctionOne", "java.lang.Integer",
				null, null, null, null, null, null, null, null, null, 600, 601));
		functions.add(new FunctionModel("functionTwo", false, "namespace", "managedFunctionTwo", null, null, null, null,
				null, null, null, null, null, null, 610, 611));
		functions.add(new FunctionModel("functionThree", false, "namespace", "managedFunctionThree",
				"java.lang.Integer", null, null, null, null, null, null, null, null, null, 620, 621));
		functions.add(new FunctionModel("functionFour", false, "namespace", "managedFunctionFour", null, null, null,
				null, null, null, null, null, null, null, 630, 631));
		assertList(new String[] { "getFunctionName", "getFunctionNamespaceName", "getManagedFunctionName",
				"getReturnType", "getX", "getY" }, desk.getFunctions(), functions.toArray(new FunctionModel[0]));
		FunctionModel functionOne = desk.getFunctions().get(0);
		FunctionModel functionTwo = desk.getFunctions().get(1);
		FunctionModel functionThree = desk.getFunctions().get(2);
		FunctionModel functionFour = desk.getFunctions().get(3);

		// Validate the flows (keyed and indexed)
		String[] flowValidation = new String[] { "getFlowName", "getKey", "getArgumentType" };
		assertList(flowValidation, functionOne.getFunctionFlows(),
				new FunctionFlowModel("First", "ONE", "java.lang.Double"),
				new FunctionFlowModel("Second", "TWO", "java.lang.Integer"),
				new FunctionFlowModel("Third", "THREE", null));
		assertEquals(0, functionTwo.getFunctionFlows().size());
		assertList(flowValidation, functionThree.getFunctionFlows(),
				new FunctionFlowModel("0", null, "java.lang.Integer"),
				new FunctionFlowModel("1", null, "java.lang.Double"));
		assertEquals(0, functionFour.getFunctionFlows().size());

		// Validate the flow connections
		FunctionFlowModel functionOneFirst = functionOne.getFunctionFlows().get(0);
		assertProperties(new FunctionFlowToExternalFlowModel("flow", false), functionOneFirst.getExternalFlow(),
				"getExternalFlowName", "getIsSpawnThreadState");
		assertNull(functionOneFirst.getFunction());
		FunctionFlowModel functionOneSecond = functionOne.getFunctionFlows().get(1);
		assertNull(functionOneSecond.getExternalFlow());
		assertProperties(new FunctionFlowToFunctionModel("functionTwo", true), functionOneSecond.getFunction(),
				"getFunctionName", "getIsSpawnThreadState");

		// Validate next flows
		assertProperties(new FunctionToNextFunctionModel("functionTwo"), functionOne.getNextFunction(),
				"getNextFunctionName");
		assertNull(functionOne.getNextExternalFlow());
		assertNull(functionTwo.getNextFunction());
		assertProperties(new FunctionToNextExternalFlowModel("flow"), functionTwo.getNextExternalFlow(),
				"getExternalFlowName");

		// Validate escalations
		String[] escalationValidate = new String[] { "getEscalationType" };
		assertList(escalationValidate, functionOne.getFunctionEscalations(),
				new FunctionEscalationModel("java.io.IOException"),
				new FunctionEscalationModel("java.sql.SQLException"),
				new FunctionEscalationModel("java.lang.NullPointerException"));
		assertList(escalationValidate, functionTwo.getFunctionEscalations());

		// Validate escalation connections
		FunctionEscalationModel functionOneIO = functionOne.getFunctionEscalations().get(0);
		assertProperties(functionOneIO.getFunction(), new FunctionEscalationToFunctionModel("functionThree"),
				"getFunctionName");
		assertNull(functionOneIO.getExternalFlow());
		FunctionEscalationModel functionOneSQL = functionOne.getFunctionEscalations().get(1);
		assertNull(functionOneSQL.getFunction());
		assertProperties(functionOneSQL.getExternalFlow(), new FunctionEscalationToExternalFlowModel("escalation"),
				"getExternalFlowName");
		FunctionEscalationModel functionOneNull = functionOne.getFunctionEscalations().get(2);
		assertNull(functionOneNull.getFunction());
		assertNull(functionOneNull.getExternalFlow());
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
		assertGraph(desk, reloadedDesk, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}