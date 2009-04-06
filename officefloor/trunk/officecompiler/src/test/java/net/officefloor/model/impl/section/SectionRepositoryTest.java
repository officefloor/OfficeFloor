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
package net.officefloor.model.impl.section;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionRepository;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link SectionRepository}.
 * 
 * @author Daniel
 */
public class SectionRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this
			.createMock(ModelRepository.class);

	/**
	 * {@link ConfigurationItem}.
	 */
	private final ConfigurationItem configurationItem = this
			.createMock(ConfigurationItem.class);

	/**
	 * {@link SectionRepository} to be tested.
	 */
	private final SectionRepository sectionRepository = new SectionRepositoryImpl(
			this.modelRepository);

	/**
	 * Ensures on retrieving a {@link SectionModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveSection() throws Exception {

		// Create the raw section to be connected
		SectionModel section = new SectionModel();
		SubSectionModel subSection = new SubSectionModel("SUB_SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION");
		section.addSubSection(subSection);

		// output -> input
		SubSectionOutputModel output_input = new SubSectionOutputModel(
				"OUTPUT_INPUT", Integer.class.getName(), false);
		subSection.addSubSectionOutput(output_input);
		SubSectionModel subSection_input = new SubSectionModel(
				"SUB_SECTION_INPUT", "net.example.ExampleSectionSource",
				"INPUT_SECTION_LOCATION");
		section.addSubSection(subSection_input);
		SubSectionInputModel input = new SubSectionInputModel("output - input",
				Integer.class.getName(), false, null);
		subSection_input.addSubSectionInput(input);
		SubSectionOutputToSubSectionInputModel outputToInput = new SubSectionOutputToSubSectionInputModel(
				"SUB_SECTION_INPUT", "output - input");
		output_input.setSubSectionInput(outputToInput);

		// output -> extFlow
		SubSectionOutputModel output_extFlow = new SubSectionOutputModel(
				"OUTPUT_EXTERNAL_FLOW", Exception.class.getName(), true);
		subSection.addSubSectionOutput(output_extFlow);
		ExternalFlowModel extFlow = new ExternalFlowModel("output - extFlow",
				Integer.class.getName());
		section.addExternalFlow(extFlow);
		SubSectionOutputToExternalFlowModel outputToExtFlow = new SubSectionOutputToExternalFlowModel(
				"output - extFlow");
		output_extFlow.setExternalFlow(outputToExtFlow);

		// object -> extMo
		SubSectionObjectModel object = new SubSectionObjectModel("OBJECT",
				Object.class.getName());
		subSection.addSubSectionObject(object);
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"object - extMo", Object.class.getName());
		section.addExternalManagedObject(extMo);
		SubSectionObjectToExternalManagedObjectModel objectToExtMo = new SubSectionObjectToExternalManagedObjectModel(
				"object - extMo");
		object.setExternalManagedObject(objectToExtMo);

		// Record retrieving the section
		this.recordReturn(this.modelRepository, this.modelRepository.retrieve(
				null, this.configurationItem), section, new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertTrue("Must be section model",
						actual[0] instanceof SectionModel);
				assertEquals("Incorrect configuration item",
						SectionRepositoryTest.this.configurationItem, actual[1]);
				return true;
			}
		});

		// Retrieve the section
		this.replayMockObjects();
		SectionModel retrievedSection = this.sectionRepository
				.retrieveSection(this.configurationItem);
		this.verifyMockObjects();
		assertEquals("Incorrect section", section, retrievedSection);

		// Ensure output to input connected
		assertEquals("output -> input", input, outputToInput
				.getSubSectionInput());
		assertEquals("output <- input", output_input, outputToInput
				.getSubSectionOutput());

		// Ensure the external flow connected
		assertEquals("output -> extFlow", extFlow, outputToExtFlow
				.getExternalFlow());
		assertEquals("output <- extFlow", output_extFlow, outputToExtFlow
				.getSubSectionOutput());

		// Ensure the external managed object connected
		assertEquals("object -> extMo", extMo, objectToExtMo
				.getExternalManagedObject());
		assertEquals("object <- extMo", object, objectToExtMo
				.getSubSectionObject());
	}

	/**
	 * Ensures on storing a {@link SectionModel} that all
	 * {@link ConnectionModel} instances are readied for storing.
	 */
	public void testStoreSection() throws Exception {

		// Create the section (without connections)
		SectionModel section = new SectionModel();
		SubSectionModel subSection = new SubSectionModel("SUB_SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION");
		section.addSubSection(subSection);
		SubSectionInputModel input = new SubSectionInputModel("INPUT",
				Integer.class.getName(), false, null);
		subSection.addSubSectionInput(input);
		SubSectionOutputModel output_input = new SubSectionOutputModel(
				"OUTPUT_INPUT", Integer.class.getName(), false);
		subSection.addSubSectionOutput(output_input);
		SubSectionOutputModel output_extFlow = new SubSectionOutputModel(
				"OUTPUT_EXTERNAL_FLOW", Exception.class.getName(), true);
		subSection.addSubSectionOutput(output_extFlow);
		ExternalFlowModel extFlow = new ExternalFlowModel("FLOW", Integer.class
				.getName());
		section.addExternalFlow(extFlow);
		SubSectionObjectModel object = new SubSectionObjectModel("OBJECT",
				Object.class.getName());
		subSection.addSubSectionObject(object);
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel("MO",
				Object.class.getName());
		section.addExternalManagedObject(extMo);

		// output -> input
		SubSectionOutputToSubSectionInputModel outputToInput = new SubSectionOutputToSubSectionInputModel();
		outputToInput.setSubSectionOutput(output_input);
		outputToInput.setSubSectionInput(input);
		outputToInput.connect();

		// output -> extFlow
		SubSectionOutputToExternalFlowModel outputToExtFlow = new SubSectionOutputToExternalFlowModel();
		outputToExtFlow.setSubSectionOutput(output_extFlow);
		outputToExtFlow.setExternalFlow(extFlow);
		outputToExtFlow.connect();

		// object -> extMo
		SubSectionObjectToExternalManagedObjectModel objectToExtMo = new SubSectionObjectToExternalManagedObjectModel();
		objectToExtMo.setSubSectionObject(object);
		objectToExtMo.setExternalManagedObject(extMo);

		// Record storing the section
		this.modelRepository.store(section, this.configurationItem);

		// Store the section
		this.replayMockObjects();
		this.sectionRepository.storeSection(section, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("output - input (sub section)", "SUB_SECTION",
				outputToInput.getSubSectionName());
		assertEquals("output - input", "INPUT", outputToInput
				.getSubSectionInputName());
		assertEquals("output - extFlow", "FLOW", outputToExtFlow
				.getExternalFlowName());
		assertEquals("object - extMo", "MO", objectToExtMo
				.getExternalManagedObjectName());
	}

}