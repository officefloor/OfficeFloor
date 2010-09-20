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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.servlet.container.HttpServletContainer;
import net.officefloor.plugin.servlet.container.source.HttpServletTask.DependencyKeys;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

/**
 * {@link WorkSource} for a {@link HttpServletContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletWorkSource extends AbstractWorkSource<HttpServletTask> {

	/**
	 * Name of property for the {@link HttpServlet} name.
	 */
	public static final String PROPERTY_SERVLET_NAME = "servlet.name";

	/**
	 * Name of property for the {@link HttpServlet} path.
	 */
	public static final String PROPERTY_SERVLET_PATH = "servlet.path";

	/**
	 * Name of property for the class name of the {@link HttpServlet}
	 * implementation.
	 */
	public static final String PROPERTY_HTTP_SERVLET_CLASS_NAME = "http.servlet.class.name";

	/**
	 * Prefix of property for an initialisation parameter.
	 */
	public static final String PROPERTY_PREFIX_INIT_PARAMETER = "init.parameter.";

	/*
	 * ===================== WorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SERVLET_NAME, "Servlet Name");
		context.addProperty(PROPERTY_SERVLET_PATH, "Servlet Path");
		context.addProperty(PROPERTY_HTTP_SERVLET_CLASS_NAME, "Servlet Class");
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpServletTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the properties
		String servletName = context.getProperty(PROPERTY_SERVLET_NAME);
		String servletPath = context.getProperty(PROPERTY_SERVLET_PATH);
		String servletClassName = context
				.getProperty(PROPERTY_HTTP_SERVLET_CLASS_NAME);

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

		// Create the HTTP Servlet instance
		HttpServlet servlet = (HttpServlet) context.getClassLoader().loadClass(
				servletClassName).newInstance();

		// Construct the HttpServletTask
		HttpServletTask factory = new HttpServletTask(servletName, servletPath,
				servlet, initParameters);

		// Load the type information
		workTypeBuilder.setWorkFactory(factory);

		// Add task to service HTTP request with HTTP Servlet
		TaskTypeBuilder<DependencyKeys, None> task = workTypeBuilder
				.addTaskType("service", factory, DependencyKeys.class,
						None.class);
		task.addObject(ServletContext.class).setKey(
				DependencyKeys.SERVLET_CONTEXT);
		task.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.HTTP_CONNECTION);
		task.addObject(Map.class).setKey(DependencyKeys.REQUEST_ATTRIBUTES);
		task.addObject(HttpSession.class).setKey(DependencyKeys.HTTP_SESSION);
		task.addObject(HttpSecurity.class).setKey(DependencyKeys.HTTP_SECURITY);
		task.addEscalation(ServletException.class);
		task.addEscalation(IOException.class);
	}

}