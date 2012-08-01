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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;

/**
 * Tests the {@link HttpConversation}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpConversationTest extends OfficeFrameTestCase {

	/**
	 * {@link Charset}.
	 */
	private static final Charset US_ASCII = Charset.forName("US-ASCII");

	/**
	 * {@link BufferSquirtFactory}.
	 */
	private final BufferSquirtFactory squirtFactory = new BufferSquirtFactory() {
		@Override
		public BufferSquirt createBufferSquirt() {
			return new MockBufferSquirt();
		}
	};

	/**
	 * {@link MockBufferSquirt} instances.
	 */
	private final List<MockBufferSquirt> squirts = new LinkedList<MockBufferSquirt>();

	/**
	 * {@link HttpConversation} to test.
	 */
	private final HttpConversation conversation = new HttpConversationImpl(
			new MockConnection(), this.squirtFactory, false);

	/**
	 * {@link BufferStream} containing the data output to the wire.
	 */
	private final BufferStream wire = new BufferStreamImpl(this.squirtFactory);

	/**
	 * Ensure no data on the wire until {@link HttpResponse} is closed.
	 */
	public void testNoResponse() throws IOException {
		HttpManagedObject mo = this
				.addRequest("GET", "/path", "HTTP/1.1", null);
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		OutputBufferStream body = response.getBody();
		writeUsAscii(body, "TEST");
		this.assertWireData("");

		// Close to ensure buffers get closed
		body.close();
	}

	/**
	 * Ensure able to provide response for single request.
	 */
	public void testSingleRequest() throws IOException {
		HttpManagedObject mo = this
				.addRequest("GET", "/path", "HTTP/1.1", null);
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		writeUsAscii(response.getBody(), "TEST");
		response.send();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST");
	}

	/**
	 * Ensures that closing the {@link OutputBufferStream} body triggers sending
	 * the {@link HttpResponse}.
	 */
	public void testInputBufferStreamCloseTriggersSend() throws IOException {
		HttpManagedObject mo = this
				.addRequest("GET", "/path", "HTTP/1.1", null);
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		writeUsAscii(response.getBody(), "TEST");

		// Close body as should trigger sending response
		response.getBody().close();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST");
	}

	/**
	 * Ensures that closing the {@link OutputStream} body triggers sending the
	 * {@link HttpResponse}.
	 */
	public void testInputStreamCloseTriggersSend() throws IOException {
		HttpManagedObject mo = this
				.addRequest("GET", "/path", "HTTP/1.1", null);
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		writeUsAscii(response.getBody(), "TEST");

		// Close body as should trigger sending response
		response.getBody().getOutputStream().close();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST");
	}

	/**
	 * Ensures that cleanup of the {@link HttpManagedObject} triggers the
	 * response.
	 */
	public void testCleanupTriggerResponse() throws IOException {
		HttpManagedObject mo = this
				.addRequest("GET", "/path", "HTTP/1.1", null);
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		writeUsAscii(response.getBody(), "TEST");
		mo.cleanup();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST");
	}

	/**
	 * Ensure able to handle two requests.
	 */
	public void testTwoRequests() throws IOException {
		// Two requests
		HttpManagedObject moOne = this.addRequest("GET", "/pathOne",
				"HTTP/1.1", null);
		HttpManagedObject moTwo = this.addRequest("GET", "/pathTwo",
				"HTTP/1.1", null);

		// Ensure responds immediately to first request
		HttpResponse responseOne = moOne.getServerHttpConnection()
				.getHttpResponse();
		writeUsAscii(responseOne.getBody(), "ONE");
		responseOne.send();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 3\n\nONE");

		// Ensure responds immediately to second request (as first sent)
		HttpResponse responseTwo = moTwo.getServerHttpConnection()
				.getHttpResponse();
		writeUsAscii(responseTwo.getBody(), "TWO");
		responseTwo.send();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 3\n\nTWO");
	}

	/**
	 * Ensure second response is not sent until first sent.
	 */
	public void testFirstResponseDelaysSecond() throws IOException {
		// Two requests
		HttpManagedObject moOne = this.addRequest("GET", "/pathOne",
				"HTTP/1.1", null);
		HttpManagedObject moTwo = this.addRequest("GET", "/pathTwo",
				"HTTP/1.1", null);

		// Ensure second response is delayed until first response sent
		HttpResponse responseTwo = moTwo.getServerHttpConnection()
				.getHttpResponse();
		writeUsAscii(responseTwo.getBody(), "TWO");
		responseTwo.send();
		this.assertWireData(""); // no data as first response must be sent

		// Send first response and ensure second also gets sent
		HttpResponse responseOne = moOne.getServerHttpConnection()
				.getHttpResponse();
		writeUsAscii(responseOne.getBody(), "ONE");
		responseOne.send();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 3\n\nONE"
				+ "HTTP/1.1 200 OK\nContent-Length: 3\n\nTWO");
	}

	/**
	 * Ensure {@link HttpRequestParseException} response is sent immediately.
	 */
	public void testParseFailure() throws IOException {
		final HttpRequestParseException failure = new HttpRequestParseException(
				HttpStatus.SC_BAD_REQUEST, "Body of parse failure response");
		this.conversation.parseFailure(failure, true);
		String message = failure.getMessage();
		this
				.assertWireData("HTTP/1.0 400 Bad Request\nContent-Type: text/html; charset=UTF-8\nContent-Length: "
						+ message.length() + "\n\n" + message);

		// Ensure the connection is closed
		assertEquals("Connection should be closed", BufferStream.END_OF_STREAM,
				this.wire.getInputBufferStream().available());
	}

	/**
	 * Ensures {@link HttpRequestParseException} response sent to correct
	 * request. In other words all previous requests are sent response and
	 * {@link HttpRequestParseException} is then sent immediately.
	 */
	public void testStopProcessingOnParseFailure() throws IOException {
		final HttpRequestParseException failure = new HttpRequestParseException(
				HttpStatus.SC_REQUEST_URI_TOO_LARGE,
				"Body of parse failure response");

		// Add request and then parse failure
		HttpResponse response = this.addRequest("GET", "/pathOne", "HTTP/1.1",
				null).getServerHttpConnection().getHttpResponse();
		this.conversation.parseFailure(failure, true);

		// Should be no response sent until first request serviced
		this.assertWireData("");

		// Send the request which should also send the parse fail response
		writeUsAscii(response.getBody(), "TEST");
		response.getBody().close();

		// Both request and parse failure responses should be sent
		String message = failure.getMessage();
		this
				.assertWireData("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST"
						+ "HTTP/1.0 414 Request-URI Too Large\nContent-Type: text/html; charset=UTF-8\nContent-Length: "
						+ message.length() + "\n\n" + message);

		// Ensure the connection is closed
		assertEquals("Connection should be closed", BufferStream.END_OF_STREAM,
				this.wire.getInputBufferStream().available());
	}

	/**
	 * Ensure able to provide failure from {@link EscalationHandler}.
	 */
	public void testEscalationHandler() throws Throwable {
		final Throwable failure = new Throwable("Handle Failure");

		// Add request
		HttpManagedObject mo = this.addRequest("POST", "/path", "HTTP/1.1",
				"REQUEST BODY");

		// Provide some content on response
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		response.addHeader("SuccessfulHeader", "SuccessfulValue");
		response.getBody().write("SUCCESSFUL CONTENT".getBytes());

		// Handle failure in processing the request
		mo.getEscalationHandler().handleEscalation(failure);

		// Ensure failure written as response
		String message = failure.getMessage();
		this
				.assertWireData("HTTP/1.1 500 Internal Server Error\nContent-Type: text/html; charset=UTF-8\nContent-Length: "
						+ message.length() + "\n\n" + message);
	}

	/*
	 * ================== Helper methods ====================================
	 */

	/**
	 * Writes the text to the {@link OutputBufferStream} in US-ASCII.
	 * 
	 * @param output
	 *            {@link OutputBufferStream}.
	 * @param text
	 *            Text to write.
	 * @throws IOException
	 *             If fails to write.
	 */
	private static void writeUsAscii(OutputBufferStream output, String text)
			throws IOException {
		Writer writer = new OutputStreamWriter(output.getOutputStream(),
				US_ASCII);
		writer.write(text);
		writer.flush();
	}

	/**
	 * Asserts that the data on the wire is as expected.
	 * 
	 * @param expectedData
	 *            Expected data on the wire.
	 */
	private void assertWireData(String expectedData) {

		// Transform the expected data to HTTP
		String expectedWireData = UsAsciiUtil.convertToString(UsAsciiUtil
				.convertToHttp(expectedData));

		// Obtain the wire data
		Reader reader = new InputStreamReader(this.wire.getInputBufferStream()
				.getInputStream(), US_ASCII);
		int availableBytes = (int) this.wire.available();
		StringBuilder data = new StringBuilder(availableBytes);
		try {
			for (int i = 0; i < availableBytes; i++) {
				char character = (char) reader.read();
				data.append(character);
			}
		} catch (IOException ex) {
			fail("Should not fail obtaining the wire content: "
					+ ex.getMessage());
		}

		// Assert that the wire data
		String wireData = data.toString();
		assertEquals("Invalid data on the wire", expectedWireData, wireData);
	}

	/**
	 * Adds a {@link HttpRequest}.
	 * 
	 * @param method
	 *            Method.
	 * @param requestURI
	 *            Request URI.
	 * @param httpVersion
	 *            HTTP version.
	 * @param body
	 *            Body contents of request.
	 * @param headerNameValuePairs
	 *            {@link HttpHeader} name/value pairs.
	 * @return {@link HttpManagedObject} from adding {@link HttpRequest}.
	 */
	private HttpManagedObject addRequest(String method, String requestURI,
			String httpVersion, String body, String... headerNameValuePairs)
			throws IOException {

		// Create the listing of headers
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		for (int i = 0; i < headerNameValuePairs.length; i += 2) {
			String name = headerNameValuePairs[i];
			String value = headerNameValuePairs[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}

		// Create the body for the request
		BufferStream bufferStream = new BufferStreamImpl(this.squirtFactory);
		OutputBufferStream outputStream = bufferStream.getOutputBufferStream();
		if ((body != null) && (body.length() == 0)) {
			writeUsAscii(outputStream, body);
		}
		outputStream.close();

		// Add the request
		return this.conversation.addRequest(method, requestURI, httpVersion,
				headers, bufferStream.getInputBufferStream());
	}

	/*
	 * ======================= TestCase =====================================
	 */

	@Override
	protected void tearDown() throws Exception {

		// Close the wire
		this.wire.getInputBufferStream().close();

		// Ensure all buffers are closed
		for (int i = 0; i < this.squirts.size(); i++) {
			MockBufferSquirt squirt = this.squirts.get(i);
			assertTrue("Squirt " + i + " not closed", squirt.isClosed);
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
		 * Flag indicating if closed.
		 */
		public volatile boolean isClosed = false;

		/**
		 * Register this {@link MockBufferSquirt}.
		 */
		public MockBufferSquirt() {
			HttpConversationTest.this.squirts.add(this);
		}

		/*
		 * =================== BufferSquirt =============================
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
		 * ================== Connection =================================
		 */

		@Override
		public Object getLock() {
			return HttpConversationTest.this.wire;
		}

		@Override
		public InetSocketAddress getLocalAddress() {
			fail("Local InetSocketAddress should not be required for HTTP conversation");
			return null;
		}

		@Override
		public InetSocketAddress getRemoteAddress() {
			fail("Remote InetSocketAddress should not be required for HTTP conversation");
			return null;
		}

		@Override
		public boolean isSecure() {
			fail("Determine if secure should not be required for HTTP conversation");
			return false;
		}

		@Override
		public InputBufferStream getInputBufferStream() {
			fail("Should not input from Connection");
			return null; // should fail
		}

		@Override
		public OutputBufferStream getOutputBufferStream() {
			return HttpConversationTest.this.wire.getOutputBufferStream();
		}
	}

}