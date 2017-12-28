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
package net.officefloor.web.resource.source;

import java.io.File;
import java.io.IOException;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.source.HttpFileManagedFunctionSource;
import net.officefloor.web.resource.source.SourceHttpResourceFactory;
import net.officefloor.web.resource.source.HttpFileManagedFunctionSource.DependencyKeys;
import net.officefloor.web.resource.source.HttpFileManagedFunctionSource.SendHttpFileFunction;

/**
 * Tests the {@link HttpFileManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(HttpFileManagedFunctionSource.class,
				HttpFileManagedFunctionSource.PROPERTY_RESOURCE_PATH, "Resource Path");
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		FunctionNamespaceBuilder type = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
		SendHttpFileFunction factory = new SendHttpFileFunction(null);
		ManagedFunctionTypeBuilder<DependencyKeys, None> function = type.addManagedFunctionType(
				HttpFileManagedFunctionSource.FUNCTION_HTTP_FILE, factory, DependencyKeys.class, None.class);
		function.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
		function.addEscalation(IOException.class);

		// Validate type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(type, HttpFileManagedFunctionSource.class,
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, this.getClass().getPackage().getName(),
				HttpFileManagedFunctionSource.PROPERTY_RESOURCE_PATH, "index.html");
	}

	/**
	 * Ensure can send {@link HttpFile}.
	 */
	public void testSendHttpFile() throws Throwable {
		this.doSendHttpFileTest("index.html", "index.html");
	}

	/**
	 * Ensure find {@link HttpFile} with canonical path.
	 */
	public void testCanonicalPathToSendHttpFile() throws Throwable {
		this.doSendHttpFileTest("index.html", "/non-canonical/../index.html");
	}

	/**
	 * Ensures that sends the {@link HttpFile}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doSendHttpFileTest(String fileName, String configuredFilePath) throws Throwable {

		// Create the mocks
		ManagedFunctionContext<DependencyKeys, None> functionContext = this.createMock(ManagedFunctionContext.class);

		// Create the connection
		MockServerHttpConnection connection = MockHttpServer.mockConnection();

		// Read in the expected file content
		File file = this.findFile(this.getClass(), fileName);
		String fileContents = this.getFileContents(file);

		// Record obtaining body to send HTTP file
		this.recordReturn(functionContext, functionContext.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				connection);

		// Test
		this.replayMockObjects();

		// Load the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				HttpFileManagedFunctionSource.class, SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX,
				this.getClass().getPackage().getName(), HttpFileManagedFunctionSource.PROPERTY_RESOURCE_PATH,
				configuredFilePath);

		// Create the function
		ManagedFunction function = namespaceType.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();

		// Execute the function to send the HTTP file
		Object result = function.execute(functionContext);
		assertNull("File should be sent and no return value", result);

		// Verify
		this.verifyMockObjects();

		// Verify the file contents
		MockHttpResponse mockResponse = connection.send(null);
		assertEquals("Incorrect file content", fileContents, mockResponse.getEntity(null));
	}

}