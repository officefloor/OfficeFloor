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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.execute.WorkContext;
import net.officefloor.plugin.impl.socket.server.messagesegment.DirectBufferMessageSegmentPool;
import net.officefloor.plugin.socket.server.spi.Message;
import net.officefloor.plugin.socket.server.spi.MessageSegment;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;

/**
 * Accepts connections.
 * 
 * @author Daniel
 */
class ServerSocketAccepter implements Work, WorkFactory<ServerSocketAccepter>,
		Task<Object, ServerSocketAccepter, None, Indexed>,
		TaskFactory<Object, ServerSocketAccepter, None, Indexed> {

	/**
	 * {@link InetSocketAddress}.
	 */
	private final InetSocketAddress serverSocketAddress;

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
	private ServerSocketChannel channel;

	/**
	 * {@link Selector} to aid in listening for connections.
	 */
	private Selector selector;

	/**
	 * {@link ServerSocketHandler}.
	 */
	private ServerSocketHandler<?> serverSocketHandler;

	/**
	 * Initiate.
	 * 
	 * @param server
	 *            {@link Server}.
	 * @param serverSocketAddress
	 *            {@link InetSocketAddress} to listen for connections.
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
			ConnectionManager connectionManager, int recommendedSegmentCount,
			MessageSegmentPool messageSegmentPool) throws IOException {
		this.serverSocketAddress = serverSocketAddress;
		this.connectionManager = connectionManager;
		this.recommendedSegmentCount = recommendedSegmentCount;
		this.messageSegmentPool = messageSegmentPool;
	}

	/**
	 * Binds to the {@link ServerSocketChannel} and starts listening for
	 * connections.
	 * 
	 * @param serverSocketHandler
	 *            {@link ServerSocketHandler}.
	 * @throws IOException
	 *             If fails to bind.
	 */
	void bind(ServerSocketHandler<?> serverSocketHandler) throws IOException {

		// Store reference to the server socket handler
		this.serverSocketHandler = serverSocketHandler;

		// Bind to the socket to start listening
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		this.channel.socket().bind(this.serverSocketAddress);

		// Register the channel with the selector
		this.selector = Selector.open();
		this.channel.register(this.selector, this.channel.validOps());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkFactory#createWork()
	 */
	public ServerSocketAccepter createWork() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Work#setWorkContext(net.officefloor.frame.api.execute.WorkContext)
	 */
	public void setWorkContext(WorkContext context) throws IOException {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskFactory#createTask(W)
	 */
	public Task<Object, ServerSocketAccepter, None, Indexed> createTask(
			ServerSocketAccepter work) {
		// Return this
		return work;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
	 */
	@SuppressWarnings("unchecked")
	public Object doTask(
			TaskContext<Object, ServerSocketAccepter, None, Indexed> context)
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
				for (SelectionKey key : selector.selectedKeys()) {

					// Check if accepting a connection
					if (key.isAcceptable()) {

						// Obtain the Server Socket Channel
						ServerSocketChannel channel = (ServerSocketChannel) key
								.channel();

						// Obtain the server channel
						SocketChannel socketChannel = channel.accept();
						if (socketChannel != null) {

							// Flag socket as unblocking
							socketChannel.configureBlocking(false);

							// Create the connection (registering itself)
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
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.plugin.impl.socket.server.NonblockingSocketChannel#register(java.nio.channels.Selector,
		 *      int, java.lang.Object)
		 */
		@Override
		public SelectionKey register(Selector selector, int ops,
				Object attachment) throws IOException {
			return this.socketChannel.register(selector, ops, attachment);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.plugin.impl.socket.server.NonblockingSocketChannel#read(java.nio.ByteBuffer)
		 */
		@Override
		public int read(ByteBuffer buffer) throws IOException {
			return this.socketChannel.read(buffer);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.plugin.impl.socket.server.NonblockingSocketChannel#write(java.nio.ByteBuffer)
		 */
		@Override
		public int write(ByteBuffer data) throws IOException {
			return this.socketChannel.write(data);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.plugin.impl.socket.server.NonblockingSocketChannel#close()
		 */
		@Override
		public void close() throws IOException {
			this.socketChannel.close();
		}

	}

}
