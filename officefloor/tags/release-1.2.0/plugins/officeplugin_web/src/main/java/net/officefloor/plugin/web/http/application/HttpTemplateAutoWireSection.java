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

package net.officefloor.plugin.web.http.application;

import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;

/**
 * Allows wiring the flows of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpTemplateAutoWireSection extends AutoWireSection {

	/**
	 * Obtains path to the template file.
	 * 
	 * @return Path to the template file.
	 */
	String getTemplatePath();

	/**
	 * Obtains the logic class for the template.
	 * 
	 * @return Logic class for the template.
	 */
	Class<?> getTemplateLogicClass();

	/**
	 * Obtains the URI to the template. May be <code>null</code> if not publicly
	 * exposed template.
	 * 
	 * @return URI to the template. May be <code>null</code> if not publicly
	 *         exposed template.
	 */
	String getTemplateUri();

	/**
	 * Adds an {@link HttpTemplateSectionExtension} to this
	 * {@link HttpTemplateAutoWireSection}.
	 * 
	 * @param extensionClass
	 *            Class of the {@link HttpTemplateSectionExtension}.
	 * @return {@link HttpTemplateAutoWireSectionExtension}.
	 */
	HttpTemplateAutoWireSectionExtension addTemplateExtension(
			Class<? extends HttpTemplateSectionExtension> extensionClass);

}