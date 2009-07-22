/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.Request;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.stream.BufferPopulator;
import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;
import net.officefloor.plugin.stream.squirtfactory.NotCreateBufferSquirtFactory;

/**
 * Implementation of a {@link Connection}.
 *
 * @author Daniel Sagenschneider
 */
public class ConnectionImpl<F extends Enum<F>> implements Connection,
		BufferProcessor, BufferPopulator {

	/**
	 * {@link SocketChannel} of this {@link Connection}.
	 */
	private final NonblockingSocketChannel socketChannel;

	/**
	 * {@link ConnectionHandler} for this {@link Connection}.
	 */
	private final ConnectionHandler connectionHandler;

	/**
	 * {@link BufferStream} containing the data from the client.
	 */
	private final BufferStream fromClientStream;

	/**
	 * {@link BufferStream} of data to be sent to the client.
	 */
	private final BufferStream toClientStream;

	/**
	 * Bytes read/written from/to buffers.
	 */
	private int byteSize;

	/**
	 * {@link SocketListener} handling this {@link Connection}.
	 */
	private SocketListener socketListener;

	/**
	 * Flags if this {@link Connection} has been cancelled.
	 */
	private volatile boolean isCancelled = false;

	/**
	 * Initiate.
	 *
	 * @param nonblockingSocketChannel
	 *            {@link NonblockingSocketChannel}.
	 * @param serverSocketHandler
	 *            {@link ServerSocketHandler}.
	 * @param bufferSquirtFactory
	 *            {@link BufferSquirtFactory}.
	 */
	public ConnectionImpl(NonblockingSocketChannel nonblockingSocketChannel,
			ServerSocketHandler<?> serverSocketHandler,
			BufferSquirtFactory bufferSquirtFactory) {
		this.socketChannel = nonblockingSocketChannel;

		// Create the streams
		this.fromClientStream = new BufferStreamImpl(bufferSquirtFactory);
		this.toClientStream = new BufferStreamImpl(bufferSquirtFactory);

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
		synchronized (this) {
			this.socketListener = socketListener;
		}
	}

	/**
	 * Obtains the {@link ConnectionHandler} for this {@link Connection}.
	 *
	 * @return {@link ConnectionHandler} for this {@link Connection}.
	 */
	ConnectionHandler getConnectionHandler() {
		return this.connectionHandler;
	}

	/**
	 * Obtains the {@link NonblockingSocketChannel} for this {@link Connection}.
	 *
	 * @return {@link NonblockingSocketChannel} for this {@link Connection}.
	 */
	NonblockingSocketChannel getSocketChannel() {
		return this.socketChannel;
	}

	/**
	 * Reads data from the client.
	 *
	 * @return Number of bytes read from the client. -1 indicates connection
	 *         closed.
	 * @throws IOException
	 *             If fails to read data from client.
	 */
	int readDataFromClient() throws IOException {
		this.byteSize = 0;
		this.fromClientStream.getOutputBufferStream().write(this);
		return this.byteSize;
	}

	/**
	 * Obtains the {@link InputStream} to browse the data available from the
	 * client.
	 *
	 * @return {@link InputStream} to browse the data available from the client.
	 */
	InputStream getAvailableDataFromClientBrowseStream() {
		return this.fromClientStream.getInputBufferStream().getBrowseStream();
	}

	/**
	 * Indicates if there is data to be written to the client.
	 *
	 * @return <code>true</code> if there is data to be written to the client.
	 */
	boolean isDataForClient() {
		return (this.toClientStream.getInputBufferStream().available() > 0);
	}

	/**
	 * Writes data to the client.
	 *
	 * @return Number of bytes written to the client. -1 indicates connection
	 *         closed.
	 * @throws IOException
	 *             If fails to write data to the client.
	 */
	int writeDataToClient() throws IOException {
		this.byteSize = 0;
		this.toClientStream.getInputBufferStream().read(this);
		return byteSize;
	}

	/**
	 * Creates the {@link Request}.
	 *
	 * @param requestByteSize
	 *            Number of bytes for the {@link Request}.
	 * @param attachment
	 *            Attachment for the {@link Request}.
	 * @return {@link Request}.
	 * @throws IOException
	 *             If fails to read {@link Request}.
	 */
	Request createRequest(long requestByteSize, Object attachment)
			throws IOException {

		// Create the request
		BufferStream content = new BufferStreamImpl(
				new NotCreateBufferSquirtFactory());
		Request request = new RequestImpl(content.getInputBufferStream(),
				attachment);

		// Write content to the request (take into account long -> int)
		OutputBufferStream outputBufferStream = content.getOutputBufferStream();
		long remaining = requestByteSize;
		while (remaining > Integer.MAX_VALUE) {
			this.fromClientStream.read(Integer.MAX_VALUE, outputBufferStream);
			remaining -= Integer.MAX_VALUE;
		}
		this.fromClientStream.read((int) remaining, outputBufferStream);
		outputBufferStream.close();

		// Return the request
		return request;
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

		// Obtain the socket listener
		SocketListener socketListener;
		synchronized (this) {
			socketListener = this.socketListener;
		}

		// Wake up the socket listener if available
		if (socketListener != null) {
			socketListener.wakeup();
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

		// Wake up socket listener to process closing
		this.wakeupSocketListener();
	}

	/**
	 * Indicates if this {@link Connection} has been flagged for closing.
	 *
	 * @return <code>true</code> if this {@link Connection} flagged for closing.
	 */
	boolean isCancelled() {
		return this.isCancelled;
	}

	/*
	 * ================== BufferPopulator ===================================
	 */

	@Override
	public void populate(ByteBuffer buffer) throws IOException {
		this.byteSize = this.socketChannel.read(buffer);
	}

	/*
	 * =================== BufferProcessor =================================
	 */

	@Override
	public void process(ByteBuffer buffer) throws IOException {
		this.byteSize = this.socketChannel.write(buffer);
	}

	/*
	 * ================= Connection =======================================
	 */

	@Override
	public Object getLock() {
		return this;
	}

	// TODO provide synchronized wrapped streams for thread safety
	// TODO hook into close to trigger close of connection

	@Override
	public InputBufferStream getInputBufferStream() {
		return this.fromClientStream.getInputBufferStream();
	}

	@Override
	public OutputBufferStream getOutputBufferStream() {
		return this.toClientStream.getOutputBufferStream();
	}

}