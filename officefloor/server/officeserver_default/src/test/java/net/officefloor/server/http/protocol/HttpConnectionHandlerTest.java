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
package net.officefloor.server.http.protocol;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.conversation.HttpConversation;
import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.http.conversation.impl.HttpManagedObjectImpl;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.parse.HttpRequestParser;

/**
 * Tests the {@link HttpConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpConnectionHandlerTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link ReadContext}.
	 */
	private ReadContext readContext = this.createMock(ReadContext.class);

	/**
	 * Mock read data.
	 */
	private byte[] readData = new byte[] { 1 };

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
	public HttpConnectionHandler handler = new HttpConnectionHandler(this.conversation, this.parser,
			this.executeContext, 0);

	/**
	 * Ensures successful read of {@link HttpRequest}.
	 */
	public void testSuccessfulRead() throws Exception {

		// Additional test objects
		final String method = "GET";
		final String requestURI = "/path";
		final String httpVersion = "HTTP/1.1";
		final HttpRequestHeaders headers = this.createMock(HttpRequestHeaders.class);
		final HttpEntity entity = this.createMock(HttpEntity.class);
		final HttpManagedObjectImpl managedObject = new HttpManagedObjectImpl(null);

		// Record actions
		this.recordReturn(this.readContext, this.readContext.getData(), this.readData);
		this.recordReturn(this.parser, this.parser.parse(this.readData, 0), true);
		this.recordReturn(this.parser, this.parser.getMethod(), method);
		this.recordReturn(this.parser, this.parser.getRequestURI(), requestURI);
		this.recordReturn(this.parser, this.parser.getVersion(), httpVersion);
		this.recordReturn(this.parser, this.parser.getHeaders(), headers);
		this.recordReturn(this.parser, this.parser.getEntity(), entity);
		this.parser.reset();
		this.recordReturn(this.conversation,
				this.conversation.serviceRequest(method, requestURI, httpVersion, headers, entity), managedObject);
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
		final HttpRequestParseException failure = new HttpRequestParseException(HttpStatus.BAD_REQUEST);

		// Record actions
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

}