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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Container for a {@link Filter}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FilterContainer {

	/**
	 * Does the filtering.
	 * 
	 * @param request
	 *            {@link ServletRequest}.
	 * @param response
	 *            {@link ServletResponse}.
	 * @param chain
	 *            {@link FilterChain}.
	 * @throws IOException
	 *             As per {@link Filter} interface.
	 * @throws ServletException
	 *             As per {@link Filter} interface.
	 */
	void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException;

}