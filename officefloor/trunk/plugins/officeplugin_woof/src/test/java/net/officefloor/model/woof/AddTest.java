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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

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
						context.addSectionOutput(
								HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME,
								null, false);
						context.addSectionOutput("OUTPUT_1", Integer.class,
								false);
						context.addSectionOutput("OUTPUT_2", null, false);
						context.addSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addSectionObject("IGNORE_OBJECT",
								DataSource.class, null);
					}
				});

		// Add the template
		Change<WoofTemplateModel> change = this.operations.addTemplate("uri",
				"example/Template.ofp", "net.example.LogicClass", section,
				null, false, null, null, false, null, null, false, null);
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
	 * Ensure able to add {@link WoofTemplateModel} continuing rendering.
	 */
	public void testAddTemplateWithContinueRendering() {

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
						context.addSectionInput("renderTemplate", null);
						context.addSectionOutput(
								HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME,
								null, false);
						context.addSectionOutput("OUTPUT_1", Integer.class,
								false);
						context.addSectionOutput("OUTPUT_2", null, false);
						context.addSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addSectionObject("IGNORE_OBJECT",
								DataSource.class, null);
					}
				});

		// Add the template
		Change<WoofTemplateModel> change = this.operations.addTemplate("uri",
				"example/Template.ofp", "net.example.LogicClass", section,
				null, false, null, null, true, null, null, false, null);
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
	 * Ensure able to add root {@link WoofTemplateModel}.
	 */
	public void testAddRootTemplate() {

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
					}
				});

		// Add the root template
		Change<WoofTemplateModel> change = this.operations.addTemplate("/",
				"root.ofp", null, section, null, false, null, null, false,
				null, null, false, null);

		// Validate change
		this.assertChange(change, null, "Add Template", true);
	}

	/**
	 * Ensure able to add with links and redirect configuration.
	 */
	public void testAddSecureLinkRedirectConfiguredTemplate() {

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
					}
				});

		// Add the template
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_1", Boolean.TRUE);
		secureLinks.put("LINK_2", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.addTemplate(
				"Template", "example/Template.ofp", "net.example.LogicClass",
				section, null, true, secureLinks, new String[] { "POST", "PUT",
						"OTHER" }, true, null, null, false, null);

		// Validate change
		this.assertChange(change, null, "Add Template", true);
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

		// Add the first template
		this.operations.addTemplate("Template", "example/Template.ofp",
				"Class1", section, null, false, null, null, false, null, null,
				false, null).apply();

		// Add twice
		this.operations.addTemplate("Template", "example/Template.ofp",
				"Class2", section, null, false, null, null, false, null, null,
				false, null).apply();

		// Add again with absolute URI
		this.operations.addTemplate("/Template", "example/Template.ofp",
				"Class3", section, null, false, null, null, false, null, null,
				false, null).apply();

		// Ensure appropriately added templates
		this.validateModel();
	}

	/**
	 * Ensure can add a template with a GWT extension.
	 */
	public void testAddTemplateWithGwtExtension() {

		final String TEMPLATE_URI = "uri";
		final String ENTRY_POINT_CLASS_NAME = "net.example.client.ExampleGwtEntryPoint";
		final String[] SERVICE_ASYNC_INTERFACE_NAMES = new String[] {
				"net.example.GwtServiceAsync", "net.example.GwtAnotherAsync" };

		// Record GWT changes
		this.recordGwtModulePath("net/example/uri.gwt.xml");
		Change<?> change = this.recordGwtUpdate(TEMPLATE_URI,
				ENTRY_POINT_CLASS_NAME, null);
		change.apply();

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
					}
				});

		// Test
		this.replayMockObjects();

		// Add the template with GWT
		this.operations.addTemplate(TEMPLATE_URI, "example/Template.ofp",
				"net.example.LogicClass", section, null, false, null, null,
				false, ENTRY_POINT_CLASS_NAME, SERVICE_ASYNC_INTERFACE_NAMES,
				false, null).apply();

		// Verify
		this.verifyMockObjects();

		// Ensure appropriately added templates
		this.validateModel();
	}

	/**
	 * Ensure can add a template with a Comet extension.
	 */
	public void testAddTemplateWithCometExtension() {

		final String TEMPLATE_URI = "uri";
		final String PUBLISH_METHOD_NAME = "manualPublish";

		// Record GWT changes

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
					}
				});

		// Test
		this.replayMockObjects();

		// Add the template with Comet
		this.operations.addTemplate(TEMPLATE_URI, "example/Template.ofp",
				"net.example.LogicClass", section, null, false, null, null,
				false, null, null, true, PUBLISH_METHOD_NAME).apply();

		// Verify
		this.verifyMockObjects();

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
								DataSource.class, null);
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
	 * Ensure able to specify the {@link WoofAccessModel}.
	 */
	public void testSetAccess() {

		// Create the HTTP Security type
		HttpSecurityType<?, ?, ?, ?> httpSecurityType = this
				.constructHttpSecurityType(HttpCredentials.class,
						new HttpSecurityTypeConstructor() {
							@Override
							public void construct(
									HttpSecurityTypeContext context) {

								// Should be auto-wired (not in configuration)
								context.addDependency("DEPENDENCY",
										String.class, "qualifier", null);

								// Include flows
								context.addFlow("OUTPUT_1", String.class, null);
								context.addFlow("OUTPUT_2", null, null);
							}
						});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Specify the access
		Change<WoofAccessModel> change = this.operations.setAccess(
				"net.example.HttpSecuritySource", 2000, properties,
				httpSecurityType);
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Set Access", true);

		// Ensure appropriately specified access
		change.apply();
		WoofAccessModel woofAccess = this.model.getWoofAccess();
		assertSame("Incorrect access", woofAccess, change.getTarget());
	}

	/**
	 * Ensure able to specify the {@link WoofAccessModel} with no application
	 * required behaviour.
	 */
	public void testSetAccessWithNoApplicationBehaviour() {

		// Create the HTTP Security type
		HttpSecurityType<?, ?, ?, ?> httpSecurityType = this
				.constructHttpSecurityType(HttpCredentials.class,
						new HttpSecurityTypeConstructor() {
							@Override
							public void construct(
									HttpSecurityTypeContext context) {
								// Nothing required of application
							}
						});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.a").setValue("value.a");
		properties.addProperty("name.b").setValue("value.b");

		// Specify the access
		Change<WoofAccessModel> change = this.operations.setAccess(
				"net.other.HttpSecuritySource", 3000, properties,
				httpSecurityType);
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Set Access", true);

		// Ensure appropriately specified access
		change.apply();
		WoofAccessModel woofAccess = this.model.getWoofAccess();
		assertSame("Incorrect access", woofAccess, change.getTarget());
	}

	/**
	 * Ensure able to add {@link WoofGovernanceModel}.
	 */
	public void testAddGovernance() {

		// Create the governance type
		final GovernanceType<?, ?> governanceType = this
				.createMock(GovernanceType.class);
		this.replayMockObjects();

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Validate add governance
		Change<WoofGovernanceModel> change = this.operations.addGovernance(
				"GOVERNANCE", "net.example.ExampleGovernanceSource",
				properties, governanceType);
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Governance", true);

		// Ensure appropriately added governance
		change.apply();
		WoofGovernanceModel governance = this.model.getWoofGovernances().get(0);
		assertSame("Incorrect resource", governance, change.getTarget());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to add multiple {@link WoofGovernanceModel} instances with
	 * clashing names.
	 */
	public void testAddMultipleGovernances() {

		// Create the governance type
		final GovernanceType<?, ?> governanceType = this
				.createMock(GovernanceType.class);
		this.replayMockObjects();

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();

		// Add the governances
		this.operations.addGovernance("GOVERNANCE",
				"net.example.ExampleGovernanceSource", properties,
				governanceType).apply();
		this.operations.addGovernance("GOVERNANCE",
				"net.example.ExampleGovernanceSource", properties,
				governanceType).apply();

		// Validate appropriately added governances
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

	/**
	 * Ensure able to add {@link WoofStartModel}.
	 */
	public void testAddStart() {

		// Validate add start
		Change<WoofStartModel> change = this.operations.addStart();
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Start", true);

		// Ensure appropriately added exception
		change.apply();
		WoofStartModel start = this.model.getWoofStarts().get(0);
		assertSame("Incorrect start", start, change.getTarget());
	}

	/**
	 * Ensure able to add multiple {@link WoofStartModel} instances.
	 */
	public void testAddMultipleStarts() {

		// Add the starts
		for (int i = 0; i <= 2; i++) {
			Change<WoofStartModel> change = this.operations.addStart();
			change.getTarget().setX(i);
			change.getTarget().setY(i);
			change.apply();
		}

		// Validate appropriately added starts
		this.validateModel();
	}

}