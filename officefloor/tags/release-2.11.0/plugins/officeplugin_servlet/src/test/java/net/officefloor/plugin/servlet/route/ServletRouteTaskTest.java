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

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.context.ServletTaskReference;
import net.officefloor.plugin.servlet.route.ServletRouteTask.DependencyKeys;
import net.officefloor.plugin.servlet.route.ServletRouteTask.FlowKeys;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Tests the {@link ServletRouteTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletRouteTaskTest extends OfficeFrameTestCase {

	/**
	 * {@link ServletRouteTask} being tested.
	 */
	private final ServletRouteTask task = new ServletRouteTask();

	/**
	 * {@link Office}.
	 */
	private final Office office = this.createMock(Office.class);

	/**
	 * {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<ServletRouteTask, DependencyKeys, FlowKeys> taskContext = this
			.createMock(TaskContext.class);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * {@link OfficeServletContext}.
	 */
	private final OfficeServletContext officeServletContext = this
			.createMock(OfficeServletContext.class);

	/**
	 * {@link ServletTaskReference}.
	 */
	private final ServletTaskReference reference = this
			.createMock(ServletTaskReference.class);

	@Override
	protected void setUp() throws Exception {
		// Provide office as Office aware
		this.task.setOffice(this.office);
	}

	/**
	 * Ensure can route to {@link Servlet}.
	 */
	public void testRouteToServlet() throws Throwable {

		final String PATH = "/path?name=value";
		final String WORK_NAME = "WORK";
		final String TASK_NAME = "TASK";

		// Record
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.HTTP_CONNECTION), this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), PATH);
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.OFFICE_SERVLET_CONTEXT),
				this.officeServletContext);
		this.recordReturn(this.officeServletContext, this.officeServletContext
				.mapPath(this.office, PATH), this.reference);
		this.recordReturn(this.reference, this.reference.getWorkName(),
				WORK_NAME);
		this.recordReturn(this.reference, this.reference.getTaskName(),
				TASK_NAME);
		this.taskContext.doFlow(WORK_NAME, TASK_NAME, null);

		// Test
		this.replayMockObjects();
		this.task.doTask(this.taskContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure indicates if no appropriate {@link Servlet} to handle.
	 */
	public void testNoHandlingServlet() throws Throwable {

		final String PATH = "/unknown";

		// Record
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.HTTP_CONNECTION), this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), PATH);
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.OFFICE_SERVLET_CONTEXT),
				this.officeServletContext);
		this.recordReturn(this.officeServletContext, this.officeServletContext
				.mapPath(this.office, PATH), null);
		this.recordReturn(this.taskContext, this.taskContext.doFlow(
				FlowKeys.UNHANDLED, null), null);

		// Test
		this.replayMockObjects();
		this.task.doTask(this.taskContext);
		this.verifyMockObjects();
	}

}