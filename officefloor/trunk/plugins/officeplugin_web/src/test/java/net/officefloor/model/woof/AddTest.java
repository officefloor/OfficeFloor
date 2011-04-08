/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.model.woof;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.model.change.Change;

/**
 * Tests adding to a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure able to add {@link WoofTemplateModel}.
	 */
	public void testAddTemplate() {

		// Create the section type
		OfficeSection section = this.constructOfficeSection("TEMPLATE",
				new OfficeSectionConstructor() {
					@Override
					public void construct(OfficeSectionContext context) {
						context.addOfficeSectionInput("renderTemplate", null);
						context.addOfficeSectionOutput("OUTPUT_1",
								Integer.class, false);
						context.addOfficeSectionOutput("OUTPUT_2", null, false);
						context.addOfficeSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addOfficeSectionObject("IGNORE_OBJECT",
								DataSource.class);
					}
				});

		// Add the template
		Change<WoofTemplateModel> change = this.operations.addTemplate(section,
				"example/Template.ofp", "net.example.LogicClass", "uri");
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Template", true);

		// Ensure appropriately added template
		change.apply();
		WoofTemplateModel template = this.model.getWoofTemplates().get(0);
		assertSame("Incorrect template", template, change.getTarget());
	}

	/**
	 * Ensure able to add {@link WoofSectionModel}.
	 */
	public void testAddSection() {

		// Create the section type
		OfficeSection section = this.constructOfficeSection("SECTION",
				new OfficeSectionConstructor() {
					@Override
					public void construct(OfficeSectionContext context) {
						context.addOfficeSectionInput("INPUT_A", Integer.class);
						context.addOfficeSectionInput("INPUT_B", Long.class);
						context.addOfficeSectionInput("INPUT_C", null);
						context.addOfficeSectionInput("INPUT_D", null);
						context.addOfficeSectionOutput("OUTPUT_1",
								String.class, false);
						context.addOfficeSectionOutput("OUTPUT_2", null, false);
						context.addOfficeSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addOfficeSectionObject("IGNORE_OBJECT",
								DataSource.class);
					}
				});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Create the mapping of input to uri
		Map<String, String> inputToUri = new HashMap<String, String>();
		inputToUri.put("INPUT_A", "uriA");
		inputToUri.put("INPUT_C", "uriC");

		// Add the section
		Change<WoofSectionModel> change = this.operations.addSection(section,
				"net.example.ExampleSectionSource", "SECTION_LOCATION",
				properties, inputToUri);
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Section", true);

		// Ensure appropriately added section
		change.apply();
		WoofSectionModel woofSection = this.model.getWoofSections().get(0);
		assertSame("Incorrect section", woofSection, change.getTarget());
	}

	/**
	 * Ensure able to add {@link WoofResourceModel}.
	 */
	public void testAddResource() {

		// Validate add resource
		Change<WoofResourceModel> change = this.operations.addResource(
				"RESOURCE", "index.html");
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Resource", true);

		// Ensure appropriately added resource
		change.apply();
		WoofResourceModel resource = this.model.getWoofResources().get(0);
		assertSame("Incorrect resource", resource, change.getTarget());
	}

	/**
	 * Ensure able to add {@link WoofExceptionModel}.
	 */
	public void testAddException() {

		// Validate add exception
		Change<WoofExceptionModel> change = this.operations
				.addException(Exception.class.getName());
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Exception", true);

		// Ensure appropriately added exception
		change.apply();
		WoofExceptionModel exception = this.model.getWoofExceptions().get(0);
		assertSame("Incorrect exception", exception, change.getTarget());
	}

}