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
package net.officefloor.server.http.conversation;

import java.io.IOException;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.protocol.Connection;

/**
 * HTTP conversation.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpConversation {

	/**
	 * Adds a {@link HttpRequest} to the conversation.
	 * 
	 * @param method
	 *            Method.
	 * @param requestURI
	 *            Request URI.
	 * @param httpVersion
	 *            HTTP Version.
	 * @param headers
	 *            {@link HttpRequestHeaders}.
	 * @param entity
	 *            {@link HttpEntity} to the entity of the {@link HttpRequest}.
	 * @return {@link HttpManagedObject} to process the {@link HttpRequest}.
	 */
	HttpManagedObject addRequest(String method, String requestURI, String httpVersion, HttpRequestHeaders headers,
			HttpEntity entity);

	/**
	 * Handles a failure in parsing a {@link HttpRequest}.
	 * 
	 * @param failure
	 *            Failure in parsing a {@link HttpRequest}.
	 * @param isCloseConnection
	 *            Flags to close the {@link Connection} once the
	 *            {@link HttpRequestParseException} has been processed.
	 * @throws IOException
	 *             If fails to write response regarding the
	 *             {@link HttpRequestParseException}.
	 */
	void parseFailure(HttpRequestParseException failure, boolean isCloseConnection) throws IOException;

	/**
	 * Closes the {@link Connection}.
	 * 
	 * @throws IOException
	 *             If fails to close the {@link Connection}.
	 */
	void closeConnection() throws IOException;

}