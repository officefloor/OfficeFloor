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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.servlet.container.IteratorEnumeration;

/**
 * {@link ServicerMapping} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServicerMappingImpl implements ServicerMapping {

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer servicer;

	/**
	 * {@link HttpServlet} path.
	 */
	private final String servletPath;

	/**
	 * Path info.
	 */
	private final String pathInfo;

	/**
	 * Query string.
	 */
	private final String queryString;

	/**
	 * Parameters.
	 */
	private final Map<String, String[]> parameters;

	/**
	 * Initiate.
	 * 
	 * @param servicer
	 *            {@link HttpServletServicer}.
	 * @param servletPath
	 *            {@link HttpServlet} Path.
	 * @param pathInfo
	 *            Path info.
	 * @param queryString
	 *            Query string.
	 * @param parameters
	 *            Parameters.
	 */
	public ServicerMappingImpl(HttpServletServicer servicer,
			String servletPath, String pathInfo, String queryString,
			Map<String, String[]> parameters) {
		this.servicer = servicer;
		this.servletPath = servletPath;
		this.pathInfo = pathInfo;
		this.queryString = queryString;
		this.parameters = Collections.unmodifiableMap(parameters);
	}

	/*
	 * ======================= ServicerMapping ======================
	 */

	@Override
	public HttpServletServicer getServicer() {
		return this.servicer;
	}

	@Override
	public String getServletPath() {
		return this.servletPath;
	}

	@Override
	public String getPathInfo() {
		return this.pathInfo;
	}

	@Override
	public String getQueryString() {
		return this.queryString;
	}

	@Override
	public String getParameter(String name) {
		String[] values = this.parameters.get(name);
		if (values == null) {
			// No value
			return null;
		} else {
			// Return first value (if available)
			return (values.length > 0 ? values[0] : null);
		}
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return this.parameters;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return new IteratorEnumeration<String>(this.parameters.keySet()
				.iterator());
	}

	@Override
	public String[] getParameterValues(String name) {
		return this.parameters.get(name);
	}

}