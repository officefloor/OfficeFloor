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

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.container.ServletContextImpl;
import net.officefloor.plugin.servlet.context.OfficeServletContext;

/**
 * {@link FilterContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterContainerImpl implements FilterContainer {

	/**
	 * {@link Filter}.
	 */
	private final Filter filter;

	/**
	 * Initiate.
	 * 
	 * @param filterName
	 *            Name of {@link Filter}.
	 * @param filter
	 *            {@link Filter}.
	 * @param initParameters
	 *            Initialise parameters.
	 * @param officeServletContext
	 *            {@link OfficeServletContext}.
	 * @param office
	 *            {@link Office}.
	 * @throws ServletException
	 *             If fails to initialise {@link Filter}.
	 */
	public FilterContainerImpl(String filterName, Filter filter,
			Map<String, String> initParameters,
			OfficeServletContext officeServletContext, Office office)
			throws ServletException {
		this.filter = filter;

		// Construct the servlet context
		ServletContext servletContext = new ServletContextImpl(
				officeServletContext, office);

		// Initialise the filter
		FilterConfig config = new FilterConfigImpl(filterName, initParameters,
				servletContext);
		this.filter.init(config);
	}

	/*
	 * ======================= FilterContainer ======================
	 */

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		this.filter.doFilter(request, response, chain);
	}

}