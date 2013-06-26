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
package net.officefloor.plugin.servlet.filter;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import net.officefloor.plugin.servlet.container.IteratorEnumeration;

/**
 * {@link FilterConfig} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterConfigImpl implements FilterConfig {

	/**
	 * {@link Filter} name.
	 */
	private final String filterName;

	/**
	 * Init parameters.
	 */
	private final Map<String, String> initParameters;

	/**
	 * {@link ServletContext}.
	 */
	private final ServletContext servletContext;

	/**
	 * Initiate.
	 * 
	 * @param filterName
	 *            {@link Filter} name.
	 * @param initParameters
	 *            Init parameters.
	 * @param servletContext
	 *            {@link ServletContext}.
	 */
	public FilterConfigImpl(String filterName,
			Map<String, String> initParameters, ServletContext servletContext) {
		this.filterName = filterName;
		this.initParameters = initParameters;
		this.servletContext = servletContext;
	}

	/*
	 * =================== FilterConfig ===================
	 */

	@Override
	public String getFilterName() {
		return this.filterName;
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

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

}