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

import java.util.List;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.ParseException;
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
	 * Initiate.
	 *
	 * @param connection
	 *            {@link Connection}.
	 * @param bufferSquirtFactory
	 *            {@link BufferSquirtFactory}.
	 */
	public HttpConversationImpl(Connection connection,
			BufferSquirtFactory bufferSquirtFactory) {
		this.connection = connection;
		this.bufferSquirtFactory = bufferSquirtFactory;
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

		// Create the corresponding response
		HttpResponseImpl response = new HttpResponseImpl(this.connection,
				this.bufferSquirtFactory, httpVersion);

		// Create the http managed object
		HttpManagedObject managedObject = new HttpManagedObjectImpl(request,
				response);

		// Return the managed object
		return managedObject;
	}

	@Override
	public void parseFailure(ParseException failure) {
		// TODO Implement HttpConversation.parseFailure
		throw new UnsupportedOperationException("HttpConversation.parseFailure");
	}

}