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
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Mapping to a {@link Servicer}.
 * <p>
 * Interface follows the {@link Servlet} specification (though considers a
 * {@link Servlet} a {@link Servicer} for more generic mapping).
 * 
 * @author Daniel Sagenschneider
 */
public interface ServicerMapping {

	/**
	 * Mapped {@link Servicer}.
	 * 
	 * @return Mapped {@link Servicer}.
	 */
	Servicer getServicer();

	/**
	 * Obtains the path segment that mapped to the {@link Servicer}.
	 * 
	 * @return {@link Servicer} path.
	 * 
	 * @see HttpServletRequest#getServletPath()
	 */
	String getServicerPath();

	/**
	 * Obtains the remaining of the path after the {@link Servicer} path.
	 * 
	 * @return Remaining path or <code>null</code> if exact mapping.
	 */
	String getPathInfo();

	/**
	 * Query string after the path.
	 * 
	 * @return Query string after the path or <code>null</code> if no query
	 *         string.
	 */
	String getQueryString();

	/**
	 * Obtains the parameter value as per the query string.
	 * 
	 * @param name
	 *            Name of parameter.
	 * @return Value for the parameter or <code>null</code> if parameter not on
	 *         query string.
	 */
	String getParameter(String name);

}