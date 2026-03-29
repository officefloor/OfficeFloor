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
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.CleanupException;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpExternalResponse;
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link ProcessAwareServerHttpConnectionManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareServerHttpConnectionManagerTest implements HttpResponseWriter<ByteBuffer> {

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
	@Test
	public void intialiseConnection() throws IOException {

		// Add a request header (and cookie)
		this.requestHeaders.addHttpHeader("header", "one");
		this.requestHeaders.addHttpHeader("cookie", "cookie=two");

		// Ensure correct connection information
		assertEquals(HttpMethod.GET, this.connection.getClientRequest().getMethod(), "Incorrect connection method");

		// Ensure correct request information
		HttpRequest request = this.connection.getRequest();
		assertEquals(HttpMethod.GET, request.getMethod(), "Incorrect request method");
		assertEquals("/", request.getUri(), "Incorrect request URI");
		assertEquals(this.requestVersion, request.getVersion(), "Incorrect request version");
		assertEquals(2, request.getHeaders().length(), "Incorrect number of request headers");
		assertEquals("one", request.getHeaders().getHeader("header").getValue(), "Incorrect request header");
		assertEquals(1, request.getCookies().length(), "Incorrect number of cookies");
		assertEquals("two", request.getCookies().getCookie("cookie").getValue(), "Incorrect cookie");

		// Ensure correct request entity content
		StringWriter requestContent = new StringWriter();
		Reader entityReader = new InputStreamReader(request.getEntity(),
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		for (int character = entityReader.read(); character != -1; character = entityReader.read()) {
			requestContent.write(character);
		}
		assertEquals("TEST", requestContent.toString(), "Incorrect request content");
	}

	/**
	 * Ensure can send default response.
	 */
	@Test
	public void defaultSend() throws Throwable {

		// Send
		this.connection.getServiceFlowCallback().run(null);

		// Ensure not external send
		assertFalse(this.isExternal, "Should write response");

		// Validate the default details
		assertEquals(this.requestVersion, this.responseVersion, "Incorrect version");
		assertEquals(HttpStatus.NO_CONTENT, this.status, "Incorrect status");
		assertNull(this.responseHeader, "Should be no headers");
		assertNull(this.responseCookie, "Should be no cookies");
		assertEquals(0, this.contentLength, "Should be no entity");
		assertNull(this.contentType, "No Content-Type for no entity");
		assertNull(this.contentHeadStreamBuffer, "No entity content");
	}

	/**
	 * Ensure external send.
	 */
	@Test
	public void externalSend() throws Throwable {

		// Obtain the external response
		HttpExternalResponseManagedObjectSource mos = new ManagedObjectSourceStandAlone().loadManagedObjectSource(HttpExternalResponseManagedObjectSource.class);
		ManagedObjectUserStandAlone standAlone = new ManagedObjectUserStandAlone();
		standAlone.mapDependency(HttpExternalResponseManagedObjectSource.DependencyKeys.SERVER_HTTP_CONNECTION, this.connection);
		ManagedObject mo = standAlone.sourceManagedObject(mos);
		HttpExternalResponse externalResponse = (HttpExternalResponse) mo.getObject();

		// Flag external send
		externalResponse.externalSend();

		// Send
		this.connection.getServiceFlowCallback().run(null);

		// Ensure external send
		assertTrue(this.isExternal, "Should be external send");

		// Validate no response sent
		assertNull(this.responseVersion, "Should be no version");
		assertNull(this.status, "Should be no status");
		assertNull(this.responseHeader, "Should be no headers");
		assertNull(this.responseCookie, "Should be no cookies");
		assertEquals(0, this.contentLength, "Should be no entity");
		assertNull(this.contentType, "No Content-Type for no entity");
		assertNull(this.contentHeadStreamBuffer, "No entity content");
	}

	/**
	 * Ensure can send altered information.
	 */
	@Test
	public void alteredSend() throws Throwable {

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
		assertEquals(HttpVersion.HTTP_1_0, this.responseVersion, "Incorrect version");
		assertEquals(HttpStatus.CREATED, this.status, "Incorect status");
		assertNotNull(this.responseHeader, "Should be a header");
		assertNotNull(this.responseCookie, "Should be a cookie");
		assertEquals("name", this.responseHeader.getName(), "Incorrect header");
		assertNull(this.responseHeader.next, "Should be just the one header");

		// Validate the content details
		assertEquals(expectedContent.length, this.contentLength, "Incorrect content length");
		assertEquals("text/html; charset=" + charset.name(), this.contentType.getValue(), "Incorrect content type");

		// Validate the content
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer, charset);
		assertEquals("TEST RESPONSE", content, "Incorrect content");
	}

	/**
	 * Ensure can send Server name.
	 */
	@Test
	public void serverHttpHeader() throws Throwable {

		// Create with server name
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("TEST",
				"OfficeFloorServer", null);
		connection.getServiceFlowCallback().run(null);

		// Ensure server HTTP header
		assertNotNull(this.responseHeader, "Should have HTTP header");
		assertEquals("server", this.responseHeader.getName(), "Incorrect Server HTTP header");
		assertEquals("OfficeFloorServer", this.responseHeader.getValue(), "Incorrect Server value");
		assertNull(this.responseHeader.next, "Should just be Server header");
	}

	/**
	 * Ensure can send Date.
	 */
	@Test
	public void dateHttpHeader() throws Throwable {

		// Create with server name
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("TEST",
				null, () -> new HttpHeaderValue("<date>"));
		connection.getServiceFlowCallback().run(null);

		// Ensure date HTTP header
		assertNotNull(this.responseHeader, "Should have HTTP header");
		assertEquals("date", this.responseHeader.getName(), "Incorrect Date HTTP header");
		assertEquals("<date>", this.responseHeader.getValue(), "Incorrect Date value");
		assertNull(this.responseHeader.next, "Should just be Date header");
	}

	/**
	 * Ensure able to send Server, Date and Custom {@link HttpHeader} without issue.
	 */
	@Test
	public void serverDateCustomHttpHeaders() throws Throwable {

		// Create with server name
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("TEST",
				"OfficeFloorServer", () -> new HttpHeaderValue("<date>"));
		HttpResponseHeaders responseHeaders = connection.getResponse().getHeaders();
		responseHeaders.addHeader("Custom", "test");
		responseHeaders.addHeader(new HttpHeaderName("MaintainCase", true), "Case Sensitive");

		// Send response
		connection.getServiceFlowCallback().run(null);

		// Ensure date HTTP header
		assertNotNull(this.responseHeader, "Should have HTTP header");

		// Validate server
		assertEquals("server", this.responseHeader.getName(), "Incorrect Server HTTP header");
		assertEquals("OfficeFloorServer", this.responseHeader.getValue(), "Incorrect Server value");

		// Validate date
		this.responseHeader = this.responseHeader.next;
		assertEquals("date", this.responseHeader.getName(), "Incorrect Date HTTP header");
		assertEquals("<date>", this.responseHeader.getValue(), "Incorrect Date value");

		// Validate custom
		this.responseHeader = this.responseHeader.next;
		assertEquals("custom", this.responseHeader.getName(), "Incorrect Custom HTTP header");
		assertEquals("test", this.responseHeader.getValue(), "Incorrect Custom value");

		// Ensure can maintain case (clients may not not be case insensitive)
		this.responseHeader = this.responseHeader.next;
		assertEquals("MaintainCase", this.responseHeader.getName(), "Incorrect maintain case HTTP header");
		assertEquals("Case Sensitive", this.responseHeader.getValue(), "Incorrect maintain case value");

		// Ensure no further headers
		this.responseHeader = this.responseHeader.next;
		assertNull(this.responseHeader, "Should be no further headers");
	}

	/**
	 * Ensure can flush {@link HttpResponse} without a send.
	 */
	@Test
	public void flushResponseWithoutSend() throws Throwable {

		// Create the connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = this.createServerHttpConnection("DELAY");
		assertNull(this.status, "Should not send on creation");

		// Flush response
		connection.getServiceFlowCallback().run(null);
		assertSame(HttpStatus.NO_CONTENT, this.status, "Should now write response");

		// Ensure only flushes once
		this.status = null;
		connection.getServiceFlowCallback().run(null);
		assertNull(this.status, "Should not write response again on flush");
	}

	/**
	 * Ensure handles {@link Exception}.
	 */
	@Test
	public void exception() throws Throwable {

		// Handle exception
		final Exception exception = new Exception("TEST");
		this.connection.getServiceFlowCallback().run(exception);

		// Validate exception details
		assertEquals(this.requestVersion, this.responseVersion, "Incorrect version");
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Incorrect status");
		assertNull(this.responseHeader, "Should be no headers");
		assertNull(this.responseCookie, "Should be no cookies");

		// Verify send stack trace
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		StringWriter expected = new StringWriter();
		PrintWriter writer = new PrintWriter(expected);
		exception.printStackTrace(writer);
		writer.flush();
		assertEquals(expected.toString(), content, "Incorrect entity");
	}

	/**
	 * Ensure can handle {@link Exception}.
	 */
	@Test
	public void handleEscalation() throws Throwable {

		// Handle escalation
		final Exception escalation = new Exception("TEST");
		this.connection.getResponse().setEscalationHandler((context) -> {
			context.getServerHttpConnection().getResponse().getEntityWriter()
					.write("{escalation: '" + context.getEscalation().getMessage() + "'}");
			return true;
		});
		this.connection.getServiceFlowCallback().run(escalation);

		// Validate escalation details
		assertEquals(this.requestVersion, this.responseVersion, "Incorrect version");
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Incorrect status");
		assertNull(this.responseHeader, "Should be no headers");
		assertNull(this.responseCookie, "Should be no cookies");
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("{escalation: 'TEST'}", content, "Incorrect entity");
	}

	/**
	 * Ensure handles {@link HttpException}.
	 */
	@Test
	public void httpEscalation() throws Throwable {

		// Handle HTTP escalation
		HttpException escalation = new HttpException(HttpStatus.NOT_FOUND,
				new WritableHttpHeader[] { new WritableHttpHeader("name", "value") }, "ENTITY");
		this.connection.getServiceFlowCallback().run(escalation);

		// Validate exception details
		assertEquals(this.requestVersion, this.responseVersion, "Incorrect version");
		assertEquals(HttpStatus.NOT_FOUND, this.status, "Incorrect status");
		assertEquals("name", this.responseHeader.getName(), "Incorrect header");
		assertNull(this.responseHeader.next, "Should be just the one header");
		assertNull(this.responseCookie, "Should be no cookies");
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("ENTITY", content, "Incorrect entity");
	}

	/**
	 * Ensure can handle {@link HttpException}.
	 */
	@Test
	public void handleHttpEscalation() throws Throwable {

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
		assertEquals(this.requestVersion, this.responseVersion, "Incorrect version");
		assertEquals(HttpStatus.NOT_FOUND, this.status, "Incorrect status");
		assertEquals("name", this.responseHeader.getName(), "Incorrect header");
		assertNull(this.responseHeader.next, "Should be just the one header");
		assertNull(this.responseCookie, "Should be no cookies");
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("{escalation: 'TEST'}", content, "Incorrect entity");
	}

	/**
	 * Ensure reports {@link CleanupEscalation}.
	 */
	@Test
	public void sendCleanupEscalation() throws Throwable {

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
        connection.clean(cleanupEscalations);

		// Flush the response
		connection.getServiceFlowCallback().run(null);

		// Ensure correct response
		assertSame(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Incorrect status");
		assertSame(this.requestVersion, this.responseVersion, "Incorrect version");
		assertNull(this.responseHeader, "Should be no headers");
		assertNull(this.responseCookie, "Should be no cookies");

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
		assertEquals("text/plain", this.contentType.getValue(), "Incorrect content-type value");
		assertEquals(this.contentLength, expectedContent.length(), "Incorrect content-length value");

		// Ensure correct content
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals(expectedContent, content, "Incorrect content");
	}

	/**
	 * Ensure handles {@link CleanupEscalation}.
	 */
	@Test
	public void handleCleanupEscalation() throws Throwable {

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
        connection.clean(cleanupEscalations);

		// Flush the response
		connection.getServiceFlowCallback().run(null);

		// Ensure correct response
		assertSame(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Incorrect status");
		assertSame(this.requestVersion, this.responseVersion, "Incorrect version");
		assertNull(this.responseHeader, "Should be no headers");
		assertNull(this.responseCookie, "Should be no cookies");

		// Ensure correct content
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("{escalation: 'TEST'}", content, "Incorrect content");
	}

	/**
	 * Ensure {@link Escalation} overrides {@link CleanupEscalation} instances in
	 * sending response.
	 */
	@Test
	public void escalationOverridesCleanupEscalation() throws Throwable {

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
        connection.clean(cleanupEscalations);

		// Flush the response with overriding escalation
		connection.getServiceFlowCallback().run(escalation);

		// Ensure correct response
		assertSame(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Incorrect status");
		assertSame(this.requestVersion, this.responseVersion, "Incorrect version");
		assertNull(this.responseHeader, "Should be no headers");
		assertNull(this.responseCookie, "Should be no cookies");

		// Create the expected response
		StringWriter expected = new StringWriter();
		PrintWriter writer = new PrintWriter(expected);
		escalation.printStackTrace(writer);
		writer.flush();
		String expectedContent = expected.toString();

		// Ensure correct content details
		assertEquals("text/plain", this.contentType.getValue(), "Incorrect content-type value");
		assertEquals(this.contentLength, expectedContent.length(), "Incorrect content-length value");

		// Ensure correct content
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals(expectedContent, content, "Incorrect content");
	}

	/**
	 * Ensure can serialise the {@link ServerHttpConnection} state.
	 */
	@Test
	public void serialiseState() throws IOException {

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
		assertEquals(this.method, clientRequest.getMethod(), "Incorrect client method");
		assertEquals(clientHeaderNameValuePairs.length / 2,
				clientRequest.getHeaders().length(), "Incorrect number of client headers");
		for (int i = 0; i < clientHeaderNameValuePairs.length; i += 2) {
			String name = clientHeaderNameValuePairs[i];
			String value = clientHeaderNameValuePairs[i + 1];
			HttpHeader header = clientRequest.getHeaders().headerAt(i / 2);
			assertEquals(name, header.getName(), "Incorrect client header name");
			assertEquals(value, header.getValue(), "Incorrect client header value");
		}
		assertEquals(clientCookieNameValuePairs.length / 2,
				clientRequest.getCookies().length(), "Incorrect number of client cookeis");
		for (int i = 0; i < clientCookieNameValuePairs.length; i += 2) {
			String name = clientCookieNameValuePairs[i];
			String value = clientCookieNameValuePairs[i + 1];
			HttpRequestCookie cookie = clientRequest.getCookies().cookieAt(i / 2);
			assertEquals(name, cookie.getName(), "Incorrect client cooke name");
			assertEquals(value, cookie.getValue(), "Incorrect client cooke value");
		}

		// Validate the request
		HttpRequest request = connection.getRequest();
		assertEquals(requestMethod, request.getMethod(), "Incorrect request method");
		assertEquals(requestUri, request.getUri(), "Incorrect request URI");
		assertEquals(requestVersion, request.getVersion(), "Incorrect request version");

		// Validate the request headers
		assertEquals(requestHeaderNameValuePairs.length / 2,
				request.getHeaders().length(), "Incorrect number of request headers");
		for (int i = 0; i < requestHeaderNameValuePairs.length; i += 2) {
			String name = requestHeaderNameValuePairs[i];
			String value = requestHeaderNameValuePairs[i + 1];
			HttpHeader header = request.getHeaders().headerAt(i / 2);
			assertEquals(name, header.getName(), "Incorrect request header name");
			assertEquals(value, header.getValue(), "Incorrect request header value");
		}

		/*
		 * Validate the request cookies are always the client cookies. Main reason is
		 * for security, in avoiding high jacking request with serialised cookies and
		 * gaining access to session token.
		 */
		assertEquals(clientCookieNameValuePairs.length / 2,
				request.getCookies().length(), "Incorrect number of request cookeis");
		for (int i = 0; i < clientCookieNameValuePairs.length; i += 2) {
			String name = clientCookieNameValuePairs[i];
			String value = clientCookieNameValuePairs[i + 1];
			HttpRequestCookie cookie = request.getCookies().cookieAt(i / 2);
			assertEquals(name, cookie.getName(), "Incorrect request cooke name");
			assertEquals(value, cookie.getValue(), "Incorrect request cooke value");
		}

		// Validate the entity content
		StringWriter actualContent = new StringWriter();
		InputStreamReader reader = new InputStreamReader(request.getEntity(),
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		for (int character = reader.read(); character != -1; character = reader.read()) {
			actualContent.write(character);
		}
		assertEquals(enityContent, actualContent.toString(), "Incorrect request entity");
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

	private boolean isExternal = false;

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

	@Override
	public void writeHttpExternalResponse() {
		this.isExternal = true;
	}
}
