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
package net.officefloor.plugin.web.template.build;

import java.io.Reader;

/**
 * Creates web templates.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplater {

	/**
	 * Adds a {@link WebTemplate}.
	 * 
	 * @param applicationPath
	 *            Application path to the {@link WebTemplate}. May contain path
	 *            parameters.
	 * @param template
	 *            {@link Reader} to the template content.
	 * @return {@link WebTemplate}.
	 */
	WebTemplate addTemplate(String applicationPath, Reader template);

	/**
	 * Adds a {@link WebTemplate}.
	 * 
	 * @param applicationPath
	 *            Application path to the {@link WebTemplate}. May contain path
	 *            parameters.
	 * @param locationOfTemplate
	 *            Location of the template content.
	 * @return {@link WebTemplate}.
	 */
	WebTemplate addTemplate(String applicationPath, String locationOfTemplate);

}