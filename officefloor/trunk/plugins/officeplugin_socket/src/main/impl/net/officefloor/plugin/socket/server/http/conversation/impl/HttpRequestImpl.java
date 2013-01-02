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
package net.officefloor.plugin.socket.server.http.conversation.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.conversation.HttpEntity;
import net.officefloor.plugin.stream.ServerInputStream;
import net.officefloor.plugin.stream.impl.NotAllDataAvailableException;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

/**
 * {@link HttpRequest} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestImpl implements HttpRequest {

	/**
	 * Method.
	 */
	private final String method;

	/**
	 * Request URI.
	 */
	private final String requestURI;

	/**
	 * Version.
	 */
	private final String version;

	/**
	 * Headers.
	 */
	private final List<HttpHeader> headers;

	/**
	 * Entity.
	 */
	private final HttpEntity entity;

	/**
	 * Initiate.
	 * 
	 * @param method
	 *            Method.
	 * @param requestURI
	 *            Request URI.
	 * @param httpVersion
	 *            HTTP version.
	 * @param headers
	 *            {@link HttpHeader} instances.
	 * @param entity
	 *            {@link HttpEntity} to the entity.
	 */
	public HttpRequestImpl(String method, String requestURI,
			String httpVersion, List<HttpHeader> headers, HttpEntity entity) {
		this.method = method;
		this.requestURI = requestURI;
		this.version = httpVersion;
		this.headers = headers;
		this.entity = entity;
	}

	/**
	 * Initiate.
	 * 
	 * @param httpVersion
	 *            HTTP version.
	 * @param momento
	 *            Momento containing the state.
	 */
	public HttpRequestImpl(String httpVersion, Serializable momento) {

		// Ensure appropriate momento
		if (!(momento instanceof StateMomento)) {
			throw new IllegalArgumentException("Invalid momento for "
					+ HttpRequest.class.getSimpleName());
		}
		StateMomento state = (StateMomento) momento;

		// Load the state
		this.method = state.method;
		this.requestURI = state.requestURI;
		this.version = httpVersion;
		this.headers = state.headers;
		this.entity = new HttpEntityImpl(new ServerInputStreamImpl(
				new Object(), state.entityMomento));
	}

	/**
	 * Exports a momento for the current state of this {@link HttpRequest}.
	 * 
	 * @return Momento for the current state of this {@link HttpRequest}.
	 * @throws NotAllDataAvailableException
	 *             Should not all data be available for the {@link HttpEntity}.
	 */
	Serializable exportState() throws NotAllDataAvailableException {

		// Prepare state for momento
		List<HttpHeader> httpHeaders = new ArrayList<HttpHeader>(this.headers);
		Serializable entityMomento = this.entity.exportState();

		// Create and return the momento
		return new StateMomento(this.method, this.requestURI, httpHeaders,
				entityMomento);
	}

	/*
	 * ================ HttpRequest ================================
	 */

	@Override
	public String getMethod() {
		return this.method;
	}

	@Override
	public String getRequestURI() {
		return this.requestURI;
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public List<HttpHeader> getHeaders() {
		return this.headers;
	}

	@Override
	public ServerInputStream getEntity() {
		return this.entity.getInputStream();
	}

	/**
	 * Momento for state of this {@link HttpRequest}.
	 */
	private static class StateMomento implements Serializable {

		/**
		 * Method.
		 */
		private final String method;

		/**
		 * Request URI.
		 */
		private final String requestURI;

		/**
		 * Headers.
		 */
		private final List<HttpHeader> headers;

		/**
		 * Momento for the {@link ServerInputStream}.
		 */
		private final Serializable entityMomento;

		/**
		 * Initiate.
		 * 
		 * @param method
		 *            Method.
		 * @param requestURI
		 *            Request URI.
		 * @param headers
		 *            {@link HttpHeader} instances.
		 * @param entityMomento
		 *            {@link ServerInputStream} momento.
		 */
		public StateMomento(String method, String requestURI,
				List<HttpHeader> headers, Serializable entityMomento) {
			this.method = method;
			this.requestURI = requestURI;
			this.headers = headers;
			this.entityMomento = entityMomento;
		}
	}

}