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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
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
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
						context.addSectionInput("renderTemplate", null);
						context.addSectionOutput("OUTPUT_1", Integer.class,
								false);
						context.addSectionOutput("OUTPUT_2", null, false);
						context.addSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addSectionObject("IGNORE_OBJECT",
								DataSource.class);
					}
				});

		// Add the template
		Change<WoofTemplateModel> change = this.operations.addTemplate(
				"example/Template.ofp", "net.example.LogicClass", section,
				"uri");
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
	 * Ensure able to add multiple with clashing names.
	 */
	public void testAddMultipleTemplates() {

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
					}
				});

		// Add the templates
		this.operations.addTemplate("example/Template.ofp", "Class1", section,
				"Template").apply();
		this.operations.addTemplate("example/Template.ofp", "Class2", section,
				"Template").apply(); // add twice
		this.operations.addTemplate("example/Template.ofp", "Class3", section,
				null).apply(); // add with same name by template path
		this.operations.addTemplate("Template.html", "Class4", section, null)
				.apply(); // add with template path resulting in same name

		// Ensure appropriately added templates
		this.validateModel();
	}

	/**
	 * Ensure able to add {@link WoofSectionModel}.
	 */
	public void testAddSection() {

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
						context.addSectionInput("INPUT_A", Integer.class);
						context.addSectionInput("INPUT_B", Long.class);
						context.addSectionInput("INPUT_C", null);
						context.addSectionInput("INPUT_D", null);
						context.addSectionOutput("OUTPUT_1", String.class,
								false);
						context.addSectionOutput("OUTPUT_2", null, false);
						context.addSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addSectionObject("IGNORE_OBJECT",
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
		Change<WoofSectionModel> change = this.operations.addSection("SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION",
				properties, section, inputToUri);
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
	 * Ensure able to add multiple sections with clashing names.
	 */
	public void testAddMultipleSections() {

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
					}
				});

		// Add the sections
		this.operations.addSection("SECTION", "Section1", "Location1", null,
				section, null).apply();
		this.operations.addSection("SECTION", "Section2", "Location2", null,
				section, null).apply();

		// Ensure appropriately added sections
		this.validateModel();
	}

	/**
	 * Ensure able to add {@link WoofResourceModel}.
	 */
	public void testAddResource() {

		// Validate add resource
		Change<WoofResourceModel> change = this.operations
				.addResource("index.html");
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
	 * Ensure able to add multiple {@link WoofResourceModel} instances with
	 * clashing names.
	 */
	public void testAddMultipleResources() {

		// Add the resources
		this.operations.addResource("example/index.html").apply();
		this.operations.addResource("example/index.html").apply();

		// Validate appropriately added resources
		this.validateModel();
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

	/**
	 * Ensure able to add multiple {@link WoofExceptionModel} instances with
	 * clashing classes.
	 */
	public void testAddMultipleExceptions() {

		// Add the exceptions
		for (int i = 0; i <= 2; i++) {
			Change<WoofExceptionModel> change = this.operations
					.addException(SQLException.class.getName());
			change.getTarget().setX(i);
			change.getTarget().setY(i);
			change.apply();
		}

		// Validate appropriately added one exception moved
		this.validateModel();
	}

}