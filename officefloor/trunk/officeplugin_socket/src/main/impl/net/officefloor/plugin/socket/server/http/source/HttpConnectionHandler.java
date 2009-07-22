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
package net.officefloor.plugin.socket.server.http.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.Request;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.parse.ParseException;
import net.officefloor.plugin.stream.BufferStream;

/**
 * HTTP {@link ConnectionHandler}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpConnectionHandler implements ConnectionHandler {

	/**
	 * {@link HttpServerSocketManagedObjectSource}.
	 */
	private final HttpServerSocketManagedObjectSource source;

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * Time of last interaction. Will be set on the first read.
	 */
	private long lastInteractionTime;

	/**
	 * Initiate.
	 *
	 * @param source
	 *            {@link HttpServerSocketManagedObjectSource}.
	 * @param connection
	 *            {@link Connection}.
	 */
	public HttpConnectionHandler(HttpServerSocketManagedObjectSource source,
			Connection connection) {
		this.source = source;
		this.connection = connection;
	}

	/**
	 * Obtains the {@link HttpRequestParser}.
	 *
	 * @return {@link HttpRequestParser}.
	 */
	public HttpRequestParser getHttpRequestParser() {
		return this.httpRequestParser;
	}

	/**
	 * Resets for next request.
	 */
	public void resetForNextRequest() {
		// Clear parser so new created on next request
		this.httpRequestParser = null;
	}

	/**
	 * Sends the {@link HttpResponseImpl}.
	 *
	 * @param response
	 *            {@link HttpResponseImpl}.
	 * @throws IOException
	 *             If failure of {@link Connection} (may be closed).
	 */
	public void sendResponse(HttpResponseImpl response) throws IOException {
		// Create the message, loading response content and writing to client
		response.loadContent(this.connection.getOutputBufferStream());
	}

	/**
	 * Obtains the response buffer length for each {@link MessageSegment} being
	 * appended to by the {@link OutputStream} to populate the body.
	 *
	 * @return Response buffer length.
	 */
	protected int getResponseBufferLength() {
		return this.source.getResponseBufferLength();
	}

	/*
	 * ================ ConnectionHandler ==============================
	 * Thread-safe by the lock taken in SockerListener.
	 */

	/**
	 * {@link HttpRequestParser}.
	 */
	private HttpRequestParser httpRequestParser;

	/**
	 * Size of the {@link Request}.
	 */
	private long requestSize = 0;

	@Override
	public void handleRead(ReadContext context) throws IOException {
		try {

			// New last interaction time
			this.lastInteractionTime = context.getTime();

			// Ensure a parser is available
			if (this.httpRequestParser == null) {
				this.httpRequestParser = new HttpRequestParser(this.source
						.getInitialRequestBodyBufferLength(), this.source
						.getMaximumRequestBodyLength());
			}

			// Obtain the input stream at next byte of request
			InputStream inputStream = context.getInputStream();
			inputStream.skip(this.requestSize);

			// Attempt to read the remaining content of request
			for (int value = inputStream.read(); value != BufferStream.END_OF_STREAM; value = inputStream
					.read()) {
				this.requestSize++;
				if (this.httpRequestParser.parseMoreContent((byte) value)) {
					// Received the full HTTP request to start processing
					context.requestReceived(this.requestSize,
							this.httpRequestParser);
				}
			}

		} catch (ParseException ex) {
			try {

				// Attempt to obtain version
				String version = this.httpRequestParser.getVersion();
				if ((version == null) || (version.trim().length() == 0)) {
					// Use default version
					version = HttpRequestParser.DEFAULT_HTTP_VERSION;
				}

				// Send HTTP response, indicating parse failure
				new HttpResponseImpl(this, version, ex.getHttpStatus(), ex
						.getMessage()).send();

				// Flag read over
				this.httpRequestParser = null;
				context.requestReceived(0, null);

			} catch (IOException io) {
				// Failure constructing response, so fail connection
				context.setCloseConnection(true);
			}
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
		if (timeIdle >= this.source.getConnectionTimeout()) {
			context.setCloseConnection(true);
		}
	}

}