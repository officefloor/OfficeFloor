/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpResponseImpl;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
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
	 * US-ASCII {@link Charset}.
	 */
	private static final Charset US_ASCII = Charset.forName("US-ASCII");

	/**
	 * Contains the content written on the wire.
	 */
	private final BufferStream wire = new BufferStreamImpl(
			new HeapByteBufferSquirtFactory(1024));

	/**
	 * Listing of {@link MockBufferSquirt} instances.
	 */
	private final List<MockBufferSquirt> squirts = new LinkedList<MockBufferSquirt>();

	/**
	 * Ensure can send a simple response.
	 */
	public void testSimpleResponse() throws IOException {
		HttpResponseImpl response = this.createHttpResponse();
		this.writeBody(response, "TEST");
		response.write();
		this.assertWireContent("HTTP/1.1 200 OK\nContent-Length: 4\n\nTEST");
	}

	/**
	 * Ensures provides correct response if no content.
	 */
	public void testNoContent() throws IOException {
		HttpResponseImpl response = this.createHttpResponse();
		response.write();
		this
				.assertWireContent("HTTP/1.1 204 No Content\nContent-Length: 0\n\n");
	}

	/*
	 * ===================== Helper methods ==============================
	 */

	/**
	 * Creates a {@link HttpResponseImpl} to be tested.
	 *
	 * @return New {@link HttpResponseImpl}.
	 */
	private HttpResponseImpl createHttpResponse() {
		return new HttpResponseImpl(new MockConnection(),
				new BufferSquirtFactory() {
					@Override
					public BufferSquirt createBufferSquirt() {
						return new MockBufferSquirt();
					}
				}, "HTTP/1.1");
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
	 * Asserts the content on the wire is as expected.
	 *
	 * @param expectedContent
	 *            Expected content on the wire.
	 */
	private void assertWireContent(String expectedContent) {

		// Transform expected content to wire format
		String expectedHttpContent = UsAsciiUtil.convertToString(UsAsciiUtil
				.convertToHttp(expectedContent));

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