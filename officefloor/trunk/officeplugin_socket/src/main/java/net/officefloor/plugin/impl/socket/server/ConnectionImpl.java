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

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.ReadMessage;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;
import net.officefloor.plugin.socket.server.spi.WriteMessage;
import net.officefloor.plugin.socket.server.spi.WriteMessageListener;

/**
 * Implementation of a
 * {@link net.officefloor.plugin.socket.server.spi.Connection}.
 * 
 * @author Daniel
 */
class ConnectionImpl<F extends Enum<F>> implements Connection {

	/**
	 * {@link SocketChannel} for this {@link Connection}.
	 */
	private final SocketChannel socketChannel;

	/**
	 * {@link ConnectionHandler} for this
	 * {@link net.officefloor.plugin.socket.server.spi.Connection}.
	 */
	private final ConnectionHandler handler;

	/**
	 * {@link MessageSegmentPool}.
	 */
	private final MessageSegmentPool messageSegmentPool;

	/**
	 * List of {@link ReadMessage} instances for this {@link Connection}.
	 */
	private final List<ReadMessageImpl> readMessages = new LinkedList<ReadMessageImpl>();

	/**
	 * List of {@link WriteMessage} instances for this {@link Connection}.
	 */
	private final List<WriteMessageImpl> writeMessages = new LinkedList<WriteMessageImpl>();

	/**
	 * Flags if this {@link Connection} has been cancelled.
	 */
	private boolean isCancelled = false;

	/**
	 * Initiate.
	 * 
	 * @param socketChannel
	 *            {@link SocketChannel}.
	 * @param serverSocketHandler
	 *            {@link ServerSocketHandler}.
	 * @param messageSegmentPool
	 *            {@link MessageSegmentPool}.
	 */
	ConnectionImpl(SocketChannel socketChannel,
			ServerSocketHandler<F> serverSocketHandler,
			MessageSegmentPool messageSegmentPool) {

		// Store state
		this.socketChannel = socketChannel;
		this.messageSegmentPool = messageSegmentPool;

		// Create the handler for this connection
		this.handler = serverSocketHandler.createConnectionHandler(this);
	}

	/**
	 * Obtains the {@link SocketChannel} for this {@link Connection}.
	 * 
	 * @return {@link SocketChannel} for this {@link Connection}.
	 */
	SocketChannel getSocketChannel() {
		return this.socketChannel;
	}

	/**
	 * Obtains the {@link ConnectionHandler}.
	 * 
	 * @return {@link ConnectionHandler} for this {@link Connection}.
	 */
	ConnectionHandler getConnectionHandler() {
		return this.handler;
	}

	/**
	 * Obtains the {@link MessageSegmentPool}.
	 * 
	 * @return {@link MessageSegmentPool}.
	 */
	MessageSegmentPool getMessageSegmentPool() {
		return this.messageSegmentPool;
	}

	/**
	 * Obtains the first
	 * {@link net.officefloor.plugin.socket.server.spi.WriteMessage} to write.
	 * 
	 * @return First
	 *         {@link net.officefloor.plugin.socket.server.spi.WriteMessage} to
	 *         write.
	 */
	synchronized WriteMessageImpl getFirstWriteMessage() {
		// Return the first write message
		if (this.writeMessages.size() == 0) {
			// No write message
			return null;
		} else {
			// Return first write message
			return this.writeMessages.get(0);
		}
	}

	/**
	 * Removes the input {@link WriteMessage} from the {@link Connection}.
	 * 
	 * @param writeMessage
	 *            {@link WriteMessage} to remove from this {@link Connection}.
	 */
	synchronized void removeWriteMessage(WriteMessageImpl writeMessage) {
		this.writeMessages.remove(writeMessage);
	}

	/**
	 * Obtains the first {@link ReadMessage} to read.
	 * 
	 * @return {@link ReadMessage}.
	 */
	synchronized ReadMessageImpl getFirstReadMessage() {
		// Return the first read message
		if (this.readMessages.size() == 0) {
			// No read message
			return null;
		} else {
			// Return first read message
			return this.readMessages.get(0);
		}
	}

	/**
	 * Cancels this {@link Connection}.
	 */
	synchronized void cancel() {
		// Flag the connection as cancelled
		this.isCancelled = true;
	}

	/*
	 * ====================================================================
	 * Connection
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#createReadMessage()
	 */
	public ReadMessageImpl createReadMessage() {
		// Create the read message
		ReadMessageImpl readMessage = new ReadMessageImpl(this);

		synchronized (this) {
			// Ensure connection is not cancelled
			if (this.isCancelled) {
				throw new IllegalStateException("Connection has been cancelled");
			}

			// Append to listing of messages to read
			this.readMessages.add(readMessage);
		}

		// Return the read message
		return readMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#createWriteMessage()
	 */
	public WriteMessageImpl createWriteMessage(WriteMessageListener listener) {
		// Create the write message
		WriteMessageImpl writeMessage = new WriteMessageImpl(this, listener);

		synchronized (this) {
			// Ensure connection is not cancelled
			if (this.isCancelled) {
				throw new IllegalStateException("Connection has been cancelled");
			}

			// Append to listing of messages to write
			this.writeMessages.add(writeMessage);
		}

		// Return the write message
		return writeMessage;
	}

}
