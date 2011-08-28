/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.PropertyModel;
import net.officefloor.model.woof.WoofExceptionModel;
import net.officefloor.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepository;
import net.officefloor.model.woof.WoofResourceModel;
import net.officefloor.model.woof.WoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionModel;
import net.officefloor.model.woof.WoofSectionOutputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateExtensionModel;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateOutputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofTemplateModel;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSectionExtension;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;

/**
 * {@link WoofLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderImpl implements WoofLoader {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOG = Logger.getLogger(WoofLoaderImpl.class
			.getName());

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext context;

	/**
	 * {@link WoofRepository}.
	 */
	private final WoofRepository repository;

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @param repository
	 *            {@link WoofRepository}.
	 */
	public WoofLoaderImpl(ClassLoader classLoader,
			ConfigurationContext context, WoofRepository repository) {
		this.classLoader = classLoader;
		this.context = context;
		this.repository = repository;
	}

	/*
	 * ======================= WoofLoader ===========================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void loadWoofConfiguration(String woofLocation,
			WebAutoWireApplication application) throws Exception {

		// Obtain the woof configuration (ensuring exists)
		ConfigurationItem configuration = this.context
				.getConfigurationItem(woofLocation);
		if (configuration == null) {
			throw new FileNotFoundException("Can not find configuration file '"
					+ woofLocation + "'");
		}

		// Load the WoOF model
		WoofModel woof = this.repository.retrieveWoOF(configuration);

		// Load the extension services
		Map<String, WoofTemplateExtensionService> extensionServices = new HashMap<String, WoofTemplateExtensionService>();
		ServiceLoader<WoofTemplateExtensionService> extensionServiceLoader = ServiceLoader
				.load(WoofTemplateExtensionService.class, this.classLoader);
		Iterator<WoofTemplateExtensionService> extensionIterator = extensionServiceLoader
				.iterator();
		while (extensionIterator.hasNext()) {
			try {
				WoofTemplateExtensionService extensionService = extensionIterator
						.next();
				extensionServices.put(
						extensionService.getTemplateExtensionAlias(),
						extensionService);
			} catch (ServiceConfigurationError ex) {
				// Warning that service not available
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING,
							WoofTemplateExtensionService.class.getSimpleName()
									+ " configuration failure: "
									+ ex.getMessage(), ex);
				}
			}
		}

		// Configure the HTTP templates
		Map<String, HttpTemplateAutoWireSection> templates = new HashMap<String, HttpTemplateAutoWireSection>();
		for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {

			// Obtain template details
			String templateName = templateModel.getWoofTemplateName();
			String templatePath = templateModel.getTemplatePath();
			String templateClassName = templateModel.getTemplateClassName();
			String uri = templateModel.getUri();

			// Obtain the template logic class
			Class<?> templateLogicClass = this.classLoader
					.loadClass(templateClassName);

			// Configure the template
			HttpTemplateAutoWireSection template = application.addHttpTemplate(
					templatePath, templateLogicClass, uri);

			// Maintain reference to template by name
			templates.put(templateName, template);

			// Configure the HTTP template extensions
			for (WoofTemplateExtensionModel extensionModel : templateModel
					.getExtensions()) {

				// Obtain the extension
				String extensionClassName = extensionModel
						.getExtensionClassName();
				try {
					WoofTemplateExtensionService extensionService = extensionServices
							.get(extensionClassName);
					if (extensionService != null) {
						// Load via extension service
						PropertyList properties = OfficeFloorCompiler
								.newPropertyList();
						for (PropertyModel property : extensionModel
								.getProperties()) {
							properties.addProperty(property.getName())
									.setValue(property.getValue());
						}
						extensionService
								.extendTemplate(new WoofTemplateExtensionServiceContextImpl(
										template, application, properties,
										this.classLoader));

					} else {
						// Load via extension class name
						Class<? extends HttpTemplateSectionExtension> extensionClass = (Class<? extends HttpTemplateSectionExtension>) this.classLoader
								.loadClass(extensionClassName);
						HttpTemplateAutoWireSectionExtension extension = template
								.addTemplateExtension(extensionClass);
						for (PropertyModel property : extensionModel
								.getProperties()) {
							extension.addProperty(property.getName(),
									property.getValue());
						}
					}
				} catch (Exception ex) {
					// Indicate failure to extend template
					throw new WoofTemplateExtensionException(
							"Failed loading Template Extension "
									+ extensionClassName + ". "
									+ ex.getMessage(), ex);
				}
			}
		}

		// Configure the sections
		Map<String, AutoWireSection> sections = new HashMap<String, AutoWireSection>();
		Map<WoofSectionInputModel, WoofSectionModel> inputToSection = new HashMap<WoofSectionInputModel, WoofSectionModel>();
		for (WoofSectionModel sectionModel : woof.getWoofSections()) {

			// Obtain the section details
			String sectionName = sectionModel.getWoofSectionName();
			String sectionSourceClassName = sectionModel
					.getSectionSourceClassName();
			String sectionLocation = sectionModel.getSectionLocation();

			// Obtain the section source class
			Class sectionSourceClass = this.classLoader
					.loadClass(sectionSourceClassName);

			// Configure the section
			AutoWireSection section = application.addSection(sectionName,
					sectionSourceClass, sectionLocation);
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
					application.linkUri(uri, section, inputName);
				}

				// Maintain references from inputs to section
				inputToSection.put(inputModel, sectionModel);
			}
		}

		// Link the template outputs
		for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {

			// Obtain the auto-wire template
			String templateName = templateModel.getWoofTemplateName();
			HttpTemplateAutoWireSection template = templates.get(templateName);

			// Link outputs for the template
			for (WoofTemplateOutputModel outputModel : templateModel
					.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofTemplateOutputName();

				// Link potential section input
				WoofTemplateOutputToWoofSectionInputModel sectionLink = outputModel
						.getWoofSectionInput();
				if (sectionLink != null) {
					WoofSectionInputModel sectionInput = sectionLink
							.getWoofSectionInput();
					if (sectionInput != null) {

						// Obtain target input name
						String targetInputName = sectionInput
								.getWoofSectionInputName();

						// Obtain the target section
						WoofSectionModel sectionModel = inputToSection
								.get(sectionInput);
						AutoWireSection targetSection = sections
								.get(sectionModel.getWoofSectionName());

						// Link template output to section input
						application.link(template, outputName, targetSection,
								targetInputName);
					}
				}

				// Link potential template
				WoofTemplateOutputToWoofTemplateModel templateLink = outputModel
						.getWoofTemplate();
				if (templateLink != null) {
					WoofTemplateModel targetTemplateModel = templateLink
							.getWoofTemplate();
					if (targetTemplateModel != null) {
						HttpTemplateAutoWireSection targetTemplate = templates
								.get(targetTemplateModel.getWoofTemplateName());
						application.linkToHttpTemplate(template, outputName,
								targetTemplate);
					}
				}

				// Link potential resource
				WoofTemplateOutputToWoofResourceModel resourceLink = outputModel
						.getWoofResource();
				if (resourceLink != null) {
					WoofResourceModel resourceModel = resourceLink
							.getWoofResource();
					if (resourceModel != null) {
						application.linkToResource(template, outputName,
								resourceModel.getResourcePath());
					}
				}
			}
		}

		// Link the section outputs
		for (WoofSectionModel sectionModel : woof.getWoofSections()) {

			// Obtain the auto-wire section
			String sectionName = sectionModel.getWoofSectionName();
			AutoWireSection section = sections.get(sectionName);

			// Link outputs for the section
			for (WoofSectionOutputModel outputModel : sectionModel.getOutputs()) {

				// Obtain output name
				String outputName = outputModel.getWoofSectionOutputName();

				// Link potential section input
				WoofSectionOutputToWoofSectionInputModel sectionLink = outputModel
						.getWoofSectionInput();
				if (sectionLink != null) {
					WoofSectionInputModel sectionInput = sectionLink
							.getWoofSectionInput();
					if (sectionInput != null) {

						// Obtain target input name
						String targetInputName = sectionInput
								.getWoofSectionInputName();

						// Obtain the target section
						WoofSectionModel targetSectionModel = inputToSection
								.get(sectionInput);
						AutoWireSection targetSection = sections
								.get(targetSectionModel.getWoofSectionName());

						// Link section output to section input
						application.link(section, outputName, targetSection,
								targetInputName);
					}
				}

				// Link potential template
				WoofSectionOutputToWoofTemplateModel templateLink = outputModel
						.getWoofTemplate();
				if (templateLink != null) {
					WoofTemplateModel targetTemplateModel = templateLink
							.getWoofTemplate();
					if (targetTemplateModel != null) {
						HttpTemplateAutoWireSection targetTemplate = templates
								.get(targetTemplateModel.getWoofTemplateName());
						application.linkToHttpTemplate(section, outputName,
								targetTemplate);
					}
				}

				// Link potential resource
				WoofSectionOutputToWoofResourceModel resourceLink = outputModel
						.getWoofResource();
				if (resourceLink != null) {
					WoofResourceModel resourceModel = resourceLink
							.getWoofResource();
					if (resourceModel != null) {
						application.linkToResource(section, outputName,
								resourceModel.getResourcePath());
					}
				}
			}
		}

		// Link the escalations
		for (WoofExceptionModel exceptionModel : woof.getWoofExceptions()) {

			// Obtain the exception type
			String exceptionClassName = exceptionModel.getClassName();
			Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) this.classLoader
					.loadClass(exceptionClassName);

			// Link potential section input
			WoofExceptionToWoofSectionInputModel sectionLink = exceptionModel
					.getWoofSectionInput();
			if (sectionLink != null) {
				WoofSectionInputModel sectionInput = sectionLink
						.getWoofSectionInput();
				if (sectionInput != null) {

					// Obtain target input name
					String targetInputName = sectionInput
							.getWoofSectionInputName();

					// Obtain the target section
					WoofSectionModel targetSectionModel = inputToSection
							.get(sectionInput);
					AutoWireSection targetSection = sections
							.get(targetSectionModel.getWoofSectionName());

					// Link escalation handling to section input
					application.linkEscalation(exceptionType, targetSection,
							targetInputName);
				}
			}

			// Link potential template
			WoofExceptionToWoofTemplateModel templateLink = exceptionModel
					.getWoofTemplate();
			if (templateLink != null) {
				WoofTemplateModel targetTemplateModel = templateLink
						.getWoofTemplate();
				if (targetTemplateModel != null) {
					HttpTemplateAutoWireSection targetTemplate = templates
							.get(targetTemplateModel.getWoofTemplateName());
					application.linkEscalation(exceptionType, targetTemplate);
				}
			}

			// Link potential resource
			WoofExceptionToWoofResourceModel resourceLink = exceptionModel
					.getWoofResource();
			if (resourceLink != null) {
				WoofResourceModel resourceModel = resourceLink
						.getWoofResource();
				if (resourceModel != null) {
					application.linkEscalation(exceptionType,
							resourceModel.getResourcePath());
				}
			}
		}

	}

	/**
	 * {@link WoofTemplateExtensionServiceContext} implementation.
	 */
	private static class WoofTemplateExtensionServiceContextImpl extends
			SourcePropertiesImpl implements WoofTemplateExtensionServiceContext {

		/**
		 * {@link HttpTemplateAutoWireSection}.
		 */
		private final HttpTemplateAutoWireSection template;

		/**
		 * {@link WebAutoWireApplication}.
		 */
		private final WebAutoWireApplication application;

		/**
		 * {@link ClassLoader}.
		 */
		private final ClassLoader classLoader;

		/**
		 * Initiate.
		 * 
		 * @param template
		 *            {@link HttpTemplateAutoWireSection}.
		 * @param application
		 *            {@link WebAutoWireApplication}.
		 * @param properties
		 *            {@link PropertyList}.
		 * @param classLoader
		 *            {@link ClassLoader}.
		 */
		public WoofTemplateExtensionServiceContextImpl(
				HttpTemplateAutoWireSection template,
				WebAutoWireApplication application, PropertyList properties,
				ClassLoader classLoader) {
			super(new PropertyListSourceProperties(properties));
			this.template = template;
			this.application = application;
			this.classLoader = classLoader;
		}

		@Override
		public HttpTemplateAutoWireSection getTemplate() {
			return this.template;
		}

		@Override
		public WebAutoWireApplication getWebApplication() {
			return this.application;
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.classLoader;
		}
	}

}