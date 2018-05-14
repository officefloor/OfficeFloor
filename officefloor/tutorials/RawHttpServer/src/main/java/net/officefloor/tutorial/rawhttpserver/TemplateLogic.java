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
package net.officefloor.tutorial.rawhttpserver;

import net.officefloor.web.template.NotEscaped;

/**
 * Example template logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateLogic {

	/**
	 * Able to use <code>this</code> as bean for populating.
	 * 
	 * @return {@link TemplateLogic} to provide properties.
	 */
	public TemplateLogic getTemplateData() {
		return this;
	}

	/**
	 * Provides the raw HTML to render. It is not escaped due to the
	 * {@link NotEscaped} annotation.
	 * 
	 * @return Raw HTML to render.
	 */
	@NotEscaped
	public String getRawHtml() {
		return "<p style=\"color: blue\">" + "<img src=\"./images/OfficeFloorLogo.png\" />"
				+ " Web on OfficeFloor (WoOF)</p>";
	}

}
// END SNIPPET: example