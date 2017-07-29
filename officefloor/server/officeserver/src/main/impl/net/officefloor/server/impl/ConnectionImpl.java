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
package net.officefloor.server.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.server.ManagedConnection;
import net.officefloor.server.WriteDataAction;
import net.officefloor.server.protocol.CommunicationProtocol;
import net.officefloor.server.protocol.Connection;
import net.officefloor.server.protocol.ConnectionHandler;
import net.officefloor.server.protocol.WriteBuffer;
import net.officefloor.server.protocol.WriteBufferEnum;

/**
 * Implementation of a {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionImpl implements Connection, ManagedConnection, SelectionKeyAttachment, WriteDataAction {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConnectionImpl.class.getName());

	/**
	 * {@link SelectionKey} for this {@link Connection}.
	 */
	private final SelectionKey selectionKey;

	/**
	 * {@link SocketChannel} for this {@link Connection}.
	 */
	private final SocketChannel socketChannel;

	/**
	 * {@link ConnectionHandler} for this {@link Connection}.
	 */
	private final ConnectionHandler connectionHandler;

	/**
	 * {@link SocketListener} handling this {@link Connection}.
	 */
	private final SocketListener socketListener;

	/**
	 * {@link Queue} of {@link WriteAction} instances.
	 */
	private final Queue<WriteAction> writeActions = new ArrayDeque<WriteAction>();

	/**
	 * Indicates if registered for write.
	 */
	private boolean isRegisteredForWrite = false;

	/**
	 * Flags whether to terminate after {@link WriteAction} instances.
	 */
	private volatile boolean isTerminateAfterWrites = false;

	/**
	 * Flags if this {@link Connection} has been closed.
	 */
	private volatile boolean isClosed = false;

	/**
	 * Initiate.
	 * 
	 * @param selectionKey
	 *            {@link SelectionKey} for this {@link Connection}.
	 * @param socketChannel
	 *            {@link SocketChannel} for this {@link Connection}.
	 * @param socketListener
	 *            {@link SocketListener}.
	 * @param communicationProtocol
	 *            {@link CommunicationProtocol}.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 */
	public ConnectionImpl(SelectionKey selectionKey, SocketChannel socketChannel, SocketListener socketListener,
			CommunicationProtocol communicationProtocol, ManagedObjectExecuteContext<Indexed> executeContext) {
		this.selectionKey = selectionKey;
		this.socketChannel = socketChannel;
		this.socketListener = socketListener;

		// Create the connection handler for this connection
		this.connectionHandler = communicationProtocol.createConnectionHandler(this, executeContext);
	}

	/*
	 * ================= Connection =======================================
	 */

	@Override
	public Object getWriteLock() {
		return this;
	}

	@Override
	public boolean isSecure() {
		// Connection is not secure
		return false;
	}

	@Override
	public WriteBuffer createWriteBuffer(byte[] data, int length) {
		return new ArrayWriteBuffer(data, length);
	}

	@Override
	public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
		return new BufferWriteBuffer(buffer);
	}

	@Override
	public void writeData(WriteBuffer[] data) throws IOException {

		synchronized (this.getWriteLock()) {

			// Ignore write action if terminating
			if (this.isTerminateAfterWrites) {

				// Should not be writing data after terminating connection
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Attempting to write data after closing connection");
				}

				// Terminating so no further writes
				return;
			}

			// Queue the write action
			this.writeActions.add(new WriteAction(data));

			// Flush the write actions
			this.flushWrites();
		}
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		Socket socket = this.socketChannel.socket();
		return new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		Socket socket = this.socketChannel.socket();
		return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
	}

	@Override
	public void close() throws IOException {

		synchronized (this.getWriteLock()) {

			// Indicate closed
			this.isClosed = true;

			// Flag to terminate after last write
			this.isTerminateAfterWrites = true;

			// Flush the writes
			this.flushWrites();
		}
	}

	@Override
	public boolean isClosed() {
		return this.isClosed;
	}

	/**
	 * Flushes the writes and indicates if need to register as
	 * {@link WriteDataAction}.
	 * 
	 * @return <code>true</code> if to register as {@link WriteDataAction}.
	 * @throws IOException
	 *             If fails to flush the data.
	 */
	private void flushWrites() throws IOException {

		// Only process write queue if not queued for write action
		if (!this.isRegisteredForWrite) {

			// Process the write data queue
			if (!(this.processWriteQueue())) {

				// Socket buffer full, register write action
				this.isRegisteredForWrite = true;
				this.socketListener.registerWriteDataAction(this);
			}
		}
	}

	/*
	 * ================ SelectionKeyAttachment ================
	 */

	@Override
	public ManagedConnection getManagedConnection() {
		return this;
	}

	/*
	 * ==================== ManagedConnection ============================
	 * 
	 * Only called by the SocketListener thread, so thread safe.
	 */

	@Override
	public SelectionKey getSelectionKey() {
		return this.selectionKey;
	}

	@Override
	public SocketChannel getSocketChannel() {
		return this.socketChannel;
	}

	@Override
	public ConnectionHandler getConnectionHandler() {
		return this.connectionHandler;
	}

	@Override
	public boolean processWriteQueue() throws IOException {

		synchronized (this.getWriteLock()) {

			// No longer registered for write
			this.isRegisteredForWrite = false;

			try {

				// Write the data for each queued write action
				for (Iterator<WriteAction> iterator = this.writeActions.iterator(); iterator.hasNext();) {

					// Undertake the current write action
					WriteAction action = iterator.next();
					if (!(action.writeData())) {
						return false; // further writes required
					}

					// Action written, so remove
					iterator.remove();
				}

			} catch (IOException ex) {

				// Indicate failure to write the data
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Failed to write data", ex);
				}

				// Terminate connection immediately
				this.terminate();
			}

			// All writes undertaken, determine if terminate
			if (this.isTerminateAfterWrites) {
				this.terminate();
			}

			// All actions undertaken
			return true;
		}
	}

	@Override
	public void terminate() throws IOException {

		synchronized (this.getWriteLock()) {

			// Ensure flagged as closed and terminated
			this.isClosed = true;
			this.isTerminateAfterWrites = true;

			// Terminate connection
			try {
				this.socketChannel.close();
				this.selectionKey.cancel();
			} catch (ClosedChannelException ex) {
				// Already closed
			}

			// Return all buffers to pool and clear any writes
			for (WriteAction action : this.writeActions) {
				action.cleanup();
			}
			this.writeActions.clear();
		}
	}

	/*
	 * ================= WriteDataAction ===========================
	 */

	@Override
	public ManagedConnection getConnection() {
		return this;
	}

	/**
	 * {@link ByteBuffer} instances ready for writing.
	 */
	private class WriteAction {

		/**
		 * Filled {@link ByteBuffer} instances ready to write to the client.
		 */
		private final ByteBuffer[] buffers;

		/**
		 * Corresponding flags indicating if a pooled {@link ByteBuffer}.
		 */
		private final boolean[] isPooled;

		/**
		 * Starting index to write the {@link ByteBuffer} instances.
		 */
		private int startIndex = 0;

		/**
		 * Index to load the next filled {@link ByteBuffer}.
		 */
		private int nextIndex = 0;

		/**
		 * Initiate.
		 * 
		 * @param writeBuffers
		 *            {@link WriteBuffer} providing the data to write.
		 */
		public WriteAction(WriteBuffer[] writeBuffers) {

			/*
			 * Create the filled buffers. As the array data length should not
			 * exceed the pooled ByteBuffer sizes, there should never be more
			 * ByteBuffers than WriteBuffers.
			 */
			this.buffers = new ByteBuffer[writeBuffers.length];
			this.isPooled = new boolean[this.buffers.length];

			// Populate the array of byte buffers to write
			ByteBuffer currentBuffer = ConnectionImpl.this.socketListener.getWriteBufferFromPool();
			for (int i = 0; i < writeBuffers.length; i++) {
				WriteBuffer writeBuffer = writeBuffers[i];

				// Handle loading the buffers
				WriteBufferEnum type = writeBuffer.getType();
				switch (type) {
				case BYTE_ARRAY:
					// Write the data into the buffer
					byte[] data = writeBuffer.getData();
					int length = writeBuffer.length();

					// Load all the data into buffers
					int offset = 0;
					while (offset < length) {

						// Obtain the available in the current buffer
						int remaining = currentBuffer.remaining();

						// Determine amount of data to load into current buffer
						int loadSize = Math.min((length - offset), remaining);

						// Load the data (reducing remaining length to write)
						currentBuffer.put(data, offset, loadSize);
						offset += loadSize;

						// Determine if space available on current buffer
						if (currentBuffer.remaining() == 0) {
							// Load in the current buffer for writing
							currentBuffer.flip();
							this.addBuffer(currentBuffer, true);

							// Obtain new buffer for next write
							currentBuffer = ConnectionImpl.this.socketListener.getWriteBufferFromPool();
						}
					}
					break;

				case BYTE_BUFFER:
					// Include the current buffer (if has data)
					if (currentBuffer.position() > 0) {
						// Include the current buffer
						currentBuffer.flip();
						this.addBuffer(currentBuffer, true);

						// Obtain the next current buffer
						currentBuffer = ConnectionImpl.this.socketListener.getWriteBufferFromPool();
					}

					// Include the cached buffer
					this.addBuffer(writeBuffer.getDataBuffer(), false);
					break;

				default:
					throw new IllegalStateException("Unknown " + WriteBuffer.class.getSimpleName() + " type: " + type);
				}
			}

			// Include the current buffer (if has data)
			if (currentBuffer.position() > 0) {
				// Include the current buffer
				currentBuffer.flip();
				this.addBuffer(currentBuffer, true);

			} else {
				// Return to current buffer to pool as not used
				ConnectionImpl.this.socketListener.returnWriteBufferToPool(currentBuffer);
			}
		}

		/**
		 * Writes the data to the {@link ManagedConnection}.
		 * 
		 * @return <code>true</code> if all data has been written.
		 *         <code>false</code> if further data to write.
		 * @throws IOException
		 *             If fails to write the data.
		 */
		public boolean writeData() throws IOException {

			// Write the data
			SocketChannel socketChannel = ConnectionImpl.this.getSocketChannel();
			IOException failure = null;
			try {
				socketChannel.write(this.buffers, this.startIndex, (this.nextIndex - this.startIndex));
			} catch (IOException ex) {
				failure = ex;
			}

			// Return written buffers to the pool (or all if failure)
			while ((this.startIndex < this.nextIndex)
					&& (((this.buffers[this.startIndex].remaining() == 0)) || (failure != null))) {

				// Buffer written or connection failure, return to pool
				if (this.isPooled[this.startIndex]) {
					ConnectionImpl.this.socketListener.returnWriteBufferToPool(this.buffers[this.startIndex]);
				}

				// Start writing from next buffer
				this.startIndex++;
			}

			// Propagate the failure
			if (failure != null) {
				throw failure;
			}

			// All data written if no remaining buffers to write
			return (this.startIndex >= this.nextIndex);
		}

		/**
		 * Returns all {@link ByteBuffer} instances to the pool regardless of
		 * whether they are written or not.
		 */
		private void cleanup() {

			// Return all buffers to pool
			for (int i = this.startIndex; i < this.nextIndex; i++) {
				if (this.isPooled[i]) {
					ConnectionImpl.this.socketListener.returnWriteBufferToPool(this.buffers[i]);
				}
			}
		}

		/**
		 * Adds a pooled {@link ByteBuffer}.
		 * 
		 * @param buffer
		 *            Pooled {@link ByteBuffer}.
		 * @param isPooled
		 *            Indicates if pooled.
		 */
		private void addBuffer(ByteBuffer buffer, boolean isPooled) {
			this.buffers[this.nextIndex] = buffer;
			this.isPooled[this.nextIndex] = isPooled;
			this.nextIndex++;
		}
	}

}