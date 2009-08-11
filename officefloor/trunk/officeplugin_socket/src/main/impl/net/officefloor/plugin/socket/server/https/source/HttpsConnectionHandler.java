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
package net.officefloor.plugin.socket.server.https.source;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.WriteContext;

/**
 * HTTPS {@link ConnectionHandler}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpsConnectionHandler implements ConnectionHandler {

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link SSLEngine}.
	 */
	private SSLEngine engine;

	/**
	 * Initiate.
	 *
	 * @param connection
	 *            {@link Connection}.
	 * @param context
	 *            {@link SSLContext}.
	 */
	public HttpsConnectionHandler(Connection connection, SSLContext context) {
		this.connection = connection;

		// Obtain the peer details and create the SSL Engine
		String peerHost = null;
		int peerPort = 0;
		this.engine = context.createSSLEngine(peerHost, peerPort);
	}

	/*
	 * ================== ConnectionHandler ================================
	 */

	@Override
	public void handleRead(ReadContext context) throws IOException {

		// Obtain the browse stream
		InputStream browseStream = context.getInputBufferStream()
				.getBrowseStream();

		// TODO Implement ConnectionHandler.handleRead
		throw new UnsupportedOperationException("ConnectionHandler.handleRead");
	}

	@Override
	public void handleWrite(WriteContext context) throws IOException {
		// TODO Implement ConnectionHandler.handleWrite
		throw new UnsupportedOperationException("ConnectionHandler.handleWrite");
	}

	@Override
	public void handleIdleConnection(IdleContext context) throws IOException {
		// TODO Implement ConnectionHandler.handleIdleConnection
		throw new UnsupportedOperationException(
				"ConnectionHandler.handleIdleConnection");
	}

}