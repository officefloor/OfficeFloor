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

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;

import org.apache.jasper.servlet.JspServlet;

/**
 * {@link WorkSource} to service a {@link HttpRequest} via a {@link JspServlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class JspWorkSource extends AbstractWorkSource<HttpServletTask> {

	/**
	 * Prefix of property for an initialisation parameter. This allows for
	 * additional configuration of the {@link JspServlet}.
	 */
	public static final String PROPERTY_PREFIX_INIT_PARAMETER = HttpServletTask.PROPERTY_PREFIX_INIT_PARAMETER;

	/**
	 * Name of property to obtain overriding mappings to the JSP.
	 */
	public static final String PROPERTY_SERVLET_MAPPINGS = HttpServletTask.PROPERTY_SERVLET_MAPPINGS;

	/*
	 * ======================== WorkSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpServletTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Create the JSP Servlet
		HttpServlet servlet = new JspServlet();

		// Source the JSP work
		HttpServletTask.sourceWork(workTypeBuilder, context, "JSP", servlet,
				"*.jsp");
	}

}