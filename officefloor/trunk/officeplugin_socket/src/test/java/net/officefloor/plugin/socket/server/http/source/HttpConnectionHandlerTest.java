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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.parse.ParseException;
import net.officefloor.plugin.stream.InputBufferStream;

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

	/**
	 * Mock {@link InputBufferStream}.
	 */
	private InputBufferStream inputBufferStream = this
			.createMock(InputBufferStream.class);

	/**
	 * Mock {@link HttpConversation}.
	 */
	private HttpConversation conversation = this
			.createMock(HttpConversation.class);

	/**
	 * Mock {@link HttpRequestParser}.
	 */
	private HttpRequestParser parser = this.createMock(HttpRequestParser.class);

	/**
	 * {@link HttpConnectionHandler} being tested.
	 */
	public HttpConnectionHandler handler = new HttpConnectionHandler(
			this.conversation, this.parser, 255, CONNECTION_TIMEOUT);

	/**
	 * Ensures successful read of {@link HttpRequest}.
	 */
	public void testSuccessfulRead() throws Exception {

		// Additional test objects
		final char[] tempBuffer = new char[255];
		final String method = "GET";
		final String requestURI = "/path";
		final String httpVersion = "HTTP/1.1";
		final List<HttpHeader> headers = new LinkedList<HttpHeader>();
		final InputBufferStream body = this.createMock(InputBufferStream.class);
		final HttpManagedObject managedObject = this
				.createMock(HttpManagedObject.class);

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), System
				.currentTimeMillis());
		this.recordReturn(this.readContext,
				this.readContext.getContextObject(), tempBuffer);
		this.recordReturn(this.readContext, this.readContext
				.getInputBufferStream(), this.inputBufferStream);
		this.recordReturn(this.parser, this.parser.parse(
				this.inputBufferStream, tempBuffer), true);
		this.recordReturn(this.parser, this.parser.getMethod(), method);
		this.recordReturn(this.parser, this.parser.getRequestURI(), requestURI);
		this.recordReturn(this.parser, this.parser.getHttpVersion(),
				httpVersion);
		this.recordReturn(this.parser, this.parser.getHeaders(), headers);
		this.recordReturn(this.parser, this.parser.getBody(), body);
		this.parser.reset();
		this.recordReturn(this.conversation, this.conversation.addRequest(
				method, requestURI, httpVersion, headers, body), managedObject);
		this.readContext.processRequest(managedObject);
		this.recordReturn(this.parser, this.parser.parse(
				this.inputBufferStream, tempBuffer), false);

		// Replay mocks
		this.replayMockObjects();

		// Handle the read
		this.handler.handleRead(this.readContext);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensures correctly handles {@link ParseException}.
	 */
	public void testParseFailure() throws Exception {

		// Additional test objects
		final char[] tempBuffer = new char[255];
		final ParseException failure = new ParseException(HttpStatus._400,
				"Parse Failure");

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), System
				.currentTimeMillis());
		this.recordReturn(this.readContext,
				this.readContext.getContextObject(), tempBuffer);
		this.recordReturn(this.readContext, this.readContext
				.getInputBufferStream(), this.inputBufferStream);
		this.parser.parse(this.inputBufferStream, tempBuffer);
		this.control(this.parser).setThrowable(failure);
		this.conversation.parseFailure(failure);
		this.readContext.setCloseConnection(true);

		// Replay mocks
		this.replayMockObjects();

		// Handle the read
		this.handler.handleRead(this.readContext);

		// Verify mocks
		this.verifyMockObjects();
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