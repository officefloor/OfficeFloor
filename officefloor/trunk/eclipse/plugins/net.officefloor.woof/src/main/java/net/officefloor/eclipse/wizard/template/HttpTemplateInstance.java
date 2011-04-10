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
package net.officefloor.eclipse.wizard.template;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Instance of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInstance {

	/**
	 * {@link HttpTemplate} name.
	 */
	private final String templateName;

	/**
	 * Path to the {@link HttpTemplate}.
	 */
	private final String templatePath;

	/**
	 * Name of the logic class.
	 */
	private final String logicClassName;

	/**
	 * {@link OfficeSection}.
	 */
	private final OfficeSection officeSection;

	/**
	 * URI.
	 */
	private final String uri;

	/**
	 * Initiate.
	 * 
	 * @param templateName
	 *            {@link HttpTemplate} name.
	 * @param templatePath
	 *            Path to the {@link HttpTemplate}.
	 * @param logicClassName
	 *            Name of the logic class.
	 * @param officeSection
	 *            {@link OfficeSection}.
	 * @param uri
	 *            URI.
	 */
	public HttpTemplateInstance(String templateName, String templatePath,
			String logicClassName, OfficeSection officeSection, String uri) {
		this.templateName = templateName;
		this.templatePath = templatePath;
		this.logicClassName = logicClassName;
		this.officeSection = officeSection;
		this.uri = uri;
	}

	/**
	 * Obtains the {@link HttpTemplate} name.
	 * 
	 * @return {@link HttpTemplate} name.
	 */
	public String getTemplateName() {
		return this.templateName;
	}

	/**
	 * Obtains the path to the {@link HttpTemplate}.
	 * 
	 * @return Path to the {@link HttpTemplate}.
	 */
	public String getTemplatePath() {
		return this.templatePath;
	}

	/**
	 * Obtains the name of the logic class for the {@link HttpTemplate}.
	 * 
	 * @return Name of the logic class for the {@link HttpTemplate}.
	 */
	public String getLogicClassName() {
		return this.logicClassName;
	}

	/**
	 * Obtains the {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSection}.
	 */
	public OfficeSection getOfficeSection() {
		return this.officeSection;
	}

	/**
	 * Obtains the URI.
	 * 
	 * @return URI.
	 */
	public String getUri() {
		return this.uri;
	}

}