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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplater;
import net.officefloor.web.template.build.WebTemplaterEmployer;
import net.officefloor.woof.WoofLoader;
import net.officefloor.woof.WoofLoaderContext;
import net.officefloor.woof.model.woof.PropertyModel;
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofRepository;
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
import net.officefloor.woof.model.woof.WoofTemplateToSuperWoofTemplateModel;
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void loadWoofConfiguration(WoofLoaderContext context) throws Exception {

		// Load the WoOF model
		WoofModel woof = new WoofModel();
		this.repository.retrieveWoof(woof, context.getConfiguration());

		// Obtain the office and web architect
		OfficeArchitect officeArchitect = context.getOfficeArchitect();
		OfficeExtensionContext extensionContext = context.getOfficeExtensionContext();
		WebArchitect webArchitect = WebArchitectEmployer.employWebArchitect(officeArchitect, extensionContext);
		HttpSecurityArchitect securityArchitect = HttpSecurityArchitectEmployer
				.employHttpSecurityArchitect(webArchitect, officeArchitect, extensionContext);
		WebTemplater templaterArchitect = WebTemplaterEmployer.employWebTemplater(webArchitect, officeArchitect,
				extensionContext);
		HttpResourceArchitect resourceArchitect = HttpResourceArchitectEmployer
				.employHttpResourceArchitect(webArchitect, securityArchitect, officeArchitect, extensionContext);

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
				officeArchitect.addIssue(WoofTemplateExtensionSource.class.getSimpleName() + " configuration failure: "
						+ ex.getMessage(), ex);

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
		Map<String, WebTemplate> templates = new HashMap<String, WebTemplate>();
		for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {

			// Obtain template details
			String applicationPath = templateModel.getApplicationPath();
			String templateLocation = templateModel.getTemplateLocation();
			String templateClassName = templateModel.getTemplateClassName();

			// Obtain the template logic class (if provided)
			Class<?> templateLogicClass = CompileUtil.isBlank(templateClassName) ? null
					: classLoader.loadClass(templateClassName);

			// Configure the template
			WebTemplate template = templaterArchitect.addTemplate(applicationPath, templateLocation);
			if (!CompileUtil.isBlank(templateClassName)) {
				template.setLogicClass(templateLogicClass);
			}

			// Configure content type for template
			String contentType = templateModel.getTemplateContentType();
			if (!CompileUtil.isBlank(contentType)) {
				template.setContentType(contentType);
			}

			// Configure secure for template
			template.setSecure(templateModel.getIsTemplateSecure());
			for (WoofTemplateLinkModel linkModel : templateModel.getLinks()) {
				template.setLinkSecure(linkModel.getWoofTemplateLinkName(), linkModel.getIsLinkSecure());
			}

			// Configure HTTP methods for rendering
			for (String renderMethodName : templateModel.getRenderHttpMethods()) {
				template.addRenderMethod(HttpMethod.getHttpMethod(renderMethodName));
			}

			// Maintain reference to template by application path
			templates.put(applicationPath, template);

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
				extensionLoader.extendTemplate(implicitExtensionSourceClassName, OfficeFloorCompiler.newPropertyList(),
						applicationPath, template, officeArchitect, webArchitect, extensionContext);
			}
		}

		// Configure the sections
		Map<String, OfficeSection> sections = new HashMap<String, OfficeSection>();
		Map<WoofSectionInputModel, WoofSectionModel> inputToSection = new HashMap<WoofSectionInputModel, WoofSectionModel>();
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
			sections.put(sectionName, section);

			// Maintain references from inputs to section
			for (WoofSectionInputModel inputModel : sectionModel.getInputs()) {
				inputToSection.put(inputModel, sectionModel);
			}
		}

		// Configure the Security
		Map<String, HttpSecurityBuilder> securityBuilders = new HashMap<>();
		for (WoofSecurityModel securityModel : woof.getWoofSecurities()) {

			// Obtain the security details
			String httpSecurityName = securityModel.getHttpSecurityName();
			String httpSecuritySourceClassName = securityModel.getHttpSecuritySourceClassName();
			long timeout = securityModel.getTimeout();

			// Obtain the HTTP Security Source class
			Class<? extends HttpSecuritySource<?, ?, ?, ?, ?>> httpSecuritySourceClass = (Class<? extends HttpSecuritySource<?, ?, ?, ?, ?>>) extensionContext
					.loadClass(httpSecuritySourceClassName);

			// Add the HTTP security
			HttpSecurityBuilder securityBuilder = securityArchitect.addHttpSecurity(httpSecurityName,
					httpSecuritySourceClass);
			securityBuilder.setTimeout(timeout);
			for (PropertyModel property : securityModel.getProperties()) {
				securityBuilder.addProperty(property.getName(), property.getValue());
			}
			for (String contentType : securityModel.getContentTypes()) {
				securityBuilder.addContentType(contentType);
			}

			// Register the HTTP security
			securityBuilders.put(httpSecurityName, securityBuilder);
		}

		// Link the template outputs
		for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {

			// Obtain the auto-wire template
			String applicationPath = templateModel.getApplicationPath();
			WebTemplate template = templates.get(applicationPath);

			// Provide link configuration inheritance
			WoofTemplateToSuperWoofTemplateModel superTemplateLink = templateModel.getSuperWoofTemplate();
			if (superTemplateLink != null) {
				WebTemplate superTemplate = templates.get(superTemplateLink.getSuperWoofTemplateApplicationPath());
				template.setSuperTemplate(superTemplate);
			}

			// Link outputs for the template
			for (WoofTemplateOutputModel outputModel : templateModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofTemplateOutputName();

				// Link potential section input
				WoofTemplateOutputToWoofSectionInputModel sectionLink = outputModel.getWoofSectionInput();
				if (sectionLink != null) {
					WoofSectionInputModel sectionInput = sectionLink.getWoofSectionInput();
					if (sectionInput != null) {

						// Obtain target input name
						String targetInputName = sectionInput.getWoofSectionInputName();

						// Obtain the target section
						WoofSectionModel sectionModel = inputToSection.get(sectionInput);
						OfficeSection targetSection = sections.get(sectionModel.getWoofSectionName());

						// Link template output to section input
						officeArchitect.link(template.getOutput(outputName),
								targetSection.getOfficeSectionInput(targetInputName));
					}
				}

				// Link potential template
				WoofTemplateOutputToWoofTemplateModel templateLink = outputModel.getWoofTemplate();
				if (templateLink != null) {
					WoofTemplateModel targetTemplateModel = templateLink.getWoofTemplate();
					if (targetTemplateModel != null) {
						WebTemplate targetTemplate = templates.get(targetTemplateModel.getApplicationPath());

						// TODO obtain template output argument type
						Class<?> templateOutputArgumentType = null;

						targetTemplate.link(template.getOutput(outputName), templateOutputArgumentType);
					}
				}

				// Link security inputs
				WoofTemplateOutputToWoofSecurityModel accessLink = outputModel.getWoofSecurity();
				if (accessLink != null) {
					WoofSecurityModel targetSecurityModel = accessLink.getWoofSecurity();
					if (targetSecurityModel != null) {
						HttpSecurityBuilder securityBuilder = securityBuilders
								.get(targetSecurityModel.getHttpSecurityName());
						officeArchitect.link(template.getOutput(outputName), securityBuilder.getAuthenticateInput());
					}
				}

				// Link potential resource
				WoofTemplateOutputToWoofResourceModel resourceLink = outputModel.getWoofResource();
				if (resourceLink != null) {
					WoofResourceModel resourceModel = resourceLink.getWoofResource();
					if (resourceModel != null) {
						resourceArchitect.link(template.getOutput(outputName), resourceModel.getResourcePath());
					}
				}
			}
		}

		// Link the section outputs
		for (WoofSectionModel sectionModel : woof.getWoofSections()) {

			// Obtain the auto-wire section
			String sectionName = sectionModel.getWoofSectionName();
			OfficeSection section = sections.get(sectionName);

			// Link outputs for the section
			for (WoofSectionOutputModel outputModel : sectionModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofSectionOutputName();

				// Link potential section input
				WoofSectionOutputToWoofSectionInputModel sectionLink = outputModel.getWoofSectionInput();
				if (sectionLink != null) {
					WoofSectionInputModel sectionInput = sectionLink.getWoofSectionInput();
					if (sectionInput != null) {

						// Obtain target input name
						String targetInputName = sectionInput.getWoofSectionInputName();

						// Obtain the target section
						WoofSectionModel targetSectionModel = inputToSection.get(sectionInput);
						OfficeSection targetSection = sections.get(targetSectionModel.getWoofSectionName());

						// Link section output to section input
						officeArchitect.link(section.getOfficeSectionOutput(outputName),
								targetSection.getOfficeSectionInput(targetInputName));
					}
				}

				// Link potential template
				WoofSectionOutputToWoofTemplateModel templateLink = outputModel.getWoofTemplate();
				if (templateLink != null) {
					WoofTemplateModel targetTemplateModel = templateLink.getWoofTemplate();
					if (targetTemplateModel != null) {
						WebTemplate targetTemplate = templates.get(targetTemplateModel.getApplicationPath());

						// TODO obtain template output argument type
						Class<?> templateOutputArgumentType = null;

						targetTemplate.link(section.getOfficeSectionOutput(outputName), templateOutputArgumentType);
					}
				}

				// Link potential access input
				WoofSectionOutputToWoofSecurityModel securityLink = outputModel.getWoofSecurity();
				if (securityLink != null) {
					WoofSecurityModel targetSecurityModel = securityLink.getWoofSecurity();
					if (targetSecurityModel != null) {
						HttpSecurityBuilder securityBuilder = securityBuilders
								.get(targetSecurityModel.getHttpSecurityName());
						officeArchitect.link(section.getOfficeSectionOutput(outputName),
								securityBuilder.getAuthenticateInput());
					}
				}

				// Link potential resource
				WoofSectionOutputToWoofResourceModel resourceLink = outputModel.getWoofResource();
				if (resourceLink != null) {
					WoofResourceModel resourceModel = resourceLink.getWoofResource();
					if (resourceModel != null) {
						resourceArchitect.link(section.getOfficeSectionOutput(outputName),
								resourceModel.getResourcePath());
					}
				}
			}
		}

		// Link the security outputs
		for (WoofSecurityModel securityModel : woof.getWoofSecurities()) {

			// Obtain the HTTP security builder
			HttpSecurityBuilder securityBuilder = securityBuilders.get(securityModel.getHttpSecurityName());

			// Link outputs for the security
			for (WoofSecurityOutputModel outputModel : securityModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofSecurityOutputName();

				// Link potential section input
				WoofSecurityOutputToWoofSectionInputModel sectionLink = outputModel.getWoofSectionInput();
				if (sectionLink != null) {
					WoofSectionInputModel sectionInput = sectionLink.getWoofSectionInput();
					if (sectionInput != null) {

						// Obtain target input name
						String targetInputName = sectionInput.getWoofSectionInputName();

						// Obtain the target section
						WoofSectionModel targetSectionModel = inputToSection.get(sectionInput);
						OfficeSection targetSection = sections.get(targetSectionModel.getWoofSectionName());

						// Link security output to section input
						officeArchitect.link(securityBuilder.getOutput(outputName),
								targetSection.getOfficeSectionInput(targetInputName));
					}
				}

				// Link potential template
				WoofSecurityOutputToWoofTemplateModel templateLink = outputModel.getWoofTemplate();
				if (templateLink != null) {
					WoofTemplateModel targetTemplateModel = templateLink.getWoofTemplate();
					if (targetTemplateModel != null) {
						WebTemplate targetTemplate = templates.get(targetTemplateModel.getApplicationPath());

						// TODO obtain template output argument type
						Class<?> templateOutputArgumentType = null;

						targetTemplate.link(securityBuilder.getOutput(outputName), templateOutputArgumentType);
					}
				}

				// Link potential resource
				WoofSecurityOutputToWoofResourceModel resourceLink = outputModel.getWoofResource();
				if (resourceLink != null) {
					WoofResourceModel resourceModel = resourceLink.getWoofResource();
					if (resourceModel != null) {
						resourceArchitect.link(securityBuilder.getOutput(outputName), resourceModel.getResourcePath());
					}
				}
			}
		}

		// Link the escalations
		for (WoofExceptionModel exceptionModel : woof.getWoofExceptions()) {

			// Obtain the exception type
			String exceptionClassName = exceptionModel.getClassName();
			Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) classLoader
					.loadClass(exceptionClassName);

			// Link potential section input
			WoofExceptionToWoofSectionInputModel sectionLink = exceptionModel.getWoofSectionInput();
			if (sectionLink != null) {
				WoofSectionInputModel sectionInput = sectionLink.getWoofSectionInput();
				if (sectionInput != null) {

					// Obtain target input name
					String targetInputName = sectionInput.getWoofSectionInputName();

					// Obtain the target section
					WoofSectionModel targetSectionModel = inputToSection.get(sectionInput);
					OfficeSection targetSection = sections.get(targetSectionModel.getWoofSectionName());

					// Link escalation handling to section input
					OfficeEscalation escalation = officeArchitect.addOfficeEscalation(exceptionType.getName());
					officeArchitect.link(escalation, targetSection.getOfficeSectionInput(targetInputName));
				}
			}

			// Link potential template
			WoofExceptionToWoofTemplateModel templateLink = exceptionModel.getWoofTemplate();
			if (templateLink != null) {
				WoofTemplateModel targetTemplateModel = templateLink.getWoofTemplate();
				if (targetTemplateModel != null) {
					WebTemplate targetTemplate = templates.get(targetTemplateModel.getApplicationPath());

					// Link escalation handling to template
					OfficeEscalation escalation = officeArchitect.addOfficeEscalation(exceptionType.getName());
					targetTemplate.link(escalation, exceptionType);
				}
			}

			// Link potential resource
			WoofExceptionToWoofResourceModel resourceLink = exceptionModel.getWoofResource();
			if (resourceLink != null) {
				WoofResourceModel resourceModel = resourceLink.getWoofResource();
				if (resourceModel != null) {
					// Link escalation handling to template
					OfficeEscalation escalation = officeArchitect.addOfficeEscalation(exceptionType.getName());
					resourceArchitect.link(escalation, resourceModel.getResourcePath());
				}
			}
		}

		// Link the starts
		int startIndex = 1;
		for (WoofStartModel startModel : woof.getWoofStarts()) {

			// Link potential section input
			WoofStartToWoofSectionInputModel sectionLink = startModel.getWoofSectionInput();
			if (sectionLink != null) {
				WoofSectionInputModel sectionInput = sectionLink.getWoofSectionInput();
				if (sectionInput != null) {

					// Obtain target input name
					String targetInputName = sectionInput.getWoofSectionInputName();

					// Obtain the target section
					WoofSectionModel targetSectionModel = inputToSection.get(sectionInput);
					OfficeSection targetSection = sections.get(targetSectionModel.getWoofSectionName());

					// Add start-up flow
					OfficeStart start = officeArchitect.addOfficeStart(String.valueOf(startIndex++));
					officeArchitect.link(start, targetSection.getOfficeSectionInput(targetInputName));
				}
			}
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
						WebTemplate template = templates.get(templateModel.getApplicationPath());
						template.addGovernance(governance);
					}
				}

				// Govern the sections within the governance area
				for (WoofSectionModel sectionModel : woof.getWoofSections()) {
					if (this.isWithinGovernanceArea(sectionModel.getX(), sectionModel.getY(), area)) {
						// Section within governance area so govern
						OfficeSection section = sections.get(sectionModel.getWoofSectionName());
						section.addGovernance(governance);
					}
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