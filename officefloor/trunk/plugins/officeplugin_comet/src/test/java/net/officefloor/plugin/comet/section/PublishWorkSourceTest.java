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
package net.officefloor.plugin.comet.section;

import java.lang.reflect.Method;

import com.google.gwt.user.server.rpc.RPCRequest;

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometPublicationService;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.section.PublishWorkSource.Dependencies;
import net.officefloor.plugin.comet.section.PublishWorkSource.PublishTask;
import net.officefloor.plugin.comet.spi.CometService;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * Tests the {@link PublishWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PublishWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(PublishWorkSource.class);
	}

	/**
	 * Validate type for automatically publishing {@link CometEvent} instances.
	 */
	public void testTypeForAutomatedPublishing() {

		// Create the expected type
		PublishTask factory = new PublishTask(null);
		WorkTypeBuilder<PublishTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(factory);
		TaskTypeBuilder<Dependencies, Indexed> task = type.addTaskType(
				"PUBLISH", factory, Dependencies.class, Indexed.class);
		task.addObject(ServerGwtRpcConnection.class).setKey(
				Dependencies.SERVER_GWT_RPC_CONNECTION);
		task.addObject(CometService.class).setKey(Dependencies.COMET_SERVICE);

		// Validate type
		WorkLoaderUtil.validateWorkType(type, PublishWorkSource.class);
	}

	/**
	 * Validate type for manually publishing {@link CometEvent} instances.
	 */
	public void testTypeForManualPublishing() {

		// Create the expected type
		PublishTask factory = new PublishTask(null);
		WorkTypeBuilder<PublishTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(factory);
		TaskTypeBuilder<Dependencies, Indexed> task = type.addTaskType(
				"PUBLISH", factory, Dependencies.class, Indexed.class);
		task.addObject(ServerGwtRpcConnection.class).setKey(
				Dependencies.SERVER_GWT_RPC_CONNECTION);
		task.addObject(CometService.class).setKey(Dependencies.COMET_SERVICE);

		// First manual flow
		TaskFlowTypeBuilder<Indexed> flowOne = task.addFlow();
		flowOne.setArgumentType(CometEvent.class);
		flowOne.setLabel("one");

		// Second manual flow
		TaskFlowTypeBuilder<Indexed> flowTwo = task.addFlow();
		flowTwo.setArgumentType(CometEvent.class);
		flowTwo.setLabel("two");

		// Validate type
		WorkLoaderUtil.validateWorkType(type, PublishWorkSource.class,
				PublishWorkSource.PROPERTY_MANUAL_PUBLISH_URI_PREFIX + "one",
				"/templateOne",
				PublishWorkSource.PROPERTY_MANUAL_PUBLISH_URI_PREFIX + "two",
				"/templateTwo");
	}

	/**
	 * Validate appropriately executes {@link Task} for automatic publishing.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testExecuteForAutomaticPublishing() throws Throwable {

		final Method publishMethod = CometPublicationService.class.getMethod(
				"publish", CometEvent.class);
		final CometEvent event = new CometEvent("LISTENER_TYPE", "EVENT",
				"MATCH_KEY");

		final TaskContext context = this.createMock(TaskContext.class);
		final ServerGwtRpcConnection<Long> connection = this
				.createMock(ServerGwtRpcConnection.class);
		final RPCRequest rpcRequest = new RPCRequest(publishMethod,
				new Object[] { event }, null, 0);
		final HttpRequest httpRequest = this.createMock(HttpRequest.class);
		final CometService service = this.createMock(CometService.class);
		final Long sequenceNumber = Long.valueOf(1);

		// Record
		this.recordReturn(context,
				context.getObject(Dependencies.SERVER_GWT_RPC_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getRpcRequest(), rpcRequest);
		this.recordReturn(connection, connection.getHttpRequest(), httpRequest);
		this.recordReturn(httpRequest, httpRequest.getRequestURI(),
				"/template/comet-publish");
		this.recordReturn(context,
				context.getObject(Dependencies.COMET_SERVICE), service);
		this.recordReturn(service, service.publishEvent(
				CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER, "LISTENER_TYPE",
				"EVENT", "MATCH_KEY"), sequenceNumber);
		connection.onSuccess(sequenceNumber);

		// Test
		this.replayMockObjects();

		// Load the type (for automatic publishing for URI)
		WorkType<PublishTask> type = WorkLoaderUtil
				.loadWorkType(PublishWorkSource.class);

		// Create the task
		PublishTask work = type.getWorkFactory().createWork();
		Task<PublishTask, ?, ?> task = type.getTaskTypes()[0].getTaskFactory()
				.createTask(work);

		// Ensure handle automatically
		task.doTask(context);

		// Validate
		this.verifyMockObjects();
	}

	/**
	 * Validate appropriately executes {@link Task} for manual publishing.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testExecuteForManualPublishing() throws Throwable {

		final Method publishMethod = CometPublicationService.class.getMethod(
				"publish", CometEvent.class);
		final CometEvent event = new CometEvent("TEST", "EVENT", null);

		final TaskContext context = this.createMock(TaskContext.class);
		final ServerGwtRpcConnection<?> connection = this
				.createMock(ServerGwtRpcConnection.class);
		final RPCRequest rpcRequest = new RPCRequest(publishMethod,
				new Object[] { event }, null, 0);
		final HttpRequest httpRequest = this.createMock(HttpRequest.class);

		// Record
		this.recordReturn(context,
				context.getObject(Dependencies.SERVER_GWT_RPC_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getRpcRequest(), rpcRequest);
		this.recordReturn(connection, connection.getHttpRequest(), httpRequest);
		this.recordReturn(httpRequest, httpRequest.getRequestURI(),
				"/template/comet-publish");
		this.recordReturn(context, context.doFlow(0, event), null);

		// Test
		this.replayMockObjects();

		// Load the type (for manual publishing on URI)
		WorkType<PublishTask> type = WorkLoaderUtil.loadWorkType(
				PublishWorkSource.class,
				PublishWorkSource.PROPERTY_MANUAL_PUBLISH_URI_PREFIX
						+ "template", "/template");

		// Create the task
		PublishTask work = type.getWorkFactory().createWork();
		Task<PublishTask, ?, ?> task = type.getTaskTypes()[0].getTaskFactory()
				.createTask(work);

		// Ensure handle automatically
		task.doTask(context);

		// Validate
		this.verifyMockObjects();
	}

}