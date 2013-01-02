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
package net.officefloor.plugin.servlet.mapping;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import net.officefloor.plugin.servlet.container.HttpServletServicer;

/**
 * <p>
 * Mapping to a {@link HttpServletServicer}.
 * <p>
 * Interface follows the {@link Servlet} specification.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServicerMapping {

	/**
	 * Mapped {@link HttpServletServicer}.
	 * 
	 * @return Mapped {@link HttpServletServicer}.
	 */
	HttpServletServicer getServicer();

	/**
	 * Obtains the path segment that mapped to the {@link HttpServletServicer}.
	 * 
	 * @return {@link HttpServlet} path.
	 * 
	 * @see HttpServletRequest#getServletPath()
	 */
	String getServletPath();

	/**
	 * Obtains the remaining of the path after the {@link HttpServletServicer}
	 * path.
	 * 
	 * @return Remaining path or <code>null</code> if exact mapping.
	 * 
	 * @see HttpServletRequest#getPathInfo()
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

	/**
	 * Obtains the names of the parameters.
	 * 
	 * @return Names of the parameters.
	 */
	Enumeration<String> getParameterNames();

	/**
	 * Obtains the values for the parameter name.
	 * 
	 * @param name
	 *            Name of the parameter.
	 * @return Values for the parameter.
	 */
	String[] getParameterValues(String name);

	/**
	 * Obtains the parameter map.
	 * 
	 * @return Parameter map.
	 */
	Map<String, String[]> getParameterMap();

}