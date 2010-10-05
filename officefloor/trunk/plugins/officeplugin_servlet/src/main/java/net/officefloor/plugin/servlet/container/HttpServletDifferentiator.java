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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.mapping.Servicer;

/**
 * {@link HttpServlet} {@link Task} differentiator.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServletDifferentiator extends Servicer {

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