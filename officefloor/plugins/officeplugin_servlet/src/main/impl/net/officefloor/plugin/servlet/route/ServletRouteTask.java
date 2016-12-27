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
package net.officefloor.plugin.servlet.route;

import javax.servlet.Servlet;

import net.officefloor.frame.api.build.OfficeAwareManagedFunctionFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.context.ServletTaskReference;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * {@link ManagedFunction} for routing {@link HttpRequest} to be serviced by a
 * {@link Servlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletRouteTask
		extends
		AbstractSingleTask<ServletRouteTask, ServletRouteTask.DependencyKeys, ServletRouteTask.FlowKeys>
		implements OfficeAwareManagedFunctionFactory<ServletRouteTask> {

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKeys {
		HTTP_CONNECTION, OFFICE_SERVLET_CONTEXT
	}

	/**
	 * Flow keys.
	 */
	public static enum FlowKeys {
		UNHANDLED
	}

	/**
	 * {@link Office}.
	 */
	private Office office;

	/*
	 * ================= OfficeAwareWorkFactory ===================
	 */

	@Override
	public void setOffice(Office office) {
		this.office = office;
	}

	/*
	 * ======================= Task ===============================
	 */

	@Override
	public Object execute(
			ManagedFunctionContext<ServletRouteTask, DependencyKeys, FlowKeys> context)
			throws Exception {

		// Obtain the path being requested
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(DependencyKeys.HTTP_CONNECTION);
		HttpRequest request = connection.getHttpRequest();
		String path = request.getRequestURI();

		// Obtain the Office Servlet Context
		OfficeServletContext officeServletContext = (OfficeServletContext) context
				.getObject(DependencyKeys.OFFICE_SERVLET_CONTEXT);

		// Obtain the servlet task to service the task
		ServletTaskReference reference = officeServletContext.mapPath(
				this.office, path);
		if (reference == null) {
			// No servlet to service path, therefore unhandled
			context.doFlow(FlowKeys.UNHANDLED, null);
			return null; // no further processing
		}

		// Have servlet handle the request
		context.doFlow(reference.getWorkName(), reference.getTaskName(), null);
		return null;
	}

}