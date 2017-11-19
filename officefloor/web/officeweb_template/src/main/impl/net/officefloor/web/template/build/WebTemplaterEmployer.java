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

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplater;
import net.officefloor.web.template.extension.WebTemplateExtension;

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
	 * @return {@link WebTemplater}.
	 */
	public static WebTemplater employWebTemplater(WebArchitect webArchitect, OfficeArchitect officeArchitect) {
		return null;
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
	 * Instantiate.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 */
	public WebTemplaterEmployer(WebArchitect webArchitect, OfficeArchitect officeArchitect) {
		this.webArchitect = webArchitect;
		this.officeArchitect = officeArchitect;
	}

	/*
	 * ====================== WebTemplater ===========================
	 */

	@Override
	public WebTemplate addTemplate(String applicationPath, Reader template) {

		// Read in the template
		StringWriter content = new StringWriter();
		try {
			for (int character = template.read(); character != -1; character = template.read()) {
				content.write(character);
			}
		} catch (IOException ex) {
			this.officeArchitect.addIssue("Failed to read in template content for " + applicationPath, ex);
			return null; // unable to add the template
		}

		// Add the template
		return null;
	}

	@Override
	public WebTemplate addTemplate(String applicationPath, String locationOfTemplate) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@link WebTemplate} implementation.
	 */
	private static class WebTemplateImpl implements WebTemplate {

		@Override
		public void addProperty(String name, String value) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setLogicClass(Class<?> logicClass) {
			// TODO Auto-generated method stub

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