/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.woof.model.woof;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.model.change.Change;
import net.officefloor.web.template.type.WebTemplateType;

/**
 * Tests refactoring the {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorTemplateTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofTemplateOutputModel} name mapping.
	 */
	private Map<String, String> templateOutputNameMapping = new HashMap<String, String>();

	/**
	 * Initiate.
	 */
	public RefactorTemplateTest() {
		super(true);
	}

	/**
	 * Ensure no change.
	 */
	public void testNoChange() {

		final String TEMPLATE_APPLICATION_PATH = "/template";

		// Obtain the template
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);
		assertEquals("Incorrect template", TEMPLATE_APPLICATION_PATH, template.getApplicationPath());

		// Create the type
		WebTemplateType type = this.constructWebTemplateType((context) -> {
			context.addSectionInput("renderTemplate", null);
			context.addSectionOutput("OUTPUT_A", Integer.class, false);
			context.addSectionOutput("OUTPUT_B", String.class, false);
			context.addSectionOutput("OUTPUT_C", null, false);
			context.addSectionOutput("OUTPUT_D", null, false);
			context.addSectionOutput("OUTPUT_E", null, false);
			context.addSectionOutput("OUTPUT_F", null, false);
			context.addSectionOutput("OUTPUT_INHERIT", null, false);
			context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
			context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
		});

		// Keep template output names
		this.templateOutputNameMapping.put("OUTPUT_A", "OUTPUT_A");
		this.templateOutputNameMapping.put("OUTPUT_B", "OUTPUT_B");
		this.templateOutputNameMapping.put("OUTPUT_C", "OUTPUT_C");
		this.templateOutputNameMapping.put("OUTPUT_D", "OUTPUT_D");
		this.templateOutputNameMapping.put("OUTPUT_E", "OUTPUT_E");
		this.templateOutputNameMapping.put("OUTPUT_F", "OUTPUT_F");

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, TEMPLATE_APPLICATION_PATH,
				new String[] { "ONE", "A", "TWO", "B" }, TEMPLATE_APPLICATION_PATH,
				new String[] { "ONE", "A", "TWO", "B" }, this.getWoofTemplateChangeContext());

		// Record changing
		MockChangeWoofTemplateExtensionSource.recordAssertChange(extensionChange, this);

		// Create the extensions
		WoofTemplateExtension[] extensions = new WoofTemplateExtension[] {
				new WoofTemplateExtensionImpl(MockNoChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("name", "value")),
				new WoofTemplateExtensionImpl(MockNoChangeWoofTemplateExtensionSource.class.getName()),
				new WoofTemplateExtensionImpl(MockChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("ONE", "A"),
						new WoofTemplateExtensionPropertyImpl("TWO", "B")) };

		// Test
		this.replayMockObjects();

		// Refactor the template with same details
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_1", Boolean.TRUE);
		secureLinks.put("LINK_2", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(template, TEMPLATE_APPLICATION_PATH,
				"example/Template.html", "net.example.LogicClass", type, "redirect",
				new HashSet<String>(Arrays.asList("OUTPUT_INHERIT")), "text/plain; charset=UTF-16", "UTF-16", true, "_",
				secureLinks, new String[] { "POST", "PUT", "OTHER" }, extensions, this.templateOutputNameMapping,
				this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Refactor Template", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure change all values.
	 */
	public void testChange() {

		final String TEMPLATE_APPLICATION_PATH = "/change";

		// Obtain the template
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);
		assertEquals("Incorrect template", "/template", template.getApplicationPath());

		// Create the type
		WebTemplateType type = this.constructWebTemplateType((context) -> {
			context.addSectionInput("renderTemplate", null);
			context.addSectionOutput("OUTPUT_A", Integer.class, false);
			context.addSectionOutput("OUTPUT_B", String.class, false);
			context.addSectionOutput("OUTPUT_C", null, false);
			context.addSectionOutput("OUTPUT_D", null, false);
			context.addSectionOutput("OUTPUT_E", null, false);
			context.addSectionOutput("OUTPUT_F", null, false);
			context.addSectionOutput("OUTPUT_INHERIT", null, false);
		});

		// Re-map template output names
		this.templateOutputNameMapping.put("OUTPUT_A", "OUTPUT_B");
		this.templateOutputNameMapping.put("OUTPUT_B", "OUTPUT_C");
		this.templateOutputNameMapping.put("OUTPUT_C", "OUTPUT_D");
		this.templateOutputNameMapping.put("OUTPUT_D", "OUTPUT_E");
		this.templateOutputNameMapping.put("OUTPUT_E", "OUTPUT_F");
		this.templateOutputNameMapping.put("OUTPUT_F", "OUTPUT_A");

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, "/template",
				new String[] { "ONE", "A", "TWO", "B" }, TEMPLATE_APPLICATION_PATH,
				new String[] { "newName", "newValue" }, this.getWoofTemplateChangeContext());

		// Create the extensions
		WoofTemplateExtension[] extensions = new WoofTemplateExtension[] {
				new WoofTemplateExtensionImpl(MockChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("newName", "newValue")),
				new WoofTemplateExtensionImpl(MockNoChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("change", "first")) };

		// Record extension change
		MockChangeWoofTemplateExtensionSource.recordAssertChange(extensionChange, this);

		// Test
		this.replayMockObjects();

		// Refactor the template with changed details
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_2", Boolean.TRUE);
		secureLinks.put("LINK_3", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(template, TEMPLATE_APPLICATION_PATH,
				"example/Change.html", "net.example.ChangeClass", type, "change-redirect",
				new HashSet<String>(Arrays.asList("OUTPUT_INHERIT")), "text/changed", "UTF-CHANGE", false, "c",
				secureLinks, new String[] { "CHANGE" }, extensions, this.templateOutputNameMapping,
				this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Refactor Template", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to remove URI, {@link WoofTemplateOutputModel} and
	 * {@link WoofTemplateExtensionModel} instances.
	 */
	public void testRemoveDetails() {

		final String TEMPLATE_APPLICATION_PATH = "/template";

		// Obtain the template
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);
		assertEquals("Incorrect template", TEMPLATE_APPLICATION_PATH, template.getApplicationPath());

		// Create the type
		WebTemplateType type = this.constructWebTemplateType((context) -> {
			context.addSectionInput("renderTemplate", null);
			// No outputs
		});

		// Register the extension to handle remove
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, TEMPLATE_APPLICATION_PATH,
				new String[] { "ONE", "A", "TWO", "B" }, null, null, this.getWoofTemplateChangeContext());

		// Record extension change
		MockChangeWoofTemplateExtensionSource.recordAssertChange(extensionChange, this);

		// Test
		this.replayMockObjects();

		// Refactor the template removing outputs and extensions
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(template, TEMPLATE_APPLICATION_PATH,
				"example/Remove.ofp", null, type, null, null, null, null, false, null, null, null, null, null,
				this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Refactor Template", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to add URI, {@link WoofTemplateOutputModel} and
	 * {@link WoofTemplateExtensionModel} instances.
	 */
	public void testAddDetails() {

		final String TEMPLATE_APPLICATION_PATH = "/template";

		// Obtain the template
		WoofTemplateModel template = this.model.getWoofTemplates().get(2);
		assertEquals("Incorrect template", TEMPLATE_APPLICATION_PATH, template.getApplicationPath());

		// Create the type
		WebTemplateType type = this.constructWebTemplateType((context) -> {
			context.addSectionInput("renderTemplate", null);
			context.addSectionOutput("OUTPUT_1", Integer.class, false);
			context.addSectionOutput("OUTPUT_2", String.class, false);
			context.addSectionOutput("OUTPUT_3", null, false);
			context.addSectionOutput("OUTPUT_INHERIT", null, false);
			context.addSectionOutput("OUTPUT_4", null, false);
			context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
			context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
		});

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, null, null, TEMPLATE_APPLICATION_PATH,
				new String[] { "ONE", "A", "TWO", "B" }, this.getWoofTemplateChangeContext());

		// Create the extensions
		WoofTemplateExtension[] extensions = new WoofTemplateExtension[] {
				new WoofTemplateExtensionImpl(MockNoChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("name", "value")),
				new WoofTemplateExtensionImpl(MockNoChangeWoofTemplateExtensionSource.class.getName()),
				new WoofTemplateExtensionImpl(MockChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("ONE", "A"),
						new WoofTemplateExtensionPropertyImpl("TWO", "B")) };

		// Record changing
		MockChangeWoofTemplateExtensionSource.recordAssertChange(extensionChange, this);

		// Test
		this.replayMockObjects();

		// Refactor the template removing outputs and extensions
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_1", Boolean.TRUE);
		secureLinks.put("LINK_2", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(template, TEMPLATE_APPLICATION_PATH,
				"example/Add.html", "net.example.AddClass", type, "redirect",
				new HashSet<String>(Arrays.asList("OUTPUT_INHERIT", "OUTPUT_GRAND_INHERIT")),
				"text/html; charset=UTF-8", "UTF-8", true, "_", secureLinks, new String[] { "POST", "OTHER" },
				extensions, this.templateOutputNameMapping, this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Refactor Template", true);

		// Verify
		this.verifyMockObjects();
	}

}