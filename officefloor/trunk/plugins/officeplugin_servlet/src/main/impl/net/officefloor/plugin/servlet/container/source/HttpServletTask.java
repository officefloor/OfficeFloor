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
package net.officefloor.plugin.servlet.container.source;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeAwareWorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.servlet.container.HttpServletContainer;
import net.officefloor.plugin.servlet.container.HttpServletContainerImpl;
import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;
import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link Task} for a {@link HttpServlet} to service a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletTask
		extends
		AbstractSingleTask<HttpServletTask, HttpServletTask.DependencyKeys, None>
		implements OfficeAwareWorkFactory<HttpServletTask>, HttpServletServicer {

	/**
	 * Prefix of property for an initialisation parameter.
	 */
	public static final String PROPERTY_PREFIX_INIT_PARAMETER = "init.parameter.";

	/**
	 * Name of property to obtain mappings to the {@link HttpServlet}. Mappings
	 * are comma (,) separated.
	 */
	public static final String PROPERTY_SERVLET_MAPPINGS = "servlet.mappings";

	/**
	 * Name of the servicing {@link Task} for the {@link Servlet}.
	 */
	public static final String TASK_NAME = "service";

	/**
	 * Keys for the dependencies.
	 */
	public static enum DependencyKeys {
		SERVICER_MAPPING, OFFICE_SERVLET_CONTEXT, HTTP_CONNECTION, REQUEST_ATTRIBUTES, HTTP_SESSION, HTTP_SECURITY
	}

	/**
	 * Sources the {@link Work} for a {@link HttpServletTask}.
	 * 
	 * @param workTypeBuilder
	 *            {@link WorkTypeBuilder}.
	 * @param context
	 *            {@link WorkSourceContext}.
	 * @param servletName
	 *            Servlet name.
	 * @param servlet
	 *            {@link HttpServlet}.
	 * @param servletMappings
	 *            Mappings to the {@link HttpServlet}.
	 */
	public static void sourceWork(
			WorkTypeBuilder<HttpServletTask> workTypeBuilder,
			WorkSourceContext context, String servletName, HttpServlet servlet,
			String... servletMappings) {

		// Obtain the initialisation parameters
		Map<String, String> initParameters = new HashMap<String, String>();
		for (String propertyName : context.getPropertyNames()) {
			if (propertyName.startsWith(PROPERTY_PREFIX_INIT_PARAMETER)) {
				String parameterName = propertyName
						.substring(PROPERTY_PREFIX_INIT_PARAMETER.length());
				String parameterValue = context.getProperty(propertyName);
				initParameters.put(parameterName, parameterValue);
			}
		}

		// Determine if overriding servlet mappings
		String servletMappingsText = context.getProperty(
				PROPERTY_SERVLET_MAPPINGS, null);
		if (servletMappingsText != null) {
			// Override the mappings (trimming for use)
			servletMappings = servletMappingsText.split(",");
			for (int i = 0; i < servletMappings.length; i++) {
				servletMappings[i] = servletMappings[i].trim();
			}
		}

		// Construct the HttpServletTask
		HttpServletTask factory = new HttpServletTask(servletName, servlet,
				initParameters, servletMappings);

		// Load the type information
		workTypeBuilder.setWorkFactory(factory);

		// Add task to service HTTP request with HTTP Servlet
		TaskTypeBuilder<DependencyKeys, None> task = workTypeBuilder
				.addTaskType(TASK_NAME, factory, DependencyKeys.class,
						None.class);
		task.setDifferentiator(factory);
		task.addObject(ServicerMapping.class).setKey(
				DependencyKeys.SERVICER_MAPPING);
		task.addObject(OfficeServletContext.class).setKey(
				DependencyKeys.OFFICE_SERVLET_CONTEXT);
		task.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.HTTP_CONNECTION);
		task.addObject(HttpRequestState.class).setKey(
				DependencyKeys.REQUEST_ATTRIBUTES);
		task.addObject(HttpSession.class).setKey(DependencyKeys.HTTP_SESSION);
		task.addObject(HttpSecurity.class).setKey(DependencyKeys.HTTP_SECURITY);
		task.addEscalation(ServletException.class);
		task.addEscalation(IOException.class);
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
	 * {@link HttpServlet} to service the {@link HttpRequest}.
	 */
	private final HttpServlet servlet;

	/**
	 * Initialisation parameters.
	 */
	private final Map<String, String> initParameters;

	/**
	 * Mappings to the {@link HttpServlet}.
	 */
	private final String[] servletMappings;

	/**
	 * {@link Office}.
	 */
	private Office office;

	/**
	 * {@link HttpServletContainer}.
	 */
	private HttpServletContainer container = null;

	/**
	 * Initiate.
	 * 
	 * @param servletName
	 *            Name of the {@link HttpServlet}.
	 * @param servlet
	 *            {@link HttpServlet} to service the {@link HttpRequest}.
	 * @param initParameters
	 *            Initialisation parameters.
	 * @param servletMappings
	 *            Mappings to the {@link HttpServlet}.
	 */
	public HttpServletTask(String servletName, HttpServlet servlet,
			Map<String, String> initParameters, String... servletMappings) {
		this.servletName = servletName;
		this.servlet = servlet;
		this.initParameters = initParameters;
		this.servletMappings = servletMappings;
	}

	/**
	 * Obtains the {@link HttpServletContainer}.
	 * 
	 * @param officeServletContext
	 *            {@link OfficeServletContext}.
	 * @return {@link HttpServletContainer}.
	 * @throws ServletException
	 *             If fails to create {@link HttpServletContainer}.
	 */
	private HttpServletContainer getHttpServletContainer(
			OfficeServletContext officeServletContext) throws ServletException {

		// Lazy load the HTTP Servlet Container
		synchronized (this) {
			if (this.container == null) {

				// TODO consider configuring the Locale
				Locale locale = Locale.getDefault();

				// Create the HTTP Servlet Container
				this.container = new HttpServletContainerImpl(this.servletName,
						this.servlet, this.initParameters,
						officeServletContext, this.office, CLOCK, locale);
			}
		}

		// Return the container
		return this.container;
	}

	/*
	 * ================== OfficeAwareWorkFactory ===========================
	 */

	@Override
	public void setOffice(Office office) throws Exception {
		this.office = office;
	}

	/*
	 * ====================== Task ================================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpServletTask, DependencyKeys, None> context)
			throws ServletException, IOException {

		// Obtain the HTTP Servlet Container
		OfficeServletContext officeServletContext = (OfficeServletContext) context
				.getObject(DependencyKeys.OFFICE_SERVLET_CONTEXT);
		HttpServletContainer container = this
				.getHttpServletContainer(officeServletContext);

		// Obtain the parameters for servicing the request
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(DependencyKeys.HTTP_CONNECTION);
		HttpRequestState attributes = (HttpRequestState) context
				.getObject(DependencyKeys.REQUEST_ATTRIBUTES);
		HttpSession session = (HttpSession) context
				.getObject(DependencyKeys.HTTP_SESSION);
		HttpSecurity security = (HttpSecurity) context
				.getObject(DependencyKeys.HTTP_SECURITY);
		ServicerMapping mapping = (ServicerMapping) context
				.getObject(DependencyKeys.SERVICER_MAPPING);

		// Service the request
		container.service(connection, attributes, session, security, context,
				mapping);

		// Nothing to return
		return null;
	}

	/*
	 * ===================== HttpServletServicer ===================
	 */

	@Override
	public String getServletName() {
		return this.servletName;
	}

	@Override
	public String[] getServletMappings() {
		return this.servletMappings;
	}

	@Override
	public void include(OfficeServletContext context,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Obtain the HTTP Servlet Container
		HttpServletContainer container = this.getHttpServletContainer(context);

		// Include
		container.include(request, response);
	}

}