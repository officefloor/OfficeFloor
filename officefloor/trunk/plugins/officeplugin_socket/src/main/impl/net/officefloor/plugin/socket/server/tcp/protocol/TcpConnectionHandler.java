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

package net.officefloor.plugin.socket.server.tcp.protocol;

import java.io.IOException;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;
import net.officefloor.plugin.socket.server.protocol.HeartBeatContext;
import net.officefloor.plugin.socket.server.protocol.ReadContext;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;

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
	 * {@link CommunicationProtocol}.
	 */
	private final CommunicationProtocol<TcpConnectionHandler> server;

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * Maximum idle time for the {@link Connection} measured in milliseconds.
	 */
	private final long maxIdleTime;

	/**
	 * Flag indicating if read started.
	 */
	private boolean isReadStarted = false;

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
	 * @param server
	 *            {@link CommunicationProtocol}.
	 * @param connection
	 *            {@link Connection}.
	 * @param maxIdleTime
	 *            Maximum idle time for the {@link Connection} measured in
	 *            milliseconds.
	 */
	public TcpConnectionHandler(CommunicationProtocol<TcpConnectionHandler> server,
			Connection connection, long maxIdleTime) {
		this.server = server;
		this.connection = connection;
		this.maxIdleTime = maxIdleTime;
	}

	/**
	 * <p>
	 * Called by the {@link TcpServer} to start processing the
	 * {@link Connection} of this {@link ConnectionHandler}.
	 * <p>
	 * Called within the lock on the {@link Connection}.
	 * 
	 * @param newConnectionFlowIndex
	 *            Flow index to handle a new connection.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 */
	public void invokeProcess(int newConnectionFlowIndex,
			ManagedObjectExecuteContext<Indexed> executeContext) {

		// Only invoke the process once
		if (!this.isProcessStarted) {

			// Invokes the process
			executeContext.invokeProcess(newConnectionFlowIndex, this, this, 0);

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
	public void handleIdleConnection(HeartBeatContext context) throws IOException {

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
				this.connection.getOutputBufferStream().close();
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
	public void handleRead(ReadContext context) throws IOException {

		// Connection not idle
		this.idleSinceTimestamp = NON_IDLE_SINCE_TIMESTAMP;

		// Indicate data available from client
		synchronized (this.getLock()) {
			if (this.asynchronousListener != null) {
				this.asynchronousListener.notifyComplete();
			}
		}

		// Always new message on start reading to kick of processing
		if (!this.isReadStarted) {
			// To ensure streaming always input first read
			this.server.processRequest(this, null);

			// Flag that the read has started
			this.isReadStarted = true;
		}
	}

	@Override
	public void handleWrite(WriteContext context) {

		// Connection not idle
		this.idleSinceTimestamp = NON_IDLE_SINCE_TIMESTAMP;

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
	public Object getObject() {
		return this;
	}

	/*
	 * ====================== ServerTcpConnection =======================
	 */

	@Override
	public Object getLock() {
		return this.connection.getLock();
	}

	@Override
	public InputBufferStream getInputBufferStream() {
		return this.connection.getInputBufferStream();
	}

	@Override
	public void waitOnClientData() throws IOException {
		synchronized (this.getLock()) {

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
	public OutputBufferStream getOutputBufferStream() {
		return this.connection.getOutputBufferStream();
	}

}