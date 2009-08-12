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
package net.officefloor.plugin.socket.server.ssl.protocol;

import java.io.IOException;

import javax.net.ssl.SSLEngine;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.socket.server.ssl.SslConnection;
import net.officefloor.plugin.socket.server.ssl.SslTaskExecutor;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * SSL {@link ConnectionHandler}.
 *
 * @author Daniel Sagenschneider
 */
public class SslConnectionHandler<CH extends ConnectionHandler> implements
		ConnectionHandler {

	/**
	 * {@link SslConnection}.
	 */
	private final SslConnection connection;

	/**
	 * Wrapped {@link ConnectionHandler}.
	 */
	private final CH wrappedConnectionHandler;

	/**
	 * Initiate.
	 *
	 * @param connection
	 *            {@link Connection}.
	 * @param engine
	 *            {@link SSLEngine}.
	 * @param bufferSquirtFactory
	 *            {@link BufferSquirtFactory}.
	 * @param taskExecutor
	 *            {@link SslTaskExecutor}.
	 * @param wrappedServerSocketHandler
	 *            Wrapped {@link ServerSocketHandler}.
	 */
	public SslConnectionHandler(Connection connection, SSLEngine engine,
			BufferSquirtFactory bufferSquirtFactory,
			SslTaskExecutor taskExecutor,
			ServerSocketHandler<CH> wrappedServerSocketHandler) {

		// Creates the SSL connection
		this.connection = new SslConnectionImpl(connection.getLock(),
				connection.getInputBufferStream(), connection
						.getOutputBufferStream(), engine, bufferSquirtFactory,
				null, taskExecutor);

		// Create the connection handler to wrap
		this.wrappedConnectionHandler = wrappedServerSocketHandler
				.createConnectionHandler(this.connection);
	}

	/**
	 * Obtains the wrapped {@link ConnectionHandler}.
	 *
	 * @return Wrapped {@link ConnectionHandler}.
	 */
	public CH getWrappedConnectionHandler() {
		return this.wrappedConnectionHandler;
	}

	/*
	 * ======================== ConnectionHandler ============================
	 */

	@Override
	public void handleRead(ReadContext context) throws IOException {

		// Process the data from peer
		this.connection.processDataFromPeer();

		// Always handle read even if no application data (stop timeouts)
		this.wrappedConnectionHandler.handleRead(context);
	}

	@Override
	public void handleWrite(WriteContext context) throws IOException {
		// Delegate to wrapped handler
		this.wrappedConnectionHandler.handleWrite(context);
	}

	@Override
	public void handleIdleConnection(IdleContext context) throws IOException {
		// Delegate to wrapped handler
		this.wrappedConnectionHandler.handleIdleConnection(context);
	}

}