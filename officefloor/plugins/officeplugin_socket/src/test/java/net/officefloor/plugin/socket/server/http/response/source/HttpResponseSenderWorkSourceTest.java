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
package net.officefloor.plugin.socket.server.http.response.source;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.ByteBuffer;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseSendTask.HttpResponseSendTaskDependencies;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.stream.WriteBufferReceiver;
import net.officefloor.plugin.stream.impl.ServerOutputStreamImpl;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link HttpResponseSenderWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseSenderWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil
				.validateSpecification(HttpResponseSenderWorkSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {
		HttpResponseSendTask task = new HttpResponseSendTask(-1, null);
		FunctionNamespaceBuilder<Work> workTypeBuilder = WorkLoaderUtil
				.createWorkTypeBuilder(task);
		ManagedFunctionTypeBuilder<HttpResponseSendTaskDependencies, None> taskBuilder = workTypeBuilder
				.addManagedFunctionType("SEND", task,
						HttpResponseSendTaskDependencies.class, None.class);
		taskBuilder.addObject(ServerHttpConnection.class).setKey(
				HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		taskBuilder.addEscalation(IOException.class);
		WorkLoaderUtil.validateWorkType(workTypeBuilder,
				HttpResponseSenderWorkSource.class);
	}

	/**
	 * Ensure can trigger sending the {@link HttpResponse} with no content.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testTriggerSendWithNoContent() throws Throwable {

		final int status = 204;

		ManagedFunctionContext<Work, HttpResponseSendTaskDependencies, None> taskContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpResponse response = this.createMock(HttpResponse.class);

		// Record
		this.recordReturn(
				taskContext,
				taskContext
						.getObject(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpResponse(), response);
		response.setStatus(status);
		response.send();

		// Test
		this.replayMockObjects();

		// Create the task
		FunctionNamespaceType<Work> work = WorkLoaderUtil.loadWorkType(
				HttpResponseSenderWorkSource.class,
				HttpResponseSenderWorkSource.PROPERTY_HTTP_STATUS,
				String.valueOf(status));
		ManagedFunction task = work.getManagedFunctionTypes()[0].getManagedFunctionFactory().createManagedFunction(
				work.getWorkFactory().createWork());

		// Execute the task
		task.execute(taskContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can trigger sending the {@link HttpResponse} with content.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testTriggerSendWithContent() throws Throwable {

		final int status = 204;
		final String testContentFileName = "TestContent.html";
		final String testContentFilePath = this.getPackageRelativePath(this
				.getClass()) + "/" + testContentFileName;
		File testContentFile = this.findFile(this.getClass(),
				"TestContent.html");
		String testContentFileContents = this.getFileContents(testContentFile);

		ManagedFunctionContext<Work, HttpResponseSendTaskDependencies, None> taskContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpResponse response = this.createMock(HttpResponse.class);
		WriteBufferReceiver receiver = this
				.createMock(WriteBufferReceiver.class);
		final WriteBuffer writeBuffer = this.createMock(WriteBuffer.class);

		// Record
		this.recordReturn(
				taskContext,
				taskContext
						.getObject(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpResponse(), response);
		response.setStatus(status);
		this.recordReturn(response, response.getEntity(),
				new ServerOutputStreamImpl(receiver, 1024));
		this.recordReturn(receiver, receiver.getLock(), new Object());
		final ByteBuffer[] entityContent = new ByteBuffer[1];
		this.recordReturn(receiver, receiver.createWriteBuffer(null),
				writeBuffer, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						entityContent[0] = (ByteBuffer) actual[0];
						return true;
					}
				});
		response.send();

		// Test
		this.replayMockObjects();

		// Create the task
		FunctionNamespaceType<Work> work = WorkLoaderUtil
				.loadWorkType(
						HttpResponseSenderWorkSource.class,
						HttpResponseSenderWorkSource.PROPERTY_HTTP_STATUS,
						String.valueOf(status),
						HttpResponseSenderWorkSource.PROPERTY_HTTP_RESPONSE_CONTENT_FILE,
						testContentFilePath);
		ManagedFunction task = work.getManagedFunctionTypes()[0].getManagedFunctionFactory().createManagedFunction(
				work.getWorkFactory().createWork());

		// Execute the task
		task.execute(taskContext);
		this.verifyMockObjects();

		// Validate the entity contents
		byte[] entityData = new byte[entityContent[0].limit()];
		entityContent[0].get(entityData);
		assertContents(new StringReader(testContentFileContents),
				new InputStreamReader(new ByteArrayInputStream(entityData)));
	}

}