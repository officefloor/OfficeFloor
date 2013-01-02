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
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

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
	private Map<String, String> templateOutputNameMapping = new HashMap<String, String>();;

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

		// Record GWT changes
		this.recordGwtModulePath("net/example/template.gwt.xml");
		Change<?> gwtUpdateChange = this.recordGwtUpdate("template",
				"net.example.client.ExampleGwtEntryPoint",
				"net/example/template.gwt.xml");
		this.recordReturn(gwtUpdateChange, gwtUpdateChange.getConflicts(),
				new Conflict[0]);
		gwtUpdateChange.apply();
		gwtUpdateChange.revert();
		gwtUpdateChange.apply();
		gwtUpdateChange.revert();

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

		// Test
		this.replayMockObjects();

		// Refactor the template with same details
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_1", Boolean.TRUE);
		secureLinks.put("LINK_2", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(
				this.template, "template", "example/Template.html",
				"net.example.LogicClass", section, true, secureLinks,
				new String[] { "POST", "PUT", "OTHER" },
				"net.example.client.ExampleGwtEntryPoint", new String[] {
						"net.example.GwtServiceAsync",
						"net.example.GwtAnotherAsync" }, true, "manualPublish",
				this.templateOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Template", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure change all values.
	 */
	public void testChange() {

		// Record GWT changes
		this.recordGwtModulePath("net/example/change.gwt.xml");
		Change<?> gwtUpdateChange = this.recordGwtUpdate("change",
				"net.example.client.ExampleGwtEntryPoint",
				"net/example/template.gwt.xml");
		this.recordReturn(gwtUpdateChange, gwtUpdateChange.getConflicts(),
				new Conflict[0]);
		gwtUpdateChange.apply();
		gwtUpdateChange.revert();
		gwtUpdateChange.apply();
		gwtUpdateChange.revert();

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
					}
				});

		// Re-map template output names
		this.templateOutputNameMapping.put("OUTPUT_2", "OUTPUT_1");
		this.templateOutputNameMapping.put("OUTPUT_3", "OUTPUT_2");
		this.templateOutputNameMapping.put("OUTPUT_1", "OUTPUT_3");

		// Test
		this.replayMockObjects();

		// Refactor the template with changed details
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_2", Boolean.TRUE);
		secureLinks.put("LINK_3", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(
				this.template, "change", "example/Change.html",
				"net.example.ChangeClass", section, false, secureLinks,
				new String[] { "CHANGE" },
				"net.example.client.ExampleGwtEntryPoint",
				new String[] { "net.example.GwtChangeAsync" }, true,
				"manualChange", this.templateOutputNameMapping);

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

		/*
		 * Remove Woof configuration but leave GWT configuration (i.e. no GWT
		 * changes).
		 */

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
						context.addSectionInput("renderTemplate", null);
						// No outputs
					}
				});

		// Test
		this.replayMockObjects();

		// Refactor the template removing outputs and extensions
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(
				this.template, "remove", "example/Remove.html", null, section,
				false, null, null, null, null, false, null, null);

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

		// Record GWT changes
		this.recordGwtModulePath("net/example/add.gwt.xml");
		Change<?> gwtUpdateChange = this.recordGwtUpdate("add",
				"net.example.client.AddGwtEntryPoint", null);
		this.recordReturn(gwtUpdateChange, gwtUpdateChange.getConflicts(),
				new Conflict[0]);
		gwtUpdateChange.apply();
		gwtUpdateChange.revert();
		gwtUpdateChange.apply();
		gwtUpdateChange.revert();

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
						context.addSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addSectionObject("IGNORE_OBJECT",
								DataSource.class, null);
					}
				});

		// Test
		this.replayMockObjects();

		// Refactor the template removing outputs and extensions
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_1", Boolean.TRUE);
		secureLinks.put("LINK_2", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(
				this.template, "add", "example/Add.html",
				"net.example.AddClass", section, true, secureLinks,
				new String[] { "POST", "OTHER" },
				"net.example.client.AddGwtEntryPoint",
				new String[] { "net.example.GwtAddAsync" }, true, "manualAdd",
				this.templateOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Template", true);

		// Verify
		this.verifyMockObjects();
	}

}