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
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.server.http.HttpMethod;
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
			this.officeArchitect.addIssue("Failed to read in template content for " + applicationPath, ex);
			return null; // unable to add the template
		}

		// Add the section for the template
		WebTemplateSectionSource webTemplateSectionSource = new WebTemplateSectionSource();
		OfficeSection templateSection = this.officeArchitect.addOfficeSection(applicationPath, webTemplateSectionSource,
				applicationPath);

		// Configure the template content
		PropertyList properties = this.sourceContext.createPropertyList();
		properties.addProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_CONTENT).setValue(content.toString());

		// Add the template
		WebTemplateImpl template = new WebTemplateImpl(applicationPath, webTemplateSectionSource, templateSection,
				properties);
		this.templates.add(template);

		// Return the template
		return template;
	}

	@Override
	public WebTemplate addTemplate(String applicationPath, String locationOfTemplate) {

		// TODO Auto-generated method stub
		return null;
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
					applicationPath, sectionInput);
			this.webTemplateSectionSource.setHttpInputPath(templateInput.getPath());

			// Configure properties for template
			if (this.logicClass != null) {
				this.properties.addProperty(WebTemplateSectionSource.PROPERTY_CLASS_NAME)
						.setValue(this.logicClass.getName());
			}
			if (this.redirectValuesFunctionName != null) {
				this.properties.addProperty(WebTemplateSectionSource.PROPERTY_REDIRECT_VALUES_FUNCTION)
						.setValue(this.redirectValuesFunctionName);
			}

			// Load the type
			OfficeSectionType type = WebTemplaterEmployer.this.sourceContext.loadOfficeSectionType(this.applicationPath,
					WebTemplateSectionSource.class.getName(), this.applicationPath, this.properties);

			// Load the link inputs
			for (OfficeSectionInputType inputType : type.getOfficeSectionInputTypes()) {
				for (Object annotation : inputType.getAnnotations()) {
					if (annotation instanceof WebTemplateLinkAnnotation) {
						WebTemplateLinkAnnotation link = (WebTemplateLinkAnnotation) annotation;

						// Obtain the input
						String linkName = link.getLinkName();
						OfficeSectionInput linkInput = this.section.getOfficeSectionInput(linkName);

						// Create the input for the link
						boolean isLinkSecure = link.isLinkSecure();
						String linkPath = this.applicationPath + "+" + linkName;
						WebTemplaterEmployer.this.webArchitect.link(isLinkSecure, linkPath, linkInput);
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
								.getOfficeSectionOutput(WebTemplateSectionSource.REDIRECT_TEMPLATE_OUTPUT_NAME);
						WebTemplaterEmployer.this.webArchitect.link(redirectOutput, templateInput, valuesType);
					}
				}
			}

			// Configure the section
			this.properties.configureProperties(this.section);
		}

		/*
		 * ================== WebTemplate ============================
		 */

		@Override
		public void addProperty(String name, String value) {
			// TODO Auto-generated method stub

		}

		@Override
		public WebTemplate setLogicClass(Class<?> logicClass) {
			this.logicClass = logicClass;
			return this;
		}

		@Override
		public WebTemplate setRedirectValuesFunction(String functionName) {
			this.redirectValuesFunctionName = functionName;
			return null;
		}

		@Override
		public WebTemplate setContentType(String contentType) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public WebTemplate setCharset(Charset charset) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public WebTemplate setLinkSeparatorCharacter(char separator) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public WebTemplate setSecure(boolean isSecure) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public WebTemplate setLinkSecure(String linkName, boolean isSecure) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public WebTemplate addNonRedirectMethod(HttpMethod method) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public WebTemplate setSuperTemplate(WebTemplate superTemplate) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public WebTemplate addExtension(WebTemplateExtension extension) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public OfficeSectionInput getInput(Class<?> valuesType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public OfficeSectionOutput getOutput(String outputName) {
			return this.section.getOfficeSectionOutput(outputName);
		}
	}

}