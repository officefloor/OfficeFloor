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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
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

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "TestSection.section.xml"), null);
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
		assertList(new String[] { "getExternalManagedObjectName",
				"getObjectType", "getX", "getY" }, section
				.getExternalManagedObjects(), new ExternalManagedObjectModel(
				"MO", Object.class.getName(), null, null, 100, 101));

		// ----------------------------------------------
		// Validate the managed object sources
		// ----------------------------------------------
		assertList(new String[] { "getSectionManagedObjectSourceName",
				"getManagedObjectSourceClassName", "getObjectType",
				"getTimeout", "getX", "getY" }, section
				.getSectionManagedObjectSources(),
				new SectionManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
						"net.example.ExampleManagedObjectSource",
						"net.orm.Session", "10", null, null, null, 200, 201));
		SectionManagedObjectSourceModel mos = section
				.getSectionManagedObjectSources().get(0);
		assertList(new String[] { "getName", "getValue" }, mos.getProperties(),
				new PropertyModel("MO_ONE", "VALUE_ONE"), new PropertyModel(
						"MO_TWO", "VALUE_TWO"));
		assertList(new String[] { "getSectionManagedObjectSourceFlowName",
				"getArgumentType" }, mos.getSectionManagedObjectSourceFlows(),
				new SectionManagedObjectSourceFlowModel("FLOW_ONE",
						String.class.getName()),
				new SectionManagedObjectSourceFlowModel("FLOW_TWO",
						Integer.class.getName()));
		SectionManagedObjectSourceFlowModel flowOne = mos
				.getSectionManagedObjectSourceFlows().get(0);
		assertProperties(new SectionManagedObjectSourceFlowToExternalFlowModel(
				"FLOW"), flowOne.getExternalFlow(), "getExternalFlowName");
		SectionManagedObjectSourceFlowModel flowTwo = mos
				.getSectionManagedObjectSourceFlows().get(1);
		assertProperties(
				new SectionManagedObjectSourceFlowToSubSectionInputModel(
						"SECTION_A", "INPUT_A"), flowTwo.getSubSectionInput(),
				"getSubSectionName", "getSubSectionInputName");

		// ----------------------------------------------
		// Validate the managed objects
		// ----------------------------------------------
		assertList(new String[] { "getSectionManagedObjectName",
				"getManagedObjectScope", "getX", "getY" }, section
				.getSectionManagedObjects(), new SectionManagedObjectModel(
				"MANAGED_OBJECT_ONE", "THREAD", null, null, null, null, 300,
				301), new SectionManagedObjectModel("MANAGED_OBJECT_TWO",
				"PROCESS", null, null, null, null, 310, 311));
		SectionManagedObjectModel mo = section.getSectionManagedObjects()
				.get(0);
		assertProperties(
				new SectionManagedObjectToSectionManagedObjectSourceModel(
						"MANAGED_OBJECT_SOURCE"), mo
						.getSectionManagedObjectSource(),
				"getSectionManagedObjectSourceName");
		assertList(new String[] { "getSectionManagedObjectDependencyName",
				"getDependencyType" },
				mo.getSectionManagedObjectDependencies(),
				new SectionManagedObjectDependencyModel("DEPENDENCY_ONE",
						Object.class.getName()),
				new SectionManagedObjectDependencyModel("DEPENDENCY_TWO",
						Connection.class.getName()));
		SectionManagedObjectDependencyModel dependencyOne = mo
				.getSectionManagedObjectDependencies().get(0);
		assertProperties(
				new SectionManagedObjectDependencyToExternalManagedObjectModel(
						"MO"), dependencyOne.getExternalManagedObject(),
				"getExternalManagedObjectName");
		SectionManagedObjectDependencyModel dependencyTwo = mo
				.getSectionManagedObjectDependencies().get(1);
		assertProperties(
				new SectionManagedObjectDependencyToSectionManagedObjectModel(
						"MANAGED_OBJECT_TWO"), dependencyTwo
						.getSectionManagedObject(),
				"getSectionManagedObjectName");

		// ----------------------------------------------
		// Validate the external flows
		// ----------------------------------------------
		assertList(new String[] { "getExternalFlowName", "getArgumentType",
				"getX", "getY" }, section.getExternalFlows(),
				new ExternalFlowModel("FLOW", String.class.getName(), null,
						null, 400, 401));

		// ----------------------------------------------
		// Validate the sub sections
		// ----------------------------------------------
		assertList(new String[] { "getSubSectionName",
				"getSectionSourceClassName", "getSectionLocation", "getX",
				"getY" }, section.getSubSections(), new SubSectionModel(
				"SECTION_A", "DESK", "DESK_LOCATION", null, null, null, null,
				500, 501), new SubSectionModel("SECTION_B", "SECTION",
				"SECTION_LOCATION", null, null, null, null, 510, 511),
				new SubSectionModel("SECTION_C",
						"net.example.ExampleSectionSource", "EXAMPLE_LOCATION",
						null, null, null, null, 520, 521));
		SubSectionModel subSectionA = section.getSubSections().get(0);
		SubSectionModel subSectionB = section.getSubSections().get(1);
		SubSectionModel subSectionC = section.getSubSections().get(2);

		// Validate the properties
		String[] propertyValidation = new String[] { "getName", "getValue" };
		assertList(propertyValidation, subSectionA.getProperties(),
				new PropertyModel("name.one", "value.one"), new PropertyModel(
						"name.two", "value.two"));
		assertList(propertyValidation, subSectionB.getProperties());

		// Validate the inputs
		String[] inputValidation = new String[] { "getSubSectionInputName",
				"getParameterType", "getIsPublic", "getPublicInputName" };
		assertList(inputValidation, subSectionA.getSubSectionInputs(),
				new SubSectionInputModel("INPUT_A", Integer.class.getName(),
						true, null), new SubSectionInputModel("INPUT_B", null,
						false, null));
		assertList(inputValidation, subSectionB.getSubSectionInputs(),
				new SubSectionInputModel("INPUT_A", Exception.class.getName(),
						true, "PUBLIC_INPUT_A"));
		assertList(inputValidation, subSectionC.getSubSectionInputs());

		// Validate the outputs
		String[] outputValidation = new String[] { "getSubSectionOutputName",
				"getArgumentType", "getEscalationOnly" };
		assertList(outputValidation, subSectionA.getSubSectionOutputs(),
				new SubSectionOutputModel("OUTPUT_A", String.class.getName(),
						false), new SubSectionOutputModel("OUTPUT_B",
						Exception.class.getName(), true),
				new SubSectionOutputModel("OUTPUT_C", null, false));
		assertList(outputValidation, subSectionB.getSubSectionOutputs());
		SubSectionOutputModel outputA = subSectionA.getSubSectionOutputs().get(
				0);
		SubSectionOutputModel outputB = subSectionA.getSubSectionOutputs().get(
				1);

		// Validate the output connections
		assertProperties(new SubSectionOutputToExternalFlowModel("FLOW"),
				outputA.getExternalFlow(), "getExternalFlowName");
		assertNull("Should not link input", outputA.getSubSectionInput());
		assertNull("Should not link external flow", outputB.getExternalFlow());
		assertProperties(new SubSectionOutputToSubSectionInputModel(
				"SECTION_B", "INPUT_A"), outputB.getSubSectionInput(),
				"getSubSectionName", "getSubSectionInputName");

		// Validate the objects
		String[] objectValidation = new String[] {};
		assertList(objectValidation, subSectionA.getSubSectionObjects(),
				new SubSectionObjectModel("OBJECT_A", Object.class.getName()),
				new SubSectionObjectModel("OBJECT_B", Double.class.getName()),
				new SubSectionObjectModel("OBJECT_C", "net.orm.Session"));
		assertList(objectValidation, subSectionB.getSubSectionObjects());
		SubSectionObjectModel objectA = subSectionA.getSubSectionObjects().get(
				0);
		SubSectionObjectModel objectB = subSectionA.getSubSectionObjects().get(
				1);
		SubSectionObjectModel objectC = subSectionA.getSubSectionObjects().get(
				2);

		// Validate the object connections
		assertProperties(
				new SubSectionObjectToExternalManagedObjectModel("MO"), objectA
						.getExternalManagedObject(),
				"getExternalManagedObjectName");
		assertNull("Should not link object", objectB.getExternalManagedObject());
		assertNull("Should not link object", objectB.getSectionManagedObject());
		assertProperties(new SubSectionObjectToSectionManagedObjectModel(
				"MANAGED_OBJECT"), objectC.getSectionManagedObject(),
				"getSectionManagedObjectName");
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
		assertGraph(section, reloadedSection,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}