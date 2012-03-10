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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.ConnectionHandlerContext;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.stream.InputBufferStream;

/**
 * Listens to {@link Socket} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketListener<CH extends ConnectionHandler>
		implements
		Task<ConnectionManager<CH>, SocketListener.SocketListenerDependencies, Indexed>,
		ReadContext, WriteContext, IdleContext {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(SocketListener.class
			.getName());

	/**
	 * Keys for the dependencies for the {@link SocketListener}.
	 */
	public static enum SocketListenerDependencies {
		CONNECTION
	}

	/**
	 * Value to indicate an unbounded number of {@link Connection} instances can
	 * be registered.
	 */
	public static final int UNBOUNDED_MAX_CONNECTIONS = 0;

	/**
	 * Maximum number of {@link Connection} instances that can be registered
	 * with this {@link SocketListener}.
	 */
	private final int maxConnections;

	/**
	 * {@link Server}.
	 */
	private final Server<CH> server;

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory;

	/**
	 * List of {@link InternalCommunication} just registered.
	 */
	private final List<ConnectionImpl<CH>> justRegistered = new LinkedList<ConnectionImpl<CH>>();

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
	 * Initially 1 for the {@link Connection} passed as a parameter.
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
	public SocketListener(SelectorFactory selectorFactory, Server<CH> server,
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
	synchronized boolean registerConnection(ConnectionImpl<CH> connection)
			throws IOException {

		// All connections complete so may not register
		if (this.registeredConnections <= 0) {
			// Can not register as complete
			return false;
		}

		// Determine if space to register input connection
		if (this.maxConnections > UNBOUNDED_MAX_CONNECTIONS) {
			if (this.registeredConnections >= this.maxConnections) {
				// Can not register connection
				return false;
			}
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
	@SuppressWarnings("unchecked")
	public Object doTask(
			TaskContext<ConnectionManager<CH>, SocketListenerDependencies, Indexed> context)
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
					ConnectionImpl<CH> connection = (ConnectionImpl<CH>) context
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
		this.selector.select(10000); // 10 seconds

		// Synchronising at this point as may be removing connections altering
		// counts that the registerConnection utilises.
		boolean isUnregisterFromConnectionManager = false;
		synchronized (this) {

			// Reset current time (optimisation)
			this.currentTime = -1;

			// Obtain the selected keys
			Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

			// Process all the selected channels
			for (SelectionKey key : this.selector.keys()) {

				// Obtain the connection
				ConnectionImpl<CH> connection = (ConnectionImpl<CH>) key
						.attachment();

				// Synchronise on connection to reduce locking
				synchronized (connection.getLock()) {
					try {

						// Flag that checking connection
						connection.flagCheckingConnection();

						// Interest Operations (always reading)
						int interestOps = SelectionKey.OP_READ;

						// Reset connection context
						this.resetConnectionContext(connection);

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
									// Determine if active (something read)
									isActive |= (bytesRead > 0);
									break;
								}
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
							connection.getConnectionHandler()
									.handleIdleConnection(this);
						}

						// Handle closing connection
						if (this.isCloseConnection || connection.isCancelled()) {
							// Attempt to close connection
							if (this.closeConnection(key, connection)) {
								// Connection closed, so no further processing
								continue;
							}

							// Connection closing, so write until closed
							interestOps |= SelectionKey.OP_WRITE;

						} else {
							// Determine if data for the client
							if (connection.isDataForClient()) {
								// Flag to write data to client
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

						// Indicate failure details
						if (ex instanceof ClosedChannelException) {
							// Peer closed connection
							if (LOGGER.isLoggable(Level.FINE)) {
								LOGGER.log(Level.FINE,
										"Peer closed connection", ex);
							}
						} else {
							// Another error so provide full details
							if (LOGGER.isLoggable(Level.WARNING)) {
								LOGGER.log(Level.WARNING,
										"Failure with connection", ex);
							}
						}
					}
				}
			}

			// Start listening to the just registered connections
			for (Iterator<ConnectionImpl<CH>> iterator = this.justRegistered
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

				// Unregister from connection manager
				isUnregisterFromConnectionManager = true;
			}
		}

		// Must unregister from connection manager outside lock
		if (isUnregisterFromConnectionManager) {
			context.getWork().socketListenerComplete(this);
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
			ConnectionImpl<CH> connection) throws IOException {

		// Flag the connection to close
		connection.cancel();

		// Determine if all data written
		if (connection.isDataForClient()) {
			// Data still to write so do not yet terminate connection
			return false;
		}

		// All data written so terminate the connection
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
			ConnectionImpl<CH> connection) throws IOException {

		// Flag connection as closed
		connection.cancel();

		// Connection unregistered
		this.registeredConnections--;

		// Cancel the key and terminate connection
		key.cancel();
		connection.terminate();
	}

	/**
	 * Listens to the {@link Connection}.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @throws IOException
	 *             If fails to start listening to the {@link Connection}.
	 */
	private void listenToConnection(ConnectionImpl<CH> connection)
			throws IOException {

		// On listening to a connection, always read a message
		int operation = SelectionKey.OP_READ;

		// Ensure safe to manipulate the connection
		synchronized (connection.getLock()) {

			// Link connection into this socket listener
			connection.setSocketListener(this);

			// Determine if writing data for connection
			if (connection.isDataForClient()) {
				// Also writing a message
				operation = operation | SelectionKey.OP_WRITE;
			}
		}

		// Register the connection with the selector
		connection.registerWithSelector(this.selector, operation);
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
	int readData(ConnectionImpl<CH> connection) throws IOException {

		// Record the number of bytes read
		int bytesRead = 0;

		// Read the data from the socket into the message
		boolean isConnectionClosed = false;
		boolean isMoreData = true;
		while (isMoreData) {

			// Read from socket into the buffer
			int bytesSize = connection.readDataFromClient();

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
			}
		}

		// Handle the read (only if read data)
		if (bytesRead > 0) {
			// Handle the read
			connection.getConnectionHandler().handleRead(this);
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
	int writeData(ConnectionImpl<CH> connection) throws IOException {

		// Record the number of bytes written
		int bytesWritten = 0;

		// Flags indicating to stop writing
		boolean isConnectionLost = false;
		boolean isWriteBufferFull = false;

		// Keep writing until either:
		// - connection lost
		// - network write buffer to client is full
		// - no further data for client
		while ((!isConnectionLost) && (!isWriteBufferFull)
				&& (connection.isDataForClient())) {

			// Write the data to the client
			int bytesSize = connection.writeDataToClient();

			// Handle completion of read
			switch (bytesSize) {
			case -1:
				// Connection lost
				isConnectionLost = true;
				break;

			case 0:
				// Write buffer full to client
				isWriteBufferFull = true;
				break;

			default:
				// Increment the number of bytes written
				bytesWritten += bytesSize;
			}
		}

		// Handle the write (only if bytes written)
		if (bytesWritten > 0) {
			connection.getConnectionHandler().handleWrite(this);
		}

		// Return the number of bytes written (or -1 if connection lost)
		return (isConnectionLost ? -1 : bytesWritten);
	}

	/*
	 * =============== ConnectionHandlerContext ===============================
	 * Does not require thread-safety as only accessed by the same Thread.
	 */

	/**
	 * Flag to indicate to close the {@link Connection}.
	 */
	private boolean isCloseConnection = false;

	/**
	 * {@link Connection} of the current {@link ConnectionHandlerContext}.
	 */
	private ConnectionImpl<CH> contextConnection = null;

	/**
	 * Resets the context for the {@link Connection}.
	 * 
	 * @param connection
	 *            {@link ConnectionImpl} currently being processed.
	 */
	private void resetConnectionContext(ConnectionImpl<CH> connection) {
		this.isCloseConnection = false;
		this.contextConnection = connection;
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

	/**
	 * Context object.
	 */
	private Object contextObject = null;

	@Override
	public Object getContextObject() {
		return this.contextObject;
	}

	@Override
	public void setContextObject(Object contextObject) {
		this.contextObject = contextObject;
	}

	/*
	 * ================== ReadContext =====================================
	 * ReadContext does not require thread-safety as only accessed by the same
	 * Thread.
	 */

	@Override
	public InputBufferStream getInputBufferStream() {
		return this.contextConnection.getConnectionInputBufferStream();
	}

	@Override
	public void processRequest(Object attachment) throws IOException {
		// Have the server process the request
		this.server.processRequest(
				this.contextConnection.getConnectionHandler(), attachment);
	}

	/*
	 * ============== IdleContext and WriteContext =========================
	 * IdleContext and WriteContext does not require thread-safety as only
	 * accessed by the same Thread.
	 */

	// No specific methods for IdleContext and WriteContext.
}