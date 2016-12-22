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

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.mapping.ServicerMapper;

/**
 * {@link HttpServlet} servicer that is also the differentiator type for a
 * {@link HttpServlet} {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServletServicer {

	/**
	 * <p>
	 * Obtains the name of this {@link HttpServletServicer}.
	 * <p>
	 * This will be used to find this {@link HttpServletServicer} by name.
	 * 
	 * @return {@link HttpServletServicer} name.
	 */
	String getServletName();

	/**
	 * <p>
	 * Obtains the mappings that are handled by this {@link HttpServletServicer}.
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
	 * @return Mappings that are handled by this {@link HttpServletServicer}.
	 * 
	 * @see ServicerMapper
	 */
	String[] getServletMappings();

	/**
	 * Includes the content from this {@link HttpServlet}.
	 * 
	 * @param context
	 *            {@link OfficeServletContext}.
	 * @param request
	 *            {@link HttpServletRequest}.
	 * @param response
	 *            {@link HttpServletResponse}.
	 * @throws ServletException
	 *             As per {@link RequestDispatcher}.
	 * @throws IOException
	 *             As per {@link RequestDispatcher}.
	 */
	void include(OfficeServletContext context, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException;

}