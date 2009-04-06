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
package net.officefloor.model.impl.repository;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

/**
 * Tests the marshalling/unmarshalling of the {@link SectionModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel
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
				"MO", Object.class.getName(), null, 10, 11));

		// ----------------------------------------------
		// Validate the external flows
		// ----------------------------------------------
		assertList(new String[] { "getExternalFlowName", "getArgumentType",
				"getX", "getY" }, section.getExternalFlows(),
				new ExternalFlowModel("FLOW", String.class.getName(), null, 20,
						21));

		// ----------------------------------------------
		// Validate the sub sections
		// ----------------------------------------------
		assertList(new String[] { "getSubSectionName",
				"getSectionSourceClassName", "getSectionLocation", "getX",
				"getY" }, section.getSubSections(), new SubSectionModel(
				"SECTION_A", "DESK", "DESK_LOCATION", null, null, null, null,
				100, 101), new SubSectionModel("SECTION_B", "SECTION",
				"SECTION_LOCATION", null, null, null, null, 200, 201),
				new SubSectionModel("SECTION_C",
						"net.example.ExampleSectionSource", "EXAMPLE_LOCATION",
						null, null, null, null, 300, 301));
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
				new SubSectionObjectModel("OBJECT_B", Double.class.getName()));
		assertList(objectValidation, subSectionB.getSubSectionObjects());
		SubSectionObjectModel objectA = subSectionA.getSubSectionObjects().get(
				0);
		SubSectionObjectModel objectB = subSectionA.getSubSectionObjects().get(
				1);

		// Validate the object connections
		assertProperties(
				new SubSectionObjectToExternalManagedObjectModel("MO"), objectA
						.getExternalManagedObject(),
				"getExternalManagedObjectName");
		assertNull("Should not link object", objectB.getExternalManagedObject());
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