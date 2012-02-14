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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.InputBufferStream;

/**
 * Manages the HTTP conversation on a {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpConversationImpl implements HttpConversation {

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link BufferSquirtFactory}.
	 */
	private final BufferSquirtFactory bufferSquirtFactory;

	/**
	 * {@link HttpManagedObjectImpl} instances.
	 */
	private final List<HttpManagedObjectImpl> managedObjects = new LinkedList<HttpManagedObjectImpl>();

	/**
	 * Flags whether to send the stack trace on failure.
	 */
	private final boolean isSendStackTraceOnFailure;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param bufferSquirtFactory
	 *            {@link BufferSquirtFactory}.
	 * @param isSendStackTraceOnFailure
	 *            Flags whether to send the stack trace on failure.
	 */
	public HttpConversationImpl(Connection connection,
			BufferSquirtFactory bufferSquirtFactory,
			boolean isSendStackTraceOnFailure) {
		this.connection = connection;
		this.bufferSquirtFactory = bufferSquirtFactory;
		this.isSendStackTraceOnFailure = isSendStackTraceOnFailure;
	}

	/**
	 * Sends complete {@link HttpResponse} instances.
	 * 
	 * @throws IOException
	 *             If fails to send complete {@link HttpResponse} instances.
	 */
	void sendCompleteResponses() throws IOException {
		// Send the complete responses in order registered
		for (Iterator<HttpManagedObjectImpl> iterator = this.managedObjects
				.iterator(); iterator.hasNext();) {
			HttpManagedObjectImpl managedObject = iterator.next();

			// Attempt to send response
			if (!managedObject.attemptSendResponse()) {
				// Response not yet complete to send
				return; // send no further responses
			}

			// Response sent, so remove managed object
			iterator.remove();
		}
	}

	/**
	 * Flags to send stack trace on failure.
	 * 
	 * @return <code>true</code> to send the stack trace on a failure.
	 */
	boolean isSendStackTraceOnFailure() {
		return this.isSendStackTraceOnFailure;
	}

	/*
	 * ======================= HttpConversation ============================
	 */

	@Override
	public HttpManagedObject addRequest(String method, String requestURI,
			String httpVersion, List<HttpHeader> headers, InputBufferStream body) {

		// Create the request
		HttpRequestImpl request = new HttpRequestImpl(method, requestURI,
				httpVersion, headers, body);

		// Create the corresponding response (keeping connection open)
		HttpResponseImpl response = new HttpResponseImpl(this, this.connection,
				this.bufferSquirtFactory, httpVersion, false);

		// Create the HTTP managed object
		HttpManagedObjectImpl managedObject = new HttpManagedObjectImpl(
				this.connection, request, response);

		// Register the HTTP managed object
		this.managedObjects.add(managedObject);

		// Return the managed object
		return managedObject;
	}

	@Override
	public void parseFailure(HttpRequestParseException failure,
			boolean isCloseConnection) throws IOException {

		// Create response for parse failure
		HttpResponseImpl response = new HttpResponseImpl(this, this.connection,
				this.bufferSquirtFactory, "HTTP/1.0", isCloseConnection);

		// Create the HTTP managed object
		HttpManagedObjectImpl managedObject = new HttpManagedObjectImpl(
				response);

		// Register the HTTP managed object
		this.managedObjects.add(managedObject);

		// Send the failure
		response.sendFailure(failure);
	}

}