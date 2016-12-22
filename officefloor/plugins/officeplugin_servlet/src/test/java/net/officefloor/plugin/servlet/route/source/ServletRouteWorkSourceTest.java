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

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.route.ServletRouteTask;
import net.officefloor.plugin.servlet.route.ServletRouteTask.DependencyKeys;
import net.officefloor.plugin.servlet.route.ServletRouteTask.FlowKeys;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Tests the {@link ServletRouteWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletRouteWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(ServletRouteWorkSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		final ServletRouteTask factory = new ServletRouteTask();

		// Create expected type
		FunctionNamespaceBuilder<ServletRouteTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(factory);
		ManagedFunctionTypeBuilder<DependencyKeys, FlowKeys> task = type.addManagedFunctionType(
				ServletRouteWorkSource.TASK_ROUTE, factory,
				DependencyKeys.class, FlowKeys.class);
		task.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.HTTP_CONNECTION);
		task.addObject(OfficeServletContext.class).setKey(
				DependencyKeys.OFFICE_SERVLET_CONTEXT);
		task.addFlow().setKey(FlowKeys.UNHANDLED);

		// Validate type
		WorkLoaderUtil.validateWorkType(type, ServletRouteWorkSource.class);
	}

}