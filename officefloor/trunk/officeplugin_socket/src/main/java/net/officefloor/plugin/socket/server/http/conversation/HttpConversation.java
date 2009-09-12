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
package net.officefloor.plugin.socket.server.http.conversation;

import java.io.IOException;
import java.util.List;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.stream.InputBufferStream;

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
	 *            {@link HttpHeader} instances.
	 * @param body
	 *            {@link InputBufferStream} to the contents of the
	 *            {@link HttpRequest}.
	 * @return {@link HttpManagedObject} to process the {@link HttpRequest}.
	 */
	HttpManagedObject addRequest(String method, String requestURI,
			String httpVersion, List<HttpHeader> headers, InputBufferStream body);

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
	void parseFailure(HttpRequestParseException failure, boolean isCloseConnection)
			throws IOException;

}