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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;
import net.officefloor.plugin.servlet.security.HttpServletSecurity;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Container for a {@link HttpServlet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServletContainer {

	/**
	 * Services the {@link HttpRequest} of the {@link ServerHttpConnection}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection} containing the
	 *            {@link HttpRequest} to service.
	 * @param attributes
	 *            {@link HttpRequestState}.
	 * @param session
	 *            {@link HttpSession} for the {@link HttpRequest}.
	 * @param security
	 *            {@link HttpServletSecurity}. May be <code>null</code> if
	 *            anonymous {@link HttpRequest}.
	 * @param functionContext
	 *            {@link ManagedFunctionContext} to allow access to
	 *            {@link OfficeFloor} capabilities.
	 * @param mapping
	 *            {@link ServicerMapping} that mapped the {@link HttpRequest} to
	 *            the {@link HttpServlet}. May be <code>null</code> if not
	 *            mapped.
	 * @throws ServletException
	 *             As per {@link HttpServlet} API.
	 * @throws IOException
	 *             As per {@link HttpServlet} API.
	 */
	void service(ServerHttpConnection connection, HttpRequestState attributes, HttpSession session,
			HttpServletSecurity security, ManagedFunctionContext<?, ?> functionContext, ServicerMapping mapping)
			throws ServletException, IOException;

	/**
	 * Includes the servicing of contained {@link HttpServlet} on the inputs.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}.
	 * @param response
	 *            {@link HttpServletResponse}.
	 * @throws ServletException
	 *             As per {@link HttpServlet} API.
	 * @throws IOException
	 *             As per {@link HttpServlet} API.
	 */
	void include(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}