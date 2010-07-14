/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

/**
 * {@link HttpServletContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletContainerImpl implements HttpServletContainer {

	/**
	 * {@link HttpServlet}.
	 */
	private final HttpServlet servlet;

	/**
	 * Initiate.
	 * 
	 * @param servlet
	 *            {@link HttpServlet}.
	 */
	public HttpServletContainerImpl(HttpServlet servlet) {
		this.servlet = servlet;
	}

	/*
	 * ===================== HttpServletContainer ========================
	 */

	@Override
	public void service(ServerHttpConnection connection, HttpSession session)
			throws ServletException, IOException {

		// Create the request and response
		HttpServletRequest request = new HttpServletRequestImpl(connection
				.getHttpRequest());
		HttpServletResponse response = new HttpServletResponseImpl(connection
				.getHttpResponse());

		// Service the request
		this.servlet.service(request, response);
	}

}