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

package net.officefloor.plugin.socket.server.http.protocol;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.HeartBeatContext;
import net.officefloor.plugin.socket.server.protocol.ReadContext;
import net.officefloor.plugin.stream.NioInputStream;
import net.officefloor.plugin.stream.impl.NioInputStreamImpl;

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
	 * Mock {@link HeartBeatContext}.
	 */
	private HeartBeatContext idleContext = this
			.createMock(HeartBeatContext.class);

	/**
	 * Mock read data.
	 */
	private byte[] readData = new byte[] { 1 };

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
	 * Mock {@link ManagedObjectExecuteContext}.
	 */
	@SuppressWarnings("unchecked")
	private ManagedObjectExecuteContext<Indexed> executeContext = this
			.createMock(ManagedObjectExecuteContext.class);

	/**
	 * {@link HttpConnectionHandler} being tested.
	 */
	public HttpConnectionHandler handler = new HttpConnectionHandler(
			this.server, this.conversation, this.parser, 255,
			CONNECTION_TIMEOUT);

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
		final NioInputStream entity = new NioInputStreamImpl();
		final HttpManagedObject managedObject = this
				.createMock(HttpManagedObject.class);

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(),
				System.currentTimeMillis());
		this.recordReturn(this.readContext,
				this.readContext.getContextObject(), tempBuffer);
		this.recordReturn(this.readContext, this.readContext.getData(),
				this.readData);
		this.recordReturn(this.parser,
				this.parser.parse(this.readData, tempBuffer), true);
		this.recordReturn(this.parser, this.parser.getMethod(), method);
		this.recordReturn(this.parser, this.parser.getRequestURI(), requestURI);
		this.recordReturn(this.parser, this.parser.getHttpVersion(),
				httpVersion);
		this.recordReturn(this.parser, this.parser.getHeaders(), headers);
		this.recordReturn(this.parser, this.parser.getEntity(), entity);
		this.parser.reset();
		this.recordReturn(this.conversation, this.conversation.addRequest(
				method, requestURI, httpVersion, headers, entity),
				managedObject);
		this.server.processRequest(this.handler, managedObject);

		// Replay mocks
		this.replayMockObjects();

		// Handle the read
		this.handler.handleRead(this.readContext);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensures correctly handles {@link HttpRequestParseException}.
	 */
	public void testParseFailure() throws Exception {

		// Additional test objects
		final char[] tempBuffer = new char[255];
		final HttpRequestParseException failure = new HttpRequestParseException(
				HttpStatus.SC_BAD_REQUEST, "Parse Failure");

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(),
				System.currentTimeMillis());
		this.recordReturn(this.readContext,
				this.readContext.getContextObject(), tempBuffer);
		this.recordReturn(this.readContext, this.readContext.getData(),
				this.readData);
		this.parser.parse(this.readData, tempBuffer);
		this.control(this.parser).setThrowable(failure);
		this.conversation.parseFailure(failure, true);

		// Replay mocks
		this.replayMockObjects();

		// Handle the read
		this.handler.handleRead(this.readContext);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensures checks idle time.
	 */
	public void testIdleByRead() throws IOException {

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), 1000);
		this.recordReturn(this.idleContext, this.idleContext.getTime(), 1000);

		// Replay mocks
		this.replayMockObjects();

		// Invoke read to set last interaction time
		this.handler.handleRead(this.readContext);

		// Handle the idle (not timing out)
		this.handler.handleHeartbeat(this.idleContext);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensures checks idle time.
	 */
	public void testIdleByWrite() {

		// Record actions
		// TODO record writing
		fail("TODO record writing");
		this.recordReturn(this.idleContext, this.idleContext.getTime(), 1000);

		// Replay mocks
		this.replayMockObjects();

		// Invoke write to set last interaction time
		// TODO undertake write

		// Handle the idle (not timing out)
		this.handler.handleHeartbeat(this.idleContext);

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
		this.recordReturn(this.readContext, this.readContext.getTime(),
				START_TIME);
		this.recordReturn(this.idleContext, this.idleContext.getTime(),
				FIRST_IDLE_TIME);
		this.recordReturn(this.idleContext, this.idleContext, SECOND_IDLE_TIME);
		this.conversation.getConnection().close();

		// Replay mocks
		this.replayMockObjects();

		// Invoke read to set last interaction time
		this.handler.handleRead(this.readContext);

		// Invoke idle that is not timed out
		this.handler.handleHeartbeat(this.idleContext);

		// Invoke idle that times out connection
		this.handler.handleHeartbeat(this.idleContext);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensure closes {@link Connection} if established and no data received for
	 * long period of time.
	 */
	public void testIdleNoData() {

		// Record actions
		final long START_TIME = System.currentTimeMillis();
		final long TIMEOUT_IDLE_TIME = START_TIME + CONNECTION_TIMEOUT;
		this.recordReturn(this.idleContext, this.idleContext.getTime(),
				START_TIME);
		this.recordReturn(this.idleContext, this.idleContext, TIMEOUT_IDLE_TIME);
		this.conversation.getConnection().close();

		// Replay mocks
		this.replayMockObjects();

		// Invoke idle waiting on data
		this.handler.handleHeartbeat(this.idleContext);

		// Invoke idle still waiting on data and times out
		this.handler.handleHeartbeat(this.idleContext);

		// Verify mocks
		this.verifyMockObjects();
	}

}