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

import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.parse.ParseException;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.IdleContext;
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
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 */
	public HttpConnectionHandler(Connection connection) {
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
		// Unset parser so new created on next request
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

		// Create the message to write the response
		WriteMessage message = this.connection.createWriteMessage(null);
		message.append(response.getContent());
		message.write();
	}

	/*
	 * ==================================================================
	 * ConnectionHandler
	 * 
	 * Thread-safe by the lock taken in SockerListener.
	 * ==================================================================
	 */

	/**
	 * {@link HttpRequestParserTest}.
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
	 * @see net.officefloor.plugin.socket.server.spi.ConnectionHandler#handleRead(net.officefloor.plugin.socket.server.spi.ReadContext)
	 */
	@Override
	public void handleRead(ReadContext context) {
		try {

			// Ensure a parser is available
			if (this.httpRequestParser == null) {
				this.httpRequestParser = new HttpRequestParser();
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
			// TODO Handle parse failure
			System.err.println("TODO [" + this.getClass().getSimpleName()
					+ "] handle parse failure");
			ex.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ConnectionHandler#handleWrite(net.officefloor.plugin.socket.server.spi.WriteContext)
	 */
	@Override
	public void handleWrite(WriteContext context) {
		// TODO handle write
		System.err.println("TODO [" + this.getClass().getSimpleName()
				+ "] handleWrite");
		// context.setCloseConnection(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ConnectionHandler#handleIdleConnection(net.officefloor.plugin.socket.server.spi.IdleContext)
	 */
	@Override
	public void handleIdleConnection(IdleContext context) {
		// TODO implement
		System.err.println("TODO [" + this.getClass().getSimpleName()
				+ "] handleIdleConnection");
		// context.setCloseConnection(true);
	}

}
