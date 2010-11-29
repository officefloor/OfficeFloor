/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
import java.net.InetSocketAddress;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;

/**
 * {@link ManagedObject} for the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpManagedObjectImpl implements HttpManagedObject,
		ServerHttpConnection, EscalationHandler {

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequestImpl request;

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponseImpl response;

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
	}

	/**
	 * Sends the {@link HttpResponse} if it is completed.
	 * 
	 * @return <code>true</code> if {@link HttpResponse} is completed and was
	 *         added to the {@link Connection} output to be sent.
	 * @throws IOException
	 *             If fails to send the {@link HttpResponse}.
	 */
	boolean attemptSendResponse() throws IOException {
		// Attempt to send the response
		if (this.response.attemptSendResponse()) {
			// Response sent, so clean up request (if available)
			if (this.request != null) {
				this.request.cleanup();
			}
			return true;
		}

		// Response not sent
		return false;
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
		// Close the response to trigger sending it
		this.response.getBody().close();
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

	/*
	 * ================== EscalationHandler =============================
	 */

	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		// Send failure on handling request
		this.response.sendFailure(escalation);
	}

}