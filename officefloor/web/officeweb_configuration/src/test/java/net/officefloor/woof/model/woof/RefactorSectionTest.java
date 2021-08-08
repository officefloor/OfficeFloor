/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.model.woof;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link WoofSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorSectionTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofSectionModel}.
	 */
	private WoofSectionModel section;

	/**
	 * {@link WoofSectionInputModel} name mapping.
	 */
	private Map<String, String> sectionInputNameMapping = new HashMap<String, String>();;

	/**
	 * {@link WoofSectionOutputModel} name mapping.
	 */
	private Map<String, String> sectionOutputNameMapping = new HashMap<String, String>();;

	/**
	 * Initiate.
	 */
	public RefactorSectionTest() {
		super(true);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.section = this.model.getWoofSections().get(0);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Create the section type
		SectionType sectionType = this.constructSectionType((context) -> {
			context.addSectionInput("INPUT", Double.class);
			context.addSectionOutput("OUTPUT_A", Integer.class, false);
			context.addSectionOutput("OUTPUT_B", String.class, false);
			context.addSectionOutput("OUTPUT_C", null, false);
			context.addSectionOutput("OUTPUT_D", null, false);
			context.addSectionOutput("OUTPUT_E", null, false);
			context.addSectionOutput("OUTPUT_F", null, false);
			context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
			context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Keep section input names
		this.sectionInputNameMapping.put("INPUT", "INPUT");

		// Keep section output names
		this.sectionOutputNameMapping.put("OUTPUT_A", "OUTPUT_A");
		this.sectionOutputNameMapping.put("OUTPUT_B", "OUTPUT_B");
		this.sectionOutputNameMapping.put("OUTPUT_C", "OUTPUT_C");
		this.sectionOutputNameMapping.put("OUTPUT_D", "OUTPUT_D");
		this.sectionOutputNameMapping.put("OUTPUT_E", "OUTPUT_E");
		this.sectionOutputNameMapping.put("OUTPUT_F", "OUTPUT_F");

		// Refactor the section with same details
		Change<WoofSectionModel> change = this.operations.refactorSection(this.section, "SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION", properties, sectionType,
				this.sectionInputNameMapping, this.sectionOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Section", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Create the section type
		SectionType sectionType = this.constructSectionType((context) -> {
			context.addSectionInput("INPUT_CHANGE", Double.class);
			context.addSectionOutput("OUTPUT_A", Integer.class, false);
			context.addSectionOutput("OUTPUT_B", String.class, false);
			context.addSectionOutput("OUTPUT_C", null, false);
			context.addSectionOutput("OUTPUT_D", null, false);
			context.addSectionOutput("OUTPUT_E", null, false);
			context.addSectionOutput("OUTPUT_F", null, false);
			context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
			context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.1").setValue("value.one");
		properties.addProperty("name.two").setValue("value.2");

		// Keep section input names
		this.sectionInputNameMapping.put("INPUT_CHANGE", "INPUT");

		// Change section output names around
		this.sectionOutputNameMapping.put("OUTPUT_B", "OUTPUT_A");
		this.sectionOutputNameMapping.put("OUTPUT_C", "OUTPUT_B");
		this.sectionOutputNameMapping.put("OUTPUT_D", "OUTPUT_C");
		this.sectionOutputNameMapping.put("OUTPUT_E", "OUTPUT_D");
		this.sectionOutputNameMapping.put("OUTPUT_F", "OUTPUT_E");
		this.sectionOutputNameMapping.put("OUTPUT_A", "OUTPUT_F");

		// Refactor the section with same details
		Change<WoofSectionModel> change = this.operations.refactorSection(this.section, "CHANGE",
				"net.example.ChangeSectionSource", "CHANGE_LOCATION", properties, sectionType,
				this.sectionInputNameMapping, this.sectionOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Section", true);
	}

	/**
	 * Ensure handle remove {@link PropertyModel}, {@link WoofSectionInputModel} and
	 * {@link WoofSectionOutputModel} instances.
	 */
	public void testRemoveDetails() {

		// Create the section type
		SectionType sectionType = this.constructSectionType(null);

		// Refactor the section removing details
		Change<WoofSectionModel> change = this.operations.refactorSection(this.section, "REMOVE",
				"net.example.RemoveSectionSource", "REMOVE_LOCATION", null, sectionType, null, null);

		// Validate change
		this.assertChange(change, null, "Refactor Section", true);
	}

	/**
	 * Ensure handle adding {@link PropertyModel}, {@link WoofSectionInputModel} and
	 * {@link WoofSectionOutputModel} instances.
	 */
	public void testAddDetails() {

		// Create the section type
		SectionType sectionType = this.constructSectionType((context) -> {
			context.addSectionInput("INPUT_1", Double.class);
			context.addSectionInput("INPUT_2", null);
			context.addSectionOutput("OUTPUT_A", Integer.class, false);
			context.addSectionOutput("OUTPUT_B", String.class, false);
			context.addSectionOutput("OUTPUT_C", null, false);
			context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
			context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Refactor the section with same details
		Change<WoofSectionModel> change = this.operations.refactorSection(this.section, "ADD",
				"net.example.AddSectionSource", "ADD_LOCATION", properties, sectionType, this.sectionInputNameMapping,
				this.sectionOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Section", true);
	}

}
