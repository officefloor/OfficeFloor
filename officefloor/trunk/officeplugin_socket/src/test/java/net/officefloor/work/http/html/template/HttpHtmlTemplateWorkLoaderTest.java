/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.work.http.html.template;

import java.io.IOException;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.work.TaskEscalationModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.work.PropertyList;
import net.officefloor.work.WorkLoaderUtil;
import net.officefloor.work.http.HttpException;
import net.officefloor.work.http.HttpResponseSendTask;
import net.officefloor.work.http.MockHttpResponse;

/**
 * Tests the {@link HttpHtmlTemplateWorkLoader}.
 * 
 * @author Daniel
 */
public class HttpHtmlTemplateWorkLoaderTest extends OfficeFrameTestCase {

	/**
	 * Allow for obtaining loaded {@link WorkModel}.
	 */
	private WorkModel<?> loadedWork;

	/**
	 * Ensures loads work.
	 */
	public void testLoadWork() throws Exception {

		// Obtain the template file path
		String templateFilePath = this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/Template.ofp";

		// Create the properties
		PropertyList properties = new PropertyList();
		properties.addProperty(
				HttpHtmlTemplateWorkLoader.PROPERTY_TEMPLATE_FILE,
				templateFilePath);
		properties.addProperty(HttpHtmlTemplateWorkLoader.PROPERTY_BEAN_PREFIX
				+ "template", TemplateBean.class.getName());
		properties.addProperty(HttpHtmlTemplateWorkLoader.PROPERTY_BEAN_PREFIX
				+ "List", TableRowBean.class.getName());

		// Load the work
		WorkModel<?> work = WorkLoaderUtil.loadWork(
				HttpHtmlTemplateWorkLoader.class, properties
						.getNameValuePairs());

		// Create the expected work model and validate work against this
		WorkModel<HttpHtmlTemplateWork> expectedWork = this
				.createExpectedWork();
		WorkLoaderUtil.assertWorkModelMatch(expectedWork, work);

		// Record for later use
		this.loadedWork = work;
	}

	/**
	 * Tests running the template to generate response.
	 */
	@SuppressWarnings("unchecked")
	public void testTemplate() throws Throwable {

		// Obtain the loaded work
		this.testLoadWork();
		WorkModel<?> work = this.loadedWork;

		// Create the mock objects
		TaskContext taskContext = this.createMock(TaskContext.class);
		ServerHttpConnection httpConnection = this
				.createMock(ServerHttpConnection.class);

		// Create the HTTP response to record output
		MockHttpResponse httpResponse = new MockHttpResponse();

		// Beans to populate template
		TemplateBean templateBean = new TemplateBean("Test");
		TableRowBean tableRowBean = new TableRowBean("one", "Same");
		ChildTableRowBean childRowBean = new ChildTableRowBean("two", "Child");

		// Record actions for 'template' task
		this
				.recordReturn(taskContext, taskContext.getObject(0),
						httpConnection);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
				httpResponse);
		this
				.recordReturn(taskContext, taskContext.getParameter(),
						templateBean);

		// Record actions for 'List' task with table row bean
		this
				.recordReturn(taskContext, taskContext.getObject(0),
						httpConnection);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
				httpResponse);
		this
				.recordReturn(taskContext, taskContext.getParameter(),
						tableRowBean);

		// Record actions for 'List' task with child row bean
		this
				.recordReturn(taskContext, taskContext.getObject(0),
						httpConnection);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
				httpResponse);
		this
				.recordReturn(taskContext, taskContext.getParameter(),
						childRowBean);

		// Record actions for 'Tail' task
		this
				.recordReturn(taskContext, taskContext.getObject(0),
						httpConnection);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
				httpResponse);

		// Record actions for 'SendHttpResponse' task
		this
				.recordReturn(taskContext, taskContext.getObject(0),
						httpConnection);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
				httpResponse);

		// Replay mocks
		this.replayMockObjects();

		// Execute the 'template' task
		TaskModel<?, ?> templateTask = work.getTasks().get(0);
		Task template = templateTask.getTaskFactoryManufacturer()
				.createTaskFactory().createTask(null);
		template.doTask(taskContext);

		// Execute the 'List' task (for table and its child)
		TaskModel<?, ?> listTask = work.getTasks().get(1);
		Task list = listTask.getTaskFactoryManufacturer().createTaskFactory()
				.createTask(null);
		list.doTask(taskContext); // table row bean
		list.doTask(taskContext); // child row bean

		// Execute the 'Tail' task
		TaskModel<?, ?> tailTask = work.getTasks().get(2);
		Task tail = tailTask.getTaskFactoryManufacturer().createTaskFactory()
				.createTask(null);
		tail.doTask(taskContext);

		// Send the HTTP response
		TaskModel<?, ?> sendTask = work.getTasks().get(3);
		Task send = sendTask.getTaskFactoryManufacturer().createTaskFactory()
				.createTask(null);
		send.doTask(taskContext);

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
	 * Creates the expected {@link WorkModel}.
	 * 
	 * @return Expected {@link WorkModel}.
	 */
	private WorkModel<HttpHtmlTemplateWork> createExpectedWork() {

		// Create the expected work model
		WorkModel<HttpHtmlTemplateWork> work = new WorkModel<HttpHtmlTemplateWork>();
		work.setTypeOfWork(HttpHtmlTemplateWork.class);
		work.setWorkFactory(new HttpHtmlTemplateWork());

		// Create the task factory manufacturer
		HttpHtmlTemplateTask taskFactoryManufacturer = new HttpHtmlTemplateTask(
				false, null);

		// Create the escalations that on all tasks
		TaskEscalationModel ioEscalation = new TaskEscalationModel(
				IOException.class.getName());
		TaskEscalationModel httpEscalation = new TaskEscalationModel(
				HttpException.class.getName());

		// Create the object on all tasks
		TaskObjectModel<Indexed> httpConnection = new TaskObjectModel<Indexed>(
				null, ServerHttpConnection.class.getName());

		// 'template' task
		TaskModel<Indexed, None> templateTask = new TaskModel<Indexed, None>();
		templateTask.setTaskName("template");
		templateTask.setTaskFactoryManufacturer(taskFactoryManufacturer);
		templateTask.addObject(httpConnection);
		templateTask.addObject(new TaskObjectModel<Indexed>(null,
				TemplateBean.class.getName()));
		templateTask.addEscalation(ioEscalation);
		templateTask.addEscalation(httpEscalation);
		work.addTask(templateTask);

		// 'List' task
		TaskModel<Indexed, None> listTask = new TaskModel<Indexed, None>();
		listTask.setTaskName("List");
		listTask.setTaskFactoryManufacturer(taskFactoryManufacturer);
		listTask.addObject(httpConnection);
		listTask.addObject(new TaskObjectModel<Indexed>(null,
				TableRowBean.class.getName()));
		listTask.addEscalation(ioEscalation);
		listTask.addEscalation(httpEscalation);
		work.addTask(listTask);

		// 'Tail' task
		TaskModel<Indexed, None> tailTask = new TaskModel<Indexed, None>();
		tailTask.setTaskName("Tail");
		tailTask.setTaskFactoryManufacturer(taskFactoryManufacturer);
		tailTask.addObject(httpConnection);
		tailTask.addEscalation(ioEscalation);
		tailTask.addEscalation(httpEscalation);
		work.addTask(tailTask);

		// Send task
		TaskModel<Indexed, None> sendTask = new TaskModel<Indexed, None>();
		sendTask.setTaskName("SendHttpResponse");
		sendTask.setTaskFactoryManufacturer(new HttpResponseSendTask());
		sendTask.addObject(httpConnection);
		sendTask.addEscalation(ioEscalation);
		work.addTask(sendTask);

		// Return the expected work
		return work;
	}
}
