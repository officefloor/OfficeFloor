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
package net.officefloor.plugin.woof;

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
import net.officefloor.model.woof.PropertyModel;
import net.officefloor.model.woof.WoofAccessInputModel;
import net.officefloor.model.woof.WoofAccessModel;
import net.officefloor.model.woof.WoofAccessOutputModel;
import net.officefloor.model.woof.WoofAccessOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofAccessOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofAccessOutputToWoofTemplateModel;
import net.officefloor.model.woof.WoofExceptionModel;
import net.officefloor.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.model.woof.WoofGovernanceAreaModel;
import net.officefloor.model.woof.WoofGovernanceModel;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepository;
import net.officefloor.model.woof.WoofResourceModel;
import net.officefloor.model.woof.WoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionModel;
import net.officefloor.model.woof.WoofSectionOutputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofAccessInputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.model.woof.WoofStartModel;
import net.officefloor.model.woof.WoofStartToWoofSectionInputModel;
import net.officefloor.model.woof.WoofTemplateExtensionModel;
import net.officefloor.model.woof.WoofTemplateLinkModel;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateOutputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofAccessInputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateRedirectModel;
import net.officefloor.plugin.web.http.application.HttpSecuritySection;
import net.officefloor.plugin.web.http.application.HttpTemplateSection;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.application.WebArchitectEmployer;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionLoader;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionLoaderImpl;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceService;

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
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeExtensionContext extensionContext = context.getOfficeExtensionContext();
		WebArchitect web = WebArchitectEmployer.employWebArchitect(office, extensionContext);

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
				office.addIssue(WoofTemplateExtensionSource.class.getSimpleName() + " configuration failure: "
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
				office.addIssue("Failed loading extension from " + extensionService.getClass().getName() + " : "
						+ ex.getMessage(), ex);

				// Carry on to next service
				continue;
			}
		}

		// Create the template extension loader
		WoofTemplateExtensionLoader extensionLoader = new WoofTemplateExtensionLoaderImpl();

		// Configure the HTTP templates
		Map<String, HttpTemplateSection> templates = new HashMap<String, HttpTemplateSection>();
		for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {

			// Obtain template details
			String templateName = templateModel.getWoofTemplateName();
			String templatePath = templateModel.getTemplatePath();
			String templateClassName = templateModel.getTemplateClassName();
			String uri = templateModel.getUri();

			// Obtain the template logic class (if provided)
			Class<?> templateLogicClass = CompileUtil.isBlank(templateClassName) ? null
					: classLoader.loadClass(templateClassName);

			// Configure the template
			HttpTemplateSection template = web.addHttpTemplate(uri, templatePath, templateLogicClass);

			// Configure content type for template
			String contentType = templateModel.getTemplateContentType();
			if (!CompileUtil.isBlank(contentType)) {
				template.setTemplateContentType(contentType);
			}

			// Configure secure for template
			template.setTemplateSecure(templateModel.getIsTemplateSecure());
			for (WoofTemplateLinkModel linkModel : templateModel.getLinks()) {
				template.setLinkSecure(linkModel.getWoofTemplateLinkName(), linkModel.getIsLinkSecure());
			}

			// Configure HTTP methods for redirect
			for (WoofTemplateRedirectModel redirectModel : templateModel.getRedirects()) {
				template.addRenderRedirectHttpMethod(redirectModel.getWoofTemplateRedirectHttpMethod());
			}

			// Maintain reference to template by name
			templates.put(templateName, template);

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
				extensionLoader.extendTemplate(extensionSourceClassName, properties, uri, template, office, web,
						extensionContext);
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
						uri, template, office, web, extensionContext);
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
			OfficeSection section = office.addOfficeSection(sectionName, sectionSourceClassName, sectionLocation);
			for (PropertyModel property : sectionModel.getProperties()) {
				section.addProperty(property.getName(), property.getValue());
			}

			// Maintain reference to section by name
			sections.put(sectionName, section);

			// Link URIs to inputs
			for (WoofSectionInputModel inputModel : sectionModel.getInputs()) {

				// Obtain the name of the input
				String inputName = inputModel.getWoofSectionInputName();

				// Link to URI if configured
				String uri = inputModel.getUri();
				if ((uri != null) && (uri.trim().length() > 0)) {
					web.linkUri(uri, section.getOfficeSectionInput(inputName));
				}

				// Maintain references from inputs to section
				inputToSection.put(inputModel, sectionModel);
			}
		}

		// Configure the Accesses
		Map<String, HttpSecuritySection> securitySections = new HashMap<>();
		for (WoofAccessModel accessModel : woof.getWoofAccesses()) {

			// Obtain the access details
			String httpSecurityName = accessModel.getAccessName();
			String httpSecuritySourceClassName = accessModel.getHttpSecuritySourceClassName();
			long timeout = accessModel.getTimeout();

			// Obtain the HTTP Security Source class
			Class<? extends HttpSecuritySource<?, ?, ?, ?>> httpSecuritySourceClass = (Class<? extends HttpSecuritySource<?, ?, ?, ?>>) extensionContext
					.loadClass(httpSecuritySourceClassName);

			// Add the HTTP security
			HttpSecuritySection securitySection = web.addHttpSecurity(httpSecurityName, httpSecuritySourceClass);
			securitySection.setSecurityTimeout(timeout);
			for (PropertyModel property : accessModel.getProperties()) {
				securitySection.addProperty(property.getName(), property.getValue());
			}

			// Register the HTTP security
			securitySections.put(httpSecurityName, securitySection);
		}

		// Link the template outputs
		for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {

			// Obtain the auto-wire template
			String templateName = templateModel.getWoofTemplateName();
			HttpTemplateSection template = templates.get(templateName);

			// Provide link configuration inheritance
			String superTemplateName = templateModel.getSuperTemplate();
			if (superTemplateName != null) {
				HttpTemplateSection superTemplate = templates.get(superTemplateName);
				template.setSuperHttpTemplate(superTemplate);
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
						office.link(template.getOfficeSection().getOfficeSectionOutput(outputName),
								targetSection.getOfficeSectionInput(targetInputName));
					}
				}

				// Link potential template
				WoofTemplateOutputToWoofTemplateModel templateLink = outputModel.getWoofTemplate();
				if (templateLink != null) {
					WoofTemplateModel targetTemplateModel = templateLink.getWoofTemplate();
					if (targetTemplateModel != null) {
						HttpTemplateSection targetTemplate = templates.get(targetTemplateModel.getWoofTemplateName());
						web.linkToHttpTemplate(template.getOfficeSection().getOfficeSectionOutput(outputName),
								targetTemplate);
					}
				}

				// Link access inputs
				WoofTemplateOutputToWoofAccessInputModel accessLink = outputModel.getWoofAccessInput();
				if (accessLink != null) {
					WoofAccessInputModel targetAccessInputModel = accessLink.getWoofAccessInput();
					if (targetAccessInputModel != null) {
						// TODO obtain the access
						HttpSecuritySection securitySection = null; // TODO
						office.link(template.getOfficeSection().getOfficeSectionOutput(outputName),
								securitySection.getOfficeSection()
										.getOfficeSectionInput(targetAccessInputModel.getWoofAccessInputName()));
					}
				}

				// Link potential resource
				WoofTemplateOutputToWoofResourceModel resourceLink = outputModel.getWoofResource();
				if (resourceLink != null) {
					WoofResourceModel resourceModel = resourceLink.getWoofResource();
					if (resourceModel != null) {
						web.linkToResource(template.getOfficeSection().getOfficeSectionOutput(outputName),
								resourceModel.getResourcePath());
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
						office.link(section.getOfficeSectionOutput(outputName),
								targetSection.getOfficeSectionInput(targetInputName));
					}
				}

				// Link potential template
				WoofSectionOutputToWoofTemplateModel templateLink = outputModel.getWoofTemplate();
				if (templateLink != null) {
					WoofTemplateModel targetTemplateModel = templateLink.getWoofTemplate();
					if (targetTemplateModel != null) {
						HttpTemplateSection targetTemplate = templates.get(targetTemplateModel.getWoofTemplateName());
						web.linkToHttpTemplate(section.getOfficeSectionOutput(outputName), targetTemplate);
					}
				}

				// Link potential access input
				WoofSectionOutputToWoofAccessInputModel accessLink = outputModel.getWoofAccessInput();
				if (accessLink != null) {
					WoofAccessInputModel targetAccessInputModel = accessLink.getWoofAccessInput();
					if (targetAccessInputModel != null) {
						// TODO obtain the access
						HttpSecuritySection securitySection = null; // TODO
						office.link(section.getOfficeSectionOutput(outputName), securitySection.getOfficeSection()
								.getOfficeSectionInput(targetAccessInputModel.getWoofAccessInputName()));
					}
				}

				// Link potential resource
				WoofSectionOutputToWoofResourceModel resourceLink = outputModel.getWoofResource();
				if (resourceLink != null) {
					WoofResourceModel resourceModel = resourceLink.getWoofResource();
					if (resourceModel != null) {
						web.linkToResource(section.getOfficeSectionOutput(outputName), resourceModel.getResourcePath());
					}
				}
			}
		}

		// Link the access outputs
		for (WoofAccessModel accessModel : woof.getWoofAccesses()) {

			// Obtain the HTTP security section
			HttpSecuritySection security = securitySections.get(accessModel.getAccessName());

			// Link outputs for the access
			for (WoofAccessOutputModel outputModel : accessModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofAccessOutputName();

				// Link potential section input
				WoofAccessOutputToWoofSectionInputModel sectionLink = outputModel.getWoofSectionInput();
				if (sectionLink != null) {
					WoofSectionInputModel sectionInput = sectionLink.getWoofSectionInput();
					if (sectionInput != null) {

						// Obtain target input name
						String targetInputName = sectionInput.getWoofSectionInputName();

						// Obtain the target section
						WoofSectionModel targetSectionModel = inputToSection.get(sectionInput);
						OfficeSection targetSection = sections.get(targetSectionModel.getWoofSectionName());

						// Link access output to section input
						office.link(security.getOfficeSection().getOfficeSectionOutput(outputName),
								targetSection.getOfficeSectionInput(targetInputName));
					}
				}

				// Link potential template
				WoofAccessOutputToWoofTemplateModel templateLink = outputModel.getWoofTemplate();
				if (templateLink != null) {
					WoofTemplateModel targetTemplateModel = templateLink.getWoofTemplate();
					if (targetTemplateModel != null) {
						HttpTemplateSection targetTemplate = templates.get(targetTemplateModel.getWoofTemplateName());
						web.linkToHttpTemplate(security.getOfficeSection().getOfficeSectionOutput(outputName),
								targetTemplate);
					}
				}

				// Link potential resource
				WoofAccessOutputToWoofResourceModel resourceLink = outputModel.getWoofResource();
				if (resourceLink != null) {
					WoofResourceModel resourceModel = resourceLink.getWoofResource();
					if (resourceModel != null) {
						web.linkToResource(security.getOfficeSection().getOfficeSectionOutput(outputName),
								resourceModel.getResourcePath());
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
					OfficeEscalation escalation = office.addOfficeEscalation(exceptionType.getName());
					office.link(escalation, targetSection.getOfficeSectionInput(targetInputName));
				}
			}

			// Link potential template
			WoofExceptionToWoofTemplateModel templateLink = exceptionModel.getWoofTemplate();
			if (templateLink != null) {
				WoofTemplateModel targetTemplateModel = templateLink.getWoofTemplate();
				if (targetTemplateModel != null) {
					HttpTemplateSection targetTemplate = templates.get(targetTemplateModel.getWoofTemplateName());
					web.linkEscalation(exceptionType, targetTemplate);
				}
			}

			// Link potential resource
			WoofExceptionToWoofResourceModel resourceLink = exceptionModel.getWoofResource();
			if (resourceLink != null) {
				WoofResourceModel resourceModel = resourceLink.getWoofResource();
				if (resourceModel != null) {
					web.linkEscalation(exceptionType, resourceModel.getResourcePath());
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
					OfficeStart start = office.addOfficeStart(String.valueOf(startIndex++));
					office.link(start, targetSection.getOfficeSectionInput(targetInputName));
				}
			}
		}

		// Load the governance
		for (WoofGovernanceModel govModel : woof.getWoofGovernances()) {

			// Obtain the governance details
			String governanceName = govModel.getWoofGovernanceName();
			String governanceSourceClassName = govModel.getGovernanceSourceClassName();

			// Configure the governance
			OfficeGovernance governance = office.addOfficeGovernance(governanceName, governanceSourceClassName);
			for (PropertyModel property : govModel.getProperties()) {
				governance.addProperty(property.getName(), property.getValue());
			}

			// Configure the governance of the sections
			for (WoofGovernanceAreaModel area : govModel.getGovernanceAreas()) {

				// Govern the templates within the governance area
				for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {
					if (this.isWithinGovernanceArea(templateModel.getX(), templateModel.getY(), area)) {
						// Template within governance area so govern
						HttpTemplateSection template = templates.get(templateModel.getWoofTemplateName());
						template.getOfficeSection().addGovernance(governance);
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