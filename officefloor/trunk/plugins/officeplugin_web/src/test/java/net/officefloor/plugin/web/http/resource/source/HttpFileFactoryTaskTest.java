/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceCreationListener;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.NotExistHttpResource;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryTask;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryTask.DependencyKeys;

/**
 * Tests the {@link HttpFileFactoryTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileFactoryTaskTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link HttpResourceFactory}.
	 */
	private HttpResourceFactory factory = this
			.createMock(HttpResourceFactory.class);

	/**
	 * Mock {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<HttpFileFactoryTask<Indexed>, DependencyKeys, Indexed> taskContext = this
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
	 * Mock {@link HttpFile}.
	 */
	private final HttpFile httpFile = this.createMock(HttpFile.class);

	/**
	 * Mock {@link HttpResourceCreationListener}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpResourceCreationListener<Indexed> creationListener = this
			.createMock(HttpResourceCreationListener.class);

	/**
	 * Ensures handle if {@link HttpFile} exists.
	 */
	public void testFileExists() throws Throwable {

		final String PATH = "/path";

		// Record
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), PATH);
		this.recordReturn(this.factory, this.factory.createHttpResource(PATH),
				this.httpFile);
		this.creationListener.httpResourceCreated(this.httpFile,
				this.connection, this.taskContext);

		// Test
		this.replayMockObjects();
		HttpFileFactoryTask<Indexed> task = new HttpFileFactoryTask<Indexed>(
				this.factory, this.creationListener);
		HttpFile file = (HttpFile) task.doTask(this.taskContext);
		this.verifyMockObjects();
		assertEquals("Incorrect resulting HTTP file", this.httpFile, file);
	}

	/**
	 * Ensures handle if {@link HttpFile} does not exist.
	 */
	public void testFileNotExists() throws Throwable {

		final String PATH = "/path";
		final NotExistHttpResource notExistResource = new NotExistHttpResource(
				PATH);

		// Record
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), PATH);
		this.recordReturn(this.factory, this.factory.createHttpResource(PATH),
				notExistResource);
		this.creationListener.httpResourceCreated(notExistResource,
				this.connection, this.taskContext);

		// Test
		this.replayMockObjects();
		HttpFileFactoryTask<Indexed> task = new HttpFileFactoryTask<Indexed>(
				this.factory, this.creationListener);
		HttpResource resource = (HttpResource) task.doTask(this.taskContext);
		this.verifyMockObjects();
		assertEquals("Incorrect resulting HTTP resource", notExistResource,
				resource);
	}

	/**
	 * Ensures handle if default {@link HttpFile}.
	 */
	public void testDefaultFile() throws Throwable {

		final String PATH = "/path";
		final HttpDirectory directory = this.createMock(HttpDirectory.class);

		// Record
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), PATH);
		this.recordReturn(this.factory, this.factory.createHttpResource(PATH),
				directory);
		this.recordReturn(directory, directory.getDefaultFile(), this.httpFile);
		this.creationListener.httpResourceCreated(this.httpFile,
				this.connection, this.taskContext);

		// Test
		this.replayMockObjects();
		HttpFileFactoryTask<Indexed> task = new HttpFileFactoryTask<Indexed>(
				this.factory, this.creationListener);
		HttpFile file = (HttpFile) task.doTask(this.taskContext);
		this.verifyMockObjects();
		assertEquals("Incorrect resulting default HTTP file", this.httpFile,
				file);
	}

}