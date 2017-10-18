/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.mock.MockNonMaterialisedHttpHeaders;
import net.officefloor.server.http.mock.MockProcessAwareContext;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteArrayByteSequence;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * Tests the {@link ProcessAwareServerHttpConnectionManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareServerHttpConnectionManagerTest extends OfficeFrameTestCase
		implements HttpResponseWriter<ByteBuffer> {

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation serverLocation = new HttpServerLocationImpl();

	/**
	 * {@link HttpMethod}.
	 */
	private HttpMethod method = HttpMethod.GET;

	/**
	 * Request URI.
	 */
	private String requestUri = "/";

	/**
	 * {@link HttpVersion}.
	 */
	private HttpVersion requestVersion = HttpVersion.HTTP_1_1;

	/**
	 * {@link NonMaterialisedHttpHeaders}.
	 */
	private MockNonMaterialisedHttpHeaders requestHeaders = new MockNonMaterialisedHttpHeaders();

	/**
	 * {@link MockStreamBufferPool}.
	 */
	private final MockStreamBufferPool bufferPool = new MockStreamBufferPool();

	/**
	 * {@link ProcessAwareServerHttpConnectionManagedObject} to be tested.
	 */
	private final ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this
			.createServerHttpConnection("TEST");

	/**
	 * Creates a {@link ProcessAwareServerHttpConnectionManagedObject} for
	 * testing.
	 * 
	 * @param requestEntityContent
	 *            Content for the {@link HttpRequest} entity.
	 */
	private ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> createServerHttpConnection(
			String requestEntityContent) {
		ByteSequence requestEntity = new ByteArrayByteSequence(
				requestEntityContent.getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
				this.serverLocation, true, () -> this.method, () -> this.requestUri, this.requestVersion,
				this.requestHeaders, requestEntity, this, this.bufferPool);
		connection.setProcessAwareContext(new MockProcessAwareContext());
		return connection;
	}

	/**
	 * Ensure correct {@link HttpRequest} details.
	 */
	public void testIntialiseConnection() throws IOException {

		// Add a request header
		this.requestHeaders.addHttpHeader("name", "value");

		// Ensure correct connection information
		assertEquals("Incorrect connection method", HttpMethod.GET, this.connection.getClientHttpMethod());

		// Ensure correct request information
		HttpRequest request = this.connection.getHttpRequest();
		assertEquals("Incorrect request method", HttpMethod.GET, request.getHttpMethod());
		assertEquals("Incorrect request URI", "/", request.getRequestURI());
		assertEquals("Incorrect request version", this.requestVersion, request.getHttpVersion());
		assertEquals("Incorrect number of request headers", 1, request.getHttpHeaders().length());
		assertEquals("Incorrect request header", "value", request.getHttpHeaders().getHeader("name").getValue());

		// Ensure correct request entity content
		StringWriter requestContent = new StringWriter();
		Reader entityReader = new InputStreamReader(request.getEntity(),
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		for (int character = entityReader.read(); character != -1; character = entityReader.read()) {
			requestContent.write(character);
		}
		assertEquals("Incorrect request content", "TEST", requestContent.toString());
	}

	/**
	 * Ensure can send default response.
	 */
	public void testDefaultSend() throws Throwable {

		// Send
		this.connection.getServiceFlowCallback().run(null);

		// Validate the default details
		assertEquals("Incorrect version", this.requestVersion, this.responseVersion);
		assertEquals("Incorect status", HttpStatus.NO_CONTENT, this.status);
		assertNull("Should be no headers", this.responseHeader);
		assertEquals("Should be no entity", 0, this.contentLength);
		assertNull("No Content-Type for no entity", this.contentType);
		assertNull("No entity content", this.contentHeadStreamBuffer);
	}

	/**
	 * Ensure can send altered information.
	 */
	public void testAlteredSend() throws Throwable {

		Charset charset = Charset.forName("UTF-16");

		// Alter the response
		HttpResponse response = this.connection.getHttpResponse();
		response.setHttpStatus(HttpStatus.CREATED);
		response.setHttpVersion(HttpVersion.HTTP_1_0);
		response.getHttpHeaders().addHeader("name", "value");
		response.setContentType("text/html", charset);
		ServerWriter writer = response.getEntityWriter();
		writer.write("TEST RESPONSE");

		// Send response
		this.connection.getServiceFlowCallback().run(null);

		// Obtain the expected content
		byte[] expectedContent = "TEST RESPONSE".getBytes(charset);

		// Validate the response
		assertEquals("Incorrect version", HttpVersion.HTTP_1_0, this.responseVersion);
		assertEquals("Incorect status", HttpStatus.CREATED, this.status);
		assertNotNull("Should be a headers", this.responseHeader);
		assertEquals("Incorrect header", "name", this.responseHeader.getName());
		assertNull("Should be just the one header", this.responseHeader.next);

		// Validate the content details
		assertEquals("Incorrect content length", expectedContent.length, this.contentLength);
		assertEquals("Incorrect content type", "text/html; charset=" + charset.name(), this.contentType.getValue());

		// Validate the content
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer, charset);
		assertEquals("Incorrect content", "TEST RESPONSE", content);
	}

	/**
	 * Ensure can flush {@link HttpResponse} without a send.
	 */
	public void testFlushResponseWithoutSend() throws Throwable {

		// Create the connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("DELAY");
		assertNull("Should not send on creation", this.status);

		// Flush response
		connection.getServiceFlowCallback().run(null);
		assertSame("Should now write response", HttpStatus.NO_CONTENT, this.status);

		// Ensure only flushes once
		this.status = null;
		connection.getServiceFlowCallback().run(null);
		assertNull("Should not write response again on flush", this.status);
	}

	/**
	 * Ensure handles {@link HttpException}.
	 */
	public void testHttpEscalation() throws Throwable {

		// Handle HTTP escalation
		HttpException escalation = new HttpException(HttpStatus.NOT_FOUND,
				new WritableHttpHeader[] { new WritableHttpHeader("name", "value") }, "ENTITY");
		this.connection.run(escalation);

		// Validate exception details
		assertEquals("Incorrect version", this.requestVersion, this.responseVersion);
		assertEquals("Incorrect status", HttpStatus.NOT_FOUND, this.status);
		assertEquals("Incorrect header", "name", this.responseHeader.getName());
		assertNull("Should be just the one header", this.responseHeader.next);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("Incorrect entity", "ENTITY", content);
	}

	/**
	 * Ensure reports {@link CleanupEscalation}.
	 */
	public void testSendCleanupEscalation() throws Throwable {

		// Create the connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("DELAY");

		// Write a response (should be reset)
		HttpResponse response = connection.getHttpResponse();
		response.getHttpHeaders().addHeader("TEST", "HEADER");
		response.setContentType("text/html", Charset.forName("UTF-16"));
		response.getEntityWriter().write("Content to be reset");

		// Create the clean up escalations
		final SQLException connectionEscalation = new SQLException("TEST");
		final NumberFormatException integerEscalation = new NumberFormatException("TEST");
		CleanupEscalation[] cleanupEscalations = new CleanupEscalation[] { new CleanupEscalation() {

			@Override
			public Class<?> getObjectType() {
				return Connection.class;
			}

			@Override
			public Throwable getEscalation() {
				return connectionEscalation;
			}
		}, new CleanupEscalation() {

			@Override
			public Class<?> getObjectType() {
				return Integer.class;
			}

			@Override
			public Throwable getEscalation() {
				return integerEscalation;
			}
		} };

		// Send the response (should delay to allow reset)
		response.send();

		// Load the clean up escalations
		ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler().handleCleanupEscalations(connection,
				cleanupEscalations);

		// Flush the response
		connection.getServiceFlowCallback().run(null);

		// Ensure correct response
		assertSame("Incorrect status", HttpStatus.INTERNAL_SERVER_ERROR, this.status);
		assertSame("Incorrect version", this.requestVersion, this.responseVersion);
		assertNull("Should be no headers", this.responseHeader);

		// Create the expected response
		StringWriter expected = new StringWriter();
		PrintWriter writer = new PrintWriter(expected);
		writer.println("Clean up failure with object of type " + Connection.class.getName());
		connectionEscalation.printStackTrace(writer);
		writer.println();
		writer.println();
		writer.println("Clean up failure with object of type " + Integer.class.getName());
		integerEscalation.printStackTrace(writer);
		writer.println();
		writer.println();
		writer.flush();
		String expectedContent = expected.toString();

		// Ensure correct content details
		assertEquals("Incorrect content-type value", "text/plain", this.contentType.getValue());
		assertEquals("Incorrect content-length value", this.contentLength, expectedContent.length());

		// Ensure correct content
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("Incorrect content", expectedContent, content);
	}

	/**
	 * Ensure can serialise the {@link ServerHttpConnection} state.
	 */
	public void testSerialiseState() throws IOException {

		// Create connection with altered request state
		this.method = HttpMethod.POST;
		this.requestUri = "/serialise";
		this.requestVersion = HttpVersion.HTTP_1_0;
		this.requestHeaders.addHttpHeader("serialise", "serialised");
		ServerHttpConnection connection = this.createServerHttpConnection("SERIALISE");
		assertServerHttpConnection(connection, HttpMethod.POST, "/serialise", HttpVersion.HTTP_1_0,
				new String[] { "serialise", "serialised" }, "SERIALISE", HttpMethod.POST,
				new String[] { "serialise", "serialised" });

		// Serialise out the connection state
		Serializable momento = connection.exportState();

		// Reset the connection details (for new connection)
		this.method = HttpMethod.GET;
		this.requestUri = "/";
		this.requestVersion = HttpVersion.HTTP_1_1;
		this.requestHeaders = new MockNonMaterialisedHttpHeaders();
		this.requestHeaders.addHttpHeader("input", "header");
		ServerHttpConnection newConnection = this.createServerHttpConnection("TEST");
		assertServerHttpConnection(newConnection, HttpMethod.GET, "/", HttpVersion.HTTP_1_1,
				new String[] { "input", "header" }, "TEST", HttpMethod.GET, new String[] { "input", "header" });

		// Ensure can import state (maintains version)
		newConnection.importState(momento);
		assertServerHttpConnection(newConnection, HttpMethod.POST, "/serialise", HttpVersion.HTTP_1_1,
				new String[] { "serialise", "serialised" }, "SERIALISE", HttpMethod.GET,
				new String[] { "input", "header" });
	}

	/**
	 * Asserts the content of the {@link HttpRequest}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection} containing the
	 *            {@link HttpRequest}.
	 * @param requestMethod
	 *            Expected {@link HttpRequest} {@link HttpMethod}.
	 * @param requestUri
	 *            Expected {@link HttpRequest} URI.
	 * @param requestVersion
	 *            Expected {@link HttpRequest} {@link HttpVersion}.
	 * @param requestHeaderNameValuePairs
	 *            Expected {@link HttpRequest} {@link HttpRequestHeaders}
	 *            name/value pairs.
	 * @param enityContent
	 *            Expected {@link HttpRequest} entity content.
	 * @param clientMethod
	 *            Expected client {@link HttpMethod}.
	 * @param clientHeaderNameValuePairs
	 *            Expected client {@link HttpRequestHeaders}.
	 */
	private void assertServerHttpConnection(ServerHttpConnection connection, HttpMethod requestMethod,
			String requestUri, HttpVersion requestVersion, String[] requestHeaderNameValuePairs, String enityContent,
			HttpMethod clientMethod, String[] clientHeaderNameValuePairs) throws IOException {

		// Validate the client details
		assertEquals("Incorrect client method", this.method, connection.getClientHttpMethod());
		assertEquals("Incorrect number of client headers", clientHeaderNameValuePairs.length / 2,
				connection.getClientHttpHeaders().length());
		for (int i = 0; i < clientHeaderNameValuePairs.length; i += 2) {
			String name = clientHeaderNameValuePairs[i];
			String value = clientHeaderNameValuePairs[i + 1];
			HttpHeader header = connection.getClientHttpHeaders().headerAt(i / 2);
			assertEquals("Incorrect client header name", name, header.getName());
			assertEquals("Incorrect client header value", value, header.getValue());
		}

		// Validate the request
		HttpRequest request = connection.getHttpRequest();
		assertEquals("Incorrect request method", requestMethod, request.getHttpMethod());
		assertEquals("Incorrect request URI", requestUri, request.getRequestURI());
		assertEquals("Incorrect request version", requestVersion, request.getHttpVersion());

		// Validate the request headers
		assertEquals("Incorrect number of request headers", requestHeaderNameValuePairs.length / 2,
				request.getHttpHeaders().length());
		for (int i = 0; i < requestHeaderNameValuePairs.length; i += 2) {
			String name = requestHeaderNameValuePairs[i];
			String value = requestHeaderNameValuePairs[i + 1];
			HttpHeader header = request.getHttpHeaders().headerAt(i / 2);
			assertEquals("Incorrect request header name", name, header.getName());
			assertEquals("Incorrect request header value", value, header.getValue());
		}

		// Validate the entity content
		StringWriter actualContent = new StringWriter();
		InputStreamReader reader = new InputStreamReader(request.getEntity(),
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		for (int character = reader.read(); character != -1; character = reader.read()) {
			actualContent.write(character);
		}
		assertEquals("Incorrect request entity", enityContent, actualContent.toString());
	}

	/*
	 * ===================== HttpResponseWriter ============================
	 */

	private HttpVersion responseVersion = null;

	private HttpStatus status = null;

	private WritableHttpHeader responseHeader = null;

	private long contentLength;

	private HttpHeaderValue contentType = null;

	private StreamBuffer<ByteBuffer> contentHeadStreamBuffer = null;

	@Override
	public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader httpHeader,
			long contentLength, HttpHeaderValue contentType, StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {
		this.responseVersion = version;
		this.status = status;
		this.responseHeader = httpHeader;
		this.contentLength = contentLength;
		this.contentType = contentType;
		this.contentHeadStreamBuffer = contentHeadStreamBuffer;
	}

}