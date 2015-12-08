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
package net.officefloor.plugin.servlet.container;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * {@link ServletConfig} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletConfigImpl implements ServletConfig {

	/**
	 * Servlet name.
	 */
	private final String servletName;

	/**
	 * {@link ServletContext}.
	 */
	private final ServletContext servletContext;

	/**
	 * Init parameters.
	 */
	private final Map<String, String> initParameters;

	/**
	 * Initiate.
	 * 
	 * @param servletName
	 *            Servlet name.
	 * @param servletContext
	 *            {@link ServletContext}.
	 * @param initParameters
	 *            Init parameters.
	 */
	public ServletConfigImpl(String servletName, ServletContext servletContext,
			Map<String, String> initParameters) {
		this.servletName = servletName;
		this.servletContext = servletContext;
		this.initParameters = initParameters;
	}

	/*
	 * ================== ServletConfig =======================
	 */

	@Override
	public String getServletName() {
		return this.servletName;
	}

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Override
	public String getInitParameter(String name) {
		return this.initParameters.get(name);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getInitParameterNames() {
		return new IteratorEnumeration<String>(this.initParameters.keySet()
				.iterator());
	}

}