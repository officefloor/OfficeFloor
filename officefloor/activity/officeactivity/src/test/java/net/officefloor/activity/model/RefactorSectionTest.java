/*-
 * #%L
 * Activity
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.activity.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link ActivitySectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorSectionTest extends AbstractActivityChangesTestCase {

	/**
	 * {@link ActivitySectionModel}.
	 */
	private ActivitySectionModel section;

	/**
	 * {@link ActivitySectionInputModel} name mapping.
	 */
	private Map<String, String> sectionInputNameMapping = new HashMap<String, String>();;

	/**
	 * {@link ActivitySectionOutputModel} name mapping.
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
		this.section = this.model.getActivitySections().get(0);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Create the section type
		SectionType sectionType = this.constructSectionType((context) -> {
			context.addSectionInput("INPUT", null);
			context.addSectionOutput("OUTPUT_A", Integer.class, false);
			context.addSectionOutput("OUTPUT_B", null, false);
			context.addSectionOutput("OUTPUT_C", null, false);
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

		// Refactor the section with same details
		Change<ActivitySectionModel> change = this.operations.refactorSection(this.section, "SECTION",
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
		this.sectionOutputNameMapping.put("OUTPUT_A", "OUTPUT_C");

		// Refactor the section with same details
		Change<ActivitySectionModel> change = this.operations.refactorSection(this.section, "CHANGE",
				"net.example.ChangeSectionSource", "CHANGE_LOCATION", properties, sectionType,
				this.sectionInputNameMapping, this.sectionOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Section", true);
	}

	/**
	 * Ensure handle remove {@link PropertyModel}, {@link ActivitySectionInputModel}
	 * and {@link ActivitySectionOutputModel} instances.
	 */
	public void testRemoveDetails() {

		// Create the section type
		SectionType sectionType = this.constructSectionType(null);

		// Refactor the section removing details
		Change<ActivitySectionModel> change = this.operations.refactorSection(this.section, "REMOVE",
				"net.example.RemoveSectionSource", "REMOVE_LOCATION", null, sectionType, null, null);

		// Validate change
		this.assertChange(change, null, "Refactor Section", true);
	}

	/**
	 * Ensure handle adding {@link PropertyModel}, {@link ActivitySectionInputModel}
	 * and {@link ActivitySectionOutputModel} instances.
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
		Change<ActivitySectionModel> change = this.operations.refactorSection(this.section, "ADD",
				"net.example.AddSectionSource", "ADD_LOCATION", properties, sectionType, this.sectionInputNameMapping,
				this.sectionOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Section", true);
	}

}
