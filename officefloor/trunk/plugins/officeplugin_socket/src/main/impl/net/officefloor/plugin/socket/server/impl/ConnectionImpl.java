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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.plugin.socket.server.CloseConnectionAction;
import net.officefloor.plugin.socket.server.ConnectionActionEnum;
import net.officefloor.plugin.socket.server.ManagedConnection;
import net.officefloor.plugin.socket.server.WriteDataAction;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBufferEnum;

/**
 * Implementation of a {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionImpl implements Connection, ManagedConnection,
		CloseConnectionAction {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConnectionImpl.class
			.getName());

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
	private final Queue<WriteAction> writeActions = new LinkedList<WriteAction>();

	/**
	 * Flags whether to terminate after {@link WriteAction} instances.
	 */
	private boolean isTerminateAfterWrites = false;

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
	 * @param communicationProtocol
	 *            {@link CommunicationProtocol}.
	 * @param socketListener
	 *            {@link SocketListener}.
	 */
	public ConnectionImpl(SelectionKey selectionKey,
			SocketChannel socketChannel,
			CommunicationProtocol communicationProtocol,
			SocketListener socketListener) {
		this.selectionKey = selectionKey;
		this.socketChannel = socketChannel;
		this.socketListener = socketListener;

		// Create the connection handler for this connection
		this.connectionHandler = communicationProtocol
				.createConnectionHandler(this);
	}

	/*
	 * ================= Connection =======================================
	 */

	@Override
	public Object getLock() {
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
	public void writeData(WriteBuffer[] data) {
		this.socketListener.doConnectionAction(new WriteDataActionImpl(this,
				data));
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		Socket socket = this.socketChannel.socket();
		return new InetSocketAddress(socket.getLocalAddress(),
				socket.getLocalPort());
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		Socket socket = this.socketChannel.socket();
		return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
	}

	@Override
	public void close() {
		// Close and trigger for closing (after all data written)
		this.isClosed = true;
		this.socketListener.doConnectionAction(this);
	}

	@Override
	public boolean isClosed() {
		return this.isClosed;
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
	public void queueWrite(WriteDataAction action) {

		// Ignore write action if terminating
		if (this.isTerminateAfterWrites) {

			// Should not be writing data after terminating connection
			if (LOGGER.isLoggable(Level.WARNING)) {
				LOGGER.log(Level.WARNING,
						"Attempting to write data after closing connection");
			}

			// Terminating so no further writes
			return;
		}

		// Queue the write action
		this.writeActions.add(new WriteAction(action));
	}

	@Override
	public void queueClose() {
		// Flag to terminate after last write
		this.isTerminateAfterWrites = true;
	}

	@Override
	public boolean processWriteQueue() throws IOException {

		// Write the data for each queued write action
		for (Iterator<WriteAction> iterator = this.writeActions.iterator(); iterator
				.hasNext();) {

			// Undertake the current write action
			WriteAction action = iterator.next();
			if (!(action.writeData())) {
				return false; // further writes required
			}

			// Action written, so remove
			iterator.remove();
		}

		// All writes undertaken, determine if terminate
		if (this.isTerminateAfterWrites) {
			this.terminate();
		}

		// All actions undertaken
		return true;
	}

	@Override
	public void terminate() throws IOException {

		// Ensure flagged as closed and terminated
		this.isClosed = true;
		this.isTerminateAfterWrites = true;

		// Terminate connection
		this.socketChannel.close();
		this.selectionKey.cancel();

		// Return all buffers to pool
		for (WriteAction action : this.writeActions) {
			action.cleanup();
		}
	}

	/*
	 * ================= CloseConnectionAction ===========================
	 */

	@Override
	public ConnectionActionEnum getType() {
		return ConnectionActionEnum.CLOSE_CONNECTION;
	}

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
		 * @param action
		 *            {@link WriteDataAction} providing the data to write.
		 */
		public WriteAction(WriteDataAction action) {

			// Obtain the data to write
			WriteBuffer[] writeBuffers = action.getData();

			/*
			 * Create the filled buffers. As the array data length should not
			 * exceed the pooled ByteBuffer sizes, there should never be more
			 * ByteBuffers than WriteBuffers.
			 */
			this.buffers = new ByteBuffer[writeBuffers.length];
			this.isPooled = new boolean[this.buffers.length];

			// Populate the array of byte buffers to write
			ByteBuffer currentBuffer = ConnectionImpl.this.socketListener
					.getWriteBufferFromPool();
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
							currentBuffer = ConnectionImpl.this.socketListener
									.getWriteBufferFromPool();
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
						currentBuffer = ConnectionImpl.this.socketListener
								.getWriteBufferFromPool();
					}

					// Include the cached buffer
					this.addBuffer(writeBuffer.getDataBuffer(), false);
					break;

				default:
					throw new IllegalStateException("Unknown "
							+ WriteBuffer.class.getSimpleName() + " type: "
							+ type);
				}
			}

			// Include the current buffer (if has data)
			if (currentBuffer.position() > 0) {
				// Include the current buffer
				currentBuffer.flip();
				this.addBuffer(currentBuffer, true);

			} else {
				// Return to current buffer to pool as not used
				ConnectionImpl.this.socketListener
						.returnWriteBufferToPool(currentBuffer);
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
			SocketChannel socketChannel = ConnectionImpl.this
					.getSocketChannel();
			boolean isFailure = false;
			try {
				socketChannel.write(this.buffers, this.startIndex,
						(this.nextIndex - this.startIndex));
			} catch (IOException ex) {
				// Connection failed (terminate immediately)
				ConnectionImpl.this.terminate();
				isFailure = true;
			}

			// Return written buffers to the pool
			while ((this.startIndex < this.nextIndex)
					&& (((this.buffers[this.startIndex].remaining() == 0)) || isFailure)) {

				// Buffer written or connection failure, return to pool
				if (this.isPooled[this.startIndex]) {
					ConnectionImpl.this.socketListener
							.returnWriteBufferToPool(this.buffers[this.startIndex]);
				}

				// Start writing from next buffer
				this.startIndex++;
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
					ConnectionImpl.this.socketListener
							.returnWriteBufferToPool(this.buffers[i]);
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