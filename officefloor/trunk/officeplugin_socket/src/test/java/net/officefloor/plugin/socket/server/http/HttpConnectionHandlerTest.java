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
import net.officefloor.plugin.socket.server.http.parse.ParseException;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.IdleContext;
import net.officefloor.plugin.socket.server.spi.ReadContext;
import net.officefloor.plugin.socket.server.spi.ReadMessage;
import net.officefloor.plugin.socket.server.spi.WriteContext;
import net.officefloor.plugin.socket.server.spi.WriteMessage;

/**
 * Tests the {@link HttpConnectionHandler}.
 * 
 * @author Daniel
 */
public class HttpConnectionHandlerTest extends OfficeFrameTestCase {

	/**
	 * Timeout of the {@link Connection}.
	 */
	private static final long CONNECTION_TIMEOUT = 10;

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

	/**
	 * Mock {@link WriteContext}.
	 */
	private WriteContext writeContext = this.createMock(WriteContext.class);

	/**
	 * Mock {@link IdleContext}.
	 */
	private IdleContext idleContext = this.createMock(IdleContext.class);

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
				1024 * 1024, CONNECTION_TIMEOUT);
	}

	/**
	 * Ensures successful read of HTTP request.
	 */
	public void testSuccessfulRead() throws Exception {

		// Obtain the request
		String requestText = "GET /path HTTP/1.1\nhost: localhost\n\n";
		final byte[] request = UsAsciiUtil.convertToHttp(requestText);

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), System
				.currentTimeMillis());
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
	 */
	public void testInvalidRead() throws Exception {

		String invalidRequestText = "Invalid Request";
		final byte[] request = UsAsciiUtil.convertToHttp(invalidRequestText);

		// Generate details of invalid response message
		String badRequestResponseText = null;
		try {
			new HttpRequestParser(1024, 1024).parseMoreContent(request, 0,
					request.length);
			fail("Test invalid as should not be able to parse request");
		} catch (ParseException ex) {
			int status = ex.getHttpStatus();
			String statusMsg = HttpStatus.getStatusMessage(status);
			String reason = ex.getMessage();
			badRequestResponseText = "HTTP/1.0 " + status + " " + statusMsg
					+ "\nContent-Length: " + reason.length() + "\n\n" + reason;
		}
		final byte[] badRequestResponse = UsAsciiUtil
				.convertToHttp(badRequestResponseText);

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), System
				.currentTimeMillis());
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

	/**
	 * Ensures does nothing on write.
	 */
	public void testWrite() {

		// Record actions
		this.recordReturn(this.writeContext, this.writeContext.getTime(), 1000);

		// Replay mocks
		this.replayMockObjects();

		// Handle the write
		this.handler.handleWrite(this.writeContext);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensures checks idle time.
	 */
	public void testIdle() {

		// Record actions
		this.recordReturn(this.writeContext, this.writeContext.getTime(), 1000);
		this.recordReturn(this.idleContext, this.idleContext.getTime(), 1000);

		// Replay mocks
		this.replayMockObjects();

		// Invoke write to set last interaction time
		this.handler.handleWrite(this.writeContext);

		// Handle the idle
		this.handler.handleIdleConnection(this.idleContext);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensures closes {@link Connection} on {@link Connection} being too long
	 * idle.
	 */
	public void testIdleTooLong() {

		// Record actions
		final long START_TIME = System.currentTimeMillis();
		final long FIRST_IDLE_TIME = START_TIME + CONNECTION_TIMEOUT - 1;
		final long SECOND_IDLE_TIME = START_TIME + CONNECTION_TIMEOUT;
		this.recordReturn(this.writeContext, this.writeContext.getTime(),
				START_TIME);
		this.recordReturn(this.idleContext, this.idleContext.getTime(),
				FIRST_IDLE_TIME);
		this.recordReturn(this.idleContext, this.idleContext, SECOND_IDLE_TIME);
		this.idleContext.setCloseConnection(true);

		// Replay mocks
		this.replayMockObjects();

		// Invoke write to set last interaction time
		this.handler.handleWrite(this.writeContext);

		// Invoke idle that is not timed out
		this.handler.handleIdleConnection(this.idleContext);

		// Invoke idle that times out connection
		this.handler.handleIdleConnection(this.idleContext);

		// Verify mocks
		this.verifyMockObjects();
	}

}
