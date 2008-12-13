/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http;

import java.io.IOException;
import java.io.OutputStream;

import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.parse.ParseException;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.IdleContext;
import net.officefloor.plugin.socket.server.spi.MessageSegment;
import net.officefloor.plugin.socket.server.spi.ReadContext;
import net.officefloor.plugin.socket.server.spi.WriteContext;
import net.officefloor.plugin.socket.server.spi.WriteMessage;

/**
 * HTTP {@link ConnectionHandler}.
 * 
 * @author Daniel
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
	 *             If failure of {@link Connection} (eg closed).
	 */
	public void sendResponse(HttpResponseImpl response) throws IOException {
		// Create the message, loading response content and writing to client
		WriteMessage message = this.connection.createWriteMessage(null);
		response.loadContent(message);
		message.write();
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
	 * Buffer to read in content. Use the same buffer to reduce unnecessary
	 * allocations of memory.
	 */
	private final byte[] buffer = new byte[1024];

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.socket.server.spi.ConnectionHandler#handleRead
	 * (net.officefloor.plugin.socket.server.spi.ReadContext)
	 */
	@Override
	public void handleRead(ReadContext context) {
		try {

			// New last interaction time
			this.lastInteractionTime = context.getTime();

			// Ensure a parser is available
			if (this.httpRequestParser == null) {
				this.httpRequestParser = new HttpRequestParser(this.source
						.getInitialRequestBodyBufferLength(), this.source
						.getMaximumRequestBodyLength());
			}

			// Read in the content
			int bytesRead = context.getReadMessage().read(this.buffer);

			// Parse the just read in content
			if (this.httpRequestParser.parseMoreContent(this.buffer, 0,
					bytesRead)) {
				// Received the full HTTP request so start processing
				context.setReadComplete(true);
			}

		} catch (ParseException ex) {
			try {

				// Attempt to obtain version
				String version = this.httpRequestParser.getVersion();
				if ((version == null) || (version.trim().length() == 0)) {
					// Use default version
					version = HttpRequestParser.DEFAULT_HTTP_VERSION;
				}

				// Flag read over
				this.httpRequestParser = null;
				context.setReadComplete(true);

				// Send HTTP response, indicating parse failure
				new HttpResponseImpl(this, version, ex.getHttpStatus(), ex
						.getMessage()).send();

			} catch (IOException io) {
				// Failure constructing response, so fail connection
				context.setCloseConnection(true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.socket.server.spi.ConnectionHandler#handleWrite
	 * (net.officefloor.plugin.socket.server.spi.WriteContext)
	 */
	@Override
	public void handleWrite(WriteContext context) {
		// New last interaction time
		this.lastInteractionTime = context.getTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.plugin.socket.server.spi.ConnectionHandler#
	 * handleIdleConnection
	 * (net.officefloor.plugin.socket.server.spi.IdleContext)
	 */
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
