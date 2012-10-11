/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Servlet} added by the {@link OfficeFloorServletFilter} to ensure all
 * URIs have a serving {@link Servlet} so that the
 * {@link OfficeFloorServletFilter} may intercept and handle the
 * {@link HttpServletRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilteredServlet extends HttpServlet {

	/*
	 * ================ HttpServlet =========================
	 */

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		throw new ServletException("Illegal state: "
				+ OfficeFloorServletFilter.class.getSimpleName()
				+ " implementation should intercept and handle this request ("
				+ request.getRequestURI() + ")");
	}

}