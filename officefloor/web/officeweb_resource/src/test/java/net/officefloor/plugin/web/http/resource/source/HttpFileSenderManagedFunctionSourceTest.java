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
import java.util.ArrayList;
import java.util.List;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryFunction.DependencyKeys;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpResponseBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.escalation.InvalidRequestUriHttpException;
import net.officefloor.web.path.HttpApplicationLocation;

/**
 * Tests the {@link HttpFileSenderManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileSenderManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionContext<DependencyKeys, None> functionContext = this
			.createMock(ManagedFunctionContext.class);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * Mock {@link HttpApplicationLocation}.
	 */
	private final HttpApplicationLocation location = this.createMock(HttpApplicationLocation.class);

	/**
	 * Mock {@link HttpResponse}.
	 */
	private final MockHttpResponseBuilder response = MockHttpServer.mockResponse();

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(HttpFileSenderManagedFunctionSource.class);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Provide work type
		FunctionNamespaceBuilder workBuilder = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Provide function type
		HttpFileFactoryFunction<None> function = new HttpFileFactoryFunction<None>(null, null);
		ManagedFunctionTypeBuilder<DependencyKeys, None> functionBuilder = workBuilder
				.addManagedFunctionType("SendFile", function, DependencyKeys.class, None.class);
		functionBuilder.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
		functionBuilder.addObject(HttpApplicationLocation.class).setKey(DependencyKeys.HTTP_APPLICATION_LOCATION);
		functionBuilder.addEscalation(IOException.class);
		functionBuilder.addEscalation(InvalidRequestUriHttpException.class);

		// Validate
		ManagedFunctionLoaderUtil.validateManagedFunctionType(workBuilder, HttpFileSenderManagedFunctionSource.class,
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, this.getClass().getPackage().getName(),
				SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES, "index.html");
	}

	/**
	 * Validate sending a {@link HttpFile}.
	 */
	public void testSendHttpFile() throws Throwable {

		// Record
		this.recordSendFile("/index.html", 200);

		// Test
		this.replayMockObjects();

		// Load the function
		ManagedFunction<DependencyKeys, None> function = this.loadAndObtainFunction();

		// Execute the task to send the HTTP file
		Object result = function.execute(this.functionContext);
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

		// Load the function
		ManagedFunction<DependencyKeys, None> function = this.loadAndObtainFunction();

		// Execute the task to send the HTTP file
		Object result = function.execute(this.functionContext);
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

		// Load the function
		ManagedFunction<DependencyKeys, None> function = this.loadAndObtainFunction(
				HttpFileSenderManagedFunctionSource.PROPERTY_NOT_FOUND_FILE_PATH, "OverrideFileNotFound.html");

		// Execute the task to send the HTTP file
		Object result = function.execute(this.functionContext);
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

		// Load the function
		ManagedFunction<DependencyKeys, None> function = this.loadAndObtainFunction(
				HttpFileSenderManagedFunctionSource.PROPERTY_NOT_FOUND_FILE_PATH,
				"non-canonical/../OverrideFileNotFound.html");

		// Execute the task to send the HTTP file
		Object result = function.execute(this.functionContext);
		assertNotNull("Ensure have HTTP resource returned", result);
		HttpResource httpResource = (HttpResource) result;
		assertFalse("HTTP resource should be missing", httpResource.isExist());

		// Verify
		this.verifyFileSent("OverrideFileNotFound.html");
	}

	/**
	 * Loads the {@link ManagedFunctionSource} and returns a created
	 * {@link ManagedFunction}.
	 * 
	 * @param additionalParameterNameValues
	 *            Addition parameter name/value pairs.
	 * @return {@link ManagedFunction} to execute.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final ManagedFunction<DependencyKeys, None> loadAndObtainFunction(String... additionalParameterNameValues)
			throws Throwable {

		// Create the listing of parameters
		List<String> parameters = new ArrayList<String>(4 + (additionalParameterNameValues.length / 2));
		parameters.add(SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX);
		parameters.add(this.getClass().getPackage().getName());
		parameters.add(SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES);
		parameters.add("index.html");
		for (String parameterNameValue : additionalParameterNameValues) {
			parameters.add(parameterNameValue);
		}

		// Load the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				(Class) HttpFileSenderManagedFunctionSource.class, parameters.toArray(new String[parameters.size()]));

		// Create the function
		ManagedFunction function = namespaceType.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();

		// Return the task
		return function;
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
		this.recordReturn(this.functionContext, this.functionContext.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(), this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), uri);
		this.recordReturn(this.functionContext,
				this.functionContext.getObject(DependencyKeys.HTTP_APPLICATION_LOCATION), this.location);
		this.recordReturn(this.location, this.location.transformToApplicationCanonicalPath(uri), uri);
		this.recordReturn(this.connection, this.connection.getHttpResponse(), this.response);
	}

	/**
	 * Verifies the file sent content is valid.
	 */
	private void verifyFileSent(String fileName) throws IOException {
		this.verifyMockObjects();

		// Validate the file content
		File file = this.findFile(this.getClass(), fileName);
		String fileContents = this.getFileContents(file);
		MockHttpResponse mockResponse = this.response.build();
		assertEquals("Incorrect status", HttpStatus.OK, mockResponse.getHttpStatus());
		assertEquals("Incorrect file content", fileContents, mockResponse.getHttpEntity(null));
	}

}