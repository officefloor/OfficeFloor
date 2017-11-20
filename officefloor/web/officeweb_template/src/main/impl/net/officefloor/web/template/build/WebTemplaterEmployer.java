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
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.extension.WebTemplateExtension;
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
		OfficeSection templateSection = this.officeArchitect.addOfficeSection(applicationPath,
				WebTemplateSectionSource.class.getName(), applicationPath);

		// Configure the template content
		templateSection.addProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_CONTENT, content.toString());

		// Add the template
		WebTemplateImpl template = new WebTemplateImpl(applicationPath, templateSection);
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
		 * {@link OfficeSection} for the {@link WebTemplate}.
		 */
		private final OfficeSection templateSection;

		/**
		 * Logic {@link Class} for the {@link WebTemplate}.
		 */
		private Class<?> logicClass = null;

		/**
		 * Indicates if the {@link WebTemplate} is secure.
		 */
		private boolean isSecure = false;

		/**
		 * Instantiate.
		 * 
		 * @param applicationPath
		 *            Application path for the {@link WebTemplate}.
		 * @param templateSection
		 *            {@link OfficeSection} for the {@link WebTemplate}.
		 */
		private WebTemplateImpl(String applicationPath, OfficeSection templateSection) {
			this.applicationPath = applicationPath;
			this.templateSection = templateSection;
		}

		/**
		 * Load the properties.
		 */
		private void loadProperties() {

			// Obtain the template input
			OfficeSectionInput sectionInput = this.templateSection
					.getOfficeSectionInput(WebTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME);

			// Configure the input
			HttpUrlContinuation templateInput = WebTemplaterEmployer.this.webArchitect.link(this.isSecure,
					applicationPath, sectionInput);

			// Configure details for template
			if (this.logicClass != null) {
				this.templateSection.addProperty(WebTemplateSectionSource.PROPERTY_CLASS_NAME,
						this.logicClass.getName());
			}
		}

		/*
		 * ================== WebTemplate ============================
		 */

		@Override
		public void addProperty(String name, String value) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setLogicClass(Class<?> logicClass) {
			this.logicClass = logicClass;
		}

		@Override
		public void setContentType(String contentType) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setCharset(Charset charset) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setSecure(boolean isSecure) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setLinkSecure(String linkName, boolean isSecure) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addNonRedirectMethod(HttpMethod method) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setSuperTemplate(WebTemplate superTemplate) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addExtension(WebTemplateExtension extension) {
			// TODO Auto-generated method stub

		}

		@Override
		public OfficeSectionInput getInput(Class<?> valuesType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public OfficeSectionOutput getOutput(String outputName) {
			// TODO Auto-generated method stub
			return null;
		}
	}

}