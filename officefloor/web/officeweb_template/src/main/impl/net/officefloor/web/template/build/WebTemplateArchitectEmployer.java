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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpInputPath;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.extension.WebTemplateExtension;
import net.officefloor.web.template.section.WebTemplateLinkAnnotation;
import net.officefloor.web.template.section.WebTemplateRedirectAnnotation;
import net.officefloor.web.template.section.WebTemplateSectionSource;

/**
 * {@link WebTemplateArchitect} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateArchitectEmployer extends AbstractWebTemplateFactory implements WebTemplateArchitect {

	/**
	 * Employs the {@link WebTemplateArchitect}.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 * @return {@link WebTemplateArchitect}.
	 */
	public static WebTemplateArchitect employWebTemplater(WebArchitect webArchitect, OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		return new WebTemplateArchitectEmployer(webArchitect, officeArchitect, officeSourceContext);
	}

	/**
	 * Employs the {@link WebTemplateLoader}.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @return {@link WebTemplateLoader}.
	 */
	public static WebTemplateLoader employWebTemplateLoader(OfficeFloorCompiler compiler) {
		return new WebTemplateLoaderImpl(compiler);
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
	private WebTemplateArchitectEmployer(WebArchitect webArchitect, OfficeArchitect officeArchitect,
			OfficeSourceContext sourceContext) {
		this.webArchitect = webArchitect;
		this.officeArchitect = officeArchitect;
		this.sourceContext = sourceContext;
	}

	/*
	 * ======================= WebTemplateFactory ============================
	 */

	@Override
	protected PropertyList createPropertyList() {
		return this.sourceContext.createPropertyList();
	}

	@Override
	protected SourceIssues getSourceIssues() {
		return this.officeArchitect;
	}

	@Override
	protected boolean isPathParameters(String applicationPath) {
		return this.webArchitect.isPathParameters(applicationPath);
	}

	@Override
	protected WebTemplate addTemplate(boolean isSecure, String applicationPath, PropertyList properties) {

		// Add the section for the template
		WebTemplateSectionSource webTemplateSectionSource = new WebTemplateSectionSource();
		OfficeSection templateSection = this.officeArchitect.addOfficeSection(applicationPath, webTemplateSectionSource,
				applicationPath);

		// Add the template
		WebTemplateImpl template = new WebTemplateImpl(isSecure, applicationPath, webTemplateSectionSource,
				templateSection, properties);
		this.templates.add(template);

		// Return the template
		return template;
	}

	/*
	 * ====================== WebTemplateArchitect ===========================
	 */

	@Override
	public void informWebArchitect() {
		for (WebTemplateImpl template : this.templates) {
			template.loadProperties();
		}
	}

	/**
	 * {@link WebTemplate} implementation.
	 */
	private class WebTemplateImpl extends AbstractWebTemplate {

		/**
		 * {@link WebTemplateSectionSource}.
		 */
		private final WebTemplateSectionSource webTemplateSectionSource;

		/**
		 * {@link OfficeSection} for the {@link WebTemplate}.
		 */
		private final OfficeSection section;

		/**
		 * {@link OfficeSectionInput} to render the {@link WebTemplate}.
		 */
		private final OfficeSectionInput sectionInput;

		/**
		 * {@link HttpUrlContinuation} to redirect to render the {@link WebTemplate}.
		 */
		private final HttpUrlContinuation templateInput;

		/**
		 * {@link OfficeFlowSinkNode} to render {@link WebTemplate} by values type.
		 */
		private final Map<String, OfficeFlowSinkNode> renderInputs = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param isSecure
		 *            Indicates if requires secure {@link ServerHttpConnection} to
		 *            render the {@link WebTemplate}.
		 * @param applicationPath
		 *            Application path for the {@link WebTemplate}.
		 * @param webTemplateSectionSource
		 *            {@link WebTemplateSectionSource}.
		 * @param templateSection
		 *            {@link OfficeSection} for the {@link WebTemplate}.
		 * @param properties
		 *            {@link PropertyList}.
		 */
		private WebTemplateImpl(boolean isSecure, String applicationPath,
				WebTemplateSectionSource webTemplateSectionSource, OfficeSection templateSection,
				PropertyList properties) {
			super(isSecure, applicationPath, properties, WebTemplateArchitectEmployer.this.officeArchitect);
			this.webTemplateSectionSource = webTemplateSectionSource;
			this.section = templateSection;

			// Configure the input
			this.sectionInput = this.section.getOfficeSectionInput(WebTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME);
			this.templateInput = WebTemplateArchitectEmployer.this.webArchitect.getHttpInput(isSecure, applicationPath);
			WebTemplateArchitectEmployer.this.officeArchitect.link(this.templateInput.getInput(), this.sectionInput);
		}

		/**
		 * Load the properties.
		 */
		private void loadProperties() {

			// Provide the input path to template section
			HttpInputPath templateInputPath = this.templateInput.getPath();
			this.webTemplateSectionSource.setHttpInputPath(templateInputPath);

			// Configure properties for the template
			PropertyList properties = this.loadProperties(templateInputPath.isPathParameters());

			// Configure the properties into the section
			properties.configureProperties(this.section);

			// Load the type
			OfficeSectionType type = WebTemplateArchitectEmployer.this.sourceContext.loadOfficeSectionType(
					this.applicationPath, WebTemplateSectionSource.class.getName(), this.applicationPath, properties);

			// Load other methods
			for (String httpMethodName : this.renderHttpMethodNames) {

				// Ignore GET, as added with continuation
				if (HttpMethod.GET.getName().equals(httpMethodName)) {
					continue;
				}

				// Route to template for method
				WebTemplateArchitectEmployer.this.officeArchitect.link(
						WebTemplateArchitectEmployer.this.webArchitect
								.getHttpInput(this.isSecure, httpMethodName, this.applicationPath).getInput(),
						this.sectionInput);
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
						for (String httpMethodName : link.getHttpMethods()) {
							WebTemplateArchitectEmployer.this.officeArchitect.link(
									WebTemplateArchitectEmployer.this.webArchitect
											.getHttpInput(isLinkSecure, httpMethodName, linkPath).getInput(),
									linkInput);
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
						WebTemplateArchitectEmployer.this.officeArchitect.link(redirectOutput,
								this.templateInput.getRedirect(valuesType == null ? null : valuesType.getName()));
					}
				}
			}
		}

		/*
		 * ================== WebTemplate ============================
		 */

		@Override
		public WebTemplateExtensionBuilder addExtension(WebTemplateExtension extension) {
			return this.webTemplateSectionSource.addWebTemplateExtension(extension,
					WebTemplateArchitectEmployer.this.sourceContext.createPropertyList());
		}

		@Override
		public OfficeFlowSinkNode getRender(String valuesTypeName) {
			OfficeFlowSinkNode renderInput = this.renderInputs.get(valuesTypeName);
			if (renderInput == null) {
				renderInput = this.templateInput.getRedirect(valuesTypeName);
				this.renderInputs.put(valuesTypeName, renderInput);
			}
			return renderInput;
		}

		@Override
		public OfficeFlowSourceNode getOutput(String outputName) {
			return this.section.getOfficeSectionOutput(outputName);
		}

		@Override
		public WebTemplate addGovernance(OfficeGovernance governance) {
			this.section.addGovernance(governance);
			return this;
		}
	}

}