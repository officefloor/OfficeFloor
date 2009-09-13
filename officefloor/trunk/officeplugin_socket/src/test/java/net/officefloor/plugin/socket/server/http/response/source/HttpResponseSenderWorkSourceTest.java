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
package net.officefloor.plugin.socket.server.http.response.source;

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseSendTask.HttpResponseSendTaskDependencies;

/**
 * Tests the {@link HttpResponseSenderWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseSenderWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpResponseSenderWorkSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {
		HttpResponseSendTask task = new HttpResponseSendTask(-1);
		WorkTypeBuilder<Work> workTypeBuilder = WorkLoaderUtil
				.createWorkTypeBuilder(task);
		TaskTypeBuilder<HttpResponseSendTaskDependencies, None> taskBuilder = workTypeBuilder
				.addTaskType("SEND", task,
						HttpResponseSendTaskDependencies.class, None.class);
		taskBuilder.addObject(ServerHttpConnection.class).setKey(
				HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		taskBuilder.addEscalation(IOException.class);
		WorkLoaderUtil.validateWorkType(workTypeBuilder,
				HttpResponseSenderWorkSource.class);
	}

	/**
	 * Ensure can trigger sending the {@link HttpResponse}.
	 */
	@SuppressWarnings("unchecked")
	public void testTriggerSend() throws Throwable {

		final int status = 204;

		TaskContext<Work, HttpResponseSendTaskDependencies, None> taskContext = this
				.createMock(TaskContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpResponse response = this.createMock(HttpResponse.class);

		// Record
		this
				.recordReturn(
						taskContext,
						taskContext
								.getObject(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION),
						connection);
		this.recordReturn(connection, connection.getHttpResponse(), response);
		response.setStatus(status);
		response.send();

		// Test
		this.replayMockObjects();

		// Create the task
		WorkType<Work> work = WorkLoaderUtil.loadWorkType(
				HttpResponseSenderWorkSource.class,
				HttpResponseSenderWorkSource.PROPERTY_HTTP_STATUS, String
						.valueOf(status));
		Task task = work.getTaskTypes()[0].getTaskFactory().createTask(
				work.getWorkFactory().createWork());

		// Execute the task
		task.doTask(taskContext);
		this.verifyMockObjects();
	}

}