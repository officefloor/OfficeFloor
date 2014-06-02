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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
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
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "WebOnOfficeFloor.woof.xml"), null);
	}

	/**
	 * Ensure retrieve the {@link WoofModel}.
	 */
	public void testRetrieveWoof() throws Exception {

		// Load the WoOF
		ModelRepository repository = new ModelRepositoryImpl();
		WoofModel woof = new WoofModel();
		woof = repository.retrieve(woof, this.configurationItem);

		// ----------------------------------------
		// Validate the templates
		// ----------------------------------------
		assertList(
				new String[] { "getWoofTemplateName", "getUri",
						"getTemplatePath", "getSuperTemplate",
						"getTemplateClassName", "getIsTemplateSecure",
						"getIsContinueRendering", "getX", "getY" },
				woof.getWoofTemplates(), new WoofTemplateModel("TEMPLATE_A",
						"example", "example/TemplateA.ofp", "TEMPLATE_B",
						"net.example.ExampleClassA", true, true, null, null,
						null, null, null, null, null, null, 300, 301),
				new WoofTemplateModel("TEMPLATE_B", "another",
						"example/TemplateB.ofp", null, null, false, false,
						null, null, null, null, null, null, null, null, 302,
						303));
		WoofTemplateModel template = woof.getWoofTemplates().get(0);
		assertList(new String[] { "getWoofTemplateOutputName",
				"getArgumentType" }, template.getOutputs(),
				new WoofTemplateOutputModel("OUTPUT_1", "java.lang.Integer"),
				new WoofTemplateOutputModel("OUTPUT_2", null),
				new WoofTemplateOutputModel("OUTPUT_3", null),
				new WoofTemplateOutputModel("OUTPUT_4", null),
				new WoofTemplateOutputModel("OUTPUT_5", null));
		WoofTemplateOutputModel output1 = template.getOutputs().get(0);
		assertProperties(new WoofTemplateOutputToWoofSectionInputModel(
				"SECTION_A", "INPUT_A"), output1.getWoofSectionInput(),
				"getSectionName", "getInputName");
		WoofTemplateOutputModel output2 = template.getOutputs().get(1);
		assertProperties(
				new WoofTemplateOutputToWoofTemplateModel("TEMPLATE_B"),
				output2.getWoofTemplate(), "getTemplateName");
		WoofTemplateOutputModel output3 = template.getOutputs().get(2);
		assertProperties(new WoofTemplateOutputToWoofResourceModel("RESOURCE"),
				output3.getWoofResource(), "getResourceName");
		WoofTemplateOutputModel output4 = template.getOutputs().get(3);
		assertProperties(new WoofTemplateOutputToWoofAccessInputModel(
				"Authenticate"), output4.getWoofAccessInput(), "getInputName");

		// Validate links
		assertList(
				new String[] { "getWoofTemplateLinkName", "getIsLinkSecure" },
				template.getLinks(), new WoofTemplateLinkModel("LINK_1", true),
				new WoofTemplateLinkModel("LINK_2", false));

		// Validate redirect methods
		assertList(new String[] { "getWoofTemplateRedirectHttpMethod" },
				template.getRedirects(), new WoofTemplateRedirectModel(
						"REDIRECT_POST"), new WoofTemplateRedirectModel(
						"REDIRECT_PUT"));

		// Validate Template extensions
		assertList(new String[] { "getExtensionClassName" },
				template.getExtensions(),
				new WoofTemplateExtensionModel("GWT"),
				new WoofTemplateExtensionModel("net.example.Extension"));
		WoofTemplateExtensionModel extension = template.getExtensions().get(0);
		assertList(new String[] { "getName", "getValue" },
				extension.getProperties(), new PropertyModel("NAME.1",
						"VALUE.1"), new PropertyModel("NAME.2", "VALUE.2"));

		// ----------------------------------------
		// Validate the sections
		// ----------------------------------------
		assertList(new String[] { "getWoofSectionName",
				"getSectionSourceClassName", "getSectionLocation", "getX",
				"getY" }, woof.getWoofSections(), new WoofSectionModel(
				"SECTION_A", "DESK", "DESK_LOCATION", null, null, null, 400,
				401), new WoofSectionModel("SECTION_B",
				"net.example.ExampleSectionSource", "EXAMPLE_LOCATION", null,
				null, null, 402, 403));
		WoofSectionModel section = woof.getWoofSections().get(0);
		assertList(new String[] { "getName", "getValue" },
				section.getProperties(), new PropertyModel("name.one",
						"value.one"),
				new PropertyModel("name.two", "value.two"));
		assertList(
				new String[] { "getWoofSectionInputName", "getParameterType",
						"getUri" },
				section.getInputs(),
				new WoofSectionInputModel("INPUT_A", "java.lang.Integer", null),
				new WoofSectionInputModel("INPUT_B", null, "example"));
		assertList(
				new String[] { "getWoofSectionOutputName", "getArgumentType" },
				section.getOutputs(), new WoofSectionOutputModel("OUTPUT_A",
						"java.lang.String"), new WoofSectionOutputModel(
						"OUTPUT_B", null), new WoofSectionOutputModel(
						"OUTPUT_C", null), new WoofSectionOutputModel(
						"OUTPUT_D", null), new WoofSectionOutputModel(
						"OUTPUT_E", null));
		WoofSectionOutputModel section1 = section.getOutputs().get(0);
		assertProperties(new WoofSectionOutputToWoofSectionInputModel(
				"SECTION_B", "INPUT_1"), section1.getWoofSectionInput(),
				"getSectionName", "getInputName");
		WoofSectionOutputModel section2 = section.getOutputs().get(1);
		assertProperties(
				new WoofSectionOutputToWoofTemplateModel("TEMPLATE_A"),
				section2.getWoofTemplate(), "getTemplateName");
		WoofSectionOutputModel section3 = section.getOutputs().get(2);
		assertProperties(new WoofSectionOutputToWoofResourceModel("RESOURCE"),
				section3.getWoofResource(), "getResourceName");
		WoofSectionOutputModel section4 = section.getOutputs().get(3);
		assertProperties(new WoofSectionOutputToWoofAccessInputModel(
				"Authenticate"), section4.getWoofAccessInput(), "getInputName");

		// ----------------------------------------
		// Validate the access
		// ----------------------------------------
		WoofAccessModel access = woof.getWoofAccess();
		assertProperties(new WoofAccessModel("net.example.HttpSecuritySource",
				2000, null, null, null, 500, 501), access,
				"getHttpSecuritySourceClassName", "getTimeout", "getX", "getY");
		assertList(new String[] { "getName", "getValue" },
				access.getProperties(), new PropertyModel("name.first",
						"value.first"), new PropertyModel("name.second",
						"value.second"));
		assertList(
				new String[] { "getWoofAccessInputName", "getParameterType" },
				access.getInputs(), new WoofAccessInputModel("Authenticate",
						"net.example.HttpCredentials"));
		assertList(
				new String[] { "getWoofAccessOutputName", "getArgumentType" },
				access.getOutputs(), new WoofAccessOutputModel("OUTPUT_ONE",
						"java.lang.String"), new WoofAccessOutputModel(
						"OUTPUT_TWO", null), new WoofAccessOutputModel(
						"OUTPUT_THREE", null), new WoofAccessOutputModel(
						"OUTPUT_FOUR", null));
		WoofAccessOutputModel accessOne = access.getOutputs().get(0);
		assertProperties(new WoofAccessOutputToWoofSectionInputModel(
				"SECTION_B", "INPUT_1"), accessOne.getWoofSectionInput(),
				"getSectionName", "getInputName");
		WoofAccessOutputModel accessTwo = access.getOutputs().get(1);
		assertProperties(new WoofAccessOutputToWoofTemplateModel("TEMPLATE_A"),
				accessTwo.getWoofTemplate(), "getTemplateName");
		WoofAccessOutputModel accessThree = access.getOutputs().get(2);
		assertProperties(new WoofAccessOutputToWoofResourceModel("RESOURCE"),
				accessThree.getWoofResource(), "getResourceName");

		// ----------------------------------------
		// Validate the governances
		// ----------------------------------------
		assertList(new String[] { "getWoofGovernanceName",
				"getGovernanceSourceClassName", "getX", "getY" },
				woof.getWoofGovernances(), new WoofGovernanceModel(
						"GOVERNANCE", "net.example.ExampleGovernanceSource",
						null, null, null, 600, 601));
		WoofGovernanceModel governance = woof.getWoofGovernances().get(0);
		assertList(new String[] { "getName", "getValue" },
				governance.getProperties(), new PropertyModel("name.a",
						"value.a"), new PropertyModel("name.b", "value.b"));
		assertList(new String[] { "getX", "getY", "getWidth", "getHeight" },
				governance.getGovernanceAreas(), new WoofGovernanceAreaModel(
						620, 621, null, 610, 611), new WoofGovernanceAreaModel(
						640, 641, null, 630, 631));

		// ----------------------------------------
		// Validate the resources
		// ----------------------------------------
		assertList(new String[] { "getWoofResourceName", "getResourcePath",
				"getX", "getY" }, woof.getWoofResources(),
				new WoofResourceModel("RESOURCE", "Example.html", null, null,
						null, null, 700, 701));

		// ----------------------------------------
		// Validate the exceptions
		// ----------------------------------------
		assertList(new String[] { "getClassName", "getX", "getY" },
				woof.getWoofExceptions(), new WoofExceptionModel(
						"java.lang.Exception", null, null, null, 800, 801),
				new WoofExceptionModel("java.lang.RuntimeException", null,
						null, null, 802, 803), new WoofExceptionModel(
						"java.sql.SQLException", null, null, null, 804, 805),
				new WoofExceptionModel("java.io.IOException", null, null, null,
						806, 807));
		WoofExceptionModel exception1 = woof.getWoofExceptions().get(0);
		assertProperties(new WoofExceptionToWoofSectionInputModel("SECTION_A",
				"INPUT_A"), exception1.getWoofSectionInput(), "getSectionName",
				"getInputName");
		WoofExceptionModel exception2 = woof.getWoofExceptions().get(1);
		assertProperties(new WoofExceptionToWoofTemplateModel("TEMPLATE_A"),
				exception2.getWoofTemplate(), "getTemplateName");
		WoofExceptionModel exception3 = woof.getWoofExceptions().get(2);
		assertProperties(new WoofExceptionToWoofResourceModel("RESOURCE"),
				exception3.getWoofResource(), "getResourceName");

		// ----------------------------------------
		// Validate the starts
		// ----------------------------------------
		assertList(new String[] { "getX", "getY" }, woof.getWoofStarts(),
				new WoofStartModel(null, 900, 901));
		WoofStartModel start = woof.getWoofStarts().get(0);
		assertProperties(new WoofStartToWoofSectionInputModel("SECTION_A",
				"INPUT_A"), start.getWoofSectionInput(), "getSectionName",
				"getInputName");
	}

	/**
	 * Ensure able to round trip storing and retrieving the {@link WoofModel}.
	 */
	public void testRoundTripStoreRetrieveWoof() throws Exception {

		// Load the WoOF
		ModelRepository repository = new ModelRepositoryImpl();
		WoofModel woof = new WoofModel();
		woof = repository.retrieve(woof, this.configurationItem);

		// Store the WoOF
		MemoryConfigurationItem contents = new MemoryConfigurationItem();
		repository.store(woof, contents);

		// Reload the WoOF
		WoofModel reloadedWoOF = new WoofModel();
		reloadedWoOF = repository.retrieve(reloadedWoOF, contents);

		// Validate round trip
		assertGraph(woof, reloadedWoOF,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}