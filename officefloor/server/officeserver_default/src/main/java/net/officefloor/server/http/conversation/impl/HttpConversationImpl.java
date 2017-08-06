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
package net.officefloor.server.http.conversation.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.clock.HttpServerClock;
import net.officefloor.server.http.conversation.HttpConversation;
import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.http.conversation.HttpManagedObject;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.protocol.Connection;

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
	 * {@link HttpManagedObjectImpl} instances currently being processed for the
	 * {@link Connection}.
	 */
	private final Queue<HttpManagedObjectImpl> managedObjects = new LinkedList<HttpManagedObjectImpl>();

	/**
	 * Server name.
	 */
	private final String serverName;

	/**
	 * Size of the send buffers.
	 */
	private final int sendBufferSize;

	/**
	 * Default {@link Charset} for the {@link HttpResponse} entity.
	 */
	private final Charset defaultCharset;

	/**
	 * Flags whether to send the stack trace on failure.
	 */
	private final boolean isSendStackTraceOnFailure;

	/**
	 * {@link HttpServerClock}.
	 */
	private final HttpServerClock clock;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param serverName
	 *            Server name.
	 * @param sendBufferSize
	 *            Size of the send buffers.
	 * @param defaultCharset
	 *            Default {@link Charset} for the {@link HttpResponse} entity.
	 * @param isSendStackTraceOnFailure
	 *            Flags whether to send the stack trace on failure.
	 * @param clock
	 *            {@link HttpServerClock}.
	 */
	public HttpConversationImpl(Connection connection, String serverName, int sendBufferSize, Charset defaultCharset,
			boolean isSendStackTraceOnFailure, HttpServerClock clock) {
		this.connection = connection;
		this.serverName = serverName;
		this.sendBufferSize = sendBufferSize;
		this.defaultCharset = defaultCharset;
		this.isSendStackTraceOnFailure = isSendStackTraceOnFailure;
		this.clock = clock;
	}

	/**
	 * Obtains the server name.
	 * 
	 * @return Server name.
	 */
	String getServerName() {
		return this.serverName;
	}

	/**
	 * Obtains the send buffer size.
	 * 
	 * @return Send buffer size.
	 */
	int getSendBufferSize() {
		return this.sendBufferSize;
	}

	/**
	 * Obtains the default {@link Charset}.
	 * 
	 * @return Default {@link Charset}.
	 */
	Charset getDefaultCharset() {
		return this.defaultCharset;
	}

	/**
	 * Obtains the {@link HttpServerClock}.
	 * 
	 * @return {@link HttpServerClock}.
	 */
	HttpServerClock getHttpServerClock() {
		return this.clock;
	}

	/**
	 * Queues complete {@link HttpResponse} instances for sending.
	 * 
	 * @throws IOException
	 *             If fails to send complete {@link HttpResponse} instances.
	 */
	void queueCompleteResponses() throws IOException {

		synchronized (this) {

			// Send the complete responses in order registered
			for (Iterator<HttpManagedObjectImpl> iterator = this.managedObjects.iterator(); iterator.hasNext();) {
				HttpManagedObjectImpl managedObject = iterator.next();

				// Attempt to queue response for sending
				if (!managedObject.queueHttpResponseIfComplete()) {
					// Response not yet complete to send
					return; // send no further responses
				}

				// Response sent, so remove managed object
				iterator.remove();
			}
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
	public HttpManagedObject addRequest(String method, String requestURI, String httpVersion,
			HttpRequestHeaders headers, HttpEntity entity) {

		// Create the request
		HttpRequestImpl request = new HttpRequestImpl(method, requestURI, httpVersion, headers, entity);

		// Create the HTTP managed object
		HttpManagedObjectImpl managedObject = new HttpManagedObjectImpl(this.connection, this, request);

		// Register the HTTP managed object
		synchronized (this) {
			this.managedObjects.add(managedObject);
		}

		// Return the managed object
		return managedObject;
	}

	@Override
	public void parseFailure(HttpRequestParseException failure, boolean isCloseConnection) throws IOException {

		// Create response for parse failure
		HttpResponseImpl response = new HttpResponseImpl(this, this.connection, HttpVersion.HTTP_1_0);

		// Create the HTTP managed object
		HttpManagedObjectImpl managedObject = new HttpManagedObjectImpl(response);

		// Register the HTTP managed object
		synchronized (this) {
			this.managedObjects.add(managedObject);
		}

		// Send the failure
		response.sendFailure(failure);
	}

	@Override
	public void closeConnection() throws IOException {
		// Close the connection
		this.connection.close();
	}

}