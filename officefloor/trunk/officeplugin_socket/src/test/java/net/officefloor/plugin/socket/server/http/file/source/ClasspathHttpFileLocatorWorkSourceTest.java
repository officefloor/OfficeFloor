/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.file.AbstractHttpFileLocatorTestCase;
import net.officefloor.plugin.socket.server.http.file.HttpFile;
import net.officefloor.plugin.socket.server.http.file.source.HttpFileLocatorTask.HttpFileLocatorTaskFlows;

/**
 * {@link WorkSource} to locate a {@link HttpFile} on the class path.
 *
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileLocatorWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(
				ClasspathHttpFileLocatorWorkSource.class,
				ClasspathHttpFileLocatorWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"classpath.prefix",
				ClasspathHttpFileLocatorWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"default.file.name");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {
		WorkTypeBuilder<?> workTypeBuilder = WorkLoaderUtil
				.createWorkTypeBuilder(null);
		WorkLoaderUtil.validateWorkType(workTypeBuilder,
				ClasspathHttpFileLocatorWorkSource.class,
				ClasspathHttpFileLocatorWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"html",
				ClasspathHttpFileLocatorWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"index.html");
	}

	/**
	 * Validate load and use to locate a {@link HttpFile}.
	 */
	@SuppressWarnings("unchecked")
	public void testLocateHttpFile() throws Throwable {

		// Record
		TaskContext<HttpFileLocatorTask, Indexed, HttpFileLocatorTaskFlows> taskContext = this
				.createMock(TaskContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		this.recordReturn(taskContext, taskContext.getObject(0), connection);
		HttpRequest request = this.createMock(HttpRequest.class);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(), "/");

		// Test
		this.replayMockObjects();

		// Load the work type
		WorkType<HttpFileLocatorTask> workType = WorkLoaderUtil.loadWorkType(
				ClasspathHttpFileLocatorWorkSource.class,
				ClasspathHttpFileLocatorWorkSource.PROPERTY_CLASSPATH_PREFIX,
				AbstractHttpFileLocatorTestCase.class.getPackage().getName()
						.replace('.', '/'),
				ClasspathHttpFileLocatorWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"index.html");

		// Create the task
		Task task = workType.getTaskTypes()[0].getTaskFactory().createTask(
				workType.getWorkFactory().createWork());

		// Execute the task to locate the HTTP file
		HttpFile httpFile = (HttpFile) task.doTask(taskContext);
		assertTrue("Ensure locates the file", httpFile.isExist());

		this.verifyMockObjects();
	}

	/**
	 * Validate load and attempt to locate not existing {@link HttpFile}.
	 */
	@SuppressWarnings("unchecked")
	public void testNotLocateHttpFile() throws Throwable {

		// Record
		TaskContext<HttpFileLocatorTask, Indexed, HttpFileLocatorTaskFlows> taskContext = this
				.createMock(TaskContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		this.recordReturn(taskContext, taskContext.getObject(0), connection);
		HttpRequest request = this.createMock(HttpRequest.class);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(), "/not_found.html");
		FlowFuture flowFuture = this.createMock(FlowFuture.class);
		this.recordReturn(taskContext, taskContext.doFlow(0, null), flowFuture);

		// Test
		this.replayMockObjects();

		// Load the work type
		WorkType<HttpFileLocatorTask> workType = WorkLoaderUtil.loadWorkType(
				ClasspathHttpFileLocatorWorkSource.class,
				ClasspathHttpFileLocatorWorkSource.PROPERTY_CLASSPATH_PREFIX,
				AbstractHttpFileLocatorTestCase.class.getPackage().getName()
						.replace('.', '/'),
				ClasspathHttpFileLocatorWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"index.html");

		// Create the task
		Task task = workType.getTaskTypes()[0].getTaskFactory().createTask(
				workType.getWorkFactory().createWork());

		// Execute the task to locate the HTTP file
		HttpFile httpFile = (HttpFile) task.doTask(taskContext);
		assertTrue("Ensure locates the file", httpFile.isExist());

		this.verifyMockObjects();
	}

}