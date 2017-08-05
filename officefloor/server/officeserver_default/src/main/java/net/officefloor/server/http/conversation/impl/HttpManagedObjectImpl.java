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
import java.io.Serializable;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.conversation.HttpConversation;
import net.officefloor.server.http.conversation.HttpManagedObject;
import net.officefloor.server.http.protocol.Connection;

/**
 * {@link ManagedObject} for the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpManagedObjectImpl implements HttpManagedObject, ServerHttpConnection, FlowCallback {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(HttpManagedObjectImpl.class.getName());

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link HttpConversation}.
	 */
	private final HttpConversationImpl conversation;

	/**
	 * {@link HttpRequest}.
	 */
	private volatile HttpRequestImpl request;

	/**
	 * {@link HttpResponse}.
	 */
	private volatile HttpResponseImpl response;

	/**
	 * {@link HttpMethod} sent by the client.
	 */
	private final HttpMethod clientHttpMethod;

	/**
	 * Initiate to process the {@link HttpRequest} by populating the
	 * {@link HttpResponse}.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param conversation
	 *            {@link HttpConversationImpl}.
	 * @param request
	 *            {@link HttpRequestImpl}.
	 */
	public HttpManagedObjectImpl(Connection connection, HttpConversationImpl conversation, HttpRequestImpl request) {
		this.connection = connection;
		this.conversation = conversation;
		this.request = request;
		this.response = new HttpResponseImpl(this.conversation, this.connection, request.getHttpVersion());

		// Keep track of the client HTTP method
		this.clientHttpMethod = request.getHttpMethod();
	}

	/**
	 * Initiate with {@link HttpResponse} ready to be sent.
	 * 
	 * @param completedResponse
	 *            {@link HttpResponse} ready to be sent.
	 */
	public HttpManagedObjectImpl(HttpResponseImpl completedResponse) {
		this.connection = null;
		this.conversation = null;
		this.request = null;
		this.response = completedResponse;
		this.clientHttpMethod = null;
	}

	/**
	 * <p>
	 * Queues the {@link HttpResponse} for sending if it is complete.
	 * 
	 * @return <code>true</code> should the {@link HttpResponse} be queued for
	 *         sending.
	 * @throws IOException
	 *             If fails writing {@link HttpResponse} if no need to queue.
	 */
	boolean queueHttpResponseIfComplete() throws IOException {
		return this.response.queueHttpResponseIfComplete();
	}

	/*
	 * =============== HttpManagedObject =================================
	 */

	@Override
	public Object getObject() throws Exception {
		return this;
	}

	@Override
	public FlowCallback getFlowCallback() {
		return this;
	}

	@Override
	public ServerHttpConnection getServerHttpConnection() {
		return this;
	}

	@Override
	public void cleanup(CleanupEscalation[] cleanupEscalations) throws IOException {
		// Report cleanup escalations
		this.response.handleCleanupEscalations(cleanupEscalations);
	}

	/*
	 * ================== ServerHttpConnection =========================
	 */

	@Override
	public HttpRequest getHttpRequest() {
		return this.request;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return this.response;
	}

	@Override
	public boolean isSecure() {
		return this.connection.isSecure();
	}

	@Override
	public Serializable exportState() throws IOException {

		// Obtain the request and response momentos
		Serializable requestMomento = this.request.exportState();
		Serializable responseMomento = this.response.exportState();

		// Create and return the state momento
		return new StateMomento(requestMomento, responseMomento);
	}

	@Override
	public void importState(Serializable momento) {

		// Ensure valid momento
		if (!(momento instanceof StateMomento)) {
			throw new IllegalArgumentException("Invalid momento for " + ServerHttpConnection.class.getSimpleName());
		}
		StateMomento state = (StateMomento) momento;

		// Override the request with momento state
		String requestHttpVersion = this.request.getHttpVersion().getName();
		this.request = new HttpRequestImpl(requestHttpVersion, state.requestMomento);

		// Override the response with momento state
		HttpVersion responseHttpVersion = this.response.getHttpVersion();
		this.response = new HttpResponseImpl(this.conversation, this.connection, responseHttpVersion,
				state.responseMomento);
	}

	@Override
	public HttpMethod getHttpMethod() {
		return this.clientHttpMethod;
	}

	/*
	 * ================== FlowCallback =============================
	 */

	@Override
	public void run(Throwable escalation) throws Throwable {
		try {

			// Send failure on handling request
			if (escalation != null) {
				this.response.sendFailure(escalation);
				return;
			}

		} catch (ClosedChannelException ex) {
			// Can not send failure, as connection closed
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "Failed sending escalation over closed connection", ex);
			}

		} catch (IOException ex) {
			// Failed to send failure
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.log(Level.INFO, "Unable to send HTTP failure message", ex);
			}
		}

		// Send the response
		this.response.send();
	}

	/**
	 * Momento for the state of this {@link ServerHttpConnection}.
	 */
	private static class StateMomento implements Serializable {

		/**
		 * Momento for the {@link HttpRequest}.
		 */
		private final Serializable requestMomento;

		/**
		 * Momento for the {@link HttpResponse}.
		 */
		private final Serializable responseMomento;

		/**
		 * Initiate.
		 * 
		 * @param requestMomento
		 *            Momento for the {@link HttpRequest}.
		 * @param responseMomento
		 *            Momento for the {@link HttpResponse}.
		 */
		public StateMomento(Serializable requestMomento, Serializable responseMomento) {
			this.requestMomento = requestMomento;
			this.responseMomento = responseMomento;
		}
	}

}