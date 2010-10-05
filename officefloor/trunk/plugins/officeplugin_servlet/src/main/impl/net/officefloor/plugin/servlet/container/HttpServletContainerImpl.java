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

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.context.DispatcherHttpServletRequest;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;
import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.tokenise.HttpRequestTokeniseException;

/**
 * {@link HttpServletContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletContainerImpl implements HttpServletContainer {

	/**
	 * Name of attribute containing the last access time for the
	 * {@link HttpSession}.
	 */
	private static final String ATTRIBUTE_LAST_ACCESS_TIME = "#HttpServlet.LastAccessTime#";

	/**
	 * {@link ServletContext}.
	 */
	private final ServletContext servletContext;

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
	 * @param servlet
	 *            {@link HttpServlet}.
	 * @param initParameters
	 *            Init parameters for the {@link ServletConfig}.
	 * @param officeServletContext
	 *            {@link OfficeServletContext}.
	 * @param office
	 *            {@link Office}.
	 * @param clock
	 *            {@link Clock}.
	 * @param defaultLocale
	 *            Default {@link Locale}.
	 * @throws ServletException
	 *             If fails to initialise the {@link HttpServlet}.
	 */
	public HttpServletContainerImpl(String servletName, HttpServlet servlet,
			Map<String, String> initParameters,
			OfficeServletContext officeServletContext, Office office,
			Clock clock, Locale defaultLocale) throws ServletException {

		// Initiate state
		this.servlet = servlet;
		this.clock = clock;
		this.defaultLocale = defaultLocale;

		// Initate the servlet context
		this.servletContext = new ServletContextImpl(officeServletContext,
				office);

		// Initialise the servlet
		this.servlet.init(new ServletConfigImpl(servletName,
				this.servletContext, initParameters));
	}

	/*
	 * ===================== HttpServletContainer ========================
	 */

	@Override
	public void service(ServerHttpConnection connection,
			Map<String, Object> attributes, HttpSession session,
			HttpSecurity security, TaskContext<?, ?, ?> taskContext,
			ServicerMapping mapping) throws ServletException, IOException {

		// Obtain the last access time
		Long lastAccessTime = (Long) attributes.get(ATTRIBUTE_LAST_ACCESS_TIME);
		if (lastAccessTime == null) {
			// Not available for request, so use last request time
			lastAccessTime = (Long) session
					.getAttribute(ATTRIBUTE_LAST_ACCESS_TIME);
			long currentTime = this.clock.currentTimeMillis();
			if (lastAccessTime == null) {
				// No last request, so use current time
				lastAccessTime = new Long(currentTime);
			}

			// Update request with time so only the one time per request.
			// Above get will return value for further request servicing.
			attributes.put(ATTRIBUTE_LAST_ACCESS_TIME, lastAccessTime);

			// Update session for next request (with time of this request)
			session.setAttribute(ATTRIBUTE_LAST_ACCESS_TIME, new Long(
					currentTime));
		}

		HttpServletRequest request;
		HttpServletResponseImpl response;
		try {
			// Create the HTTP session
			javax.servlet.http.HttpSession httpSession = new HttpSessionImpl(
					session, lastAccessTime.longValue(), this.clock,
					this.servletContext);

			// Obtain the session Id token name
			String sessionIdTokenName = session.getTokenName();

			// Create the HTTP request
			request = new HttpServletRequestImpl(connection, attributes,
					security, sessionIdTokenName, httpSession,
					this.servletContext, this.defaultLocale, taskContext);

			// If have mapping, wrap to provide servicer mapping values
			if (mapping != null) {
				request = new DispatcherHttpServletRequest(mapping, request);
			}

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

		// Serviced so flush buffered content
		response.flushBuffers();
	}

	@Override
	public void include(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Include
		this.servlet.service(request, response);
	}

}