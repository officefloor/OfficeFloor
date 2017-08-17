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
import java.util.function.Supplier;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.conversation.HttpConversation;
import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.http.conversation.HttpManagedObject;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.protocol.Connection;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * Manages the HTTP conversation on a {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // TODO merge into HttpConnectionHandler
public class HttpConversationImpl implements HttpConversation {

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * {@link Flow} index to handle processing {@link HttpRequest}.
	 */
	private final int requestHandlingFlowIndex;

	/**
	 * {@link HttpManagedObjectImpl} instances currently being processed for the
	 * {@link Connection}.
	 */
	private final Queue<HttpManagedObjectImpl> managedObjects = new LinkedList<HttpManagedObjectImpl>();

	/**
	 * Flags whether to send the stack trace on failure.
	 */
	private final boolean isSendStackTraceOnFailure;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @param requestHandlingFlowIndex
	 *            {@link Flow} index to handle processing {@link HttpRequest}.
	 * @param isSendStackTraceOnFailure
	 *            Flags whether to send the stack trace on failure.
	 */
	public HttpConversationImpl(Connection connection, ManagedObjectExecuteContext<Indexed> executeContext,
			int requestHandlingFlowIndex, boolean isSendStackTraceOnFailure) {
		this.connection = connection;
		this.executeContext = executeContext;
		this.requestHandlingFlowIndex = requestHandlingFlowIndex;
		this.isSendStackTraceOnFailure = isSendStackTraceOnFailure;
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
	public void serviceRequest(Supplier<HttpMethod> methodSupplier, Supplier<String> requestUriSupplier,
			HttpVersion httpVersion, NonMaterialisedHttpHeaders headers, ByteSequence entity) {

		// Create the request
		HttpRequestImpl request = new HttpRequestImpl(method, requestURI, httpVersion, headers, entity);

		// Create the HTTP managed object
		HttpManagedObjectImpl managedObject = new HttpManagedObjectImpl(this.connection, this, request);

		// Register the HTTP managed object
		synchronized (this) {
			this.managedObjects.add(managedObject);
		}

		// Invoke process to service the request
		this.executeContext.invokeProcess(this.requestHandlingFlowIndex, null, managedObject, 0,
				managedObject.getFlowCallback());
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