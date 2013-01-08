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
package net.officefloor.plugin.socket.server.tcp.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;
import net.officefloor.plugin.socket.server.protocol.HeartBeatContext;
import net.officefloor.plugin.socket.server.protocol.ReadContext;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.ServerInputStream;
import net.officefloor.plugin.stream.WriteBufferReceiver;
import net.officefloor.plugin.stream.impl.ServerOutputStreamImpl;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

/**
 * TCP {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class TcpConnectionHandler implements ConnectionHandler,
		AsynchronousManagedObject, WriteBufferReceiver, ServerTcpConnection {

	/**
	 * Value of {@link #idleSinceTimestamp} if the {@link Connection} is not
	 * idle.
	 */
	private static final long NON_IDLE_SINCE_TIMESTAMP = -1;

	/**
	 * {@link TcpCommunicationProtocol}.
	 */
	private final TcpCommunicationProtocol protocol;

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * Maximum idle time for the {@link Connection} measured in milliseconds.
	 */
	private final long maxIdleTime;

	/**
	 * {@link ServerInputStream}.
	 */
	private final ServerInputStreamImpl inputStream;

	/**
	 * {@link ServerOutputStream}.
	 */
	private final ServerOutputStreamImpl outputStream;

	/**
	 * Flag indicating if process started.
	 */
	private boolean isProcessStarted = false;

	/**
	 * Time stamp that the {@link Connection} went idle.
	 */
	private long idleSinceTimestamp = NON_IDLE_SINCE_TIMESTAMP;

	/**
	 * Initiate.
	 * 
	 * @param protocol
	 *            {@link TcpCommunicationProtocol}.
	 * @param connection
	 *            {@link Connection}.
	 * @param sendBufferSize
	 *            Send buffer size.
	 * @param maxIdleTime
	 *            Maximum idle time for the {@link Connection} measured in
	 *            milliseconds.
	 */
	public TcpConnectionHandler(TcpCommunicationProtocol protocol,
			Connection connection, int sendBufferSize, long maxIdleTime) {
		this.protocol = protocol;
		this.connection = connection;
		this.maxIdleTime = maxIdleTime;

		// Create the input stream
		this.inputStream = new ServerInputStreamImpl(connection.getLock());

		// Create the output stream
		this.outputStream = new ServerOutputStreamImpl(this, sendBufferSize);
	}

	/*
	 * ====================== ConnectionHandler =======================
	 * 
	 * Thread-safe by the lock taken in SockerListener.
	 */

	@Override
	public void handleHeartbeat(HeartBeatContext context) throws IOException {

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
				this.connection.close();

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
	public void handleRead(ReadContext context) throws IOException {

		// Connection not idle
		this.idleSinceTimestamp = NON_IDLE_SINCE_TIMESTAMP;

		// Indicate data available from client
		synchronized (this.getLock()) {

			// Write the data
			byte[] data = context.getData();
			this.inputStream.inputData(data, 0, (data.length - 1), true);

			// Notify potential waiting servicing
			if (this.asynchronousListener != null) {
				this.asynchronousListener.notifyComplete();
			}
		}

		// Only trigger servicing the connection once
		if (!this.isProcessStarted) {
			this.protocol.serviceConnection(this);
			this.isProcessStarted = true;
		}
	}

	/*
	 * =================== WriteBufferReceiver ============================+
	 */

	@Override
	public Object getLock() {
		return this.connection.getLock();
	}

	@Override
	public WriteBuffer createWriteBuffer(byte[] data, int length) {
		return this.connection.createWriteBuffer(data, length);
	}

	@Override
	public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
		return this.connection.createWriteBuffer(buffer);
	}

	@Override
	public void writeData(WriteBuffer[] data) throws IOException {

		// Connection not idle
		this.idleSinceTimestamp = NON_IDLE_SINCE_TIMESTAMP;

		// Write the data
		this.connection.writeData(data);
	}

	@Override
	public void close() throws IOException {
		this.connection.close();
	}

	@Override
	public boolean isClosed() {
		return this.connection.isClosed();
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
	public Object getObject() {
		return this;
	}

	/*
	 * ====================== ServerTcpConnection =======================
	 */

	@Override
	public boolean waitOnClientData() throws IOException {
		synchronized (this.getLock()) {

			// Determine if data available to read
			if (this.inputStream.available() > 0) {
				// Data is available, so do not wait
				return false;
			}

			// Wait for data from client
			this.asynchronousListener.notifyStarted();
			return true; // waiting on client data
		}
	}

	@Override
	public ServerInputStream getInputStream() {
		return this.inputStream;
	}

	@Override
	public ServerOutputStream getOutputStream() {
		return this.outputStream;
	}

}