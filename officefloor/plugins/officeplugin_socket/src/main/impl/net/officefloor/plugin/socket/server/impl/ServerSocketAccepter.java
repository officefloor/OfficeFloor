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
package net.officefloor.plugin.socket.server.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.plugin.socket.server.ConnectionManager;
import net.officefloor.plugin.socket.server.EstablishedConnection;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.Connection;

/**
 * Accepts {@link Connection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerSocketAccepter
		implements ManagedFunctionFactory<None, ServerSocketAccepter.ServerSockerAccepterFlows>,
		ManagedFunction<None, ServerSocketAccepter.ServerSockerAccepterFlows> {

	/**
	 * Flows for the {@link ServerSocketAccepter}.
	 */
	public static enum ServerSockerAccepterFlows {
		REPEAT
	}

	/**
	 * {@link CommunicationProtocol} for the {@link ServerSocket}.
	 */
	private final CommunicationProtocol communicationProtocol;

	/**
	 * {@link ConnectionManager}.
	 */
	private final ConnectionManager connectionManager;

	/**
	 * {@link InetSocketAddress} to listen for connections.
	 */
	private final InetSocketAddress serverSocketAddress;

	/**
	 * Back log size for the {@link ServerSocket}.
	 */
	private final int serverSocketBackLogSize;

	/**
	 * {@link ServerSocketChannel} to listen for connections.
	 */
	private ServerSocketChannel channel;

	/**
	 * {@link Selector} to aid in listening for connections.
	 */
	private Selector selector;

	/**
	 * Flag indicating if should stop accepting further connections.
	 */
	private volatile boolean isComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param serverSocketAddress
	 *            {@link InetSocketAddress} to listen for connections.
	 * @param communicationProtocol
	 *            {@link CommunicationProtocol} for the {@link ServerSocket}.
	 * @param connectionManager
	 *            {@link ConnectionManager}.
	 * @param serverSocketBackLogSize
	 *            {@link ServerSocket} back log size.
	 * @throws IOException
	 *             If fails to set up the {@link ServerSocket}.
	 */
	public ServerSocketAccepter(InetSocketAddress serverSocketAddress, CommunicationProtocol communicationProtocol,
			ConnectionManager connectionManager, int serverSocketBackLogSize) throws IOException {
		this.communicationProtocol = communicationProtocol;
		this.connectionManager = connectionManager;
		this.serverSocketAddress = serverSocketAddress;
		this.serverSocketBackLogSize = serverSocketBackLogSize;
	}

	/**
	 * Opens and binds the {@link ServerSocketChannel}.
	 * 
	 * @throws IOException
	 *             {@link IOException}.
	 */
	void bindToSocket() throws IOException {

		// Bind to the socket to start listening
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		ServerSocket socket = this.channel.socket();
		socket.setReuseAddress(true);
		socket.bind(this.serverSocketAddress, this.serverSocketBackLogSize);

		// Register the channel with the selector
		this.selector = Selector.open();
		this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * Unbinds from the {@link ServerSocketChannel}.
	 * 
	 * @throws IOException
	 *             If fails to unbind from the {@link Socket}.
	 */
	void unbindFromSocket() throws IOException {

		// Flag to complete processing
		this.isComplete = true;

		// Wake up selector immediately to allow stopping
		if (this.selector != null) {
			this.selector.wakeup();
		}

		// Ensure stop accepting immediately
		synchronized (this) {
			try {
				// Close the selector
				this.selector.close();
			} finally {
				// Unbind the socket
				this.channel.socket().close();
			}
		}
	}

	/*
	 * =================== ManagedFunctionFactory =============================
	 */

	@Override
	public ManagedFunction<None, ServerSockerAccepterFlows> createManagedFunction() throws Throwable {
		return this;
	}

	/*
	 * ======================= ManagedFunction ================================
	 */

	@Override
	public Object execute(ManagedFunctionContext<None, ServerSockerAccepterFlows> context) throws Exception {

		// Do nothing if complete
		if (this.isComplete) {
			return null;
		}

		// Wait some time for a connection (10 seconds)
		this.selector.select(10000);

		// Obtain the selection key
		for (Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator(); iterator.hasNext();) {
			SelectionKey key = iterator.next();
			iterator.remove();

			// Check if accepting a connection
			if (key.isAcceptable()) {

				// Obtain the socket channel
				final SocketChannel socketChannel = this.channel.accept();
				if (socketChannel != null) {

					// Flag socket as unblocking
					socketChannel.configureBlocking(false);

					// Configure the socket
					Socket socket = socketChannel.socket();
					socket.setTcpNoDelay(true);

					// Create the established connection
					EstablishedConnection connection = new EstablishedConnectionImpl(socketChannel);

					// Manage the connection
					this.connectionManager.manageConnection(connection);
				}
			}
		}

		// Repeat until shutdown
		context.doFlow(ServerSockerAccepterFlows.REPEAT, null, null);
		return null;
	}

	/**
	 * {@link EstablishedConnection} implementation.
	 */
	private class EstablishedConnectionImpl implements EstablishedConnection {

		/**
		 * {@link SocketChannel}.
		 */
		private final SocketChannel socketChannel;

		/**
		 * Initiate.
		 * 
		 * @param socketChannel
		 *            {@link SocketChannel}.
		 */
		public EstablishedConnectionImpl(SocketChannel socketChannel) {
			this.socketChannel = socketChannel;
		}

		/*
		 * =================== EstablishedConnection =====================
		 */

		@Override
		public SocketChannel getSocketChannel() {
			return this.socketChannel;
		}

		@Override
		public CommunicationProtocol getCommunicationProtocol() {
			return ServerSocketAccepter.this.communicationProtocol;
		}
	}

}