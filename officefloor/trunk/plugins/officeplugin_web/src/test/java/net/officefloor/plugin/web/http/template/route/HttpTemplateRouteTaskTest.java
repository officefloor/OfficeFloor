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
import net.officefloor.plugin.web.http.template.RequestHandlerTask.RequestHandlerIdentifier;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteDependencies;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteTaskFlows;

/**
 * Tests the {@link HttpTemplateRouteTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateRouteTaskTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpTemplateRouteTask} to test.
	 */
	private final HttpTemplateRouteTask task = new HttpTemplateRouteTask();

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
	 * Ensure can route to {@link Task}.
	 */
	public void testRoute() throws Throwable {
		// Record
		this.record_cacheOfficeMetaData("WORK", "TASK",
				RequestHandlerIdentifier.class);
		this.record_requestURI("/WORK/TASK.task");
		this.taskContext.doFlow("WORK", "TASK", null);

		// Test
		this.doTest();
	}

	/**
	 * Ensure handle no {@link Work} name (only {@link Task} name).
	 */
	public void testNoWorkName() throws Throwable {
		// Record
		this.record_cacheOfficeMetaData("WORK", "TASK",
				RequestHandlerIdentifier.class);
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
				RequestHandlerIdentifier.class);
		this.record_requestURI("/NON_MATCHING/TASK.task");
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
				RequestHandlerIdentifier.class);
		this.record_requestURI("/WORK/NON_MATCHING.task");
		this.record_doNonMatchedRequestFlow();

		// Test
		this.doTest();
	}

	/**
	 * Ensures handle not using handler {@link Task} as it has incorrect
	 * parameter type.
	 */
	public void testNonMatchingParameterType() throws Throwable {
		// Record
		this.record_cacheOfficeMetaData("WORK", "TASK", Integer.class);
		this.record_requestURI("/WORK/TASK.task");
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

		// Ensure Office aware
		this.task.setOffice(this.office);

		// Route to task
		this.task.doTask(this.taskContext);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Record loading the meta-data for the {@link Office}.
	 * 
	 * @param workName
	 *            {@link Work} name.
	 * @param taskParamPairs
	 *            Listing of {@link Task} name and parameter type pairs.
	 */
	private void record_cacheOfficeMetaData(String workName,
			Object... taskParamPairs) throws Exception {

		// Record returning list of work names
		this.recordReturn(this.office, this.office.getWorkNames(),
				new String[] { workName });

		// Record obtaining the Work Manager
		WorkManager workManager = this.createMock(WorkManager.class);
		this.recordReturn(this.office, this.office.getWorkManager(workName),
				workManager);

		// Create the listing of task names and parameter types
		String[] taskNames = new String[taskParamPairs.length / 2];
		Class<?>[] parameterTypes = new Class[taskParamPairs.length / 2];
		for (int i = 0; i < taskParamPairs.length; i += 2) {
			taskNames[i / 2] = (String) taskParamPairs[i];
			parameterTypes[i / 2] = (Class<?>) taskParamPairs[i + 1];
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

			// Record returning the parameter type
			Class<?> parameterType = parameterTypes[i];
			this.recordReturn(taskManager, taskManager.getParameterType(),
					parameterType);
		}
	}

	/**
	 * Record obtaining the request URI.
	 * 
	 * @param requestUri
	 *            Request URI.
	 */
	private void record_requestURI(String requestUri) {
		this
				.recordReturn(
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