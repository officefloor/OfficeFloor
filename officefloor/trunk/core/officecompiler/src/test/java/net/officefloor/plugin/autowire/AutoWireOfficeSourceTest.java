/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.autowire;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.TaskTeam;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AmbiguousException;
import net.officefloor.plugin.autowire.AutoWireOfficeSource;

/**
 * Tests the {@link AutoWireOfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link OfficeArchitect}.
	 */
	private final OfficeArchitect architect = this
			.createMock(OfficeArchitect.class);

	/**
	 * Mock {@link OfficeSourceContext}.
	 */
	private final OfficeSourceContext context = this
			.createMock(OfficeSourceContext.class);

	/**
	 * Ensure single section.
	 */
	public void testSingleSection() throws Exception {

		final String SECTION = "Section";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION, "name", "value");

		// Record creating the section
		this.recordTeam();
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure sub sections.
	 */
	public void testSubSections() throws Exception {

		final String SECTION = "Section";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION, "name", "value");

		// Record creating the section
		this.recordTeam();
		this.recordOfficeSection(SECTION, "SubSection");
		this.recordSubSections("SubSection", "SubSubSectionOne",
				"SubSubSectionTwo");
		this.recordSubSections("SubSubSectionOne");
		this.recordSubSections("SubSubSectionTwo");
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure link sections - output to input.
	 */
	public void testLinkSections() throws Exception {

		final String ONE = "One";
		final String TWO = "Two";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, ONE);
		this.addSection(source, TWO);

		// Record creating the section
		this.recordTeam();
		this.recordOfficeSection(ONE);
		this.recordSectionObjects(ONE);
		this.recordSectionInputs(ONE);
		this.recordSectionOutputs(ONE, String.class);
		this.recordOfficeSection(TWO);
		this.recordSectionObjects(TWO);
		this.recordSectionInputs(TWO, String.class);
		this.recordSectionOutputs(TWO);
		OfficeSectionOutput output = this.outputs.get(ONE).get(String.class);
		OfficeSectionInput input = this.inputs.get(TWO).get(String.class);
		this.architect.link(output, input);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if ambiguous {@link OfficeSectionOutput} link.
	 */
	public void testAmbiguousOutputLink() throws Exception {

		final String OUTPUT = "Output";
		final String ONE = "One";
		final String TWO = "Two";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, OUTPUT);
		this.addSection(source, ONE);
		this.addSection(source, TWO);

		// Record creating the section
		this.recordTeam();
		this.recordOfficeSection(OUTPUT);
		this.recordSectionObjects(OUTPUT);
		this.recordSectionInputs(OUTPUT);
		this.recordSectionOutputs(OUTPUT, String.class);
		this.recordOfficeSection(ONE);
		this.recordSectionObjects(ONE);
		this.recordSectionInputs(ONE, String.class);
		this.recordSectionOutputs(ONE);
		this.recordOfficeSection(TWO);
		this.recordSectionObjects(TWO);
		this.recordSectionInputs(TWO, String.class);
		this.recordSectionOutputs(TWO);

		// Test
		this.replayMockObjects();
		try {
			source.sourceOffice(this.architect, context);
			fail("Should not be successful");
		} catch (AmbiguousException ex) {
			assertEquals("Incorrect failure",
					"More than one input for output type java.lang.String", ex
							.getMessage());
		}
		this.verifyMockObjects();
	}

	/**
	 * Ensure dependency.
	 */
	public void testDependency() throws Exception {

		final String SECTION = "Section";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);

		// Record creating the section
		this.recordTeam();
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION, Connection.class);
		this.recordSectionInputs(SECTION);
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link OfficeTeam}.
	 */
	private final OfficeTeam team = this.createMock(OfficeTeam.class);

	/**
	 * {@link PropertyList} instances by section name.
	 */
	private final Map<String, PropertyList> sectionProperties = new HashMap<String, PropertyList>();

	/**
	 * {@link OfficeSection} instances by name.
	 */
	private final Map<String, OfficeSection> sections = new HashMap<String, OfficeSection>();

	/**
	 * {@link OfficeSectionInput} instances by {@link OfficeSection} name and
	 * input type.
	 */
	private final Map<String, Map<Class<?>, OfficeSectionInput>> inputs = new HashMap<String, Map<Class<?>, OfficeSectionInput>>();

	/**
	 * {@link OfficeSectionOutput} instances by {@link OfficeSection} name and
	 * output type.
	 */
	private final Map<String, Map<Class<?>, OfficeSectionOutput>> outputs = new HashMap<String, Map<Class<?>, OfficeSectionOutput>>();

	/**
	 * {@link OfficeSectionObject} instances by {@link OfficeSection} name and
	 * object type.
	 */
	private final Map<String, Map<Class<?>, OfficeSectionObject>> objects = new HashMap<String, Map<Class<?>, OfficeSectionObject>>();

	/**
	 * {@link OfficeObject} instances by type.
	 */
	private final Map<Class<?>, OfficeObject> dependencies = new HashMap<Class<?>, OfficeObject>();

	/**
	 * {@link OfficeSubSection} instances by name.
	 */
	private final Map<String, OfficeSubSection> subSections = new HashMap<String, OfficeSubSection>();

	/**
	 * Records the {@link OfficeTeam}.
	 */
	private void recordTeam() {
		this.recordReturn(this.context, this.context.getClassLoader(), Thread
				.currentThread().getContextClassLoader());
		this.recordReturn(this.architect, this.architect.addOfficeTeam("team"),
				this.team);
	}

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param source
	 *            {@link AutoWireOfficeSource}.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param propertyNameValues
	 *            Property name value pairs.
	 */
	private void addSection(AutoWireOfficeSource source, String sectionName,
			String... propertyNameValues) {
		PropertyList properties = source.addSection(sectionName,
				SectionSource.class, sectionName + "Location");
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			properties.addProperty(name).setValue(value);
		}
		this.sectionProperties.put(sectionName, properties);
	}

	/**
	 * Records adding an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name.
	 * @param subSectionNames
	 *            Sub section names.
	 * @return {@link OfficeSection}.
	 */
	private void recordOfficeSection(String sectionName,
			String... subSectionNames) {
		assertNull("Already section by name " + sectionName, this.sections
				.get(sectionName));

		// Obtain the properties
		PropertyList properties = this.sectionProperties.get(sectionName);
		assertNotNull("Section " + sectionName + " should be added", properties);

		// Record creating the section
		OfficeSection section = this.createMock(OfficeSection.class);
		this.recordReturn(this.architect, this.architect.addOfficeSection(
				sectionName, SectionSource.class.getName(), sectionName
						+ "Location", properties), section);
		this.sections.put(sectionName, section);

		// Record task on section
		OfficeTask task = this.createMock(OfficeTask.class);
		this.recordReturn(section, section.getOfficeTasks(),
				new OfficeTask[] { task });
		TaskTeam taskTeam = this.createMock(TaskTeam.class);
		this.recordReturn(task, task.getTeamResponsible(), taskTeam);
		this.architect.link(taskTeam, this.team);

		// Record the sub sections
		this.recordSubSections(sectionName, subSectionNames);
	}

	/**
	 * Obtains the {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name.
	 * @return {@link OfficeSection}.
	 */
	private OfficeSection getOfficeSection(String sectionName) {
		OfficeSection section = this.sections.get(sectionName);
		assertNotNull("Unknown section " + sectionName, section);
		return section;
	}

	/**
	 * Records the {@link OfficeSectionInput} instances.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param inputTypes
	 *            Types of the inputs.
	 */
	private void recordSectionInputs(String sectionName, Class<?>... inputTypes) {
		OfficeSectionInput[] inputs = this.createSectionItems(sectionName,
				OfficeSectionInput.class, this.inputs, inputTypes);
		OfficeSection section = this.getOfficeSection(sectionName);
		this.recordReturn(section, section.getOfficeSectionInputs(), inputs);
		for (int i = 0; i < inputTypes.length; i++) {
			OfficeSectionInput input = inputs[i];
			this.recordReturn(input, input.getParameterType(), inputTypes[i]
					.getName());
		}
	}

	/**
	 * Records the {@link OfficeSectionOutput} instances.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param outputTypes
	 *            Types of the outputs.
	 */
	private void recordSectionOutputs(String sectionName,
			Class<?>... outputTypes) {
		OfficeSectionOutput[] outputs = this.createSectionItems(sectionName,
				OfficeSectionOutput.class, this.outputs, outputTypes);
		OfficeSection section = this.getOfficeSection(sectionName);
		this.recordReturn(section, section.getOfficeSectionOutputs(), outputs);
		for (int i = 0; i < outputTypes.length; i++) {
			OfficeSectionOutput output = outputs[i];
			this.recordReturn(output, output.getArgumentType(), outputTypes[i]
					.getName());
			this.recordReturn(output, output.isEscalationOnly(), false);
		}
	}

	/**
	 * Records the {@link OfficeSectionObject} instances.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param objectTypes
	 *            Types of the objects.
	 */
	private void recordSectionObjects(String sectionName,
			Class<?>... objectTypes) {

		// Record obtaining the objects
		OfficeSectionObject[] objects = this.createSectionItems(sectionName,
				OfficeSectionObject.class, this.objects, objectTypes);
		OfficeSection section = this.getOfficeSection(sectionName);
		this.recordReturn(section, section.getOfficeSectionObjects(), objects);

		// Link objects as dependencies
		for (int i = 0; i < objectTypes.length; i++) {
			Class<?> objectType = objectTypes[i];
			OfficeSectionObject object = objects[i];

			// Obtain object type
			this.recordReturn(object, object.getObjectType(), objectType
					.getName());

			// Lazy add the dependency
			OfficeObject dependency = this.dependencies.get(objectType);
			if (dependency == null) {
				dependency = this.createMock(OfficeObject.class);
				this.dependencies.put(objectType, dependency);
				this.recordReturn(this.architect, this.architect
						.addOfficeObject(objectType.getName(), objectType
								.getName()), dependency);
			}

			// Link the object to dependency
			this.architect.link(object, dependency);
		}
	}

	/**
	 * Records the {@link OfficeSubSection} instances.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection} or {@link OfficeSubSection}.
	 * @param subSectionNames
	 *            Names of the {@link OfficeSubSection}.
	 */
	private void recordSubSections(String sectionName,
			String... subSectionNames) {

		// Create the sub sections
		List<OfficeSubSection> list = new LinkedList<OfficeSubSection>();
		for (String subSectionName : subSectionNames) {
			assertNull("Sub section already registered " + subSectionName,
					this.subSections.get(subSectionName));
			OfficeSubSection subSection = this
					.createMock(OfficeSubSection.class);
			this.subSections.put(subSectionName, subSection);
			list.add(subSection);
		}
		OfficeSubSection[] subSections = list.toArray(new OfficeSubSection[list
				.size()]);

		// Record from office section
		OfficeSection section = this.sections.get(sectionName);
		if (section != null) {
			this.recordReturn(section, section.getOfficeSubSections(),
					subSections);

		} else {
			// Record from office sub section
			OfficeSubSection subSection = this.subSections.get(sectionName);
			if (subSection != null) {
				this.recordReturn(subSection,
						subSection.getOfficeSubSections(), subSections);

			} else {
				// Unknown section
				fail("Unknown section " + sectionName);
			}
		}

		// Link sub section tasks to team
		for (OfficeSubSection subSection : subSections) {
			OfficeTask task = this.createMock(OfficeTask.class);
			this.recordReturn(subSection, subSection.getOfficeTasks(),
					new OfficeTask[] { task });
			TaskTeam taskTeam = this.createMock(TaskTeam.class);
			this.recordReturn(task, task.getTeamResponsible(), taskTeam);
			this.architect.link(taskTeam, this.team);
		}
	}

	/**
	 * Records the {@link OfficeSectionInput} instances.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param inputTypes
	 *            Types of the inputs.
	 */
	@SuppressWarnings("unchecked")
	private <T> T[] createSectionItems(String sectionName, Class<T> itemType,
			Map<String, Map<Class<?>, T>> items, Class<?>... types) {
		assertNull("Already obtained  " + itemType.getSimpleName()
				+ " for section " + sectionName, items.get(sectionName));

		// Create and register the items
		Map<Class<?>, T> entries = new HashMap<Class<?>, T>();
		items.put(sectionName, entries);
		List<T> list = new LinkedList<T>();
		for (Class<?> type : types) {
			T item = this.createMock(itemType);
			entries.put(type, item);
			list.add(item);
		}

		// Return the listing of items
		return list.toArray((T[]) Array.newInstance(itemType, list.size()));
	}

}