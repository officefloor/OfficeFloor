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
package net.officefloor.model.impl.section;

import java.sql.Connection;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
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
import net.officefloor.model.section.SectionRepository;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link SectionRepository}.
 *
 * @author Daniel Sagenschneider
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
		SubSectionInputModel input = new SubSectionInputModel("INPUT",
				Integer.class.getName(), false, null);
		subSection.addSubSectionInput(input);
		SubSectionOutputModel output = new SubSectionOutputModel("OUTPUT",
				Integer.class.getName(), false);
		subSection.addSubSectionOutput(output);
		ExternalFlowModel extFlow = new ExternalFlowModel("EXTERNAL_FLOW",
				Integer.class.getName());
		section.addExternalFlow(extFlow);
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT", Object.class.getName());
		section.addExternalManagedObject(extMo);
		SectionManagedObjectModel mo = new SectionManagedObjectModel(
				"MANAGED_OBJECT", "THREAD");
		section.addSectionManagedObject(mo);
		SectionManagedObjectDependencyModel dependency = new SectionManagedObjectDependencyModel(
				"DEPENDENCY", Object.class.getName());
		mo.addSectionManagedObjectDependency(dependency);
		SectionManagedObjectSourceModel mos = new SectionManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Connection.class
						.getName(), "0");
		section.addSectionManagedObjectSource(mos);
		SectionManagedObjectSourceFlowModel flow = new SectionManagedObjectSourceFlowModel(
				"FLOW", Object.class.getName());
		mos.addSectionManagedObjectSourceFlow(flow);

		// managed object -> managed object source
		SectionManagedObjectToSectionManagedObjectSourceModel moToMos = new SectionManagedObjectToSectionManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE");
		mo.setSectionManagedObjectSource(moToMos);

		// managed object source flow -> external flow
		SectionManagedObjectSourceFlowToExternalFlowModel flowToExtFlow = new SectionManagedObjectSourceFlowToExternalFlowModel(
				"EXTERNAL_FLOW");
		flow.setExternalFlow(flowToExtFlow);

		// managed object source flow -> sub section input
		SectionManagedObjectSourceFlowToSubSectionInputModel flowToInput = new SectionManagedObjectSourceFlowToSubSectionInputModel(
				"SUB_SECTION", "INPUT");
		flow.setSubSectionInput(flowToInput);

		// dependency -> external managed object
		SectionManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new SectionManagedObjectDependencyToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		dependency.setExternalManagedObject(dependencyToExtMo);

		// dependency -> managed object
		SectionManagedObjectDependencyToSectionManagedObjectModel dependencyToMo = new SectionManagedObjectDependencyToSectionManagedObjectModel(
				"MANAGED_OBJECT");
		dependency.setSectionManagedObject(dependencyToMo);

		// output -> input
		SubSectionOutputToSubSectionInputModel outputToInput = new SubSectionOutputToSubSectionInputModel(
				"SUB_SECTION", "INPUT");
		output.setSubSectionInput(outputToInput);

		// output -> extFlow
		SubSectionOutputModel output_extFlow = new SubSectionOutputModel(
				"OUTPUT_EXTERNAL_FLOW", Exception.class.getName(), true);
		subSection.addSubSectionOutput(output_extFlow);
		SubSectionOutputToExternalFlowModel outputToExtFlow = new SubSectionOutputToExternalFlowModel(
				"EXTERNAL_FLOW");
		output_extFlow.setExternalFlow(outputToExtFlow);

		// object -> extMo
		SubSectionObjectModel object_extMo = new SubSectionObjectModel(
				"OBJECT_EXT", Object.class.getName());
		subSection.addSubSectionObject(object_extMo);
		SubSectionObjectToExternalManagedObjectModel objectToExtMo = new SubSectionObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		object_extMo.setExternalManagedObject(objectToExtMo);

		// object -> managed object
		SubSectionObjectModel object_mo = new SubSectionObjectModel(
				"OBJECT_MO", Connection.class.getName());
		subSection.addSubSectionObject(object_mo);
		SubSectionObjectToSectionManagedObjectModel objectToMo = new SubSectionObjectToSectionManagedObjectModel(
				"MANAGED_OBJECT");
		object_mo.setSectionManagedObject(objectToMo);

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

		// Ensure managed object connected to its source
		assertEquals("mo -> mos", mos, moToMos.getSectionManagedObjectSource());
		assertEquals("mo <- mos", mo, moToMos.getSectionManagedObject());

		// Ensure managed object source flow connected to external flow
		assertEquals("mos flow <- external flow", flow, flowToExtFlow
				.getSectionManagedObjectSourceFlow());
		assertEquals("mos flow -> external flow", extFlow, flowToExtFlow
				.getExternalFlow());

		// Ensure managed object source flow connected to sub section input
		assertEquals("mos flow <- sub section input", flow, flowToInput
				.getSectionManagedObjectSourceFlow());
		assertEquals("mos flow -> sub section input", input, flowToInput
				.getSubSectionInput());

		// Ensure dependency connected to external managed object
		assertEquals("dependency <- external mo", dependency, dependencyToExtMo
				.getSectionManagedObjectDependency());
		assertEquals("dependency -> managed object", extMo, dependencyToExtMo
				.getExternalManagedObject());

		// Ensure dependency connected to managed object
		assertEquals("dependency <- managed object", dependency, dependencyToMo
				.getSectionManagedObjectDependency());
		assertEquals("dependency -> managed object", mo, dependencyToMo
				.getSectionManagedObject());

		// Ensure output to input connected
		assertEquals("output -> input", input, outputToInput
				.getSubSectionInput());
		assertEquals("output <- input", output, outputToInput
				.getSubSectionOutput());

		// Ensure the external flow connected
		assertEquals("output -> extFlow", extFlow, outputToExtFlow
				.getExternalFlow());
		assertEquals("output <- extFlow", output_extFlow, outputToExtFlow
				.getSubSectionOutput());

		// Ensure the external managed object connected
		assertEquals("object -> extMo", extMo, objectToExtMo
				.getExternalManagedObject());
		assertEquals("object <- extMo", object_extMo, objectToExtMo
				.getSubSectionObject());

		// Ensure the section managed object connected
		assertEquals("object -> mo", mo, objectToMo.getSectionManagedObject());
		assertEquals("object <- mo", object_mo, objectToMo
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
		SectionManagedObjectModel mo = new SectionManagedObjectModel(
				"MANAGED_OBJECT", "THREAD");
		section.addSectionManagedObject(mo);
		SectionManagedObjectDependencyModel dependency = new SectionManagedObjectDependencyModel(
				"DEPENDENCY", Object.class.getName());
		mo.addSectionManagedObjectDependency(dependency);
		SectionManagedObjectSourceModel mos = new SectionManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Connection.class
						.getName(), "0");
		section.addSectionManagedObjectSource(mos);
		SectionManagedObjectSourceFlowModel mosFlow = new SectionManagedObjectSourceFlowModel(
				"MOS_FLOW", Object.class.getName());
		mos.addSectionManagedObjectSourceFlow(mosFlow);

		// mo -> mos
		SectionManagedObjectToSectionManagedObjectSourceModel moToMos = new SectionManagedObjectToSectionManagedObjectSourceModel();
		moToMos.setSectionManagedObject(mo);
		moToMos.setSectionManagedObjectSource(mos);
		moToMos.connect();

		// mos flow -> external flow
		SectionManagedObjectSourceFlowToExternalFlowModel flowToExtFlow = new SectionManagedObjectSourceFlowToExternalFlowModel();
		flowToExtFlow.setSectionManagedObjectSourceFlow(mosFlow);
		flowToExtFlow.setExternalFlow(extFlow);
		flowToExtFlow.connect();

		// mos flow -> sub section input
		SectionManagedObjectSourceFlowToSubSectionInputModel flowToInput = new SectionManagedObjectSourceFlowToSubSectionInputModel();
		flowToInput.setSectionManagedObjectSourceFlow(mosFlow);
		flowToInput.setSubSectionInput(input);
		flowToInput.connect();

		// dependency -> extMo
		SectionManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new SectionManagedObjectDependencyToExternalManagedObjectModel();
		dependencyToExtMo.setSectionManagedObjectDependency(dependency);
		dependencyToExtMo.setExternalManagedObject(extMo);
		dependencyToExtMo.connect();

		// dependency -> mo
		SectionManagedObjectDependencyToSectionManagedObjectModel dependencyToMo = new SectionManagedObjectDependencyToSectionManagedObjectModel();
		dependencyToMo.setSectionManagedObjectDependency(dependency);
		dependencyToMo.setSectionManagedObject(mo);
		dependencyToMo.connect();

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
		objectToExtMo.connect();

		// object -> mo
		SubSectionObjectToSectionManagedObjectModel objectToMo = new SubSectionObjectToSectionManagedObjectModel();
		objectToMo.setSubSectionObject(object);
		objectToMo.setSectionManagedObject(mo);
		objectToMo.connect();

		// Record storing the section
		this.modelRepository.store(section, this.configurationItem);

		// Store the section
		this.replayMockObjects();
		this.sectionRepository.storeSection(section, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("mo - mos", "MANAGED_OBJECT_SOURCE", moToMos
				.getSectionManagedObjectSourceName());
		assertEquals("mos flow - extFlow", "FLOW", flowToExtFlow
				.getExternalFlowName());
		assertEquals("mos flow - input (sub section)", "SUB_SECTION",
				flowToInput.getSubSectionName());
		assertEquals("mos flow - input (input)", "INPUT", flowToInput
				.getSubSectionInputName());
		assertEquals("dependency - extMo", "MO", dependencyToExtMo
				.getExternalManagedObjectName());
		assertEquals("dependency - mo", "MANAGED_OBJECT", dependencyToMo
				.getSectionManagedObjectName());
		assertEquals("output - input (sub section)", "SUB_SECTION",
				outputToInput.getSubSectionName());
		assertEquals("output - input (input)", "INPUT", outputToInput
				.getSubSectionInputName());
		assertEquals("output - extFlow", "FLOW", outputToExtFlow
				.getExternalFlowName());
		assertEquals("object - extMo", "MO", objectToExtMo
				.getExternalManagedObjectName());
		assertEquals("object - mo", "MANAGED_OBJECT", objectToMo
				.getSectionManagedObjectName());
	}

}