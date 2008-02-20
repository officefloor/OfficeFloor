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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.MessageSegment;
import net.officefloor.plugin.socket.server.spi.ReadContext;
import net.officefloor.plugin.socket.server.spi.ReadMessage;
import net.officefloor.plugin.socket.server.spi.WriteContext;
import net.officefloor.plugin.socket.server.spi.WriteMessage;

/**
 * Listens to {@link java.net.Socket} instances.
 * 
 * @author Daniel
 */
class SocketListener implements
		Task<Object, ConnectionManager, None, Indexed>, ReadContext,
		WriteContext {

	/**
	 * Maximum number of {@link Communciation} instances that can be registered
	 * with this {@link SocketListener}.
	 */
	private final int maxConnections;

	/**
	 * {@link ServerSocketManagedObjectSource}.
	 */
	private final ServerSocketManagedObjectSource<?, ?> moSource;

	/**
	 * List of {@link InternalCommunication} just registered.
	 */
	private final List<ConnectionImpl<?>> justRegistered = new LinkedList<ConnectionImpl<?>>();

	/**
	 * {@link Selector} to aid in listening for connections. This should be
	 * treated as <code>final</code>, however is specified on first run of
	 * this {@link Task}.
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
	 * @param moSource
	 *            {@link ServerSocketManagedObjectSource}.
	 * @param maxCommunications
	 *            Maximum number of {@link Communciation} instances that can be
	 *            registered with this {@link SocketListener}.
	 */
	SocketListener(ServerSocketManagedObjectSource<?, ?> moSource,
			int maxCommunications) {
		this.moSource = moSource;
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

		// Determine if space to register input connection
		if (this.registeredConnections >= this.maxConnections) {
			// Can not register connection
			return false;
		}

		// Add just registered listing
		this.justRegistered.add(connection);

		// Increment the number registered with this listener
		this.registeredConnections++;

		// Wake up the selector (to pick up this communication)
		this.selector.wakeup();

		// Registered
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
	 */
	public Object doTask(
			TaskContext<Object, ConnectionManager, None, Indexed> context)
			throws Exception {

		// Flag to loop forever
		context.setComplete(false);

		// Determine if require initialising
		if (!this.isInitialised) {
			// Create the selector
			this.selector = Selector.open();

			// Obtain the Connection
			ConnectionImpl<?> connection = (ConnectionImpl<?>) context
					.getParameter();

			// Listen to the connection
			this.listenToConnection(connection);

			// Flag initialised
			this.isInitialised = true;
		}

		// Listen to sockets
		int selectCount = this.selector.select(1000);

		// Handle listening to connections
		synchronized (this) {

			// Determine if require processing
			if (selectCount == 0) {

				// Handle idle connections
				for (SelectionKey key : this.selector.keys()) {
					// Obtain the connection
					ConnectionImpl<?> connection = (ConnectionImpl<?>) key
							.attachment();

					// Handle the connection being idle
					connection.getConnectionHandler().handleIdleConnection(
							connection);
				}

			} else {
				// Process the selection keys
				for (SelectionKey key : this.selector.selectedKeys()) {

					// Obtain the connection
					ConnectionImpl<?> connection = (ConnectionImpl<?>) key
							.attachment();

					// Ensure key is valid
					if (!key.isValid()) {
						// No longer valid, therefore close the connection
						this.closeConnection(key, connection);

					} else {

						// Interest Operations
						int interestOps = 0;

						// Reset connection context
						this.resetConnectionContext();

						// Reading
						if (key.isReadable()) {
							// Read data from connection
							switch (this.readData(connection)) {
							case -1:
								// Connection lost, therefore close connection
								this.closeConnection(key, connection);
							default:
								// Handle completed read
								if (this.completedReadMessage != null) {
									// Process the read message
									this.moSource
											.processMessage(this.completedReadMessage);
								}

								// Determine if to continue reading
								if (this.isContinueReading) {
									// Flag to continue reading
									interestOps |= SelectionKey.OP_READ;
								}
							}
						} else {
							// Determine if maintain interest in reading
							interestOps |= (key.interestOps() & SelectionKey.OP_READ);
						}

						// Writing
						if (key.isWritable()) {
							// Write data to connection
							this.writeData(connection);
						}

						// Handle continue with connection
						if (this.isCloseConnection) {
							// Close the connection
							this.closeConnection(key, connection);

						} else {
							// Continue with connection

							// Determine if require reading
							if (connection.getFirstReadMessage() != null) {
								// Flag to read message
								interestOps |= SelectionKey.OP_READ;
							}

							// Determine if require writing
							if (connection.getFirstWriteMessage() != null) {
								// Flag to write message
								interestOps |= SelectionKey.OP_WRITE;
							}

							// Determine if require changing interest ops
							if (interestOps != key.interestOps()) {
								// Change the interest
								key.interestOps(interestOps);
							}
						}
					}
				}
			}

			// Start listening to the just registered communications
			for (Iterator<ConnectionImpl<?>> iterator = this.justRegistered
					.iterator(); iterator.hasNext();) {

				// Listen to the connection
				this.listenToConnection(iterator.next());

				// Remove from just registered
				iterator.remove();
			}
		}

		// No return (as will loop forever)
		return null;
	}

	/**
	 * Closes the {@link Connection}.
	 * 
	 * @param key
	 *            {@link SelectionKey} for the {@link Connection}.
	 * @param connection
	 *            {@link Connection} to close.
	 * @throws IOException
	 *             If fails closing.
	 */
	private void closeConnection(SelectionKey key, ConnectionImpl<?> connection)
			throws IOException {
		// Cancel the key and then connection
		key.cancel();
		connection.cancel();
		connection.getSocketChannel().close();

		// Connection is also unregistered
		this.registeredConnections--;
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

		// Determine if writing message
		WriteMessage writeMessage = connection.getFirstWriteMessage();
		if (writeMessage != null) {
			// Also writing a message
			operation = operation | SelectionKey.OP_WRITE;
		}

		// Register the socket to listen on the communication
		connection.getSocketChannel().register(this.selector, operation,
				connection);
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
	private int readData(ConnectionImpl<?> connection) throws IOException {

		// Obtain the socket channel
		SocketChannel socketChannel = connection.getSocketChannel();

		// Data to be read, therefore ensure have a read message
		ReadMessageImpl message = connection.getFirstReadMessage();
		if (message == null) {
			message = connection.createReadMessage();
		}

		// Start appending to last segment
		MessageSegment segment = message.getLastSegment();
		if (segment == null) {
			// No segments on read message, therefore append one
			segment = message.appendSegment();
		}

		// Read the data from the socket into the message
		int bytesRead = 0;
		boolean isMoreData = true;
		while (isMoreData) {

			// Determine if require another segment
			if (segment.getBuffer().position() == segment.getBuffer().limit()) {
				segment = message.appendSegment();
			}

			// Read from socket into the buffer
			int bytes = socketChannel.read(segment.getBuffer());

			// Handle completion of reading
			switch (bytes) {
			case -1:
				// End of stream (connection lost)
				return -1;

			case 0:
				// No data read thus finished
				isMoreData = false;
				break;

			default:
				// Increment bytes read
				bytesRead += bytes;

				// Determine if filled buffer and potentially more data
				isMoreData = (segment.getBuffer().position() == segment
						.getBuffer().limit());
			}
		}

		// Handle the read
		this.resetReadContext(message);
		connection.getConnectionHandler().handleRead(this);

		// Return the number of bytes read
		return bytesRead;
	}

	/**
	 * Writes data to the {@link Connection}.
	 * 
	 * @param connection
	 *            {@link ConnectionImpl}.
	 * @throws IOException
	 *             If fails to handle write.
	 */
	private void writeData(ConnectionImpl<?> connection) throws IOException {

		// Handle write
		WriteMessageImpl message = connection.getFirstWriteMessage();

		// Obtain the medium to communicate over
		SocketChannel socketChannel = connection.getSocketChannel();

		// Write the segments
		int segmentCount = message.getSegmentCount();
		switch (segmentCount) {
		case 0:
			// Determine if write message
			if (message.isFilled()) {
				// As no segment, message written
				message.written();
			}
			break;

		case 1:
			// Determine if write message
			if (message.isFilled()) {
				// Handle only a single segment
				ByteBuffer buffer = message.getFirstSegment().getBuffer();
				socketChannel.write(buffer);

				// Determine if complete
				if (buffer.position() == buffer.limit()) {
					// Flag the message written
					message.written();
				}
			}
			break;

		default:
			// TODO Handle more than one segment
			throw new UnsupportedOperationException("TODO implement");
		}

		// Handle the write
		this.resetWriteContext(message);
		connection.getConnectionHandler().handleWrite(this);
	}

	/*
	 * ====================================================================
	 * ReadContext
	 * 
	 * ReadContext does not require thread-safety as should only be accessed by
	 * the same Thread.
	 * ====================================================================
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ReadContext#getReadMessage()
	 */
	public ReadMessage getReadMessage() {
		return this.readMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ReadContext#setReadComplete(boolean)
	 */
	public void setReadComplete(boolean isComplete) {
		if (isComplete) {
			// Current read message is complete
			this.completedReadMessage = this.readMessage;
		} else {
			// Unset read message being complete
			this.completedReadMessage = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ReadContext#setContinueReading(boolean)
	 */
	public void setContinueReading(boolean isContinue) {
		this.isContinueReading = isContinue;
	}

	/*
	 * ====================================================================
	 * WriteContext
	 * 
	 * WriteContext does not require thread-safety as should only be accessed by
	 * the same Thread ever.
	 * ====================================================================
	 */

	/**
	 * {@link WriteMessage}.
	 */
	private WriteMessage writeMessage = null;

	/**
	 * Resets the {@link ReadContext} for the input {@link ReadMessage}.
	 * 
	 * @param readMessage
	 *            {@link ReadMessage}.
	 */
	private void resetWriteContext(WriteMessage writeMessage) {
		// Reset the write context
		this.writeMessage = writeMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteContext#getWriteMessage()
	 */
	public WriteMessage getWriteMessage() {
		return this.writeMessage;
	}

	/*
	 * ====================================================================
	 * ReadContext & WriteContext overlap (ie Connection Context)
	 * 
	 * Does not require thread-safety as should only be accessed by the same
	 * Thread ever.
	 * ====================================================================
	 */

	/**
	 * Flag to indiate to close the {@link Connection}.
	 */
	private boolean isCloseConnection = false;

	/**
	 * Resets the context for the {@link Connection}.
	 * 
	 */
	private void resetConnectionContext() {
		this.isCloseConnection = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ReadContext#setCloseConnection(boolean)
	 */
	public void setCloseConnection(boolean isClose) {
		this.isCloseConnection = isClose;
	}

}
