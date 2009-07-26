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
package net.officefloor.plugin.socket.server.http.source;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.socket.server.http.parse.HttpHeader;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.parse.ParseException;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;

/**
 * Tests the {@link HttpConnectionHandler}.
 *
 * @author Daniel Sagenschneider
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

		// Source with specified timeout
		HttpServerSocketManagedObjectSource source = new HttpServerSocketManagedObjectSource();
		source.connectionTimeout = CONNECTION_TIMEOUT;

		// Create the handler to test
		this.handler = new HttpConnectionHandler(source, this.connection);
	}

	/**
	 * Ensures successful read of HTTP request.
	 */
	public void testSuccessfulRead() throws Exception {

		// Create mocks
		InputBufferStream requestInputBufferStream = this
				.createMock(InputBufferStream.class);

		// Obtain the request and input stream to its contents
		String requestText = "GET /path HTTP/1.1\nhost: localhost\n\n";
		final byte[] request = UsAsciiUtil.convertToHttp(requestText);
		InputStream requestInputStream = new ByteArrayInputStream(request);

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), System
				.currentTimeMillis());
		this.recordReturn(this.readContext, this.readContext
				.getInputBufferStream(), requestInputBufferStream);
		this.recordReturn(requestInputBufferStream, requestInputBufferStream
				.getBrowseStream(), requestInputStream);
		this.readContext.requestReceived();

		// Replay mocks
		this.replayMockObjects();

		// Handle the read
		this.handler.handleRead(this.readContext);

		// Verify mocks
		this.verifyMockObjects();

		// Validate HTTP request
		HttpRequestParser parser = this.handler.getHttpRequestParser();
		assertEquals("Incorrect method", "GET", parser.getMethod());
		assertEquals("Incorrect path", "/path", parser.getRequestURI());
		assertEquals("Incorrect version", "HTTP/1.1", parser.getHttpVersion());
		assertEquals("Incorrect number of headers", 1, parser.getHeaders()
				.size());
		HttpHeader header = parser.getHeaders().get(0);
		assertEquals("Incorrect header name", "host", header.getName());
		assertEquals("Incorrect header value", "localhost", header.getValue());
		assertEquals("Incorrect body", 0, parser.getBody().available());
	}

	/**
	 * Ensures correctly handles an invalid HTTP request.
	 */
	public void testInvalidRead() throws Exception {

		// Create mocks
		InputBufferStream requestInputBufferStream = this
				.createMock(InputBufferStream.class);
		OutputBufferStream outputBufferStream = this
				.createMock(OutputBufferStream.class);

		// Create invalid request
		String invalidRequestText = "Invalid Request";
		final byte[] request = UsAsciiUtil.convertToHttp(invalidRequestText);
		InputStream requestInputStream = new ByteArrayInputStream(request);

		// Generate details of invalid response message
		byte[] badRequestResponseHeader = null;
		byte[] badRequestResponseDetail = null;
		try {
			new HttpRequestParserImpl(1024).parse(new BufferStreamImpl(
					ByteBuffer.wrap(request)).getInputBufferStream(),
					new char[255]);
			fail("Test invalid as should not be able to parse request");
		} catch (ParseException ex) {
			// Obtain detail of bad request
			badRequestResponseDetail = UsAsciiUtil.convertToUsAscii(ex
					.getMessage());

			// Provide header of bad request response
			int status = ex.getHttpStatus();
			String statusMsg = HttpStatus.getStatusMessage(status);
			badRequestResponseHeader = UsAsciiUtil.convertToHttp("HTTP/1.0 "
					+ status + " " + statusMsg + "\nContent-Length: "
					+ badRequestResponseDetail.length + "\n\n");
		}

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), System
				.currentTimeMillis());
		this.recordReturn(this.readContext, this.readContext
				.getInputBufferStream(), requestInputBufferStream);
		this.recordReturn(requestInputBufferStream, requestInputBufferStream
				.getBrowseStream(), requestInputStream);
		this.recordReturn(this.connection, this.connection
				.getOutputBufferStream(), outputBufferStream);
		outputBufferStream.write(badRequestResponseHeader, 0,
				badRequestResponseHeader.length);
		this.control(outputBufferStream).setMatcher(
				UsAsciiUtil.createUsAsciiMatcher());
		outputBufferStream.append(ByteBuffer.wrap(badRequestResponseDetail));
		this.control(outputBufferStream).setMatcher(
				UsAsciiUtil.createUsAsciiMatcher());
		this.readContext.requestReceived();

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
