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
package net.officefloor.woof;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.compile.OfficeFloorCompiler;
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
import net.officefloor.woof.model.woof.PropertyModel;
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofHttpInputModel;
import net.officefloor.woof.model.woof.WoofModel;
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
	 * @param repository
	 *            {@link WoofRepository}.
	 */
	public WoofLoaderImpl(WoofRepository repository) {
		this.repository = repository;
	}

	/*
	 * ======================= WoofLoader ===========================
	 */

	@Override
	public void loadWoofConfiguration(WoofLoaderContext context) throws Exception {

		// Load the WoOF model
		WoofModel woof = new WoofModel();
		this.repository.retrieveWoof(woof, context.getConfiguration());

		// Obtain the various architects
		OfficeArchitect officeArchitect = context.getOfficeArchitect();
		WebArchitect webArchitect = context.getWebArchitect();
		HttpSecurityArchitect securityArchitect = context.getHttpSecurityArchitect();
		WebTemplateArchitect templaterArchitect = context.getWebTemplater();
		HttpResourceArchitect resourceArchitect = context.getHttpResourceArchitect();

		// Obtain the context
		OfficeExtensionContext extensionContext = context.getOfficeExtensionContext();

		// Configure the templates
		TemplateConnector templates = new TemplateConnector(woof, templaterArchitect, webArchitect, officeArchitect,
				extensionContext);

		// Configure the Sections
		SectionConnector sections = new SectionConnector(woof, officeArchitect);

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
			sections.linkToSectionInput(() -> httpContinuation.getInput(), httpContinuationModel.getWoofSectionInput(),
					(link) -> link.getWoofSectionInput());
			templates.linkToTemplate(() -> httpContinuation.getInput(), httpContinuationModel.getWoofTemplate(),
					(link) -> link.getWoofTemplate(), null);
			securities.linkToSecurity(() -> httpContinuation.getInput(), httpContinuationModel.getWoofSecurity(),
					(link) -> link.getWoofSecurity());
			resources.linkToResource(() -> httpContinuation.getInput(), httpContinuationModel.getWoofResource(),
					(link) -> link.getWoofResource());
			httpContinuations.linkToHttpContinuation(() -> httpContinuation.getInput(),
					httpContinuationModel.getWoofRedirect(), (link) -> link.getWoofRedirect(), null);
		}

		// Link the HTTP inputs
		for (WoofHttpInputModel httpInputModel : woof.getWoofHttpInputs()) {

			// Obtain the HTTP input
			boolean isSecure = httpInputModel.getIsSecure();
			String httpMethod = httpInputModel.getHttpMethod();
			String applicationPath = httpInputModel.getApplicationPath();
			HttpInput httpInput = webArchitect.getHttpInput(isSecure, httpMethod, applicationPath);

			// Undertake links
			sections.linkToSectionInput(() -> httpInput.getInput(), httpInputModel.getWoofSectionInput(),
					(link) -> link.getWoofSectionInput());
			templates.linkToTemplate(() -> httpInput.getInput(), httpInputModel.getWoofTemplate(),
					(link) -> link.getWoofTemplate(), null);
			securities.linkToSecurity(() -> httpInput.getInput(), httpInputModel.getWoofSecurity(),
					(link) -> link.getWoofSecurity());
			resources.linkToResource(() -> httpInput.getInput(), httpInputModel.getWoofResource(),
					(link) -> link.getWoofResource());
			httpContinuations.linkToHttpContinuation(() -> httpInput.getInput(),
					httpInputModel.getWoofHttpContinuation(), (link) -> link.getWoofHttpContinuation(), null);
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
				sections.linkToSectionInput(() -> template.getOutput(outputName), outputModel.getWoofSectionInput(),
						(link) -> link.getWoofSectionInput());
				templates.linkToTemplate(() -> template.getOutput(outputName), outputModel.getWoofTemplate(),
						(link) -> link.getWoofTemplate(), outputArgumentTypeName);
				securities.linkToSecurity(() -> template.getOutput(outputName), outputModel.getWoofSecurity(),
						(link) -> link.getWoofSecurity());
				resources.linkToResource(() -> template.getOutput(outputName), outputModel.getWoofResource(),
						(link) -> link.getWoofResource());
				httpContinuations.linkToHttpContinuation(() -> template.getOutput(outputName),
						outputModel.getWoofHttpContinuation(), (link) -> link.getWoofHttpContinuation(),
						outputArgumentTypeName);
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
				sections.linkToSectionInput(() -> section.getOfficeSectionOutput(outputName),
						outputModel.getWoofSectionInput(), (link) -> link.getWoofSectionInput());
				templates.linkToTemplate(() -> section.getOfficeSectionOutput(outputName),
						outputModel.getWoofTemplate(), (link) -> link.getWoofTemplate(), outputArgumentTypeName);
				securities.linkToSecurity(() -> section.getOfficeSectionOutput(outputName),
						outputModel.getWoofSecurity(), (link) -> link.getWoofSecurity());
				resources.linkToResource(() -> section.getOfficeSectionOutput(outputName),
						outputModel.getWoofResource(), (link) -> link.getWoofResource());
				httpContinuations.linkToHttpContinuation(() -> section.getOfficeSectionOutput(outputName),
						outputModel.getWoofHttpContinuation(), (link) -> link.getWoofHttpContinuation(),
						outputArgumentTypeName);
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
				sections.linkToSectionInput(() -> securityBuilder.getOutput(outputName),
						outputModel.getWoofSectionInput(), (link) -> link.getWoofSectionInput());
				templates.linkToTemplate(() -> securityBuilder.getOutput(outputName), outputModel.getWoofTemplate(),
						(link) -> link.getWoofTemplate(), outputArgumentTypeName);
				securities.linkToSecurity(() -> securityBuilder.getOutput(outputName), outputModel.getWoofSecurity(),
						(link) -> link.getWoofSecurity());
				resources.linkToResource(() -> securityBuilder.getOutput(outputName), outputModel.getWoofResource(),
						(link) -> link.getWoofResource());
				httpContinuations.linkToHttpContinuation(() -> securityBuilder.getOutput(outputName),
						outputModel.getWoofHttpContinuation(), (link) -> link.getWoofHttpContinuation(),
						outputArgumentTypeName);
			}
		}

		// Link the escalations
		for (WoofExceptionModel exceptionModel : woof.getWoofExceptions()) {

			// Obtain the exception type
			String exceptionClassName = exceptionModel.getClassName();

			// Undertake links
			sections.linkToSectionInput(() -> officeArchitect.addOfficeEscalation(exceptionClassName),
					exceptionModel.getWoofSectionInput(), (link) -> link.getWoofSectionInput());
			templates.linkToTemplate(() -> officeArchitect.addOfficeEscalation(exceptionClassName),
					exceptionModel.getWoofTemplate(), (link) -> link.getWoofTemplate(), exceptionClassName);
			securities.linkToSecurity(() -> officeArchitect.addOfficeEscalation(exceptionClassName),
					exceptionModel.getWoofSecurity(), (link) -> link.getWoofSecurity());
			resources.linkToResource(() -> officeArchitect.addOfficeEscalation(exceptionClassName),
					exceptionModel.getWoofResource(), (link) -> link.getWoofResource());
			httpContinuations.linkToHttpContinuation(() -> officeArchitect.addOfficeEscalation(exceptionClassName),
					exceptionModel.getWoofHttpContinuation(), (link) -> link.getWoofHttpContinuation(),
					exceptionClassName);
		}

		// Link the starts
		int startIndex[] = new int[] { 1 };
		for (WoofStartModel startModel : woof.getWoofStarts()) {
			sections.linkToSectionInput(() -> officeArchitect.addOfficeStart(String.valueOf(startIndex[0]++)),
					startModel.getWoofSectionInput(), (link) -> link.getWoofSectionInput());
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
		 * @param woof
		 *            {@link WoofModel}
		 * @param officeArchitect
		 *            {@link OfficeArchitect}.
		 * @param webArchitect
		 *            {@link WebArchitect}.
		 */
		private HttpContinuationConnector(WoofModel woof, OfficeArchitect officeArchitect, WebArchitect webArchitect) {
			this.officeArchitect = officeArchitect;

			// Configure the HTTP continuations
			for (WoofHttpContinuationModel httpContinuationModel : woof.getWoofHttpContinuations()) {

				// Obtain the HTTP continuation
				boolean isSecure = httpContinuationModel.getIsSecure();
				String applicationPath = httpContinuationModel.getApplicationPath();
				HttpUrlContinuation httpContinuation = webArchitect.getHttpInput(isSecure, applicationPath);

				// Register the HTTP continuation
				httpContinuations.put(applicationPath, httpContinuation);
			}
		}

		/**
		 * Link to {@link WoofHttpContinuationModel}.
		 * 
		 * @param flowSourceFactory
		 *            {@link Supplier} of the {@link OfficeFlowSourceNode}.
		 * @param connectionModel
		 *            {@link ConnectionModel} to
		 *            {@link WoofHttpContinuationModel}.
		 * @param continuationFactory
		 *            Factory to extract {@link WoofTemplateModel} from
		 *            {@link ConnectionModel}.
		 * @param parameterType
		 *            Parameter type.
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
		 * @param woof
		 *            {@link WoofModel}.
		 * @param templaterArchitect
		 *            {@link WebTemplateArchitect}.
		 * @param webArchitect
		 *            {@link WebArchitect}.
		 * @param officeArchitect
		 *            {@link OfficeArchitect}.
		 * @param extensionContext
		 *            {@link OfficeExtensionContext}.
		 * @throws WoofTemplateExtensionException
		 *             {@link WoofTemplateExtensionException}.
		 */
		private TemplateConnector(WoofModel woof, WebTemplateArchitect templaterArchitect, WebArchitect webArchitect,
				OfficeArchitect officeArchitect, OfficeExtensionContext extensionContext)
				throws WoofTemplateExtensionException {
			this.officeArchitect = officeArchitect;

			// Obtain the class loader
			ClassLoader classLoader = extensionContext.getClassLoader();

			// Load the implicit template extension sources
			Set<String> implicitExtensionSources = new HashSet<String>();
			ServiceLoader<WoofTemplateExtensionSourceService> extensionServiceLoader = ServiceLoader
					.load(WoofTemplateExtensionSourceService.class, classLoader);
			Iterator<WoofTemplateExtensionSourceService> extensionIterator = extensionServiceLoader.iterator();
			while (extensionIterator.hasNext()) {

				// Obtain the extension service
				WoofTemplateExtensionSourceService<?> extensionService;
				try {
					extensionService = extensionIterator.next();
				} catch (ServiceConfigurationError ex) {
					// Issue that service not available
					officeArchitect.addIssue(WoofTemplateExtensionSource.class.getSimpleName()
							+ " configuration failure: " + ex.getMessage(), ex);

					// Carry on to next service (likely not on class path)
					continue;
				}

				// Register the extension if implicit
				try {
					// Determine if extension is implicit
					boolean isImplicitExtension = extensionService.isImplicitExtension();
					if (!isImplicitExtension) {
						continue; // ignore as only include implicit
					}

					// Obtain instance of the extension source
					String extensionSourceClassName = extensionService.getWoofTemplateExtensionSourceClass().getName();

					// Register the extension source
					implicitExtensionSources.add(extensionSourceClassName);

				} catch (Throwable ex) {
					// Issue that error with service
					officeArchitect.addIssue("Failed loading extension from " + extensionService.getClass().getName()
							+ " : " + ex.getMessage(), ex);

					// Carry on to next service
					continue;
				}
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

				// Keep track of the explicit HTTP template extensions
				Set<String> explicitTemplateExtensions = new HashSet<String>();

				// Configure the HTTP template extensions
				for (WoofTemplateExtensionModel extensionModel : templateModel.getExtensions()) {

					// Obtain the extension source class name
					String extensionSourceClassName = extensionModel.getExtensionClassName();

					// Keep track of the explicit extension
					explicitTemplateExtensions.add(extensionSourceClassName);

					// Create the context for the extension source
					PropertyList properties = OfficeFloorCompiler.newPropertyList();
					for (PropertyModel property : extensionModel.getProperties()) {
						properties.addProperty(property.getName()).setValue(property.getValue());
					}

					// Load the extension
					extensionLoader.extendTemplate(extensionSourceClassName, properties, applicationPath, template,
							officeArchitect, webArchitect, extensionContext);
				}

				// Include implicit extensions (in deterministic order)
				String[] implicitExtensionSourceClassNames = implicitExtensionSources.toArray(new String[0]);
				Arrays.sort(implicitExtensionSourceClassNames, String.CASE_INSENSITIVE_ORDER);
				for (String implicitExtensionSourceClassName : implicitExtensionSourceClassNames) {

					// Ignore if explicitly included (by class name)
					if (explicitTemplateExtensions.contains(implicitExtensionSourceClassName)) {
						continue;
					}

					// Extend the template with implicit extension
					extensionLoader.extendTemplate(implicitExtensionSourceClassName,
							extensionContext.createPropertyList(), applicationPath, template, officeArchitect,
							webArchitect, extensionContext);
				}
			}
		}

		/**
		 * Link to {@link WoofTemplateModel}.
		 * 
		 * @param flowSourceFactory
		 *            {@link Supplier} of the {@link OfficeFlowSourceNode}.
		 * @param connectionModel
		 *            {@link ConnectionModel} to {@link WoofTemplateModel}.
		 * @param templateFactory
		 *            Factory to extract {@link WoofTemplateModel} from
		 *            {@link ConnectionModel}.
		 * @param valuesType
		 *            Values type.
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
		 * {@link WoofSectionInputModel} mapping to its
		 * {@link WoofSectionModel}.
		 */
		private final Map<WoofSectionInputModel, WoofSectionModel> inputToSection = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param woof
		 *            {@link WoofModel}.
		 * @param officeArchitect
		 *            {@link OfficeArchitect}.
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
		 * @param flowSourceFactory
		 *            {@link Supplier} of the {@link OfficeFlowSourceNode}.
		 * @param connectionModel
		 *            {@link ConnectionModel} to
		 *            {@link OfficeSectionInputModel}.
		 * @param sectionInputFactory
		 *            Factory to extract {@link OfficeSectionInputModel} from
		 *            {@link ConnectionModel}.
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
		 * @param woof
		 *            {@link WoofModel}.
		 * @param officeArchitect
		 *            {@link OfficeArchitect}.
		 * @param securityArchitect
		 *            {@link HttpSecurityArchitect}.
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
		 * @param flowSourceFactory
		 *            {@link Supplier} of the {@link OfficeFlowSourceNode}.
		 * @param connectionModel
		 *            {@link ConnectionModel} to {@link WoofSecurityModel}.
		 * @param securityFactory
		 *            Factory to extract {@link WoofSecurityModel} from
		 *            {@link ConnectionModel}.
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
		 * @param woof
		 *            {@link WoofModel}.
		 * @param officeArchitect
		 *            {@link OfficeArchitect}.
		 * @param resourceArchitect
		 *            {@link HttpResourceArchitect}.
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
		 * @param flowSourceFactory
		 *            {@link Supplier} of the {@link OfficeFlowSourceNode}.
		 * @param connectionModel
		 *            {@link ConnectionModel} to {@link WoofResourceModel}.
		 * @param resourceFactory
		 *            Factory to extract {@link WoofResourceModel} from
		 *            {@link ConnectionModel}.
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
	 * @param posX
	 *            Position X location.
	 * @param posY
	 *            Position Y location.
	 * @param area
	 *            {@link WoofGovernanceAreaModel}.
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