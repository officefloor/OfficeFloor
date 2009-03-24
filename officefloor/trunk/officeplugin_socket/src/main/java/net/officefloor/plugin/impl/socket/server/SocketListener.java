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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandlerContext;
import net.officefloor.plugin.socket.server.spi.IdleContext;
import net.officefloor.plugin.socket.server.spi.MessageSegment;
import net.officefloor.plugin.socket.server.spi.ReadContext;
import net.officefloor.plugin.socket.server.spi.ReadMessage;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.WriteContext;
import net.officefloor.plugin.socket.server.spi.WriteMessage;

/**
 * Listens to {@link Socket} instances.
 * 
 * @author Daniel
 */
class SocketListener
		implements
		Task<ConnectionManager, SocketListener.SocketListenerDependencies, Indexed>,
		ReadContext, WriteContext, IdleContext {

	/**
	 * Keys for the dependencies for the {@link SocketListener}.
	 */
	public static enum SocketListenerDependencies {
		CONNECTION
	}

	/**
	 * Maximum number of {@link Connection} instances that can be registered
	 * with this {@link SocketListener}.
	 */
	private final int maxConnections;

	/**
	 * {@link Server}.
	 */
	private final Server<?> server;

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory;

	/**
	 * List of {@link InternalCommunication} just registered.
	 */
	private final List<ConnectionImpl<?>> justRegistered = new LinkedList<ConnectionImpl<?>>();

	/**
	 * {@link Selector} to aid in listening for connections. This should be
	 * treated as <code>final</code>, however is specified on first run of this
	 * {@link Task}.
	 */
	private Selector selector;

	/**
	 * Flag indicating if have initialised the {@link Task}.
	 */
	private boolean isInitialised = false;

	/**
	 * <p>
	 * Number of registered {@link Connection} instances within this
	 * {@link SocketListener}.
	 * <p>
	 * Initially 1 for the {@link Connection} from the
	 * {@link TaskContext#getParameter()}.
	 */
	private int registeredConnections = 1;

	/**
	 * Initiate.
	 * 
	 * @param selectorFactory
	 *            {@link SelectorFactory}.
	 * @param server
	 *            {@link Server}.
	 * @param maxCommunications
	 *            Maximum number of {@link Connection} instances that can be
	 *            registered with this {@link SocketListener}.
	 */
	SocketListener(SelectorFactory selectorFactory, Server<?> server,
			int maxCommunications) {
		this.selectorFactory = selectorFactory;
		this.server = server;
		this.maxConnections = maxCommunications;
	}

	/**
	 * Registers a {@link Connection} with this {@link SocketListener}.
	 * 
	 * @param connection
	 *            {@link ConnectionImpl}.
	 * @return <code>true</code> if registered, otherwise <code>false</code>.
	 * @throws IOException
	 *             If fails to register the connection.
	 */
	synchronized boolean registerConnection(ConnectionImpl<?> connection)
			throws IOException {

		// All connections complete so may not register
		if (this.registeredConnections <= 0) {
			// Can not register as complete
			return false;
		}

		// Determine if space to register input connection
		if (this.registeredConnections >= this.maxConnections) {
			// Can not register connection
			return false;
		}

		// Add just registered listing
		this.justRegistered.add(connection);

		// Increment the number registered with this listener
		this.registeredConnections++;

		// May not yet be initialised
		if (this.selector != null) {
			// Wake up the selector (to pick up this connection)
			this.selector.wakeup();
		}

		// Registered
		return true;
	}

	/**
	 * Wakes up the {@link Selector}.
	 */
	void wakeup() {
		// Wakes up the selector
		this.selector.wakeup();
	}

	/*
	 * ====================== Task =======================================
	 */

	@Override
	public Object doTask(
			TaskContext<ConnectionManager, SocketListenerDependencies, Indexed> context)
			throws Exception {

		// Flag to loop forever
		context.setComplete(false);

		// Double check lock to determine if initialised
		if (!this.isInitialised) {
			synchronized (this) {
				if (!this.isInitialised) {
					// Requires initialising

					// Create the selector
					this.selector = this.selectorFactory.createSelector();

					// Obtain the Connection
					ConnectionImpl<?> connection = (ConnectionImpl<?>) context
							.getObject(SocketListenerDependencies.CONNECTION);

					// Listen to the connection
					this.listenToConnection(connection);

					// Flag initialised
					this.isInitialised = true;
				}
			}
		}

		// Listen on the socket.
		// This is outside locks so that other connections may be registered
		// while waiting. On registering a connection this will be waked up.
		this.selector.select(1000);

		// Synchronising at this point as may be removing connections altering
		// counts that the registerConnection utilises.
		synchronized (this) {

			// Reset current time (optimisation)
			this.currentTime = -1;

			// Obtain the selected keys
			Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

			// Process all the selected channels
			for (SelectionKey key : this.selector.keys()) {

				// Obtain the connection
				ConnectionImpl<?> connection = (ConnectionImpl<?>) key
						.attachment();

				// Synchronise on connection to reduce locking
				synchronized (connection.getLock()) {
					try {

						// Interest Operations
						int interestOps = 0;

						// Reset connection context
						this.resetConnectionContext();

						// Determine if selected connection
						boolean isActive = false; // idle
						if (selectedKeys.contains(key)) {

							// Remove the key from selection
							selectedKeys.remove(key);

							// Ensure key is valid
							if (!key.isValid()) {
								// Invalid, terminate and stop processing
								this.terminateConnection(key, connection);
								continue;
							}

							// Reading
							if (key.isReadable()) {
								// Read data from connection
								int bytesRead = this.readData(connection);
								switch (bytesRead) {
								case -1:
									// Connection lost, therefore terminate and
									// stop processing.
									this.terminateConnection(key, connection);
									continue;

								default:
									// Determine if active (ie something read)
									isActive |= (bytesRead > 0);

									// Handle completed read
									if (this.completedReadMessage != null) {
										// Process the read message
										this.server
												.processReadMessage(
														this.completedReadMessage,
														this.completedReadMessage.stream.connection.connectionHandler);
									}

									// Determine if to continue reading
									if (this.isContinueReading) {
										// Flag to continue reading
										interestOps |= SelectionKey.OP_READ;
									}
									break;
								}
							} else {
								// Determine if maintain interest in reading
								interestOps |= (key.interestOps() & SelectionKey.OP_READ);
							}

							// Writing (or closed attempting to write remaining)
							if (key.isWritable() || connection.isCancelled()) {
								// Write data to connection
								int bytesWritten = this.writeData(connection);
								switch (bytesWritten) {
								case -1:
									// Connection lost, therefore terminate and
									// stop processing.
									this.terminateConnection(key, connection);
									continue;

								default:
									// Determine if idle (ie something written)
									isActive |= (bytesWritten > 0);

									// Data on buffer on way to client.
									// Do nothing more for writing.
									break;
								}
							}
						}

						// Handle if connection was idle
						if (!isActive) {
							// Handle non-selected key (idle connection)
							connection.connectionHandler
									.handleIdleConnection(this);
						}

						// Handle closing connection
						if (this.isCloseConnection || connection.isCancelled()) {
							// Attempt to close connection
							if (this.closeConnection(key, connection)) {
								// Connection closed, so no further processing
								continue;
							}

							// Ensure write messages are being written
							WriteMessage activeWriteMessage = connection
									.getActiveWriteMessage();
							if (activeWriteMessage != null) {
								activeWriteMessage.write();
							}

							// Connection closing, so write until closed
							interestOps |= SelectionKey.OP_WRITE;

						} else {
							// Connection not closed, continue reading
							interestOps |= SelectionKey.OP_READ;

							// Determine if require writing
							if (connection.writeStream.getFirstMessage() != null) {
								// Flag to write message
								interestOps |= SelectionKey.OP_WRITE;
							}
						}

						// Determine if require changing interest ops
						if (interestOps != key.interestOps()) {
							// Change the interest
							key.interestOps(interestOps);
						}

					} catch (Exception ex) {
						// Terminate connection on failure
						this.terminateConnection(key, connection);

						// TODO how to handle exception issues
						System.err.println("TODO handle failure of connection");
						ex.printStackTrace();
					}
				}
			}

			// Start listening to the just registered connections
			for (Iterator<ConnectionImpl<?>> iterator = this.justRegistered
					.iterator(); iterator.hasNext();) {

				// Listen to the connection
				this.listenToConnection(iterator.next());

				// Remove from just registered
				iterator.remove();
			}

			// Flag that complete if no further connections
			if (this.registeredConnections <= 0) {
				// Allow task to complete
				context.setComplete(true);

				// Unregister the socket listener from connection manager
				context.getWork().socketListenerComplete(this);
			}
		}

		// No return (as should be looping)
		return null;
	}

	/**
	 * Closes the {@link Connection}.
	 * 
	 * @param key
	 *            {@link SelectionKey} for the {@link Connection}.
	 * @param connection
	 *            {@link Connection} to close.
	 * @return <code>true</code> {@link Connection} was closed.
	 * @throws IOException
	 *             If fails closing.
	 */
	private boolean closeConnection(SelectionKey key,
			ConnectionImpl<?> connection) throws IOException {

		// Flag the connection to close
		connection.cancel();

		// Determine if all write messages are written
		if (connection.getActiveWriteMessage() != null) {
			// Active message, so do not yet terminate connection
			return false;
		}

		// All messages written, so terminate the connection
		this.terminateConnection(key, connection);

		// Connection closed
		return true;
	}

	/**
	 * Terminates the {@link Connection} immediately.
	 * 
	 * @param key
	 *            {@link SelectionKey} for the {@link Connection}.
	 * @param connection
	 *            {@link Connection} to close.
	 * @throws IOException
	 *             If fails closing.
	 */
	private void terminateConnection(SelectionKey key,
			ConnectionImpl<?> connection) throws IOException {

		// Flag connection as closed
		connection.cancel();

		// Connection unregistered
		this.registeredConnections--;

		// Cancel the key and close connection
		key.cancel();
		connection.socketChannel.close();
	}

	/**
	 * Listens to the {@link Connection}.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @throws IOException
	 *             If fails to start listening to the {@link Connection}.
	 */
	private void listenToConnection(ConnectionImpl<?> connection)
			throws IOException {

		// On listening to a connection, always read a message
		int operation = SelectionKey.OP_READ;

		// Ensure safe to manipulate the connection
		synchronized (connection.getLock()) {

			// Link connection into this socket listener
			connection.setSocketListener(this);

			// Determine if writing message
			if (connection.writeStream.getFirstMessage() != null) {
				// Also writing a message
				operation = operation | SelectionKey.OP_WRITE;
			}
		}

		// Register the socket to listen on the communication
		connection.socketChannel.register(this.selector, operation, connection);
	}

	/**
	 * Reads data from the {@link Connection}.
	 * 
	 * @param connection
	 *            {@link ConnectionImpl}.
	 * @return Number of bytes read.
	 * @throws IOException
	 *             If fails to handle read.
	 */
	int readData(ConnectionImpl<?> connection) throws IOException {

		// Record the number of bytes read
		int bytesRead = 0;

		// Data to be read, therefore ensure have a read message
		ReadMessageImpl message = connection.readStream.getLastMessage();
		if (message == null) {
			message = connection.readStream.appendMessage(null);
		}

		// Start appending to last segment
		MessageSegment segment = message.getLastSegment();
		if (segment == null) {
			// No segments on read message, therefore append one
			segment = message.appendMessageSegment(null);
		}

		// Read the data from the socket into the message
		boolean isConnectionClosed = false;
		boolean isMoreData = true;
		while (isMoreData) {

			// Determine if require another segment
			if (segment.getBuffer().remaining() == 0) {
				segment = message.appendMessageSegment(null);
			}

			// Read from socket into the buffer
			int bytesSize = connection.socketChannel.read(segment.getBuffer());

			// Handle completion of reading
			switch (bytesSize) {
			case -1:
				// End of stream (connection lost)
				isConnectionClosed = true;
				isMoreData = false;
				break;

			case 0:
				// No data read thus finished
				isMoreData = false;
				break;

			default:
				// Increment bytes read
				bytesRead += bytesSize;

				// Determine if filled buffer and potentially more data
				isMoreData = (segment.getBuffer().remaining() == 0);
			}
		}

		// Handle the read (only if read data)
		this.resetReadContext(message);
		if (bytesRead > 0) {
			connection.connectionHandler.handleRead(this);
		}

		// Return the number of bytes read (or -1 if connection lost)
		return (isConnectionClosed ? -1 : bytesRead);
	}

	/**
	 * Writes data to the {@link Connection}.
	 * 
	 * @param connection
	 *            {@link ConnectionImpl}.
	 * @return Number of bytes written.
	 * @throws IOException
	 *             If fails to handle write.
	 */
	int writeData(ConnectionImpl<?> connection) throws IOException {

		// Record the number of bytes written
		int bytesWritten = 0;

		// Flags indicating to stop writing
		boolean isConnectionLost = false;
		boolean isWriteBufferFull = false;

		// Iterate over messages to write
		WriteMessageImpl writeMessage = connection.writeStream
				.getFirstMessage();
		while (writeMessage != null) {

			// Determine if message ready to be written
			if (!writeMessage.isFilled()) {
				// No data to write
				break;
			}

			// Obtain the starting point to write the data
			MessageSegment messageSegment = writeMessage.currentMessageSegment;
			if (messageSegment == null) {
				// No current, so start at beginning of message
				messageSegment = writeMessage.getFirstSegment();
			}
			int offset = writeMessage.currentMessageSegmentOffset;

			// Write the segments
			while (messageSegment != null) {

				// Setup buffer to write contents
				ByteBuffer buffer = messageSegment.getBuffer().duplicate();
				if (buffer.position() > 0) {
					buffer.flip();
				}
				if (offset > 0) {
					// Only move position if an offset (as already 0)
					buffer.position(offset);
				}

				// Write the data to the client
				int bytesSize = connection.socketChannel.write(buffer);

				// Handle completion of read
				switch (bytesSize) {
				case -1:
					// Connection lost
					isConnectionLost = true;
					break;

				default:
					// Increment the number of bytes written
					bytesWritten += bytesSize;

					// Determine if message segment written
					if (buffer.remaining() != 0) {
						// Further data on segment (write buffer full)
						isWriteBufferFull = true;

						// Mark current position to continue writing later
						offset += bytesSize;
						writeMessage.currentMessageSegment = messageSegment;
						writeMessage.currentMessageSegmentOffset = offset;

						break;
					}
				}

				// Determine if the write buffer is full or connection lost
				if (isWriteBufferFull || isConnectionLost) {
					// Stop writing segments
					break;
				}

				// Setup to write the next segment
				messageSegment = messageSegment.getNextSegment();
				offset = 0;
			}

			// Determine if the write buffer is full or connection lost
			if (isWriteBufferFull || isConnectionLost) {
				// Stop writing messages
				break;
			}

			// Obtain the next write message
			WriteMessageImpl nextWriteMessage = (WriteMessageImpl) writeMessage.next;

			// Flag message has been written, and remove
			writeMessage.written();

			// Set next write message for next iteration
			writeMessage = nextWriteMessage;
		}

		// Handle the write (only if bytes written)
		if (bytesWritten > 0) {
			connection.connectionHandler.handleWrite(this);
		}

		// Return the number of bytes written (or -1 if connection lost)
		return (isConnectionLost ? -1 : bytesWritten);
	}

	/*
	 * =============== ConnectionHandlerContext ===============================
	 * Does not require thread-safety as should only be accessed by the same
	 * Thread.
	 */

	/**
	 * Flag to indicate to close the {@link Connection}.
	 */
	private boolean isCloseConnection = false;

	/**
	 * Resets the context for the {@link Connection}.
	 * 
	 */
	private void resetConnectionContext() {
		this.isCloseConnection = false;
	}

	@Override
	public void setCloseConnection(boolean isClose) {
		this.isCloseConnection = isClose;
	}

	/**
	 * <p>
	 * Current time for {@link ConnectionHandlerContext}.
	 * <p>
	 * This is reset on new run of this {@link Task}.
	 */
	private long currentTime = -1;

	@Override
	public long getTime() {
		// Lazy obtain the time
		if (currentTime < 0) {
			this.currentTime = System.currentTimeMillis();
		}
		return this.currentTime;
	}

	/*
	 * ================== ReadContext =====================================
	 * ReadContext does not require thread-safety as should only be accessed by
	 * the same Thread.
	 */

	/**
	 * {@link ReadMessage}.
	 */
	private ReadMessageImpl readMessage = null;

	/**
	 * Flag to indicate read complete.
	 */
	private ReadMessageImpl completedReadMessage = null;

	/**
	 * Flag to indicate continue reading.
	 */
	private boolean isContinueReading = true;

	/**
	 * Resets the {@link ReadContext} for the input {@link ReadMessage}.
	 * 
	 * @param readMessage
	 *            {@link ReadMessage}.
	 */
	private void resetReadContext(ReadMessageImpl readMessage) {
		// Reset the read context
		this.readMessage = readMessage;
		this.completedReadMessage = null;
		this.isContinueReading = true;
	}

	@Override
	public ReadMessage getReadMessage() {
		return this.readMessage;
	}

	@Override
	public void setReadComplete(boolean isComplete) {
		if (isComplete) {
			// Current read message is complete
			this.completedReadMessage = this.readMessage;
		} else {
			// Unset read message being complete
			this.completedReadMessage = null;
		}
	}

	@Override
	public void setContinueReading(boolean isContinue) {
		this.isContinueReading = isContinue;
	}

	/*
	 * ============== IdleContext and WriteContext =========================
	 * IdleContext and WriteContext does not require thread-safety as should
	 * only be accessed by the same Thread.
	 */

	// No specific methods for IdleContext and WriteContext.
}