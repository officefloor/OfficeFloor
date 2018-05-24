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

import javax.servlet.http.HttpServlet;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractWorkSource;
import net.officefloor.plugin.servlet.container.HttpServletContainer;

/**
 * {@link ManagedFunctionSource} for a {@link HttpServletContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletWorkSource extends AbstractWorkSource<HttpServletTask> {

	/**
	 * Name of property for the {@link HttpServlet} name.
	 */
	public static final String PROPERTY_SERVLET_NAME = "servlet.name";

	/**
	 * Name of property for the {@link HttpServlet} mappings.
	 */
	public static final String PROPERTY_SERVLET_MAPPINGS = HttpServletTask.PROPERTY_SERVLET_MAPPINGS;

	/**
	 * Name of property for the class name of the {@link HttpServlet}
	 * implementation.
	 */
	public static final String PROPERTY_HTTP_SERVLET_CLASS_NAME = "http.servlet.class.name";

	/**
	 * Prefix of property for an initialisation parameter.
	 */
	public static final String PROPERTY_PREFIX_INIT_PARAMETER = HttpServletTask.PROPERTY_PREFIX_INIT_PARAMETER;

	/*
	 * ===================== WorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SERVLET_NAME, "Servlet Name");
		context.addProperty(PROPERTY_HTTP_SERVLET_CLASS_NAME, "Servlet Class");
		context.addProperty(PROPERTY_SERVLET_MAPPINGS, "Servlet Mappings");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder<HttpServletTask> workTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the properties
		String servletName = context.getProperty(PROPERTY_SERVLET_NAME);
		String servletClassName = context
				.getProperty(PROPERTY_HTTP_SERVLET_CLASS_NAME);

		// Ensure servlet mappings configured
		context.getProperty(PROPERTY_SERVLET_MAPPINGS);

		// Create the HTTP Servlet instance
		HttpServlet servlet = (HttpServlet) context.loadClass(servletClassName)
				.newInstance();

		// Source the HTTP Servlet work
		HttpServletTask.sourceWork(workTypeBuilder, context, servletName,
				servlet);
	}

}