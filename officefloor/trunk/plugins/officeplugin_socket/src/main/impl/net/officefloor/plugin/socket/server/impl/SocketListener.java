/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.ConnectionAction;
import net.officefloor.plugin.socket.server.ConnectionActionEnum;
import net.officefloor.plugin.socket.server.ConnectionManager;
import net.officefloor.plugin.socket.server.EstablishedConnection;
import net.officefloor.plugin.socket.server.ManagedConnection;
import net.officefloor.plugin.socket.server.WriteDataAction;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandlerContext;
import net.officefloor.plugin.socket.server.protocol.HeartBeatContext;
import net.officefloor.plugin.socket.server.protocol.ReadContext;

/**
 * Listens to {@link Socket} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketListener extends
		AbstractSingleTask<SocketListener, None, Indexed> implements
		ReadContext, HeartBeatContext {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(SocketListener.class
			.getName());

	/**
	 * {@link Queue} of {@link EstablishedConnection} instances.
	 */
	private final Queue<EstablishedConnection> establishedConnections = new ConcurrentLinkedQueue<EstablishedConnection>();

	/**
	 * {@link Queue} of {@link ConnectionAction} instances to be undertaken on
	 * their respective {@link Connection}.
	 */
	private final Queue<ConnectionAction> connectionActions = new ConcurrentLinkedQueue<ConnectionAction>();

	/**
	 * {@link ByteBuffer} to use for reading content.
	 */
	private final ByteBuffer readBuffer;

	/**
	 * Send {@link ByteBuffer} size.
	 */
	private final int sendBufferSize;

	/**
	 * Pool of {@link ByteBuffer} instances.
	 */
	private final Deque<ByteBuffer> writeBufferPool = new ArrayDeque<ByteBuffer>();

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
	 * Initiate.
	 * 
	 * @param sendBufferSize
	 *            Send buffer size.
	 * @param receiveBufferSize
	 *            Receive buffer size.
	 */
	public SocketListener(int sendBufferSize, int receiveBufferSize) {
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
	void openSelector() throws IOException {
		// Open the selector
		this.selector = Selector.open();
	}

	/**
	 * Close the {@link Selector} for this {@link SocketListener}.
	 */
	void closeSelector() {

		// Flag to stop listening
		this.isStopListening = true;

		// Wake up the selector (to close out)
		this.selector.wakeup();
	}

	/**
	 * Registers a {@link EstablishedConnection} with this
	 * {@link SocketListener}.
	 * 
	 * @param connection
	 *            {@link EstablishedConnection}.
	 * @throws IOException
	 *             If fails to register the {@link EstablishedConnection}.
	 */
	void registerEstablishedConnection(EstablishedConnection connection) {

		// Add to the queue
		this.establishedConnections.add(connection);

		// Wake up selector to start listening to connection
		this.selector.wakeup();
	}

	/**
	 * Registers the {@link ConnectionAction} to be undertaken on its respective
	 * {@link Connection}.
	 * 
	 * @param connectionAction
	 *            {@link ConnectionAction} to be undertaken.
	 */
	void doConnectionAction(ConnectionAction connectionAction) {

		// Add to the queue
		this.connectionActions.add(connectionAction);

		// Wake up selector to start writing data
		this.selector.wakeup();
	}

	/**
	 * Undertakes a heart beat for this {@link ConnectionManager}.
	 */
	void doHeartBeat() {

		synchronized (this) {

			// Reset time (for optimising obtaining time in heart beats)
			this.currentTime = -1;

			// Provide heart beat to all connections
			if (this.selector.isOpen()) {
				for (SelectionKey selectionKey : this.selector.keys()) {

					// Obtain the connection
					ManagedConnection connection = this
							.getManagedConnection(selectionKey);

					// Undertake the heart beat
					try {
						connection.getConnectionHandler().handleHeartbeat(this);
					} catch (IOException ex) {
						if (LOGGER.isLoggable(Level.FINE)) {
							LOGGER.log(Level.FINE,
									"Failed heart beat for connection", ex);
						}
					}
				}
			}
		}
	}

	/**
	 * Obtains a {@link ByteBuffer} for writing.
	 * 
	 * @return {@link ByteBuffer} for writing.
	 */
	ByteBuffer getWriteBufferFromPool() {

		// Determine if one in pool
		ByteBuffer buffer = this.writeBufferPool.poll();
		if (buffer == null) {

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

		// Reset buffer
		buffer.position(0);
		buffer.limit(buffer.capacity());

		// Return buffer to pool
		this.writeBufferPool.push(buffer);
	}

	/**
	 * Obtains the {@link ManagedConnection} for the {@link SelectionKey}.
	 * 
	 * @param selectionKey
	 *            {@link SelectionKey}.
	 * @return {@link ManagedConnection}.
	 */
	private ManagedConnection getManagedConnection(SelectionKey selectionKey) {
		return (ManagedConnection) selectionKey.attachment();
	}

	/*
	 * ====================== Task =======================================
	 */

	@Override
	public Object doTask(TaskContext<SocketListener, None, Indexed> context)
			throws Exception {

		// Synchronising as may be invoked by differing threads
		synchronized (this) {

			// Flag to loop forever (or until told to stop listening)
			context.setComplete(false);

			// Start listening on the established connections.
			// Use size to only do currently added.
			int establishedConnectionCount = this.establishedConnections.size();
			EstablishedConnection establishedConnection;
			while ((establishedConnectionCount-- > 0)
					&& ((establishedConnection = this.establishedConnections
							.poll()) != null)) {

				// Obtains the socket channel
				SocketChannel socketChannel = establishedConnection
						.getSocketChannel();

				// Register connection with selector
				SelectionKey selectionKey = socketChannel.register(
						this.selector, SelectionKey.OP_READ);

				// Create the connection
				ManagedConnection connection = new ConnectionImpl(selectionKey,
						socketChannel,
						establishedConnection.getCommunicationProtocol(), this);

				// Associate connection to its selection key
				selectionKey.attach(connection);
			}

			// Undertake the the connection actions.
			// Use size to only do currently added.
			int actionCount = this.connectionActions.size();
			ConnectionAction action;
			while ((actionCount-- > 0)
					&& ((action = this.connectionActions.poll()) != null)) {

				// Obtain the connection details
				ManagedConnection connection = action.getConnection();

				// Queue the action on the connection
				ConnectionActionEnum actionType = action.getType();
				switch (actionType) {

				case CLOSE_CONNECTION:
					// Queue closing the connection
					connection.queueClose();
					break;

				case WRITE_DATA:
					// Queue writing the data
					WriteDataAction writeDataAction = (WriteDataAction) action;
					connection.queueWrite(writeDataAction);
					break;

				default:
					throw new IllegalStateException("Unknown action type: "
							+ actionType);
				}

				// Undertake queued actions on connection
				if (!(connection.processWriteQueue())) {
					// Take interest in write (socket buffer clearing)
					connection.getSelectionKey().interestOps(
							SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				}
			}

			// Determine if stop listening
			if (this.isStopListening) {

				// Terminate all connections
				Set<SelectionKey> allKeys = this.selector.keys();

				// Determine if connections to notify (need another select)
				if (allKeys.size() == 0) {
					try {
						// Close the selector
						this.selector.close();
					} catch (IOException ex) {
						if (LOGGER.isLoggable(Level.WARNING)) {
							LOGGER.log(Level.WARNING,
									"Failed to close selector", ex);
						}
					}

					// May complete as no further connections
					context.setComplete(true);
					return null; // shut

				} else {
					// Force remaining connections to close
					for (SelectionKey key : allKeys) {
						this.getManagedConnection(key).terminate();
					}
				}
			}

			// Listen on the socket
			this.selector.select(1000); // 1 second

			// Reset time (for optimising obtaining time in reads)
			this.currentTime = -1;

			// Obtain the all keys and selected keys
			Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

			// Service the selected keys
			NEXT_KEY: for (SelectionKey selectedKey : selectedKeys) {

				// Obtain the connection details
				ManagedConnection connection = this
						.getManagedConnection(selectedKey);
				SocketChannel socketChannel = connection.getSocketChannel();

				// Determine if read content
				if (selectedKey.isReadable()) {

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
							isFurtherDataToRead = false; // all data read
						}
					}
				}

				// Determine if send data consumed for further write
				if (selectedKey.isWritable()) {

					// Process the write queue for the connection
					if (connection.processWriteQueue()) {
						// All data written, just listen
						try {
							selectedKey.interestOps(SelectionKey.OP_READ);
						} catch (CancelledKeyException ex) {
							// Ignore as connection is closed
						}
					}
				}
			}

			// Clear the selected keys as now serviced
			selectedKeys.clear();
		}

		// No return (as should be executed again and again until shutdown)
		return null;
	}

	/*
	 * =============== ConnectionHandlerContext ===============================
	 * Does not require thread-safety as only accessed by the same Thread.
	 */

	/**
	 * Current time for {@link ConnectionHandlerContext}.
	 */
	private long currentTime = -1;

	@Override
	public long getTime() {

		// Lazy obtain the time (cleared for each run)
		if (this.currentTime == -1) {
			this.currentTime = System.currentTimeMillis();
		}

		// Return the current time
		return this.currentTime;
	}

	/*
	 * ================== ReadContext =====================================
	 * ReadContext does not require thread-safety as only accessed by the same
	 * Thread.
	 */

	/**
	 * Data just read for the {@link ConnectionHandlerContext}.
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

}