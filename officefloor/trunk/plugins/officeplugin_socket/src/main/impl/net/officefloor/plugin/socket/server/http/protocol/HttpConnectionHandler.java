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

package net.officefloor.plugin.socket.server.http.protocol;

import java.io.IOException;
import java.util.List;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.stream.InputBufferStream;

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
	 * Maximum length of text part for {@link HttpRequest}.
	 */
	private final int maxTextPartLength;

	/**
	 * {@link Connection} timeout in milliseconds.
	 */
	private final long connectionTimout;

	/**
	 * Time of last interaction. Will be set on the first read.
	 */
	private long lastInteractionTime;

	/**
	 * Flag indicating if {@link HttpRequestParseException} on processing input. Once a
	 * {@link HttpRequestParseException} occurs it is unrecoverable and the
	 * {@link Connection} should be closed.
	 */
	private boolean isParseFailure = false;

	/**
	 * Initiate.
	 *
	 * @param conversation
	 *            {@link HttpConversation}.
	 * @param parser
	 *            {@link HttpRequestParser}.
	 * @param maxTextPartLength
	 *            Maximum length of text part for {@link HttpRequest}.
	 * @param connectionTimeout
	 *            {@link Connection} timeout in milliseconds.
	 */
	public HttpConnectionHandler(HttpConversation conversation,
			HttpRequestParser parser, int maxTextPartLength,
			long connectionTimeout) {
		this.conversation = conversation;
		this.parser = parser;
		this.maxTextPartLength = maxTextPartLength;
		this.connectionTimout = connectionTimeout;
	}

	/*
	 * ================ ConnectionHandler ==============================
	 * Thread-safe by the lock taken in SockerListener.
	 */

	@Override
	public void handleRead(ReadContext context) throws IOException {
		try {

			// Ignore all further content if parse failure
			if (this.isParseFailure) {
				// Consume all content read (ignore as become invalid)
				InputBufferStream ignore = context.getInputBufferStream();
				ignore.skip(ignore.available());
				return; // no further processing
			}

			// New last interaction time
			this.lastInteractionTime = context.getTime();

			// Lazy obtain the temporary buffer
			char[] tempBuffer = (char[]) context.getContextObject();
			if (tempBuffer == null) {
				tempBuffer = new char[this.maxTextPartLength];
				context.setContextObject(tempBuffer);
			}

			// Loop as may have more than one request on read
			InputBufferStream inputBufferStream = context
					.getInputBufferStream();
			for (;;) {
				// Attempt to parse the remaining content of request
				if (this.parser.parse(inputBufferStream, tempBuffer)) {

					// Received the full HTTP request to start processing
					String method = this.parser.getMethod();
					String requestURI = this.parser.getRequestURI();
					String httpVersion = this.parser.getHttpVersion();
					List<HttpHeader> headers = this.parser.getHeaders();
					InputBufferStream body = this.parser.getBody();
					this.parser.reset(); // reset for next request

					// Process the request
					HttpManagedObject managedObject = this.conversation
							.addRequest(method, requestURI, httpVersion,
									headers, body);
					context.processRequest(managedObject);

				} else {
					// No further content to parse
					return;
				}
			}

		} catch (HttpRequestParseException ex) {
			// Flag that input no longer valid
			this.isParseFailure = true;

			// Process failed parsing (close connection when response sent)
			this.conversation.parseFailure(ex, true);
		}
	}

	@Override
	public void handleWrite(WriteContext context) {
		// New last interaction time
		this.lastInteractionTime = context.getTime();
	}

	@Override
	public void handleIdleConnection(IdleContext context) {

		// Obtain the current time
		long currentTime = context.getTime();

		// Determine time idle
		long timeIdle = currentTime - this.lastInteractionTime;

		// Close connection if idle too long
		if (timeIdle >= this.connectionTimout) {
			context.setCloseConnection(true);
		}
	}

}