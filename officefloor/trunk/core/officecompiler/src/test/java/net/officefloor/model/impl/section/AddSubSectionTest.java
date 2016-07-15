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

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;

/**
 * Tests adding a {@link SubSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddSubSectionTest extends AbstractSectionChangesTestCase {

	/**
	 * Ensure can add a {@link SubSectionModel} that only has properties.
	 */
	public void testAddSubSectionWithPropertiesOnly() {

		// Create the sub section
		SectionType sectionType = new SectionNodeImpl(null, null, null)
				.loadSectionType();

		// Ensure can add
		Change<SubSectionModel> change = this.operations.addSubSection(
				"SUB_SECTION", "net.example.ExampleSectionSource",
				"SECTION_LOCATION", new PropertyListImpl("name.one",
						"value.one", "name.two", "value.two"), sectionType);
		this.assertChange(change, null, "Add sub section SUB_SECTION", true);

		// Ensure correct target
		change.apply();
		assertEquals("Incorrect target", this.model.getSubSections().get(0),
				change.getTarget());
	}

	/**
	 * Ensure can add a {@link SubSectionModel} with
	 * {@link SubSectionInputModel}, {@link SubSectionOutputModel},
	 * {@link SubSectionObjectModel} instances.
	 */
	public void testAddSubSectionWithInputsOutputsObjects() {

		// Create the sub section with inputs, outputs, objects
		SectionNodeImpl sectionNode = new SectionNodeImpl(null, null, null);
		sectionNode.addSectionInput("INPUT_B", Integer.class.getName());
		sectionNode.addSectionInput("INPUT_A", Double.class.getName());
		sectionNode.addSectionOutput("OUTPUT_B", String.class.getName(), false);
		sectionNode.addSectionOutput("OUTPUT_A", Exception.class.getName(),
				true);
		sectionNode.addSectionObject("OBJECT_B", Object.class.getName());
		sectionNode.addSectionObject("OBJECT_A", Connection.class.getName());

		// Ensure can add (ordering the inputs, outputs, objects for easier SCM)
		Change<SubSectionModel> change = this.operations.addSubSection(
				"SUB_SECTION", "net.example.ExampleSectionSource",
				"SECTION_LOCATION", new PropertyListImpl(),
				sectionNode.loadSectionType());
		this.assertChange(change, null, "Add sub section SUB_SECTION", true);
	}

	/**
	 * Ensure can add multiple {@link SubSectionModel} instances.
	 */
	public void testAddMultipleSubSections() {

		// Create the section type
		SectionType sectionType = new SectionNodeImpl(null, null, null)
				.loadSectionType();

		// Add multiple section types
		Change<SubSectionModel> changeB = this.operations.addSubSection(
				"SUB_SECTION_B", "net.example.ExampleSectionSource",
				"LOCATION_B", new PropertyListImpl(), sectionType);
		Change<SubSectionModel> changeA = this.operations.addSubSection(
				"SUB_SECTION_A", "net.example.ExampleSectionSource",
				"LOCATION_A", new PropertyListImpl(), sectionType);
		Change<SubSectionModel> changeC = this.operations.addSubSection(
				"SUB_SECTION_C", "net.example.ExampleSectionSource",
				"LOCATION_C", new PropertyListImpl(), sectionType);

		// Apply the changes, ensuring ordering of the sub sections
		this.assertChanges(changeB, changeA, changeC);
	}

}