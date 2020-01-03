/*-
 * #%L
 * Web Template
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.template.build;

import java.io.Reader;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Factory for the creation of a {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateFactory {

	/**
	 * Adds a {@link WebTemplate}.
	 * 
	 * @param isSecure
	 *            Indicates if requires secure {@link ServerHttpConnection} to
	 *            render the {@link WebTemplate}.
	 * @param applicationPath
	 *            Application path to the {@link WebTemplate}. May contain path
	 *            parameters.
	 * @param templateContent
	 *            {@link Reader} to the template content.
	 * @return {@link WebTemplate}.
	 */
	WebTemplate addTemplate(boolean isSecure, String applicationPath, Reader templateContent);

	/**
	 * Adds a {@link WebTemplate}.
	 * 
	 * @param isSecure
	 *            Indicates if requires secure {@link ServerHttpConnection} to
	 *            render the {@link WebTemplate}.
	 * @param applicationPath
	 *            Application path to the {@link WebTemplate}. May contain path
	 *            parameters.
	 * @param locationOfTemplate
	 *            Location of the template content.
	 * @return {@link WebTemplate}.
	 */
	WebTemplate addTemplate(boolean isSecure, String applicationPath, String locationOfTemplate);

}
