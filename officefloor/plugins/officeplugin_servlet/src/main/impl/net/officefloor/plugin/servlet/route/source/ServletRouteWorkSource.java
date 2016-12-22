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
package net.officefloor.plugin.servlet.route.source;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.route.ServletRouteTask;
import net.officefloor.plugin.servlet.route.ServletRouteTask.DependencyKeys;
import net.officefloor.plugin.servlet.route.ServletRouteTask.FlowKeys;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * {@link ManagedFunctionSource} to route {@link HttpRequest} to a {@link Servlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletRouteWorkSource extends
		AbstractWorkSource<ServletRouteTask> {

	/**
	 * Name of {@link ManagedFunction} to route {@link HttpRequest}.
	 */
	public static final String TASK_ROUTE = "route";

	/*
	 * ======================== WorkSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder<ServletRouteTask> workTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Create the factory (task)
		ServletRouteTask factory = new ServletRouteTask();

		// Specify the work
		workTypeBuilder.setWorkFactory(factory);

		// Add task to route
		ManagedFunctionTypeBuilder<DependencyKeys, FlowKeys> task = workTypeBuilder
				.addManagedFunctionType(TASK_ROUTE, factory, DependencyKeys.class,
						FlowKeys.class);
		task.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.HTTP_CONNECTION);
		task.addObject(OfficeServletContext.class).setKey(
				DependencyKeys.OFFICE_SERVLET_CONTEXT);
		task.addFlow().setKey(FlowKeys.UNHANDLED);
	}

}