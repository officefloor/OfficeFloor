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
package net.officefloor.model.woof;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

/**
 * Tests refactoring the {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorTemplateTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofTemplateModel}.
	 */
	private WoofTemplateModel template;

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.template = this.model.getWoofTemplates().get(0);
	}

	/**
	 * Ensure no change.
	 */
	public void testNoChange() {

		final String TEMPLATE_URI = "template";

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
						context.addSectionInput("renderTemplate", null);
						context.addSectionOutput("OUTPUT_1", Integer.class,
								false);
						context.addSectionOutput("OUTPUT_2", String.class,
								false);
						context.addSectionOutput("OUTPUT_3", null, false);
						context.addSectionOutput("OUTPUT_INHERIT", null, false);
						context.addSectionOutput(
								HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME,
								null, false);
						context.addSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addSectionObject("IGNORE_OBJECT",
								DataSource.class, null);
					}
				});

		// Keep template output names
		this.templateOutputNameMapping.put("OUTPUT_1", "OUTPUT_1");
		this.templateOutputNameMapping.put("OUTPUT_2", "OUTPUT_2");
		this.templateOutputNameMapping.put("OUTPUT_3", "OUTPUT_3");

		// Obtain the super template
		WoofTemplateModel superTemplate = this.model.getWoofTemplates().get(2);
		assertEquals("Incorrect super template", "TEMPLATE_PARENT",
				superTemplate.getWoofTemplateName());

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange,
				TEMPLATE_URI, new String[] { "ONE", "A", "TWO", "B" },
				TEMPLATE_URI, new String[] { "ONE", "A", "TWO", "B" },
				this.getWoofTemplateChangeContext());

		// Record changing
		MockChangeWoofTemplateExtensionSource.recordAssertChange(
				extensionChange, this);

		// Create the extensions
		WoofTemplateExtension[] extensions = new WoofTemplateExtension[] {
				new WoofTemplateExtensionImpl(
						MockNoChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("name", "value")),
				new WoofTemplateExtensionImpl(
						MockNoChangeWoofTemplateExtensionSource.class.getName()),
				new WoofTemplateExtensionImpl(
						MockChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("ONE", "A"),
						new WoofTemplateExtensionPropertyImpl("TWO", "B")) };

		// Test
		this.replayMockObjects();

		// Refactor the template with same details
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_1", Boolean.TRUE);
		secureLinks.put("LINK_2", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(
				this.template, TEMPLATE_URI, "example/Template.html",
				"net.example.LogicClass", section, superTemplate,
				new HashSet<String>(Arrays.asList("OUTPUT_INHERIT")), true,
				secureLinks, new String[] { "POST", "PUT", "OTHER" }, true,
				extensions, this.templateOutputNameMapping,
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

		final String TEMPLATE_URI = "change";

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
						context.addSectionInput("renderTemplate", null);
						context.addSectionOutput("OUTPUT_1", Integer.class,
								false);
						context.addSectionOutput("OUTPUT_2", String.class,
								false);
						context.addSectionOutput("OUTPUT_3", null, false);
						context.addSectionOutput("OUTPUT_INHERIT", null, false);
						context.addSectionOutput(
								HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME,
								null, false);
					}
				});

		// Re-map template output names
		this.templateOutputNameMapping.put("OUTPUT_2", "OUTPUT_1");
		this.templateOutputNameMapping.put("OUTPUT_3", "OUTPUT_2");
		this.templateOutputNameMapping.put("OUTPUT_1", "OUTPUT_3");

		// Obtain the super template
		WoofTemplateModel superTemplate = this.model.getWoofTemplates().get(1);
		assertEquals("Incorrect super template", "TEMPLATE_LINK",
				superTemplate.getWoofTemplateName());

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange,
				"template", new String[] { "ONE", "A", "TWO", "B" },
				TEMPLATE_URI, new String[] { "newName", "newValue" },
				this.getWoofTemplateChangeContext());

		// Create the extensions
		WoofTemplateExtension[] extensions = new WoofTemplateExtension[] {
				new WoofTemplateExtensionImpl(
						MockChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("newName",
								"newValue")),
				new WoofTemplateExtensionImpl(
						MockNoChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("change", "first")) };

		// Record extension change
		MockChangeWoofTemplateExtensionSource.recordAssertChange(
				extensionChange, this);

		// Test
		this.replayMockObjects();

		// Refactor the template with changed details
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_2", Boolean.TRUE);
		secureLinks.put("LINK_3", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(
				this.template, TEMPLATE_URI, "example/Change.html",
				"net.example.ChangeClass", section, superTemplate,
				new HashSet<String>(Arrays.asList("OUTPUT_INHERIT")), false,
				secureLinks, new String[] { "CHANGE" }, false, extensions,
				this.templateOutputNameMapping,
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

		final String TEMPLATE_URI = "remove";

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
						context.addSectionInput("renderTemplate", null);
						// No outputs
					}
				});

		// Register the extension to handle remove
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange,
				"template", new String[] { "ONE", "A", "TWO", "B" }, null,
				null, this.getWoofTemplateChangeContext());

		// Record extension change
		MockChangeWoofTemplateExtensionSource.recordAssertChange(
				extensionChange, this);

		// Test
		this.replayMockObjects();

		// Refactor the template removing outputs and extensions
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(
				this.template, TEMPLATE_URI, "example/Remove.html", null,
				section, null, null, false, null, null, false, null, null,
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

		final String TEMPLATE_URI = "add";

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
						context.addSectionInput("renderTemplate", null);
						context.addSectionOutput("OUTPUT_1", Integer.class,
								false);
						context.addSectionOutput("OUTPUT_2", String.class,
								false);
						context.addSectionOutput("OUTPUT_3", null, false);
						context.addSectionOutput("OUTPUT_INHERIT", null, false);
						context.addSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addSectionObject("IGNORE_OBJECT",
								DataSource.class, null);
					}
				});

		// Obtain the super template
		WoofTemplateModel superTemplate = this.model.getWoofTemplates().get(1);
		assertEquals("Incorrect super template", "TEMPLATE_PARENT",
				superTemplate.getWoofTemplateName());

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, null,
				null, TEMPLATE_URI, new String[] { "ONE", "A", "TWO", "B" },
				this.getWoofTemplateChangeContext());

		// Create the extensions
		WoofTemplateExtension[] extensions = new WoofTemplateExtension[] {
				new WoofTemplateExtensionImpl(
						MockNoChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("name", "value")),
				new WoofTemplateExtensionImpl(
						MockNoChangeWoofTemplateExtensionSource.class.getName()),
				new WoofTemplateExtensionImpl(
						MockChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("ONE", "A"),
						new WoofTemplateExtensionPropertyImpl("TWO", "B")) };

		// Record changing
		MockChangeWoofTemplateExtensionSource.recordAssertChange(
				extensionChange, this);

		// Test
		this.replayMockObjects();

		// Refactor the template removing outputs and extensions
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_1", Boolean.TRUE);
		secureLinks.put("LINK_2", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(
				this.template, TEMPLATE_URI, "example/Add.html",
				"net.example.AddClass", section, superTemplate,
				new HashSet<String>(Arrays.asList("OUTPUT_INHERIT")), true,
				secureLinks, new String[] { "POST", "OTHER" }, true,
				extensions, this.templateOutputNameMapping,
				this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Refactor Template", true);

		// Verify
		this.verifyMockObjects();
	}

}