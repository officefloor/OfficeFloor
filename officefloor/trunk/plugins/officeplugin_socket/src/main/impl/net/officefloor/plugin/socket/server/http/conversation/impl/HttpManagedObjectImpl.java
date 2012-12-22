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
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.stream.impl.NotAllDataAvailableException;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

/**
 * {@link ManagedObject} for the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpManagedObjectImpl implements HttpManagedObject,
		ServerHttpConnection, EscalationHandler {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(HttpManagedObjectImpl.class.getName());

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link HttpRequest}.
	 */
	private volatile HttpRequestImpl request;

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponseImpl response;

	/**
	 * HTTP method sent by the client.
	 */
	private final String clientHttpMethod;

	/**
	 * Initiate to process the {@link HttpRequest} by populating the
	 * {@link HttpResponse}.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param request
	 *            {@link HttpRequestImpl}.
	 * @param response
	 *            {@link HttpResponseImpl}.
	 */
	public HttpManagedObjectImpl(Connection connection,
			HttpRequestImpl request, HttpResponseImpl response) {
		this.connection = connection;
		this.request = request;
		this.response = response;

		// Keep track of the client HTTP method
		this.clientHttpMethod = request.getMethod();
	}

	/**
	 * Initiate with {@link HttpResponse} ready to be sent.
	 * 
	 * @param completedResponse
	 *            {@link HttpResponse} ready to be sent.
	 */
	public HttpManagedObjectImpl(HttpResponseImpl completedResponse) {
		this.connection = null;
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
	public EscalationHandler getEscalationHandler() {
		return this;
	}

	@Override
	public ServerHttpConnection getServerHttpConnection() {
		return this;
	}

	@Override
	public void cleanup() throws IOException {
		// Ensure response is triggered for sending
		this.response.send();
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
	public InetSocketAddress getLocalAddress() {
		return this.connection.getLocalAddress();
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return this.connection.getRemoteAddress();
	}

	@Override
	public Serializable exportState() throws NotAllDataAvailableException {
		return new StateMomento(this.request.getMethod(),
				this.request.getRequestURI(), this.request.getHeaders(),
				this.request.exportEntityState());
	}

	@Override
	public void importState(Serializable momento) {

		// Ensure valid momento
		if (!(momento instanceof StateMomento)) {
			throw new IllegalArgumentException("Invalid momento for "
					+ ServerHttpConnection.class.getSimpleName());
		}
		StateMomento state = (StateMomento) momento;

		// Override the request with momento state
		ServerInputStreamImpl entityStream = new ServerInputStreamImpl(
				new Object(), state.entityMomento);
		HttpRequestImpl overrideRequest = new HttpRequestImpl(state.method,
				state.requestUri, this.request.getVersion(), state.headers,
				new HttpEntityImpl(entityStream));
		this.request = overrideRequest;
	}

	@Override
	public String getHttpMethod() {
		return this.clientHttpMethod;
	}

	/*
	 * ================== EscalationHandler =============================
	 */

	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		try {

			// Send failure on handling request
			this.response.sendFailure(escalation);

		} catch (ClosedChannelException ex) {
			// Can not send failure, as connection closed
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE,
						"Failed sending escalation over closed connection", ex);
			}

		} catch (IOException ex) {
			// Failed to send failure
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.log(Level.INFO, "Unable to send HTTP failure message",
						ex);
			}
		}
	}

	/**
	 * Momento for the state of this {@link ServerHttpConnection}.
	 */
	private static class StateMomento implements Serializable {

		/**
		 * HTTP method.
		 */
		private final String method;

		/**
		 * Request URI.
		 */
		private final String requestUri;

		/**
		 * {@link HttpHeader} instances.
		 */
		private final List<HttpHeader> headers;

		/**
		 * Momento for the state of the entity.
		 */
		private final Serializable entityMomento;

		/**
		 * Initiate.
		 * 
		 * @param method
		 *            Method.
		 * @param requestUri
		 *            Request URI.
		 * @param headers
		 *            {@link HttpHeader} instances.
		 * @param entityMomento
		 *            Entity state momento.
		 */
		public StateMomento(String method, String requestUri,
				List<HttpHeader> headers, Serializable entityMomento) {
			this.method = method;
			this.requestUri = requestUri;
			this.headers = headers;
			this.entityMomento = entityMomento;
		}
	}

}