/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.mapping;

import javax.servlet.Servlet;

import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * Servicer of a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Servicer {

	/**
	 * <p>
	 * Obtains the name of this {@link Servicer}.
	 * <p>
	 * This will be used to find this {@link Servicer} by name.
	 * 
	 * @return {@link Servicer} name.
	 */
	String getServicerName();

	/**
	 * <p>
	 * Obtains the mappings that are handled by this {@link Servicer}.
	 * <p>
	 * The mappings are of the form:
	 * <ol>
	 * <li><code>/some/path.extension</code>: for exact mapping (extension is
	 * optional)</li>
	 * <li><code>/wild/card/*</code>: for path wild card mapping</li>
	 * <li><code>*.extension</code>: for extension mapping</li>
	 * </ol>
	 * <p>
	 * This follows the {@link Servlet} specification in regards to mappings.
	 * 
	 * @return Mappings that are handled by this {@link Servicer}.
	 * 
	 * @see ServicerMapper
	 */
	String[] getServicerMappings();

}