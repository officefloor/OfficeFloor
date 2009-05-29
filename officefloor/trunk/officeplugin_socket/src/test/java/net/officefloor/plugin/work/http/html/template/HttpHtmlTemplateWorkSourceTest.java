/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.html.template;

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.work.http.HttpException;
import net.officefloor.plugin.work.http.HttpResponseSendTask;
import net.officefloor.plugin.work.http.MockHttpResponse;
import net.officefloor.plugin.work.http.HttpResponseSendTask.HttpResponseSendTaskDependencies;
import net.officefloor.plugin.work.http.html.template.HttpHtmlTemplateTask.HttpHtmlTemplateTaskDependencies;

/**
 * Tests the {@link HttpHtmlTemplateWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHtmlTemplateWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Properties to load the {@link WorkType}.
	 */
	private final String[] properties;

	/**
	 * Initiate.
	 */
	public HttpHtmlTemplateWorkSourceTest() {
		// Create the properties
		this.properties = new String[6];
		this.properties[0] = HttpHtmlTemplateWorkSource.PROPERTY_TEMPLATE_FILE;
		this.properties[1] = this.getClass().getPackage().getName().replace(
				'.', '/')
				+ "/Template.ofp";
		this.properties[2] = HttpHtmlTemplateWorkSource.PROPERTY_BEAN_PREFIX
				+ "template";
		this.properties[3] = TemplateBean.class.getName();
		this.properties[4] = HttpHtmlTemplateWorkSource.PROPERTY_BEAN_PREFIX
				+ "List";
		this.properties[5] = TableRowBean.class.getName();
	}

	/**
	 * Verifies the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpHtmlTemplateWorkSource.class,
				HttpHtmlTemplateWorkSource.PROPERTY_TEMPLATE_FILE, "template");
	}

	/**
	 * Ensures loads work.
	 */
	public void testLoadWork() throws Exception {

		// Create the expected work
		HttpHtmlTemplateWork workFactory = new HttpHtmlTemplateWork();
		WorkTypeBuilder<HttpHtmlTemplateWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(workFactory);

		// Create the task factory manufacturer
		HttpHtmlTemplateTask taskFactoryManufacturer = new HttpHtmlTemplateTask(
				false, null);

		// 'template' task
		TaskTypeBuilder<HttpHtmlTemplateTaskDependencies, None> template = work
				.addTaskType("template", taskFactoryManufacturer,
						HttpHtmlTemplateTaskDependencies.class, None.class);
		template.addObject(ServerHttpConnection.class).setKey(
				HttpHtmlTemplateTaskDependencies.SERVER_HTTP_CONNECTION);
		template.addObject(TemplateBean.class).setKey(
				HttpHtmlTemplateTaskDependencies.BEAN);
		template.addEscalation(HttpException.class);
		template.addEscalation(IOException.class);

		// 'List' task
		TaskTypeBuilder<HttpHtmlTemplateTaskDependencies, None> list = work
				.addTaskType("List", taskFactoryManufacturer,
						HttpHtmlTemplateTaskDependencies.class, None.class);
		list.addObject(ServerHttpConnection.class).setKey(
				HttpHtmlTemplateTaskDependencies.SERVER_HTTP_CONNECTION);
		list.addObject(TableRowBean.class).setKey(
				HttpHtmlTemplateTaskDependencies.BEAN);
		list.addEscalation(HttpException.class);
		list.addEscalation(IOException.class);

		// 'Tail' task
		TaskTypeBuilder<HttpHtmlTemplateTaskDependencies, None> tail = work
				.addTaskType("Tail", taskFactoryManufacturer,
						HttpHtmlTemplateTaskDependencies.class, None.class);
		tail.addObject(ServerHttpConnection.class).setKey(
				HttpHtmlTemplateTaskDependencies.SERVER_HTTP_CONNECTION);
		tail.addObject(Object.class).setKey(
				HttpHtmlTemplateTaskDependencies.BEAN);
		tail.addEscalation(HttpException.class);
		tail.addEscalation(IOException.class);

		// Send task
		TaskTypeBuilder<HttpResponseSendTaskDependencies, None> send = work
				.addTaskType("SendHttpResponse", new HttpResponseSendTask(),
						HttpResponseSendTaskDependencies.class, None.class);
		send.addObject(ServerHttpConnection.class).setKey(
				HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		send.addEscalation(IOException.class);

		// Verify the work type
		WorkLoaderUtil.validateWorkType(work, HttpHtmlTemplateWorkSource.class,
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
			this
					.recordReturn(
							taskContext,
							taskContext
									.getObject(HttpHtmlTemplateTaskDependencies.SERVER_HTTP_CONNECTION),
							httpConnection);
			this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
					httpResponse);
			if (beans[i] != null) {
				this.recordReturn(taskContext, taskContext
						.getObject(HttpHtmlTemplateTaskDependencies.BEAN),
						beans[i]);
			}
		}

		// Record actions for 'SendHttpResponse' task
		this
				.recordReturn(
						taskContext,
						taskContext
								.getObject(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION),
						httpConnection);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
				httpResponse);

		// Load the work type
		WorkType<HttpHtmlTemplateWork> workType = WorkLoaderUtil.loadWorkType(
				HttpHtmlTemplateWorkSource.class, this.properties);

		// Replay mocks
		this.replayMockObjects();

		// Create the work type
		HttpHtmlTemplateWork work = workType.getWorkFactory().createWork();

		// Execute the 'template' task
		this.doTask("template", work, workType, taskContext);

		// Execute the 'List' task (for table and its child)
		this.doTask("List", work, workType, taskContext); // table row bean
		this.doTask("List", work, workType, taskContext); // child row bean

		// Execute the 'Tail' task
		this.doTask("Tail", work, workType, taskContext);

		// Send the HTTP response
		this.doTask("SendHttpResponse", work, workType, taskContext);

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

		// Ensure send
		assertTrue("HTTP response should be sent", httpResponse.isSent());
	}

	/**
	 * Does the {@link Task} on the {@link WorkType}.
	 * 
	 * @param taskName
	 *            Name of {@link Task} on {@link WorkType} to execute.
	 * @param work
	 *            {@link HttpHtmlTemplateWork}.
	 * @param workType
	 *            {@link WorkType}.
	 * @param taskContext
	 *            {@link TaskContext}.
	 * @throws Throwable
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	private void doTask(String taskName, HttpHtmlTemplateWork work,
			WorkType<HttpHtmlTemplateWork> workType,
			TaskContext<HttpHtmlTemplateWork, ?, ?> taskContext)
			throws Throwable {

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