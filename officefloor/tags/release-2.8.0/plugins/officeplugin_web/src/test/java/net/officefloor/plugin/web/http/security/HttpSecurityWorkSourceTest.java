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
package net.officefloor.plugin.web.http.security;

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.HttpSecurityTask;
import net.officefloor.plugin.web.http.security.HttpSecurityWorkSource;
import net.officefloor.plugin.web.http.security.HttpSecurityTask.DependencyKeys;
import net.officefloor.plugin.web.http.security.HttpSecurityTask.FlowKeys;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;

/**
 * Tests the {@link HttpSecurityWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<HttpSecurityTask, DependencyKeys, FlowKeys> taskContext = this
			.createMock(TaskContext.class);

	/**
	 * {@link HttpSecurityService}.
	 */
	private final HttpSecurityService service = this
			.createMock(HttpSecurityService.class);

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity security = this.createMock(HttpSecurity.class);

	/**
	 * {@link FlowFuture}.
	 */
	private final FlowFuture flowFuture = this.createMock(FlowFuture.class);

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpSecurityWorkSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the work/task factory
		HttpSecurityTask taskFactory = new HttpSecurityTask();

		// Create the expected type
		WorkTypeBuilder<HttpSecurityTask> work = WorkLoaderUtil
				.createWorkTypeBuilder(taskFactory);
		TaskTypeBuilder<DependencyKeys, FlowKeys> task = work.addTaskType(
				"Authenticate", taskFactory, DependencyKeys.class,
				FlowKeys.class);
		task.addObject(HttpSecurityService.class).setKey(
				DependencyKeys.HTTP_SECURITY_SERVICE);
		TaskFlowTypeBuilder<FlowKeys> authenticated = task.addFlow();
		authenticated.setKey(FlowKeys.AUTHENTICATED);
		authenticated.setArgumentType(HttpSecurity.class);
		TaskFlowTypeBuilder<FlowKeys> unauthenticated = task.addFlow();
		unauthenticated.setKey(FlowKeys.UNAUTHENTICATED);
		task.setReturnType(HttpSecurity.class);
		task.addEscalation(AuthenticationException.class);

		// Validate the type
		WorkLoaderUtil.validateWorkType(work, HttpSecurityWorkSource.class);
	}

	/**
	 * Ensure able to authenticate.
	 */
	public void testAuthenticate() throws Throwable {

		// Record
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.HTTP_SECURITY_SERVICE), this.service);
		this.recordReturn(this.service, this.service.authenticate(),
				this.security);
		this.recordReturn(this.taskContext, this.taskContext.doFlow(
				FlowKeys.AUTHENTICATED, this.security), this.flowFuture);

		// Test
		this.replayMockObjects();
		HttpSecurityTask task = this.createHttpSecurityTask();
		Object value = task.doTask(this.taskContext);
		this.verifyMockObjects();

		// Ensure correct return
		assertEquals("Incorrect return", this.security, value);
	}

	/**
	 * Ensure appropriate handling if not authenticated.
	 */
	public void testNotAuthenticated() throws Throwable {

		// Record
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(DependencyKeys.HTTP_SECURITY_SERVICE), this.service);
		this.recordReturn(this.service, this.service.authenticate(), null);
		this.service.loadUnauthorised();
		this.recordReturn(this.taskContext, this.taskContext.doFlow(
				FlowKeys.UNAUTHENTICATED, null), this.flowFuture);

		// Test
		this.replayMockObjects();
		HttpSecurityTask task = this.createHttpSecurityTask();
		Object value = task.doTask(this.taskContext);
		this.verifyMockObjects();

		// Ensure no return as not authenticated
		assertNull("Incorrect return", value);
	}

	/**
	 * Creates the {@link HttpSecurityTask}.
	 * 
	 * @return {@link HttpSecurityTask}.
	 */
	private HttpSecurityTask createHttpSecurityTask() {

		// Create the task
		WorkType<HttpSecurityTask> type = WorkLoaderUtil
				.loadWorkType(HttpSecurityWorkSource.class);
		HttpSecurityTask work = type.getWorkFactory().createWork();
		HttpSecurityTask task = (HttpSecurityTask) type.getTaskTypes()[0]
				.getTaskFactory().createTask(work);

		// Return the task
		return task;
	}

}