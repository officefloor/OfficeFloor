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
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.plugin.socket.server.AcceptedSocket;
import net.officefloor.plugin.socket.server.ManagedConnection;
import net.officefloor.plugin.socket.server.SocketManager;
import net.officefloor.plugin.socket.server.WriteDataAction;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ReadContext;

/**
 * Listens to {@link Socket} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketListener extends StaticManagedFunction<None, SocketListener.SocketListenerFlows>
		implements ReadContext {

	/**
	 * Flows for the {@link SocketListener}.
	 */
	public static enum SocketListenerFlows {
		REPEAT
	}

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(SocketListener.class.getName());

	/**
	 * {@link SocketManager}.
	 */
	private final SocketManager socketManager;

	/**
	 * Listing of bound {@link ServerSocketChannel} instances.
	 */
	private final List<ServerSocketChannel> boundServerSocketChannels = new LinkedList<>();

	/**
	 * {@link Queue} of {@link AcceptedSocket} instances.
	 */
	private final Queue<AcceptedSocket> establishedConnections = new ConcurrentLinkedQueue<AcceptedSocket>();

	/**
	 * {@link ByteBuffer} to use for reading content.
	 */
	private final ByteBuffer readBuffer;

	/**
	 * Send {@link ByteBuffer} size.
	 */
	private final int sendBufferSize;

	/**
	 * {@link Queue} of {@link WriteDataAction} instances to be undertaken on
	 * their respective {@link Connection} when their {@link SocketChannel}
	 * empties.
	 */
	private final Queue<WriteDataAction> writeActions = new ConcurrentLinkedQueue<WriteDataAction>();

	/**
	 * Pool of {@link ByteBuffer} instances.
	 */
	private final Queue<ByteBuffer> writeBufferPool = new ConcurrentLinkedQueue<ByteBuffer>();

	/**
	 * <p>
	 * {@link Selector} to aid in listening for connections.
	 * <p>
	 * This should be considered final as it should be created before any
	 * {@link Team} instances are created.
	 */
	private Selector selector;

	/**
	 * Flag indicating to stop listening.
	 */
	private volatile boolean isStopListening = false;

	/**
	 * Flags indicated that shutdown.
	 */
	private boolean isShutdown = false;

	/**
	 * Initiate.
	 * 
	 * @param socketManager
	 *            {@link SocketManager}.
	 * @param receiveBufferSize
	 *            Receive buffer size.
	 * @param sendBufferSize
	 *            Send buffer size.
	 */
	public SocketListener(SocketManager socketManager, int receiveBufferSize, int sendBufferSize) {
		this.socketManager = socketManager;
		this.sendBufferSize = sendBufferSize;

		// Create the read buffer
		this.readBuffer = ByteBuffer.allocateDirect(receiveBufferSize);
	}

	/**
	 * Open the {@link Selector} for this {@link SocketListener}.
	 * 
	 * @throws IOException
	 *             If fails to open the {@link Selector}.
	 */
	synchronized void openSelector() throws IOException {

		// Do nothing if shutdown
		if (this.isShutdown) {
			return;
		}

		// Open the selector
		this.selector = Selector.open();
	}

	/**
	 * Opens and binds the {@link ServerSocketChannel}.
	 * 
	 * @param serverSocketAddress
	 *            {@link InetSocketAddress} to listen for connections.
	 * @param communicationProtocol
	 *            {@link CommunicationProtocol} for the {@link ServerSocket}.
	 * @param serverSocketBackLogSize
	 *            {@link ServerSocketChannel} back log size.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @throws IOException
	 *             {@link IOException}.
	 */
	synchronized void bindServerSocket(InetSocketAddress serverSocketAddress, int serverSocketBackLogSize,
			CommunicationProtocol communicationProtocol, ManagedObjectExecuteContext<Indexed> executeContext)
			throws IOException {

		// Do not bind if shutdown
		if (this.isShutdown) {
			return;
		}

		// Bind to the socket to start listening
		ServerSocketChannel channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		ServerSocket socket = channel.socket();
		socket.setReuseAddress(true);
		socket.bind(serverSocketAddress, serverSocketBackLogSize);
		this.boundServerSocketChannels.add(channel);

		// Register the channel with the selector
		channel.register(this.selector, SelectionKey.OP_ACCEPT,
				new ServerSocketAttachment(channel, communicationProtocol, executeContext));
	}

	/**
	 * Enable waking up the {@link Selector}.
	 * 
	 * @throws IOException
	 *             If fails to wake up the {@link Selector}.
	 */
	public synchronized void wakeupSelector() throws IOException {
		if (this.selector != null) {
			this.selector.wakeup();
		}
	}

	/**
	 * Close the {@link Selector} for this {@link SocketListener}.
	 * 
	 * @throws IOException
	 *             If fails to close {@link Selector} and any
	 *             {@link ServerSocketChannel} instances.
	 */
	synchronized void closeSelector() throws IOException {

		// Flag to stop listening
		this.isStopListening = true;

		// Wake up the selecter
		if (this.selector != null) {
			this.selector.wakeup();
		}
	}

	/**
	 * Waits for shutdown of the {@link SocketListener}.
	 * 
	 * @throws IOException
	 *             If fails to shut down.
	 */
	synchronized void waitForShutdown() throws IOException {

		// Determine if started
		if (this.selector == null) {
			this.isShutdown = true;
			return;
		}

		// Wait for shut down of this listener
		try {
			do {
				// Wake up the selector
				this.selector.wakeup();

				// Wait some time if not shutdown
				if (!this.isShutdown) {
					this.wait(100);
				}

			} while (!this.isShutdown);
		} catch (InterruptedException ex) {
			// Consider shutdown
			this.isShutdown = true;
		}

		// Unbind the server sockets
		for (ServerSocketChannel channel : this.boundServerSocketChannels) {
			channel.socket().close();
		}
	}

	/**
	 * Registers a {@link AcceptedSocket} with this {@link SocketListener}.
	 * 
	 * @param connection
	 *            {@link AcceptedSocket}.
	 * @throws IOException
	 *             If fails to register the {@link AcceptedSocket}.
	 */
	void registerEstablishedConnection(AcceptedSocket connection) {

		// Add to the queue
		this.establishedConnections.add(connection);

		// Wake up selector to start listening to connection
		this.selector.wakeup();
	}

	/**
	 * Registers the {@link WriteDataAction} to be undertaken on its respective
	 * {@link Connection}.
	 * 
	 * @param writeDataAction
	 *            {@link WriteDataAction} to be undertaken.
	 */
	void registerWriteDataAction(WriteDataAction writeDataAction) {

		// Add to the queue
		this.writeActions.add(writeDataAction);

		// Wake up selector to start writing data
		this.selector.wakeup();
	}

	/**
	 * Obtains a {@link ByteBuffer} for writing.
	 * 
	 * @return {@link ByteBuffer} for writing.
	 */
	ByteBuffer getWriteBufferFromPool() {

		// Determine if one in pool
		ByteBuffer buffer = this.writeBufferPool.poll();
		if (buffer != null) {

			// Clear pooled buffer for use
			buffer.clear();

		} else {
			// No buffers in pool so create a new one
			buffer = ByteBuffer.allocateDirect(this.sendBufferSize);
		}

		// Return the buffer
		return buffer;
	}

	/**
	 * Returns the {@link ByteBuffer} to the pool.
	 */
	void returnWriteBufferToPool(ByteBuffer buffer) {

		// Return buffer to pool
		this.writeBufferPool.add(buffer);
	}

	/**
	 * Obtains the {@link SelectionKeyAttachment} for the {@link SelectionKey}.
	 * 
	 * @param selectionKey
	 *            {@link SelectionKey}.
	 * @return {@link SelectionKeyAttachment}.
	 */
	private SelectionKeyAttachment getSelectionKeyAttachment(SelectionKey selectionKey) {
		return (SelectionKeyAttachment) selectionKey.attachment();
	}

	/*
	 * ====================== ManagedFunction ======================
	 */

	@Override
	public Object execute(ManagedFunctionContext<None, SocketListenerFlows> context) throws Exception {

		// Start listening on the established connections.
		// Use size to only do currently added.
		int establishedConnectionCount = this.establishedConnections.size();
		AcceptedSocket establishedConnection;
		while ((establishedConnectionCount-- > 0)
				&& ((establishedConnection = this.establishedConnections.poll()) != null)) {

			// Obtains the socket channel
			SocketChannel socketChannel = establishedConnection.getSocketChannel();

			// Register connection with selector
			SelectionKey selectionKey = socketChannel.register(this.selector, SelectionKey.OP_READ);

			// Create the connection
			ManagedConnection connection = new ConnectionImpl(selectionKey, socketChannel, this,
					establishedConnection.getCommunicationProtocol(),
					establishedConnection.getManagedObjectExecuteContext());

			// Associate connection to its selection key
			selectionKey.attach(connection);
		}

		// Undertake the the connection actions.
		// Use size to only do currently added.
		int actionCount = this.writeActions.size();
		WriteDataAction action;
		while ((actionCount-- > 0) && ((action = this.writeActions.poll()) != null)) {

			// Obtain the connection
			ManagedConnection connection = action.getConnection();

			// Undertake queued writes on connection
			if (!(connection.processWriteQueue())) {
				// Take interest in write (socket buffer clearing)
				connection.getSelectionKey().interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			}
		}

		// Determine if stop listening
		if (this.isStopListening) {
			synchronized (this) {

				// Determine if selector closed by another thread
				if (!this.selector.isOpen()) {
					// Selector already closed, so ready for shutdown
					this.isShutdown = true;
					this.notify();
					return null; // shutdown
				}

				// Terminate all connections
				Set<SelectionKey> allKeys = this.selector.keys();

				// Determine if connections to notify (need another select)
				if (allKeys.size() == 0) {
					try {
						// Close the selector
						this.selector.close();
					} catch (IOException ex) {
						if (LOGGER.isLoggable(Level.WARNING)) {
							LOGGER.log(Level.WARNING, "Failed to close selector", ex);
						}
					}

					// May complete as no further connections
					this.isShutdown = true;
					this.notify();
					return null; // shutdown

				} else {
					// Force remaining connections to close
					for (SelectionKey key : allKeys) {
						SelectionKeyAttachment attachment = this.getSelectionKeyAttachment(key);
						ManagedConnection connection = attachment.getManagedConnection();
						if (connection != null) {
							// Terminate the connection (canceling key)
							connection.terminate();
						} else {
							// Cancel the acceptor key
							key.cancel();
						}
					}
				}
			}
		}

		// Listen on the socket
		this.selector.select(1000); // 1 second

		// Obtain the all keys and selected keys
		Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

		// Service the selected keys
		NEXT_KEY: for (SelectionKey selectedKey : selectedKeys) {

			// Obtain the ready operations
			int readyOps = selectedKey.readyOps();

			// Check if accepting a connection
			if ((readyOps & SelectionKey.OP_ACCEPT) != 0) {

				// Obtain the socket channel
				ServerSocketAttachment attachment = (ServerSocketAttachment) selectedKey.attachment();
				final SocketChannel socketChannel = attachment.channel.accept();
				if (socketChannel != null) {

					// Flag socket as unblocking
					socketChannel.configureBlocking(false);

					// Configure the socket
					Socket socket = socketChannel.socket();
					socket.setTcpNoDelay(true);

					// Manage the established socket
					this.socketManager.manageSocket(new AcceptedSocketImpl(socketChannel,
							attachment.communicationProtocol, attachment.executeContext));
				}

				// Accepted the connection
				continue NEXT_KEY;
			}

			// Obtain the connection details
			SelectionKeyAttachment attachment = this.getSelectionKeyAttachment(selectedKey);
			ManagedConnection connection = attachment.getManagedConnection();
			if (connection == null) {
				continue NEXT_KEY;
			}
			SocketChannel socketChannel = connection.getSocketChannel();
			try {

				// Determine if read content
				if ((readyOps & SelectionKey.OP_READ) != 0) {

					// Keep reading data until empty socket buffer
					boolean isFurtherDataToRead = true;
					while (isFurtherDataToRead) {

						// Obtain the buffer
						ByteBuffer buffer = this.readBuffer.duplicate();

						// Read content from channel
						int bytesRead;
						try {
							bytesRead = socketChannel.read(buffer);
						} catch (IOException ex) {
							// Connection failed
							connection.terminate();
							continue NEXT_KEY;
						}

						// Determine if closed connection
						if (bytesRead < 0) {
							connection.terminate();
							continue NEXT_KEY;
						}

						// Obtain the data and handle
						buffer.flip();
						this.readData = new byte[bytesRead];
						buffer.get(this.readData);
						connection.getConnectionHandler().handleRead(this);

						// Determine if further data
						if (bytesRead < this.readBuffer.limit()) {
							isFurtherDataToRead = false;
						}
					}
				}

				// Determine if send data consumed for further write
				if ((readyOps & SelectionKey.OP_WRITE) != 0) {

					// Process the write queue for the connection
					if (connection.processWriteQueue()) {
						// All data written, just listen
						selectedKey.interestOps(SelectionKey.OP_READ);
					}
				}

			} catch (CancelledKeyException ex) {
				// Key cancelled, ensure connection closed
				connection.terminate();
			}
		}

		// Clear the selected keys as now serviced
		selectedKeys.clear();

		// Execute again and again until shutdown
		context.doFlow(SocketListenerFlows.REPEAT, null, null);
		return null;
	}

	/*
	 * ================== ReadContext =====================================
	 * ReadContext does not require thread-safety as only accessed by the same
	 * Thread.
	 */

	/**
	 * Data just read for the {@link ReadContext}.
	 */
	private byte[] readData;

	@Override
	public byte[] getData() {
		return this.readData;
	}

	/*
	 * =================== HeartbeatContext ===============================
	 * HeartbeatContext does not require thread-safety as only accessed by the
	 * same Thread.
	 */

	// No specific methods for HeartbeatContext.

	/**
	 * {@link ServerSocketChannel} attachment.
	 */
	private class ServerSocketAttachment implements SelectionKeyAttachment {

		/**
		 * {@link ServerSocketChannel}.
		 */
		private final ServerSocketChannel channel;

		/**
		 * {@link CommunicationProtocol}.
		 */
		private final CommunicationProtocol communicationProtocol;

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private final ManagedObjectExecuteContext<Indexed> executeContext;

		/**
		 * Instantiate.
		 * 
		 * @param channel
		 *            {@link ServerSocketChannel}.
		 * @param communicationProtocol
		 *            {@link CommunicationProtocol}.
		 * @param executeContext
		 *            {@link ManagedObjectExecuteContext}.
		 */
		public ServerSocketAttachment(ServerSocketChannel channel, CommunicationProtocol communicationProtocol,
				ManagedObjectExecuteContext<Indexed> executeContext) {
			this.channel = channel;
			this.communicationProtocol = communicationProtocol;
			this.executeContext = executeContext;
		}

		/*
		 * ================ SelectionKeyAttachment ==================
		 */

		@Override
		public ManagedConnection getManagedConnection() {
			return null;
		}
	}

	/**
	 * {@link AcceptedSocket} implementation.
	 */
	private class AcceptedSocketImpl implements AcceptedSocket {

		/**
		 * {@link SocketChannel}.
		 */
		private final SocketChannel socketChannel;

		/**
		 * {@link CommunicationProtocol}.
		 */
		private final CommunicationProtocol communicationProtocol;

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private final ManagedObjectExecuteContext<Indexed> executeContext;

		/**
		 * Initiate.
		 * 
		 * @param socketChannel
		 *            {@link SocketChannel}.
		 * @param communicationProtocol
		 *            {@link CommunicationProtocol}.
		 * @param executeContext
		 *            {@link ManagedObjectExecuteContext}.
		 */
		private AcceptedSocketImpl(SocketChannel socketChannel, CommunicationProtocol communicationProtocol,
				ManagedObjectExecuteContext<Indexed> executeContext) {
			this.socketChannel = socketChannel;
			this.communicationProtocol = communicationProtocol;
			this.executeContext = executeContext;
		}

		/*
		 * =================== AcceptedConnection =====================
		 */

		@Override
		public SocketChannel getSocketChannel() {
			return this.socketChannel;
		}

		@Override
		public CommunicationProtocol getCommunicationProtocol() {
			return this.communicationProtocol;
		}

		@Override
		public ManagedObjectExecuteContext<Indexed> getManagedObjectExecuteContext() {
			return this.executeContext;
		}
	}

}