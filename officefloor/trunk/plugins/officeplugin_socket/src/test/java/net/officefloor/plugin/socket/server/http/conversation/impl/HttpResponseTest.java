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
package net.officefloor.plugin.socket.server.http.conversation.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpEntity;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.socket.server.impl.ArrayWriteBuffer;
import net.officefloor.plugin.socket.server.impl.BufferWriteBuffer;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBufferEnum;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

/**
 * Tests the {@link HttpResponseImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseTest extends OfficeFrameTestCase implements Connection {

	/**
	 * Default {@link Charset} for testing.
	 */
	private static final Charset DEFAULT_CHARSET = UsAsciiUtil.US_ASCII;

	/**
	 * Contains the content written on the wire.
	 */
	private final ByteArrayOutputStream wire = new ByteArrayOutputStream();

	/**
	 * {@link HttpConversation} to create the {@link HttpResponse}.
	 */
	private HttpConversation conversation = this.createHttpConversation(false);

	/**
	 * Indicates if the {@link Connection} is closed.
	 */
	private boolean isConnectionClosed = false;

	/**
	 * Ensure can send a simple response.
	 */
	public void testSimpleResponse() throws IOException {
		HttpResponse response = this.createHttpResponse();
		ServerOutputStream entity = response.getEntity();
		entity.write("TEST".getBytes(DEFAULT_CHARSET));
		entity.close();
		this.assertWireContent("HTTP/1.1 200 OK\nContent-Length: 4", "TEST",
				DEFAULT_CHARSET);
		assertFalse("Connection should not be closed", this.isConnectionClosed);
	}

	/**
	 * Ensure appropriately provides header to cached {@link ByteBuffer}.
	 */
	public void testSendCachedByteBuffer() throws IOException {
		HttpResponse response = this.createHttpResponse();
		ServerOutputStream entity = response.getEntity();
		entity.write(ByteBuffer.wrap("TEST".getBytes(DEFAULT_CHARSET)));
		entity.close();
		this.assertWireContent("HTTP/1.1 200 OK\nContent-Length: 4", "TEST",
				DEFAULT_CHARSET);
		assertFalse("Connection should not be closed", this.isConnectionClosed);
	}

	/**
	 * Ensures provides correct response if no content.
	 */
	public void testNoContent() throws IOException {
		HttpResponse response = this.createHttpResponse();
		response.getEntity().close();
		this.assertWireContent("HTTP/1.1 204 No Content\nContent-Length: 0",
				null, null);
		assertFalse("Connection should not be closed", this.isConnectionClosed);
	}

	/**
	 * Ensure able to send <code>null</code> header value.
	 */
	public void testNullHeaderValue() throws IOException {
		HttpResponse response = this.createHttpResponse();
		response.addHeader("null", null);
		response.send();
		this.assertWireContent("HTTP/1.1 204 No Content\n" + "null: \n"
				+ "Content-Length: 0", null, null);
		assertFalse("Connection should not be closed", this.isConnectionClosed);
	}

	/**
	 * Tests manipulating the {@link HttpHeader} instances.
	 */
	public void testHeaderManipulation() {

		// Create the response
		HttpResponse response = this.createHttpResponse();

		// Ensure can add header
		HttpHeader headerOne = response.addHeader("test", "one");
		assertHttpHeader("test", "one", headerOne);

		// Add another header
		HttpHeader headerTwo = response.addHeader("test", "two");
		assertHttpHeader("test", "two", headerTwo);

		// Ensure get first header
		HttpHeader firstHeader = response.getHeader("test");
		assertHttpHeader("test", "one", firstHeader);

		// Ensure listing of headers returned is correct
		HttpHeader[] headers = response.getHeaders();
		assertEquals("Incorrect number of headers", 2, headers.length);
		assertHttpHeader("test", "one", headers[0]);
		assertHttpHeader("test", "two", headers[1]);

		// Remove header one
		response.removeHeader(headerOne);

		// Ensure find new first header
		HttpHeader newFirstHeader = response.getHeader("test");
		assertHttpHeader("test", "two", newFirstHeader);

		// Add further headers by name and remove ensuring no headers
		response.addHeader("test", "three");
		response.addHeader("test", "four");
		response.removeHeaders("test");
		assertNull("Should be no headers by name on removing",
				response.getHeader("test"));
	}

	/**
	 * Ensures provides parse failure details.
	 */
	public void testSendParseFailure() throws IOException {

		final Throwable FAILURE = new HttpRequestParseException(
				HttpStatus.SC_BAD_REQUEST, "Fail Parse Test");

		this.sendFailure(FAILURE);
		this.assertWireContent("HTTP/1.1 400 Bad Request\n"
				+ "Content-Type: text/html; charset=" + DEFAULT_CHARSET.name()
				+ "\nContent-Length: 42", FAILURE.getClass().getSimpleName()
				+ ": Fail Parse Test", DEFAULT_CHARSET);
		assertTrue("Connection should be closed", this.isConnectionClosed);
	}

	/**
	 * Ensures provides failure message.
	 */
	public void testSendServerFailure() throws IOException {

		final Throwable FAILURE = new Exception("Failure Test");

		this.sendFailure(FAILURE);
		this.assertWireContent("HTTP/1.1 500 Internal Server Error\n"
				+ "Content-Type: text/html; charset=" + DEFAULT_CHARSET.name()
				+ "\nContent-Length: 23", FAILURE.getClass().getSimpleName()
				+ ": Failure Test", DEFAULT_CHARSET);
		assertTrue("Connection should be closed", this.isConnectionClosed);
	}

	/**
	 * Ensures provides failure details should the exception have no message.
	 */
	public void testSendServerFailureWithNoMessage() throws IOException {

		final Throwable FAILURE = new Exception();

		this.sendFailure(FAILURE);
		this.assertWireContent("HTTP/1.1 500 Internal Server Error\n"
				+ "Content-Type: text/html; charset=" + DEFAULT_CHARSET.name()
				+ "\nContent-Length: 15", Exception.class.getSimpleName()
				+ ": null", DEFAULT_CHARSET);
		assertTrue("Connection should be closed", this.isConnectionClosed);
	}

	/**
	 * Ensures able to obtain stack trace for testing and debugging purposes.
	 */
	public void testSendServerFailureWithStackTrace() throws IOException {

		final Throwable FAILURE = new Exception("Test");

		// Obtain the expected stack trace
		StringWriter buffer = new StringWriter();
		FAILURE.printStackTrace(new PrintWriter(buffer));
		String stackTrace = buffer.toString();

		// Determine the expected content and its length
		String content = FAILURE.getClass().getSimpleName() + ": Test\n\n"
				+ stackTrace;
		int contentLength = content.getBytes(DEFAULT_CHARSET).length;

		// Create conversation to send stack trace on failure
		this.conversation = this.createHttpConversation(true);

		// Run test
		this.sendFailure(FAILURE);
		this.assertWireContent("HTTP/1.1 500 Internal Server Error\n"
				+ "Content-Type: text/html; charset=" + DEFAULT_CHARSET.name()
				+ "\nContent-Length: " + contentLength, content,
				DEFAULT_CHARSET);
		assertTrue("Connection should be closed", this.isConnectionClosed);
	}

	/**
	 * Ensures able to provide failure {@link HttpResponse} after having already
	 * written content to the {@link ServerOutputStream}.
	 */
	public void testSendFailureAfterEntityContent() throws IOException {

		// Write some content
		HttpResponse response = this.createHttpResponse();
		ServerOutputStream entity = response.getEntity();
		entity.write("TEST".getBytes());
		entity.flush();

		// Send failure
		final Throwable FAILURE = new Exception("Failure Test");
		((HttpResponseImpl) response).sendFailure(FAILURE);
		this.assertWireContent("HTTP/1.1 500 Internal Server Error\n"
				+ "Content-Type: text/html; charset=" + DEFAULT_CHARSET.name()
				+ "\nContent-Length: 23", FAILURE.getClass().getSimpleName()
				+ ": Failure Test", DEFAULT_CHARSET);
	}

	/**
	 * Ensures able to provide failure {@link HttpResponse} after having already
	 * written content to the {@link ServerWriter}.
	 */
	public void testSendFailureAfterEntityWriterContent() throws IOException {

		final Charset charset = Charset.forName("UTF-8");

		// Write some content
		HttpResponse response = this.createHttpResponse();
		response.setContentCharset(charset, charset.name());
		ServerWriter entityWriter = response.getEntityWriter();
		entityWriter.write("TEST");
		entityWriter.flush();

		// Send failure
		final Throwable FAILURE = new Exception("Failure Test");
		((HttpResponseImpl) response).sendFailure(FAILURE);
		this.assertWireContent("HTTP/1.1 500 Internal Server Error\n"
				+ "Content-Type: text/html; charset=UTF-8\n"
				+ "Content-Length: 23", FAILURE.getClass().getSimpleName()
				+ ": Failure Test", charset);
	}

	/**
	 * Ensure on obtaining the {@link ServerOutputStream} that can not obtain
	 * {@link ServerWriter}.
	 */
	public void testOnlyEntityOutputStream() throws IOException {
		HttpResponse response = this.createHttpResponse();
		assertNotNull("Should obtain server outputstream", response.getEntity());
		try {
			response.getEntityWriter();
			fail("Should not be successful");
		} catch (IOException ex) {
			assertEquals("Incorrect cause",
					"getEntity() has already been invoked", ex.getMessage());
		}
		assertNotNull(
				"Should continue to be able to obtain server outputstream",
				response.getEntity());

		// Ensure can obtain after reset
		response.reset();
		assertNotNull("Should be able to obtain writer after reset",
				response.getEntityWriter());
	}

	/**
	 * Ensure same {@link ServerOutputStream} on subsequent calls.
	 */
	public void testSameEntityOutputStream() throws IOException {
		HttpResponse response = this.createHttpResponse();
		ServerOutputStream entityOne = response.getEntity();
		ServerOutputStream entityTwo = response.getEntity();
		assertSame("Should be same entity output stream", entityOne, entityTwo);
	}

	/**
	 * Ensure on obtaining the {@link ServerWriter} that can not obtain
	 * {@link ServerOutputStream}.
	 */
	public void testOnlyEntityWriter() throws IOException {
		HttpResponse response = this.createHttpResponse();
		assertNotNull("Should obtain server writer", response.getEntityWriter());
		try {
			response.getEntity();
			fail("Should not be successful");
		} catch (IOException ex) {
			assertEquals("Incorrect cause",
					"getEntityWriter() has already been invoked",
					ex.getMessage());
		}
		assertNotNull("Should continue to be able to obtain server writer",
				response.getEntityWriter());

		// Ensure can obtain after reset
		response.reset();
		assertNotNull("Should be able to obtain outputstream after reset",
				response.getEntity());
	}

	/**
	 * Ensure can use {@link ServerWriter} for the entity.
	 */
	public void testEntityWriter() throws IOException {
		HttpResponse response = this.createHttpResponse();
		ServerWriter entity = response.getEntityWriter();
		entity.write("TEST");
		entity.close();
		this.assertWireContent("HTTP/1.1 200 OK\n"
				+ "Content-Type: text/html; charset=" + DEFAULT_CHARSET.name()
				+ "\nContent-Length: 4", "TEST", DEFAULT_CHARSET);
		assertFalse("Connection should not be closed", this.isConnectionClosed);
	}

	/**
	 * Ensure same {@link ServerWriter} for the entity on subsequent calls.
	 */
	public void testSameEntityWriter() throws IOException {
		HttpResponse response = this.createHttpResponse();
		ServerWriter entityOne = response.getEntityWriter();
		ServerWriter entityTwo = response.getEntityWriter();
		assertSame("Should be the same writer", entityOne, entityTwo);
	}

	/**
	 * Ensure can specify alternate Content-Type.
	 */
	public void testEntityWriterWithAlternateContentType() throws IOException {
		HttpResponse response = this.createHttpResponse();
		response.setContentType("text/plain");
		ServerWriter entity = response.getEntityWriter();
		entity.write("TEST");
		entity.close();
		this.assertWireContent("HTTP/1.1 200 OK\n"
				+ "Content-Type: text/plain; charset=" + DEFAULT_CHARSET.name()
				+ "\nContent-Length: 4", "TEST", DEFAULT_CHARSET);
		assertFalse("Connection should not be closed", this.isConnectionClosed);
	}

	/**
	 * Ensure can use {@link ServerWriter} with alternate {@link Charset} for
	 * the entity.
	 */
	public void testEntityWriterWithAlternateCharsets() throws IOException {

		Map<String, Charset> charsets = Charset.availableCharsets();
		for (Charset charset : charsets.values()) {

			// Indicate the charset being tested
			System.out.print("Charset: " + charset.name());

			// Determine if can use
			boolean canUse = true;
			try {
				charset.newEncoder();
			} catch (UnsupportedOperationException ex) {
				canUse = false; // can not use
			}
			System.out.println(canUse ? "" : " (no encoder)");

			// Test with the charset if can use
			if (canUse) {
				HttpResponse response = this.createHttpResponse();
				response.setContentCharset(charset, charset.name());
				ServerWriter entity = response.getEntityWriter();
				entity.write("TEST");
				entity.close();
				this.assertWireContent(
						"HTTP/1.1 200 OK\nContent-Type: text/html; charset="
								+ charset.name() + "\nContent-Length: "
								+ ("TEST".getBytes(charset).length), "TEST",
						charset);
				assertFalse("Connection should not be closed",
						this.isConnectionClosed);
			}
		}
	}

	/**
	 * Ensure can not change the {@link Charset} for the {@link ServerWriter}.
	 */
	public void testNotChangeContentCharsetForEntityWriter() throws IOException {

		// Obtain the entity writer
		HttpResponse response = this.createHttpResponse();
		response.getEntityWriter();

		// Should now not be able to change charset
		try {
			response.setContentCharset(UsAsciiUtil.US_ASCII, null);
			fail("Should not be able to change the charset");
		} catch (IOException ex) {
			assertEquals("Incorrect cause",
					"getEntityWriter() has already been invoked",
					ex.getMessage());
		}

		// Ensure can change after reset
		response.reset();
		response.setContentCharset(UsAsciiUtil.US_ASCII, null);
	}

	/**
	 * Ensure can reset the {@link HttpResponse}.
	 */
	public void testResetHttpResponse() throws IOException {

		// Obtain the entity writer
		HttpResponse response = this.createHttpResponse();

		// Provide details (to be reset)
		response.addHeader("RESET", "ME");
		ServerWriter writer = response.getEntityWriter();
		writer.write("TEST");
		writer.flush();

		// Reset the response
		response.reset();

		// Validate reset content
		response.send();
		this.assertWireContent(
				"HTTP/1.1 204 No Content\nContent-Type: text/html; charset=US-ASCII\nContent-Length: 0",
				null, null);
	}

	/*
	 * ===================== Helper methods ==============================
	 */

	/**
	 * Creates a {@link HttpConversation} for testing.
	 * 
	 * @param isSendStackTraceOnFailure
	 *            Flags whether to send the stack trace on failure.
	 */
	private HttpConversation createHttpConversation(
			boolean isSendStackTraceOnFailure) {
		return new HttpConversationImpl(this, 1024, DEFAULT_CHARSET,
				isSendStackTraceOnFailure);
	}

	/**
	 * Creates a {@link HttpResponse} to be tested.
	 * 
	 * @return New {@link HttpResponse}.
	 */
	private HttpResponse createHttpResponse() {

		// Add the request
		ServerInputStreamImpl content = new ServerInputStreamImpl(new Object());
		content.inputData(null, 0, 0, false);
		HttpEntity entity = new HttpEntityImpl(content);
		HttpManagedObject mo = this.conversation.addRequest("GET", "/mock",
				"HTTP/1.1", new LinkedList<HttpHeader>(), entity);

		// Return the http response from managed object
		return mo.getServerHttpConnection().getHttpResponse();
	}

	/**
	 * Sends the failure.
	 * 
	 * @param failure
	 *            Failure.
	 * @throws IOException
	 *             If fails to send failure.
	 */
	private void sendFailure(Throwable failure) throws IOException {
		HttpResponseImpl response = (HttpResponseImpl) this
				.createHttpResponse();
		response.sendFailure(failure);
	}

	/**
	 * Asserts the content on the wire is as expected.
	 * 
	 * @param expectedHeader
	 *            Expected header content on the wire.
	 * @param expectedEntity
	 *            Expected entity content on the wire.
	 * @param entityCharset
	 *            Entity {@link Charset}.
	 */
	private void assertWireContent(String expectedHeader,
			String expectedEntity, Charset entityCharset) {

		// Obtain the wire bytes
		byte[] wireBytes = this.wire.toByteArray();

		// Transform expected header to bytes
		expectedHeader = expectedHeader + "\n\n";
		byte[] expectedHeaderBytes = UsAsciiUtil.convertToHttp(expectedHeader);

		// Transform expected entity to bytes
		byte[] expectedEntityBytes = (expectedEntity == null ? new byte[0]
				: expectedEntity.getBytes(entityCharset));

		// Ensure correct number of bytes on wire
		assertEquals("Incorrect number of bytes on the wire:\nEXPECTED: "
				+ expectedHeader
				+ (expectedEntity == null ? "" : expectedEntity) + "\nACTUAL: "
				+ UsAsciiUtil.convertToString(wireBytes),
				(expectedHeaderBytes.length + expectedEntityBytes.length),
				wireBytes.length);

		// Obtain the actual header data
		byte[] actualHeaderBytes = new byte[expectedHeaderBytes.length];
		System.arraycopy(wireBytes, 0, actualHeaderBytes, 0,
				actualHeaderBytes.length);
		String actualHeader = UsAsciiUtil.convertToString(actualHeaderBytes);

		// Obtain the actual entity data
		byte[] actualEntityBytes = new byte[expectedEntityBytes.length];
		System.arraycopy(wireBytes, expectedHeaderBytes.length,
				actualEntityBytes, 0, actualEntityBytes.length);
		String actualEntity = new String(actualEntityBytes,
				entityCharset == null ? DEFAULT_CHARSET : entityCharset);

		// Validate header and entity content
		assertEquals("Incorrect response on the wire",
				UsAsciiUtil.convertToString(expectedHeaderBytes)
						+ (expectedEntity == null ? "" : new String(
								expectedEntityBytes, entityCharset)),
				actualHeader + actualEntity);

		// Clear the wire content
		this.wire.reset();
	}

	/**
	 * Asserts the {@link HttpHeader} is correct.
	 * 
	 * @param name
	 *            Expected name.
	 * @param value
	 *            Expected value.
	 * @param header
	 *            {@link HttpHeader} to be validate.
	 */
	private static void assertHttpHeader(String name, String value,
			HttpHeader header) {
		assertEquals("Incorrect header name", name, header.getName());
		assertEquals("Incorrect header value", value, header.getValue());
	}

	/*
	 * ========================= WriteBufferReceiver ===========================
	 */

	@Override
	public WriteBuffer createWriteBuffer(byte[] data, int length) {
		return new ArrayWriteBuffer(data, length);
	}

	@Override
	public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
		return new BufferWriteBuffer(buffer);
	}

	@Override
	public void writeData(WriteBuffer[] data) {
		// Write the data to the wire
		for (WriteBuffer buffer : data) {
			WriteBufferEnum type = buffer.getType();
			switch (type) {
			case BYTE_ARRAY:
				this.wire.write(buffer.getData(), 0, buffer.length());
				break;
			case BYTE_BUFFER:
				ByteBuffer dataBuffer = buffer.getDataBuffer();
				byte[] bytes = new byte[dataBuffer.remaining()];
				dataBuffer.get(bytes);
				this.wire.write(bytes, 0, bytes.length);
				break;
			default:
				fail("Unknown type: " + type);
			}
		}
	}

	@Override
	public Object getLock() {
		return this;
	}

	@Override
	public boolean isSecure() {
		fail("Should not be invoked");
		return false;
	}

	@Override
	public void close() {
		this.isConnectionClosed = true;
	}

	@Override
	public boolean isClosed() {
		return this.isConnectionClosed;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		fail("Should not be invoked");
		return null;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		fail("Should not be invoked");
		return null;
	}

}