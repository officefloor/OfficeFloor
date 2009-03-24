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
package net.officefloor.plugin.impl.socket.server;

import java.io.IOException;
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
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.impl.socket.server.messagesegment.DirectBufferMessageSegmentPool;
import net.officefloor.plugin.socket.server.spi.Message;
import net.officefloor.plugin.socket.server.spi.MessageSegment;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;
import net.officefloor.work.clazz.Flow;

/**
 * Accepts connections.
 * 
 * @author Daniel
 */
class ServerSocketAccepter
		extends
		AbstractSingleTask<ServerSocketAccepter, None, ServerSocketAccepter.ServerSocketAccepterFlows> {

	/**
	 * {@link Flow} instances for the {@link ServerSocketAccepter}.
	 */
	public static enum ServerSocketAccepterFlows {
		LISTEN
	}

	/**
	 * {@link ConnectionManager}.
	 */
	private final ConnectionManager connectionManager;

	/**
	 * Recommended number of {@link MessageSegment} instances per
	 * {@link Message}.
	 */
	private final int recommendedSegmentCount;

	/**
	 * {@link MessageSegmentPool}.
	 */
	private final MessageSegmentPool messageSegmentPool;

	/**
	 * {@link ServerSocketChannel} to listen for connections.
	 */
	private final ServerSocketChannel channel;

	/**
	 * {@link Selector} to aid in listening for connections.
	 */
	private final Selector selector;

	/**
	 * {@link ServerSocketHandler}.
	 */
	private final ServerSocketHandler<?> serverSocketHandler;

	/**
	 * Initiate.
	 * 
	 * @param serverSocketAddress
	 *            {@link InetSocketAddress} to listen for connections.
	 * @param serverSocketHandler
	 *            {@link ServerSocketHandler}.
	 * @param connectionManager
	 *            {@link ConnectionManager}.
	 * @param recommendedSegmentCount
	 *            Recommended number of {@link MessageSegment} instances per
	 *            {@link Message}.
	 * @param messageSegmentPool
	 *            {@link DirectBufferMessageSegmentPool}.
	 * @throws IOException
	 *             If fails to set up the {@link ServerSocket}.
	 */
	ServerSocketAccepter(InetSocketAddress serverSocketAddress,
			ServerSocketHandler<?> serverSocketHandler,
			ConnectionManager connectionManager, int recommendedSegmentCount,
			MessageSegmentPool messageSegmentPool) throws IOException {
		this.serverSocketHandler = serverSocketHandler;
		this.connectionManager = connectionManager;
		this.recommendedSegmentCount = recommendedSegmentCount;
		this.messageSegmentPool = messageSegmentPool;

		// Bind to the socket to start listening
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		this.channel.socket().bind(serverSocketAddress);

		// Register the channel with the selector
		this.selector = Selector.open();
		this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
	}

	/*
	 * ======================= Task ============================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Object doTask(
			TaskContext<ServerSocketAccepter, None, ServerSocketAccepterFlows> context)
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
							ConnectionImpl<?> connection = new ConnectionImpl(
									new NonblockingSocketChannelImpl(
											socketChannel),
									this.serverSocketHandler,
									this.recommendedSegmentCount,
									this.messageSegmentPool);

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