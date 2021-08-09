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

package net.officefloor.woof;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplateArchitect;
import net.officefloor.woof.model.woof.DocumentationModel;
import net.officefloor.woof.model.woof.PropertyModel;
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofHttpInputModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureNextModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputModel;
import net.officefloor.woof.model.woof.WoofRepository;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;
import net.officefloor.woof.model.woof.WoofSecurityContentTypeModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofTemplateExtensionModel;
import net.officefloor.woof.model.woof.WoofTemplateLinkModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel;
import net.officefloor.woof.model.woof.WoofTemplateRenderHttpMethodModel;
import net.officefloor.woof.model.woof.WoofTemplateToSuperWoofTemplateModel;
import net.officefloor.woof.template.WoofTemplateExtensionException;
import net.officefloor.woof.template.WoofTemplateExtensionLoader;
import net.officefloor.woof.template.WoofTemplateExtensionLoaderImpl;
import net.officefloor.woof.template.WoofTemplateExtensionSource;
import net.officefloor.woof.template.WoofTemplateExtensionSourceService;

/**
 * {@link WoofLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderImpl implements WoofLoader {

	/**
	 * {@link WoofRepository}.
	 */
	private final WoofRepository repository;

	/**
	 * Initiate.
	 * 
	 * @param repository {@link WoofRepository}.
	 */
	public WoofLoaderImpl(WoofRepository repository) {
		this.repository = repository;
	}

	/*
	 * ======================= WoofLoader ===========================
	 */

	@Override
	public void loadWoofConfiguration(WoofContext context) throws Exception {

		// Load the WoOF model
		WoofModel woof = new WoofModel();
		this.repository.retrieveWoof(woof, context.getConfiguration());

		// Obtain the various architects
		OfficeArchitect officeArchitect = context.getOfficeArchitect();
		WebArchitect webArchitect = context.getWebArchitect();
		HttpSecurityArchitect securityArchitect = context.getHttpSecurityArchitect();
		WebTemplateArchitect templaterArchitect = context.getWebTemplater();
		HttpResourceArchitect resourceArchitect = context.getHttpResourceArchitect();
		ProcedureArchitect<OfficeSection> procedureArchitect = context.getProcedureArchitect();

		// Obtain the context
		OfficeExtensionContext extensionContext = context.getOfficeExtensionContext();

		// Configure the templates
		TemplateConnector templates = new TemplateConnector(woof, templaterArchitect, webArchitect, officeArchitect,
				extensionContext);

		// Configure the Sections
		SectionConnector sections = new SectionConnector(woof, officeArchitect);

		// Configure the Procedures
		ProcedureConnector procedures = new ProcedureConnector(woof, officeArchitect, procedureArchitect,
				extensionContext);

		// Configure the Security
		SecurityConnector securities = new SecurityConnector(woof, officeArchitect, securityArchitect);

		// Configure the resources
		ResourceConnector resources = new ResourceConnector(woof, officeArchitect, resourceArchitect);

		// Configure the HTTP continuations
		HttpContinuationConnector httpContinuations = new HttpContinuationConnector(woof, officeArchitect,
				webArchitect);

		// Link the HTTP continuations
		for (WoofHttpContinuationModel httpContinuationModel : woof.getWoofHttpContinuations()) {

			// Obtain the HTTP continuation
			String applicationPath = httpContinuationModel.getApplicationPath();
			HttpUrlContinuation httpContinuation = httpContinuations.httpContinuations.get(applicationPath);

			// Undertake links
			Supplier<OfficeFlowSourceNode> handleFlow = () -> httpContinuation.getInput();
			sections.linkToSectionInput(handleFlow, httpContinuationModel.getWoofSectionInput(),
					(link) -> link.getWoofSectionInput());
			templates.linkToTemplate(handleFlow, httpContinuationModel.getWoofTemplate(),
					(link) -> link.getWoofTemplate(), null);
			securities.linkToSecurity(handleFlow, httpContinuationModel.getWoofSecurity(),
					(link) -> link.getWoofSecurity());
			resources.linkToResource(handleFlow, httpContinuationModel.getWoofResource(),
					(link) -> link.getWoofResource());
			httpContinuations.linkToHttpContinuation(handleFlow, httpContinuationModel.getWoofRedirect(),
					(link) -> link.getWoofRedirect(), null);
			procedures.linkToProcedure(handleFlow, httpContinuationModel.getWoofProcedure(),
					(link) -> link.getWoofProcedure());
		}

		// Link the HTTP inputs
		for (WoofHttpInputModel httpInputModel : woof.getWoofHttpInputs()) {

			// Obtain the HTTP input
			boolean isSecure = httpInputModel.getIsSecure();
			String httpMethod = httpInputModel.getHttpMethod();
			String applicationPath = httpInputModel.getApplicationPath();
			HttpInput httpInput = webArchitect.getHttpInput(isSecure, httpMethod, applicationPath);

			// Provide possible documentation
			DocumentationModel documentation = httpInputModel.getDocumentation();
			if (documentation != null) {
				httpInput.setDocumentation(documentation.getDescription());
			}

			// Undertake links
			Supplier<OfficeFlowSourceNode> handleFlow = () -> httpInput.getInput();
			sections.linkToSectionInput(handleFlow, httpInputModel.getWoofSectionInput(),
					(link) -> link.getWoofSectionInput());
			templates.linkToTemplate(handleFlow, httpInputModel.getWoofTemplate(), (link) -> link.getWoofTemplate(),
					null);
			securities.linkToSecurity(handleFlow, httpInputModel.getWoofSecurity(), (link) -> link.getWoofSecurity());
			resources.linkToResource(handleFlow, httpInputModel.getWoofResource(), (link) -> link.getWoofResource());
			httpContinuations.linkToHttpContinuation(handleFlow, httpInputModel.getWoofHttpContinuation(),
					(link) -> link.getWoofHttpContinuation(), null);
			procedures.linkToProcedure(handleFlow, httpInputModel.getWoofProcedure(),
					(link) -> link.getWoofProcedure());
		}

		// Link the template outputs
		for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {

			// Obtain the template
			String applicationPath = templateModel.getApplicationPath();
			WebTemplate template = templates.templates.get(applicationPath);

			// Provide link configuration inheritance
			WoofTemplateToSuperWoofTemplateModel superTemplateLink = templateModel.getSuperWoofTemplate();
			if (superTemplateLink != null) {
				WebTemplate superTemplate = templates.templates
						.get(superTemplateLink.getSuperWoofTemplateApplicationPath());
				template.setSuperTemplate(superTemplate);
			}

			// Link outputs for the template
			for (WoofTemplateOutputModel outputModel : templateModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofTemplateOutputName();

				// Obtain the output argument type
				String outputArgumentTypeName = outputModel.getArgumentType();
				if (CompileUtil.isBlank(outputArgumentTypeName)) {
					outputArgumentTypeName = null;
				}

				// Undertake links
				Supplier<OfficeFlowSourceNode> outputFlow = () -> template.getOutput(outputName);
				sections.linkToSectionInput(outputFlow, outputModel.getWoofSectionInput(),
						(link) -> link.getWoofSectionInput());
				templates.linkToTemplate(outputFlow, outputModel.getWoofTemplate(), (link) -> link.getWoofTemplate(),
						outputArgumentTypeName);
				securities.linkToSecurity(outputFlow, outputModel.getWoofSecurity(), (link) -> link.getWoofSecurity());
				resources.linkToResource(outputFlow, outputModel.getWoofResource(), (link) -> link.getWoofResource());
				httpContinuations.linkToHttpContinuation(outputFlow, outputModel.getWoofHttpContinuation(),
						(link) -> link.getWoofHttpContinuation(), outputArgumentTypeName);
				procedures.linkToProcedure(outputFlow, outputModel.getWoofProcedure(),
						(link) -> link.getWoofProcedure());
			}
		}

		// Link the section outputs
		for (WoofSectionModel sectionModel : woof.getWoofSections()) {

			// Obtain the auto-wire section
			String sectionName = sectionModel.getWoofSectionName();
			OfficeSection section = sections.sections.get(sectionName);

			// Link outputs for the section
			for (WoofSectionOutputModel outputModel : sectionModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofSectionOutputName();

				// Obtain the output argument type
				String outputArgumentTypeName = outputModel.getArgumentType();
				if (CompileUtil.isBlank(outputArgumentTypeName)) {
					outputArgumentTypeName = null;
				}

				// Undertake links
				Supplier<OfficeFlowSourceNode> outputFlow = () -> section.getOfficeSectionOutput(outputName);
				sections.linkToSectionInput(outputFlow, outputModel.getWoofSectionInput(),
						(link) -> link.getWoofSectionInput());
				templates.linkToTemplate(outputFlow, outputModel.getWoofTemplate(), (link) -> link.getWoofTemplate(),
						outputArgumentTypeName);
				securities.linkToSecurity(outputFlow, outputModel.getWoofSecurity(), (link) -> link.getWoofSecurity());
				resources.linkToResource(outputFlow, outputModel.getWoofResource(), (link) -> link.getWoofResource());
				httpContinuations.linkToHttpContinuation(outputFlow, outputModel.getWoofHttpContinuation(),
						(link) -> link.getWoofHttpContinuation(), outputArgumentTypeName);
				procedures.linkToProcedure(outputFlow, outputModel.getWoofProcedure(),
						(link) -> link.getWoofProcedure());
			}
		}

		// Link the procedures
		for (WoofProcedureModel procedureModel : woof.getWoofProcedures()) {

			// Obtain the auto-wire section
			String procedureName = procedureModel.getWoofProcedureName();
			OfficeSection procedure = procedures.procedures.get(procedureName);

			// Link next for procedure
			WoofProcedureNextModel nextModel = procedureModel.getNext();
			if (nextModel != null) {

				// Obtain the output argument type
				String nextArgumentTypeName = nextModel.getArgumentType();
				if (CompileUtil.isBlank(nextArgumentTypeName)) {
					nextArgumentTypeName = null;
				}

				// Undertake links
				Supplier<OfficeFlowSourceNode> nextFlow = () -> procedure
						.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME);
				sections.linkToSectionInput(nextFlow, nextModel.getWoofSectionInput(),
						(link) -> link.getWoofSectionInput());
				templates.linkToTemplate(nextFlow, nextModel.getWoofTemplate(), (link) -> link.getWoofTemplate(),
						nextArgumentTypeName);
				securities.linkToSecurity(nextFlow, nextModel.getWoofSecurity(), (link) -> link.getWoofSecurity());
				resources.linkToResource(nextFlow, nextModel.getWoofResource(), (link) -> link.getWoofResource());
				httpContinuations.linkToHttpContinuation(nextFlow, nextModel.getWoofHttpContinuation(),
						(link) -> link.getWoofHttpContinuation(), nextArgumentTypeName);
				procedures.linkToProcedure(nextFlow, nextModel.getWoofProcedure(), (link) -> link.getWoofProcedure());
			}

			// Link outputs for procedure
			for (WoofProcedureOutputModel outputModel : procedureModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofProcedureOutputName();

				// Obtain the output argument type
				String outputArgumentTypeName = outputModel.getArgumentType();
				if (CompileUtil.isBlank(outputArgumentTypeName)) {
					outputArgumentTypeName = null;
				}

				// Undertake links
				Supplier<OfficeFlowSourceNode> outputFlow = () -> procedure.getOfficeSectionOutput(outputName);
				sections.linkToSectionInput(outputFlow, outputModel.getWoofSectionInput(),
						(link) -> link.getWoofSectionInput());
				templates.linkToTemplate(outputFlow, outputModel.getWoofTemplate(), (link) -> link.getWoofTemplate(),
						outputArgumentTypeName);
				securities.linkToSecurity(outputFlow, outputModel.getWoofSecurity(), (link) -> link.getWoofSecurity());
				resources.linkToResource(outputFlow, outputModel.getWoofResource(), (link) -> link.getWoofResource());
				httpContinuations.linkToHttpContinuation(outputFlow, outputModel.getWoofHttpContinuation(),
						(link) -> link.getWoofHttpContinuation(), outputArgumentTypeName);
				procedures.linkToProcedure(outputFlow, outputModel.getWoofProcedure(),
						(link) -> link.getWoofProcedure());
			}
		}

		// Link the security outputs
		for (WoofSecurityModel securityModel : woof.getWoofSecurities()) {

			// Obtain the HTTP security builder
			HttpSecurityBuilder securityBuilder = securities.securityBuilders.get(securityModel.getHttpSecurityName());

			// Link outputs for the security
			for (WoofSecurityOutputModel outputModel : securityModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofSecurityOutputName();

				// Obtain the output argument type
				String outputArgumentTypeName = outputModel.getArgumentType();
				if (CompileUtil.isBlank(outputArgumentTypeName)) {
					outputArgumentTypeName = null;
				}

				// Undertake links
				Supplier<OfficeFlowSourceNode> outputFlow = () -> securityBuilder.getOutput(outputName);
				sections.linkToSectionInput(outputFlow, outputModel.getWoofSectionInput(),
						(link) -> link.getWoofSectionInput());
				templates.linkToTemplate(outputFlow, outputModel.getWoofTemplate(), (link) -> link.getWoofTemplate(),
						outputArgumentTypeName);
				securities.linkToSecurity(outputFlow, outputModel.getWoofSecurity(), (link) -> link.getWoofSecurity());
				resources.linkToResource(outputFlow, outputModel.getWoofResource(), (link) -> link.getWoofResource());
				httpContinuations.linkToHttpContinuation(outputFlow, outputModel.getWoofHttpContinuation(),
						(link) -> link.getWoofHttpContinuation(), outputArgumentTypeName);
				procedures.linkToProcedure(outputFlow, outputModel.getWoofProcedure(),
						(link) -> link.getWoofProcedure());
			}
		}

		// Link the escalations
		for (WoofExceptionModel exceptionModel : woof.getWoofExceptions()) {

			// Obtain the exception type
			String exceptionClassName = exceptionModel.getClassName();

			// Undertake links
			Supplier<OfficeFlowSourceNode> handleFlow = () -> officeArchitect.addOfficeEscalation(exceptionClassName);
			sections.linkToSectionInput(handleFlow, exceptionModel.getWoofSectionInput(),
					(link) -> link.getWoofSectionInput());
			templates.linkToTemplate(handleFlow, exceptionModel.getWoofTemplate(), (link) -> link.getWoofTemplate(),
					exceptionClassName);
			securities.linkToSecurity(handleFlow, exceptionModel.getWoofSecurity(), (link) -> link.getWoofSecurity());
			resources.linkToResource(handleFlow, exceptionModel.getWoofResource(), (link) -> link.getWoofResource());
			httpContinuations.linkToHttpContinuation(handleFlow, exceptionModel.getWoofHttpContinuation(),
					(link) -> link.getWoofHttpContinuation(), exceptionClassName);
			procedures.linkToProcedure(handleFlow, exceptionModel.getWoofProcedure(),
					(link) -> link.getWoofProcedure());
		}

		// Link the starts
		int startIndex[] = new int[] { 1 };
		for (WoofStartModel startModel : woof.getWoofStarts()) {

			// Undertake links
			Supplier<OfficeFlowSourceNode> startFlow = () -> officeArchitect
					.addOfficeStart(String.valueOf(startIndex[0]++));
			sections.linkToSectionInput(startFlow, startModel.getWoofSectionInput(),
					(link) -> link.getWoofSectionInput());
			procedures.linkToProcedure(startFlow, startModel.getWoofProcedure(), (link) -> link.getWoofProcedure());
		}

		// Load the governance
		for (WoofGovernanceModel govModel : woof.getWoofGovernances()) {

			// Obtain the governance details
			String governanceName = govModel.getWoofGovernanceName();
			String governanceSourceClassName = govModel.getGovernanceSourceClassName();

			// Configure the governance
			OfficeGovernance governance = officeArchitect.addOfficeGovernance(governanceName,
					governanceSourceClassName);
			for (PropertyModel property : govModel.getProperties()) {
				governance.addProperty(property.getName(), property.getValue());
			}
			governance.enableAutoWireExtensions();

			// Configure the governance of the sections
			for (WoofGovernanceAreaModel area : govModel.getGovernanceAreas()) {

				// Govern the templates within the governance area
				for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {
					if (this.isWithinGovernanceArea(templateModel.getX(), templateModel.getY(), area)) {
						// Template within governance area so govern
						WebTemplate template = templates.templates.get(templateModel.getApplicationPath());
						template.addGovernance(governance);
					}
				}

				// Govern the sections within the governance area
				for (WoofSectionModel sectionModel : woof.getWoofSections()) {
					if (this.isWithinGovernanceArea(sectionModel.getX(), sectionModel.getY(), area)) {
						// Section within governance area so govern
						OfficeSection section = sections.sections.get(sectionModel.getWoofSectionName());
						section.addGovernance(governance);
					}
				}

				// Govern the procedures within the governance area
				for (WoofProcedureModel procedureModel : woof.getWoofProcedures()) {
					if (this.isWithinGovernanceArea(procedureModel.getX(), procedureModel.getY(), area)) {
						// Procedure within governance area so govern
						OfficeSection procedure = procedures.procedures.get(procedureModel.getWoofProcedureName());
						procedure.addGovernance(governance);
					}
				}
			}
		}
	}

	/**
	 * Connector for the {@link WoofHttpContinuationModel} instances.
	 */
	private static class HttpContinuationConnector {

		/**
		 * {@link OfficeArchitect}.
		 */
		private final OfficeArchitect officeArchitect;

		/**
		 * {@link HttpUrlContinuation} instances by application path.
		 */
		private final Map<String, HttpUrlContinuation> httpContinuations = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param woof            {@link WoofModel}
		 * @param officeArchitect {@link OfficeArchitect}.
		 * @param webArchitect    {@link WebArchitect}.
		 */
		private HttpContinuationConnector(WoofModel woof, OfficeArchitect officeArchitect, WebArchitect webArchitect) {
			this.officeArchitect = officeArchitect;

			// Configure the HTTP continuations
			for (WoofHttpContinuationModel httpContinuationModel : woof.getWoofHttpContinuations()) {

				// Obtain the HTTP continuation
				boolean isSecure = httpContinuationModel.getIsSecure();
				String applicationPath = httpContinuationModel.getApplicationPath();
				HttpUrlContinuation httpContinuation = webArchitect.getHttpInput(isSecure, applicationPath);

				// Provide possible documentation
				DocumentationModel documentation = httpContinuationModel.getDocumentation();
				if (documentation != null) {
					httpContinuation.setDocumentation(documentation.getDescription());
				}

				// Register the HTTP continuation
				httpContinuations.put(applicationPath, httpContinuation);
			}
		}

		/**
		 * Link to {@link WoofHttpContinuationModel}.
		 * 
		 * @param flowSourceFactory   {@link Supplier} of the
		 *                            {@link OfficeFlowSourceNode}.
		 * @param connectionModel     {@link ConnectionModel} to
		 *                            {@link WoofHttpContinuationModel}.
		 * @param continuationFactory Factory to extract {@link WoofTemplateModel} from
		 *                            {@link ConnectionModel}.
		 * @param parameterType       Parameter type.
		 */
		private <C extends ConnectionModel> void linkToHttpContinuation(
				Supplier<OfficeFlowSourceNode> flowSourceFactory, C connectionModel,
				Function<C, WoofHttpContinuationModel> continuationFactory, String parameterType) {

			// Determine if linking
			if (connectionModel != null) {
				WoofHttpContinuationModel httpContinuation = continuationFactory.apply(connectionModel);
				if (httpContinuation != null) {

					// Obtain the target continuation
					HttpUrlContinuation targetContinuation = this.httpContinuations
							.get(httpContinuation.getApplicationPath());

					// Link the flow to the continuation
					this.officeArchitect.link(flowSourceFactory.get(), targetContinuation.getRedirect(parameterType));
				}
			}
		}
	}

	/**
	 * Connector for the {@link WoofTemplateModel} instances.
	 */
	private static class TemplateConnector {

		/**
		 * {@link OfficeArchitect}.
		 */
		private final OfficeArchitect officeArchitect;

		/**
		 * {@link WebTemplate} instances by application path.
		 */
		private final Map<String, WebTemplate> templates = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param woof               {@link WoofModel}.
		 * @param templaterArchitect {@link WebTemplateArchitect}.
		 * @param webArchitect       {@link WebArchitect}.
		 * @param officeArchitect    {@link OfficeArchitect}.
		 * @param extensionContext   {@link OfficeExtensionContext}.
		 * @throws WoofTemplateExtensionException {@link WoofTemplateExtensionException}.
		 */
		private TemplateConnector(WoofModel woof, WebTemplateArchitect templaterArchitect, WebArchitect webArchitect,
				OfficeArchitect officeArchitect, OfficeExtensionContext extensionContext)
				throws WoofTemplateExtensionException {
			this.officeArchitect = officeArchitect;

			// Load the implicit template extension sources
			List<WoofTemplateExtensionSource> implicitExtensionSources = new LinkedList<>();
			for (WoofTemplateExtensionSource extension : extensionContext
					.loadOptionalServices(WoofTemplateExtensionSourceService.class)) {
				implicitExtensionSources.add(extension);
			}

			// Create the template extension loader
			WoofTemplateExtensionLoader extensionLoader = new WoofTemplateExtensionLoaderImpl();

			// Configure the web templates
			for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {

				// Obtain template details
				boolean isSecure = templateModel.getIsTemplateSecure();
				String applicationPath = templateModel.getApplicationPath();
				String templateLocation = templateModel.getTemplateLocation();
				String templateClassName = templateModel.getTemplateClassName();

				// Configure the template
				WebTemplate template = templaterArchitect.addTemplate(isSecure, applicationPath, templateLocation);
				if (!CompileUtil.isBlank(templateClassName)) {
					template.setLogicClass(templateClassName);
				}

				// Configure the redirect vales function
				String redirectValuesFunction = templateModel.getRedirectValuesFunction();
				if (!CompileUtil.isBlank(redirectValuesFunction)) {
					template.setRedirectValuesFunction(redirectValuesFunction);
				}

				// Configure content type for template
				String contentType = templateModel.getTemplateContentType();
				if (!CompileUtil.isBlank(contentType)) {
					template.setContentType(contentType);
				}

				// Configure charset for template
				String charset = templateModel.getTemplateCharset();
				if (!CompileUtil.isBlank(charset)) {
					template.setCharset(charset);
				}

				// Configure the link separator
				String linkSeparatorCharacter = templateModel.getLinkSeparatorCharacter();
				if (!CompileUtil.isBlank(linkSeparatorCharacter)) {
					if (linkSeparatorCharacter.length() != 1) {
						throw officeArchitect.addIssue(
								"Link separator character must only be one character '" + linkSeparatorCharacter + "'");
					}
					template.setLinkSeparatorCharacter(linkSeparatorCharacter.charAt(0));
				}

				// Configure secure for template
				for (WoofTemplateLinkModel linkModel : templateModel.getLinks()) {
					template.setLinkSecure(linkModel.getWoofTemplateLinkName(), linkModel.getIsLinkSecure());
				}

				// Configure HTTP methods for rendering
				for (WoofTemplateRenderHttpMethodModel renderMethod : templateModel.getRenderHttpMethods()) {
					template.addRenderHttpMethod(renderMethod.getWoofTemplateRenderHttpMethodName());
				}

				// Maintain reference to template by application path
				this.templates.put(applicationPath, template);

				// Configure the template explicit extensions
				Set<Class<?>> explicitTemplateExtensions = new HashSet<>();
				for (WoofTemplateExtensionModel extensionModel : templateModel.getExtensions()) {

					// Configure the extension
					String extensionSourceClassName = extensionModel.getExtensionClassName();
					try {

						// Obtain the extension source
						Class<?> extensionSourceClass = extensionContext.loadClass(extensionSourceClassName);
						WoofTemplateExtensionSource extensionSource = (WoofTemplateExtensionSource) extensionSourceClass
								.getDeclaredConstructor().newInstance();

						// Keep track of the explicit extension
						explicitTemplateExtensions.add(extensionSourceClass);

						// Create the context for the extension source
						PropertyList properties = extensionContext.createPropertyList();
						for (PropertyModel property : extensionModel.getProperties()) {
							properties.addProperty(property.getName()).setValue(property.getValue());
						}

						// Load the extension
						extensionLoader.extendTemplate(extensionSource, properties, applicationPath, template,
								officeArchitect, webArchitect, extensionContext);

					} catch (Exception ex) {
						throw new WoofTemplateExtensionException(
								"Failed to load template extension " + extensionSourceClassName, ex);
					}
				}

				// Include implicit extensions
				for (WoofTemplateExtensionSource implicitExtensionSource : implicitExtensionSources) {

					// Ignore if explicitly included (by class name)
					if (explicitTemplateExtensions.contains(implicitExtensionSource.getClass())) {
						continue;
					}

					// Extend the template with implicit extension
					extensionLoader.extendTemplate(implicitExtensionSource, extensionContext.createPropertyList(),
							applicationPath, template, officeArchitect, webArchitect, extensionContext);
				}
			}
		}

		/**
		 * Link to {@link WoofTemplateModel}.
		 * 
		 * @param flowSourceFactory {@link Supplier} of the
		 *                          {@link OfficeFlowSourceNode}.
		 * @param connectionModel   {@link ConnectionModel} to
		 *                          {@link WoofTemplateModel}.
		 * @param templateFactory   Factory to extract {@link WoofTemplateModel} from
		 *                          {@link ConnectionModel}.
		 * @param valuesType        Values type.
		 */
		private <C extends ConnectionModel> void linkToTemplate(Supplier<OfficeFlowSourceNode> flowSourceFactory,
				C connectionModel, Function<C, WoofTemplateModel> templateFactory, String valuesType) {

			// Determine if linking
			if (connectionModel != null) {
				WoofTemplateModel template = templateFactory.apply(connectionModel);
				if (template != null) {

					// Obtain the target template
					WebTemplate targetTemplate = this.templates.get(template.getApplicationPath());

					// Link the flow to the template
					this.officeArchitect.link(flowSourceFactory.get(), targetTemplate.getRender(valuesType));
				}
			}
		}
	}

	/**
	 * Connector for the {@link WoofSectionModel} instances.
	 */
	private static class SectionConnector {

		/**
		 * {@link OfficeArchitect}.
		 */
		private final OfficeArchitect officeArchitect;

		/**
		 * {@link OfficeSection} instances by name.
		 */
		private final Map<String, OfficeSection> sections = new HashMap<>();

		/**
		 * {@link WoofSectionInputModel} mapping to its {@link WoofSectionModel}.
		 */
		private final Map<WoofSectionInputModel, WoofSectionModel> inputToSection = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param woof            {@link WoofModel}.
		 * @param officeArchitect {@link OfficeArchitect}.
		 */
		private SectionConnector(WoofModel woof, OfficeArchitect officeArchitect) {
			this.officeArchitect = officeArchitect;

			// Configure the sections
			for (WoofSectionModel sectionModel : woof.getWoofSections()) {

				// Obtain the section details
				String sectionName = sectionModel.getWoofSectionName();
				String sectionSourceClassName = sectionModel.getSectionSourceClassName();
				String sectionLocation = sectionModel.getSectionLocation();

				// Configure the section
				OfficeSection section = officeArchitect.addOfficeSection(sectionName, sectionSourceClassName,
						sectionLocation);
				for (PropertyModel property : sectionModel.getProperties()) {
					section.addProperty(property.getName(), property.getValue());
				}

				// Maintain reference to section by name
				this.sections.put(sectionName, section);

				// Maintain references from inputs to section
				for (WoofSectionInputModel inputModel : sectionModel.getInputs()) {
					this.inputToSection.put(inputModel, sectionModel);
				}
			}
		}

		/**
		 * Link to {@link OfficeSectionInput}.
		 * 
		 * @param flowSourceFactory   {@link Supplier} of the
		 *                            {@link OfficeFlowSourceNode}.
		 * @param connectionModel     {@link ConnectionModel} to
		 *                            {@link OfficeSectionInputModel}.
		 * @param sectionInputFactory Factory to extract {@link OfficeSectionInputModel}
		 *                            from {@link ConnectionModel}.
		 */
		private <C extends ConnectionModel> void linkToSectionInput(Supplier<OfficeFlowSourceNode> flowSourceFactory,
				C connectionModel, Function<C, WoofSectionInputModel> sectionInputFactory) {

			// Determine if linking
			if (connectionModel != null) {
				WoofSectionInputModel sectionInput = sectionInputFactory.apply(connectionModel);
				if (sectionInput != null) {
					// Obtain target input name
					String targetInputName = sectionInput.getWoofSectionInputName();

					// Obtain the target section
					WoofSectionModel sectionModel = this.inputToSection.get(sectionInput);
					OfficeSection targetSection = this.sections.get(sectionModel.getWoofSectionName());

					// Link the flow to the section input
					this.officeArchitect.link(flowSourceFactory.get(),
							targetSection.getOfficeSectionInput(targetInputName));
				}
			}
		}
	}

	/**
	 * Connector for the {@link WoofProcedureModel} instances.
	 */
	private static class ProcedureConnector {

		/**
		 * {@link OfficeArchitect}.
		 */
		private final OfficeArchitect officeArchitect;

		/**
		 * {@link OfficeExtensionContext}.
		 */
		private final OfficeExtensionContext extensionContext;

		/**
		 * {@link Procedure} instances by name.
		 */
		private final Map<String, OfficeSection> procedures = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param woof               {@link WoofModel}.
		 * @param officeArchitect    {@link OfficeArchitect}.
		 * @param procedureArchitect {@link ProcedureArchitect}.
		 * @param extensionContext   {@link OfficeExtensionContext}.
		 */
		private ProcedureConnector(WoofModel woof, OfficeArchitect officeArchitect,
				ProcedureArchitect<OfficeSection> procedureArchitect, OfficeExtensionContext extensionContext) {
			this.officeArchitect = officeArchitect;
			this.extensionContext = extensionContext;

			// Configure the procedures
			for (WoofProcedureModel procedureModel : woof.getWoofProcedures()) {

				// Obtain the procedure details
				String sectionName = procedureModel.getWoofProcedureName();
				String resource = procedureModel.getResource();
				String sourceName = procedureModel.getSourceName();
				String procedureName = procedureModel.getProcedureName();

				// Determine if next
				WoofProcedureNextModel nextModel = procedureModel.getNext();
				boolean isNext = (nextModel != null)
						&& ((nextModel.getWoofHttpContinuation() != null) || (nextModel.getWoofProcedure() != null)
								|| (nextModel.getWoofResource() != null) || (nextModel.getWoofSectionInput() != null)
								|| (nextModel.getWoofSecurity() != null) || (nextModel.getWoofTemplate() != null));

				// Load the properties
				PropertyList properties = this.extensionContext.createPropertyList();
				for (PropertyModel propertyModel : procedureModel.getProperties()) {
					properties.addProperty(propertyModel.getName()).setValue(propertyModel.getValue());
				}

				// Configure the procedure
				OfficeSection procedure = procedureArchitect.addProcedure(sectionName, resource, sourceName,
						procedureName, isNext, properties);

				// Maintain reference to procedure by name
				String woofProcedureName = procedureModel.getWoofProcedureName();
				this.procedures.put(woofProcedureName, procedure);
			}
		}

		/**
		 * Link to {@link Procedure}.
		 * 
		 * @param flowSourceFactory {@link Supplier} of the
		 *                          {@link OfficeFlowSourceNode}.
		 * @param connectionModel   {@link ConnectionModel} to
		 *                          {@link WoofProcedureModel}.
		 * @param procedureFactory  Factory to extract procedure
		 *                          {@link WoofProcedureModel} from
		 *                          {@link ConnectionModel}.
		 */
		private <C extends ConnectionModel> void linkToProcedure(Supplier<OfficeFlowSourceNode> flowSourceFactory,
				C connectionModel, Function<C, WoofProcedureModel> procedureFactory) {

			// Determine if linking
			if (connectionModel != null) {
				WoofProcedureModel procedure = procedureFactory.apply(connectionModel);
				if (procedure != null) {
					// Obtain target procedure name
					String targetProcedureName = procedure.getWoofProcedureName();

					// Obtain the target procedure
					OfficeSection targetProcedure = this.procedures.get(targetProcedureName);

					// Link the flow to the procedure
					this.officeArchitect.link(flowSourceFactory.get(),
							targetProcedure.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
				}
			}
		}
	}

	/**
	 * Connector for the {@link WoofSecurityModel} instances.
	 */
	private static class SecurityConnector {

		/**
		 * {@link OfficeArchitect}.
		 */
		private final OfficeArchitect officeArchitect;

		/**
		 * {@link HttpSecurityBuilder} instances by their names.
		 */
		private final Map<String, HttpSecurityBuilder> securityBuilders = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param woof              {@link WoofModel}.
		 * @param officeArchitect   {@link OfficeArchitect}.
		 * @param securityArchitect {@link HttpSecurityArchitect}.
		 */
		private SecurityConnector(WoofModel woof, OfficeArchitect officeArchitect,
				HttpSecurityArchitect securityArchitect) {
			this.officeArchitect = officeArchitect;

			// Configure the Security
			for (WoofSecurityModel securityModel : woof.getWoofSecurities()) {

				// Obtain the security details
				String httpSecurityName = securityModel.getHttpSecurityName();
				String httpSecuritySourceClassName = securityModel.getHttpSecuritySourceClassName();
				long timeout = securityModel.getTimeout();

				// Add the HTTP security
				HttpSecurityBuilder securityBuilder = securityArchitect.addHttpSecurity(httpSecurityName,
						httpSecuritySourceClassName);
				if (timeout > 0) {
					securityBuilder.setTimeout(timeout);
				}
				for (PropertyModel property : securityModel.getProperties()) {
					securityBuilder.addProperty(property.getName(), property.getValue());
				}
				for (WoofSecurityContentTypeModel contentType : securityModel.getContentTypes()) {
					securityBuilder.addContentType(contentType.getContentType());
				}

				// Register the HTTP security
				this.securityBuilders.put(httpSecurityName, securityBuilder);
			}
		}

		/**
		 * Link to {@link WoofSecurityModel}.
		 * 
		 * @param flowSourceFactory {@link Supplier} of the
		 *                          {@link OfficeFlowSourceNode}.
		 * @param connectionModel   {@link ConnectionModel} to
		 *                          {@link WoofSecurityModel}.
		 * @param securityFactory   Factory to extract {@link WoofSecurityModel} from
		 *                          {@link ConnectionModel}.
		 */
		private <C extends ConnectionModel> void linkToSecurity(Supplier<OfficeFlowSourceNode> flowSourceFactory,
				C connectionModel, Function<C, WoofSecurityModel> securityFactory) {

			// Determine if linking
			if (connectionModel != null) {
				WoofSecurityModel security = securityFactory.apply(connectionModel);
				if (security != null) {

					// Obtain the target security
					HttpSecurityBuilder targetSecurity = this.securityBuilders.get(security.getHttpSecurityName());

					// Link the flow to the security
					this.officeArchitect.link(flowSourceFactory.get(), targetSecurity.getAuthenticateInput());
				}
			}
		}
	}

	/**
	 * Connector for the {@link WoofResourceModel} instances.
	 */
	private static class ResourceConnector {

		/**
		 * {@link OfficeArchitect}.
		 */
		private final OfficeArchitect officeArchitect;

		/**
		 * {@link OfficeFlowSinkNode} instances to their resource path.
		 */
		private final Map<String, OfficeFlowSinkNode> resources = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param woof              {@link WoofModel}.
		 * @param officeArchitect   {@link OfficeArchitect}.
		 * @param resourceArchitect {@link HttpResourceArchitect}.
		 */
		private ResourceConnector(WoofModel woof, OfficeArchitect officeArchitect,
				HttpResourceArchitect resourceArchitect) {
			this.officeArchitect = officeArchitect;

			// Configure the resources
			for (WoofResourceModel resourceModel : woof.getWoofResources()) {

				// Add the resource
				String resourcePath = resourceModel.getResourcePath();
				OfficeFlowSinkNode resource = resourceArchitect.getResource(resourcePath);

				// Register the resource
				this.resources.put(resourcePath, resource);
			}
		}

		/**
		 * Link to {@link WoofResourceModel}.
		 * 
		 * @param flowSourceFactory {@link Supplier} of the
		 *                          {@link OfficeFlowSourceNode}.
		 * @param connectionModel   {@link ConnectionModel} to
		 *                          {@link WoofResourceModel}.
		 * @param resourceFactory   Factory to extract {@link WoofResourceModel} from
		 *                          {@link ConnectionModel}.
		 */
		private <C extends ConnectionModel> void linkToResource(Supplier<OfficeFlowSourceNode> flowSourceFactory,
				C connectionModel, Function<C, WoofResourceModel> resourceFactory) {

			// Determine if linking
			if (connectionModel != null) {
				WoofResourceModel resource = resourceFactory.apply(connectionModel);
				if (resource != null) {

					// Obtain the target resource
					OfficeFlowSinkNode targetResource = this.resources.get(resource.getResourcePath());

					// Link the flow to the resource
					this.officeArchitect.link(flowSourceFactory.get(), targetResource);
				}
			}
		}
	}

	/**
	 * Indicates if the position is within the area.
	 * 
	 * @param posX Position X location.
	 * @param posY Position Y location.
	 * @param area {@link WoofGovernanceAreaModel}.
	 * @return <code>true</code> if position within the
	 *         {@link WoofGovernanceAreaModel}.
	 */
	private boolean isWithinGovernanceArea(int posX, int posY, WoofGovernanceAreaModel area) {

		// Obtain left and right X locations
		int left = area.getX();
		int right = area.getX() + area.getWidth();
		if (left > right) {
			// Swap around to have correct left right orientation
			int temp = left;
			left = right;
			right = temp;
		}

		// Obtain top and bottom Y locations
		int top = area.getY();
		int bottom = area.getY() + area.getHeight();
		if (top > bottom) {
			// Swap around to have correct top bottom orientation
			int temp = top;
			top = bottom;
			bottom = temp;
		}

		// Return whether position within area
		return ((left <= posX) && (posX <= right)) && ((top <= posY) && (posY <= bottom));
	}

}
