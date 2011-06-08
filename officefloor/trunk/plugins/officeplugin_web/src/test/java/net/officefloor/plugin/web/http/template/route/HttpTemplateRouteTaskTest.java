/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.template.route;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.template.HttpTemplateRequestHandlerDifferentiator;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteDependencies;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteTaskFlows;

/**
 * Tests the {@link HttpTemplateRouteTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateRouteTaskTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link Office}.
	 */
	private final Office office = this.createMock(Office.class);

	/**
	 * Mock {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<HttpTemplateRouteTask, HttpTemplateRouteDependencies, HttpTemplateRouteTaskFlows> taskContext = this
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
	 * {@link Task} name prefix to use for testing.
	 */
	private String taskNamePrefix = null;

	/**
	 * Ensure can route to {@link Task}.
	 */
	public void testRoute() throws Throwable {
		// Record
		this.record_cacheOfficeMetaData("WORK", "TASK",
				new HttpTemplateRequestHandlerDifferentiator());
		this.record_requestURI("/WORK-TASK.task");
		this.taskContext.doFlow("WORK", "TASK", null);

		// Test
		this.doTest();
	}

	/**
	 * Ensure can route to {@link Task} that has a {@link Task} name prefix.
	 */
	public void testRouteWithTaskNamePrefix() throws Throwable {

		// Use task name prefix
		this.taskNamePrefix = "PREFIX_";

		// Record
		this.record_cacheOfficeMetaData("WORK", "TASK",
				new HttpTemplateRequestHandlerDifferentiator());
		this.record_requestURI("/WORK-TASK.task");
		this.taskContext.doFlow("WORK", "PREFIX_TASK", null);

		// Test
		this.doTest();
	}

	/**
	 * Ensure handle no {@link Work} name (only {@link Task} name).
	 */
	public void testNoWorkName() throws Throwable {
		// Record
		this.record_cacheOfficeMetaData("WORK", "TASK",
				new HttpTemplateRequestHandlerDifferentiator());
		this.record_requestURI("/TASK.task");
		this.record_doNonMatchedRequestFlow();

		// Test
		this.doTest();
	}

	/**
	 * Ensures handle non matching request due to {@link Work} name.
	 */
	public void testNonMatchingWork() throws Throwable {
		// Record
		this.record_cacheOfficeMetaData("WORK", "TASK",
				new HttpTemplateRequestHandlerDifferentiator());
		this.record_requestURI("/NON_MATCHING-TASK.task");
		this.record_doNonMatchedRequestFlow();

		// Test
		this.doTest();
	}

	/**
	 * Ensures handle non matching request due to {@link Task} name.
	 */
	public void testNonMatchingTask() throws Throwable {
		// Record
		this.record_cacheOfficeMetaData("WORK", "TASK",
				new HttpTemplateRequestHandlerDifferentiator());
		this.record_requestURI("/WORK-NON_MATCHING.task");
		this.record_doNonMatchedRequestFlow();

		// Test
		this.doTest();
	}

	/**
	 * Ensures handle not using handler {@link Task} as it has incorrect
	 * differentiator type.
	 */
	public void testNonMatchingDifferentiator() throws Throwable {
		// Record
		this.record_cacheOfficeMetaData("WORK", "TASK",
				"Wrong differentiator type");
		this.record_requestURI("/WORK-TASK.task");
		this.record_doNonMatchedRequestFlow();

		// Test
		this.doTest();
	}

	/**
	 * Does the test.
	 */
	private void doTest() throws Throwable {
		// Replay
		this.replayMockObjects();

		// Create the task
		HttpTemplateRouteTask task = new HttpTemplateRouteTask(
				this.taskNamePrefix);

		// Ensure Office aware
		task.setOffice(this.office);

		// Route to task
		task.doTask(this.taskContext);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Record loading the meta-data for the {@link Office}.
	 * 
	 * @param workName
	 *            {@link Work} name.
	 * @param taskNameAndDifferentiatorPairs
	 *            Listing of {@link Task} name and its differentiator.
	 */
	private void record_cacheOfficeMetaData(String workName,
			Object... taskNameAndDifferentiatorPairs) throws Exception {

		// Record returning list of work names
		this.recordReturn(this.office, this.office.getWorkNames(),
				new String[] { workName });

		// Record obtaining the Work Manager
		WorkManager workManager = this.createMock(WorkManager.class);
		this.recordReturn(this.office, this.office.getWorkManager(workName),
				workManager);

		// Create the listing of task names and parameter types
		String[] taskNames = new String[taskNameAndDifferentiatorPairs.length / 2];
		Object[] differentiators = new Object[taskNameAndDifferentiatorPairs.length / 2];
		for (int i = 0; i < taskNameAndDifferentiatorPairs.length; i += 2) {
			taskNames[i / 2] = (this.taskNamePrefix == null ? ""
					: this.taskNamePrefix)
					+ ((String) taskNameAndDifferentiatorPairs[i]);
			differentiators[i / 2] = taskNameAndDifferentiatorPairs[i + 1];
		}

		// Record returning the listing of task names
		this.recordReturn(workManager, workManager.getTaskNames(), taskNames);

		// Iterate over tasks providing their parameter types
		for (int i = 0; i < taskNames.length; i++) {

			// Record obtaining the Task Manager
			String taskName = taskNames[i];
			TaskManager taskManager = this.createMock(TaskManager.class);
			this.recordReturn(workManager,
					workManager.getTaskManager(taskName), taskManager);

			// Record returning the differentiator
			Object differentiator = differentiators[i];
			this.recordReturn(taskManager, taskManager.getDifferentiator(),
					differentiator);
		}
	}

	/**
	 * Record obtaining the request URI.
	 * 
	 * @param requestUri
	 *            Request URI.
	 */
	private void record_requestURI(String requestUri) {
		this.recordReturn(
				this.taskContext,
				this.taskContext
						.getObject(HttpTemplateRouteDependencies.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getRequestURI(),
				requestUri);
	}

	/**
	 * Record doing the non matched {@link Flow}.
	 */
	private void record_doNonMatchedRequestFlow() {
		this.recordReturn(this.taskContext, this.taskContext.doFlow(
				HttpTemplateRouteTaskFlows.NON_MATCHED_REQUEST, null), null);
	}

}