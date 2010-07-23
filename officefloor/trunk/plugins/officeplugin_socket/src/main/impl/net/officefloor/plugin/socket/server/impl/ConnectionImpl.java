/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.stream.BufferPopulator;
import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;
import net.officefloor.plugin.stream.synchronise.SynchronizedInputBufferStream;
import net.officefloor.plugin.stream.synchronise.SynchronizedOutputBufferStream;

/**
 * Implementation of a {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionImpl<CH extends ConnectionHandler> implements
		Connection, BufferProcessor, BufferPopulator {

	/**
	 * {@link SocketChannel} of this {@link Connection}.
	 */
	private final NonblockingSocketChannel socketChannel;

	/**
	 * {@link ConnectionHandler} for this {@link Connection}.
	 */
	private final CH connectionHandler;

	/**
	 * {@link BufferStream} containing the data from the client.
	 */
	private final BufferStream fromClientStream;

	/**
	 * {@link SynchronizedInputBufferStream} providing application access to the
	 * data from the client.
	 */
	private final SynchronizedInputBufferStream safeFromClientStream;

	/**
	 * {@link BufferStream} of data to be sent to the client.
	 */
	private final BufferStream toClientStream;

	/**
	 * {@link SynchronizedOutputBufferStream} providing application access to
	 * send data to the client.
	 */
	private final SynchronizedOutputBufferStream safeToClientStream;

	/**
	 * Bytes read/written from/to buffers.
	 */
	private int byteSize;

	/**
	 * {@link SocketListener} handling this {@link Connection}.
	 */
	private SocketListener<CH> socketListener;

	/**
	 * Flag to only notify of write once per check on the {@link Connection}.
	 */
	private boolean isNotifiedOfWrite = false;

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
			ServerSocketHandler<CH> serverSocketHandler,
			BufferSquirtFactory bufferSquirtFactory) {
		this.socketChannel = nonblockingSocketChannel;

		// Create the streams
		this.fromClientStream = new BufferStreamImpl(bufferSquirtFactory);
		this.toClientStream = new BufferStreamImpl(bufferSquirtFactory);

		// Create the safe streams to be used by applications
		this.safeFromClientStream = new SynchronizedInputBufferStream(
				this.fromClientStream.getInputBufferStream(), this.getLock());
		this.safeToClientStream = new SynchronizedOutputBufferStream(
				new ConnectionOutputBufferStream(this.toClientStream
						.getOutputBufferStream()), this.getLock());

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
	void setSocketListener(SocketListener<CH> socketListener) {
		synchronized (this.getLock()) {
			this.socketListener = socketListener;
		}
	}

	/**
	 * Obtains the {@link ConnectionHandler} for this {@link Connection}.
	 * 
	 * @return {@link ConnectionHandler} for this {@link Connection}.
	 */
	CH getConnectionHandler() {
		return this.connectionHandler;
	}

	/**
	 * Registers this {@link Connection} with the {@link Selector}.
	 * 
	 * @param selector
	 *            {@link Selector}.
	 * @param operation
	 *            Operation to register under.
	 * @throws IOException
	 *             If fails to register the {@link Connection} with the
	 *             {@link Selector}.
	 */
	void registerWithSelector(Selector selector, int operation)
			throws IOException {
		this.socketChannel.register(selector, operation, this);
	}

	/**
	 * Flags that this {@link Connection} is being checked by the
	 * {@link SocketListener}.
	 */
	void flagCheckingConnection() {
		// Flag no longer notified of write as written
		this.isNotifiedOfWrite = false;
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
	 * <p>
	 * Obtains the {@link InputBufferStream} to read data available from the
	 * client.
	 * <p>
	 * The {@link InputBufferStream} is not wrapped by
	 * {@link SynchronizedInputBufferStream} as {@link ReadContext} should be
	 * called synchronized on {@link #getLock()}.
	 * 
	 * @return {@link InputBufferStream} to read data available from the client.
	 */
	InputBufferStream getConnectionInputBufferStream() {
		return this.fromClientStream.getInputBufferStream();
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
	 * <p>
	 * Wakes up the {@link SocketListener}.
	 * <p>
	 * This allows to start writing data back to the client immediately rather
	 * than having to wait for the {@link SocketListener} to wake up to process
	 * the writes.
	 */
	void wakeupSocketListener() {

		// Obtain the socket listener
		SocketListener<CH> socketListener;
		synchronized (this.getLock()) {
			socketListener = this.socketListener;
		}

		// Wake up the socket listener if available
		if (socketListener != null) {
			socketListener.wakeup();
		}
	}

	/**
	 * Notifies of a write.
	 */
	void notifyOfWrite() {
		// Only notify if not already notified
		if (this.isNotifiedOfWrite) {
			return;
		}

		// Wake up socket listener to send data to client
		this.wakeupSocketListener();

		// Notified
		this.isNotifiedOfWrite = true;
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

	/**
	 * Terminates this {@link Connection} releasing resources.
	 * 
	 * @throws IOException
	 *             If fails to terminate the {@link Connection}.
	 */
	void terminate() throws IOException {

		// Clean up resources
		this.fromClientStream.closeInput();
		this.fromClientStream.closeOutput();
		this.toClientStream.closeInput();
		this.toClientStream.closeOutput();

		// Close the socket channel
		this.socketChannel.close();
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

	@Override
	public InetSocketAddress getLocalAddress() {
		return this.socketChannel.getLocalAddress();
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return this.socketChannel.getRemoteAddress();
	}

	@Override
	public InputBufferStream getInputBufferStream() {
		return this.safeFromClientStream;
	}

	@Override
	public OutputBufferStream getOutputBufferStream() {
		return this.safeToClientStream;
	}

	/**
	 * {@link Connection} {@link OutputBufferStream}.
	 */
	private class ConnectionOutputBufferStream implements OutputBufferStream {

		/**
		 * Backing {@link OutputBufferStream}.
		 */
		private final OutputBufferStream backingStream;

		/**
		 * Initiate.
		 * 
		 * @param backingStream
		 *            Backing {@link OutputBufferStream}.
		 */
		public ConnectionOutputBufferStream(OutputBufferStream backingStream) {
			this.backingStream = backingStream;
		}

		/*
		 * ================= OutputBufferStream =======================
		 */

		@Override
		public OutputStream getOutputStream() {
			return new ConnectionOutputStream(this.backingStream
					.getOutputStream());
		}

		@Override
		public void write(byte[] bytes) throws IOException {
			this.backingStream.write(bytes);
			ConnectionImpl.this.notifyOfWrite();
		}

		@Override
		public void write(byte[] data, int offset, int length)
				throws IOException {
			this.backingStream.write(data, offset, length);
			ConnectionImpl.this.notifyOfWrite();
		}

		@Override
		public void write(BufferPopulator populator) throws IOException {
			this.backingStream.write(populator);
			ConnectionImpl.this.notifyOfWrite();
		}

		@Override
		public void append(BufferSquirt squirt) throws IOException {
			this.backingStream.append(squirt);
			ConnectionImpl.this.notifyOfWrite();
		}

		@Override
		public void append(ByteBuffer buffer) throws IOException {
			this.backingStream.append(buffer);
			ConnectionImpl.this.notifyOfWrite();
		}

		@Override
		public void close() throws IOException {
			// Cancel connection and terminate will clean up
			ConnectionImpl.this.cancel();
		}
	}

	/**
	 * {@link Connection} {@link OutputStream}.
	 */
	private class ConnectionOutputStream extends OutputStream {

		/**
		 * Backing {@link OutputStream}.
		 */
		private final OutputStream backingStream;

		/**
		 * Initiate.
		 * 
		 * @param backingStream
		 *            Backing {@link OutputStream}.
		 */
		public ConnectionOutputStream(OutputStream backingStream) {
			this.backingStream = backingStream;
		}

		/*
		 * ================== OutputStream ============================
		 */

		@Override
		public void write(int b) throws IOException {
			this.backingStream.write(b);
			ConnectionImpl.this.notifyOfWrite();
		}

		@Override
		public void write(byte[] b) throws IOException {
			this.backingStream.write(b);
			ConnectionImpl.this.notifyOfWrite();
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.backingStream.write(b, off, len);
			ConnectionImpl.this.notifyOfWrite();
		}

		@Override
		public void flush() throws IOException {
			this.backingStream.flush();
			ConnectionImpl.this.notifyOfWrite();
		}

		@Override
		public void close() throws IOException {
			// Cancel connection and terminate will clean up
			ConnectionImpl.this.cancel();
		}
	}

}