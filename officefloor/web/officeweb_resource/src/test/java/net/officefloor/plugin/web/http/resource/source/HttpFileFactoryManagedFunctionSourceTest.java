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

import java.io.IOException;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.resource.AbstractHttpResourceFactoryTestCase;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryFunction.DependencyKeys;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryManagedFunctionSource.HttpFileFactoryFunctionFlows;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.path.HttpApplicationLocation;
import net.officefloor.web.route.InvalidRequestUriHttpException;

/**
 * Tests the {@link ClasspathHttpFileFactoryWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileFactoryManagedFunctionSourceTest extends OfficeFrameTestCase {

	@Override
	protected void setUp() throws Exception {
		// Reset factories for test
		SourceHttpResourceFactory.clearHttpResourceFactories();
	}

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(HttpFileFactoryManagedFunctionSource.class);
	}

	/**
	 * Validates the type.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testType() {

		// Provide namespace type
		FunctionNamespaceBuilder namespaceBuilder = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Provide function type
		HttpFileFactoryFunction function = new HttpFileFactoryFunction(null, null);
		ManagedFunctionTypeBuilder<DependencyKeys, HttpFileFactoryFunctionFlows> functionBuilder = namespaceBuilder
				.addManagedFunctionType("FindFile", function, DependencyKeys.class, HttpFileFactoryFunctionFlows.class);
		functionBuilder.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
		functionBuilder.addObject(HttpApplicationLocation.class).setKey(DependencyKeys.HTTP_APPLICATION_LOCATION);
		ManagedFunctionFlowTypeBuilder<HttpFileFactoryFunctionFlows> flowBuilder = functionBuilder.addFlow();
		flowBuilder.setKey(HttpFileFactoryFunctionFlows.HTTP_FILE_NOT_FOUND);
		flowBuilder.setArgumentType(HttpFile.class);
		functionBuilder.setReturnType(HttpFile.class);
		functionBuilder.addEscalation(IOException.class);
		functionBuilder.addEscalation(InvalidRequestUriHttpException.class);

		// Validate
		ManagedFunctionLoaderUtil.validateManagedFunctionType(namespaceBuilder,
				HttpFileFactoryManagedFunctionSource.class);
	}

	/**
	 * Validate load and create {@link HttpFile}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testCreateHttpFile() throws Throwable {

		ManagedFunctionContext<DependencyKeys, HttpFileFactoryFunctionFlows> functionContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		HttpRequest request = this.createMock(HttpRequest.class);
		HttpApplicationLocation location = this.createMock(HttpApplicationLocation.class);

		// Record
		this.recordReturn(functionContext, functionContext.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(), "/index.html");
		this.recordReturn(functionContext, functionContext.getObject(DependencyKeys.HTTP_APPLICATION_LOCATION),
				location);
		this.recordReturn(location, location.transformToApplicationCanonicalPath("/index.html"), "/index.html");

		// Test
		this.replayMockObjects();

		// Load the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				(Class) HttpFileFactoryManagedFunctionSource.class,
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, this.getClass().getPackage().getName(),
				SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES, "no_default_file.html");

		// Create the function
		ManagedFunction function = namespaceType.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();

		// Execute the function to create the HTTP file
		HttpFile httpFile = (HttpFile) function.execute(functionContext);
		assertTrue("Ensure locates the file", httpFile.isExist());
		assertEquals("Ensure correct file", "/index.html", httpFile.getPath());
		assertEquals("Ensure default description", "text/html", httpFile.getContentType());

		this.verifyMockObjects();
	}

	/**
	 * Validate load and create default {@link HttpFile} from
	 * {@link HttpDirectory}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testDefaultHttpFile() throws Throwable {

		ManagedFunctionContext<DependencyKeys, HttpFileFactoryFunctionFlows> functionContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		HttpRequest request = this.createMock(HttpRequest.class);
		HttpApplicationLocation location = this.createMock(HttpApplicationLocation.class);

		// Record
		this.recordReturn(functionContext, functionContext.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(), "/");
		this.recordReturn(functionContext, functionContext.getObject(DependencyKeys.HTTP_APPLICATION_LOCATION),
				location);
		this.recordReturn(location, location.transformToApplicationCanonicalPath("/"), "/");

		// Test
		this.replayMockObjects();

		// Load the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				(Class) HttpFileFactoryManagedFunctionSource.class,
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, this.getClass().getPackage().getName());

		// Create the function
		ManagedFunction function = namespaceType.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();

		// Execute the function to create the HTTP file
		HttpFile httpFile = (HttpFile) function.execute(functionContext);
		assertTrue("Ensure locates the file", httpFile.isExist());
		assertEquals("Ensure correct file", "/index.html", httpFile.getPath());
		assertEquals("Ensure default description", "text/html", httpFile.getContentType());

		this.verifyMockObjects();
	}

	/**
	 * Handles the {@link HttpFile} not being found.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testHttpFileNotFound() throws Throwable {

		ManagedFunctionContext<DependencyKeys, HttpFileFactoryFunctionFlows> functionContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		HttpRequest request = this.createMock(HttpRequest.class);
		HttpApplicationLocation location = this.createMock(HttpApplicationLocation.class);

		// Record
		this.recordReturn(functionContext, functionContext.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(), "/missing-file.html");
		this.recordReturn(functionContext, functionContext.getObject(DependencyKeys.HTTP_APPLICATION_LOCATION),
				location);
		this.recordReturn(location, location.transformToApplicationCanonicalPath("/missing-file.html"),
				"/missing-file.html");
		functionContext.doFlow(HttpFileFactoryFunctionFlows.HTTP_FILE_NOT_FOUND, null, null);

		// Test
		this.replayMockObjects();

		// Load the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				(Class) HttpFileFactoryManagedFunctionSource.class,
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX,
				AbstractHttpResourceFactoryTestCase.class.getPackage().getName());

		// Create the function
		ManagedFunction function = namespaceType.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();

		// Execute the function to handle not finding the HTTP file
		HttpResource httpResource = (HttpResource) function.execute(functionContext);
		assertFalse("Ensure file is not found", httpResource.isExist());

		this.verifyMockObjects();
	}

}