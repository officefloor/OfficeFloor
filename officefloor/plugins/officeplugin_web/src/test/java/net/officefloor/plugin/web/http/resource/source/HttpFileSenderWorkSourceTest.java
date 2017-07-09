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

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.stream.impl.MockServerOutputStream;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryFunction.DependencyKeys;

/**
 * Tests the {@link HttpFileSenderManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileSenderWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionContext<HttpFileFactoryFunction<None>, DependencyKeys, None> taskContext = this
			.createMock(ManagedFunctionContext.class);

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
		WorkLoaderUtil.validateSpecification(HttpFileSenderManagedFunctionSource.class);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Provide work type
		HttpFileFactoryFunction<None> task = new HttpFileFactoryFunction<None>(null,
				null);
		FunctionNamespaceBuilder<HttpFileFactoryFunction<None>> workBuilder = WorkLoaderUtil
				.createWorkTypeBuilder(task);

		// Provide task type
		ManagedFunctionTypeBuilder<DependencyKeys, None> taskBuilder = workBuilder
				.addManagedFunctionType("SendFile", task, DependencyKeys.class, None.class);
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
						HttpFileSenderManagedFunctionSource.class,
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
		ManagedFunction<HttpFileFactoryFunction<None>, DependencyKeys, None> task = this
				.loadWorkAndObtainTask();

		// Execute the task to send the HTTP file
		Object result = task.execute(this.taskContext);
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
		ManagedFunction<HttpFileFactoryFunction<None>, DependencyKeys, None> task = this
				.loadWorkAndObtainTask();

		// Execute the task to send the HTTP file
		Object result = task.execute(this.taskContext);
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
		ManagedFunction<HttpFileFactoryFunction<None>, DependencyKeys, None> task = this
				.loadWorkAndObtainTask(
						HttpFileSenderManagedFunctionSource.PROPERTY_NOT_FOUND_FILE_PATH,
						"OverrideFileNotFound.html");

		// Execute the task to send the HTTP file
		Object result = task.execute(this.taskContext);
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
		ManagedFunction<HttpFileFactoryFunction<None>, DependencyKeys, None> task = this
				.loadWorkAndObtainTask(
						HttpFileSenderManagedFunctionSource.PROPERTY_NOT_FOUND_FILE_PATH,
						"non-canonical/../OverrideFileNotFound.html");

		// Execute the task to send the HTTP file
		Object result = task.execute(this.taskContext);
		assertNotNull("Ensure have HTTP resource returned", result);
		HttpResource httpResource = (HttpResource) result;
		assertFalse("HTTP resource should be missing", httpResource.isExist());

		// Verify
		this.verifyFileSent("OverrideFileNotFound.html");
	}

	/**
	 * Loads the {@link ManagedFunctionSource} and returns a created {@link ManagedFunction}.
	 * 
	 * @param additionalParameterNameValues
	 *            Addition parmaeter name/value pairs.
	 * @return {@link ManagedFunction} to execute.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final ManagedFunction<HttpFileFactoryFunction<None>, DependencyKeys, None> loadWorkAndObtainTask(
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
		FunctionNamespaceType<HttpFileFactoryFunction<None>> workType = WorkLoaderUtil
				.loadWorkType((Class) HttpFileSenderManagedFunctionSource.class,
						parameters.toArray(new String[parameters.size()]));

		// Create the task
		ManagedFunction task = workType.getManagedFunctionTypes()[0].getManagedFunctionFactory().createManagedFunction(
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