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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.StubMatcher;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ReadContext;
import net.officefloor.plugin.socket.server.spi.ReadMessage;
import net.officefloor.plugin.socket.server.spi.WriteMessage;

/**
 * Tests the {@link HttpConnectionHandler}.
 * 
 * @author Daniel
 */
public class HttpConnectionHandlerTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpConnectionHandler} being tested.
	 */
	public HttpConnectionHandler handler;

	/**
	 * Mock {@link Connection}.
	 */
	private Connection connection = this.createMock(Connection.class);

	/**
	 * Mock {@link ReadContext}.
	 */
	private ReadContext readContext = this.createMock(ReadContext.class);

	/**
	 * Mock {@link ReadMessage}.
	 */
	private ReadMessage readMessage = this.createMock(ReadMessage.class);

	/**
	 * Mock {@link WriteMessage}.
	 */
	private WriteMessage writeMessage = this.createMock(WriteMessage.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Create the handler to test
		this.handler = new HttpConnectionHandler(this.connection, 1024,
				1024 * 1024);
	}

	/**
	 * Ensures successful read of HTTP request.
	 */
	public void testSuccessfulRead() throws Exception {

		// Obtain the request
		String requestText = "GET /path HTTP/1.1\nhost: localhost\n\n";
		final byte[] request = UsAsciiUtil.convertToHttp(requestText);

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getReadMessage(),
				this.readMessage);
		this.recordReturn(this.readMessage, this.readMessage.read(null),
				request.length, new StubMatcher() {
					@Override
					protected void stub(Object[] arguments) {
						byte[] buffer = (byte[]) arguments[0];
						for (int i = 0; i < request.length; i++) {
							buffer[i] = request[i];
						}
					}
				});
		this.readContext.setReadComplete(true);

		// Replay mocks
		this.replayMockObjects();

		// Handle the read
		this.handler.handleRead(this.readContext);

		// Verify mocks
		this.verifyMockObjects();

		// Validate HTTP request
		HttpRequestParser parser = this.handler.getHttpRequestParser();
		assertEquals("Incorrect method", "GET", parser.getMethod());
		assertEquals("Incorrect path", "/path", parser.getPath());
		assertEquals("Incorrect version", "HTTP/1.1", parser.getVersion());
		assertEquals("Incorrect header", "localhost", parser.getHeader("host"));
		assertEquals("Incorrect body", 0, parser.getBody().length);
	}

	/**
	 * Ensures correctly handles an invalid HTTP request.
	 * 
	 * @throws Exception
	 */
	public void testInvalidRead() throws Exception {

		String invalidRequestText = "Invalid Request";
		final byte[] request = UsAsciiUtil.convertToHttp(invalidRequestText);

		String badRequestResponseText = "HTTP/1.0 400 Bad Request\nContent-Length: 23\n\nUnknown method: Invalid";
		final byte[] badRequestResponse = UsAsciiUtil
				.convertToHttp(badRequestResponseText);

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getReadMessage(),
				this.readMessage);
		this.recordReturn(this.readMessage, this.readMessage.read(null),
				request.length, new StubMatcher() {
					@Override
					protected void stub(Object[] arguments) {
						byte[] buffer = (byte[]) arguments[0];
						for (int i = 0; i < request.length; i++) {
							buffer[i] = request[i];
						}
					}
				});
		this.recordReturn(this.connection, this.connection
				.createWriteMessage(null), this.writeMessage);
		this.writeMessage.append(badRequestResponse);
		this.control(this.writeMessage).setMatcher(
				UsAsciiUtil.createUsAsciiMatcher());
		this.writeMessage.write();
		this.readContext.setReadComplete(true);

		// Replay mocks
		this.replayMockObjects();

		// Handle the read
		this.handler.handleRead(this.readContext);

		// Verify mocks
		this.verifyMockObjects();

		// Validate HTTP request
		assertNull("Should not have parser", this.handler
				.getHttpRequestParser());
	}
}
