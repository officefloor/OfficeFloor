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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.Message;
import net.officefloor.plugin.socket.server.spi.MessageSegment;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;
import net.officefloor.plugin.socket.server.spi.WriteMessageListener;

/**
 * Implementation of a {@link Connection}.
 * 
 * @author Daniel
 */
class ConnectionImpl<F extends Enum<F>> implements Connection {

	/**
	 * {@link SocketChannel} of this {@link Connection}.
	 */
	final NonblockingSocketChannel socketChannel;

	/**
	 * {@link ConnectionHandler} for this {@link Connection}.
	 */
	final ConnectionHandler connectionHandler;

	/**
	 * Recommended {@link MessageSegment} count per {@link Message}.
	 */
	final int recommendedSegmentCount;

	/**
	 * {@link MessageSegmentPool}.
	 */
	final MessageSegmentPool messageSegmentPool;

	/**
	 * {@link Stream} of {@link ReadMessageImpl} instances.
	 */
	final Stream<ReadMessageImpl> readStream;

	/**
	 * {@link Stream} of {@link WriteMessageImpl} instances.
	 */
	final Stream<WriteMessageImpl> writeStream;

	/**
	 * {@link SocketListener} handling this {@link Connection}.
	 */
	private SocketListener socketListener;

	/**
	 * Flags if this {@link Connection} has been cancelled.
	 */
	private boolean isCancelled = false;

	/**
	 * Initiate.
	 * 
	 * @param nonblockingSocketChannel
	 *            {@link NonblockingSocketChannel}.
	 * @param serverSocketHandler
	 *            {@link ServerSocketHandler}.
	 * @param recommendedSegmentCount
	 *            Recommended {@link MessageSegment} count per {@link Message}.
	 * @param messageSegmentPool
	 *            {@link MessageSegmentPool}.
	 */
	ConnectionImpl(NonblockingSocketChannel nonblockingSocketChannel,
			ServerSocketHandler<F> serverSocketHandler,
			int recommendedSegmentCount, MessageSegmentPool messageSegmentPool) {

		// Store state
		this.socketChannel = nonblockingSocketChannel;
		this.recommendedSegmentCount = recommendedSegmentCount;
		this.messageSegmentPool = messageSegmentPool;

		// Create the streams
		this.readStream = new Stream<ReadMessageImpl>(this,
				new MessageFactory<ReadMessageImpl>() {
					@Override
					public ReadMessageImpl createMessage(
							Stream<ReadMessageImpl> stream,
							WriteMessageListener listener) {
						return new ReadMessageImpl(stream);
					}
				});
		this.writeStream = new Stream<WriteMessageImpl>(this,
				new MessageFactory<WriteMessageImpl>() {
					@Override
					public WriteMessageImpl createMessage(
							Stream<WriteMessageImpl> stream,
							WriteMessageListener listener) {
						return new WriteMessageImpl(stream, listener);
					}
				});

		// Create the handler for this connection
		this.connectionHandler = serverSocketHandler
				.createConnectionHandler(this);
	}

	/**
	 * Specifies the {@link SocketListener} handling this {@link Connection}.
	 * 
	 * @param socketListener
	 *            {@link SocketListener} handling this {@link Connection}.
	 */
	void setSocketListener(SocketListener socketListener) {
		this.socketListener = socketListener;
	}

	/**
	 * <p>
	 * Wakes up the {@link SocketListener}.
	 * <p>
	 * This allows to start writing data back to the client immediately rather
	 * than having to wait for the {@link SocketListener} to wake up to process
	 * the writes.
	 */
	void wakeupSocketListener() {
		if (this.socketListener != null) {
			this.socketListener.wakeup();
		}
	}

	/**
	 * Ensures the state of the {@link Connection} is valid for further I/O
	 * operations.
	 * 
	 * @throws IOException
	 *             Indicate the state is not valid.
	 */
	void checkIOState() throws IOException {
		if (this.isCancelled) {
			throw new ClosedChannelException();
		}
	}

	/**
	 * Cancels this {@link Connection}.
	 */
	void cancel() {
		// Flag the connection as cancelled
		this.isCancelled = true;

		// Wakeup socket listener to process closing
		this.wakeupSocketListener();
	}

	/**
	 * Indicates if this {@link Connection} has been flagged for closing.
	 * 
	 * @return <code>true</code> if this {@link Connection} flagged for
	 *         closing.
	 */
	boolean isCancelled() {
		return this.isCancelled;
	}

	/*
	 * ====================================================================
	 * Connection
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#getLock()
	 */
	@Override
	public Object getLock() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#getFirstReadMessage()
	 */
	@Override
	public ReadMessageImpl getFirstReadMessage() {
		return this.readStream.getFirstMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#read(byte[],
	 *      int, int)
	 */
	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		// Read data from the read stream
		return this.readStream.read(buffer, offset, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#getActiveWriteMessage()
	 */
	@Override
	public WriteMessageImpl getActiveWriteMessage() {
		// Last write message is the active write message
		return this.writeStream.getLastMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#write(byte[],
	 *      int, int)
	 */
	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		// Write data to the write stream
		this.writeStream.write(data, offset, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#write(java.nio.ByteBuffer)
	 */
	@Override
	public void write(ByteBuffer data) throws IOException {
		// Append data to the write stream
		this.writeStream.write(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#flush()
	 */
	@Override
	public void flush() {
		// Flush write stream
		this.writeStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Connection#createWriteMessage()
	 */
	@Override
	public WriteMessageImpl createWriteMessage(WriteMessageListener listener)
			throws IOException {

		// Ensure connection is not cancelled
		this.checkIOState();

		// Write the (to now be previous) active message
		WriteMessageImpl activeMessage = this.getActiveWriteMessage();
		if (activeMessage != null) {
			activeMessage.write();
		}

		// Create the write message
		WriteMessageImpl writeMessage = this.writeStream
				.appendMessage(listener);

		// Return the created write message
		return writeMessage;
	}

}
