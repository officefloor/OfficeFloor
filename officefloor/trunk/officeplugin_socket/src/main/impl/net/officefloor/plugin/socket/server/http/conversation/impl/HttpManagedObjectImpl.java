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
package net.officefloor.plugin.socket.server.http.conversation.impl;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
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
	 * {@link HttpRequest}.
	 */
	private final HttpRequestImpl request;

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponseImpl response;

	/**
	 * Initiate.
	 *
	 * @param request
	 *            {@link HttpRequestImpl}.
	 * @param response
	 *            {@link HttpResponseImpl}.
	 */
	public HttpManagedObjectImpl(HttpRequestImpl request,
			HttpResponseImpl response) {
		this.request = request;
		this.response = response;
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
	public void cleanup() {
		// TODO Implement HttpManagedObject.cleanup
		throw new UnsupportedOperationException("HttpManagedObject.cleanup");
	}

	/*
	 * ================== ServerHttpConnection =========================
	 */

	@Override
	public synchronized HttpRequest getHttpRequest() {
		return this.request;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return this.response;
	}

	/*
	 * ================== EscalationHandler =============================
	 */

	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		// Indicate failure on handling request
		this.response.flagFailure(escalation);
	}

}