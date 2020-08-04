/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.woof.model.woof;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the {@link WoofRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this.createMock(ModelRepository.class);

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private final WritableConfigurationItem configurationItem = this.createMock(WritableConfigurationItem.class);

	/**
	 * {@link WoofRepository}.
	 */
	private final WoofRepository woofRepository = new WoofRepositoryImpl(this.modelRepository);

	/**
	 * Ensures on retrieving a {@link WoofModel} that all {@link ConnectionModel}
	 * instances are connected.
	 */
	public void testRetrieveWoOF() throws Exception {

		// Create the raw WoOF to be connected
		WoofModel woof = new WoofModel();
		WoofHttpContinuationModel httpContinuation = new WoofHttpContinuationModel(false, "HTTP_CONTINUATION");
		woof.addWoofHttpContinuation(httpContinuation);
		WoofHttpContinuationModel httpContinuationLink = new WoofHttpContinuationModel(false,
				"HTTP_CONTINUATION_REDIRECT");
		woof.addWoofHttpContinuation(httpContinuationLink);
		WoofHttpInputModel httpInput = new WoofHttpInputModel(false, "POST", "HTTP_INPUT");
		woof.addWoofHttpInput(httpInput);
		WoofTemplateModel template = new WoofTemplateModel("TEMPLATE", null, null, null, null, null, null, false);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel("TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel("SECTION_INPUT", null);
		section.addInput(sectionInput);
		WoofSectionOutputModel sectionOutput = new WoofSectionOutputModel("SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		WoofProcedureModel procedure = new WoofProcedureModel("PROCEDURE", null, null, null);
		woof.addWoofProcedure(procedure);
		WoofProcedureOutputModel procedureOutput = new WoofProcedureOutputModel("PROCEDURE_OUTPUT", null);
		procedure.addOutput(procedureOutput);
		WoofProcedureNextModel procedureNext = new WoofProcedureNextModel(null);
		procedure.setNext(procedureNext);
		WoofSecurityModel security = new WoofSecurityModel("SECURITY", null, 1000);
		woof.addWoofSecurity(security);
		WoofSecurityOutputModel securityOutput = new WoofSecurityOutputModel("SECURITY_OUTPUT", null);
		security.addOutput(securityOutput);
		WoofResourceModel resource = new WoofResourceModel("RESOURCE");
		woof.addWoofResource(resource);
		WoofExceptionModel exception = new WoofExceptionModel("EXCEPTION");
		woof.addWoofException(exception);
		WoofStartModel start = new WoofStartModel();
		woof.addWoofStart(start);

		/*
		 * HTTP Continuation links
		 */

		// Continuation -> Section Input
		WoofHttpContinuationToWoofSectionInputModel continuationToSectionInput = new WoofHttpContinuationToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		httpContinuation.setWoofSectionInput(continuationToSectionInput);

		// Continuation -> Template
		WoofHttpContinuationToWoofTemplateModel continuationToTemplate = new WoofHttpContinuationToWoofTemplateModel(
				"TEMPLATE");
		httpContinuation.setWoofTemplate(continuationToTemplate);

		// Continuation -> Resource
		WoofHttpContinuationToWoofResourceModel continuationToResource = new WoofHttpContinuationToWoofResourceModel(
				"RESOURCE");
		httpContinuation.setWoofResource(continuationToResource);

		// Continuation -> Security
		WoofHttpContinuationToWoofSecurityModel continuationToSecurity = new WoofHttpContinuationToWoofSecurityModel(
				"SECURITY");
		httpContinuation.setWoofSecurity(continuationToSecurity);

		// Continuation -> Continuation redirect
		WoofHttpContinuationToWoofHttpContinuationModel continuationToRedirect = new WoofHttpContinuationToWoofHttpContinuationModel(
				"HTTP_CONTINUATION_REDIRECT");
		httpContinuation.setWoofRedirect(continuationToRedirect);

		// Continuation -> Procedure
		WoofHttpContinuationToWoofProcedureModel continuationToProcedure = new WoofHttpContinuationToWoofProcedureModel(
				"PROCEDURE");
		httpContinuation.setWoofProcedure(continuationToProcedure);

		/*
		 * HTTP Input links
		 */

		// HTTP Input -> Section Input
		WoofHttpInputToWoofSectionInputModel httpInputToSectionInput = new WoofHttpInputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		httpInput.setWoofSectionInput(httpInputToSectionInput);

		// HTTP Input -> Template
		WoofHttpInputToWoofTemplateModel httpInputToTemplate = new WoofHttpInputToWoofTemplateModel("TEMPLATE");
		httpInput.setWoofTemplate(httpInputToTemplate);

		// HTTP Input -> Resource
		WoofHttpInputToWoofResourceModel httpInputToResource = new WoofHttpInputToWoofResourceModel("RESOURCE");
		httpInput.setWoofResource(httpInputToResource);

		// HTTP Input -> Security
		WoofHttpInputToWoofSecurityModel httpInputToSecurity = new WoofHttpInputToWoofSecurityModel("SECURITY");
		httpInput.setWoofSecurity(httpInputToSecurity);

		// HTTP Input -> Continuation
		WoofHttpInputToWoofHttpContinuationModel httpInputToContinuation = new WoofHttpInputToWoofHttpContinuationModel(
				"HTTP_CONTINUATION");
		httpInput.setWoofHttpContinuation(httpInputToContinuation);

		// HTTP Input -> Procedure
		WoofHttpInputToWoofProcedureModel httpInputToProcedure = new WoofHttpInputToWoofProcedureModel("PROCEDURE");
		httpInput.setWoofProcedure(httpInputToProcedure);

		/**
		 * Template Output links
		 */

		// Template -> Super Template
		WoofTemplateToSuperWoofTemplateModel templateToSuperTemplate = new WoofTemplateToSuperWoofTemplateModel(
				"TEMPLATE");
		template.setSuperWoofTemplate(templateToSuperTemplate);

		// Template Output -> Section Input
		WoofTemplateOutputToWoofSectionInputModel templateOutputToSectionInput = new WoofTemplateOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		templateOutput.setWoofSectionInput(templateOutputToSectionInput);

		// Template Output -> Template
		WoofTemplateOutputToWoofTemplateModel templateOutputToTemplate = new WoofTemplateOutputToWoofTemplateModel(
				"TEMPLATE");
		templateOutput.setWoofTemplate(templateOutputToTemplate);

		// Template Output -> Resource
		WoofTemplateOutputToWoofResourceModel templateOutputToResource = new WoofTemplateOutputToWoofResourceModel(
				"RESOURCE");
		templateOutput.setWoofResource(templateOutputToResource);

		// Template Output -> Security
		WoofTemplateOutputToWoofSecurityModel templateOutputToSecurity = new WoofTemplateOutputToWoofSecurityModel(
				"SECURITY");
		templateOutput.setWoofSecurity(templateOutputToSecurity);

		// Template Output -> Continuation
		WoofTemplateOutputToWoofHttpContinuationModel templateOutputToContinuation = new WoofTemplateOutputToWoofHttpContinuationModel(
				"HTTP_CONTINUATION");
		templateOutput.setWoofHttpContinuation(templateOutputToContinuation);

		// Template Output -> Procedure
		WoofTemplateOutputToWoofProcedureModel templateOutputToProcedure = new WoofTemplateOutputToWoofProcedureModel(
				"PROCEDURE");
		templateOutput.setWoofProcedure(templateOutputToProcedure);

		/*
		 * Section Output links
		 */

		// Section Output -> Section Input
		WoofSectionOutputToWoofSectionInputModel sectionOutputToSectionInput = new WoofSectionOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		sectionOutput.setWoofSectionInput(sectionOutputToSectionInput);

		// Section Output -> Template
		WoofSectionOutputToWoofTemplateModel sectionOutputToTemplate = new WoofSectionOutputToWoofTemplateModel(
				"TEMPLATE");
		sectionOutput.setWoofTemplate(sectionOutputToTemplate);

		// Section Output -> Resource
		WoofSectionOutputToWoofResourceModel sectionOutputToResource = new WoofSectionOutputToWoofResourceModel(
				"RESOURCE");
		sectionOutput.setWoofResource(sectionOutputToResource);

		// Section Output -> Security
		WoofSectionOutputToWoofSecurityModel sectionOutputToSecurity = new WoofSectionOutputToWoofSecurityModel(
				"SECURITY");
		sectionOutput.setWoofSecurity(sectionOutputToSecurity);

		// Section Output -> Continuation
		WoofSectionOutputToWoofHttpContinuationModel sectionOutputToContinuation = new WoofSectionOutputToWoofHttpContinuationModel(
				"HTTP_CONTINUATION");
		sectionOutput.setWoofHttpContinuation(sectionOutputToContinuation);

		// Section Output -> Procedure
		WoofSectionOutputToWoofProcedureModel sectionOutputToProcedure = new WoofSectionOutputToWoofProcedureModel(
				"PROCEDURE");
		sectionOutput.setWoofProcedure(sectionOutputToProcedure);

		/*
		 * Procedure next links
		 */

		// Procedure Next -> Section Input
		WoofProcedureNextToWoofSectionInputModel procedureNextToSectionInput = new WoofProcedureNextToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		procedureNext.setWoofSectionInput(procedureNextToSectionInput);

		// Procedure Next -> Template
		WoofProcedureNextToWoofTemplateModel procedureNextToTemplate = new WoofProcedureNextToWoofTemplateModel(
				"TEMPLATE");
		procedureNext.setWoofTemplate(procedureNextToTemplate);

		// Procedure Next -> Resource
		WoofProcedureNextToWoofResourceModel procedureNextToResource = new WoofProcedureNextToWoofResourceModel(
				"RESOURCE");
		procedureNext.setWoofResource(procedureNextToResource);

		// Procedure Next -> Security
		WoofProcedureNextToWoofSecurityModel procedureNextToSecurity = new WoofProcedureNextToWoofSecurityModel(
				"SECURITY");
		procedureNext.setWoofSecurity(procedureNextToSecurity);

		// Procedure Next -> Continuation
		WoofProcedureNextToWoofHttpContinuationModel procedureNextToContinuation = new WoofProcedureNextToWoofHttpContinuationModel(
				"HTTP_CONTINUATION");
		procedureNext.setWoofHttpContinuation(procedureNextToContinuation);

		// Section Next -> Procedure
		WoofProcedureNextToWoofProcedureModel procedureNextToProcedure = new WoofProcedureNextToWoofProcedureModel(
				"PROCEDURE");
		procedureNext.setWoofProcedure(procedureNextToProcedure);

		/*
		 * Procedure output links
		 */

		// Procedure Output -> Section Input
		WoofProcedureOutputToWoofSectionInputModel procedureOutputToSectionInput = new WoofProcedureOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		procedureOutput.setWoofSectionInput(procedureOutputToSectionInput);

		// Procedure Output -> Template
		WoofProcedureOutputToWoofTemplateModel procedureOutputToTemplate = new WoofProcedureOutputToWoofTemplateModel(
				"TEMPLATE");
		procedureOutput.setWoofTemplate(procedureOutputToTemplate);

		// Procedure Output -> Resource
		WoofProcedureOutputToWoofResourceModel procedureOutputToResource = new WoofProcedureOutputToWoofResourceModel(
				"RESOURCE");
		procedureOutput.setWoofResource(procedureOutputToResource);

		// Procedure Output -> Security
		WoofProcedureOutputToWoofSecurityModel procedureOutputToSecurity = new WoofProcedureOutputToWoofSecurityModel(
				"SECURITY");
		procedureOutput.setWoofSecurity(procedureOutputToSecurity);

		// Procedure Output -> Continuation
		WoofProcedureOutputToWoofHttpContinuationModel procedureOutputToContinuation = new WoofProcedureOutputToWoofHttpContinuationModel(
				"HTTP_CONTINUATION");
		procedureOutput.setWoofHttpContinuation(procedureOutputToContinuation);

		// Section Output -> Procedure
		WoofProcedureOutputToWoofProcedureModel procedureOutputToProcedure = new WoofProcedureOutputToWoofProcedureModel(
				"PROCEDURE");
		procedureOutput.setWoofProcedure(procedureOutputToProcedure);

		/*
		 * Security Output links
		 */

		// Security Output -> Section Input
		WoofSecurityOutputToWoofSectionInputModel securityOutputToSectionInput = new WoofSecurityOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		securityOutput.setWoofSectionInput(securityOutputToSectionInput);

		// Security Output -> Template
		WoofSecurityOutputToWoofTemplateModel securityOutputToTemplate = new WoofSecurityOutputToWoofTemplateModel(
				"TEMPLATE");
		securityOutput.setWoofTemplate(securityOutputToTemplate);

		// Security Output -> Resource
		WoofSecurityOutputToWoofResourceModel securityOutputToResource = new WoofSecurityOutputToWoofResourceModel(
				"RESOURCE");
		securityOutput.setWoofResource(securityOutputToResource);

		// Security Output -> Security
		WoofSecurityOutputToWoofSecurityModel securityOutputToSecurity = new WoofSecurityOutputToWoofSecurityModel(
				"SECURITY");
		securityOutput.setWoofSecurity(securityOutputToSecurity);

		// Security Output -> Continuation
		WoofSecurityOutputToWoofHttpContinuationModel securityOutputToContinuation = new WoofSecurityOutputToWoofHttpContinuationModel(
				"HTTP_CONTINUATION");
		securityOutput.setWoofHttpContinuation(securityOutputToContinuation);

		// Security Output -> Procedure
		WoofSecurityOutputToWoofProcedureModel securityOutputToProcedure = new WoofSecurityOutputToWoofProcedureModel(
				"PROCEDURE");
		securityOutput.setWoofProcedure(securityOutputToProcedure);

		/*
		 * Exception links
		 */

		// Exception -> Section Input
		WoofExceptionToWoofSectionInputModel exceptionToSectionInput = new WoofExceptionToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		exception.setWoofSectionInput(exceptionToSectionInput);

		// Exception -> Template
		WoofExceptionToWoofTemplateModel exceptionToTemplate = new WoofExceptionToWoofTemplateModel("TEMPLATE");
		exception.setWoofTemplate(exceptionToTemplate);

		// Exception -> Resource
		WoofExceptionToWoofResourceModel exceptionToResource = new WoofExceptionToWoofResourceModel("RESOURCE");
		exception.setWoofResource(exceptionToResource);

		// Exception -> Security
		WoofExceptionToWoofSecurityModel exceptionToSecurity = new WoofExceptionToWoofSecurityModel("SECURITY");
		exception.setWoofSecurity(exceptionToSecurity);

		// Exception -> Continuation
		WoofExceptionToWoofHttpContinuationModel exceptionToContinuation = new WoofExceptionToWoofHttpContinuationModel(
				"HTTP_CONTINUATION");
		exception.setWoofHttpContinuation(exceptionToContinuation);

		// Exception -> Procedure
		WoofExceptionToWoofProcedureModel exceptionToProcedure = new WoofExceptionToWoofProcedureModel("PROCEDURE");
		exception.setWoofProcedure(exceptionToProcedure);

		/*
		 * Start links
		 */

		// Start -> Section Input
		WoofStartToWoofSectionInputModel startToSectionInput = new WoofStartToWoofSectionInputModel("SECTION",
				"SECTION_INPUT");
		start.setWoofSectionInput(startToSectionInput);

		// Start -> Procedure
		WoofStartToWoofProcedureModel startToProcedure = new WoofStartToWoofProcedureModel("PROCEDURE");
		start.setWoofProcedure(startToProcedure);

		// Record retrieving the WoOF
		this.modelRepository.retrieve(this.paramType(WoofModel.class), this.param(this.configurationItem));

		// Retrieve the WoOF
		this.replayMockObjects();
		this.woofRepository.retrieveWoof(woof, this.configurationItem);
		this.verifyMockObjects();

		// HTTP Continuation links
		AssertLinks<WoofHttpContinuationModel> assertHttpContinuation = new AssertLinks<>("http continuation",
				httpContinuation);
		assertHttpContinuation.assertLink(continuationToSectionInput, "section input", sectionInput);
		assertHttpContinuation.assertLink(continuationToTemplate, "template", template);
		assertHttpContinuation.assertLink(continuationToResource, "resource", resource);
		assertHttpContinuation.assertLink(continuationToSecurity, "security", security);
		assertEquals("http continuation -> redirect", httpContinuationLink, continuationToRedirect.getWoofRedirect());
		assertEquals("http continuation <- redirect", httpContinuation,
				continuationToRedirect.getWoofHttpContinuation());
		assertHttpContinuation.assertLink(continuationToProcedure, "procedure", procedure);

		// HTTP Input links
		AssertLinks<WoofHttpInputModel> assertHttpInput = new AssertLinks<>("http input", httpInput);
		assertHttpInput.assertLink(httpInputToSectionInput, "section input", sectionInput);
		assertHttpInput.assertLink(httpInputToTemplate, "template", template);
		assertHttpInput.assertLink(httpInputToResource, "resource", resource);
		assertHttpInput.assertLink(httpInputToSecurity, "security", security);
		assertHttpInput.assertLink(httpInputToContinuation, "http continuation", httpContinuation);
		assertHttpInput.assertLink(httpInputToProcedure, "procedure", procedure);

		// Template links
		AssertLinks<WoofTemplateModel> assertTemplate = new AssertLinks<>("template", template);
		assertTemplate.assertLink(template.getSuperWoofTemplate(), "super template", template);

		// Template Output links
		AssertLinks<WoofTemplateOutputModel> assertTemplateOutput = new AssertLinks<>("template output",
				templateOutput);
		assertTemplateOutput.assertLink(templateOutputToSectionInput, "section input", sectionInput);
		assertTemplateOutput.assertLink(templateOutputToTemplate, "template", template);
		assertTemplateOutput.assertLink(templateOutputToResource, "resource", resource);
		assertTemplateOutput.assertLink(templateOutputToSecurity, "security", security);
		assertTemplateOutput.assertLink(templateOutputToContinuation, "http continuation", httpContinuation);
		assertTemplateOutput.assertLink(templateOutputToProcedure, "procedure", procedure);

		// Section Output links
		AssertLinks<WoofSectionOutputModel> assertSectionOutput = new AssertLinks<>("section output", sectionOutput);
		assertSectionOutput.assertLink(sectionOutputToSectionInput, "section input", sectionInput);
		assertSectionOutput.assertLink(sectionOutputToTemplate, "template", template);
		assertSectionOutput.assertLink(sectionOutputToResource, "resource", resource);
		assertSectionOutput.assertLink(sectionOutputToSecurity, "security", security);
		assertSectionOutput.assertLink(sectionOutputToContinuation, "http continuation", httpContinuation);
		assertSectionOutput.assertLink(sectionOutputToProcedure, "procedure", procedure);

		// Procedure Next links
		AssertLinks<WoofProcedureNextModel> assertProcedureNext = new AssertLinks<>("procedure next", procedureNext);
		assertProcedureNext.assertLink(procedureNextToSectionInput, "section input", sectionInput);
		assertProcedureNext.assertLink(procedureNextToTemplate, "template", template);
		assertProcedureNext.assertLink(procedureNextToResource, "resource", resource);
		assertProcedureNext.assertLink(procedureNextToSecurity, "security", security);
		assertProcedureNext.assertLink(procedureNextToContinuation, "http continuation", httpContinuation);
		assertProcedureNext.assertLink(procedureNextToProcedure, "procedure", procedure);

		// Procedure Output links
		AssertLinks<WoofProcedureOutputModel> assertProcedureOutput = new AssertLinks<>("procedure output",
				procedureOutput);
		assertProcedureOutput.assertLink(procedureOutputToSectionInput, "section input", sectionInput);
		assertProcedureOutput.assertLink(procedureOutputToTemplate, "template", template);
		assertProcedureOutput.assertLink(procedureOutputToResource, "resource", resource);
		assertProcedureOutput.assertLink(procedureOutputToSecurity, "security", security);
		assertProcedureOutput.assertLink(procedureOutputToContinuation, "http continuation", httpContinuation);
		assertProcedureOutput.assertLink(procedureOutputToProcedure, "procedure", procedure);

		// Security Output links
		AssertLinks<WoofSecurityOutputModel> assertSecurityOutput = new AssertLinks<>("security output",
				securityOutput);
		assertSecurityOutput.assertLink(securityOutputToSectionInput, "section input", sectionInput);
		assertSecurityOutput.assertLink(securityOutputToTemplate, "template", template);
		assertSecurityOutput.assertLink(securityOutputToResource, "resource", resource);
		assertSecurityOutput.assertLink(securityOutputToSecurity, "security", security);
		assertSecurityOutput.assertLink(securityOutputToContinuation, "http continuation", httpContinuation);
		assertSecurityOutput.assertLink(securityOutputToProcedure, "procedure", procedure);

		// Exception links
		AssertLinks<WoofExceptionModel> assertException = new AssertLinks<>("exception", exception);
		assertException.assertLink(exceptionToSectionInput, "section input", sectionInput);
		assertException.assertLink(exceptionToTemplate, "template", template);
		assertException.assertLink(exceptionToResource, "resource", resource);
		assertException.assertLink(exceptionToSecurity, "security", security);
		assertException.assertLink(exceptionToContinuation, "http continuation", httpContinuation);
		assertException.assertLink(exceptionToProcedure, "procedure", procedure);

		// Start links
		AssertLinks<WoofStartModel> assertStart = new AssertLinks<>("start", start);
		assertStart.assertLink(startToSectionInput, "section input", sectionInput);
		assertStart.assertLink(startToProcedure, "procedure", procedure);
	}

	/**
	 * Convenience class to simplify asserting links.
	 */
	private static class AssertLinks<S extends Model> {

		private String sourceName;
		private S source;
		private String previousMethodName = null;

		private AssertLinks(String sourceName, S source) {
			this.sourceName = sourceName;
			this.source = source;
		}

		@SuppressWarnings("unchecked")
		private <L extends ConnectionModel, T extends Model> void assertLink(L link, String targetName, T target) {

			// Obtain the source
			this.previousMethodName = null;
			S linkSource = (S) this.getModel(link, this.source.getClass());
			T linkTarget = (T) this.getModel(link, target.getClass());

			// Undertake the assertions
			assertEquals(this.sourceName + " <- " + targetName, this.source, linkSource);
			assertEquals(this.sourceName + " -> " + targetName, target, linkTarget);
		}

		private <L extends ConnectionModel> Object getModel(L link, Class<?> modelType) {
			for (Method method : link.getClass().getMethods()) {
				if (method.getReturnType() == modelType) {
					if (!method.getName().equals(this.previousMethodName)) {
						try {
							return method.invoke(link);
						} catch (Exception ex) {
							throw fail(ex);
						}
					}
				}
			}
			fail("Can not obtain link end model " + modelType.getName() + " from link " + link.getClass().getName());
			return null;
		}
	}

	/**
	 * Ensures on storing a {@link WoofModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreWoOF() throws Exception {

		// Create the WoOF (without connections)
		WoofModel woof = new WoofModel();
		WoofHttpContinuationModel httpContinuation = new WoofHttpContinuationModel(false, "HTTP_CONTINUATION");
		woof.addWoofHttpContinuation(httpContinuation);
		WoofHttpInputModel httpInput = new WoofHttpInputModel(false, "POST", "HTTP_INPUT");
		woof.addWoofHttpInput(httpInput);
		WoofTemplateModel template = new WoofTemplateModel("TEMPLATE", null, null, null, null, null, null, false);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel("TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel("SECTION_INPUT", null);
		section.addInput(sectionInput);
		WoofSectionOutputModel sectionOutput = new WoofSectionOutputModel("SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		WoofProcedureModel procedure = new WoofProcedureModel("PROCEDURE", null, null, null);
		woof.addWoofProcedure(procedure);
		WoofProcedureNextModel procedureNext = new WoofProcedureNextModel();
		procedure.setNext(procedureNext);
		WoofProcedureOutputModel procedureOutput = new WoofProcedureOutputModel("PROCEDURE OUTPUT", null);
		procedure.addOutput(procedureOutput);
		WoofSecurityModel security = new WoofSecurityModel("SECURITY", null, 1000);
		woof.addWoofSecurity(security);
		WoofSecurityOutputModel securityOutput = new WoofSecurityOutputModel("ACCESS_OUTPUT", null);
		security.addOutput(securityOutput);
		WoofResourceModel resource = new WoofResourceModel("RESOURCE");
		woof.addWoofResource(resource);
		WoofExceptionModel exception = new WoofExceptionModel("EXCEPTION");
		woof.addWoofException(exception);
		WoofStartModel start = new WoofStartModel();
		woof.addWoofStart(start);

		// HTTP Continuation links
		WoofHttpContinuationToWoofSectionInputModel continuationToSectionInput = link(
				new WoofHttpContinuationToWoofSectionInputModel(), httpContinuation, sectionInput);
		WoofHttpContinuationToWoofTemplateModel continuationPathToTemplate = link(
				new WoofHttpContinuationToWoofTemplateModel(), httpContinuation, template);
		WoofHttpContinuationToWoofResourceModel continuationPathToResource = link(
				new WoofHttpContinuationToWoofResourceModel(), httpContinuation, resource);
		WoofHttpContinuationToWoofSecurityModel continuationPathToSecurity = link(
				new WoofHttpContinuationToWoofSecurityModel(), httpContinuation, security);
		WoofHttpContinuationToWoofHttpContinuationModel continuationPathToApplicationPath = link(
				new WoofHttpContinuationToWoofHttpContinuationModel(), httpContinuation, httpContinuation);
		WoofHttpContinuationToWoofProcedureModel continuationToProcedure = link(
				new WoofHttpContinuationToWoofProcedureModel(), httpContinuation, procedure);

		// HTTP Input links
		WoofHttpInputToWoofSectionInputModel httpInputToSectionInput = link(new WoofHttpInputToWoofSectionInputModel(),
				httpInput, sectionInput);
		WoofHttpInputToWoofTemplateModel httpInputPathToTemplate = link(new WoofHttpInputToWoofTemplateModel(),
				httpInput, template);
		WoofHttpInputToWoofResourceModel httpInputPathToResource = link(new WoofHttpInputToWoofResourceModel(),
				httpInput, resource);
		WoofHttpInputToWoofSecurityModel httpInputPathToSecurity = link(new WoofHttpInputToWoofSecurityModel(),
				httpInput, security);
		WoofHttpInputToWoofHttpContinuationModel httpInputPathToApplicationPath = link(
				new WoofHttpInputToWoofHttpContinuationModel(), httpInput, httpContinuation);
		WoofHttpInputToWoofProcedureModel httpInputPathToProcedure = link(new WoofHttpInputToWoofProcedureModel(),
				httpInput, procedure);

		// Template links
		WoofTemplateToSuperWoofTemplateModel templateToSuperTemplate = link(new WoofTemplateToSuperWoofTemplateModel(),
				template, template);

		// Template Output links
		WoofTemplateOutputToWoofSectionInputModel templateOutputToSectionInput = link(
				new WoofTemplateOutputToWoofSectionInputModel(), templateOutput, sectionInput);
		WoofTemplateOutputToWoofTemplateModel templateOutputToTemplate = link(
				new WoofTemplateOutputToWoofTemplateModel(), templateOutput, template);
		WoofTemplateOutputToWoofResourceModel templateOutputToResource = link(
				new WoofTemplateOutputToWoofResourceModel(), templateOutput, resource);
		WoofTemplateOutputToWoofSecurityModel templateOutputToSecurity = link(
				new WoofTemplateOutputToWoofSecurityModel(), templateOutput, security);
		WoofTemplateOutputToWoofHttpContinuationModel templateOutputToApplicationPath = link(
				new WoofTemplateOutputToWoofHttpContinuationModel(), templateOutput, httpContinuation);
		WoofTemplateOutputToWoofProcedureModel templateOutputToProcedure = link(
				new WoofTemplateOutputToWoofProcedureModel(), templateOutput, procedure);

		// Section Output links
		WoofSectionOutputToWoofSectionInputModel sectionOutputToSectionInput = link(
				new WoofSectionOutputToWoofSectionInputModel(), sectionOutput, sectionInput);
		WoofSectionOutputToWoofTemplateModel sectionOutputToTemplate = link(new WoofSectionOutputToWoofTemplateModel(),
				sectionOutput, template);
		WoofSectionOutputToWoofResourceModel sectionOutputToResource = link(new WoofSectionOutputToWoofResourceModel(),
				sectionOutput, resource);
		WoofSectionOutputToWoofSecurityModel sectionOutputToSecurity = link(new WoofSectionOutputToWoofSecurityModel(),
				sectionOutput, security);
		WoofSectionOutputToWoofHttpContinuationModel sectionOutputToApplicationPath = link(
				new WoofSectionOutputToWoofHttpContinuationModel(), sectionOutput, httpContinuation);
		WoofSectionOutputToWoofProcedureModel sectionOutputToProcedure = link(
				new WoofSectionOutputToWoofProcedureModel(), sectionOutput, procedure);

		// Procedure Next links
		WoofProcedureNextToWoofSectionInputModel procedureNextToSectionInput = link(
				new WoofProcedureNextToWoofSectionInputModel(), procedureNext, sectionInput);
		WoofProcedureNextToWoofTemplateModel procedureNextToTemplate = link(new WoofProcedureNextToWoofTemplateModel(),
				procedureNext, template);
		WoofProcedureNextToWoofResourceModel procedureNextToResource = link(new WoofProcedureNextToWoofResourceModel(),
				procedureNext, resource);
		WoofProcedureNextToWoofSecurityModel procedureNextToSecurity = link(new WoofProcedureNextToWoofSecurityModel(),
				procedureNext, security);
		WoofProcedureNextToWoofHttpContinuationModel procedureNextToApplicationPath = link(
				new WoofProcedureNextToWoofHttpContinuationModel(), procedureNext, httpContinuation);
		WoofProcedureNextToWoofProcedureModel procedureNextToProcedure = link(
				new WoofProcedureNextToWoofProcedureModel(), procedureNext, procedure);

		// Procedure Output links
		WoofProcedureOutputToWoofSectionInputModel procedureOutputToSectionInput = link(
				new WoofProcedureOutputToWoofSectionInputModel(), procedureOutput, sectionInput);
		WoofProcedureOutputToWoofTemplateModel procedureOutputToTemplate = link(
				new WoofProcedureOutputToWoofTemplateModel(), procedureOutput, template);
		WoofProcedureOutputToWoofResourceModel procedureOutputToResource = link(
				new WoofProcedureOutputToWoofResourceModel(), procedureOutput, resource);
		WoofProcedureOutputToWoofSecurityModel procedureOutputToSecurity = link(
				new WoofProcedureOutputToWoofSecurityModel(), procedureOutput, security);
		WoofProcedureOutputToWoofHttpContinuationModel procedureOutputToApplicationPath = link(
				new WoofProcedureOutputToWoofHttpContinuationModel(), procedureOutput, httpContinuation);
		WoofProcedureOutputToWoofProcedureModel procedureOutputToProcedure = link(
				new WoofProcedureOutputToWoofProcedureModel(), procedureOutput, procedure);

		// Security Output links
		WoofSecurityOutputToWoofSectionInputModel securityOutputToSectionInput = link(
				new WoofSecurityOutputToWoofSectionInputModel(), securityOutput, sectionInput);
		WoofSecurityOutputToWoofTemplateModel securityOutputToTemplate = link(
				new WoofSecurityOutputToWoofTemplateModel(), securityOutput, template);
		WoofSecurityOutputToWoofResourceModel securityOutputToResource = link(
				new WoofSecurityOutputToWoofResourceModel(), securityOutput, resource);
		WoofSecurityOutputToWoofSecurityModel securityOutputToSecurity = link(
				new WoofSecurityOutputToWoofSecurityModel(), securityOutput, security);
		WoofSecurityOutputToWoofHttpContinuationModel securityOutputToApplicationPath = link(
				new WoofSecurityOutputToWoofHttpContinuationModel(), securityOutput, httpContinuation);
		WoofSecurityOutputToWoofProcedureModel securityOutputToProcedure = link(
				new WoofSecurityOutputToWoofProcedureModel(), securityOutput, procedure);

		// Exception links
		WoofExceptionToWoofSectionInputModel exceptionToSectionInput = link(new WoofExceptionToWoofSectionInputModel(),
				exception, sectionInput);
		WoofExceptionToWoofTemplateModel exceptionToTemplate = link(new WoofExceptionToWoofTemplateModel(), exception,
				template);
		WoofExceptionToWoofResourceModel exceptionToResource = link(new WoofExceptionToWoofResourceModel(), exception,
				resource);
		WoofExceptionToWoofSecurityModel exceptionToSecurity = link(new WoofExceptionToWoofSecurityModel(), exception,
				security);
		WoofExceptionToWoofHttpContinuationModel exceptionToApplicationPath = link(
				new WoofExceptionToWoofHttpContinuationModel(), exception, httpContinuation);
		WoofExceptionToWoofProcedureModel exceptionToProcedure = link(new WoofExceptionToWoofProcedureModel(),
				exception, procedure);

		// Start -> Section Input
		WoofStartToWoofSectionInputModel startToSectionInput = link(new WoofStartToWoofSectionInputModel(), start,
				sectionInput);
		WoofStartToWoofProcedureModel startToProcedure = link(new WoofStartToWoofProcedureModel(), start, procedure);

		// Record storing the WoOf
		this.modelRepository.store(woof, this.configurationItem);

		// Store the WoOF
		this.replayMockObjects();
		this.woofRepository.storeWoof(woof, this.configurationItem);
		this.verifyMockObjects();

		// Assert HTTP Continuation links
		assertEquals("http continuation - section input (section name)", "SECTION",
				continuationToSectionInput.getSectionName());
		assertEquals("http continuation - section input (input name)", "SECTION_INPUT",
				continuationToSectionInput.getInputName());
		assertEquals("http continuation - template", "TEMPLATE", continuationPathToTemplate.getApplicationPath());
		assertEquals("http continuation - resource", "RESOURCE", continuationPathToResource.getResourcePath());
		assertEquals("http continuation - security", "SECURITY", continuationPathToSecurity.getHttpSecurityName());
		assertEquals("http continuation - application path", "HTTP_CONTINUATION",
				continuationPathToApplicationPath.getApplicationPath());
		assertEquals("http continuation - procedure", "PROCEDURE", continuationToProcedure.getProcedureName());

		// Assert HTTP Input links
		assertEquals("http input - section input (section name)", "SECTION", httpInputToSectionInput.getSectionName());
		assertEquals("http input - section input (input name)", "SECTION_INPUT",
				httpInputToSectionInput.getInputName());
		assertEquals("http input - template", "TEMPLATE", httpInputPathToTemplate.getApplicationPath());
		assertEquals("http input - resource", "RESOURCE", httpInputPathToResource.getResourcePath());
		assertEquals("http input - security", "SECURITY", httpInputPathToSecurity.getHttpSecurityName());
		assertEquals("http input - application path", "HTTP_CONTINUATION",
				httpInputPathToApplicationPath.getApplicationPath());
		assertEquals("http input - procedure", "PROCEDURE", httpInputPathToProcedure.getProcedureName());

		// Assert Template links
		assertEquals("template - super template", "TEMPLATE",
				templateToSuperTemplate.getSuperWoofTemplateApplicationPath());

		// Assert Template Output links
		assertEquals("template output - section input (section name)", "SECTION",
				templateOutputToSectionInput.getSectionName());
		assertEquals("template output - section input (input name)", "SECTION_INPUT",
				templateOutputToSectionInput.getInputName());
		assertEquals("template output - template", "TEMPLATE", templateOutputToTemplate.getApplicationPath());
		assertEquals("template output - resource", "RESOURCE", templateOutputToResource.getResourcePath());
		assertEquals("template output - security", "SECURITY", templateOutputToSecurity.getHttpSecurityName());
		assertEquals("template output - application path", "HTTP_CONTINUATION",
				templateOutputToApplicationPath.getApplicationPath());
		assertEquals("template output - procedure", "PROCEDURE", templateOutputToProcedure.getProcedureName());

		// Assert Section Output links
		assertEquals("section output - section input (section name)", "SECTION",
				sectionOutputToSectionInput.getSectionName());
		assertEquals("section output - section input (input name)", "SECTION_INPUT",
				sectionOutputToSectionInput.getInputName());
		assertEquals("section output - template", "TEMPLATE", sectionOutputToTemplate.getApplicationPath());
		assertEquals("section output - resource", "RESOURCE", sectionOutputToResource.getResourcePath());
		assertEquals("section output - security", "SECURITY", sectionOutputToSecurity.getHttpSecurityName());
		assertEquals("section output - application path", "HTTP_CONTINUATION",
				sectionOutputToApplicationPath.getApplicationPath());
		assertEquals("section output - procedure", "PROCEDURE", sectionOutputToProcedure.getProcedureName());

		// Assert Procedure Next links
		assertEquals("procedure next - section input (section name)", "SECTION",
				procedureNextToSectionInput.getSectionName());
		assertEquals("procedure next - section input (input name)", "SECTION_INPUT",
				procedureNextToSectionInput.getInputName());
		assertEquals("procedure next - template", "TEMPLATE", procedureNextToTemplate.getApplicationPath());
		assertEquals("procedure next - resource", "RESOURCE", procedureNextToResource.getResourcePath());
		assertEquals("procedure next - security", "SECURITY", procedureNextToSecurity.getHttpSecurityName());
		assertEquals("procedure next - application path", "HTTP_CONTINUATION",
				procedureNextToApplicationPath.getApplicationPath());
		assertEquals("procedure next - procedure", "PROCEDURE", procedureNextToProcedure.getProcedureName());

		// Assert Procedure Output links
		assertEquals("procedure output - section input (section name)", "SECTION",
				procedureOutputToSectionInput.getSectionName());
		assertEquals("procedure output - section input (input name)", "SECTION_INPUT",
				procedureOutputToSectionInput.getInputName());
		assertEquals("procedure output - template", "TEMPLATE", procedureOutputToTemplate.getApplicationPath());
		assertEquals("procedure output - resource", "RESOURCE", procedureOutputToResource.getResourcePath());
		assertEquals("procedure output - security", "SECURITY", procedureOutputToSecurity.getHttpSecurityName());
		assertEquals("procedure output - application path", "HTTP_CONTINUATION",
				procedureOutputToApplicationPath.getApplicationPath());
		assertEquals("procedure output - procedure", "PROCEDURE", procedureOutputToProcedure.getProcedureName());

		// Assert Security Output links
		assertEquals("security output - section input (section name)", "SECTION",
				securityOutputToSectionInput.getSectionName());
		assertEquals("access output - section input (input name", "SECTION_INPUT",
				securityOutputToSectionInput.getInputName());
		assertEquals("security output - template", "TEMPLATE", securityOutputToTemplate.getApplicationPath());
		assertEquals("security output - resource", "RESOURCE", securityOutputToResource.getResourcePath());
		assertEquals("security output - security", "SECURITY", securityOutputToSecurity.getHttpSecurityName());
		assertEquals("security output - application path", "HTTP_CONTINUATION",
				securityOutputToApplicationPath.getApplicationPath());
		assertEquals("security output - procedure", "PROCEDURE", securityOutputToProcedure.getProcedureName());

		// Assert Exception links
		assertEquals("exception - section input (section name)", "SECTION", exceptionToSectionInput.getSectionName());
		assertEquals("exception - section input (input name)", "SECTION_INPUT", exceptionToSectionInput.getInputName());
		assertEquals("exception - template", "TEMPLATE", exceptionToTemplate.getApplicationPath());
		assertEquals("exception - resource", "RESOURCE", exceptionToResource.getResourcePath());
		assertEquals("exception - security", "SECURITY", exceptionToSecurity.getHttpSecurityName());
		assertEquals("exception - application path", "HTTP_CONTINUATION",
				exceptionToApplicationPath.getApplicationPath());
		assertEquals("exception - procedure", "PROCEDURE", exceptionToProcedure.getProcedureName());

		// Assert Start links
		assertEquals("start - section input (section name)", "SECTION", startToSectionInput.getSectionName());
		assertEquals("start - section input (input name)", "SECTION_INPUT", startToSectionInput.getInputName());
		assertEquals("start - procedure", "PROCEDURE", startToProcedure.getProcedureName());
	}

	/**
	 * Convenience method to create a link.
	 */
	private static <L extends ConnectionModel> L link(L link, Model source, Model target) {
		final Closure<String> previousMethodName = new Closure<>();
		final Consumer<Model> loadEndModel = (model) -> {
			for (Method method : link.getClass().getMethods()) {
				if (method.getParameterTypes().length == 1) {
					if (method.getParameterTypes()[0] == model.getClass()) {
						if (!method.getName().equals(previousMethodName.value)) {
							try {
								method.invoke(link, model);
							} catch (Exception ex) {
								throw fail(ex);
							}
							previousMethodName.value = method.getName();
							return; // loaded
						}
					}
				}
			}
			fail("Unable to set model " + model.getClass().getName() + " on connection " + link.getClass().getName());
		};
		loadEndModel.accept(source);
		loadEndModel.accept(target);
		link.connect();
		return link;
	}

}
