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
package net.officefloor.plugin.web.http.parameters.source;

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpRequestImpl;
import net.officefloor.plugin.web.http.parameters.HttpParametersException;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersLoaderDependencies;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersLoaderWorkSource;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersLoaderWorkSource.HttpParametersLoaderTask;

/**
 * Tests the {@link HttpParametersLoaderWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(
				HttpParametersLoaderWorkSource.class,
				HttpParametersLoaderWorkSource.PROPERTY_TYPE_NAME,
				HttpParametersLoaderWorkSource.PROPERTY_TYPE_NAME);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {
		HttpParametersLoaderTask task = new HttpParametersLoaderWorkSource().new HttpParametersLoaderTask();
		WorkTypeBuilder<HttpParametersLoaderTask> workBuilder = WorkLoaderUtil
				.createWorkTypeBuilder(task);
		TaskTypeBuilder<HttpParametersLoaderDependencies, None> taskBuilder = workBuilder
				.addTaskType("LOADER", task,
						HttpParametersLoaderDependencies.class, None.class);
		taskBuilder.addObject(ServerHttpConnection.class).setKey(
				HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION);
		taskBuilder.addObject(MockType.class).setKey(
				HttpParametersLoaderDependencies.OBJECT);
		taskBuilder.setReturnType(MockType.class);
		taskBuilder.addEscalation(IOException.class);
		taskBuilder.addEscalation(HttpParametersException.class);
		WorkLoaderUtil.validateWorkType(workBuilder,
				HttpParametersLoaderWorkSource.class,
				HttpParametersLoaderWorkSource.PROPERTY_TYPE_NAME,
				MockType.class.getName());
	}

	/**
	 * Validates can source {@link Work} and do the {@link Task}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testDoTask() throws Throwable {

		// Record executing task (loading object)
		TaskContext taskContext = this.createMock(TaskContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		this.recordReturn(
				taskContext,
				taskContext
						.getObject(HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION),
				connection);
		MockType object = this.createMock(MockType.class);
		this.recordReturn(taskContext,
				taskContext.getObject(HttpParametersLoaderDependencies.OBJECT),
				object);
		HttpRequest request = new HttpRequestImpl("GET", "/path?VALUE=value",
				"HTTP/1.1", null, null);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		object.setValue("value");

		// Test
		this.replayMockObjects();
		WorkType<HttpParametersLoaderTask> workType = WorkLoaderUtil
				.loadWorkType(HttpParametersLoaderWorkSource.class,
						HttpParametersLoaderWorkSource.PROPERTY_TYPE_NAME,
						MockType.class.getName());
		Task<HttpParametersLoaderTask, ?, ?> task = workType.getTaskTypes()[0]
				.getTaskFactory().createTask(
						workType.getWorkFactory().createWork());
		Object result = task.doTask(taskContext);
		assertEquals("Incorrect resulting object", object, result);
		this.verifyMockObjects();
	}

	/**
	 * Type for testing.
	 */
	public static interface MockType {

		void setValue(String value);
	}

}