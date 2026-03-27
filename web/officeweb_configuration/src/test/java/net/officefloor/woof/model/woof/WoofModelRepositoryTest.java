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

import java.util.List;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.FileSystemConfigurationContext;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
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
		// Validate the HTTP continuations
		// ----------------------------------------
		assertList(new String[] { "getApplicationPath", "getIsSecure", "getX", "getY" },
				woof.getWoofHttpContinuations(), new WoofHttpContinuationModel(true, "/pathA", 100, 101),
				new WoofHttpContinuationModel(false, "/pathB", 110, 111));
		WoofHttpContinuationModel continuation = woof.getWoofHttpContinuations().get(0);
		assertProperties(new DocumentationModel("HTTP CONTINUATION DOCUMENTATION"), continuation.getDocumentation(),
				"getDescription");
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
		assertProperties(new WoofHttpContinuationToWoofProcedureModel("PROCEDURE_A"), continuation.getWoofProcedure(),
				"getProcedureName");

		// ----------------------------------------
		// Validate the HTTP inputs
		// ----------------------------------------
		assertList(new String[] { "getApplicationPath", "getIsSecure", "getHttpMethod", "getX", "getY" },
				woof.getWoofHttpInputs(), new WoofHttpInputModel(true, "POST", "/pathC", 200, 201),
				new WoofHttpInputModel(false, "PUT", "/pathD", 210, 211));
		WoofHttpInputModel httpInput = woof.getWoofHttpInputs().get(0);
		assertProperties(new DocumentationModel("HTTP INPUT DOCUMENTATION"), httpInput.getDocumentation(),
				"getDescription");
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
		assertProperties(new WoofHttpInputToWoofProcedureModel("PROCEDURE_A"), httpInput.getWoofProcedure(),
				"getProcedureName");

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
				new WoofTemplateOutputModel("OUTPUT_5", null), new WoofTemplateOutputModel("OUTPUT_6", null));
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
		WoofTemplateOutputModel templateOutputProcedure = template.getOutputs().get(5);
		assertProperties(new WoofTemplateOutputToWoofProcedureModel("PROCEDURE_A"),
				templateOutputProcedure.getWoofProcedure(), "getProcedureName");

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
		// Validate the procedures
		// ----------------------------------------
		assertList(
				new String[] { "getWoofProcedureName", "getResource", "getSourceName", "getProcedureName", "getX",
						"getY" },
				woof.getWoofProcedures(),
				new WoofProcedureModel("PROCEDURE_A", "net.example.ExampleProcedure", "Class", "procedure", 400, 401),
				new WoofProcedureModel("PROCEDURE_B", "net.example.KotlinProcedure", "Kotlin", "method", 402, 403),
				new WoofProcedureModel("PROCEDURE_C", "net.example.ScalaProcedure", "Scala", "function", 404, 405),
				new WoofProcedureModel("PROCEDURE_D", "net.example.JavaScriptProcedure", "JavaScript", "function", 406,
						407),
				new WoofProcedureModel("PROCEDURE_E", "net.example.GroovyProcedure", "Groovy", "method", 408, 409),
				new WoofProcedureModel("PROCEDURE_F", "net.example.PythonProcedure", "Python", "func", 410, 411),
				new WoofProcedureModel("PROCEDURE_G", "net.example.CustomProcedure", "Custom", "procedure", 412, 413));
		List<WoofProcedureModel> procedures = woof.getWoofProcedures();
		WoofProcedureModel procedure = procedures.get(0);
		assertList(new String[] { "getName", "getValue" }, procedure.getProperties(),
				new PropertyModel("name.ONE", "value.ONE"), new PropertyModel("name.TWO", "value.TWO"));

		// Verify next
		assertProperties(new WoofProcedureNextModel(Byte.class.getName()), procedure.getNext(), "getArgumentType");
		assertProperties(new WoofProcedureNextModel(null), procedures.get(1).getNext(), "getArgumentType");
		WoofProcedureNextModel procedureNextSectionInput = procedures.get(0).getNext();
		assertProperties(new WoofProcedureNextToWoofSectionInputModel("SECTION_A", "INPUT_A"),
				procedureNextSectionInput.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofProcedureNextModel procedureNextTemplate = procedures.get(1).getNext();
		assertProperties(new WoofProcedureNextToWoofTemplateModel("/templateA"),
				procedureNextTemplate.getWoofTemplate(), "getApplicationPath");
		WoofProcedureNextModel procedureNextResource = procedures.get(2).getNext();
		assertProperties(new WoofProcedureNextToWoofResourceModel("/resourceB.png"),
				procedureNextResource.getWoofResource(), "getResourcePath");
		WoofProcedureNextModel procedureNextSecurity = procedures.get(3).getNext();
		assertProperties(new WoofProcedureNextToWoofSecurityModel("SECURITY_A"),
				procedureNextSecurity.getWoofSecurity(), "getHttpSecurityName");
		WoofProcedureNextModel procedureNextApplicationPath = procedures.get(4).getNext();
		assertProperties(new WoofProcedureNextToWoofHttpContinuationModel("/pathB"),
				procedureNextApplicationPath.getWoofHttpContinuation(), "getApplicationPath");
		WoofProcedureNextModel procedureNextProcedure = procedures.get(5).getNext();
		assertProperties(new WoofProcedureNextToWoofProcedureModel("PROCEDURE_B"),
				procedureNextProcedure.getWoofProcedure(), "getProcedureName");

		// Verify outputs
		assertList(new String[] { "getWoofProcedureOutputName", "getArgumentType" }, procedure.getOutputs(),
				new WoofProcedureOutputModel("OUTPUT_A", String.class.getName()),
				new WoofProcedureOutputModel("OUTPUT_B", null), new WoofProcedureOutputModel("OUTPUT_C", null),
				new WoofProcedureOutputModel("OUTPUT_D", null), new WoofProcedureOutputModel("OUTPUT_E", null),
				new WoofProcedureOutputModel("OUTPUT_F", null), new WoofProcedureOutputModel("OUTPUT_G", null));
		WoofProcedureOutputModel procedureOutputSectionInput = procedure.getOutputs().get(0);
		assertProperties(new WoofProcedureOutputToWoofSectionInputModel("SECTION_B", "INPUT_0"),
				procedureOutputSectionInput.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofProcedureOutputModel procedureOutputTemplate = procedure.getOutputs().get(1);
		assertProperties(new WoofProcedureOutputToWoofTemplateModel("/templateA"),
				procedureOutputTemplate.getWoofTemplate(), "getApplicationPath");
		WoofProcedureOutputModel procedureOutputResource = procedure.getOutputs().get(2);
		assertProperties(new WoofProcedureOutputToWoofResourceModel("/resourceB.png"),
				procedureOutputResource.getWoofResource(), "getResourcePath");
		WoofProcedureOutputModel procedureOutputSecurity = procedure.getOutputs().get(3);
		assertProperties(new WoofProcedureOutputToWoofSecurityModel("SECURITY_A"),
				procedureOutputSecurity.getWoofSecurity(), "getHttpSecurityName");
		WoofProcedureOutputModel procedureOutputApplicationPath = procedure.getOutputs().get(4);
		assertProperties(new WoofProcedureOutputToWoofHttpContinuationModel("/pathB"),
				procedureOutputApplicationPath.getWoofHttpContinuation(), "getApplicationPath");
		WoofProcedureOutputModel procedureOutputProcedure = procedure.getOutputs().get(5);
		assertProperties(new WoofProcedureOutputToWoofProcedureModel("PROCEDURE_B"),
				procedureOutputProcedure.getWoofProcedure(), "getProcedureName");

		// ----------------------------------------
		// Validate the sections
		// ----------------------------------------
		assertList(
				new String[] { "getWoofSectionName", "getSectionSourceClassName", "getSectionLocation", "getX",
						"getY" },
				woof.getWoofSections(), new WoofSectionModel("SECTION_A", "SECTION", "SECTION_LOCATION", 500, 501),
				new WoofSectionModel("SECTION_B", "net.example.ExampleSectionSource", "EXAMPLE_LOCATION", 502, 503));
		WoofSectionModel section = woof.getWoofSections().get(0);
		assertList(new String[] { "getName", "getValue" }, section.getProperties(),
				new PropertyModel("name.one", "value.one"), new PropertyModel("name.two", "value.two"));
		assertList(new String[] { "getWoofSectionInputName", "getParameterType" }, section.getInputs(),
				new WoofSectionInputModel("INPUT_A", "java.lang.Integer"), new WoofSectionInputModel("INPUT_B", null));
		assertList(new String[] { "getWoofSectionOutputName", "getArgumentType" }, section.getOutputs(),
				new WoofSectionOutputModel("OUTPUT_A", "java.lang.String"),
				new WoofSectionOutputModel("OUTPUT_B", null), new WoofSectionOutputModel("OUTPUT_C", null),
				new WoofSectionOutputModel("OUTPUT_D", null), new WoofSectionOutputModel("OUTPUT_E", null),
				new WoofSectionOutputModel("OUTPUT_F", null), new WoofSectionOutputModel("OUTPUT_G", null));
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
		WoofSectionOutputModel sectionOutputProcedure = section.getOutputs().get(5);
		assertProperties(new WoofSectionOutputToWoofProcedureModel("PROCEDURE_A"),
				sectionOutputProcedure.getWoofProcedure(), "getProcedureName");

		// ----------------------------------------
		// Validate the security
		// ----------------------------------------
		assertList(
				new String[] { "getHttpSecurityName", "getHttpSecuritySourceClassName", "getTimeout", "getX", "getY" },
				woof.getWoofSecurities(),
				new WoofSecurityModel("SECURITY_A", "net.example.HttpSecuritySource", 2000, 600, 601),
				new WoofSecurityModel("SECURITY_B", "net.example.AnotherHttpSecuritySource", 0, 610, 611));
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
				new WoofSecurityOutputModel("OUTPUT_FIVE", null), new WoofSecurityOutputModel("OUTPUT_SIX", null));
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
		WoofSecurityOutputModel securityOutputProcedure = security.getOutputs().get(5);
		assertProperties(new WoofSecurityOutputToWoofProcedureModel("PROCEDURE_A"),
				securityOutputProcedure.getWoofProcedure(), "getProcedureName");

		// ----------------------------------------
		// Validate the governances
		// ----------------------------------------
		assertList(new String[] { "getWoofGovernanceName", "getGovernanceSourceClassName", "getX", "getY" },
				woof.getWoofGovernances(),
				new WoofGovernanceModel("GOVERNANCE_A", "net.example.ExampleGovernanceSource", 700, 701),
				new WoofGovernanceModel("GOVERNANCE_B", "net.example.AnotherGovernanceSource", 710, 711));
		WoofGovernanceModel governance = woof.getWoofGovernances().get(0);
		assertList(new String[] { "getName", "getValue" }, governance.getProperties(),
				new PropertyModel("name.a", "value.a"), new PropertyModel("name.b", "value.b"));
		assertList(new String[] { "getX", "getY", "getWidth", "getHeight" }, governance.getGovernanceAreas(),
				new WoofGovernanceAreaModel(740, 741, null, 720, 721),
				new WoofGovernanceAreaModel(750, 751, null, 730, 731));

		// ----------------------------------------
		// Validate the resources
		// ----------------------------------------
		assertList(new String[] { "getResourcePath", "getX", "getY" }, woof.getWoofResources(),
				new WoofResourceModel("/resourceA.html", 800, 801), new WoofResourceModel("/resourceB.png", 810, 811));

		// ----------------------------------------
		// Validate the exceptions
		// ----------------------------------------
		assertList(new String[] { "getClassName", "getX", "getY" }, woof.getWoofExceptions(),
				new WoofExceptionModel("java.lang.Exception", 900, 901),
				new WoofExceptionModel("java.lang.RuntimeException", 902, 903),
				new WoofExceptionModel("java.sql.SQLException", 904, 905),
				new WoofExceptionModel("net.example.AuthException", 906, 907),
				new WoofExceptionModel("java.lang.NullPointerException", 908, 909),
				new WoofExceptionModel("java.io.IOException", 910, 911),
				new WoofExceptionModel("java.lang.Throwable", 912, 913));
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
		WoofExceptionModel exceptionProcedure = woof.getWoofExceptions().get(5);
		assertProperties(new WoofExceptionToWoofProcedureModel("PROCEDURE_A"), exceptionProcedure.getWoofProcedure(),
				"getProcedureName");

		// ----------------------------------------
		// Validate the starts
		// ----------------------------------------
		assertList(new String[] { "getX", "getY" }, woof.getWoofStarts(), new WoofStartModel(null, null, 1000, 1001),
				new WoofStartModel(null, null, 1002, 1003), new WoofStartModel(null, null, 1004, 1005));
		WoofStartModel startSection = woof.getWoofStarts().get(0);
		assertProperties(new WoofStartToWoofSectionInputModel("SECTION_A", "INPUT_A"),
				startSection.getWoofSectionInput(), "getSectionName", "getInputName");
		WoofStartModel startProcedure = woof.getWoofStarts().get(1);
		assertProperties(new WoofStartToWoofProcedureModel("PROCEDURE_A"), startProcedure.getWoofProcedure(),
				"getProcedureName");
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
