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
package net.officefloor.plugin.socket.server.http.template;

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;

/**
 * Tests the {@link HttpTemplateWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Properties to load the {@link WorkType}.
	 */
	private final String[] properties;

	/**
	 * Initiate.
	 */
	public HttpTemplateWorkSourceTest() {
		// Create the properties
		this.properties = new String[6];
		this.properties[0] = HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE;
		this.properties[1] = this.getClass().getPackage().getName().replace(
				'.', '/')
				+ "/Template.ofp";
		this.properties[2] = HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX
				+ "template";
		this.properties[3] = TemplateBean.class.getName();
		this.properties[4] = HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX
				+ "List";
		this.properties[5] = TableRowBean.class.getName();
	}

	/**
	 * Verifies the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpTemplateWorkSource.class,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE, "template");
	}

	/**
	 * Validate type.
	 */
	public void testType() throws Exception {

		// Create the expected work
		HttpTemplateWork workFactory = new HttpTemplateWork();
		WorkTypeBuilder<HttpTemplateWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(workFactory);

		// Create the task factory
		HttpTemplateTask taskFactory = new HttpTemplateTask(null, false, null);

		// 'template' task
		TaskTypeBuilder<Indexed, None> template = work.addTaskType("template",
				taskFactory, Indexed.class, None.class);
		template.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		template.addObject(TemplateBean.class).setLabel("OBJECT");
		template.addEscalation(IOException.class);

		// 'List' task
		TaskTypeBuilder<Indexed, None> list = work.addTaskType("List",
				taskFactory, Indexed.class, None.class);
		list.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		list.addObject(TableRowBean.class).setLabel("OBJECT");
		list.addEscalation(IOException.class);

		// 'Tail' task
		TaskTypeBuilder<Indexed, None> tail = work.addTaskType("Tail",
				taskFactory, Indexed.class, None.class);
		tail.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		tail.addEscalation(IOException.class);

		// Verify the work type
		WorkLoaderUtil.validateWorkType(work, HttpTemplateWorkSource.class,
				this.properties);
	}

	/**
	 * Tests running the template to generate response.
	 */
	@SuppressWarnings("unchecked")
	public void testTemplate() throws Throwable {

		// Create the mock objects
		TaskContext taskContext = this.createMock(TaskContext.class);
		ServerHttpConnection httpConnection = this
				.createMock(ServerHttpConnection.class);

		// Create the HTTP response to record output
		MockHttpResponse httpResponse = new MockHttpResponse();

		// Record actions for each task:
		// - 'template'
		// - 'List' task with table row bean
		// - 'List' task with child row bean
		// - 'Tail'
		Object[] beans = new Object[4];
		beans[0] = new TemplateBean("Test");
		beans[1] = new TableRowBean("one", "Same");
		beans[2] = new ChildTableRowBean("two", "Child");
		beans[3] = null;
		for (int i = 0; i < 4; i++) {
			this.recordReturn(taskContext, taskContext.getObject(0),
					httpConnection);
			this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
					httpResponse);
			if (beans[i] != null) {
				this.recordReturn(taskContext, taskContext.getObject(1),
						beans[i]);
			}
		}

		// Load the work type
		WorkType<HttpTemplateWork> workType = WorkLoaderUtil.loadWorkType(
				HttpTemplateWorkSource.class, this.properties);

		// Replay mocks
		this.replayMockObjects();

		// Create the work type
		HttpTemplateWork work = workType.getWorkFactory().createWork();

		// Execute the 'template' task
		this.doTask("template", work, workType, taskContext);

		// Execute the 'List' task (for table and its child)
		this.doTask("List", work, workType, taskContext); // table row bean
		this.doTask("List", work, workType, taskContext); // child row bean

		// Execute the 'Tail' task
		this.doTask("Tail", work, workType, taskContext);

		// Verify mocks
		this.verifyMockObjects();

		// Obtain the output template
		String actualOutput = UsAsciiUtil.convertToString(httpResponse
				.getBodyContent());

		// Expected output (removing last end of line appended)
		String expectedOutput = this.getFileContents(this.findFile(this
				.getClass(), "Template.expected"));
		expectedOutput = expectedOutput.substring(0,
				expectedOutput.length() - 1);

		// Validate output
		assertTextEquals("Incorrect output", expectedOutput, actualOutput);
	}

	/**
	 * Does the {@link Task} on the {@link WorkType}.
	 *
	 * @param taskName
	 *            Name of {@link Task} on {@link WorkType} to execute.
	 * @param work
	 *            {@link HttpTemplateWork}.
	 * @param workType
	 *            {@link WorkType}.
	 * @param taskContext
	 *            {@link TaskContext}.
	 * @throws Throwable
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	private void doTask(String taskName, HttpTemplateWork work,
			WorkType<HttpTemplateWork> workType,
			TaskContext<HttpTemplateWork, ?, ?> taskContext) throws Throwable {

		// Obtain the index of the task
		int taskIndex = -1;
		TaskType<?, ?, ?>[] taskTypes = workType.getTaskTypes();
		for (int i = 0; i < taskTypes.length; i++) {
			if (taskName.equals(taskTypes[i].getTaskName())) {
				taskIndex = i;
			}
		}
		if (taskIndex == -1) {
			fail("Could not find task '" + taskName + "'");
		}

		// Create the task
		Task task = workType.getTaskTypes()[taskIndex].getTaskFactory()
				.createTask(work);

		// Execute the task
		task.doTask(taskContext);
	}

}