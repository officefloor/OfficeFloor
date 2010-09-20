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
package net.officefloor.plugin.servlet.container.source;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.servlet.container.HttpServletContainer;
import net.officefloor.plugin.servlet.container.HttpServletContainerImpl;
import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

/**
 * {@link Task} for a {@link HttpServlet} to service a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletTask
		extends
		AbstractSingleTask<HttpServletTask, HttpServletTask.DependencyKeys, None> {

	/**
	 * Keys for the dependencies.
	 */
	public static enum DependencyKeys {
		SERVLET_CONTEXT, HTTP_CONNECTION, REQUEST_ATTRIBUTES, HTTP_SECURITY, HTTP_SESSION
	}

	/**
	 * {@link Clock}.
	 */
	private static final Clock CLOCK = new Clock() {
		@Override
		public long currentTimeMillis() {
			return System.currentTimeMillis();
		}
	};

	/**
	 * Name of the {@link HttpServlet}.
	 */
	private final String servletName;

	/**
	 * Path of the {@link HttpServlet}.
	 */
	private final String servletPath;

	/**
	 * {@link HttpServlet} to service the {@link HttpRequest}.
	 */
	private final HttpServlet servlet;

	/**
	 * Initialisation parameters.
	 */
	private final Map<String, String> initParameters;

	/**
	 * {@link HttpServletContainer}.
	 */
	private HttpServletContainer container = null;

	/**
	 * Initiate.
	 * 
	 * @param servletName
	 *            Name of the {@link HttpServlet}.
	 * @param servletPath
	 *            Path of the {@link HttpServlet}.
	 * @param servlet
	 *            {@link HttpServlet} to service the {@link HttpRequest}.
	 * @param initParameters
	 *            Initialisation parameters.
	 */
	public HttpServletTask(String servletName, String servletPath,
			HttpServlet servlet, Map<String, String> initParameters) {
		this.servletName = servletName;
		this.servletPath = servletPath;
		this.servlet = servlet;
		this.initParameters = initParameters;
	}

	/*
	 * ====================== Task ================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Object doTask(
			TaskContext<HttpServletTask, DependencyKeys, None> context)
			throws ServletException, IOException {

		// Lazy load the HTTP Servlet Container
		synchronized (CLOCK) {
			if (this.container == null) {
				// Obtain the Servlet Context
				ServletContext servletContext = (ServletContext) context
						.getObject(DependencyKeys.SERVLET_CONTEXT);

				// TODO consider configuring the Locale
				Locale locale = Locale.getDefault();

				// Create the HTTP Servlet Container
				this.container = new HttpServletContainerImpl(this.servletName,
						this.servletPath, this.servlet, this.initParameters,
						servletContext, CLOCK, locale);
			}
		}

		// Obtain the parameters for servicing the request
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(DependencyKeys.HTTP_CONNECTION);
		Map<String, Object> attributes = (Map<String, Object>) context
				.getObject(DependencyKeys.REQUEST_ATTRIBUTES);
		HttpSession session = (HttpSession) context
				.getObject(DependencyKeys.HTTP_SESSION);
		HttpSecurity security = (HttpSecurity) context
				.getObject(DependencyKeys.HTTP_SECURITY);

		// Service the request
		this.container.service(connection, attributes, session, security);

		// Nothing to return
		return null;
	}

}