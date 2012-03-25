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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileWorkSource.ClasspathHttpFileTask;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileWorkSource.DependencyKeys;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link ClasspathHttpFileWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<HttpFileFactoryTask<None>, DependencyKeys, None> taskContext = this
			.createMock(TaskContext.class);

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
	 * Mock {@link OutputBufferStream}.
	 */
	private final OutputBufferStream body = this
			.createMock(OutputBufferStream.class);

	/**
	 * Actual file sent content.
	 */
	private String actualFileSentContent = "";

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(ClasspathHttpFileWorkSource.class,
				ClasspathHttpFileWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"Classpath Prefix",
				ClasspathHttpFileWorkSource.PROPERTY_RESOURCE_PATH,
				"Resource Path");
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		WorkTypeBuilder<ClasspathHttpFileTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(null);
		ClasspathHttpFileTask factory = new ClasspathHttpFileTask(null);
		type.setWorkFactory(factory);
		TaskTypeBuilder<DependencyKeys, None> task = type.addTaskType(
				ClasspathHttpFileWorkSource.TASK_HTTP_FILE, factory,
				DependencyKeys.class, None.class);
		task.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.SERVER_HTTP_CONNECTION);
		task.addEscalation(IOException.class);

		// Validate type
		WorkLoaderUtil.validateWorkType(type,
				ClasspathHttpFileWorkSource.class,
				ClasspathHttpFileWorkSource.PROPERTY_CLASSPATH_PREFIX, this
						.getClass().getPackage().getName(),
				ClasspathHttpFileWorkSource.PROPERTY_RESOURCE_PATH,
				"index.html");
	}

	/**
	 * Ensures that sends the {@link HttpFile}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testSendHttpFile() throws Throwable {

		// Read in the expected file content
		File file = this.findFile(this.getClass(), "index.html");
		String fileContents = this.getFileContents(file);

		// Record obtaining body to send HTTP file
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.response);
		this.recordReturn(this.response, this.response.getBody(), this.body);

		// Record sending HTTP file (capturing contents sent)
		this.body.append((ByteBuffer) null);
		this.control(this.body).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				// Obtain the actual contents
				ByteBuffer buffer = (ByteBuffer) actual[0];
				byte[] data = new byte[buffer.limit()];
				buffer.get(data);
				String actualContents = new String(data);

				// Append the file content
				ClasspathHttpFileWorkSourceTest.this.actualFileSentContent += actualContents;

				// Always match
				return true;
			}
		});

		// Test
		this.replayMockObjects();

		// Load the work type
		WorkType<ClasspathHttpFileTask> workType = WorkLoaderUtil.loadWorkType(
				ClasspathHttpFileWorkSource.class,
				ClasspathHttpFileWorkSource.PROPERTY_CLASSPATH_PREFIX, this
						.getClass().getPackage().getName(),
				ClasspathHttpFileWorkSource.PROPERTY_RESOURCE_PATH,
				"index.html");

		// Create the task
		Task task = workType.getTaskTypes()[0].getTaskFactory().createTask(
				workType.getWorkFactory().createWork());

		// Execute the task to send the HTTP file
		Object result = task.doTask(this.taskContext);
		assertNull("File should be sent and no return value", result);

		// Verify
		this.verifyMockObjects();
		assertEquals("Incorrect file content", fileContents,
				this.actualFileSentContent);
	}

}