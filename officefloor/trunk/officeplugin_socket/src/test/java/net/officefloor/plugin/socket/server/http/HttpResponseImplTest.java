/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.MessageSegment;
import net.officefloor.plugin.socket.server.spi.WriteMessage;

/**
 * Tests the {@link HttpResponseImpl}.
 * 
 * @author Daniel
 */
public class HttpResponseImplTest extends OfficeFrameTestCase {

	/**
	 * Ensure output via {@link OutputStream}.
	 */
	public void testOutpuStream() throws Exception {
		this.appendViaOutputStream("test");
		this.doOkTest("test");
	}

	/**
	 * Ensure output via appending {@link ByteBuffer} to body.
	 */
	public void testAppendToBody() throws Exception {
		this.appendViaBuffer("test");
		this.doOkTest("test");
	}

	/**
	 * Ensure various appending.
	 */
	public void testVariousAppending() throws Exception {
		this.appendViaOutputStream("t");
		this.appendViaBuffer("e");
		this.appendViaBuffer("s");
		this.appendViaOutputStream("t");
		this.appendViaOutputStream("i");
		this.appendViaBuffer("n");
		this.appendViaOutputStream("g");
		this.doOkTest("testing");
	}

	public void testTwoBufferAppending() throws Exception {

		// Determine length to fit in two buffers
		int msgLength = (new HttpServerSocketManagedObjectSource()
				.getResponseBufferLength() * 2) - 1;

		// Obtain the two buffer message
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < msgLength; i++) {
			int digit = i % 10;
			msg.append(digit);
		}
		String message = msg.toString();

		// Test with two buffer message
		this.appendViaOutputStream(message);
		this.doOkTest(message);
	}

	/**
	 * Ensure able to handle large appending.
	 */
	public void testLargeAppending() throws Exception {

		// Determine very large message length
		int msgLength = new HttpServerSocketManagedObjectSource()
				.getResponseBufferLength() * 100;

		// Obtain the large message
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < msgLength; i++) {
			int digit = i % 10;
			switch (digit) {
			case 9:
				msg.append("\r\n");
				break;
			default:
				msg.append(digit);
				break;
			}
		}
		String message = msg.toString();

		// Test with the large message
		this.appendViaOutputStream(message);
		this.doOkTest(message);
	}

	/*
	 * ============= Test helper methods ==========================
	 */

	/**
	 * Listing of {@link Functionality} to be performed.
	 */
	private List<Functionality> functionality = new LinkedList<Functionality>();

	/**
	 * Record appending content via {@link OutputStream}.
	 */
	private void appendViaOutputStream(String content) {
		this.functionality.add(new Functionality(
				FunctionalityType.OUTPUT_STREAM, content));
	}

	/**
	 * Record appending content via buffer.
	 */
	private void appendViaBuffer(String content) {
		this.functionality.add(new Functionality(
				FunctionalityType.APPEND_TO_BODY, content));
	}

	/**
	 * Does a test for {@link HttpResponse} of status OK.
	 * 
	 * @param body
	 *            Expected body.
	 * @param headerNameValues
	 *            Expected header name value pairs.
	 */
	private void doOkTest(String body, String... headerNameValues)
			throws Exception {
		final int HTTP_STATUS = HttpStatus._200;
		this.doTest("HTTP/1.0", HTTP_STATUS, HttpStatus
				.getStatusMessage(HTTP_STATUS), body, headerNameValues);
	}

	/**
	 * Does the test.
	 * 
	 * @param version
	 *            HTTP version.
	 * @param httpStatus
	 *            Expected HTTP status.
	 * @param statusMessage
	 *            Expected HTTP status message.
	 * @param body
	 *            Expected body.
	 * @param headerNameValues
	 *            Expected header name value pairs.
	 */
	private void doTest(String version, int httpStatus, String statusMessage,
			String body, String... headerNameValues) throws Exception {

		// Create the necessary mock objects
		Connection connection = this.createMock(Connection.class);
		HttpRequest request = this.createMock(HttpRequest.class);
		MockWriteMessage message = new MockWriteMessage();

		// Record actions on mocks
		this.recordReturn(request, request.getVersion(), version);
		this.recordReturn(connection, connection.createWriteMessage(null),
				message);

		// Replay mocks
		this.replayMockObjects();

		// Create the necessary dependency objects
		HttpConnectionHandler handler = new HttpConnectionHandler(
				new HttpServerSocketManagedObjectSource(), connection);

		// Create the HTTP response
		HttpResponse response = new HttpResponseImpl(handler, request);

		// Do functionality on body
		OutputStream outputStream = null;
		for (Functionality function : this.functionality) {
			switch (function.type) {
			case OUTPUT_STREAM:
				if (outputStream == null) {
					outputStream = response.getBody();
				}
				byte[] outputStreamData = UsAsciiUtil
						.convertToUsAscii(function.content);
				outputStream.write(outputStreamData);
				outputStream.flush();
				break;

			case APPEND_TO_BODY:
				byte[] appendData = UsAsciiUtil
						.convertToUsAscii(function.content);
				response.appendToBody(ByteBuffer.wrap(appendData));
				break;
			}
		}
		response.send();

		// Verify mocks
		this.verifyMockObjects();

		// Ensure have body content
		body = (body == null ? "" : body);

		// Obtain the expected HTTP data
		StringBuilder content = new StringBuilder();
		content.append(version + " " + httpStatus + " " + statusMessage + "\n");
		for (int i = 0; i < headerNameValues.length; i++) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			content.append(name + ": " + value + "\n");
		}
		content.append("Content-Length: " + body.length() + "\n");
		content.append("\n");
		String expectedHttpMessage = UsAsciiUtil.convertToString(UsAsciiUtil
				.convertToHttp(content.toString()))
				+ body;

		// Obtain the actual HTTP data
		message.buffer.flush();
		String actualHttpMessage = UsAsciiUtil.convertToString(message.buffer
				.toByteArray());

		// Verify content
		assertEquals("Incorrect HTTP message", expectedHttpMessage,
				actualHttpMessage);
	}

	/**
	 * Type of functionality on the {@link HttpResponse}.
	 */
	private enum FunctionalityType {
		OUTPUT_STREAM, APPEND_TO_BODY
	};

	/**
	 * Content to append to the {@link HttpResponse}.
	 */
	private class Functionality {

		/**
		 * {@link FunctionalityType}.
		 */
		public final FunctionalityType type;

		/**
		 * Content.
		 */
		public final String content;

		/**
		 * Initiate.
		 * 
		 * @param type
		 *            {@link FunctionalityType}.
		 * @param content
		 *            Content.
		 */
		public Functionality(FunctionalityType type, String content) {
			this.type = type;
			this.content = content;
		}
	}

	/**
	 * Mock {@link WriteMessage}.
	 */
	private class MockWriteMessage implements WriteMessage {

		/**
		 * Content written to the {@link WriteMessage}.
		 */
		public final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		/**
		 * Flag indicating if written.
		 */
		public boolean isWritten = false;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.spi.WriteMessage#append(byte[],
		 * int, int)
		 */
		@Override
		public void append(byte[] data, int offset, int length) {
			this.buffer.write(data, offset, length);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.spi.WriteMessage#append(byte[])
		 */
		@Override
		public void append(byte[] data) {
			this.append(data, 0, data.length);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.spi.WriteMessage#appendSegment
		 * (java.nio.ByteBuffer)
		 */
		@Override
		public MessageSegment appendSegment(ByteBuffer byteBuffer) {
			byte[] data = byteBuffer.array();
			this.append(data, 0, byteBuffer.limit());
			return null; // message segment not required
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#write()
		 */
		@Override
		public void write() {
			this.isWritten = true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.spi.WriteMessage#isWritten()
		 */
		@Override
		public boolean isWritten() {
			fail("Should not require to determine if written");
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.spi.WriteMessage#appendSegment()
		 */
		@Override
		public MessageSegment appendSegment() {
			fail("Should not blank append segment");
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.plugin.socket.server.spi.Message#getConnection()
		 */
		@Override
		public Connection getConnection() {
			fail("Should not require connection");
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.spi.Message#getFirstSegment()
		 */
		@Override
		public MessageSegment getFirstSegment() {
			fail("Should not require first segment");
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.spi.Message#getLastSegment()
		 */
		@Override
		public MessageSegment getLastSegment() {
			fail("Should not require second segment");
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.spi.Message#getSegmentCount()
		 */
		@Override
		public int getSegmentCount() {
			fail("Should not require segment count");
			return -1;
		}

	}
}
