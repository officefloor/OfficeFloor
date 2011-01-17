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

package net.officefloor.plugin.web.http.response.source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriter;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactory;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseWriterWork;
import net.officefloor.plugin.web.http.resource.HttpFile;

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

		// Obtain the writer factory
		HttpResponseWriterFactory writerFactory = work
				.getHttpResponseWriterFactory();

		// Return task to write the file
		return new HttpFileWriterTask(writerFactory);
	}

	/**
	 * {@link Task} to write a {@link HttpFile} to the {@link HttpResponse}.
	 */
	private static class HttpFileWriterTask implements
			Task<HttpResponseWriterWork, HttpFileWriterTaskDependencies, None> {

		/**
		 * {@link HttpResponseWriterFactory}.
		 */
		private final HttpResponseWriterFactory writerFactory;

		/**
		 * Initiate.
		 *
		 * @param writer
		 *            {@link HttpResponseWriterFactory}.
		 */
		public HttpFileWriterTask(HttpResponseWriterFactory writerFactory) {
			this.writerFactory = writerFactory;
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
			Charset charset = httpFile.getCharset();
			ByteBuffer contents = httpFile.getContents();
			HttpResponseWriter writer = this.writerFactory
					.createHttpResponseWriter(connection);
			writer.write(contentEncoding, contentType, charset, contents);

			// Return nothing as file written to response
			return null;
		}
	}

}