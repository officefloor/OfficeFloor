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

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.filesystem.FileSystemConfigurationContext;
import net.officefloor.configuration.impl.memory.MemoryConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshaling/unmarshaling of the {@link WoofModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link WoofModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {
		// Specify location of the configuration
		this.configurationItem = FileSystemConfigurationContext
				.createWritableConfigurationItem(this.findFile(this.getClass(), "WebOnOfficeFloor.woof.xml"));
	}

	/**
	 * Ensure retrieve the {@link WoofModel}.
	 */
	public void testRetrieveWoof() throws Exception {

		// Load the WoOF
		ModelRepository repository = new ModelRepositoryImpl();
		WoofModel woof = new WoofModel();
		repository.retrieve(woof, this.configurationItem);

		// ----------------------------------------
		// Validate the templates
		// ----------------------------------------
		assertList(
				new String[] { "getApplicationPath", "getTemplateLocation", "getTemplateClassName",
						"getRedirectValuesFunction", "getTemplateContentType", "getTemplateCharset",
						"getLinkSeparatorCharacter", "getIsTemplateSecure", "getX", "getY" },
				woof.getWoofTemplates(),
				new WoofTemplateModel("example", "example/TemplateA.ofp", "net.example.ExampleClassA", "redirect",
						"text/plain; charset=UTF-16", "UTF-16", "_", true, null, null, null, null, null, null, null,
						null, null, null, null, 300, 301),
				new WoofTemplateModel("another", "example/TemplateB.ofp", null, null, null, null, null, false, null,
						null, null, null, null, null, null, null, null, null, null, 302, 303));
		WoofTemplateModel template = woof.getWoofTemplates().get(0);
		assertList(new String[] { "getWoofTemplateOutputName", "getArgumentType" }, template.getOutputs(),
				new WoofTemplateOutputModel("OUTPUT_1", "java.lang.Integer"),
				new WoofTemplateOutputModel("OUTPUT_2", null), new WoofTemplateOutputModel("OUTPUT_3", null),
				new WoofTemplateOutputModel("OUTPUT_4", null), new WoofTemplateOutputModel("OUTPUT_5", null));
		WoofTemplateOutputModel output1 = template.getOutputs().get(0);
		assertProperties(new WoofTemplateOutputToWoofSectionInputModel("SECTION_A", "INPUT_A"),
				output1.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofTemplateOutputModel output2 = template.getOutputs().get(1);
		assertProperties(new WoofTemplateOutputToWoofTemplateModel("TEMPLATE_B"), output2.getWoofTemplate(),
				"getApplicationPath");
		WoofTemplateOutputModel output3 = template.getOutputs().get(2);
		assertProperties(new WoofTemplateOutputToWoofResourceModel("RESOURCE"), output3.getWoofResource(),
				"getResourcePath");
		WoofTemplateOutputModel output4 = template.getOutputs().get(3);
		assertProperties(new WoofTemplateOutputToWoofSecurityModel("Security"), output4.getWoofSecurity(),
				"getHttpSecurityName");

		// Validate links
		assertList(new String[] { "getWoofTemplateLinkName", "getIsLinkSecure" }, template.getLinks(),
				new WoofTemplateLinkModel("LINK_1", true), new WoofTemplateLinkModel("LINK_2", false));

		// Validate redirect methods
		assertList(new String[] { "toString" }, template.getRenderHttpMethods(), "RENDER_POST", "RENDER_PUT");

		// Validate Template extensions
		assertList(new String[] { "getExtensionClassName" }, template.getExtensions(),
				new WoofTemplateExtensionModel("GWT"), new WoofTemplateExtensionModel("net.example.Extension"));
		WoofTemplateExtensionModel extension = template.getExtensions().get(0);
		assertList(new String[] { "getName", "getValue" }, extension.getProperties(),
				new PropertyModel("NAME.1", "VALUE.1"), new PropertyModel("NAME.2", "VALUE.2"));

		// ----------------------------------------
		// Validate the sections
		// ----------------------------------------
		assertList(
				new String[] { "getWoofSectionName", "getSectionSourceClassName", "getSectionLocation", "getX",
						"getY" },
				woof.getWoofSections(),
				new WoofSectionModel("SECTION_A", "DESK", "DESK_LOCATION", null, null, null, 400, 401),
				new WoofSectionModel("SECTION_B", "net.example.ExampleSectionSource", "EXAMPLE_LOCATION", null, null,
						null, 402, 403));
		WoofSectionModel section = woof.getWoofSections().get(0);
		assertList(new String[] { "getName", "getValue" }, section.getProperties(),
				new PropertyModel("name.one", "value.one"), new PropertyModel("name.two", "value.two"));
		assertList(new String[] { "getWoofSectionInputName", "getParameterType" }, section.getInputs(),
				new WoofSectionInputModel("INPUT_A", "java.lang.Integer"), new WoofSectionInputModel("INPUT_B", null));
		assertList(new String[] { "getWoofSectionOutputName", "getArgumentType" }, section.getOutputs(),
				new WoofSectionOutputModel("OUTPUT_A", "java.lang.String"),
				new WoofSectionOutputModel("OUTPUT_B", null), new WoofSectionOutputModel("OUTPUT_C", null),
				new WoofSectionOutputModel("OUTPUT_D", null), new WoofSectionOutputModel("OUTPUT_E", null));
		WoofSectionOutputModel section1 = section.getOutputs().get(0);
		assertProperties(new WoofSectionOutputToWoofSectionInputModel("SECTION_B", "INPUT_1"),
				section1.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofSectionOutputModel section2 = section.getOutputs().get(1);
		assertProperties(new WoofSectionOutputToWoofTemplateModel("TEMPLATE_A"), section2.getWoofTemplate(),
				"getApplicationPath");
		WoofSectionOutputModel section3 = section.getOutputs().get(2);
		assertProperties(new WoofSectionOutputToWoofResourceModel("RESOURCE"), section3.getWoofResource(),
				"getResourcePath");
		WoofSectionOutputModel section4 = section.getOutputs().get(3);
		assertProperties(new WoofSectionOutputToWoofSecurityModel("Security"), section4.getWoofSecurity(),
				"getHttpSecurityName");

		// ----------------------------------------
		// Validate the access
		// ----------------------------------------
		assertList(
				new String[] { "getHttpSecurityName", "getHttpSecuritySourceClassName", "getTimeout", "getX", "getY" },
				woof.getWoofSecurities(), new WoofSecurityModel("SECURITY", "net.example.HttpSecuritySource", 2000,
						null, null, null, null, null, null, null, null, 500, 501));
		WoofSecurityModel security = woof.getWoofSecurities().get(0);
		assertList(new String[] { "getName", "getValue" }, security.getProperties(),
				new PropertyModel("name.first", "value.first"), new PropertyModel("name.second", "value.second"));
		assertList(new String[] { "getWoofAccessOutputName", "getArgumentType" }, security.getOutputs(),
				new WoofSecurityOutputModel("OUTPUT_ONE", "java.lang.String"),
				new WoofSecurityOutputModel("OUTPUT_TWO", null), new WoofSecurityOutputModel("OUTPUT_THREE", null),
				new WoofSecurityOutputModel("OUTPUT_FOUR", null));
		WoofSecurityOutputModel securityOne = security.getOutputs().get(0);
		assertProperties(new WoofSecurityOutputToWoofSectionInputModel("SECTION_B", "INPUT_1"),
				securityOne.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofSecurityOutputModel securityTwo = security.getOutputs().get(1);
		assertProperties(new WoofSecurityOutputToWoofTemplateModel("TEMPLATE_A"), securityTwo.getWoofTemplate(),
				"getTemplateName");
		WoofSecurityOutputModel securityThree = security.getOutputs().get(2);
		assertProperties(new WoofSecurityOutputToWoofResourceModel("RESOURCE"), securityThree.getWoofResource(),
				"getResourcePath");

		// ----------------------------------------
		// Validate the governances
		// ----------------------------------------
		assertList(new String[] { "getWoofGovernanceName", "getGovernanceSourceClassName", "getX", "getY" },
				woof.getWoofGovernances(), new WoofGovernanceModel("GOVERNANCE", "net.example.ExampleGovernanceSource",
						null, null, null, 600, 601));
		WoofGovernanceModel governance = woof.getWoofGovernances().get(0);
		assertList(new String[] { "getName", "getValue" }, governance.getProperties(),
				new PropertyModel("name.a", "value.a"), new PropertyModel("name.b", "value.b"));
		assertList(new String[] { "getX", "getY", "getWidth", "getHeight" }, governance.getGovernanceAreas(),
				new WoofGovernanceAreaModel(620, 621, null, 610, 611),
				new WoofGovernanceAreaModel(640, 641, null, 630, 631));

		// ----------------------------------------
		// Validate the resources
		// ----------------------------------------
		assertList(new String[] { "getResourcePath", "getX", "getY" }, woof.getWoofResources(),
				new WoofResourceModel("Example.html", null, null, null, null, null, 700, 701));

		// ----------------------------------------
		// Validate the exceptions
		// ----------------------------------------
		assertList(new String[] { "getClassName", "getX", "getY" }, woof.getWoofExceptions(),
				new WoofExceptionModel("java.lang.Exception", null, null, null, null, null, 800, 801),
				new WoofExceptionModel("java.lang.RuntimeException", null, null, null, null, null, 802, 803),
				new WoofExceptionModel("java.sql.SQLException", null, null, null, null, null, 804, 805),
				new WoofExceptionModel("java.io.IOException", null, null, null, null, null, 806, 807));
		WoofExceptionModel exception1 = woof.getWoofExceptions().get(0);
		assertProperties(new WoofExceptionToWoofSectionInputModel("SECTION_A", "INPUT_A"),
				exception1.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofExceptionModel exception2 = woof.getWoofExceptions().get(1);
		assertProperties(new WoofExceptionToWoofTemplateModel("TEMPLATE_A"), exception2.getWoofTemplate(),
				"getTemplateName");
		WoofExceptionModel exception3 = woof.getWoofExceptions().get(2);
		assertProperties(new WoofExceptionToWoofResourceModel("RESOURCE"), exception3.getWoofResource(),
				"getResourceName");

		// ----------------------------------------
		// Validate the starts
		// ----------------------------------------
		assertList(new String[] { "getX", "getY" }, woof.getWoofStarts(), new WoofStartModel(null, 900, 901));
		WoofStartModel start = woof.getWoofStarts().get(0);
		assertProperties(new WoofStartToWoofSectionInputModel("SECTION_A", "INPUT_A"), start.getWoofSectionInput(),
				"getSectionName", "getInputName");
	}

	/**
	 * Ensure able to round trip storing and retrieving the {@link WoofModel}.
	 */
	public void testRoundTripStoreRetrieveWoof() throws Exception {

		// Load the WoOF
		ModelRepository repository = new ModelRepositoryImpl();
		WoofModel woof = new WoofModel();
		repository.retrieve(woof, this.configurationItem);

		// Store the WoOF
		WritableConfigurationItem contents = MemoryConfigurationContext.createWritableConfigurationItem("test");
		repository.store(woof, contents);

		// Reload the WoOF
		WoofModel reloadedWoOF = new WoofModel();
		repository.retrieve(reloadedWoOF, contents);

		// Validate round trip
		assertGraph(woof, reloadedWoOF, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}