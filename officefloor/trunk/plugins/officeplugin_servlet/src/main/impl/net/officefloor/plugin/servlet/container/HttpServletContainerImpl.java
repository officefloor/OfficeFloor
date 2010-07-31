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
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.servlet.dispatch.RequestDispatcherFactory;
import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.resource.ResourceLocator;
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
	 * {@link ServletContext} name.
	 */
	private final String servletContextName;

	/**
	 * Context Path.
	 */
	private final String contextPath;

	/**
	 * Context parameters.
	 */
	private final Map<String, String> contextParameters;

	/**
	 * Servlet Path.
	 */
	private final String servletPath;

	/**
	 * {@link HttpServlet}.
	 */
	private final HttpServlet servlet;

	/**
	 * Name of identifier (e.g. cookie or parameter name) providing the session
	 * Id.
	 */
	private final String sessionIdIdentifierName;

	/**
	 * {@link RequestDispatcherFactory}.
	 */
	private final RequestDispatcherFactory dispatcherFactory;

	/**
	 * Mapping file extensions to MIME type.
	 */
	private final Map<String, String> fileExtensionToMimeType;

	/**
	 * {@link Clock}.
	 */
	private final Clock clock;

	/**
	 * {@link ResourceLocator}.
	 */
	private final ResourceLocator resourceLocator;

	/**
	 * {@link Logger}.
	 */
	private final Logger logger;

	/**
	 * Initiate.
	 * 
	 * @param servletContextName
	 *            {@link ServletContext} name.
	 * @param servletName
	 *            Name of the Servlet.
	 * @param contextPath
	 *            Context Path.
	 * @param contextParameters
	 *            Context parameters.
	 * @param servletPath
	 *            Servlet Path.
	 * @param servlet
	 *            {@link HttpServlet}.
	 * @param initParameters
	 *            Init parameters for the {@link ServletConfig}.
	 * @param context
	 *            {@link ServletContext}.
	 * @param sessionIdIdentifierName
	 *            Name of identifier (e.g. cookie or parameter name) providing
	 *            the session Id.
	 * @param dispatcherFactory
	 *            {@link RequestDispatcherFactory}.
	 * @param fileExtensionToMimeType
	 *            Mapping file extensions to MIME type.
	 * @param clock
	 *            {@link Clock}.
	 * @param resourceLocator
	 *            {@link ResourceLocator}.
	 * @param logger
	 *            {@link Logger}.
	 * @throws ServletException
	 *             If fails to initialise the {@link HttpServlet}.
	 */
	public HttpServletContainerImpl(String servletContextName,
			String contextPath, Map<String, String> contextParameters,
			String servletName, String servletPath, HttpServlet servlet,
			Map<String, String> initParameters, ServletContext context,
			String sessionIdIdentifierName,
			RequestDispatcherFactory dispatcherFactory,
			Map<String, String> fileExtensionToMimeType, Clock clock,
			ResourceLocator resourceLocator, Logger logger)
			throws ServletException {

		this.servletContextName = servletContextName;
		this.contextPath = contextPath;
		this.contextParameters = contextParameters;
		this.servletPath = servletPath;
		this.servlet = servlet;
		this.sessionIdIdentifierName = sessionIdIdentifierName;
		this.dispatcherFactory = dispatcherFactory;
		this.fileExtensionToMimeType = fileExtensionToMimeType;
		this.clock = clock;
		this.resourceLocator = resourceLocator;
		this.logger = logger;

		// Initialise the servlet
		this.servlet.init(new ServletConfigImpl(servletName, context,
				initParameters));
	}

	/*
	 * ===================== HttpServletContainer ========================
	 */

	@Override
	public void service(ServerHttpConnection connection,
			Map<String, Object> attributes, HttpSecurity security,
			long lastAccessTime, HttpSession session,
			ContextAttributes contextAttributes) throws ServletException,
			IOException {

		// Create the request and response
		HttpServletRequest request;
		HttpServletResponse response;
		try {
			request = new HttpServletRequestImpl(this.servletContextName,
					this.contextPath, this.contextParameters,
					contextAttributes, connection, this.servletPath,
					attributes, security, this.sessionIdIdentifierName,
					lastAccessTime, session, this.fileExtensionToMimeType,
					this.dispatcherFactory, this.clock, this.resourceLocator,
					this.logger);
			response = new HttpServletResponseImpl(connection.getHttpResponse());
		} catch (HttpRequestTokeniseException ex) {
			// Propagate invalid HTTP Request
			throw new IOException(ex);
		}

		// Service the request
		this.servlet.service(request, response);
	}

}