/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.secure;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.secure.HttpSecureTask.HttpSecureTaskDependencies;
import net.officefloor.plugin.web.http.secure.HttpSecureTask.HttpSecureTaskFlows;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link HttpSecureTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecureTaskTest extends OfficeFrameTestCase {

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * {@link HttpApplicationLocation}.
	 */
	private final HttpApplicationLocation location = this
			.createMock(HttpApplicationLocation.class);

	/**
	 * {@link HttpSecureTask}.
	 */
	private final HttpSession session = this.createMock(HttpSession.class);

	/**
	 * {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<HttpSecureTask, HttpSecureTaskDependencies, HttpSecureTaskFlows> context = this
			.createMock(TaskContext.class);

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponse response = this.createMock(HttpResponse.class);

	/**
	 * Ensure redirect {@link HttpRequest} if not appropriately secure
	 * {@link ServerHttpConnection}.
	 */
	public void testRedirect() throws Throwable {

		final String REQUEST_URL = "/test";
		final String REDIRECT_URL = "https://officefloor.net:7979/test";

		final HttpHeader header = this.createMock(HttpHeader.class);

		// Record
		this.recordDependencyAccess();
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(),
				REQUEST_URL);
		this.recordReturn(this.location,
				this.location.transformToApplicationCanonicalPath(REQUEST_URL),
				REQUEST_URL);
		this.recordReturn(this.connection, this.connection.isSecure(), false);
		this.recordReturn(this.location,
				this.location.transformToClientPath(REQUEST_URL, true),
				REDIRECT_URL);
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.response);
		this.response.setStatus(303);
		this.recordReturn(this.response,
				this.response.addHeader("Location", REDIRECT_URL), header);

		// Test
		this.replayMockObjects();
		HttpSecureTask task = this.createHttpSecureTask("http.secure.path.0",
				"/first", "http.secure.path.1", "/second",
				"http.secure.path.2", "/test");
		task.doTask(this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure service {@link HttpRequest} if appropriately secure
	 * {@link ServerHttpConnection}.
	 */
	public void testService() throws Throwable {

		final String REQUEST_URL = "/test";

		final FlowFuture future = this.createMock(FlowFuture.class);

		// Record
		this.recordDependencyAccess();
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(),
				REQUEST_URL);
		this.recordReturn(this.location,
				this.location.transformToApplicationCanonicalPath(REQUEST_URL),
				REQUEST_URL);
		this.recordReturn(this.connection, this.connection.isSecure(), false);
		this.recordReturn(this.context,
				this.context.doFlow(HttpSecureTaskFlows.SERVICE, null), future);

		// Test
		this.replayMockObjects();
		HttpSecureTask task = this.createHttpSecureTask("http.secure.path.0",
				"/not-secure");
		task.doTask(this.context);
		this.verifyMockObjects();
	}

	/**
	 * Record obtaining the dependencies.
	 */
	private void recordDependencyAccess() {
		this.recordReturn(this.context, this.context
				.getObject(HttpSecureTaskDependencies.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(
				this.context,
				this.context
						.getObject(HttpSecureTaskDependencies.HTTP_APPLICATION_LOCATION),
				this.location);
		this.recordReturn(
				this.context,
				this.context.getObject(HttpSecureTaskDependencies.HTTP_SESSION),
				this.session);
	}

	/**
	 * Creates the {@link HttpSecureTask}.
	 * 
	 * @param propertyNameValues
	 *            {@link Property} name/value pairs.
	 * @return {@link HttpSecureTask}.
	 */
	private HttpSecureTask createHttpSecureTask(String... propertyNameValues) {

		// Load the work source and create the task
		WorkType<HttpSecureTask> workType = WorkLoaderUtil.loadWorkType(
				HttpSecureWorkSource.class, propertyNameValues);
		TaskType<HttpSecureTask, ?, ?> taskType = workType.getTaskTypes()[0];
		Task<HttpSecureTask, ?, ?> task = taskType.getTaskFactory().createTask(
				workType.getWorkFactory().createWork());

		// Return the task
		return (HttpSecureTask) task;
	}

}