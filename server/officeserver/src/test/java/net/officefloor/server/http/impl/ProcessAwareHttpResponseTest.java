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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import net.officefloor.frame.api.escalate.AsynchronousFlowTimedOutEscalation;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.test.Closure;
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseCookie;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.mock.MockManagedObjectContext;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link ProcessAwareHttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponseTest implements HttpResponseWriter<ByteBuffer> {

	/**
	 * {@link MockStreamBufferPool}.
	 */
	private final MockStreamBufferPool bufferPool = new MockStreamBufferPool();

	/**
	 * {@link ProcessAwareServerHttpConnectionManagedObject}.
	 */
	private final ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
			new HttpServerLocationImpl(), false, () -> HttpMethod.GET, () -> "/", HttpVersion.HTTP_1_1, null, null,
			null, null, true, this, this.bufferPool) {
		@Override
		public HttpResponse getResponse() {
			return ProcessAwareHttpResponseTest.this.response;
		}
	};

	/**
	 * {@link ProcessAwareHttpResponse} to test.
	 */
	private final ProcessAwareHttpResponse<ByteBuffer> response = new ProcessAwareHttpResponse<ByteBuffer>(
			this.connection, HttpVersion.HTTP_1_1, new MockManagedObjectContext());

	/**
	 * Ensure correct defaults on writing.
	 */
	@Test
	public void writeDefaults() throws IOException {

		// Write the response
		this.response.flushResponseToHttpResponseWriter(null);

		// Ensure correct defaults
		assertEquals(HttpVersion.HTTP_1_1, this.version, "Incorrect version");
		assertEquals(HttpStatus.NO_CONTENT, this.status, "Incorrect status");
		assertNull(this.httpHeader, "Should be no headers");
		assertNull(this.httpCookie, "Should be no cookeis");
		assertNull(this.contentHeadStreamBuffer, "Should be no entity data");
	}

	/**
	 * Ensure can not provide <code>null</code> values.
	 */
	@Test
	public void disallowNulls() throws IOException {

		Consumer<Runnable> checkNull = (run) -> {
			try {
				run.run();
				fail("Should not be able to set null value");
			} catch (IllegalArgumentException ex) {
				// Correct as null
			}
		};

		// Ensure disallow null values
		checkNull.accept(() -> this.response.setStatus(null));
		checkNull.accept(() -> this.response.setVersion(null));
	}

	/**
	 * Test mutating the {@link HttpResponse}.
	 */
	@Test
	public void changeStatus() throws IOException {

		// Validate mutating status
		assertEquals(HttpStatus.OK, this.response.getStatus(), "Incorrect default status");
		this.response.setStatus(HttpStatus.CONTINUE);
		assertEquals(HttpStatus.CONTINUE, this.response.getStatus(), "Should have status changed");

		// Ensure writes the changes status
		this.response.flushResponseToHttpResponseWriter(null);
		assertEquals(HttpStatus.CONTINUE, this.status, "Should have changed status");
	}

	/**
	 * Test mutating the {@link HttpVersion}.
	 */
	@Test
	public void changeVersion() throws IOException {

		// Validate mutating version
		assertEquals(HttpVersion.HTTP_1_1, this.response.getVersion(), "Incorrect default version");
		this.response.setVersion(HttpVersion.HTTP_1_0);
		assertEquals(HttpVersion.HTTP_1_0, this.response.getVersion(), "Should have version changed");

		// Ensure writes the changed version
		this.response.flushResponseToHttpResponseWriter(null);
		assertEquals(HttpVersion.HTTP_1_0, this.version, "Should have changed version");
	}

	/**
	 * Add a {@link HttpHeader}.
	 */
	@Test
	public void addHeader() throws IOException {

		// Add header
		this.response.getHeaders().addHeader("test", "value");

		// Ensure writes have HTTP header
		this.response.flushResponseToHttpResponseWriter(null);
		assertNotNull(this.httpHeader, "Should have header");
		assertEquals("test", this.httpHeader.getName(), "Incorrect header name");
		assertEquals("value", this.httpHeader.getValue(), "Incorrect header value");
		assertNull(this.httpHeader.next, "Should be no further headers");
	}

	/**
	 * Set a {@link HttpResponseCookie}.
	 */
	@Test
	public void setCookie() throws IOException {

		// Set the cookie
		HttpResponseCookie initial = this.response.getCookies().setCookie("test", "initial");
		HttpResponseCookie value = this.response.getCookies().setCookie("test", "value");
		assertSame(initial, value, "Should be same cookie as same name");

		// Ensure writes have HTTP header
		this.response.flushResponseToHttpResponseWriter(null);
		assertNotNull(this.httpCookie, "Should have cookie");
		assertEquals("test", this.httpCookie.getName(), "Incorrect cookie name");
		assertEquals("value", this.httpCookie.getValue(), "Incorrect cookie value");
		assertNull(this.httpCookie.next, "Should be no further cookies");
	}

	/**
	 * Ensure no HTTP entity content.
	 */
	@Test
	public void emptyContent() throws IOException {

		// Send without content
		this.response.flushResponseToHttpResponseWriter(null);

		// Ensure no content
		assertEquals(0, this.contentLength, "Should be no content");
		assertNull(this.contentType, "No content-type, as no content");
		assertNull(this.contentHeadStreamBuffer, "No content");
	}

	/**
	 * Outputs the HTTP entity content.
	 */
	@Test
	public void outputEntityContent() throws IOException {

		// Write some content
		ServerOutputStream output = this.response.getEntity();
		output.write(1);

		// Ensure same output stream provided
		assertSame(output, this.response.getEntity(), "Should obtain same output stream");

		// Send the response
		this.response.flushResponseToHttpResponseWriter(null);

		// Ensure correct content details
		assertEquals(1, this.contentLength, "Incorrect Content-Length");
		assertEquals("application/octet-stream", this.contentType.getValue(), "Incorrect Content-Type");

		// Ensure correct content
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		InputStream input = MockStreamBufferPool.createInputStream(this.contentHeadStreamBuffer);
		assertEquals(1, input.read(), "Should be the written byte");
		assertEquals(-1, input.read(), "Should be no further content");
	}

	/**
	 * Ensure default the <code>Content-Type</code> appropriately.
	 */
	@Test
	public void defaultContentTypes() throws IOException {

		// Ensure correct default
		assertEquals("application/octet-stream", this.response.getContentType(), "Incorrect default Content-Type");

		// Obtain writer (so becomes text output)
		this.response.getEntityWriter();
		assertEquals("text/plain", this.response.getContentType(), "Incorrect text default Content-Type");
	}

	/**
	 * Ensure derive the <code>Content-Type</code> appropriately.
	 */
	@Test
	public void deriveContentType() throws IOException {

		// Ensure keep defaulting charset
		this.response.setContentType("text/html", null);
		assertEquals("text/html", this.response.getContentType());
		assertEquals(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET, this.response.getContentCharset());

		// Change charset
		Charset charset = Charset.forName("UTF-16");
		this.response.setContentType("application/json", charset);
		assertEquals("application/json; charset=UTF-16", this.response.getContentType());
		assertEquals(charset, this.response.getContentCharset());

		// Ensure specify value
		this.response.setContentType(new HttpHeaderValue("application/xml"), charset);
		assertEquals("application/xml", this.response.getContentType());
		assertEquals(charset, this.response.getContentCharset());
	}

	/**
	 * Writes the HTTP entity content.
	 */
	@Test
	public void writeEntityContent() throws IOException {

		// Write some content
		ServerWriter writer = this.response.getEntityWriter();
		writer.write("TEST");

		// Should not be able to change the charset (as committed with writer)
		Charset charset = Charset.forName("UTF-16");
		assertNotEquals(charset, this.response.getContentCharset(), "Should not be UTF-16");
		try {
			this.response.setContentType("text/html", charset);
			fail("Should not be able to change the charset");
		} catch (IOException ex) {
			assertEquals("Can not change Content-Type. Committed to writing text/plain (charset UTF-8)",
					ex.getMessage());
		}

		// Ensure same writer provided
		assertSame(writer, this.response.getEntityWriter(), "Should obtain same writer");

		// Send the response
		this.response.flushResponseToHttpResponseWriter(null);

		// Ensure correct content details
		assertEquals("TEST".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET).length, this.contentLength,
				"Incorrect Content-Length");
		assertEquals("text/plain", this.contentType.getValue(), "Incorrect Content-Type");

		// Ensure correct content
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("TEST", content, "Incorrect sent content");
	}

	/**
	 * Ensure able to write the HTTP entity with different {@link Charset}.
	 */
	@Test
	public void writeEntityInUTF16() throws IOException {

		// Ensure different charset
		Charset charset = Charset.forName("UTF-16");
		assertNotEquals(charset, this.response.getContentCharset(), "Should not be UTF-16");

		// Specify the charset
		this.response.setContentType((String) null, charset);

		// Write some content
		ServerWriter writer = this.response.getEntityWriter();
		writer.write("TEST");

		// Send the response
		this.response.flushResponseToHttpResponseWriter(null);

		// Ensure correct content details
		assertEquals("TEST".getBytes(charset).length, this.contentLength, "Incorrect Content-Length");
		assertEquals("text/plain; charset=" + charset.name(), this.contentType.getValue(), "Incorrect Content-Type");

		// Ensure correct content
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer, charset);
		assertEquals("TEST", content, "Incorrect sent content");
	}

	/**
	 * Ensure sends the {@link HttpResponse} on closing the
	 * {@link ServerOutputStream}.
	 */
	@Test
	public void sendOnCloseServerOutputStream() throws IOException {

		// Close the output stream
		this.response.getEntity().close();

		// Ensure response sent
		assertTrue(this.response.isClosed(), "Should be sent");
	}

	/**
	 * Ensure sends the {@link HttpResponse} on closing the {@link ServerWriter}.
	 */
	@Test
	public void sendOnCloseServerWriter() throws IOException {

		// Close the server writer
		this.response.getEntityWriter().close();

		// Ensure response sent
		assertTrue(this.response.isClosed(), "Should be sent");
	}

	/**
	 * Ensure send only once. Repeated sends will not do anything.
	 */
	@Test
	public void sendOnlyOnce() throws IOException {

		// Set altered status (to check does not change on sending again)
		this.response.setStatus(HttpStatus.BAD_REQUEST);

		// Obtain the entity (must be before close, otherwise exception)
		ServerOutputStream entity = this.response.getEntity();
		ServerWriter entityWriter = this.response.getEntityWriter();

		// Send
		this.response.flushResponseToHttpResponseWriter(null);
		assertTrue(this.response.isClosed(), "Should be sent");

		// Change response and send (but now should not send)
		response.setStatus(HttpStatus.OK);
		this.response.flushResponseToHttpResponseWriter(null);
		assertEquals(HttpStatus.BAD_REQUEST, this.status, "Should not re-send");

		// Ensure close output stream does not re-send
		entity.close();
		assertEquals(HttpStatus.BAD_REQUEST, this.status, "Should not re-send");

		// Ensure close writer does not re-send
		entityWriter.close();
		assertEquals(HttpStatus.BAD_REQUEST, this.status, "Should not re-send");
	}

	/**
	 * Ensure reset the {@link HttpResponse}.
	 */
	@Test
	public void reset() throws IOException {

		// Ensure different charset
		Charset charset = Charset.forName("UTF-16");
		assertNotEquals(charset, this.response.getContentCharset(), "Should not be UTF-16");

		// Mutate the response
		this.response.setStatus(HttpStatus.CONTINUE);
		this.response.setVersion(HttpVersion.HTTP_1_0);
		this.response.setContentType("text/html", charset);
		this.response.getHeaders().addHeader("name", "value");
		this.response.getCookies().setCookie("name", "value");
		ServerWriter writer = this.response.getEntityWriter();
		writer.write("TEST");
		writer.flush();

		// Ensure response mutated
		assertEquals(HttpStatus.CONTINUE, this.response.getStatus(), "Incorrect mutated status");
		assertEquals(HttpVersion.HTTP_1_0, this.response.getVersion(), "Incorrect mutated version");
		assertEquals("value", this.response.getHeaders().getHeader("name").getValue(), "Incorrect mutated heders");
		assertEquals("value", this.response.getCookies().getCookie("name").getValue(), "Incorrect mutated cookies");
		assertTrue(this.bufferPool.isActiveBuffers(), "Should be using buffers for entity content");

		// Reset
		this.response.reset();

		// Ensure response reset
		assertEquals(HttpStatus.OK, this.response.getStatus(), "Inccorect reset status");
		assertEquals(HttpVersion.HTTP_1_1, this.response.getVersion(), "Incorrect reset version");
		assertFalse(this.response.getHeaders().iterator().hasNext(), "Should clear headers");
		assertFalse(this.response.getCookies().iterator().hasNext(), "Should clear cookies");
		assertFalse(this.bufferPool.isActiveBuffers(), "No entity, as should release all buffers");

		// Ensure able to continue forming response (typically an error)
		this.response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		this.response.setVersion(HttpVersion.HTTP_1_0);
		this.response.getHeaders().addHeader("error", "occurred");
		this.response.getCookies().setCookie("store", "failure");
		this.response.getEntityWriter().write("ERROR: something");

		// Send response (to ensure reset entity)
		this.response.flushResponseToHttpResponseWriter(null);

		// Ensure correct details sent
		assertEquals(HttpVersion.HTTP_1_0, this.version, "Incorrect version");
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Incorrect status");
		assertNotNull(this.httpHeader, "Should be a header");
		assertEquals("error", this.httpHeader.getName(), "Incorrect header name");
		assertEquals("occurred", this.httpHeader.getValue(), "incorrect header value");
		assertNull(this.httpHeader.next, "Should only be one header");
		assertNotNull(this.httpCookie, "Should be a cookie");
		assertEquals("store", this.httpCookie.getName(), "Incorrect cookie name");
		assertEquals("failure", this.httpCookie.getValue(), "Incorrect cookie value");
		assertNull(this.httpCookie.next, "Should only be one cookie");

		// Ensure only entity content after the reset is sent
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		String content = MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		assertEquals("ERROR: something", content, "Incorrect entity content since reset");

		// Ensure can not reset after sending
		try {
			this.response.reset();
			fail("Should not be sucessful");
		} catch (IOException ex) {
			assertEquals("Already committed to send response", ex.getMessage());
		}
	}

	/**
	 * Ensure can send {@link Throwable}.
	 */
	@Test
	public void sendFailure() throws IOException {

		// Send escalation
		final Exception escalation = new Exception("TEST");
		this.response.flushResponseToHttpResponseWriter(escalation);

		// Obtain the escalation content
		String expected = getStackTrace(escalation);

		// Ensure correct escalation
		assertEquals(HttpVersion.HTTP_1_1, this.version, "Incorrect version");
		assertSame(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Incorrect status");
		assertNull(this.httpHeader, "Should be no headers");
		assertNull(this.httpCookie, "Should be no cookies");
		assertEquals(expected.length(), this.contentLength, "Incorrect content-length");
		assertEquals("text/plain", this.contentType.getValue(), "Incorrect content-type");
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		assertEquals(expected, MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET), "Incorrect content");
	}

	/**
	 * Ensure can send {@link Escalation}.
	 */
	@Test
	public void sendEscalation() throws IOException {

		// Send escalation
		final AsynchronousFlowTimedOutEscalation escalation = new AsynchronousFlowTimedOutEscalation("timed out");
		this.response.flushResponseToHttpResponseWriter(escalation);

		// Obtain the escalation content
		String expected = getStackTrace(escalation);

		// Ensure correct escalation
		assertEquals(HttpVersion.HTTP_1_1, this.version, "Incorrect version");
		assertSame(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Incorrect status");
		assertNull(this.httpHeader, "Should be no headers");
		assertNull(this.httpCookie, "Should be no cookies");
		assertEquals(expected.length(), this.contentLength, "Incorrect content-length");
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		assertEquals(expected, MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET), "Incorrect content");
	}

	/**
	 * Ensure can provide {@link HttpEscalationHandler}.
	 */
	@Test
	public void handleEscalation() throws IOException {

		final Exception escalation = new Exception("TEST");

		// Configure escalation handling
		Closure<Boolean> isInvoked = new Closure<>(false);
		this.response.setEscalationHandler((context) -> {
			assertSame(escalation, context.getEscalation(), "Incorrect escalation");
			isInvoked.value = true;
			this.response.setContentType("application/mock", null);
			this.response.getEntityWriter().write("{error: '" + escalation.getMessage() + "'}");
			return true; // handled
		});

		// Send escalation
		this.response.flushResponseToHttpResponseWriter(escalation);

		// Obtain the escalation content
		final String expected = "{error: 'TEST'}";

		// Ensure correct escalation
		assertEquals(HttpVersion.HTTP_1_1, this.version, "Incorrect version");
		assertSame(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Should default to internal status");
		assertNull(this.httpHeader, "Should be no headers");
		assertNull(this.httpCookie, "Should be no cookies");
		assertEquals("application/mock", this.contentType.getValue(), "Incorrect content-type");
		assertEquals(expected, MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET), "Incorrect content");
	}

	/**
	 * Ensure can handle failure from {@link HttpEscalationHandler}.
	 */
	@Test
	public void handleEscalationThrowsHttpException() throws IOException {

		// Configure escalation handling failing
		final HttpStatus httpStatus = HttpStatus.getHttpStatus(418);
		final HttpException httpException = new HttpException(httpStatus,
				new WritableHttpHeader[] { new WritableHttpHeader("name", "value") }, "content");
		this.response.setEscalationHandler((context) -> {

			// Write some response (that should be reset)
			HttpResponse response = context.getServerHttpConnection().getResponse();
			response.setStatus(HttpStatus.NOT_IMPLEMENTED);
			response.getHeaders().addHeader("reset", "header");
			response.getEntityWriter().write("reset");

			// Handler fails
			throw httpException;
		});

		// Send escalation
		this.response.flushResponseToHttpResponseWriter(new Exception("TEST"));

		// Ensure correct escalation
		assertEquals(HttpVersion.HTTP_1_1, this.version, "Incorrect version");
		assertSame(httpStatus, this.status, "Should default to internal status");
		assertEquals("name", this.httpHeader.getName(), "Incorrect header name");
		assertEquals("value", this.httpHeader.getValue(), "Incorrect header value");
		assertNull(this.httpHeader.next, "Should be no further headers");
		assertNull(this.httpCookie, "Should be no cookies");
		assertEquals("content", MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET), "Incorrect content");
	}

	/**
	 * Ensure can handle failure from {@link HttpEscalationHandler}.
	 */
	@Test
	public void handleEscalationThrowsRuntimeException() throws IOException {

		// Configure escalation handling failing
		final RuntimeException failure = new RuntimeException("TEST");
		this.response.setEscalationHandler((context) -> {
			// Handler fails
			throw failure;
		});

		// Send escalation
		this.response.flushResponseToHttpResponseWriter(new Exception("UNHANDLED"));

		// Obtain the escalation content
		final String expected = getStackTrace(failure);

		// Ensure correct escalation
		assertEquals(HttpVersion.HTTP_1_1, this.version, "Incorrect version");
		assertSame(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Should default to internal status");
		assertNull(this.httpHeader, "Should be no headers");
		assertNull(this.httpCookie, "Should be no cookies");
		assertEquals("text/plain", this.contentType.getValue(), "Incorrect content-type");
		assertEquals(expected, MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET), "Incorrect content");
	}

	/**
	 * Ensure can reset {@link HttpResponse} send {@link Escalation}.
	 */
	@Test
	public void resetForSendingEscalation() throws IOException {

		// Provide details and send response
		this.response.setVersion(HttpVersion.HTTP_1_0);
		this.response.setStatus(HttpStatus.NOT_FOUND);
		this.response.getHeaders().addHeader("TEST", "VALUE");
		this.response.getCookies().setCookie("TEST", "VALUE");
		this.response.getEntityWriter().write("TEST");

		// Send escalation
		final Exception escalation = new Exception("TEST");
		this.response.flushResponseToHttpResponseWriter(escalation);

		// Obtain the escalation content
		StringWriter content = new StringWriter();
		PrintWriter writer = new PrintWriter(content);
		escalation.printStackTrace(writer);
		writer.flush();
		String expected = content.toString();

		// Ensure correct escalation
		assertEquals(HttpVersion.HTTP_1_1, this.version, "Incorrect version");
		assertSame(HttpStatus.INTERNAL_SERVER_ERROR, this.status, "Incorrect status");
		assertNull(this.httpHeader, "Should be no headers");
		assertNull(this.httpCookie, "Should be no cookies");
		assertEquals(expected.length(), this.contentLength, "Incorrect content-length");
		assertEquals("text/plain", this.contentType.getValue(), "Incorrect content-type");
		MockStreamBufferPool.releaseStreamBuffers(this.contentHeadStreamBuffer);
		assertEquals(expected, MockStreamBufferPool.getContent(this.contentHeadStreamBuffer,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET), "Incorrect content");
	}

	/**
	 * Obtains the stack trace.
	 * 
	 * @param escalation {@link Throwable}.
	 * @return Stack trace.
	 */
	private static String getStackTrace(Throwable escalation) {
		StringWriter content = new StringWriter();
		PrintWriter writer = new PrintWriter(content);
		escalation.printStackTrace(writer);
		writer.flush();
		return content.toString();
	}

	/*
	 * ===================== HttpResponseWriter ============================
	 */

	private HttpVersion version = null;

	private HttpStatus status = null;

	private WritableHttpHeader httpHeader = null;

	private WritableHttpCookie httpCookie = null;

	private long contentLength;

	private HttpHeaderValue contentType = null;

	private StreamBuffer<ByteBuffer> contentHeadStreamBuffer = null;

	@Override
	public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader httpHeader,
			WritableHttpCookie httpCookie, long contentLength, HttpHeaderValue contentType,
			StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {
		this.version = version;
		this.status = status;
		this.httpHeader = httpHeader;
		this.httpCookie = httpCookie;
		this.contentLength = contentLength;
		this.contentType = contentType;
		this.contentHeadStreamBuffer = contentHeadStreamBuffer;
	}

}
