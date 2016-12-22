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

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.stream.impl.MockServerOutputStream;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.source.HttpFileWorkSource.DependencyKeys;
import net.officefloor.plugin.web.http.resource.source.HttpFileWorkSource.SendHttpFileTask;

/**
 * Tests the {@link HttpFileWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionContext<HttpFileFactoryTask<None>, DependencyKeys, None> taskContext = this
			.createMock(ManagedFunctionContext.class);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link HttpResponse}.
	 */
	private final HttpResponse response = this.createMock(HttpResponse.class);

	/**
	 * {@link MockServerOutputStream}.
	 */
	private final MockServerOutputStream entity = new MockServerOutputStream();

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpFileWorkSource.class,
				HttpFileWorkSource.PROPERTY_RESOURCE_PATH, "Resource Path");
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		FunctionNamespaceBuilder<SendHttpFileTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(null);
		SendHttpFileTask factory = new SendHttpFileTask(null);
		type.setWorkFactory(factory);
		ManagedFunctionTypeBuilder<DependencyKeys, None> task = type.addManagedFunctionType(
				HttpFileWorkSource.TASK_HTTP_FILE, factory,
				DependencyKeys.class, None.class);
		task.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.SERVER_HTTP_CONNECTION);
		task.addEscalation(IOException.class);

		// Validate type
		WorkLoaderUtil.validateWorkType(type, HttpFileWorkSource.class,
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, this
						.getClass().getPackage().getName(),
				HttpFileWorkSource.PROPERTY_RESOURCE_PATH, "index.html");
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
	public void doSendHttpFileTest(String fileName, String configuredFilePath)
			throws Throwable {

		// Read in the expected file content
		File file = this.findFile(this.getClass(), fileName);
		String fileContents = this.getFileContents(file);

		// Record obtaining body to send HTTP file
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.response);
		this.response.reset();
		Charset charset = Charset.defaultCharset();
		this.response.setContentType("text/html", charset);
		this.recordReturn(this.response, this.response.getEntityWriter(),
				this.entity.getServerWriter());

		// Test
		this.replayMockObjects();

		// Load the work type
		FunctionNamespaceType<SendHttpFileTask> workType = WorkLoaderUtil.loadWorkType(
				HttpFileWorkSource.class,
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, this
						.getClass().getPackage().getName(),
				HttpFileWorkSource.PROPERTY_RESOURCE_PATH, configuredFilePath);

		// Create the task
		ManagedFunction task = workType.getManagedFunctionTypes()[0].getManagedFunctionFactory().createManagedFunction(
				workType.getWorkFactory().createWork());

		// Execute the task to send the HTTP file
		Object result = task.execute(this.taskContext);
		assertNull("File should be sent and no return value", result);

		// Verify
		this.verifyMockObjects();

		// Verify the file contents
		this.entity.getServerOutputStream().flush();
		assertEquals("Incorrect file content", fileContents, new String(
				this.entity.getWrittenBytes()));
	}

}