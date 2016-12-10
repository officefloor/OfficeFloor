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
package net.officefloor.plugin.web.http.resource.source;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
import net.officefloor.plugin.stream.impl.MockServerOutputStream;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryTask.DependencyKeys;

/**
 * Tests the {@link HttpFileSenderWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileSenderWorkSourceTest extends OfficeFrameTestCase {

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
	 * Mock {@link HttpApplicationLocation}.
	 */
	private final HttpApplicationLocation location = this
			.createMock(HttpApplicationLocation.class);

	/**
	 * Mock {@link HttpResponse}.
	 */
	private final HttpResponse response = this.createMock(HttpResponse.class);

	/**
	 * {@link MockServerOutputStream}.
	 */
	private final MockServerOutputStream entity = new MockServerOutputStream();

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpFileSenderWorkSource.class);
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
		taskBuilder.addObject(HttpApplicationLocation.class).setKey(
				DependencyKeys.HTTP_APPLICATION_LOCATION);
		taskBuilder.addEscalation(IOException.class);
		taskBuilder.addEscalation(InvalidHttpRequestUriException.class);

		// Validate
		WorkLoaderUtil
				.validateWorkType(
						workBuilder,
						HttpFileSenderWorkSource.class,
						SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX,
						this.getClass().getPackage().getName(),
						SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
						"index.html");
	}

	/**
	 * Validate sending a {@link HttpFile}.
	 */
	public void testSendHttpFile() throws Throwable {

		// Record
		this.recordSendFile("/index.html", 200);

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
		this.verifyFileSent("index.html");
	}

	/**
	 * Validate sending default file not found content for not finding the
	 * {@link HttpFile}.
	 */
	public void testSendDefaultFileNotFoundContent() throws Throwable {

		// Record
		this.recordSendFile("/missing-file.html", 404);

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
		this.verifyFileSent("DefaultFileNotFound.html");
	}

	/**
	 * Validate overriding file not found content for not finding the
	 * {@link HttpFile}.
	 */
	public void testSendFileNotFoundOverrideContent() throws Throwable {

		// Record
		this.recordSendFile("/missing-file.html", 404);

		// Test
		this.replayMockObjects();

		// Load work and obtain the task
		Task<HttpFileFactoryTask<None>, DependencyKeys, None> task = this
				.loadWorkAndObtainTask(
						HttpFileSenderWorkSource.PROPERTY_NOT_FOUND_FILE_PATH,
						"OverrideFileNotFound.html");

		// Execute the task to send the HTTP file
		Object result = task.doTask(this.taskContext);
		assertNotNull("Ensure have HTTP resource returned", result);
		HttpResource httpResource = (HttpResource) result;
		assertFalse("HTTP resource should be missing", httpResource.isExist());

		// Verify
		this.verifyFileSent("OverrideFileNotFound.html");
	}

	/**
	 * Validate overriding file not found content for not finding the
	 * {@link HttpFile} with configuration of a non-canonical path.
	 */
	public void testNotFoundOverrideWithCanonicalPath() throws Throwable {

		// Record
		this.recordSendFile("/missing-file.html", 404);

		// Test
		this.replayMockObjects();

		// Load work and obtain the task
		Task<HttpFileFactoryTask<None>, DependencyKeys, None> task = this
				.loadWorkAndObtainTask(
						HttpFileSenderWorkSource.PROPERTY_NOT_FOUND_FILE_PATH,
						"non-canonical/../OverrideFileNotFound.html");

		// Execute the task to send the HTTP file
		Object result = task.doTask(this.taskContext);
		assertNotNull("Ensure have HTTP resource returned", result);
		HttpResource httpResource = (HttpResource) result;
		assertFalse("HTTP resource should be missing", httpResource.isExist());

		// Verify
		this.verifyFileSent("OverrideFileNotFound.html");
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
		parameters.add(SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX);
		parameters.add(this.getClass().getPackage().getName());
		parameters
				.add(SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES);
		parameters.add("index.html");
		for (String parameterNameValue : additionalParameterNameValues) {
			parameters.add(parameterNameValue);
		}

		// Load the work type
		WorkType<HttpFileFactoryTask<None>> workType = WorkLoaderUtil
				.loadWorkType((Class) HttpFileSenderWorkSource.class,
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
	 * @param status
	 *            Status.
	 */
	private void recordSendFile(String uri, int status) throws Exception {
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), uri);
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.HTTP_APPLICATION_LOCATION),
				this.location);
		this.recordReturn(this.location,
				this.location.transformToApplicationCanonicalPath(uri), uri);
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.response);
		this.response.reset();
		Charset charset = Charset.defaultCharset();
		this.response.setContentType("text/html", charset);
		this.recordReturn(this.response, this.response.getEntityWriter(),
				this.entity.getServerWriter());
		this.response.setStatus(status);
		this.response.send();
	}

	/**
	 * Verifies the file sent content is valid.
	 */
	private void verifyFileSent(String fileName) throws IOException {
		this.verifyMockObjects();

		// Flush contents, that otherwise would happen on response send
		this.entity.flush();

		// Validate the file content
		File file = this.findFile(this.getClass(), fileName);
		String fileContents = this.getFileContents(file);
		assertEquals("Incorrect file content", fileContents, new String(
				this.entity.getWrittenBytes()));
	}

}