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
import java.nio.ByteBuffer;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.file.HttpFile;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriter;

/**
 * {@link TaskFactory} to write a {@link HttpFile} to the {@link HttpResponse}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileWriterTaskFactory
		implements
		TaskFactory<HttpResponseWriterWork, HttpFileWriterTaskFactory.HttpFileWriterTaskDependencies, None> {

	/**
	 * Keys for the dependencies.
	 */
	public static enum HttpFileWriterTaskDependencies {
		HTTP_FILE, SERVER_HTTP_CONNECTION
	}

	/**
	 * Adds the {@link TaskType} information for {@link HttpFileWriterTask}.
	 *
	 * @param taskName
	 *            {@link Task} name.
	 * @param workTypeBuilder
	 *            {@link WorkTypeBuilder}.
	 * @return {@link TaskTypeBuilder} that added this
	 *         {@link HttpFileWriterTask} type information.
	 */
	public static TaskTypeBuilder<HttpFileWriterTaskDependencies, None> addTaskType(
			String taskName,
			WorkTypeBuilder<HttpResponseWriterWork> workTypeBuilder) {
		TaskTypeBuilder<HttpFileWriterTaskDependencies, None> task = workTypeBuilder
				.addTaskType(taskName, new HttpFileWriterTaskFactory(),
						HttpFileWriterTaskDependencies.class, None.class);
		task.addObject(HttpFile.class).setKey(
				HttpFileWriterTaskDependencies.HTTP_FILE);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpFileWriterTaskDependencies.SERVER_HTTP_CONNECTION);
		task.addEscalation(IOException.class);
		return task;
	}

	/*
	 * ===================== TaskFactory =================================
	 */

	@Override
	public Task<HttpResponseWriterWork, HttpFileWriterTaskDependencies, None> createTask(
			HttpResponseWriterWork work) {

		// Obtain the writer
		HttpResponseWriter writer = work.getHttpResponseWriter();

		// Return task to write the file
		return new HttpFileWriterTask(writer);
	}

	/**
	 * {@link Task} to write a {@link HttpFile} to the {@link HttpResponse}.
	 */
	private static class HttpFileWriterTask implements
			Task<HttpResponseWriterWork, HttpFileWriterTaskDependencies, None> {

		/**
		 * {@link HttpResponseWriter}.
		 */
		private final HttpResponseWriter writer;

		/**
		 * Initiate.
		 *
		 * @param writer
		 *            {@link HttpResponseWriter}.
		 */
		public HttpFileWriterTask(HttpResponseWriter writer) {
			this.writer = writer;
		}

		/*
		 * ===================== Task ========================================
		 */

		@Override
		public Object doTask(
				TaskContext<HttpResponseWriterWork, HttpFileWriterTaskDependencies, None> context)
				throws IOException {

			// Obtain the dependencies
			HttpFile httpFile = (HttpFile) context
					.getObject(HttpFileWriterTaskDependencies.HTTP_FILE);
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(HttpFileWriterTaskDependencies.SERVER_HTTP_CONNECTION);

			// Write the file
			String contentEncoding = httpFile.getContentEncoding();
			String contentType = httpFile.getContentType();
			ByteBuffer contents = httpFile.getContents();
			this.writer.writeContent(connection, contentEncoding, contentType,
					contents);

			// Return nothing as file written to response
			return null;
		}
	}

}