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
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.resource.AbstractHttpResourceFactoryTestCase;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryTask.DependencyKeys;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryWorkSource.HttpFileFactoryTaskFlows;

/**
 * Tests the {@link ClasspathHttpFileFactoryWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileFactoryWorkSourceTest extends OfficeFrameTestCase {

	@Override
	protected void setUp() throws Exception {
		// Reset factories for test
		SourceHttpResourceFactory.clearHttpResourceFactories();
	}

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpFileFactoryWorkSource.class);
	}

	/**
	 * Validates the type.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testType() {

		// Provide work type
		HttpFileFactoryTask task = new HttpFileFactoryTask(null, null);
		FunctionNamespaceBuilder<HttpFileFactoryTask> workBuilder = WorkLoaderUtil
				.createWorkTypeBuilder(task);

		// Provide task type
		ManagedFunctionTypeBuilder<DependencyKeys, HttpFileFactoryWorkSource.HttpFileFactoryTaskFlows> taskBuilder = workBuilder
				.addManagedFunctionType("FindFile", task, DependencyKeys.class,
						HttpFileFactoryTaskFlows.class);
		taskBuilder.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.SERVER_HTTP_CONNECTION);
		taskBuilder.addObject(HttpApplicationLocation.class).setKey(
				DependencyKeys.HTTP_APPLICATION_LOCATION);
		ManagedFunctionFlowTypeBuilder<HttpFileFactoryTaskFlows> flowBuilder = taskBuilder
				.addFlow();
		flowBuilder.setKey(HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND);
		flowBuilder.setArgumentType(HttpFile.class);
		taskBuilder.setReturnType(HttpFile.class);
		taskBuilder.addEscalation(IOException.class);
		taskBuilder.addEscalation(InvalidHttpRequestUriException.class);

		// Validate
		WorkLoaderUtil.validateWorkType(workBuilder,
				HttpFileFactoryWorkSource.class);
	}

	/**
	 * Validate load and create {@link HttpFile}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testCreateHttpFile() throws Throwable {

		ManagedFunctionContext<HttpFileFactoryTask, DependencyKeys, HttpFileFactoryTaskFlows> taskContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpRequest request = this.createMock(HttpRequest.class);
		HttpApplicationLocation location = this
				.createMock(HttpApplicationLocation.class);

		// Record
		this.recordReturn(taskContext,
				taskContext.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(), "/index.html");
		this.recordReturn(
				taskContext,
				taskContext.getObject(DependencyKeys.HTTP_APPLICATION_LOCATION),
				location);
		this.recordReturn(location,
				location.transformToApplicationCanonicalPath("/index.html"),
				"/index.html");

		// Test
		this.replayMockObjects();

		// Load the work type
		FunctionNamespaceType<HttpFileFactoryTask> workType = WorkLoaderUtil
				.loadWorkType(
						(Class) HttpFileFactoryWorkSource.class,
						SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX,
						this.getClass().getPackage().getName(),
						SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
						"no_default_file.html");

		// Create the task
		ManagedFunction task = workType.getManagedFunctionTypes()[0].getManagedFunctionFactory().createManagedFunction(
				workType.getWorkFactory().createWork());

		// Execute the task to create the HTTP file
		HttpFile httpFile = (HttpFile) task.execute(taskContext);
		assertTrue("Ensure locates the file", httpFile.isExist());
		assertEquals("Ensure correct file", "/index.html", httpFile.getPath());
		assertEquals("Ensure default description", "text/html",
				httpFile.getContentType());

		this.verifyMockObjects();
	}

	/**
	 * Validate load and create default {@link HttpFile} from
	 * {@link HttpDirectory}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testDefaultHttpFile() throws Throwable {

		ManagedFunctionContext<HttpFileFactoryTask, DependencyKeys, HttpFileFactoryTaskFlows> taskContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpRequest request = this.createMock(HttpRequest.class);
		HttpApplicationLocation location = this
				.createMock(HttpApplicationLocation.class);

		// Record
		this.recordReturn(taskContext,
				taskContext.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(), "/");
		this.recordReturn(
				taskContext,
				taskContext.getObject(DependencyKeys.HTTP_APPLICATION_LOCATION),
				location);
		this.recordReturn(location,
				location.transformToApplicationCanonicalPath("/"), "/");

		// Test
		this.replayMockObjects();

		// Load the work type
		FunctionNamespaceType<HttpFileFactoryTask> workType = WorkLoaderUtil.loadWorkType(
				(Class) HttpFileFactoryWorkSource.class,
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, this
						.getClass().getPackage().getName());

		// Create the task
		ManagedFunction task = workType.getManagedFunctionTypes()[0].getManagedFunctionFactory().createManagedFunction(
				workType.getWorkFactory().createWork());

		// Execute the task to create the HTTP file
		HttpFile httpFile = (HttpFile) task.execute(taskContext);
		assertTrue("Ensure locates the file", httpFile.isExist());
		assertEquals("Ensure correct file", "/index.html", httpFile.getPath());
		assertEquals("Ensure default description", "text/html",
				httpFile.getContentType());

		this.verifyMockObjects();
	}

	/**
	 * Handles the {@link HttpFile} not being found.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testHttpFileNotFound() throws Throwable {

		ManagedFunctionContext<HttpFileFactoryTask, DependencyKeys, HttpFileFactoryTaskFlows> taskContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpRequest request = this.createMock(HttpRequest.class);
		FlowFuture flowFuture = this.createMock(FlowFuture.class);
		HttpApplicationLocation location = this
				.createMock(HttpApplicationLocation.class);

		// Record
		this.recordReturn(taskContext,
				taskContext.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(),
				"/missing-file.html");
		this.recordReturn(
				taskContext,
				taskContext.getObject(DependencyKeys.HTTP_APPLICATION_LOCATION),
				location);
		this.recordReturn(location, location
				.transformToApplicationCanonicalPath("/missing-file.html"),
				"/missing-file.html");
		this.recordReturn(taskContext, taskContext.doFlow(
				HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND, null), flowFuture);

		// Test
		this.replayMockObjects();

		// Load the work type
		FunctionNamespaceType<HttpFileFactoryTask> workType = WorkLoaderUtil.loadWorkType(
				(Class) HttpFileFactoryWorkSource.class,
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX,
				AbstractHttpResourceFactoryTestCase.class.getPackage()
						.getName());

		// Create the task
		ManagedFunction task = workType.getManagedFunctionTypes()[0].getManagedFunctionFactory().createManagedFunction(
				workType.getWorkFactory().createWork());

		// Execute the task to handle not finding the HTTP file
		HttpResource httpResource = (HttpResource) task.execute(taskContext);
		assertFalse("Ensure file is not found", httpResource.isExist());

		this.verifyMockObjects();
	}

}