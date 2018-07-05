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

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.filesystem.FileSystemConfigurationContext;
import net.officefloor.configuration.impl.memory.MemoryConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.woof.model.woof.PropertyModel;
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofStartToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateExtensionModel;
import net.officefloor.woof.model.woof.WoofTemplateLinkModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofTemplateModel;

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
		// Validate the HTTP continuations
		// ----------------------------------------
		assertList(new String[] { "getApplicationPath", "getIsSecure", "getX", "getY" },
				woof.getWoofHttpContinuations(), new WoofHttpContinuationModel(true, "/pathA", 100, 101),
				new WoofHttpContinuationModel(false, "/pathB", 110, 111));
		WoofHttpContinuationModel continuation = woof.getWoofHttpContinuations().get(0);
		assertProperties(new WoofHttpContinuationToWoofSectionInputModel("SECTION_A", "INPUT_A"),
				continuation.getWoofSectionInput(), "getSectionName", "getInputName");
		assertProperties(new WoofHttpContinuationToWoofTemplateModel("/templateB"), continuation.getWoofTemplate(),
				"getApplicationPath");
		assertProperties(new WoofHttpContinuationToWoofSecurityModel("SECURITY_A"), continuation.getWoofSecurity(),
				"getHttpSecurityName");
		assertProperties(new WoofHttpContinuationToWoofResourceModel("/resourceA.html"), continuation.getWoofResource(),
				"getResourcePath");
		assertProperties(new WoofHttpContinuationToWoofHttpContinuationModel("/pathB"), continuation.getWoofRedirect(),
				"getApplicationPath");

		// ----------------------------------------
		// Validate the HTTP inputs
		// ----------------------------------------
		assertList(new String[] { "getApplicationPath", "getIsSecure", "getHttpMethod", "getX", "getY" },
				woof.getWoofHttpInputs(), new WoofHttpInputModel(true, "POST", "/pathC", 200, 201),
				new WoofHttpInputModel(false, "PUT", "/pathD", 210, 211));
		WoofHttpInputModel httpInput = woof.getWoofHttpInputs().get(0);
		assertProperties(new WoofHttpInputToWoofSectionInputModel("SECTION_B", "INPUT_0"),
				httpInput.getWoofSectionInput(), "getSectionName", "getInputName");
		assertProperties(new WoofHttpInputToWoofTemplateModel("/templateA"), httpInput.getWoofTemplate(),
				"getApplicationPath");
		assertProperties(new WoofHttpInputToWoofSecurityModel("SECURITY_B"), httpInput.getWoofSecurity(),
				"getHttpSecurityName");
		assertProperties(new WoofHttpInputToWoofResourceModel("/resourceB.png"), httpInput.getWoofResource(),
				"getResourcePath");
		assertProperties(new WoofHttpInputToWoofHttpContinuationModel("/pathA"), httpInput.getWoofHttpContinuation(),
				"getApplicationPath");

		// ----------------------------------------
		// Validate the templates
		// ----------------------------------------
		assertList(
				new String[] { "getApplicationPath", "getTemplateLocation", "getTemplateClassName",
						"getTemplateContentType", "getTemplateCharset", "getIsTemplateSecure",
						"getRedirectValuesFunction", "getLinkSeparatorCharacter", "getX", "getY" },
				woof.getWoofTemplates(),
				new WoofTemplateModel("/templateA", "example/TemplateA.ofp", "net.example.ExampleClassA", "redirect",
						"text/plain; charset=UTF-16", "UTF-16", "_", true, 300, 301),
				new WoofTemplateModel("/templateB", "example/TemplateB.ofp", null, null, null, null, null, false, 302,
						303));
		WoofTemplateModel template = woof.getWoofTemplates().get(0);
		assertProperties(new WoofTemplateToSuperWoofTemplateModel("/templateB"), template.getSuperWoofTemplate(),
				"getSuperWoofTemplateApplicationPath");
		assertList(new String[] { "getWoofTemplateOutputName", "getArgumentType" }, template.getOutputs(),
				new WoofTemplateOutputModel("OUTPUT_0", "java.lang.Integer"),
				new WoofTemplateOutputModel("OUTPUT_1", null), new WoofTemplateOutputModel("OUTPUT_2", null),
				new WoofTemplateOutputModel("OUTPUT_3", null), new WoofTemplateOutputModel("OUTPUT_4", null),
				new WoofTemplateOutputModel("OUTPUT_5", null));
		WoofTemplateOutputModel templateOutputSectionInput = template.getOutputs().get(0);
		assertProperties(new WoofTemplateOutputToWoofSectionInputModel("SECTION_A", "INPUT_A"),
				templateOutputSectionInput.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofTemplateOutputModel templateOutputTemplate = template.getOutputs().get(1);
		assertProperties(new WoofTemplateOutputToWoofTemplateModel("/templateB"),
				templateOutputTemplate.getWoofTemplate(), "getApplicationPath");
		WoofTemplateOutputModel templateOutputResource = template.getOutputs().get(2);
		assertProperties(new WoofTemplateOutputToWoofResourceModel("/resourceA.html"),
				templateOutputResource.getWoofResource(), "getResourcePath");
		WoofTemplateOutputModel templateOutputSecurity = template.getOutputs().get(3);
		assertProperties(new WoofTemplateOutputToWoofSecurityModel("SECURITY_B"),
				templateOutputSecurity.getWoofSecurity(), "getHttpSecurityName");
		WoofTemplateOutputModel templateOutputApplicationPath = template.getOutputs().get(4);
		assertProperties(new WoofTemplateOutputToWoofHttpContinuationModel("/pathA"),
				templateOutputApplicationPath.getWoofHttpContinuation(), "getApplicationPath");

		// Validate links
		assertList(new String[] { "getWoofTemplateLinkName", "getIsLinkSecure" }, template.getLinks(),
				new WoofTemplateLinkModel("LINK_0", true), new WoofTemplateLinkModel("LINK_1", false));

		// Validate redirect methods
		assertList(new String[] { "getWoofTemplateRenderHttpMethodName" }, template.getRenderHttpMethods(),
				new WoofTemplateRenderHttpMethodModel("POST"), new WoofTemplateRenderHttpMethodModel("PUT"));

		// Validate Template extensions
		assertList(new String[] { "getExtensionClassName" }, template.getExtensions(),
				new WoofTemplateExtensionModel("EXTENSION"), new WoofTemplateExtensionModel("net.example.Extension"));
		WoofTemplateExtensionModel extension = template.getExtensions().get(0);
		assertList(new String[] { "getName", "getValue" }, extension.getProperties(),
				new PropertyModel("NAME.0", "VALUE.0"), new PropertyModel("NAME.1", "VALUE.1"));

		// ----------------------------------------
		// Validate the sections
		// ----------------------------------------
		assertList(
				new String[] { "getWoofSectionName", "getSectionSourceClassName", "getSectionLocation", "getX",
						"getY" },
				woof.getWoofSections(),
				new WoofSectionModel("SECTION_A", "SECTION", "SECTION_LOCATION", null, null, null, 400, 401),
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
				new WoofSectionOutputModel("OUTPUT_D", null), new WoofSectionOutputModel("OUTPUT_E", null),
				new WoofSectionOutputModel("OUTPUT_F", null));
		WoofSectionOutputModel sectionOutputSectionInput = section.getOutputs().get(0);
		assertProperties(new WoofSectionOutputToWoofSectionInputModel("SECTION_B", "INPUT_0"),
				sectionOutputSectionInput.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofSectionOutputModel sectionOutputTemplate = section.getOutputs().get(1);
		assertProperties(new WoofSectionOutputToWoofTemplateModel("/templateA"),
				sectionOutputTemplate.getWoofTemplate(), "getApplicationPath");
		WoofSectionOutputModel sectionOutputResource = section.getOutputs().get(2);
		assertProperties(new WoofSectionOutputToWoofResourceModel("/resourceB.png"),
				sectionOutputResource.getWoofResource(), "getResourcePath");
		WoofSectionOutputModel sectionOutputSecurity = section.getOutputs().get(3);
		assertProperties(new WoofSectionOutputToWoofSecurityModel("SECURITY_A"),
				sectionOutputSecurity.getWoofSecurity(), "getHttpSecurityName");
		WoofSectionOutputModel sectionOutputApplicationPath = section.getOutputs().get(4);
		assertProperties(new WoofSectionOutputToWoofHttpContinuationModel("/pathB"),
				sectionOutputApplicationPath.getWoofHttpContinuation(), "getApplicationPath");

		// ----------------------------------------
		// Validate the security
		// ----------------------------------------
		assertList(
				new String[] { "getHttpSecurityName", "getHttpSecuritySourceClassName", "getTimeout", "getX", "getY" },
				woof.getWoofSecurities(),
				new WoofSecurityModel("SECURITY_A", "net.example.HttpSecuritySource", 2000, null, null, null, null,
						null, null, null, null, null, 500, 501),
				new WoofSecurityModel("SECURITY_B", "net.example.AnotherHttpSecuritySource", 0, null, null, null, null,
						null, null, null, null, null, 510, 511));
		WoofSecurityModel security = woof.getWoofSecurities().get(0);
		assertList(new String[] { "getName", "getValue" }, security.getProperties(),
				new PropertyModel("name.first", "value.first"), new PropertyModel("name.second", "value.second"));
		assertList(new String[] { "getContentType" }, security.getContentTypes(),
				new WoofSecurityContentTypeModel("application/json"),
				new WoofSecurityContentTypeModel("application/xml"));
		assertList(new String[] { "getWoofSecurityOutputName", "getArgumentType" }, security.getOutputs(),
				new WoofSecurityOutputModel("OUTPUT_ZERO", "java.lang.String"),
				new WoofSecurityOutputModel("OUTPUT_ONE", null), new WoofSecurityOutputModel("OUTPUT_TWO", null),
				new WoofSecurityOutputModel("OUTPUT_THREE", null), new WoofSecurityOutputModel("OUTPUT_FOUR", null),
				new WoofSecurityOutputModel("OUTPUT_FIVE", null));
		WoofSecurityOutputModel securityOutputSectionInput = security.getOutputs().get(0);
		assertProperties(new WoofSecurityOutputToWoofSectionInputModel("SECTION_B", "INPUT_1"),
				securityOutputSectionInput.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofSecurityOutputModel securityOutputTemplate = security.getOutputs().get(1);
		assertProperties(new WoofSecurityOutputToWoofTemplateModel("/templateA"),
				securityOutputTemplate.getWoofTemplate(), "getApplicationPath");
		WoofSecurityOutputModel securityOutputResource = security.getOutputs().get(2);
		assertProperties(new WoofSecurityOutputToWoofResourceModel("/resourceB.png"),
				securityOutputResource.getWoofResource(), "getResourcePath");
		WoofSecurityOutputModel securityOutputSecurity = security.getOutputs().get(3);
		assertProperties(new WoofSecurityOutputToWoofSecurityModel("SECURITY_B"),
				securityOutputSecurity.getWoofSecurity(), "getHttpSecurityName");
		WoofSecurityOutputModel securityOutputApplicationPath = security.getOutputs().get(4);
		assertProperties(new WoofSecurityOutputToWoofHttpContinuationModel("/pathA"),
				securityOutputApplicationPath.getWoofHttpContinuation(), "getApplicationPath");

		// ----------------------------------------
		// Validate the governances
		// ----------------------------------------
		assertList(new String[] { "getWoofGovernanceName", "getGovernanceSourceClassName", "getX", "getY" },
				woof.getWoofGovernances(),
				new WoofGovernanceModel("GOVERNANCE_A", "net.example.ExampleGovernanceSource", null, null, null, 600,
						601),
				new WoofGovernanceModel("GOVERNANCE_B", "net.example.AnotherGovernanceSource", null, null, null, 610,
						611));
		WoofGovernanceModel governance = woof.getWoofGovernances().get(0);
		assertList(new String[] { "getName", "getValue" }, governance.getProperties(),
				new PropertyModel("name.a", "value.a"), new PropertyModel("name.b", "value.b"));
		assertList(new String[] { "getX", "getY", "getWidth", "getHeight" }, governance.getGovernanceAreas(),
				new WoofGovernanceAreaModel(640, 641, null, 620, 621),
				new WoofGovernanceAreaModel(650, 651, null, 630, 631));

		// ----------------------------------------
		// Validate the resources
		// ----------------------------------------
		assertList(new String[] { "getResourcePath", "getX", "getY" }, woof.getWoofResources(),
				new WoofResourceModel("/resourceA.html", null, null, null, null, null, null, 700, 701),
				new WoofResourceModel("/resourceB.png", null, null, null, null, null, null, 710, 711));

		// ----------------------------------------
		// Validate the exceptions
		// ----------------------------------------
		assertList(new String[] { "getClassName", "getX", "getY" }, woof.getWoofExceptions(),
				new WoofExceptionModel("java.lang.Exception", null, null, null, null, null, 800, 801),
				new WoofExceptionModel("java.lang.RuntimeException", null, null, null, null, null, 802, 803),
				new WoofExceptionModel("java.sql.SQLException", null, null, null, null, null, 804, 805),
				new WoofExceptionModel("net.example.AuthException", null, null, null, null, null, 806, 807),
				new WoofExceptionModel("java.lang.NullPointerException", null, null, null, null, null, 808, 809),
				new WoofExceptionModel("java.io.IOException", null, null, null, null, null, 810, 811));
		WoofExceptionModel exceptionSectionInput = woof.getWoofExceptions().get(0);
		assertProperties(new WoofExceptionToWoofSectionInputModel("SECTION_A", "INPUT_A"),
				exceptionSectionInput.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofExceptionModel exceptionTemplate = woof.getWoofExceptions().get(1);
		assertProperties(new WoofExceptionToWoofTemplateModel("/templateA"), exceptionTemplate.getWoofTemplate(),
				"getApplicationPath");
		WoofExceptionModel exceptionResource = woof.getWoofExceptions().get(2);
		assertProperties(new WoofExceptionToWoofResourceModel("/resourceB.png"), exceptionResource.getWoofResource(),
				"getResourcePath");
		WoofExceptionModel exceptionSecurity = woof.getWoofExceptions().get(3);
		assertProperties(new WoofExceptionToWoofSecurityModel("SECURITY_B"), exceptionSecurity.getWoofSecurity(),
				"getHttpSecurityName");
		WoofExceptionModel exceptionApplicationPath = woof.getWoofExceptions().get(4);
		assertProperties(new WoofExceptionToWoofHttpContinuationModel("/pathB"),
				exceptionApplicationPath.getWoofHttpContinuation(), "getApplicationPath");

		// ----------------------------------------
		// Validate the starts
		// ----------------------------------------
		assertList(new String[] { "getX", "getY" }, woof.getWoofStarts(), new WoofStartModel(null, 900, 901),
				new WoofStartModel(null, 910, 911));
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