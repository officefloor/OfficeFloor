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
package net.officefloor.server.http.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.ConnectionHandler;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.conversation.HttpConversation;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * HTTP {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpConnectionHandler implements ConnectionHandler {

	/**
	 * {@link HttpConversation}.
	 */
	private final HttpConversation conversation;

	/**
	 * {@link HttpRequestParser}.
	 */
	private final HttpRequestParser parser;

	/**
	 * Flag indicating if {@link HttpRequestParseException} on processing input.
	 * Once a {@link HttpRequestParseException} occurs it is unrecoverable and
	 * the {@link Connection} should be closed.
	 */
	private boolean isParseFailure = false;

	/**
	 * Initiate.
	 * 
	 * @param conversation
	 *            {@link HttpConversation}.
	 * @param parser
	 *            {@link HttpRequestParser}.
	 */
	public HttpConnectionHandler(HttpConversation conversation, HttpRequestParser parser) {
		this.conversation = conversation;
		this.parser = parser;
	}

	/*
	 * ================ ConnectionHandler ==============================
	 */

	@Override
	public void handleRead(StreamBuffer<ByteBuffer> buffer) throws IOException {
		try {

			// Ignore all further content if parse failure
			if (this.isParseFailure) {
				return;
			}

			// Loop as may have more than one request on read
			do {

				// Parse the read content
				if (this.parser.parse(buffer)) {

					// Received the full HTTP request to start processing
					Supplier<HttpMethod> methodSupplier = this.parser.getMethod();
					Supplier<String> requestUriSupplier = this.parser.getRequestURI();
					HttpVersion httpVersion = this.parser.getVersion();
					NonMaterialisedHttpHeaders headers = this.parser.getHeaders();
					ByteSequence entity = this.parser.getEntity();
					this.parser.reset(); // reset for next request

					// Service the request
					this.conversation.serviceRequest(methodSupplier, requestUriSupplier, httpVersion, headers, entity);
				}

			} while (!this.parser.isFinishedReadingBuffer());

		} catch (HttpRequestParseException ex) {
			// Flag that input no longer valid
			this.isParseFailure = true;

			// Process failed parsing (close connection when response sent)
			this.conversation.parseFailure(ex, true);

		} catch (IOException ex) {
			// As error in I/O connection no longer valid
			this.isParseFailure = true; // skip remaining content if can read

			// Propagate failure
			throw ex;
		}
	}

}