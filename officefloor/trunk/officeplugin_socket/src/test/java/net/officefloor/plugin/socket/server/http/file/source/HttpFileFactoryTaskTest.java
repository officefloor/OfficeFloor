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

package net.officefloor.plugin.socket.server.http.file.source;

import java.io.File;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.file.HttpFile;
import net.officefloor.plugin.socket.server.http.file.HttpFileDescriber;
import net.officefloor.plugin.socket.server.http.file.HttpFileFactory;
import net.officefloor.plugin.socket.server.http.file.source.HttpFileFactoryTask.HttpFileFactoryTaskFlows;

/**
 * Tests the {@link HttpFileFactoryTask}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileFactoryTaskTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link HttpFileFactory}.
	 */
	private HttpFileFactory factory = this.createMock(HttpFileFactory.class);

	/**
	 * Mock {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<HttpFileFactoryTask, Indexed, HttpFileFactoryTaskFlows> taskContext = this
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
	 * Ensures handle if {@link HttpFile} exists.
	 */
	public void testFileExists() throws Throwable {

		final String PATH = "/path";

		// Record
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), PATH);
		this.recordReturn(this.factory, this.factory.createHttpFile(null, PATH,
				(HttpFileDescriber[]) null), this.httpFile);
		this.recordReturn(this.httpFile, this.httpFile.isExist(), true);

		// Test
		this.replayMockObjects();
		HttpFileFactoryTask task = new HttpFileFactoryTask(this.factory, -1);
		HttpFile file = (HttpFile) task.doTask(this.taskContext);
		this.verifyMockObjects();
		assertEquals("Incorrect resulting HTTP file", this.httpFile, file);
	}

	/**
	 * Ensures handle if {@link HttpFile} does not exist.
	 */
	public void testFileNotExists() throws Throwable {

		final String PATH = "/path";
		final FlowFuture flowFuture = this.createMock(FlowFuture.class);

		// Record
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), PATH);
		this.recordReturn(this.factory, this.factory.createHttpFile(null, PATH,
				(HttpFileDescriber[]) null), this.httpFile);
		this.recordReturn(this.httpFile, this.httpFile.isExist(), false);
		this.recordReturn(this.taskContext, this.taskContext.doFlow(
				HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND, this.httpFile),
				flowFuture);

		// Test
		this.replayMockObjects();
		HttpFileFactoryTask task = new HttpFileFactoryTask(this.factory, -1);
		HttpFile file = (HttpFile) task.doTask(this.taskContext);
		this.verifyMockObjects();
		assertEquals("Incorrect resulting HTTP file", this.httpFile, file);
	}

	/**
	 * Ensures provides the context directory if required.
	 */
	public void testProvideContextDirectory() throws Throwable {

		final String PATH = "/path";
		final int contextDirectoryIndex = 1;
		final File contextDirectory = new File(".");

		// Record
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(), PATH);
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(contextDirectoryIndex), contextDirectory);
		this.recordReturn(this.factory, this.factory.createHttpFile(
				contextDirectory, PATH, (HttpFileDescriber[]) null),
				this.httpFile);
		this.recordReturn(this.httpFile, this.httpFile.isExist(), true);

		// Test
		this.replayMockObjects();
		HttpFileFactoryTask task = new HttpFileFactoryTask(this.factory,
				contextDirectoryIndex);
		HttpFile file = (HttpFile) task.doTask(this.taskContext);
		this.verifyMockObjects();
		assertEquals("Incorrect resulting HTTP file", this.httpFile, file);
	}

}