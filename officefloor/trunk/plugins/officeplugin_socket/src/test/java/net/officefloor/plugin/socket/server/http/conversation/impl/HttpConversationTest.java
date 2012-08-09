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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import net.officefloor.plugin.socket.server.impl.ArrayWriteBuffer;
import net.officefloor.plugin.socket.server.impl.BufferWriteBuffer;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBufferEnum;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

/**
 * Tests the {@link HttpConversation}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpConversationTest extends OfficeFrameTestCase {

	/**
	 * {@link Charset}.
	 */
	private static final Charset US_ASCII = UsAsciiUtil.US_ASCII;

	/**
	 * {@link MockConnection}.
	 */
	private final Connection connection = new MockConnection();

	/**
	 * {@link HttpConversation} to test.
	 */
	private final HttpConversation conversation = new HttpConversationImpl(
			this.connection, 1024, US_ASCII, false);

	/**
	 * {@link ByteArrayOutputStream} containing the data output to the wire.
	 */
	private final ByteArrayOutputStream wire = new ByteArrayOutputStream();

	/**
	 * Ensure no data on the wire until {@link HttpResponse} is closed.
	 */
	public void testNoResponse() throws IOException {
		HttpManagedObject mo = this
				.addRequest("GET", "/path", "HTTP/1.1", null);
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		OutputStream entity = response.getEntity();
		writeUsAscii(entity, "TEST");
		this.assertWireData("");

		// Close to send
		entity.close();
	}

	/**
	 * Ensure able to provide response for single request.
	 */
	public void testSingleRequest() throws IOException {
		HttpManagedObject mo = this
				.addRequest("GET", "/path", "HTTP/1.1", null);
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		writeUsAscii(response.getEntity(), "TEST");
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
		OutputStream entity = response.getEntity();
		writeUsAscii(entity, "TEST");

		// Close entity as should trigger sending response
		entity.close();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST");
	}

	/**
	 * Ensures that closing the {@link OutputStream} entity triggers sending the
	 * {@link HttpResponse}.
	 */
	public void testInputStreamCloseTriggersSend() throws IOException {
		HttpManagedObject mo = this
				.addRequest("GET", "/path", "HTTP/1.1", null);
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		OutputStream entity = response.getEntity();
		writeUsAscii(entity, "TEST");

		// Close entity as should trigger sending response
		entity.close();
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
		writeUsAscii(response.getEntity(), "TEST");
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
		writeUsAscii(responseOne.getEntity(), "ONE");
		responseOne.send();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 3\n\nONE");

		// Ensure responds immediately to second request (as first sent)
		HttpResponse responseTwo = moTwo.getServerHttpConnection()
				.getHttpResponse();
		writeUsAscii(responseTwo.getEntity(), "TWO");
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
		writeUsAscii(responseTwo.getEntity(), "TWO");
		responseTwo.send();
		this.assertWireData(""); // no data as first response must be sent

		// Send first response and ensure second also gets sent
		HttpResponse responseOne = moOne.getServerHttpConnection()
				.getHttpResponse();
		writeUsAscii(responseOne.getEntity(), "ONE");
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
		String message = failure.getClass().getSimpleName() + ": "
				+ failure.getMessage();
		this.assertWireData("HTTP/1.0 400 Bad Request\nContent-Type: text/html; charset="
				+ US_ASCII.name()
				+ "\nContent-Length: "
				+ message.length()
				+ "\n\n" + message);

		// Ensure the connection is closed
		assertTrue("Connection should be closed", this.connection.isClosed());
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
		HttpResponse response = this
				.addRequest("GET", "/pathOne", "HTTP/1.1", null)
				.getServerHttpConnection().getHttpResponse();
		this.conversation.parseFailure(failure, true);

		// Should be no response sent until first request serviced
		this.assertWireData("");

		// Send the request which should also send the parse fail response
		OutputStream entity = response.getEntity();
		writeUsAscii(entity, "TEST");
		entity.close();

		// Both request and parse failure responses should be sent
		String message = failure.getClass().getSimpleName() + ": "
				+ failure.getMessage();
		this.assertWireData("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST"
				+ "HTTP/1.0 414 Request-URI Too Large\nContent-Type: text/html; charset="
				+ US_ASCII.name() + "\nContent-Length: " + message.length()
				+ "\n\n" + message);

		// Ensure the connection is closed
		assertTrue("Connection should be closed", this.connection.isClosed());
	}

	/**
	 * Ensure able to provide failure from {@link EscalationHandler}.
	 */
	public void testEscalationHandler() throws Throwable {
		final Throwable failure = new Throwable("Handle Failure");

		// Add request
		HttpManagedObject mo = this.addRequest("POST", "/path", "HTTP/1.1",
				"REQUEST BODY");

		// Provide some content on response (should be cleared)
		HttpResponse response = mo.getServerHttpConnection().getHttpResponse();
		response.addHeader("SuccessfulHeader", "SuccessfulValue");
		response.getEntity().write("SUCCESSFUL CONTENT".getBytes());

		// Handle failure in processing the request
		mo.getEscalationHandler().handleEscalation(failure);

		// Ensure failure written as response
		String message = failure.getClass().getSimpleName() + ": "
				+ failure.getMessage();
		this.assertWireData("HTTP/1.1 500 Internal Server Error"
				+ "\nContent-Type: text/html; charset=" + US_ASCII.name()
				+ "\nContent-Length: " + message.length() + "\n\n" + message);
	}

	/**
	 * Ensure closes the {@link Connection}.
	 */
	public void testCloseConnection() throws IOException {

		// Close the connection
		this.conversation.closeConnection();

		// Ensure closed
		assertTrue("Connection should be closed", this.connection.isClosed());
	}

	/*
	 * ================== Helper methods ====================================
	 */

	/**
	 * Writes the text to the {@link OutputBufferStream} in US-ASCII.
	 * 
	 * @param output
	 *            {@link OutputStream}.
	 * @param text
	 *            Text to write.
	 * @throws IOException
	 *             If fails to write.
	 */
	private static void writeUsAscii(OutputStream output, String text)
			throws IOException {
		Writer writer = new OutputStreamWriter(output, US_ASCII);
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
		byte[] wireBytes = this.wire.toByteArray();
		String wireData = UsAsciiUtil.convertToString(wireBytes);

		// Assert that the wire data
		assertEquals("Invalid data on the wire", expectedWireData, wireData);

		// Clear wire data for next assertion
		this.wire.reset();
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
	 * @param entity
	 *            Entity contents of request.
	 * @param headerNameValuePairs
	 *            {@link HttpHeader} name/value pairs.
	 * @return {@link HttpManagedObject} from adding {@link HttpRequest}.
	 */
	private HttpManagedObject addRequest(String method, String requestURI,
			String httpVersion, String entity, String... headerNameValuePairs)
			throws IOException {

		// Create the listing of headers
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		for (int i = 0; i < headerNameValuePairs.length; i += 2) {
			String name = headerNameValuePairs[i];
			String value = headerNameValuePairs[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}

		// Create the entity for the request
		ServerInputStreamImpl entityStream = new ServerInputStreamImpl(
				new Object());
		entity = ((entity == null) || (entity.length() == 0)) ? "" : entity;
		byte[] entityData = UsAsciiUtil.convertToUsAscii(entity);
		entityStream.inputData(entityData, 0, (entityData.length - 1), false);

		// Add the request
		return this.conversation.addRequest(method, requestURI, httpVersion,
				headers, entityStream);
	}

	/**
	 * Mock {@link Connection}.
	 */
	private class MockConnection implements Connection {

		/**
		 * Indicates if closed.
		 */
		private boolean isClosed = false;

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
		public WriteBuffer createWriteBuffer(byte[] data, int length) {
			return new ArrayWriteBuffer(data, length);
		}

		@Override
		public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
			return new BufferWriteBuffer(buffer);
		}

		@Override
		public void writeData(WriteBuffer[] data) {

			// Write out the data
			for (WriteBuffer buffer : data) {
				WriteBufferEnum type = buffer.getType();
				switch (type) {

				case BYTE_ARRAY:
					HttpConversationTest.this.wire.write(buffer.getData(), 0,
							buffer.length());
					break;

				case BYTE_BUFFER:
					ByteBuffer byteBuffer = buffer.getDataBuffer();
					byte[] writeData = new byte[byteBuffer.remaining()];
					byteBuffer.get(writeData);
					HttpConversationTest.this.wire.write(writeData, 0,
							writeData.length);
					break;

				default:
					fail("Unknown type: " + type);
				}
			}
		}

		@Override
		public void close() {
			this.isClosed = true;
		}

		@Override
		public boolean isClosed() {
			return this.isClosed;
		}
	}

}