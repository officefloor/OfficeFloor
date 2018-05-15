/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.template.type;

import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplateFactory;

/**
 * Loads the type for the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateLoader extends WebTemplateFactory {

	/**
	 * Loads the {@link WebTemplateType} for the {@link WebTemplate}.
	 * 
	 * @param template
	 *            Configured {@link WebTemplate} to provide the type information.
	 * @return {@link WebTemplateType} for the {@link WebTemplate}.
	 */
	WebTemplateType loadWebTemplateType(WebTemplate template);

}