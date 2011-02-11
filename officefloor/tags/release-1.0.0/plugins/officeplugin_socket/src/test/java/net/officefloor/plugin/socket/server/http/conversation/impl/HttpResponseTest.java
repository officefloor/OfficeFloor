/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;
import net.officefloor.plugin.stream.squirtfactory.HeapByteBufferSquirtFactory;

/**
 * Tests the {@link HttpResponseImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseTest extends OfficeFrameTestCase {

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
	private final BufferStream wire = new BufferStreamImpl(
			new HeapByteBufferSquirtFactory(1024));

	/**
	 * {@link HttpConversation} to create the {@link HttpResponse}.
	 */
	private HttpConversation conversation = this.createHttpConversation(false);

	/**
	 * Listing of {@link MockBufferSquirt} instances.
	 */
	private final List<MockBufferSquirt> squirts = new LinkedList<MockBufferSquirt>();

	/**
	 * Ensure can send a simple response.
	 */
	public void testSimpleResponse() throws IOException {
		HttpResponse response = this.createHttpResponse();
		this.writeBody(response, "TEST");
		response.getBody().close();
		this.assertWireContent("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST");
	}

	/**
	 * Ensures provides correct response if no content.
	 */
	public void testNoContent() throws IOException {
		HttpResponse response = this.createHttpResponse();
		response.getBody().close();
		this
				.assertWireContent("HTTP/1.1 204 No Content\nContent-Length: 0\n\n");
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
		assertNull("Should be no headers by name on removing", response
				.getHeader("test"));
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
		return new HttpConversationImpl(new MockConnection(),
				new BufferSquirtFactory() {
					@Override
					public BufferSquirt createBufferSquirt() {
						return new MockBufferSquirt();
					}
				}, isSendStackTraceOnFailure);
	}

	/**
	 * Creates a {@link HttpResponse} to be tested.
	 * 
	 * @return New {@link HttpResponse}.
	 */
	private HttpResponse createHttpResponse() {
		try {
			// Add the request
			HttpManagedObject mo = this.conversation.addRequest("GET", "/mock",
					"HTTP/1.1", new LinkedList<HttpHeader>(),
					new BufferStreamImpl(ByteBuffer.wrap(new byte[0]))
							.getInputBufferStream());

			// Return the http response from managed object
			return mo.getServerHttpConnection().getHttpResponse();

		} catch (IOException ex) {
			fail("Should not fail to add request: " + ex.getMessage());
			return null; // should fail
		}
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
		Writer writer = new OutputStreamWriter(response.getBody()
				.getOutputStream(), US_ASCII);

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

		// Obtain the response on the wire
		InputBufferStream wireInput = this.wire.getInputBufferStream();
		byte[] wireContent = new byte[(int) wireInput.available()];
		try {
			wireInput.read(wireContent);
		} catch (IOException ex) {
			fail("Should not fail on wire read: " + ex.getMessage());
		}

		// Validate the response on the wire
		String wireText = UsAsciiUtil.convertToString(wireContent);
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
	 * ========================== TestCase ====================================
	 */
	@Override
	protected void tearDown() throws Exception {
		// Ensure all squirts are closed
		for (int i = 0; i < this.squirts.size(); i++) {
			MockBufferSquirt squirt = this.squirts.get(i);
			assertTrue("Squirt " + i + " must be closed", squirt.isClosed);
		}
	}

	/**
	 * Mock {@link BufferSquirt}.
	 */
	private class MockBufferSquirt implements BufferSquirt {

		/**
		 * {@link ByteBuffer}.
		 */
		private final ByteBuffer buffer = ByteBuffer.allocate(1024);

		/**
		 * Flags if this {@link BufferSquirt} was closed.
		 */
		public volatile boolean isClosed = false;

		/**
		 * Register this {@link BufferSquirt}.
		 */
		public MockBufferSquirt() {
			HttpResponseTest.this.squirts.add(this);
		}

		/*
		 * ====================== BufferSquirt ===========================
		 */

		@Override
		public ByteBuffer getBuffer() {
			return this.buffer;
		}

		@Override
		public void close() {
			this.isClosed = true;
		}
	}

	/**
	 * Mock {@link Connection}.
	 */
	private class MockConnection implements Connection {

		/*
		 * ================== Connection ===========================
		 */

		@Override
		public Object getLock() {
			return HttpResponseTest.this.wire;
		}

		@Override
		public InetSocketAddress getLocalAddress() {
			fail("Local InetSocketAddress should not be required for writing HTTP response");
			return null;
		}

		@Override
		public InetSocketAddress getRemoteAddress() {
			fail("Remote InetSocketAddress should not be required for writing HTTP response");
			return null;
		}

		@Override
		public boolean isSecure() {
			fail("Determine if secure should not be required for writing HTTP response");
			return false;
		}

		@Override
		public InputBufferStream getInputBufferStream() {
			fail("Should not input from connection for response");
			return null;
		}

		@Override
		public OutputBufferStream getOutputBufferStream() {
			return HttpResponseTest.this.wire.getOutputBufferStream();
		}
	}

}