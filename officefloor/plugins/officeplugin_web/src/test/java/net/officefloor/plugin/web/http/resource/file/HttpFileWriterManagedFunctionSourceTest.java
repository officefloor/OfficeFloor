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
package net.officefloor.plugin.web.http.resource.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.file.HttpFileWriterFunction.HttpFileWriterFunctionDependencies;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.parse.UsAsciiUtil;
import net.officefloor.server.stream.MockServerOutputStream;

/**
 * Tests the {@link HttpFileWriterManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileWriterManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(HttpFileWriterManagedFunctionSource.class);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {
		HttpFileWriterFunction function = new HttpFileWriterFunction();
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
		ManagedFunctionTypeBuilder<HttpFileWriterFunctionDependencies, None> file = namespace.addManagedFunctionType(
				"WriteFileToResponse", function, HttpFileWriterFunctionDependencies.class, None.class);
		file.addObject(HttpFile.class).setKey(HttpFileWriterFunctionDependencies.HTTP_FILE);
		file.addObject(ServerHttpConnection.class).setKey(HttpFileWriterFunctionDependencies.SERVER_HTTP_CONNECTION);
		file.addEscalation(IOException.class);
		ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace, HttpFileWriterManagedFunctionSource.class);
	}

	/**
	 * Ensure can write a {@link HttpFile}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testWriteHttpFile() throws Throwable {

		final String contentEncoding = "gzip";
		final String contentType = "text/plain";
		final Charset charset = UsAsciiUtil.US_ASCII;

		ManagedFunctionContext<HttpFileWriterFunctionDependencies, None> functionContext = this
				.createMock(ManagedFunctionContext.class);
		HttpFile httpFile = this.createMock(HttpFile.class);
		ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		HttpResponse response = this.createMock(HttpResponse.class);
		HttpHeader header = this.createMock(HttpHeader.class);
		ByteBuffer contents = ByteBuffer.wrap("TEST".getBytes());
		MockServerOutputStream entity = new MockServerOutputStream();

		// Record
		this.recordReturn(functionContext, functionContext.getObject(HttpFileWriterFunctionDependencies.HTTP_FILE),
				httpFile);
		this.recordReturn(functionContext,
				functionContext.getObject(HttpFileWriterFunctionDependencies.SERVER_HTTP_CONNECTION), connection);
		this.recordReturn(connection, connection.getHttpResponse(), response);
		response.reset();
		this.recordReturn(httpFile, httpFile.getContentEncoding(), contentEncoding);
		this.recordReturn(response, response.addHeader("Content-Encoding", contentEncoding), header);
		this.recordReturn(httpFile, httpFile.getContentType(), contentType);
		this.recordReturn(httpFile, httpFile.getCharset(), charset);
		response.setContentType(contentType, charset);
		this.recordReturn(httpFile, httpFile.getContents(), contents);
		this.recordReturn(response, response.getEntityWriter(), entity.getServerWriter());

		// Test
		this.replayMockObjects();

		// Create the function
		FunctionNamespaceType namespace = ManagedFunctionLoaderUtil
				.loadManagedFunctionType(HttpFileWriterManagedFunctionSource.class);
		ManagedFunction function = namespace.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();

		// Execute the function
		assertNull(function.execute(functionContext));

		this.verifyMockObjects();

		// Validate the entity content
		entity.flush();
		assertEquals("Incorrect entity content", "TEST", new String(entity.getWrittenBytes()));
	}

}