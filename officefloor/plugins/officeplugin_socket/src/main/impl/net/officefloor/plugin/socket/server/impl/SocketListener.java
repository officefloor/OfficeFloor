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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.team.Team;
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
public class SocketListener implements ManagedFunctionFactory<None, SocketListener.SocketListenerFlows>,
		ManagedFunction<None, SocketListener.SocketListenerFlows>, ReadContext, HeartBeatContext {

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
	 * Heart beat interval in milliseconds.
	 */
	private final long heartbeatInterval;

	/**
	 * {@link Queue} of {@link EstablishedConnection} instances.
	 */
	private final Queue<EstablishedConnection> establishedConnections = new ConcurrentLinkedQueue<EstablishedConnection>();

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
	 * Initiate.
	 * 
	 * @param heartbeatInterval
	 *            Heart beat interval.
	 * @param sendBufferSize
	 *            Send buffer size.
	 * @param receiveBufferSize
	 *            Receive buffer size.
	 */
	public SocketListener(long heartbeatInterval, int sendBufferSize, int receiveBufferSize) {
		this.heartbeatInterval = heartbeatInterval;
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

		// Run function until complete (which closes connections and selector)
		try {
			CloseSelectorMangedFunctionContext context = new CloseSelectorMangedFunctionContext();
			do {
				context.isComplete = true;
				this.execute(context);
			} while (!context.isComplete);
		} catch (Exception ex) {
			if (LOGGER.isLoggable(Level.WARNING)) {
				LOGGER.log(Level.WARNING, "Failed to close Selector", ex);
			}
		}
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
	 * Undertakes a heart beat for this {@link ConnectionManager}.
	 */
	void doHeartBeat() {

		// TODO make heart beat part of the socket listener loop

		// Reset time (for optimising obtaining time in heart beats)
		this.currentTime = -1;

		// Provide heart beat to all connections
		if (this.selector.isOpen()) {
			for (SelectionKey selectionKey : this.selector.keys()) {

				// Obtain the connection
				ManagedConnection connection = this.getManagedConnection(selectionKey);

				// Undertake the heart beat
				try {
					connection.getConnectionHandler().handleHeartbeat(this);
				} catch (IOException ex) {
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.log(Level.FINE, "Failed heart beat for connection", ex);
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
	 * ================== ManagedFunctionFactory ===================
	 */

	@Override
	public ManagedFunction<None, SocketListenerFlows> createManagedFunction() throws Throwable {
		return this;
	}

	/*
	 * ====================== ManagedFunction ======================
	 */

	@Override
	public Object execute(ManagedFunctionContext<None, SocketListenerFlows> context) throws Exception {

		// Start listening on the established connections.
		// Use size to only do currently added.
		int establishedConnectionCount = this.establishedConnections.size();
		EstablishedConnection establishedConnection;
		while ((establishedConnectionCount-- > 0)
				&& ((establishedConnection = this.establishedConnections.poll()) != null)) {

			// Obtains the socket channel
			SocketChannel socketChannel = establishedConnection.getSocketChannel();

			// Register connection with selector
			SelectionKey selectionKey = socketChannel.register(this.selector, SelectionKey.OP_READ);

			// Create the connection
			ManagedConnection connection = new ConnectionImpl(selectionKey, socketChannel,
					establishedConnection.getCommunicationProtocol(), this);

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

			// Determine if selector closed by another thread
			if (!this.selector.isOpen()) {
				// Selector already closed, so ready for shutdown
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
				return null; // shutdown

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
			ManagedConnection connection = this.getManagedConnection(selectedKey);
			SocketChannel socketChannel = connection.getSocketChannel();

			try {

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
							isFurtherDataToRead = false; // all data
															// read
						}
					}
				}

				// Determine if send data consumed for further write
				if (selectedKey.isWritable()) {

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

	/**
	 * <p>
	 * Close {@link Selector} {@link ManagedFunctionContext}.
	 * <p>
	 * Enables running this {@link ManagedFunction} until the {@link Selector}
	 * is closed.
	 */
	private class CloseSelectorMangedFunctionContext
			implements ManagedFunctionContext<None, SocketListener.SocketListenerFlows> {

		/**
		 * Indicates if complete.
		 */
		private boolean isComplete = true;

		/*
		 * =================== ManagedFunctionContext =========================
		 */

		@Override
		public Object getObject(None key) {
			throw new IllegalStateException("No object should be required on closing listener selector");
		}

		@Override
		public Object getObject(int dependencyIndex) {
			throw new IllegalStateException("No object should be required on closing listener selector");
		}

		@Override
		public void doFlow(SocketListener.SocketListenerFlows key, Object parameter, FlowCallback callback) {
			// Flow invoked, so not yet complete
			this.isComplete = false;
		}

		@Override
		public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {
			throw new IllegalStateException("No indexed flow should be required on closing listener selector");
		}

		@Override
		public void doFlow(String functionkName, Object parameter, FlowCallback callback)
				throws UnknownFunctionException, InvalidParameterTypeException {
			throw new IllegalStateException("No dynamic flow should be required on closing listener selector");
		}
	}

}