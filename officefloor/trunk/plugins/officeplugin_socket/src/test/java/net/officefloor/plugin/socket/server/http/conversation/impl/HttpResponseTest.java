/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.socket.server.impl.ArrayWriteBuffer;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

/**
 * Tests the {@link HttpResponseImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseTest extends OfficeFrameTestCase implements Connection {

	/**
	 * End of line token.
	 */
	private static final String EOLN_TOKEN = "${EOLN}";

	/**
	 * US-ASCII {@link Charset}.
	 */
	private static final Charset US_ASCII = Charset.forName("US-ASCII");

	/**
	 * Contains the content written on the wire.
	 */
	private final ByteArrayOutputStream wire = new ByteArrayOutputStream();

	/**
	 * {@link HttpConversation} to create the {@link HttpResponse}.
	 */
	private HttpConversation conversation = this.createHttpConversation(false);

	/**
	 * Ensure can send a simple response.
	 */
	public void testSimpleResponse() throws IOException {
		HttpResponse response = this.createHttpResponse();
		this.writeBody(response, "TEST");
		response.getEntity().close();
		this.assertWireContent("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST");
	}

	/**
	 * Ensures provides correct response if no content.
	 */
	public void testNoContent() throws IOException {
		HttpResponse response = this.createHttpResponse();
		response.getEntity().close();
		this.assertWireContent("HTTP/1.1 204 No Content\nContent-Length: 0\n\n");
	}

	/**
	 * Ensure able to send <code>null</code> header value.
	 */
	public void testNullHeaderValue() throws IOException {
		HttpResponse response = this.createHttpResponse();
		response.addHeader("null", null);
		response.send();
		this.assertWireContent("HTTP/1.1 204 No Content\n" + "null: \n"
				+ "Content-Length: 0\n\n");
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
				+ "Content-Type: text/html; charset=UTF-8\n"
				+ "Content-Length: 15\n\n" + "Fail Parse Test");
	}

	/**
	 * Ensures provides failure message.
	 */
	public void testSendServerFailure() throws IOException {

		final Throwable FAILURE = new Exception("Failure Test");

		this.sendFailure(FAILURE);
		this.assertWireContent("HTTP/1.1 500 Internal Server Error\n"
				+ "Content-Type: text/html; charset=UTF-8\n"
				+ "Content-Length: 12\n\n" + "Failure Test");
	}

	/**
	 * Ensures provides failure details should the exception have no message.
	 */
	public void testSendServerFailureWithNoMessage() throws IOException {

		final Throwable FAILURE = new Exception();

		this.sendFailure(FAILURE);
		this.assertWireContent("HTTP/1.1 500 Internal Server Error\n"
				+ "Content-Type: text/html; charset=UTF-8\n"
				+ "Content-Length: 9\n\n" + Exception.class.getSimpleName());
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
		String content = "Test\n\n" + stackTrace;
		int contentLength = content.length();
		content = content.replace("\n", EOLN_TOKEN);

		// Create conversation to send stack trace on failure
		this.conversation = this.createHttpConversation(true);

		// Run test
		this.sendFailure(FAILURE);
		this.assertWireContent("HTTP/1.1 500 Internal Server Error\n"
				+ "Content-Type: text/html; charset=UTF-8\n"
				+ "Content-Length: " + contentLength + "\n\n" + content);
	}

	/**
	 * Ensure appropriately provides header to cached {@link ByteBuffer}.
	 */
	public void testSendCachedByteBuffer() {
		fail("TODO implement sending for cached ByteBuffer");
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
		return new HttpConversationImpl(this, 1024, isSendStackTraceOnFailure);
	}

	/**
	 * Creates a {@link HttpResponse} to be tested.
	 * 
	 * @return New {@link HttpResponse}.
	 */
	private HttpResponse createHttpResponse() {

		// Add the request
		ServerInputStreamImpl entity = new ServerInputStreamImpl(new Object());
		entity.inputData(null, 0, 0, false);
		HttpManagedObject mo = this.conversation.addRequest("GET", "/mock",
				"HTTP/1.1", new LinkedList<HttpHeader>(), entity);

		// Return the http response from managed object
		return mo.getServerHttpConnection().getHttpResponse();
	}

	/**
	 * Writes the content to the body.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @param bodyContent
	 *            Content to be written to the body.
	 */
	private void writeBody(HttpResponse response, String content) {

		// Create the writer for the body
		Writer writer = new OutputStreamWriter(response.getEntity(), US_ASCII);

		// Write the body content
		try {
			writer.write(content);
			writer.flush();
		} catch (IOException ex) {
			fail("Should not fail on writing content to body: "
					+ ex.getMessage());
		}
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
	 * @param expectedContent
	 *            Expected content on the wire.
	 */
	private void assertWireContent(String expectedContent) {

		// Transform expected content to wire format
		String expectedHttpContent = UsAsciiUtil.convertToString(UsAsciiUtil
				.convertToHttp(expectedContent));
		expectedHttpContent = expectedHttpContent.replace(EOLN_TOKEN, "\n");

		// Validate the response on the wire
		String wireText = UsAsciiUtil.convertToString(this.wire.toByteArray());
		assertEquals("Incorrect wire content", expectedHttpContent, wireText);
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
		// TODO implement WriteBufferReceiver.createWriteBuffer
		throw new UnsupportedOperationException(
				"TODO implement WriteBufferReceiver.createWriteBuffer");
	}

	@Override
	public void writeData(WriteBuffer[] data) {
		// Write the data to the wire
		for (WriteBuffer buffer : data) {
			this.wire.write(buffer.getData(), 0, buffer.length());
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
		fail("Should not be invoked");
	}

	@Override
	public boolean isClosed() {
		fail("Should not be invoked");
		return false;
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