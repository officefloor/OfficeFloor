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
package net.officefloor.plugin.socket.server.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.Request;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.socket.server.tcp.TcpServer.TcpServerFlows;
import net.officefloor.plugin.socket.server.tcp.api.ServerTcpConnection;

/**
 * TCP {@link ConnectionHandler}.
 *
 * @author Daniel Sagenschneider
 */
public class TcpConnectionHandler implements ConnectionHandler,
		AsynchronousManagedObject, ServerTcpConnection {

	/**
	 * Value of {@link #idleSinceTimestamp} if the {@link Connection} is not
	 * idle.
	 */
	private static final long NON_IDLE_SINCE_TIMESTAMP = -1;

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link OutputStream} for the {@link ServerTcpConnection}.
	 */
	private final OutputStream outputStream;

	/**
	 * Maximum idle time for the {@link Connection} measure in milliseconds.
	 */
	// TODO provide from constructor
	private final long maxIdleTime = 100000;

	/**
	 * Flag indicating if read started.
	 */
	private boolean isReadStarted = false;

	/**
	 * Flag indicating if process started.
	 */
	private boolean isProcessStarted = false;

	/**
	 * Flag indicating to close the {@link Connection}.
	 */
	private boolean isClose = false;

	/**
	 * Time stamp that the {@link Connection} went idle.
	 */
	private long idleSinceTimestamp = NON_IDLE_SINCE_TIMESTAMP;

	/**
	 * Initiate.
	 *
	 * @param connection
	 *            {@link Connection}.
	 */
	public TcpConnectionHandler(Connection connection) {
		this.connection = connection;

		// Create the output stream
		this.outputStream = new TcpOutputStream();
	}

	/**
	 * <p>
	 * Called by the {@link TcpServer} to start processing the
	 * {@link Connection} of this {@link ConnectionHandler}.
	 * <p>
	 * Called within the lock on the {@link Connection}.
	 *
	 * @param request
	 *            {@link Request}.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 */
	public void invokeProcess(Request request,
			ManagedObjectExecuteContext<TcpServerFlows> executeContext) {

		// Only invoke the process once
		if (!this.isProcessStarted) {

			// Invokes the process
			executeContext.invokeProcess(TcpServerFlows.NEW_CONNECTION, this,
					this);

			// Indicate process started
			this.isProcessStarted = true;
		}
	}

	/*
	 * ==================================================================
	 * ConnectionHandler
	 *
	 * Thread-safe by the lock taken in SockerListener.
	 * ==================================================================
	 */

	@Override
	public void handleIdleConnection(IdleContext context) {

		// Indicate if close connection
		if (this.isClose) {
			context.setCloseConnection(true);
		}

		// Determine if have to handle connection idle too long
		if (this.idleSinceTimestamp == NON_IDLE_SINCE_TIMESTAMP) {
			// Connection has now become idle
			this.idleSinceTimestamp = context.getTime();
		} else {
			// Connection already idle so determine if idle too long
			long currentTime = context.getTime();
			long idleTime = currentTime - this.idleSinceTimestamp;
			if (idleTime > this.maxIdleTime) {
				// Connection idle too long so close
				this.isClose = true;
				context.setCloseConnection(true);

				// Awake potential waiting process on client data
				synchronized (this.getLock()) {
					if (this.asynchronousListener != null) {
						this.asynchronousListener.notifyComplete();
					}
				}
			}
		}
	}

	@Override
	public void handleRead(ReadContext context) {

		// Connection not idle
		this.idleSinceTimestamp = NON_IDLE_SINCE_TIMESTAMP;

		// Indicate data available from client
		synchronized (this.getLock()) {
			if (this.asynchronousListener != null) {
				this.asynchronousListener.notifyComplete();
			}
		}

		// Determine if close connection
		if (this.isClose) {
			context.setCloseConnection(true);
			return; // closing so no further reading
		}

		// Always new message on start reading to kick of processing
		if (!this.isReadStarted) {
			// To ensure streaming always input first read
			context.requestReceived(0, null);

			// Flag that the read has started
			this.isReadStarted = true;
		}
	}

	@Override
	public void handleWrite(WriteContext context) {

		// Connection not idle
		this.idleSinceTimestamp = NON_IDLE_SINCE_TIMESTAMP;

		// Determine if close connection
		if (this.isClose) {
			context.setCloseConnection(true);
			return;
		}

		// Just keep writing
	}

	/*
	 * ================= AsynchronousManagedObject ======================
	 */

	/**
	 * {@link AsynchronousListener}.
	 */
	private AsynchronousListener asynchronousListener;

	@Override
	public void registerAsynchronousCompletionListener(
			AsynchronousListener listener) {
		synchronized (this.getLock()) {
			this.asynchronousListener = listener;
		}
	}

	@Override
	public Object getObject() throws Exception {
		return this;
	}

	/*
	 * ====================== ServerTcpConnection =======================
	 */

	/**
	 * Ensures the connection is open.
	 */
	private void ensureConnectionOpen() throws IOException {
		if (this.isClose) {
			throw new ClosedChannelException();
		}
	}

	@Override
	public Object getLock() {
		return this.connection.getLock();
	}

	@Override
	public void close() throws IOException {
		synchronized (this.getLock()) {
			// Flush writes and close connection
			this.flushWrites(true);
		}
	}

	@Override
	public boolean isClosed() {
		synchronized (this.getLock()) {
			return this.isClose;
		}
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return this.read(buffer, 0, buffer.length);
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		synchronized (this.getLock()) {
			this.ensureConnectionOpen();
			return this.connection.getInputBufferStream().read(buffer, offset,
					length);
		}
	}

	@Override
	public void waitOnClientData() throws IOException {
		synchronized (this.getLock()) {
			this.ensureConnectionOpen();

			// Determine if data available to read
			if (this.connection.getInputBufferStream().available() > 0) {
				// Data is available, so do not wait
				return;
			}

			// Wait for data from client
			this.asynchronousListener.notifyStarted();
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return this.outputStream;
	}

	@Override
	public void write(ByteBuffer buffer) throws IOException {
		synchronized (this.getLock()) {
			this.ensureConnectionOpen();
			this.connection.getOutputBufferStream().append(buffer);
		}
	}

	/**
	 * Writes the data to the {@link Connection}.
	 *
	 * @param data
	 *            Data.
	 * @param offset
	 *            Offset into the data.
	 * @param length
	 *            Number of bytes from the offset to write.
	 */
	private void write(byte[] data, int offset, int length) throws IOException {
		synchronized (this.getLock()) {
			this.ensureConnectionOpen();
			this.connection.getOutputBufferStream().write(data, offset, length);
		}
	}

	/**
	 * Flushes the write content and possibly closes the {@link Connection}.
	 *
	 * @param isClose
	 *            Flag indicating to close the {@link Connection} after
	 *            flushing.
	 */
	private void flushWrites(boolean isClose) {
		synchronized (this.getLock()) {
			// Flag if closed
			if (isClose) {
				this.isClose = isClose;
			}
		}
	}

	/**
	 * {@link OutputStream} for this {@link ServerTcpConnection}.
	 */
	private class TcpOutputStream extends OutputStream {

		/*
		 * ========== OutputStream ==============================
		 */

		@Override
		public void write(int b) throws IOException {
			this.write(new byte[] { (byte) b });
		}

		@Override
		public void write(byte[] b) throws IOException {
			this.write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			synchronized (TcpConnectionHandler.this.getLock()) {
				TcpConnectionHandler.this.write(b, off, len);
			}
		}

		@Override
		public void flush() throws IOException {
			synchronized (TcpConnectionHandler.this.getLock()) {
				TcpConnectionHandler.this.flushWrites(false);
			}
		}

		@Override
		public void close() throws IOException {
			synchronized (TcpConnectionHandler.this.getLock()) {
				TcpConnectionHandler.this.flushWrites(true);
			}
		}
	}

}