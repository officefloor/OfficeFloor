/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.CleanupException;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpRequestCookies;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.mock.MockManagedObjectContext;
import net.officefloor.server.http.mock.MockNonMaterialisedHttpHeaders;
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
	 * Creates a {@link ProcessAwareServerHttpConnectionManagedObject} for testing.
	 * 
	 * @param requestEntityContent Content for the {@link HttpRequest} entity.
	 */
	private ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> createServerHttpConnection(
			String requestEntityContent) {
		return this.createServerHttpConnection(requestEntityContent, null, null);
	}

	/**
	 * Creates a {@link ProcessAwareServerHttpConnectionManagedObject} for testing.
	 * 
	 * @param requestEntityContent Content for the {@link HttpRequest} entity.
	 * @param serverName           Name of the server. May be <code>null</code>.
	 * @param dateHttpHeaderClock  {@link DateHttpHeaderClock}. May be
	 *                             <code>null</code>.
	 */
	private ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> createServerHttpConnection(
			String requestEntityContent, String serverName, DateHttpHeaderClock dateHttpHeaderClock) {
		HttpHeaderValue serverHttpHeaderValue = serverName == null ? null : new HttpHeaderValue(serverName);
		ByteSequence requestEntity = new ByteArrayByteSequence(
				requestEntityContent.getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
				this.serverLocation, true, () -> this.method, () -> this.requestUri, this.requestVersion,
				this.requestHeaders, requestEntity, serverHttpHeaderValue, dateHttpHeaderClock, true, this,
				this.bufferPool);
		connection.setManagedObjectContext(new MockManagedObjectContext());
		return connection;
	}

	/**
	 * Ensure correct {@link HttpRequest} details.
	 */
	public void testIntialiseConnection() throws IOException {

		// Add a request header (and cookie)
		this.requestHeaders.addHttpHeader("header", "one");
		this.requestHeaders.addHttpHeader("cookie", "cookie=two");

		// Ensure correct connection information
		assertEquals("Incorrect connection method", HttpMethod.GET, this.connection.getClientRequest().getMethod());

		// Ensure correct request information
		HttpRequest request = this.connection.getRequest();
		assertEquals("Incorrect request method", HttpMethod.GET, request.getMethod());
		assertEquals("Incorrect request URI", "/", request.getUri());
		assertEquals("Incorrect request version", this.requestVersion, request.getVersion());
		assertEquals("Incorrect number of request headers", 2, request.getHeaders().length());
		assertEquals("Incorrect request header", "one", request.getHeaders().getHeader("header").getValue());
		assertEquals("Incorrect number of cookies", 1, request.getCookies().length());
		assertEquals("Incorrect cookie", "two", request.getCookies().getCookie("cookie").getValue());

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
		assertNull("Should be no cookies", this.responseCookie);
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
		HttpResponse response = this.connection.getResponse();
		response.setStatus(HttpStatus.CREATED);
		response.setVersion(HttpVersion.HTTP_1_0);
		response.getHeaders().addHeader("name", "value");
		response.getCookies().setCookie("name", "value");
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
		assertNotNull("Should be a header", this.responseHeader);
		assertNotNull("Should be a cookie", this.responseCookie);
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
	 * Ensure can send Server name.
	 */
	public void testServerHttpHeader() throws Throwable {

		// Create with server name
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("TEST",
				"OfficeFloorServer", null);
		connection.getServiceFlowCallback().run(null);

		// Ensure server HTTP header
		assertNotNull("Should have HTTP header", this.responseHeader);
		assertEquals("Incorrect Server HTTP header", "server", this.responseHeader.getName());
		assertEquals("Incorrect Server value", "OfficeFloorServer", this.responseHeader.getValue());
		assertNull("Should just be Server header", this.responseHeader.next);
	}

	/**
	 * Ensure can send Date.
	 */
	public void testDateHttpHeader() throws Throwable {

		// Create with server name
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("TEST",
				null, () -> new HttpHeaderValue("<date>"));
		connection.getServiceFlowCallback().run(null);

		// Ensure date HTTP header
		assertNotNull("Should have HTTP header", this.responseHeader);
		assertEquals("Incorrect Date HTTP header", "date", this.responseHeader.getName());
		assertEquals("Incorrect Date value", "<date>", this.responseHeader.getValue());
		assertNull("Should just be Date header", this.responseHeader.next);
	}

	/**
	 * Ensure able to send Server, Date and Custom {@link HttpHeader} without issue.
	 */
	public void testServerDateCustomHttpHeaders() throws Throwable {

		// Create with server name
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("TEST",
				"OfficeFloorServer", () -> new HttpHeaderValue("<date>"));
		HttpResponseHeaders responseHeaders = connection.getResponse().getHeaders();
		responseHeaders.addHeader("Custom", "test");
		responseHeaders.addHeader(new HttpHeaderName("MaintainCase", true), "Case Sensitive");

		// Send response
		connection.getServiceFlowCallback().run(null);

		// Ensure date HTTP header
		assertNotNull("Should have HTTP header", this.responseHeader);

		// Validate server
		assertEquals("Incorrect Server HTTP header", "server", this.responseHeader.getName());
		assertEquals("Incorrect Server value", "OfficeFloorServer", this.responseHeader.getValue());

		// Validate date
		this.responseHeader = this.responseHeader.next;
		assertEquals("Incorrect Date HTTP header", "date", this.responseHeader.getName());
		assertEquals("Incorrect Date value", "<date>", this.responseHeader.getValue());

		// Validate custom
		this.responseHeader = this.responseHeader.next;
		assertEquals("Incorrect Custom HTTP header", "custom", this.responseHeader.getName());
		assertEquals("Incorrect Custom value", "test", this.responseHeader.getValue());

		// Ensure can maintain case (clients may not not be case insensitive)
		this.responseHeader = this.responseHeader.next;
		assertEquals("Incorrect maintain case HTTP header", "MaintainCase", this.responseHeader.getName());
		assertEquals("Incorrect maintain case value", "Case Sensitive", this.responseHeader.getValue());

		// Ensure no further headers
		this.responseHeader = this.responseHeader.next;
		assertNull("Should be no further headers", this.responseHeader);
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
	 * Ensure handles {@link Exception}.
	 */
	public void testException() throws Throwable {

		// Handle exception
		final Exception exception = new Exception("TEST");
		this.connection.getServiceFlowCallback().run(exception);

		// Validate exception details
		assertEquals("Incorrect version", this.requestVersion, this.responseVersion);
		assertEquals("Incorrect status", HttpStatus.INTERNAL_SERVER_ERROR, this.status);
		assertNull("Should be no headers", this.responseHeader);
		assertNull("Should be no cookies", this.responseCookie);

		// Verify send stack trace
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		StringWriter expected = new StringWriter();
		PrintWriter writer = new PrintWriter(expected);
		exception.printStackTrace(writer);
		writer.flush();
		assertEquals("Incorrect entity", expected.toString(), content);
	}

	/**
	 * Ensure can handle {@link Exception}.
	 */
	public void testHandleEscalation() throws Throwable {

		// Handle escalation
		final Exception escalation = new Exception("TEST");
		this.connection.getResponse().setEscalationHandler((context) -> {
			context.getServerHttpConnection().getResponse().getEntityWriter()
					.write("{escalation: '" + context.getEscalation().getMessage() + "'}");
			return true;
		});
		this.connection.getServiceFlowCallback().run(escalation);

		// Validate escalation details
		assertEquals("Incorrect version", this.requestVersion, this.responseVersion);
		assertEquals("Incorrect status", HttpStatus.INTERNAL_SERVER_ERROR, this.status);
		assertNull("Should be no headers", this.responseHeader);
		assertNull("Should be no cookies", this.responseCookie);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("Incorrect entity", "{escalation: 'TEST'}", content);
	}

	/**
	 * Ensure handles {@link HttpException}.
	 */
	public void testHttpEscalation() throws Throwable {

		// Handle HTTP escalation
		HttpException escalation = new HttpException(HttpStatus.NOT_FOUND,
				new WritableHttpHeader[] { new WritableHttpHeader("name", "value") }, "ENTITY");
		this.connection.getServiceFlowCallback().run(escalation);

		// Validate exception details
		assertEquals("Incorrect version", this.requestVersion, this.responseVersion);
		assertEquals("Incorrect status", HttpStatus.NOT_FOUND, this.status);
		assertEquals("Incorrect header", "name", this.responseHeader.getName());
		assertNull("Should be just the one header", this.responseHeader.next);
		assertNull("Should be no cookies", this.responseCookie);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("Incorrect entity", "ENTITY", content);
	}

	/**
	 * Ensure can handle {@link HttpException}.
	 */
	public void testHandleHttpEscalation() throws Throwable {

		// Handle HTTP escalation
		HttpException escalation = new HttpException(HttpStatus.NOT_FOUND,
				new WritableHttpHeader[] { new WritableHttpHeader("name", "value") }, "TEST");
		this.connection.getResponse().setEscalationHandler((context) -> {
			HttpException ex = (HttpException) context.getEscalation();
			context.getServerHttpConnection().getResponse().getEntityWriter()
					.write("{escalation: '" + ex.getEntity() + "'}");
			return true;
		});
		this.connection.getServiceFlowCallback().run(escalation);

		// Validate exception details
		assertEquals("Incorrect version", this.requestVersion, this.responseVersion);
		assertEquals("Incorrect status", HttpStatus.NOT_FOUND, this.status);
		assertEquals("Incorrect header", "name", this.responseHeader.getName());
		assertNull("Should be just the one header", this.responseHeader.next);
		assertNull("Should be no cookies", this.responseCookie);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("Incorrect entity", "{escalation: 'TEST'}", content);
	}

	/**
	 * Ensure reports {@link CleanupEscalation}.
	 */
	public void testSendCleanupEscalation() throws Throwable {

		// Create the connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("DELAY");

		// Write a response (should be reset)
		HttpResponse response = connection.getResponse();
		response.getHeaders().addHeader("TEST", "HEADER");
		response.getCookies().setCookie("TEST", "COOKIE");
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
		assertNull("Should be no cookies", this.responseCookie);

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
	 * Ensure handles {@link CleanupEscalation}.
	 */
	public void testHandleCleanupEscalation() throws Throwable {

		// Create the connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("DELAY");

		// Write a response (should be reset)
		HttpResponse response = connection.getResponse();
		response.getHeaders().addHeader("TEST", "HEADER");
		response.getCookies().setCookie("TEST", "COOKIE");
		response.setContentType("text/html", Charset.forName("UTF-16"));
		response.getEntityWriter().write("Content to be reset");

		// Create the clean up escalations
		final SQLException connectionEscalation = new SQLException("TEST");
		CleanupEscalation[] cleanupEscalations = new CleanupEscalation[] { new CleanupEscalation() {

			@Override
			public Class<?> getObjectType() {
				return Connection.class;
			}

			@Override
			public Throwable getEscalation() {
				return connectionEscalation;
			}
		} };

		// Send the response
		response.setEscalationHandler((context) -> {
			CleanupException exception = (CleanupException) context.getEscalation();
			context.getServerHttpConnection().getResponse().getEntityWriter()
					.write("{escalation: '" + exception.getCleanupEscalations()[0].getEscalation().getMessage() + "'}");
			return true;
		});
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
		assertNull("Should be no cookies", this.responseCookie);

		// Ensure correct content
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("Incorrect content", "{escalation: 'TEST'}", content);
	}

	/**
	 * Ensure {@link Escalation} overrides {@link CleanupEscalation} instances in
	 * sending response.
	 */
	public void testEscalationOverridesCleanupEscalation() throws Throwable {

		// Escalation
		final Exception escalation = new Exception("OVERRIDE");

		// Create the connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("DELAY");

		// Write a response (should be reset)
		HttpResponse response = connection.getResponse();
		response.getHeaders().addHeader("TEST", "HEADER");
		response.getCookies().setCookie("TEST", "COOKIE");
		response.setContentType("text/html", Charset.forName("UTF-16"));
		response.getEntityWriter().write("Content to be reset");

		// Create the clean up escalations
		final SQLException connectionEscalation = new SQLException("TEST");
		CleanupEscalation[] cleanupEscalations = new CleanupEscalation[] { new CleanupEscalation() {

			@Override
			public Class<?> getObjectType() {
				return Connection.class;
			}

			@Override
			public Throwable getEscalation() {
				return connectionEscalation;
			}
		} };

		// Send the response (should delay to allow reset)
		response.send();

		// Load the clean up escalations
		ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler().handleCleanupEscalations(connection,
				cleanupEscalations);

		// Flush the response with overriding escalation
		connection.getServiceFlowCallback().run(escalation);

		// Ensure correct response
		assertSame("Incorrect status", HttpStatus.INTERNAL_SERVER_ERROR, this.status);
		assertSame("Incorrect version", this.requestVersion, this.responseVersion);
		assertNull("Should be no headers", this.responseHeader);
		assertNull("Should be no cookies", this.responseCookie);

		// Create the expected response
		StringWriter expected = new StringWriter();
		PrintWriter writer = new PrintWriter(expected);
		escalation.printStackTrace(writer);
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
		this.requestHeaders.addHttpHeader("cookie", "not=serialised");
		ServerHttpConnection connection = this.createServerHttpConnection("SERIALISE");
		assertServerHttpConnection(connection, HttpMethod.POST, "/serialise", HttpVersion.HTTP_1_0,
				new String[] { "serialise", "serialised", "cookie", "not=serialised" }, "SERIALISE", HttpMethod.POST,
				new String[] { "serialise", "serialised", "cookie", "not=serialised" },
				new String[] { "not", "serialised" });

		// Serialise out the connection state
		Serializable momento = connection.exportState();

		// Reset the connection details (for new connection)
		this.method = HttpMethod.GET;
		this.requestUri = "/";
		this.requestVersion = HttpVersion.HTTP_1_1;
		this.requestHeaders = new MockNonMaterialisedHttpHeaders();
		this.requestHeaders.addHttpHeader("input", "header");
		this.requestHeaders.addHttpHeader("cookie", "client=cookie");
		ServerHttpConnection newConnection = this.createServerHttpConnection("TEST");
		assertServerHttpConnection(newConnection, HttpMethod.GET, "/", HttpVersion.HTTP_1_1,
				new String[] { "input", "header", "cookie", "client=cookie" }, "TEST", HttpMethod.GET,
				new String[] { "input", "header", "cookie", "client=cookie" }, new String[] { "client", "cookie" });

		// Ensure can import state (maintains version)
		newConnection.importState(momento);
		assertServerHttpConnection(newConnection, HttpMethod.POST, "/serialise", HttpVersion.HTTP_1_1,
				new String[] { "serialise", "serialised", "cookie", "not=serialised" }, "SERIALISE", HttpMethod.GET,
				new String[] { "input", "header", "cookie", "client=cookie" }, new String[] { "client", "cookie" });
	}

	/**
	 * Asserts the content of the {@link HttpRequest}.
	 * 
	 * @param connection                  {@link ServerHttpConnection} containing
	 *                                    the {@link HttpRequest}.
	 * @param requestMethod               Expected {@link HttpRequest}
	 *                                    {@link HttpMethod}.
	 * @param requestUri                  Expected {@link HttpRequest} URI.
	 * @param requestVersion              Expected {@link HttpRequest}
	 *                                    {@link HttpVersion}.
	 * @param requestHeaderNameValuePairs Expected {@link HttpRequest}
	 *                                    {@link HttpRequestHeaders} name/value
	 *                                    pairs.
	 * @param enityContent                Expected {@link HttpRequest} entity
	 *                                    content.
	 * @param clientMethod                Expected client {@link HttpMethod}.
	 * @param clientHeaderNameValuePairs  Expected client
	 *                                    {@link HttpRequestHeaders}.
	 * @param clientCookieNameValuePairs  Expected client
	 *                                    {@link HttpRequestCookies}.
	 */
	private void assertServerHttpConnection(ServerHttpConnection connection, HttpMethod requestMethod,
			String requestUri, HttpVersion requestVersion, String[] requestHeaderNameValuePairs, String enityContent,
			HttpMethod clientMethod, String[] clientHeaderNameValuePairs, String[] clientCookieNameValuePairs)
			throws IOException {

		// Validate the client details
		HttpRequest clientRequest = connection.getClientRequest();
		assertEquals("Incorrect client method", this.method, clientRequest.getMethod());
		assertEquals("Incorrect number of client headers", clientHeaderNameValuePairs.length / 2,
				clientRequest.getHeaders().length());
		for (int i = 0; i < clientHeaderNameValuePairs.length; i += 2) {
			String name = clientHeaderNameValuePairs[i];
			String value = clientHeaderNameValuePairs[i + 1];
			HttpHeader header = clientRequest.getHeaders().headerAt(i / 2);
			assertEquals("Incorrect client header name", name, header.getName());
			assertEquals("Incorrect client header value", value, header.getValue());
		}
		assertEquals("Incorrect number of client cookeis", clientCookieNameValuePairs.length / 2,
				clientRequest.getCookies().length());
		for (int i = 0; i < clientCookieNameValuePairs.length; i += 2) {
			String name = clientCookieNameValuePairs[i];
			String value = clientCookieNameValuePairs[i + 1];
			HttpRequestCookie cookie = clientRequest.getCookies().cookieAt(i / 2);
			assertEquals("Incorrect client cooke name", name, cookie.getName());
			assertEquals("Incorrect client cooke value", value, cookie.getValue());
		}

		// Validate the request
		HttpRequest request = connection.getRequest();
		assertEquals("Incorrect request method", requestMethod, request.getMethod());
		assertEquals("Incorrect request URI", requestUri, request.getUri());
		assertEquals("Incorrect request version", requestVersion, request.getVersion());

		// Validate the request headers
		assertEquals("Incorrect number of request headers", requestHeaderNameValuePairs.length / 2,
				request.getHeaders().length());
		for (int i = 0; i < requestHeaderNameValuePairs.length; i += 2) {
			String name = requestHeaderNameValuePairs[i];
			String value = requestHeaderNameValuePairs[i + 1];
			HttpHeader header = request.getHeaders().headerAt(i / 2);
			assertEquals("Incorrect request header name", name, header.getName());
			assertEquals("Incorrect request header value", value, header.getValue());
		}

		/*
		 * Validate the request cookies are always the client cookies. Main reason is
		 * for security, in avoiding high jacking request with serialised cookies and
		 * gaining access to session token.
		 */
		assertEquals("Incorrect number of request cookeis", clientCookieNameValuePairs.length / 2,
				request.getCookies().length());
		for (int i = 0; i < clientCookieNameValuePairs.length; i += 2) {
			String name = clientCookieNameValuePairs[i];
			String value = clientCookieNameValuePairs[i + 1];
			HttpRequestCookie cookie = request.getCookies().cookieAt(i / 2);
			assertEquals("Incorrect request cooke name", name, cookie.getName());
			assertEquals("Incorrect request cooke value", value, cookie.getValue());
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

	private WritableHttpCookie responseCookie = null;

	private long contentLength;

	private HttpHeaderValue contentType = null;

	private StreamBuffer<ByteBuffer> contentHeadStreamBuffer = null;

	@Override
	public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader httpHeader,
			WritableHttpCookie httpCookie, long contentLength, HttpHeaderValue contentType,
			StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {
		this.responseVersion = version;
		this.status = status;
		this.responseHeader = httpHeader;
		this.responseCookie = httpCookie;
		this.contentLength = contentLength;
		this.contentType = contentType;
		this.contentHeadStreamBuffer = contentHeadStreamBuffer;
	}

}
