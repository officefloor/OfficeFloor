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
package net.officefloor.plugin.socket.server.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * Accepts connections.
 *
 * @author Daniel Sagenschneider
 */
public class ServerSocketAccepter<CH extends ConnectionHandler>
		extends
		AbstractSingleTask<ServerSocketAccepter<CH>, None, ServerSocketAccepter.ServerSocketAccepterFlows> {

	/**
	 * {@link Flow} instances for the {@link ServerSocketAccepter}.
	 */
	public static enum ServerSocketAccepterFlows {
		LISTEN
	}

	/**
	 * {@link ConnectionManager}.
	 */
	private final ConnectionManager<CH> connectionManager;

	/**
	 * {@link BufferSquirtFactory}.
	 */
	private final BufferSquirtFactory bufferSquirtFactory;

	/**
	 * {@link ServerSocketHandler}.
	 */
	private final ServerSocketHandler<CH> serverSocketHandler;

	/**
	 * {@link InetSocketAddress} to listen for connections.
	 */
	private InetSocketAddress serverSocketAddress;

	/**
	 * Initiate.
	 *
	 * @param serverSocketAddress
	 *            {@link InetSocketAddress} to listen for connections.
	 * @param serverSocketHandler
	 *            {@link ServerSocketHandler}.
	 * @param connectionManager
	 *            {@link ConnectionManager}.
	 * @param bufferSquirtFactory
	 *            {@link BufferSquirtFactory}.
	 * @throws IOException
	 *             If fails to set up the {@link ServerSocket}.
	 */
	public ServerSocketAccepter(InetSocketAddress serverSocketAddress,
			ServerSocketHandler<CH> serverSocketHandler,
			ConnectionManager<CH> connectionManager,
			BufferSquirtFactory bufferSquirtFactory) throws IOException {
		this.serverSocketHandler = serverSocketHandler;
		this.connectionManager = connectionManager;
		this.bufferSquirtFactory = bufferSquirtFactory;
		this.serverSocketAddress = serverSocketAddress;
	}

	/**
	 * {@link ServerSocketChannel} to listen for connections.
	 */
	private ServerSocketChannel channel;

	/**
	 * {@link Selector} to aid in listening for connections.
	 */
	private Selector selector;

	/**
	 * Opens and binds the {@link ServerSocketChannel}.
	 *
	 * @throws IOException
	 *             {@link IOException}.
	 */
	public void bindToSocket() throws IOException {

		// Bind to the socket to start listening
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		this.channel.socket().bind(this.serverSocketAddress);

		// Register the channel with the selector
		this.selector = Selector.open();
		this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
	}

	/*
	 * ======================= Task ============================================
	 */

	@Override
	public Object doTask(
			TaskContext<ServerSocketAccepter<CH>, None, ServerSocketAccepterFlows> context)
			throws Exception {

		// Loop accepting connections
		for (;;) {

			// Wait some time for a connection
			if (this.selector.select(1000) == 0) {

				// Flag task never complete (but allow for closing office)
				context.setComplete(false);
				return null;

			} else {
				// Obtain the selection key
				for (Iterator<SelectionKey> iterator = this.selector
						.selectedKeys().iterator(); iterator.hasNext();) {
					SelectionKey key = iterator.next();
					iterator.remove();

					// Check if accepting a connection
					if (key.isAcceptable()) {

						// Obtain the socket channel
						SocketChannel socketChannel = this.channel.accept();
						if (socketChannel != null) {

							// Flag socket as unblocking
							socketChannel.configureBlocking(false);

							// Create the connection
							ConnectionImpl<CH> connection = new ConnectionImpl<CH>(
									new NonblockingSocketChannelImpl(
											socketChannel),
									this.serverSocketHandler,
									this.bufferSquirtFactory);

							// Register the connection for management
							this.connectionManager.registerConnection(
									connection, context);
						}
					}
				}
			}
		}
	}

	/**
	 * Implementation of the {@link NonblockingSocketChannel}.
	 */
	private static class NonblockingSocketChannelImpl implements
			NonblockingSocketChannel {

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
		public NonblockingSocketChannelImpl(SocketChannel socketChannel) {
			this.socketChannel = socketChannel;
		}

		/*
		 * ======================= NonblockingSocketChannel ====================
		 */

		@Override
		public SelectionKey register(Selector selector, int ops,
				Object attachment) throws IOException {
			return this.socketChannel.register(selector, ops, attachment);
		}

		@Override
		public InetAddress getInetAddress() {
			return this.socketChannel.socket().getInetAddress();
		}

		@Override
		public int getPort() {
			return this.socketChannel.socket().getPort();
		}

		@Override
		public int read(ByteBuffer buffer) throws IOException {
			return this.socketChannel.read(buffer);
		}

		@Override
		public int write(ByteBuffer data) throws IOException {
			return this.socketChannel.write(data);
		}

		@Override
		public void close() throws IOException {
			this.socketChannel.close();
		}
	}

}