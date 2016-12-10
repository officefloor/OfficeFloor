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
package net.officefloor.plugin.web.http.resource.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.stream.impl.MockServerOutputStream;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.file.HttpFileWriterTask.HttpFileWriterTaskDependencies;

/**
 * Tests the {@link HttpFileWriterWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileWriterWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpFileWriterWorkSource.class);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {
		HttpFileWriterTask factory = new HttpFileWriterTask();
		WorkTypeBuilder<HttpFileWriterTask> work = WorkLoaderUtil
				.createWorkTypeBuilder(factory);
		TaskTypeBuilder<HttpFileWriterTaskDependencies, None> file = work
				.addTaskType("WriteFileToResponse", factory,
						HttpFileWriterTaskDependencies.class, None.class);
		file.addObject(HttpFile.class).setKey(
				HttpFileWriterTaskDependencies.HTTP_FILE);
		file.addObject(ServerHttpConnection.class).setKey(
				HttpFileWriterTaskDependencies.SERVER_HTTP_CONNECTION);
		file.addEscalation(IOException.class);
		WorkLoaderUtil.validateWorkType(work, HttpFileWriterWorkSource.class);
	}

	/**
	 * Ensure can write a {@link HttpFile}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testWriteHttpFile() throws Throwable {

		final String contentEncoding = "gzip";
		final String contentType = "text/plain";
		final Charset charset = UsAsciiUtil.US_ASCII;

		TaskContext<HttpFileWriterTask, HttpFileWriterTaskDependencies, None> taskContext = this
				.createMock(TaskContext.class);
		HttpFile httpFile = this.createMock(HttpFile.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpResponse response = this.createMock(HttpResponse.class);
		HttpHeader header = this.createMock(HttpHeader.class);
		ByteBuffer contents = ByteBuffer.wrap("TEST".getBytes());
		MockServerOutputStream entity = new MockServerOutputStream();

		// Record
		this.recordReturn(
				taskContext,
				taskContext.getObject(HttpFileWriterTaskDependencies.HTTP_FILE),
				httpFile);
		this.recordReturn(
				taskContext,
				taskContext
						.getObject(HttpFileWriterTaskDependencies.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpResponse(), response);
		response.reset();
		this.recordReturn(httpFile, httpFile.getContentEncoding(),
				contentEncoding);
		this.recordReturn(response,
				response.addHeader("Content-Encoding", contentEncoding), header);
		this.recordReturn(httpFile, httpFile.getContentType(), contentType);
		this.recordReturn(httpFile, httpFile.getCharset(), charset);
		response.setContentType(contentType, charset);
		this.recordReturn(httpFile, httpFile.getContents(), contents);
		this.recordReturn(response, response.getEntityWriter(),
				entity.getServerWriter());

		// Test
		this.replayMockObjects();

		// Create the task
		WorkType<HttpFileWriterTask> work = WorkLoaderUtil
				.loadWorkType(HttpFileWriterWorkSource.class);
		Task task = work.getTaskTypes()[0].getTaskFactory().createTask(
				work.getWorkFactory().createWork());

		// Execute the task
		assertNull(task.doTask(taskContext));

		this.verifyMockObjects();

		// Validate the entity content
		entity.flush();
		assertEquals("Incorrect entity content", "TEST",
				new String(entity.getWrittenBytes()));
	}

}