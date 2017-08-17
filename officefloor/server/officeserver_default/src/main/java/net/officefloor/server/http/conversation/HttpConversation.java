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
import java.util.function.Supplier;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.protocol.Connection;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * HTTP conversation.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpConversation {

	/**
	 * Services the {@link HttpRequest} in the conversation.
	 * 
	 * @param methodSupplier
	 *            {@link Supplier} of the {@link HttpMethod}.
	 * @param requestUriSupplier
	 *            {@link Supplier} of the request URI.
	 * @param httpVersion
	 *            {@link HttpVersion}.
	 * @param headers
	 *            {@link NonMaterialisedHttpHeaders}.
	 * @param entity
	 *            {@link ByteSequence} to the HTTP entity.
	 * @return {@link ManagedObject} to process the {@link HttpRequest}.
	 */
	void serviceRequest(Supplier<HttpMethod> methodSupplier, Supplier<String> requestUriSupplier,
			HttpVersion httpVersion, NonMaterialisedHttpHeaders headers, ByteSequence entity);

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