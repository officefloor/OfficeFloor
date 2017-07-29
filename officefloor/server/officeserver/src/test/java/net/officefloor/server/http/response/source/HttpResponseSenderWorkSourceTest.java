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
package net.officefloor.server.http.response.source;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.ByteBuffer;

import org.easymock.AbstractMatcher;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.response.source.HttpResponseSendFunction;
import net.officefloor.server.http.response.source.HttpResponseSenderManagedFunctionSource;
import net.officefloor.server.http.response.source.HttpResponseSendFunction.HttpResponseSendTaskDependencies;
import net.officefloor.server.protocol.WriteBuffer;
import net.officefloor.server.stream.WriteBufferReceiver;
import net.officefloor.server.stream.impl.ServerOutputStreamImpl;

/**
 * Tests the {@link HttpResponseSenderManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseSenderWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(HttpResponseSenderManagedFunctionSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {
		FunctionNamespaceBuilder functionTypeBuilder = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
		ManagedFunctionTypeBuilder<HttpResponseSendTaskDependencies, None> functionBuilder = functionTypeBuilder
				.addManagedFunctionType("SEND", new HttpResponseSendFunction(-1, null),
						HttpResponseSendTaskDependencies.class, None.class);
		functionBuilder.addObject(ServerHttpConnection.class)
				.setKey(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		functionBuilder.addEscalation(IOException.class);
		ManagedFunctionLoaderUtil.validateManagedFunctionType(functionTypeBuilder,
				HttpResponseSenderManagedFunctionSource.class);
	}

	/**
	 * Ensure can trigger sending the {@link HttpResponse} with no content.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testTriggerSendWithNoContent() throws Throwable {

		final int status = 204;

		ManagedFunctionContext<HttpResponseSendTaskDependencies, None> managedFunctionContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		HttpResponse response = this.createMock(HttpResponse.class);

		// Record
		this.recordReturn(managedFunctionContext,
				managedFunctionContext.getObject(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION), connection);
		this.recordReturn(connection, connection.getHttpResponse(), response);
		response.setStatus(status);
		response.send();

		// Test
		this.replayMockObjects();

		// Create the function
		FunctionNamespaceType namespace = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				HttpResponseSenderManagedFunctionSource.class,
				HttpResponseSenderManagedFunctionSource.PROPERTY_HTTP_STATUS, String.valueOf(status));
		ManagedFunction function = namespace.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();

		// Execute the function
		function.execute(managedFunctionContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can trigger sending the {@link HttpResponse} with content.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testTriggerSendWithContent() throws Throwable {

		final int status = 204;
		final String testContentFileName = "TestContent.html";
		final String testContentFilePath = this.getPackageRelativePath(this.getClass()) + "/" + testContentFileName;
		File testContentFile = this.findFile(this.getClass(), "TestContent.html");
		String testContentFileContents = this.getFileContents(testContentFile);

		ManagedFunctionContext<HttpResponseSendTaskDependencies, None> managedFunctionContext = this
				.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		HttpResponse response = this.createMock(HttpResponse.class);
		WriteBufferReceiver receiver = this.createMock(WriteBufferReceiver.class);
		final WriteBuffer writeBuffer = this.createMock(WriteBuffer.class);

		// Record
		this.recordReturn(managedFunctionContext,
				managedFunctionContext.getObject(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION), connection);
		this.recordReturn(connection, connection.getHttpResponse(), response);
		response.setStatus(status);
		this.recordReturn(response, response.getEntity(), new ServerOutputStreamImpl(receiver, 1024));
		this.recordReturn(receiver, receiver.getWriteLock(), new Object());
		final ByteBuffer[] entityContent = new ByteBuffer[1];
		this.recordReturn(receiver, receiver.createWriteBuffer(null), writeBuffer, new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				entityContent[0] = (ByteBuffer) actual[0];
				return true;
			}
		});
		response.send();

		// Test
		this.replayMockObjects();

		// Create the function
		FunctionNamespaceType namespace = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				HttpResponseSenderManagedFunctionSource.class,
				HttpResponseSenderManagedFunctionSource.PROPERTY_HTTP_STATUS, String.valueOf(status),
				HttpResponseSenderManagedFunctionSource.PROPERTY_HTTP_RESPONSE_CONTENT_FILE, testContentFilePath);
		ManagedFunction function = namespace.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();

		// Execute the function
		function.execute(managedFunctionContext);
		this.verifyMockObjects();

		// Validate the entity contents
		byte[] entityData = new byte[entityContent[0].limit()];
		entityContent[0].get(entityData);
		assertContents(new StringReader(testContentFileContents),
				new InputStreamReader(new ByteArrayInputStream(entityData)));
	}

}