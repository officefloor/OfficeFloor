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

package net.officefloor.model.impl.section;

import java.sql.Connection;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.source.TestSource;
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
	 * {@link SectionNode} name.
	 */
	private static final String SECTION_NAME = "SECTION";

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

	/**
	 * Mock parent {@link SectionNode}.
	 */
	private final SectionNode parentSection = this.createMock(SectionNode.class);

	/**
	 * Mock {@link NodeContext}.
	 */
	private final NodeContext context = (NodeContext) this.compiler;

	/**
	 * Ensure can add a {@link SubSectionModel} that only has properties.
	 */
	public void testAddSubSectionWithPropertiesOnly() {

		// Create the sub section
		SectionType sectionType = this.compiler.getSectionLoader().loadSectionType(EmptySectionSource.class, "location",
				this.compiler.createPropertyList());

		// Ensure can add
		Change<SubSectionModel> change = this.operations.addSubSection("SUB_SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION",
				new PropertyListImpl("name.one", "value.one", "name.two", "value.two"), sectionType);
		this.assertChange(change, null, "Add sub section SUB_SECTION", true);

		// Ensure correct target
		change.apply();
		assertEquals("Incorrect target", this.model.getSubSections().get(0), change.getTarget());
	}

	/**
	 * Ensure can add a {@link SubSectionModel} with {@link SubSectionInputModel},
	 * {@link SubSectionOutputModel}, {@link SubSectionObjectModel} instances.
	 */
	public void testAddSubSectionWithInputsOutputsObjects() {

		// Create the sub section with inputs, outputs, objects
		SectionNode sectionNode = this.context.createSectionNode(SECTION_NAME, this.parentSection);
		sectionNode.addSectionInput("INPUT_B", Integer.class.getName());
		sectionNode.addSectionInput("INPUT_A", Double.class.getName());
		sectionNode.addSectionOutput("OUTPUT_B", String.class.getName(), false);
		sectionNode.addSectionOutput("OUTPUT_A", Exception.class.getName(), true);
		sectionNode.addSectionObject("OBJECT_B", Object.class.getName());
		sectionNode.addSectionObject("OBJECT_A", Connection.class.getName());
		SectionType sectionType = sectionNode.loadSectionType(this.context.createCompileContext());

		// Ensure can add (ordering the inputs, outputs, objects for easier SCM)
		Change<SubSectionModel> change = this.operations.addSubSection("SUB_SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION", new PropertyListImpl(), sectionType);
		this.assertChange(change, null, "Add sub section SUB_SECTION", true);
	}

	/**
	 * Ensure can add multiple {@link SubSectionModel} instances.
	 */
	public void testAddMultipleSubSections() {

		// Create the section type
		SectionType sectionType = this.compiler.getSectionLoader().loadSectionType(EmptySectionSource.class, "location",
				this.compiler.createPropertyList());

		// Add multiple section types
		Change<SubSectionModel> changeB = this.operations.addSubSection("SUB_SECTION_B",
				"net.example.ExampleSectionSource", "LOCATION_B", new PropertyListImpl(), sectionType);
		Change<SubSectionModel> changeA = this.operations.addSubSection("SUB_SECTION_A",
				"net.example.ExampleSectionSource", "LOCATION_A", new PropertyListImpl(), sectionType);
		Change<SubSectionModel> changeC = this.operations.addSubSection("SUB_SECTION_C",
				"net.example.ExampleSectionSource", "LOCATION_C", new PropertyListImpl(), sectionType);

		// Apply the changes, ensuring ordering of the sub sections
		this.assertChanges(changeB, changeA, changeC);
	}

	/**
	 * Empty {@link SectionSource} for testing.
	 */
	@TestSource
	public static class EmptySectionSource extends AbstractSectionSource {

		/*
		 * ==================== SectionSource =================================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
		}
	}

}
