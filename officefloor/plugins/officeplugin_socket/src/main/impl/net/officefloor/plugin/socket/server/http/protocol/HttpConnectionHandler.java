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
package net.officefloor.plugin.socket.server.http.protocol;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpEntity;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;
import net.officefloor.plugin.socket.server.protocol.HeartBeatContext;
import net.officefloor.plugin.socket.server.protocol.ReadContext;

/**
 * HTTP {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpConnectionHandler implements ConnectionHandler {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(HttpConnectionHandler.class.getName());

	/**
	 * {@link HttpConversation}.
	 */
	private final HttpConversation conversation;

	/**
	 * {@link HttpRequestParser}.
	 */
	private final HttpRequestParser parser;

	/**
	 * {@link Connection} timeout in milliseconds.
	 */
	private final long connectionTimout;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * {@link Flow} index to handle processing {@link HttpRequest}.
	 */
	private final int requestHandlingFlowIndex;

	/**
	 * Time of last interaction. Will be set on the first read.
	 */
	private long lastInteractionTime = -1;

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
	 * @param connectionTimeout
	 *            {@link Connection} timeout in milliseconds.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @param requestHandlingFlowIndex
	 *            {@link Flow} index to handle processing {@link HttpRequest}.
	 */
	public HttpConnectionHandler(HttpConversation conversation, HttpRequestParser parser, long connectionTimeout,
			ManagedObjectExecuteContext<Indexed> executeContext, int requestHandlingFlowIndex) {
		this.conversation = conversation;
		this.parser = parser;
		this.connectionTimout = connectionTimeout;
		this.executeContext = executeContext;
		this.requestHandlingFlowIndex = requestHandlingFlowIndex;
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
				return;
			}

			// New last interaction time
			this.lastInteractionTime = context.getTime();

			// Loop as may have more than one request on read
			byte[] readData = context.getData();
			int startIndex = 0;
			while (startIndex >= 0) {

				// Parse the read content
				if (this.parser.parse(readData, startIndex)) {

					// Received the full HTTP request to start processing
					String method = this.parser.getMethod();
					String requestURI = this.parser.getRequestURI();
					String httpVersion = this.parser.getHttpVersion();
					List<HttpHeader> headers = this.parser.getHeaders();
					HttpEntity entity = this.parser.getEntity();
					this.parser.reset(); // reset for next request

					// Service the request
					HttpManagedObject managedObject = this.conversation.addRequest(method, requestURI, httpVersion,
							headers, entity);
					this.executeContext.invokeProcess(this.requestHandlingFlowIndex,
							managedObject.getServerHttpConnection(), managedObject, 0, managedObject.getFlowCallback());
				}

				// Obtain the next start index
				startIndex = this.parser.nextByteToParseIndex();
			}

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

	@Override
	public void handleHeartbeat(HeartBeatContext context) {

		// May not have received data from client on creating connection
		if (this.lastInteractionTime == -1) {
			// Allow for time out of connection if no data
			this.lastInteractionTime = context.getTime();
			return;
		}

		// Obtain the current time
		long currentTime = context.getTime();

		// Determine time idle
		long timeIdle = currentTime - this.lastInteractionTime;

		// Close connection if idle too long
		if (timeIdle >= this.connectionTimout) {
			try {
				this.conversation.closeConnection();
			} catch (IOException ex) {
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Failed closing connection on idle", ex);
				}
			}
		}
	}

}