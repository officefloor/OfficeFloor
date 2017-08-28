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
package net.officefloor.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.server.http.protocol.CommunicationProtocol;
import net.officefloor.server.http.protocol.Connection;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Implementation of a {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionImpl implements Connection, ManagedConnection, SelectionKeyAttachment, WriteDataAction {

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
	 * {@link Old_SocketListener} handling this {@link Connection}.
	 */
	private final Old_SocketListener socketListener;

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
	 *            {@link Old_SocketListener}.
	 * @param communicationProtocol
	 *            {@link CommunicationProtocol}.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 */
	public ConnectionImpl(SelectionKey selectionKey, SocketChannel socketChannel, Old_SocketListener socketListener,
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
	public boolean isSecure() {
		// Connection is not secure
		return false;
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

		// No longer registered for write
		this.isRegisteredForWrite = false;

//		try {
//
//			// Write the data for each queued write action
//			// for (Iterator<WriteAction> iterator =
//			// this.writeActions.iterator(); iterator.hasNext();) {
//			//
//			// // Undertake the current write action
//			// WriteAction action = iterator.next();
//			// if (!(action.writeData())) {
//			// return false; // further writes required
//			// }
//			//
//			// // Action written, so remove
//			// iterator.remove();
//			// }
//
//		} catch (IOException ex) {
//
//			// Indicate failure to write the data
//			if (LOGGER.isLoggable(Level.FINE)) {
//				LOGGER.log(Level.FINE, "Failed to write data", ex);
//			}
//
//			// Terminate connection immediately
//			this.terminate();
//		}

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
		try {
			this.socketChannel.close();
			this.selectionKey.cancel();
		} catch (ClosedChannelException ex) {
			// Already closed
		}

		// Return all buffers to pool and clear any writes
	}

	@Override
	public ManagedConnection getConnection() {
		return this;
	}

	@Override
	public StreamBuffer<ByteBuffer> getReadStreamBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReadStreamBuffer(StreamBuffer<ByteBuffer> readStreamBuffer) {
		// TODO Auto-generated method stub

	}

}