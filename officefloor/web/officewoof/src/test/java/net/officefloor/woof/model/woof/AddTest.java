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
package net.officefloor.woof.model.woof;

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
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.type.HttpSecurityType;

/**
 * Tests adding to a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure able to add {@link WoofHttpContinuationModel}.
	 */
	public void testAddHttpContinuation() {

		// Add the HTTP continuation
		Change<WoofHttpContinuationModel> change = this.operations.addHttpContinuation("/path", false);

		// Validate the change
		this.assertChange(change, null, "Add HTTP Continuation", true);

		// Ensure appropriately added
		change.apply();
		WoofHttpContinuationModel continuation = this.model.getWoofHttpContinuations().get(0);
		assertSame("Incorrect HTTP continuation", continuation, change.getTarget());
	}

	/**
	 * Ensure able to add {@link WoofHttpInputModel}.
	 */
	public void testAddHttpInput() {

		// Add the HTTP input
		Change<WoofHttpInputModel> change = this.operations.addHttpInput("/path", "POST", false);

		// Validate the change
		this.assertChange(change, null, "Add HTTP Input", true);

		// Ensure appropriately added
		change.apply();
		WoofHttpInputModel httpInput = this.model.getWoofHttpInputs().get(0);
		assertSame("Incorrect HTTP input", httpInput, change.getTarget());
	}

	/**
	 * Ensure able to add {@link WoofTemplateModel}.
	 */
	public void testAddTemplate() {

		// Create the section type
		SectionType section = this.constructSectionType(new SectionTypeConstructor() {
			@Override
			public void construct(SectionTypeContext context) {
				context.addSectionInput("renderTemplate", null);
				context.addSectionOutput("OUTPUT_1", Integer.class, false);
				context.addSectionOutput("OUTPUT_2", null, false);
				context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
				context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
			}
		});

		// Add the template
		Change<WoofTemplateModel> change = this.operations.addTemplate("/path", "example/Template.ofp",
				"net.example.LogicClass", section, null, null, null, false, null, null, null, null,
				this.getWoofTemplateChangeContext());
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
	 * Ensure able to add {@link WoofTemplateModel} with a
	 * <code>Content-Type</code>.
	 */
	public void testAddTemplateWithContentType() {

		// Create the section type
		SectionType section = this.constructSectionType(new SectionTypeConstructor() {
			@Override
			public void construct(SectionTypeContext context) {
				context.addSectionInput("renderTemplate", null);
			}
		});

		// Add the template
		Change<WoofTemplateModel> change = this.operations.addTemplate("/path", "example/Template.ofp",
				"net.example.LogicClass", section, null, "text/html; charset=UTF-16", "UTF-16", false, null, null, null,
				null, this.getWoofTemplateChangeContext());
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
		SectionType section = this.constructSectionType(new SectionTypeConstructor() {
			@Override
			public void construct(SectionTypeContext context) {
			}
		});

		// Add the root template
		Change<WoofTemplateModel> change = this.operations.addTemplate("/", "root.ofp", null, section, null, null, null,
				false, null, null, null, null, this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Add Template", true);
	}

	/**
	 * Ensure able to add with links and render configuration.
	 */
	public void testAddSecureLinkRenderTemplate() {

		// Create the section type
		SectionType section = this.constructSectionType(new SectionTypeConstructor() {
			@Override
			public void construct(SectionTypeContext context) {
			}
		});

		// Add the template
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_1", Boolean.TRUE);
		secureLinks.put("LINK_2", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.addTemplate("/path", "example/Template.ofp",
				"net.example.LogicClass", section, null, null, null, true, null, secureLinks,
				new String[] { "POST", "PUT", "OTHER" }, null, this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Add Template", true);
	}

	/**
	 * Ensure able to add multiple with clashing names.
	 */
	public void testAddMultipleTemplates() {

		// Create the section type
		SectionType section = this.constructSectionType(new SectionTypeConstructor() {
			@Override
			public void construct(SectionTypeContext context) {
			}
		});

		// Add the first template
		this.operations.addTemplate("/pathA", "example/TemplateOne.ofp", "Class1", section, null, null, null, false,
				null, null, null, null, this.getWoofTemplateChangeContext()).apply();

		// Add twice
		this.operations.addTemplate("/pathB", "example/TemplateTwo.ofp", "Class2", section, null, null, null, false,
				null, null, null, null, this.getWoofTemplateChangeContext()).apply();

		// Add again with absolute URI
		this.operations.addTemplate("/pathC", "example/TemplateThree.ofp", "Class3", section, null, null, null, false,
				null, null, null, null, this.getWoofTemplateChangeContext()).apply();

		// Ensure appropriately added templates
		this.validateModel();
	}

	/**
	 * Ensure can add a template with an extension.
	 */
	public void testAddTemplateWithExtension() {

		final String TEMPLATE_URI = "/path";

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, null, null, TEMPLATE_URI,
				new String[] { "ONE", "A", "TWO", "B" }, this.getWoofTemplateChangeContext());

		// Create the extensions
		WoofTemplateExtension[] extensions = new WoofTemplateExtension[] {
				new WoofTemplateExtensionImpl(MockNoChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("name", "value")),
				new WoofTemplateExtensionImpl(MockNoChangeWoofTemplateExtensionSource.class.getName()),
				new WoofTemplateExtensionImpl(MockChangeWoofTemplateExtensionSource.class.getName(),
						new WoofTemplateExtensionPropertyImpl("ONE", "A"),
						new WoofTemplateExtensionPropertyImpl("TWO", "B")) };

		// Record the extension change
		extensionChange.apply();

		// Create the section type
		SectionType section = this.constructSectionType(new SectionTypeConstructor() {
			@Override
			public void construct(SectionTypeContext context) {
			}
		});

		// Test
		this.replayMockObjects();

		// Add the template with extensions
		Change<?> addChange = this.operations.addTemplate(TEMPLATE_URI, "example/Template.ofp",
				"net.example.LogicClass", section, null, null, null, false, null, null, null, extensions,
				this.getWoofTemplateChangeContext());
		addChange.apply();

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
		SectionType section = this.constructSectionType(new SectionTypeConstructor() {
			@Override
			public void construct(SectionTypeContext context) {
				context.addSectionInput("INPUT_A", Integer.class);
				context.addSectionInput("INPUT_B", Long.class);
				context.addSectionInput("INPUT_C", null);
				context.addSectionInput("INPUT_D", null);
				context.addSectionOutput("OUTPUT_1", String.class, false);
				context.addSectionOutput("OUTPUT_2", null, false);
				context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
				context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
			}
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Add the section
		Change<WoofSectionModel> change = this.operations.addSection("SECTION", "net.example.ExampleSectionSource",
				"SECTION_LOCATION", properties, section);
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
		SectionType section = this.constructSectionType(new SectionTypeConstructor() {
			@Override
			public void construct(SectionTypeContext context) {
			}
		});

		// Add the sections
		this.operations.addSection("SECTION", "Section1", "Location1", null, section).apply();
		this.operations.addSection("SECTION", "Section2", "Location2", null, section).apply();

		// Ensure appropriately added sections
		this.validateModel();
	}

	/**
	 * Ensure able to add a {@link WoofSecurityModel}.
	 */
	public void testAddSecurity() {

		// Create the HTTP Security type
		HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType = this.constructHttpSecurityType(HttpCredentials.class,
				(context) -> {
					// Should be auto-wired (not in configuration)
					context.addDependency("DEPENDENCY", String.class, "qualifier", null);

					// Include flows
					context.addFlow("OUTPUT_1", String.class, null);
					context.addFlow("OUTPUT_2", null, null);
				});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Specify the access
		Change<WoofSecurityModel> change = this.operations.addSecurity("SECURITY", "net.example.HttpSecuritySource",
				2000, properties, new String[] { "application/json" }, httpSecurityType);
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Security", true);

		// Ensure appropriately specified access
		change.apply();
		WoofSecurityModel woofSecurity = this.model.getWoofSecurities().get(0);

		assertSame("Incorrect security", woofSecurity, change.getTarget());
	}

	/**
	 * Ensure able to specify the {@link WoofSecurityModel} with no application
	 * required behaviour.
	 */
	public void testAddSecurityWithNoApplicationBehaviour() {

		// Create the HTTP Security type
		HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType = this.constructHttpSecurityType(HttpCredentials.class,
				(context) -> {
					// Nothing required of application
				});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.a").setValue("value.a");
		properties.addProperty("name.b").setValue("value.b");

		// Specify the access
		Change<WoofSecurityModel> change = this.operations.addSecurity("SECURITY", "net.other.HttpSecuritySource", 3000,
				properties, new String[] { "application/json", "application/xml" }, httpSecurityType);
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Security", true);

		// Ensure appropriately specified access
		change.apply();
		WoofSecurityModel woofSecurity = this.model.getWoofSecurities().get(0);
		assertSame("Incorrect security", woofSecurity, change.getTarget());
	}

	/**
	 * Ensure able to add {@link WoofGovernanceModel}.
	 */
	public void testAddGovernance() {

		// Create the governance type
		final GovernanceType<?, ?> governanceType = this.createMock(GovernanceType.class);
		this.replayMockObjects();

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Validate add governance
		Change<WoofGovernanceModel> change = this.operations.addGovernance("GOVERNANCE",
				"net.example.ExampleGovernanceSource", properties, governanceType);
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
		final GovernanceType<?, ?> governanceType = this.createMock(GovernanceType.class);
		this.replayMockObjects();

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();

		// Add the governances
		this.operations.addGovernance("GOVERNANCE", "net.example.ExampleGovernanceSource", properties, governanceType)
				.apply();
		this.operations.addGovernance("GOVERNANCE", "net.example.ExampleGovernanceSource", properties, governanceType)
				.apply();

		// Validate appropriately added governances
		this.validateModel();
	}

	/**
	 * Ensure able to add {@link WoofResourceModel}.
	 */
	public void testAddResource() {

		// Validate add resource
		Change<WoofResourceModel> change = this.operations.addResource("index.html");
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
		Change<WoofExceptionModel> change = this.operations.addException(Exception.class.getName());
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
			Change<WoofExceptionModel> change = this.operations.addException(SQLException.class.getName());
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