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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.template.type.WebTemplateType;

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

		// Create the web template type
		WebTemplateType type = this.constructWebTemplateType((context) -> {
			context.addSectionInput("renderTemplate", null);
			context.addSectionOutput("OUTPUT_1", Integer.class, false);
			context.addSectionOutput("OUTPUT_2", null, false);
			context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
			context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
		});

		// Add the template
		Change<WoofTemplateModel> change = this.operations.addTemplate("/path", "example/Template.ofp",
				"net.example.LogicClass", type, null, null, null, false, null, null, null, null,
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

		// Create the type
		WebTemplateType type = this.constructWebTemplateType((context) -> {
			context.addSectionInput("renderTemplate", null);
		});

		// Add the template
		Change<WoofTemplateModel> change = this.operations.addTemplate("/path", "example/Template.ofp",
				"net.example.LogicClass", type, null, "text/html; charset=UTF-16", "UTF-16", false, null, null, null,
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

		// Create the type
		WebTemplateType type = this.constructWebTemplateType(null);

		// Add the root template
		Change<WoofTemplateModel> change = this.operations.addTemplate("/", "root.ofp", null, type, null, null, null,
				false, null, null, null, null, this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Add Template", true);
	}

	/**
	 * Ensure able to add with links and render configuration.
	 */
	public void testAddSecureLinkRenderTemplate() {

		// Create the type
		WebTemplateType type = this.constructWebTemplateType(null);

		// Add the template
		Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();
		secureLinks.put("LINK_1", Boolean.TRUE);
		secureLinks.put("LINK_2", Boolean.FALSE);
		Change<WoofTemplateModel> change = this.operations.addTemplate("/path", "example/Template.ofp",
				"net.example.LogicClass", type, null, null, null, true, null, secureLinks,
				new String[] { "POST", "PUT", "OTHER" }, null, this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Add Template", true);
	}

	/**
	 * Ensure able to add multiple with clashing names.
	 */
	public void testAddMultipleTemplates() {

		// Create the type
		WebTemplateType type = this.constructWebTemplateType(null);

		// Add the first template
		this.operations.addTemplate("/pathA", "example/TemplateOne.ofp", "Class1", type, null, null, null, false, null,
				null, null, null, this.getWoofTemplateChangeContext()).apply();

		// Add twice
		this.operations.addTemplate("/pathB", "example/TemplateTwo.ofp", "Class2", type, null, null, null, false, null,
				null, null, null, this.getWoofTemplateChangeContext()).apply();

		// Add again with absolute URI
		this.operations.addTemplate("/pathC", "example/TemplateThree.ofp", "Class3", type, null, null, null, false,
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

		// Create the type
		WebTemplateType type = this.constructWebTemplateType(null);

		// Test
		this.replayMockObjects();

		// Add the template with extensions
		Change<?> addChange = this.operations.addTemplate(TEMPLATE_URI, "example/Template.ofp",
				"net.example.LogicClass", type, null, null, null, false, null, null, null, extensions,
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
		SectionType section = this.constructSectionType((context) -> {
			context.addSectionInput("INPUT_A", Integer.class);
			context.addSectionInput("INPUT_B", Long.class);
			context.addSectionInput("INPUT_C", null);
			context.addSectionInput("INPUT_D", null);
			context.addSectionOutput("OUTPUT_1", String.class, false);
			context.addSectionOutput("OUTPUT_2", null, false);
			context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
			context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
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
		SectionType section = this.constructSectionType(null);

		// Add the sections
		this.operations.addSection("SECTION", "Section1", "Location1", null, section).apply();
		this.operations.addSection("SECTION", "Section2", "Location2", null, section).apply();

		// Ensure appropriately added sections
		this.validateModel();
	}

	/**
	 * Ensure able to add {@link WoofProcedureModel}.
	 */
	public void testAddProcedure() {

		// Create the procedure type
		ProcedureType procedure = this.constructProcedureType("procedure", String.class, (builder) -> {
			builder.addFlowType("OUTPUT_A", String.class);
			builder.addFlowType("OUTPUT_B", null);
			builder.setNextArgumentType(Short.class);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Add the procedure
		Change<WoofProcedureModel> change = this.operations.addProcedure("PROCEDURE", "resource", "Class", "procedure",
				properties, procedure);
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Procedure", true);

		// Ensure appropriately added procedure
		change.apply();
		WoofProcedureModel woofProcedure = this.model.getWoofProcedures().get(0);
		assertSame("Incorrect section", woofProcedure, change.getTarget());
	}

	/**
	 * Ensure able to add multiple procedures with clashing names.
	 */
	public void testAddMultipleProcedures() {

		// Create the procedure type
		ProcedureType procedure = this.constructProcedureType("procedure", null, null);

		// Add the procedures
		this.operations.addProcedure("PROCEDURE", "resource1", "Class", "method", null, procedure).apply();
		this.operations.addProcedure("PROCEDURE", "resource2", "JavaScript", "function", null, procedure).apply();

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
					context.addDependency("DEPENDENCY", String.class, "qualifier", 0, null);

					// Include flows
					context.addFlow("OUTPUT_1", String.class, 0, null);
					context.addFlow("OUTPUT_2", null, 1, null);
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
		HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType = this.constructHttpSecurityType(HttpCredentials.class, null);

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
	 * Check add multiple {@link WoofResourceModel} instances with clashing names.
	 */
	public void testAddMultipleResources() {

		// Add the resources
		this.operations.addResource("example/index.html").apply();
		this.validateModel();

		// Ensure conflict
		Change<WoofResourceModel> conflict = this.operations.addResource("example/index.html");
		assertFalse("Should not be able to add another resource by same path", conflict.canApply());

		// Ensure no further change
		conflict.apply();
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

		// Add the exception
		this.operations.addException(SQLException.class.getName()).apply();
		this.validateModel();

		// Ensure can not add exception again
		Change<WoofExceptionModel> change = this.operations.addException(SQLException.class.getName());
		assertFalse("Should not be able to add same exception", change.canApply());

		// Ensure not add
		change.apply();
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
