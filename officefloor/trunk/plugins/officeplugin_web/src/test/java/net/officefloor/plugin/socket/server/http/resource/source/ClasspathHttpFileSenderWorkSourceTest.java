/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.resource.source;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.resource.HttpFile;
import net.officefloor.plugin.socket.server.http.resource.HttpResource;
import net.officefloor.plugin.socket.server.http.resource.InvalidHttpRequestUriException;
import net.officefloor.plugin.socket.server.http.resource.source.HttpFileFactoryTask.DependencyKeys;
import net.officefloor.plugin.stream.OutputBufferStream;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link ClasspathHttpFileSenderWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileSenderWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<HttpFileFactoryTask<None>, DependencyKeys, None> taskContext = this
			.createMock(TaskContext.class);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * Mock {@link HttpResponse}.
	 */
	private final HttpResponse response = this.createMock(HttpResponse.class);

	/**
	 * Mock {@link OutputBufferStream}.
	 */
	private final OutputBufferStream body = this
			.createMock(OutputBufferStream.class);

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil
				.validateSpecification(
						ClasspathHttpFileSenderWorkSource.class,
						ClasspathHttpFileSenderWorkSource.PROPERTY_CLASSPATH_PREFIX,
						"classpath.prefix",
						ClasspathHttpFileSenderWorkSource.PROPERTY_DEFAULT_FILE_NAME,
						"default.file.name",
						ClasspathHttpFileSenderWorkSource.PROPERTY_FILE_NOT_FOUND_CONTENT_PATH,
						"file.not.found.content.path");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Provide work type
		HttpFileFactoryTask<None> task = new HttpFileFactoryTask<None>(null,
				null);
		WorkTypeBuilder<HttpFileFactoryTask<None>> workBuilder = WorkLoaderUtil
				.createWorkTypeBuilder(task);

		// Provide task type
		TaskTypeBuilder<DependencyKeys, None> taskBuilder = workBuilder
				.addTaskType("SendFile", task, DependencyKeys.class, None.class);
		taskBuilder.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.SERVER_HTTP_CONNECTION);
		taskBuilder.addEscalation(IOException.class);
		taskBuilder.addEscalation(InvalidHttpRequestUriException.class);

		// Validate
		WorkLoaderUtil
				.validateWorkType(
						workBuilder,
						ClasspathHttpFileSenderWorkSource.class,
						ClasspathHttpFileSenderWorkSource.PROPERTY_CLASSPATH_PREFIX,
						this.getClass().getPackage().getName(),
						ClasspathHttpFileSenderWorkSource.PROPERTY_DEFAULT_FILE_NAME,
						"index.html",
						ClasspathHttpFileSenderWorkSource.PROPERTY_FILE_NOT_FOUND_CONTENT_PATH,
						"FileNotFoundContent.html");
	}

	/**
	 * Validate sending a {@link HttpFile}.
	 */
	public void testSendHttpFile() throws Throwable {

		// Record
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(),
				"/index.html");
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.response);
		this.recordReturn(this.response, this.response.getBody(), this.body);
		this.recordFileSentContent("index.html");
		this.response.setStatus(200);
		this.response.send();

		// Test
		this.replayMockObjects();

		// Load work and obtain the task
		Task<HttpFileFactoryTask<None>, DependencyKeys, None> task = this
				.loadWorkAndObtainTask();

		// Execute the task to send the HTTP file
		Object result = task.doTask(this.taskContext);
		assertNotNull("Ensure have HTTP file returned", result);
		HttpFile httpFile = (HttpFile) result;
		assertTrue("HTTP file should exist", httpFile.isExist());

		this.verifyMockObjects();
	}

	/**
	 * Validate sending file not found content for not finding the
	 * {@link HttpFile}.
	 */
	public void testSendFileNotFoundContent() throws Throwable {

		// Record
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(),
				"/missing-file.html");
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.response);
		this.recordReturn(this.response, this.response.getBody(), this.body);
		this.recordFileSentContent("FileNotFoundContent.html");
		this.response.setStatus(404);
		this.response.send();

		// Test
		this.replayMockObjects();

		// Load work and obtain the task
		Task<HttpFileFactoryTask<None>, DependencyKeys, None> task = this
				.loadWorkAndObtainTask();

		// Execute the task to send the HTTP file
		Object result = task.doTask(this.taskContext);
		assertNotNull("Ensure have HTTP resource returned", result);
		HttpResource httpResource = (HttpResource) result;
		assertFalse("HTTP resource should be missing", httpResource.isExist());

		this.verifyMockObjects();
	}

	/**
	 * Loads the {@link WorkSource} and returns a created {@link Task}.
	 * 
	 * @return {@link Task} to execute.
	 */
	@SuppressWarnings("unchecked")
	private final Task<HttpFileFactoryTask<None>, DependencyKeys, None> loadWorkAndObtainTask() {

		// Load the work type
		WorkType<HttpFileFactoryTask<None>> workType = WorkLoaderUtil
				.loadWorkType(
						(Class) ClasspathHttpFileSenderWorkSource.class,
						ClasspathHttpFileSenderWorkSource.PROPERTY_CLASSPATH_PREFIX,
						this.getClass().getPackage().getName(),
						ClasspathHttpFileSenderWorkSource.PROPERTY_DEFAULT_FILE_NAME,
						"index.html",
						ClasspathHttpFileSenderWorkSource.PROPERTY_FILE_NOT_FOUND_CONTENT_PATH,
						"FileNotFoundContent.html");

		// Create the task
		Task task = workType.getTaskTypes()[0].getTaskFactory().createTask(
				workType.getWorkFactory().createWork());

		// Return the task
		return task;
	}

	/**
	 * Records sending the {@link HttpFile} content.
	 */
	private void recordFileSentContent(String fileName) throws IOException {

		// Read in the file content
		File file = this.findFile(this.getClass(), fileName);
		final String fileContents = this.getFileContents(file).trim();

		// Record obtaining the file content
		this.body.append((ByteBuffer) null);
		this.control(this.body).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				ByteBuffer buffer = (ByteBuffer) actual[0];
				byte[] data = new byte[buffer.limit()];
				buffer.get(data);
				String actualContents = new String(data).trim();
				assertTextEquals("Incorrect file contents sent", fileContents,
						actualContents);
				return true;
			}
		});
	}

}