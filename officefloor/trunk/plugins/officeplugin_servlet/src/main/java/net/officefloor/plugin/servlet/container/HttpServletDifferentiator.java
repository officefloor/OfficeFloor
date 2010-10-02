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
package net.officefloor.plugin.servlet.container;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.plugin.servlet.context.OfficeServletContext;

/**
 * {@link HttpServlet} {@link Task} differentiator.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServletDifferentiator {

	/**
	 * Obtains the path for the {@link HttpServlet}. The path corresponds to the
	 * path used to obtain the {@link RequestDispatcher} from the
	 * {@link ServletContext}.
	 * 
	 * @return Path for the {@link HttpServlet}.
	 * 
	 * @see ServletContext#getRequestDispatcher(String)
	 */
	String getServletPath();

	/**
	 * Obtains the name for the {@link HttpServlet}. The name corresponds to the
	 * name used to obtain the {@link RequestDispatcher} from the
	 * {@link ServletContext}.
	 * 
	 * @return Name for the {@link HttpServlet}.
	 * 
	 * @see ServletContext#getNamedDispatcher(String)
	 */
	String getServletName();

	/**
	 * Obtains the path extensions that may be handled by this
	 * {@link HttpServlet}.
	 * 
	 * @return Listing of path extensions or <code>null</code> if can not handle
	 *         based on extension.
	 */
	String[] getExtensions();

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