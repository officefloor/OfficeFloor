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
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionEscalationToSubSectionInputModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionFlowToSubSectionInputModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.FunctionToNextSubSectionInputModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectPoolModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectSourceToSectionManagedObjectPoolModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToFunctionModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

/**
 * Tests the marshalling/unmarshalling of the {@link SectionModel} via the
 * {@link ModelRepository}.
 *
 * @author Daniel Sagenschneider
 */
public class SectionModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link SectionModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {

		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(
				this.findFile(this.getClass(), "TestSection.section.xml"), null);
	}

	/**
	 * Ensure can retrieve the {@link SectionModel}.
	 */
	public void testRetrieveSection() throws Exception {

		// Load the section
		ModelRepository repository = new ModelRepositoryImpl();
		SectionModel section = new SectionModel();
		section = repository.retrieve(section, this.configurationItem);

		// ----------------------------------------------
		// Validate the external managed objects
		// ----------------------------------------------
		assertList(new String[] { "getExternalManagedObjectName", "getObjectType", "getX", "getY" },
				section.getExternalManagedObjects(),
				new ExternalManagedObjectModel("MO", Object.class.getName(), null, null, null, 100, 101));

		// ----------------------------------------------
		// Validate the managed object sources
		// ----------------------------------------------
		assertList(
				new String[] { "getSectionManagedObjectSourceName", "getManagedObjectSourceClassName", "getObjectType",
						"getTimeout", "getX", "getY" },
				section.getSectionManagedObjectSources(),
				new SectionManagedObjectSourceModel("MANAGED_OBJECT_SOURCE", "net.example.ExampleManagedObjectSource",
						"net.orm.Session", "10", null, null, null, null, 200, 201));
		SectionManagedObjectSourceModel mos = section.getSectionManagedObjectSources().get(0);
		assertList(new String[] { "getName", "getValue" }, mos.getProperties(),
				new PropertyModel("MO_ONE", "VALUE_ONE"), new PropertyModel("MO_TWO", "VALUE_TWO"));
		assertProperties(new SectionManagedObjectSourceToSectionManagedObjectPoolModel("MANAGED_OBJECT_POOL"),
				mos.getSectionManagedObjectPool(), "getSectionManagedObjectPoolName");
		assertList(new String[] { "getSectionManagedObjectSourceFlowName", "getArgumentType" },
				mos.getSectionManagedObjectSourceFlows(),
				new SectionManagedObjectSourceFlowModel("FLOW_ONE", String.class.getName()),
				new SectionManagedObjectSourceFlowModel("FLOW_TWO", Integer.class.getName()));
		SectionManagedObjectSourceFlowModel flowOne = mos.getSectionManagedObjectSourceFlows().get(0);
		assertProperties(new SectionManagedObjectSourceFlowToExternalFlowModel("FLOW"), flowOne.getExternalFlow(),
				"getExternalFlowName");
		SectionManagedObjectSourceFlowModel flowTwo = mos.getSectionManagedObjectSourceFlows().get(1);
		assertProperties(new SectionManagedObjectSourceFlowToSubSectionInputModel("SECTION_A", "INPUT_A"),
				flowTwo.getSubSectionInput(), "getSubSectionName", "getSubSectionInputName");

		// ----------------------------------------------
		// Validate the managed objects
		// ----------------------------------------------
		assertList(new String[] { "getSectionManagedObjectName", "getManagedObjectScope", "getX", "getY" },
				section.getSectionManagedObjects(),
				new SectionManagedObjectModel("MANAGED_OBJECT_ONE", "THREAD", null, null, null, null, null, 300, 301),
				new SectionManagedObjectModel("MANAGED_OBJECT_TWO", "PROCESS", null, null, null, null, null, 310, 311));
		SectionManagedObjectModel mo = section.getSectionManagedObjects().get(0);
		assertProperties(new SectionManagedObjectToSectionManagedObjectSourceModel("MANAGED_OBJECT_SOURCE"),
				mo.getSectionManagedObjectSource(), "getSectionManagedObjectSourceName");
		assertList(new String[] { "getSectionManagedObjectDependencyName", "getDependencyType" },
				mo.getSectionManagedObjectDependencies(),
				new SectionManagedObjectDependencyModel("DEPENDENCY_ONE", Object.class.getName()),
				new SectionManagedObjectDependencyModel("DEPENDENCY_TWO", Connection.class.getName()));
		SectionManagedObjectDependencyModel dependencyOne = mo.getSectionManagedObjectDependencies().get(0);
		assertProperties(new SectionManagedObjectDependencyToExternalManagedObjectModel("MO"),
				dependencyOne.getExternalManagedObject(), "getExternalManagedObjectName");
		SectionManagedObjectDependencyModel dependencyTwo = mo.getSectionManagedObjectDependencies().get(1);
		assertProperties(new SectionManagedObjectDependencyToSectionManagedObjectModel("MANAGED_OBJECT_TWO"),
				dependencyTwo.getSectionManagedObject(), "getSectionManagedObjectName");

		// ----------------------------------------------
		// Validate the managed object pools
		// ----------------------------------------------
		assertList(
				new String[] { "getSectionManagedObjectPoolName", "getManagedObjectPoolSourceClassName", "getX",
						"getY" },
				section.getSectionManagedObjectPools(), new SectionManagedObjectPoolModel("MANAGED_OBJECT_POOL",
						"net.example.ExampleManagedObjectPoolSource", null, null, 400, 401));
		SectionManagedObjectPoolModel pool = section.getSectionManagedObjectPools().get(0);
		assertList(new String[] { "getName", "getValue" }, pool.getProperties(),
				new PropertyModel("POOL_ONE", "VALUE_ONE"), new PropertyModel("POOL_TWO", "VALUE_TWO"));

		// ----------------------------------------------
		// Validate the external flows
		// ----------------------------------------------
		assertList(new String[] { "getExternalFlowName", "getArgumentType", "getX", "getY" },
				section.getExternalFlows(),
				new ExternalFlowModel("FLOW", String.class.getName(), null, null, null, null, null, 500, 501),
				new ExternalFlowModel("ESCALATION", "java.lang.Throwable", null, null, null, null, null, 510, 511));

		// ----------------------------------------
		// Validate the Function Namespace
		// ----------------------------------------
		assertList(new String[] { "getFunctionNamespaceName", "getManagedFunctionSourceClassName", "getX", "getY" },
				section.getFunctionNamespaces(), new FunctionNamespaceModel("function-namespace",
						"net.example.ExampleManagedFunctionSource", null, null, 600, 601));
		FunctionNamespaceModel namespace = section.getFunctionNamespaces().get(0);

		// Validate properties of namespace
		assertList(new String[] { "getName", "getValue" }, namespace.getProperties(),
				new PropertyModel("property.one", "VALUE_ONE"), new PropertyModel("property.two", "VALUE_TWO"));

		// Validate managed functions
		List<ManagedFunctionModel> managedFunctions = new LinkedList<ManagedFunctionModel>();
		managedFunctions.add(new ManagedFunctionModel("managedFunctionOne"));
		managedFunctions.add(new ManagedFunctionModel("managedFunctionTwo"));
		managedFunctions.add(new ManagedFunctionModel("managedFunctionThree"));
		managedFunctions.add(new ManagedFunctionModel("managedFunctionFour"));
		assertList(new String[] { "getManagedFunctionName" },
				section.getFunctionNamespaces().get(0).getManagedFunctions(),
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
		assertProperties(new ManagedFunctionObjectToSectionManagedObjectModel("MANAGED_OBJECT"),
				managedFunctionTwo.getManagedFunctionObjects().get(2).getSectionManagedObject(),
				"getSectionManagedObjectName");

		// ----------------------------------------
		// Validate the Functions
		// ----------------------------------------
		List<FunctionModel> functions = new LinkedList<FunctionModel>();
		functions.add(new FunctionModel("functionOne", true, "namespace", "managedFunctionOne", "java.lang.Integer",
				null, null, null, null, null, null, null, null, null, null, null, 700, 701));
		functions.add(new FunctionModel("functionTwo", false, "namespace", "managedFunctionTwo", null, null, null, null,
				null, null, null, null, null, null, null, null, 710, 711));
		functions.add(new FunctionModel("functionThree", false, "namespace", "managedFunctionThree",
				"java.lang.Integer", null, null, null, null, null, null, null, null, null, null, null, 720, 721));
		functions.add(new FunctionModel("functionFour", false, "namespace", "managedFunctionFour", null, null, null,
				null, null, null, null, null, null, null, null, null, 730, 731));
		assertList(new String[] { "getFunctionName", "getFunctionNamespaceName", "getManagedFunctionName",
				"getReturnType", "getX", "getY" }, section.getFunctions(), functions.toArray(new FunctionModel[0]));
		FunctionModel functionOne = section.getFunctions().get(0);
		FunctionModel functionTwo = section.getFunctions().get(1);
		FunctionModel functionThree = section.getFunctions().get(2);
		FunctionModel functionFour = section.getFunctions().get(3);

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
		assertProperties(new FunctionFlowToExternalFlowModel("flow", true), functionOneFirst.getExternalFlow(),
				"getExternalFlowName", "getIsSpawnThreadState");
		assertNull(functionOneFirst.getFunction());
		assertNull(functionOneFirst.getSubSectionInput());
		FunctionFlowModel functionOneSecond = functionOne.getFunctionFlows().get(1);
		assertProperties(new FunctionFlowToFunctionModel("functionTwo", true), functionOneSecond.getFunction(),
				"getFunctionName", "getIsSpawnThreadState");
		assertNull(functionOneSecond.getExternalFlow());
		assertNull(functionOneSecond.getSubSectionInput());
		FunctionFlowModel functionOneThird = functionOne.getFunctionFlows().get(2);
		assertProperties(new FunctionFlowToSubSectionInputModel("SECTION_A", "INPUT_A", true),
				functionOneThird.getSubSectionInput(), "getSubSectionName", "getSubSectionInputName",
				"getIsSpawnThreadState");
		assertNull(functionOneThird.getExternalFlow());
		assertNull(functionOneThird.getFunction());

		// Validate next flows
		assertProperties(new FunctionToNextExternalFlowModel("FLOW"), functionOne.getNextExternalFlow(),
				"getExternalFlowName");
		assertNull(functionOne.getNextFunction());
		assertNull(functionOne.getNextSubSectionInput());
		assertProperties(new FunctionToNextFunctionModel("functionTwo"), functionTwo.getNextFunction(),
				"getNextFunctionName");
		assertNull(functionTwo.getNextExternalFlow());
		assertNull(functionTwo.getNextSubSectionInput());
		assertProperties(new FunctionToNextSubSectionInputModel("SECTION_A", "INPUT_A"),
				functionThree.getNextSubSectionInput(), "getSubSectionName", "getSubSectionInputName");
		assertNull(functionThree.getNextExternalFlow());
		assertNull(functionThree.getNextFunction());

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
		assertProperties(functionOneNull.getSubSectionInput(),
				new FunctionEscalationToSubSectionInputModel("SECTION_C", "INPUT_C"), "getSubSectionName",
				"getSubSectionInputName");
		assertNull(functionOneNull.getFunction());
		assertNull(functionOneNull.getExternalFlow());

		// ----------------------------------------------
		// Validate the sub sections
		// ----------------------------------------------
		assertList(
				new String[] { "getSubSectionName", "getSectionSourceClassName", "getSectionLocation", "getX", "getY" },
				section.getSubSections(),
				new SubSectionModel("SECTION_A", "DESK", "DESK_LOCATION", null, null, null, null, 800, 801),
				new SubSectionModel("SECTION_B", "SECTION", "SECTION_LOCATION", null, null, null, null, 810, 811),
				new SubSectionModel("SECTION_C", "net.example.ExampleSectionSource", "EXAMPLE_LOCATION", null, null,
						null, null, 820, 821));
		SubSectionModel subSectionA = section.getSubSections().get(0);
		SubSectionModel subSectionB = section.getSubSections().get(1);
		SubSectionModel subSectionC = section.getSubSections().get(2);

		// Validate the properties
		String[] propertyValidation = new String[] { "getName", "getValue" };
		assertList(propertyValidation, subSectionA.getProperties(), new PropertyModel("name.one", "value.one"),
				new PropertyModel("name.two", "value.two"));
		assertList(propertyValidation, subSectionB.getProperties());

		// Validate the inputs
		String[] inputValidation = new String[] { "getSubSectionInputName", "getParameterType", "getIsPublic",
				"getPublicInputName" };
		assertList(inputValidation, subSectionA.getSubSectionInputs(),
				new SubSectionInputModel("INPUT_A", Integer.class.getName(), true, null),
				new SubSectionInputModel("INPUT_B", null, false, null));
		assertList(inputValidation, subSectionB.getSubSectionInputs(),
				new SubSectionInputModel("INPUT_A", Exception.class.getName(), true, "PUBLIC_INPUT_A"));
		assertList(inputValidation, subSectionC.getSubSectionInputs());

		// Validate the outputs
		String[] outputValidation = new String[] { "getSubSectionOutputName", "getArgumentType", "getEscalationOnly" };
		assertList(outputValidation, subSectionA.getSubSectionOutputs(),
				new SubSectionOutputModel("OUTPUT_A", String.class.getName(), false),
				new SubSectionOutputModel("OUTPUT_B", Exception.class.getName(), true),
				new SubSectionOutputModel("OUTPUT_C", null, false), new SubSectionOutputModel("OUTPUT_D", null, false));
		assertList(outputValidation, subSectionB.getSubSectionOutputs());
		SubSectionOutputModel outputA = subSectionA.getSubSectionOutputs().get(0);
		SubSectionOutputModel outputB = subSectionA.getSubSectionOutputs().get(1);
		SubSectionOutputModel outputC = subSectionA.getSubSectionOutputs().get(2);

		// Validate the output connections
		assertProperties(new SubSectionOutputToExternalFlowModel("FLOW"), outputA.getExternalFlow(),
				"getExternalFlowName");
		assertNull(outputA.getFunction());
		assertNull(outputA.getSubSectionInput());
		assertProperties(new SubSectionOutputToFunctionModel("functionOne"), outputB.getFunction(), "getFunctionName");
		assertNull(outputB.getExternalFlow());
		assertNull(outputB.getSubSectionInput());
		assertProperties(new SubSectionOutputToSubSectionInputModel("SECTION_B", "INPUT_A"),
				outputC.getSubSectionInput(), "getSubSectionName", "getSubSectionInputName");
		assertNull(outputC.getExternalFlow());
		assertNull(outputC.getFunction());

		// Validate the objects
		String[] objectValidation = new String[] {};
		assertList(objectValidation, subSectionA.getSubSectionObjects(),
				new SubSectionObjectModel("OBJECT_A", Object.class.getName()),
				new SubSectionObjectModel("OBJECT_B", Double.class.getName()),
				new SubSectionObjectModel("OBJECT_C", "net.orm.Session"));
		assertList(objectValidation, subSectionB.getSubSectionObjects());
		SubSectionObjectModel objectA = subSectionA.getSubSectionObjects().get(0);
		SubSectionObjectModel objectB = subSectionA.getSubSectionObjects().get(1);
		SubSectionObjectModel objectC = subSectionA.getSubSectionObjects().get(2);

		// Validate the object connections
		assertProperties(new SubSectionObjectToExternalManagedObjectModel("MO"), objectA.getExternalManagedObject(),
				"getExternalManagedObjectName");
		assertNull("Should not link object", objectB.getExternalManagedObject());
		assertNull("Should not link object", objectB.getSectionManagedObject());
		assertProperties(new SubSectionObjectToSectionManagedObjectModel("MANAGED_OBJECT"),
				objectC.getSectionManagedObject(), "getSectionManagedObjectName");
	}

	/**
	 * Ensure able to round trip storing then retrieving the
	 * {@link SectionModel}.
	 */
	public void testRoundTripStoreRetrieveSection() throws Exception {

		// Load the section
		ModelRepository repository = new ModelRepositoryImpl();
		SectionModel section = new SectionModel();
		section = repository.retrieve(section, this.configurationItem);

		// Store the Section
		MemoryConfigurationItem contents = new MemoryConfigurationItem();
		repository.store(section, contents);

		// Reload the Section
		SectionModel reloadedSection = new SectionModel();
		reloadedSection = repository.retrieve(reloadedSection, contents);

		// Validate round trip
		assertGraph(section, reloadedSection, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}