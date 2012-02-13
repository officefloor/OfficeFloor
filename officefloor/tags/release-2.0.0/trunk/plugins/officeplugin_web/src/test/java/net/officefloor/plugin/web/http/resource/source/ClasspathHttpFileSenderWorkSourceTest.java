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

package net.officefloor.plugin.web.http.resource.source;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryTask.DependencyKeys;

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
	 * Expected file sent content.
	 */
	private String expectedFileSentContent = "";

	/**
	 * Actual file sent content.
	 */
	private String actualFileSentContent = "";

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(
				ClasspathHttpFileSenderWorkSource.class,
				ClasspathHttpFileSenderWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"classpath.prefix",
				ClasspathHttpFileSenderWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"default.file.name");
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
		WorkLoaderUtil.validateWorkType(workBuilder,
				ClasspathHttpFileSenderWorkSource.class,
				ClasspathHttpFileSenderWorkSource.PROPERTY_CLASSPATH_PREFIX,
				this.getClass().getPackage().getName(),
				ClasspathHttpFileSenderWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"index.html");
	}

	/**
	 * Validate sending a {@link HttpFile}.
	 */
	public void testSendHttpFile() throws Throwable {

		// Record
		this.recordSendFile("/index.html", "index.html", 200);

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

		// Verify
		this.verifyFileSent();
	}

	/**
	 * Validate sending default file not found content for not finding the
	 * {@link HttpFile}.
	 */
	public void testSendDefaultFileNotFoundContent() throws Throwable {

		final String URI = "/missing-file.html";

		// Record
		this.recordSendFile(URI, "DefaultFileNotFound.html", 404);

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

		// Verify
		this.verifyFileSent();
	}

	/**
	 * Validate overriding file not found content for not finding the
	 * {@link HttpFile}.
	 */
	public void testSendFileNotFoundOverrideContent() throws Throwable {

		final String URI = "/missing-file.html";

		// Record
		this.recordSendFile(URI, "OverrideFileNotFound.html", 404);

		// Test
		this.replayMockObjects();

		// Load work and obtain the task
		Task<HttpFileFactoryTask<None>, DependencyKeys, None> task = this
				.loadWorkAndObtainTask(
						ClasspathHttpFileSenderWorkSource.PROPERTY_NOT_FOUND_FILE_PATH,
						"OverrideFileNotFound.html");

		// Execute the task to send the HTTP file
		Object result = task.doTask(this.taskContext);
		assertNotNull("Ensure have HTTP resource returned", result);
		HttpResource httpResource = (HttpResource) result;
		assertFalse("HTTP resource should be missing", httpResource.isExist());

		// Verify
		this.verifyFileSent();
	}

	/**
	 * Loads the {@link WorkSource} and returns a created {@link Task}.
	 * 
	 * @param additionalParameterNameValues
	 *            Addition parmaeter name/value pairs.
	 * @return {@link Task} to execute.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final Task<HttpFileFactoryTask<None>, DependencyKeys, None> loadWorkAndObtainTask(
			String... additionalParameterNameValues) {

		// Create the listing of parameters
		List<String> parameters = new ArrayList<String>(
				4 + (additionalParameterNameValues.length / 2));
		parameters
				.add(ClasspathHttpFileSenderWorkSource.PROPERTY_CLASSPATH_PREFIX);
		parameters.add(this.getClass().getPackage().getName());
		parameters
				.add(ClasspathHttpFileSenderWorkSource.PROPERTY_DEFAULT_FILE_NAME);
		parameters.add("index.html");
		for (String parameterNameValue : additionalParameterNameValues) {
			parameters.add(parameterNameValue);
		}

		// Load the work type
		WorkType<HttpFileFactoryTask<None>> workType = WorkLoaderUtil
				.loadWorkType((Class) ClasspathHttpFileSenderWorkSource.class,
						parameters.toArray(new String[parameters.size()]));

		// Create the task
		Task task = workType.getTaskTypes()[0].getTaskFactory().createTask(
				workType.getWorkFactory().createWork());

		// Return the task
		return task;
	}

	/**
	 * Records sending the {@link HttpFile}.
	 * 
	 * @param uri
	 *            URI.
	 * @param fileName
	 *            File name.
	 * @param status
	 *            Status.
	 */
	private void recordSendFile(String uri, String fileName, int status)
			throws IOException {
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), uri);
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.response);
		this.recordReturn(this.response, this.response.getBody(), this.body);
		this.recordFileSentContent(fileName);
		this.response.setStatus(status);
		this.response.send();
	}

	/**
	 * Records sending the {@link HttpFile} content.
	 * 
	 * @param fileName
	 *            Name of file containing content.
	 */
	private void recordFileSentContent(String fileName) throws IOException {

		// Read in the file content
		File file = this.findFile(this.getClass(), fileName);
		String fileContents = this.getFileContents(file);

		// Indicate expected file sent content
		ClasspathHttpFileSenderWorkSourceTest.this.expectedFileSentContent += fileContents;

		// Record obtaining the file content
		this.body.append((ByteBuffer) null);
		this.control(this.body).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				// Obtain the actual contents
				ByteBuffer buffer = (ByteBuffer) actual[0];
				byte[] data = new byte[buffer.limit()];
				buffer.get(data);
				String actualContents = new String(data);

				// Append the file content
				ClasspathHttpFileSenderWorkSourceTest.this.actualFileSentContent += actualContents;

				// Always match
				return true;
			}
		});
	}

	/**
	 * Verifies the file sent content is valid.
	 */
	private void verifyFileSent() {
		this.verifyMockObjects();
		assertEquals("Incorrect file send content",
				this.expectedFileSentContent, this.actualFileSentContent);
	}

}