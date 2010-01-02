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

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.file.AbstractHttpFileFactoryTestCase;
import net.officefloor.plugin.socket.server.http.file.HttpFile;
import net.officefloor.plugin.socket.server.http.file.InvalidHttpRequestUriException;
import net.officefloor.plugin.socket.server.http.file.source.HttpFileFactoryTask.HttpFileFactoryTaskFlows;

/**
 * {@link WorkSource} to locate a {@link HttpFile} on the class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileFactoryWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(
				ClasspathHttpFileFactoryWorkSource.class,
				ClasspathHttpFileFactoryWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"classpath.prefix",
				ClasspathHttpFileFactoryWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"default.file.name");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Provide work type
		HttpFileFactoryTask task = new HttpFileFactoryTask(null, -1);
		WorkTypeBuilder<HttpFileFactoryTask> workBuilder = WorkLoaderUtil
				.createWorkTypeBuilder(task);

		// Provide task type
		TaskTypeBuilder<Indexed, HttpFileFactoryTaskFlows> taskBuilder = workBuilder
				.addTaskType("FindFile", task, Indexed.class,
						HttpFileFactoryTaskFlows.class);
		taskBuilder.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		TaskFlowTypeBuilder<HttpFileFactoryTaskFlows> flowBuilder = taskBuilder
				.addFlow();
		flowBuilder.setKey(HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND);
		flowBuilder.setArgumentType(HttpFile.class);
		taskBuilder.setReturnType(HttpFile.class);
		taskBuilder.addEscalation(IOException.class);
		taskBuilder.addEscalation(InvalidHttpRequestUriException.class);

		// Validate
		WorkLoaderUtil.validateWorkType(workBuilder,
				ClasspathHttpFileFactoryWorkSource.class,
				ClasspathHttpFileFactoryWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"html",
				ClasspathHttpFileFactoryWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"index.html");
	}

	/**
	 * Validate load and create {@link HttpFile}.
	 */
	@SuppressWarnings("unchecked")
	public void testCreateHttpFile() throws Throwable {

		TaskContext<HttpFileFactoryTask, Indexed, HttpFileFactoryTaskFlows> taskContext = this
				.createMock(TaskContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpRequest request = this.createMock(HttpRequest.class);

		// Record
		this.recordReturn(taskContext, taskContext.getObject(0), connection);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(), "/");

		// Test
		this.replayMockObjects();

		// Load the work type
		WorkType<HttpFileFactoryTask> workType = WorkLoaderUtil.loadWorkType(
				ClasspathHttpFileFactoryWorkSource.class,
				ClasspathHttpFileFactoryWorkSource.PROPERTY_CLASSPATH_PREFIX,
				AbstractHttpFileFactoryTestCase.class.getPackage().getName(),
				ClasspathHttpFileFactoryWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"index.html");

		// Create the task
		Task task = workType.getTaskTypes()[0].getTaskFactory().createTask(
				workType.getWorkFactory().createWork());

		// Execute the task to create the HTTP file
		HttpFile httpFile = (HttpFile) task.doTask(taskContext);
		assertTrue("Ensure locates the file", httpFile.isExist());
		assertEquals("Ensure correct file", "/index.html", httpFile.getPath());
		assertEquals("Ensure default description", "text/html", httpFile
				.getContentType());

		this.verifyMockObjects();
	}

}