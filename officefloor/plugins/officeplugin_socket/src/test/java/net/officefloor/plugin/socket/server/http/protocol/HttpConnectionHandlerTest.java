/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpEntity;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpManagedObjectImpl;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.HeartBeatContext;
import net.officefloor.plugin.socket.server.protocol.ReadContext;

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
	private HeartBeatContext idleContext = this.createMock(HeartBeatContext.class);

	/**
	 * Mock read data.
	 */
	private byte[] readData = new byte[] { 1 };

	/**
	 * {@link HttpCommunicationProtocol}.
	 */
	private HttpCommunicationProtocol communicationProtocol = new HttpCommunicationProtocol();

	/**
	 * Mock {@link HttpConversation}.
	 */
	private HttpConversation conversation = this.createMock(HttpConversation.class);

	/**
	 * Mock {@link HttpRequestParser}.
	 */
	private HttpRequestParser parser = this.createMock(HttpRequestParser.class);

	/**
	 * Mock {@link ManagedObjectExecuteContext}.
	 */
	@SuppressWarnings("unchecked")
	private ManagedObjectExecuteContext<Indexed> executeContext = this.createMock(ManagedObjectExecuteContext.class);

	/**
	 * {@link HttpConnectionHandler} being tested.
	 */
	public HttpConnectionHandler handler = new HttpConnectionHandler(this.communicationProtocol, this.conversation,
			this.parser, CONNECTION_TIMEOUT);

	@Override
	protected void setUp() throws Exception {
		this.communicationProtocol.setManagedObjectExecuteContext(this.executeContext);
	}

	/**
	 * Ensures successful read of {@link HttpRequest}.
	 */
	public void testSuccessfulRead() throws Exception {

		// Additional test objects
		final String method = "GET";
		final String requestURI = "/path";
		final String httpVersion = "HTTP/1.1";
		final List<HttpHeader> headers = new LinkedList<HttpHeader>();
		final HttpEntity entity = this.createMock(HttpEntity.class);
		final HttpManagedObjectImpl managedObject = new HttpManagedObjectImpl(null);

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), System.currentTimeMillis());
		this.recordReturn(this.readContext, this.readContext.getData(), this.readData);
		this.recordReturn(this.parser, this.parser.parse(this.readData, 0), true);
		this.recordReturn(this.parser, this.parser.getMethod(), method);
		this.recordReturn(this.parser, this.parser.getRequestURI(), requestURI);
		this.recordReturn(this.parser, this.parser.getHttpVersion(), httpVersion);
		this.recordReturn(this.parser, this.parser.getHeaders(), headers);
		this.recordReturn(this.parser, this.parser.getEntity(), entity);
		this.parser.reset();
		this.recordReturn(this.conversation,
				this.conversation.addRequest(method, requestURI, httpVersion, headers, entity), managedObject);
		this.executeContext.invokeProcess(0, managedObject, managedObject, 0, managedObject.getFlowCallback());
		this.recordReturn(this.parser, this.parser.nextByteToParseIndex(), -1);

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
		final HttpRequestParseException failure = new HttpRequestParseException(HttpStatus.SC_BAD_REQUEST,
				"Parse Failure");

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), System.currentTimeMillis());
		this.recordReturn(this.readContext, this.readContext.getData(), this.readData);
		this.parser.parse(this.readData, 0);
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
	public void testIdleByRead() throws Exception {

		final byte[] data = new byte[0];

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getTime(), 1000);
		this.recordReturn(this.readContext, this.readContext.getData(), data);
		this.recordReturn(this.parser, this.parser.parse(data, 0), false);
		this.recordReturn(this.parser, this.parser.nextByteToParseIndex(), -1);
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
	 * Ensures closes {@link Connection} on {@link Connection} being too long
	 * idle.
	 */
	public void testIdleTooLong() throws Exception {

		final byte[] data = new byte[0];

		// Record actions
		final long START_TIME = System.currentTimeMillis();
		final long FIRST_IDLE_TIME = START_TIME + CONNECTION_TIMEOUT - 1;
		final long SECOND_IDLE_TIME = START_TIME + CONNECTION_TIMEOUT;
		this.recordReturn(this.readContext, this.readContext.getTime(), START_TIME);
		this.recordReturn(this.readContext, this.readContext.getData(), data);
		this.recordReturn(this.parser, this.parser.parse(data, 0), false);
		this.recordReturn(this.parser, this.parser.nextByteToParseIndex(), -1);
		this.recordReturn(this.idleContext, this.idleContext.getTime(), FIRST_IDLE_TIME);
		this.recordReturn(this.idleContext, this.idleContext, SECOND_IDLE_TIME);
		this.conversation.closeConnection();

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
	public void testIdleNoData() throws Exception {

		// Record actions
		final long START_TIME = System.currentTimeMillis();
		final long TIMEOUT_IDLE_TIME = START_TIME + CONNECTION_TIMEOUT;
		this.recordReturn(this.idleContext, this.idleContext.getTime(), START_TIME);
		this.recordReturn(this.idleContext, this.idleContext, TIMEOUT_IDLE_TIME);
		this.conversation.closeConnection();

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