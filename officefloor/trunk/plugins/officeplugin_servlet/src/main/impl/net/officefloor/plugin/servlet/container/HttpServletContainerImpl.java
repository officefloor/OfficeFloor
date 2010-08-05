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
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.servlet.security.HttpSecurity;
import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.tokenise.HttpRequestTokeniseException;

/**
 * {@link HttpServletContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletContainerImpl implements HttpServletContainer {

	/**
	 * {@link ServletContext}.
	 */
	private final ServletContext servletContext;

	/**
	 * Servlet Path.
	 */
	private final String servletPath;

	/**
	 * {@link HttpServlet}.
	 */
	private final HttpServlet servlet;

	/**
	 * {@link Clock}.
	 */
	private final Clock clock;

	/**
	 * Default {@link Locale}.
	 */
	private final Locale defaultLocale;

	/**
	 * Initiate.
	 * 
	 * @param servletName
	 *            Name of the Servlet.
	 * @param servletPath
	 *            Servlet Path.
	 * @param servlet
	 *            {@link HttpServlet}.
	 * @param initParameters
	 *            Init parameters for the {@link ServletConfig}.
	 * @param servletContext
	 *            {@link ServletContext}.
	 * @param clock
	 *            {@link Clock}.
	 * @param defaultLocale
	 *            Default {@link Locale}.
	 * @throws ServletException
	 *             If fails to initialise the {@link HttpServlet}.
	 */
	public HttpServletContainerImpl(String servletName, String servletPath,
			HttpServlet servlet, Map<String, String> initParameters,
			ServletContext servletContext, Clock clock, Locale defaultLocale)
			throws ServletException {

		// Initiate state
		this.servletPath = servletPath;
		this.servlet = servlet;
		this.servletContext = servletContext;
		this.clock = clock;
		this.defaultLocale = defaultLocale;

		// Initialise the servlet
		this.servlet.init(new ServletConfigImpl(servletName,
				this.servletContext, initParameters));
	}

	/*
	 * ===================== HttpServletContainer ========================
	 */

	@Override
	public void service(ServerHttpConnection connection,
			Map<String, Object> attributes, HttpSecurity security,
			long lastAccessTime, HttpSession session) throws ServletException,
			IOException {

		HttpServletRequest request;
		HttpServletResponse response;
		try {
			// Create the HTTP session
			javax.servlet.http.HttpSession httpSession = new HttpSessionImpl(
					session, lastAccessTime, this.clock, this.servletContext);

			// Obtain the session Id token name
			String sessionIdTokenName = session.getTokenName();

			// Create the HTTP request
			request = new HttpServletRequestImpl(connection, this.servletPath,
					attributes, security, sessionIdTokenName, httpSession,
					this.servletContext, this.defaultLocale);

			// Create the HTTP response
			response = new HttpServletResponseImpl(
					connection.getHttpResponse(), this.clock, request,
					this.defaultLocale);

		} catch (HttpRequestTokeniseException ex) {
			// Propagate invalid HTTP Request
			throw new IOException(ex);
		}

		// Service the request
		this.servlet.service(request, response);
	}

}