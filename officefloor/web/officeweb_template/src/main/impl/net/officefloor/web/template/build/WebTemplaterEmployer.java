/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.template.build;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.HttpInputPath;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.extension.WebTemplateExtension;
import net.officefloor.web.template.section.WebTemplateLinkAnnotation;
import net.officefloor.web.template.section.WebTemplateRedirectAnnotation;
import net.officefloor.web.template.section.WebTemplateSectionSource;

/**
 * {@link WebTemplater} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplaterEmployer implements WebTemplater {

	/**
	 * Employs the {@link WebTemplater}.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 * @return {@link WebTemplater}.
	 */
	public static WebTemplater employWebTemplater(WebArchitect webArchitect, OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		return new WebTemplaterEmployer(webArchitect, officeArchitect, officeSourceContext);
	}

	/**
	 * {@link WebArchitect}.
	 */
	private final WebArchitect webArchitect;

	/**
	 * {@link OfficeArchitect}.
	 */
	private final OfficeArchitect officeArchitect;

	/**
	 * {@link OfficeSourceContext}.
	 */
	private final OfficeSourceContext sourceContext;

	/**
	 * {@link WebTemplateImpl} instances.
	 */
	private final List<WebTemplateImpl> templates = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param sourceContext
	 *            {@link OfficeSourceContext}.
	 */
	private WebTemplaterEmployer(WebArchitect webArchitect, OfficeArchitect officeArchitect,
			OfficeSourceContext sourceContext) {
		this.webArchitect = webArchitect;
		this.officeArchitect = officeArchitect;
		this.sourceContext = sourceContext;
	}

	/*
	 * ====================== WebTemplater ===========================
	 */

	@Override
	public WebTemplate addTemplate(String applicationPath, Reader templateContent) {

		// Read in the template
		StringWriter content = new StringWriter();
		try {
			for (int character = templateContent.read(); character != -1; character = templateContent.read()) {
				content.write(character);
			}
		} catch (IOException ex) {
			throw this.officeArchitect.addIssue("Failed to read in template content for " + applicationPath, ex);
		}

		// Add the template
		return this.addTemplate(applicationPath, (properties) -> properties
				.addProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_CONTENT).setValue(content.toString()));
	}

	@Override
	public WebTemplate addTemplate(String applicationPath, String locationOfTemplate) {

		// Add the template
		return this.addTemplate(applicationPath, (properties) -> properties
				.addProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION).setValue(locationOfTemplate));
	}

	/**
	 * Adds the {@link WebTemplate}.
	 * 
	 * @param applicationPath
	 *            Application path.
	 * @param configurer
	 *            {@link Consumer} to configure the {@link PropertyList}.
	 * @return {@link WebTemplate}.
	 */
	private WebTemplate addTemplate(String applicationPath, Consumer<PropertyList> configurer) {

		// Add the section for the template
		WebTemplateSectionSource webTemplateSectionSource = new WebTemplateSectionSource();
		OfficeSection templateSection = this.officeArchitect.addOfficeSection(applicationPath, webTemplateSectionSource,
				applicationPath);

		// Determine if dynamic path
		boolean isPathParameters = this.webArchitect.isPathParameters(applicationPath);

		// Configure the template properties
		PropertyList properties = this.sourceContext.createPropertyList();
		properties.addProperty(WebTemplateSectionSource.PROPERTY_IS_PATH_PARAMETERS)
				.setValue(String.valueOf(isPathParameters));
		configurer.accept(properties);

		// Add the template
		WebTemplateImpl template = new WebTemplateImpl(applicationPath, webTemplateSectionSource, templateSection,
				properties);
		this.templates.add(template);

		// Return the template
		return template;
	}

	@Override
	public void informWebArchitect() {
		for (WebTemplateImpl template : this.templates) {
			template.loadProperties();
		}
	}

	/**
	 * {@link WebTemplate} implementation.
	 */
	private class WebTemplateImpl implements WebTemplate {

		/**
		 * Application path for the {@link WebTemplate}.
		 */
		private final String applicationPath;

		/**
		 * {@link WebTemplateSectionSource}.
		 */
		private final WebTemplateSectionSource webTemplateSectionSource;

		/**
		 * {@link OfficeSection} for the {@link WebTemplate}.
		 */
		private final OfficeSection section;

		/**
		 * {@link PropertyList}.
		 */
		private final PropertyList properties;

		/**
		 * Logic {@link Class} for the {@link WebTemplate}.
		 */
		private Class<?> logicClass = null;

		/**
		 * Name of {@link Method} on the logic {@link Class} to return the
		 * values for path parameters in redirecting to this
		 * {@link WebTemplate}.
		 */
		private String redirectValuesFunctionName = null;

		/**
		 * Indicates if the {@link WebTemplate} is secure.
		 */
		private boolean isSecure = false;

		/**
		 * Secure links.
		 */
		private Map<String, Boolean> secureLinks = new HashMap<>();

		/**
		 * Link separator character.
		 */
		private char linkSeparatorCharacter = '+';

		/**
		 * <code>Content-Type</code> for the {@link WebTemplate}.
		 */
		private String contentType = null;

		/**
		 * {@link Charset}.
		 */
		private Charset charset = null;

		/**
		 * Render {@link HttpMethod} instances.
		 */
		private final List<HttpMethod> renderMethods = new LinkedList<>();

		/**
		 * Listing of {@link LinkToTemplate} instances.
		 */
		private final List<LinkToTemplate> linkToTemplates = new LinkedList<>();

		/**
		 * Super {@link WebTemplate}.
		 */
		private WebTemplateImpl superTemplate = null;

		/**
		 * Instantiate.
		 * 
		 * @param applicationPath
		 *            Application path for the {@link WebTemplate}.
		 * @param webTemplateSectionSource
		 *            {@link WebTemplateSectionSource}.
		 * @param templateSection
		 *            {@link OfficeSection} for the {@link WebTemplate}.
		 * @param properties
		 *            {@link PropertyList}.
		 */
		private WebTemplateImpl(String applicationPath, WebTemplateSectionSource webTemplateSectionSource,
				OfficeSection templateSection, PropertyList properties) {
			this.applicationPath = applicationPath;
			this.webTemplateSectionSource = webTemplateSectionSource;
			this.section = templateSection;
			this.properties = properties;
		}

		/**
		 * Load the properties.
		 */
		private void loadProperties() {

			// Configure the input
			OfficeSectionInput sectionInput = this.section
					.getOfficeSectionInput(WebTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME);
			HttpUrlContinuation templateInput = WebTemplaterEmployer.this.webArchitect.link(this.isSecure,
					this.applicationPath, sectionInput);

			// Provide the input path to template section
			HttpInputPath templateInputPath = templateInput.getPath();
			this.webTemplateSectionSource.setHttpInputPath(templateInputPath);

			// Configure properties for the template
			if (this.logicClass != null) {
				this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_CLASS_NAME)
						.setValue(this.logicClass.getName());
			}
			if (this.redirectValuesFunctionName != null) {
				this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_REDIRECT_VALUES_FUNCTION)
						.setValue(this.redirectValuesFunctionName);
			}
			this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_LINK_SEPARATOR)
					.setValue(String.valueOf(this.linkSeparatorCharacter));
			if (this.contentType != null) {
				this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_CONTENT_TYPE)
						.setValue(this.contentType);
			}
			if (this.charset != null) {
				this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_CHARSET)
						.setValue(this.charset.name());
			}
			this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_SECURE)
					.setValue(String.valueOf(this.isSecure));
			for (String linkName : this.secureLinks.keySet()) {
				Boolean isLinkSecure = this.secureLinks.get(linkName);
				this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_LINK_SECURE_PREFIX + linkName)
						.setValue(String.valueOf(isLinkSecure));
			}

			// Configure inheritance properties
			Deque<WebTemplateImpl> inheritanceHeirarchy = new LinkedList<>();
			WebTemplateImpl parent = this.superTemplate;
			StringBuilder cycle = new StringBuilder();
			cycle.append(this.applicationPath + " ::");
			while (parent != null) {

				// Determine if inheritance cycle
				if (inheritanceHeirarchy.contains(parent)) {
					throw WebTemplaterEmployer.this.officeArchitect.addIssue(
							WebTemplate.class.getSimpleName() + " inheritance cycle " + cycle.toString() + " ...");
				}

				// Include parent on report for cycle
				cycle.append(" " + parent.applicationPath + " ::");

				// Include in inheritance hierarchy
				inheritanceHeirarchy.push(parent);
				parent = parent.superTemplate;
			}
			this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES_COUNT)
					.setValue(String.valueOf(inheritanceHeirarchy.size()));
			int inheritanceIndex = 0;
			while (inheritanceHeirarchy.size() > 0) {
				parent = inheritanceHeirarchy.pop();

				// Attempt to load content
				Property parentContent = parent.properties
						.getProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_CONTENT);
				if (parentContent != null) {
					this.properties.getOrAddProperty(
							WebTemplateSectionSource.PROPERTY_TEMPLATE_CONTENT + "." + String.valueOf(inheritanceIndex))
							.setValue(parentContent.getValue());
				} else {
					Property parentLocation = parent.properties
							.getProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION);
					this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION + "."
							+ String.valueOf(inheritanceIndex)).setValue(parentLocation.getValue());
				}

				// Next parent
				inheritanceIndex++;
			}

			// Configure the properties into the section
			this.properties.configureProperties(this.section);

			// Ensure appropriately configured
			if (templateInputPath.isPathParameters()) {

				// Must have logic class
				if (this.logicClass == null) {
					throw WebTemplaterEmployer.this.officeArchitect
							.addIssue("Must provide template logic class for template " + this.applicationPath
									+ ", as has dynamic path");
				}

				// Must have redirect values function
				if (CompileUtil.isBlank(this.redirectValuesFunctionName)) {
					throw WebTemplaterEmployer.this.officeArchitect.addIssue(
							"Must provide redirect values function for template /{param}, as has dynamic path");
				}
			}

			// Load the type
			OfficeSectionType type = WebTemplaterEmployer.this.sourceContext.loadOfficeSectionType(this.applicationPath,
					WebTemplateSectionSource.class.getName(), this.applicationPath, this.properties);

			// Load other methods
			for (HttpMethod method : this.renderMethods) {

				// Ignore GET, as added with continuation
				if (method == HttpMethod.GET) {
					continue;
				}

				// Route to template for method
				WebTemplaterEmployer.this.webArchitect.link(this.isSecure, method, this.applicationPath, sectionInput);
			}

			// Load the link inputs
			for (OfficeSectionInputType inputType : type.getOfficeSectionInputTypes()) {
				for (Object annotation : inputType.getAnnotations()) {
					if (annotation instanceof WebTemplateLinkAnnotation) {
						WebTemplateLinkAnnotation link = (WebTemplateLinkAnnotation) annotation;

						// Obtain the input
						String linkName = link.getLinkName();
						OfficeSectionInput linkInput = this.section.getOfficeSectionInput(linkName);

						// Obtain the link details
						boolean isLinkSecure = link.isLinkSecure();
						String linkPath = this.applicationPath + String.valueOf(this.linkSeparatorCharacter) + linkName;

						// Configure the link inputs for each method supported
						for (HttpMethod method : link.getMethods()) {
							WebTemplaterEmployer.this.webArchitect.link(isLinkSecure, method, linkPath, linkInput);
						}
					}
				}
			}

			// Load the redirect to template
			for (OfficeSectionOutputType outputType : type.getOfficeSectionOutputTypes()) {
				for (Object annotation : outputType.getAnnotations()) {
					if (annotation instanceof WebTemplateRedirectAnnotation) {
						WebTemplateRedirectAnnotation redirect = (WebTemplateRedirectAnnotation) annotation;

						// Obtain the values type for redirect
						Class<?> valuesType = redirect.getValuesType();

						// Configure the redirect
						OfficeSectionOutput redirectOutput = this.section
								.getOfficeSectionOutput(outputType.getOfficeSectionOutputName());
						WebTemplaterEmployer.this.webArchitect.link(redirectOutput, templateInput, valuesType);
					}
				}
			}

			// Link to template (redirect - should be different URL always)
			for (LinkToTemplate link : this.linkToTemplates) {
				WebTemplaterEmployer.this.webArchitect.link(link.flowSourceNode, templateInput, link.valuesType);
			}
		}

		/*
		 * ================== WebTemplate ============================
		 */

		@Override
		public void addProperty(String name, String value) {
			this.properties.addProperty(name).setValue(value);
		}

		@Override
		public WebTemplate setLogicClass(Class<?> logicClass) {
			this.logicClass = logicClass;
			return this;
		}

		@Override
		public WebTemplate setRedirectValuesFunction(String functionName) {
			this.redirectValuesFunctionName = functionName;
			return this;
		}

		@Override
		public WebTemplate setContentType(String contentType) {
			this.contentType = contentType;
			return this;
		}

		@Override
		public WebTemplate setCharset(Charset charset) {
			this.charset = charset;
			return this;
		}

		@Override
		public WebTemplate setLinkSeparatorCharacter(char separator) {
			this.linkSeparatorCharacter = separator;
			return this;
		}

		@Override
		public WebTemplate setSecure(boolean isSecure) {
			this.isSecure = isSecure;
			return this;
		}

		@Override
		public WebTemplate setLinkSecure(String linkName, boolean isSecure) {
			this.secureLinks.put(linkName, isSecure);
			return this;
		}

		@Override
		public WebTemplate addRenderMethod(HttpMethod method) {
			this.renderMethods.add(method);
			return this;
		}

		@Override
		public WebTemplate setSuperTemplate(WebTemplate superTemplate) {
			this.superTemplate = (WebTemplateImpl) superTemplate;
			return this;
		}

		@Override
		public WebTemplateExtensionBuilder addExtension(WebTemplateExtension extension) {
			return this.webTemplateSectionSource.addWebTemplateExtension(extension,
					WebTemplaterEmployer.this.sourceContext.createPropertyList());
		}

		@Override
		public WebTemplate link(OfficeFlowSourceNode flowSourceNode, Class<?> valuesType) {
			this.linkToTemplates.add(new LinkToTemplate(flowSourceNode, valuesType));
			return this;
		}

		@Override
		public OfficeSectionOutput getOutput(String outputName) {
			return this.section.getOfficeSectionOutput(outputName);
		}

		@Override
		public void addGovernance(OfficeGovernance governance) {
			this.section.addGovernance(governance);
		}
	}

	/**
	 * Link to the {@link WebTemplate}.
	 */
	private static class LinkToTemplate {

		/**
		 * {@link OfficeFlowSourceNode}.
		 */
		private final OfficeFlowSourceNode flowSourceNode;

		/**
		 * Values type.
		 */
		private final Class<?> valuesType;

		/**
		 * Instantiate.
		 * 
		 * @param flowSourceNode
		 *            {@link OfficeFlowSourceNode}.
		 * @param valuesType
		 *            Values type.
		 */
		public LinkToTemplate(OfficeFlowSourceNode flowSourceNode, Class<?> valuesType) {
			this.flowSourceNode = flowSourceNode;
			this.valuesType = valuesType;
		}
	}

}