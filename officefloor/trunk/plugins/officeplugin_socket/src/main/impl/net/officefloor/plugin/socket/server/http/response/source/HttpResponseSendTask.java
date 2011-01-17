/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import java.nio.ByteBuffer;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * {@link Task} to trigger sending the {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseSendTask
		extends
		AbstractSingleTask<Work, HttpResponseSendTask.HttpResponseSendTaskDependencies, None> {

	/**
	 * Keys for the dependencies.
	 */
	public static enum HttpResponseSendTaskDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Adds the {@link TaskType} information for {@link HttpResponseSendTask}.
	 * 
	 * @param taskName
	 *            {@link Task} name.
	 * @param taskFactory
	 *            {@link HttpResponseSendTask}.
	 * @param workTypeBuilder
	 *            {@link WorkTypeBuilder}.
	 * @return {@link TaskTypeBuilder} that added this
	 *         {@link HttpResponseSendTask} type information.
	 */
	public static TaskTypeBuilder<HttpResponseSendTaskDependencies, None> addTaskType(
			String taskName, HttpResponseSendTask taskFactory,
			WorkTypeBuilder<Work> workTypeBuilder) {
		TaskTypeBuilder<HttpResponseSendTaskDependencies, None> task = workTypeBuilder
				.addTaskType(taskName, taskFactory,
						HttpResponseSendTaskDependencies.class, None.class);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		task.addEscalation(IOException.class);
		return task;
	}

	/**
	 * HTTP status for the {@link HttpResponse}.
	 */
	private final int status;

	/**
	 * Content for the {@link HttpResponse}. May be <code>null</code>.
	 */
	private final ByteBuffer content;

	/**
	 * Initiate.
	 * 
	 * @param status
	 *            HTTP status for the {@link HttpResponse}.
	 * @param content
	 *            Content for {@link HttpResponse}. May be <code>null</code>.
	 */
	public HttpResponseSendTask(int status, byte[] content) {
		this.status = status;
		if (content == null) {
			// No content
			this.content = null;
		} else {
			// Provide direct buffer with content
			ByteBuffer buffer = ByteBuffer.allocateDirect(content.length);
			buffer.put(content);
			buffer.flip();
			this.content = buffer.asReadOnlyBuffer();
		}
	}

	/*
	 * =========================== Task ================================
	 */

	@Override
	public Object doTask(
			TaskContext<Work, HttpResponseSendTaskDependencies, None> context)
			throws IOException {

		// Obtain the response
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		HttpResponse response = connection.getHttpResponse();

		// Provide the status if specified
		if (this.status > 0) {
			response.setStatus(this.status);
		}

		// Provide body content if have content
		if (this.content != null) {
			response.getBody().append(this.content);
		}

		// Trigger sending the response
		response.send();

		// Return nothing as response sent
		return null;
	}

}