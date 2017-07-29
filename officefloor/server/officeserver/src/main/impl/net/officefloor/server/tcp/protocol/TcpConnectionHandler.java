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
package net.officefloor.server.tcp.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.protocol.Connection;
import net.officefloor.server.protocol.ConnectionHandler;
import net.officefloor.server.protocol.ReadContext;
import net.officefloor.server.protocol.WriteBuffer;
import net.officefloor.server.stream.ServerInputStream;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.WriteBufferReceiver;
import net.officefloor.server.stream.impl.ServerInputStreamImpl;
import net.officefloor.server.stream.impl.ServerOutputStreamImpl;
import net.officefloor.server.tcp.ServerTcpConnection;

/**
 * TCP {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class TcpConnectionHandler
		implements ConnectionHandler, AsynchronousManagedObject, WriteBufferReceiver, ServerTcpConnection {

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link ServerInputStream}.
	 */
	private final ServerInputStreamImpl inputStream;

	/**
	 * {@link ServerOutputStream}.
	 */
	private final ServerOutputStreamImpl outputStream;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * {@link Flow} index to handle the connection
	 */
	private final int newConnectionFlowIndex;

	/**
	 * Flag indicating if process started.
	 */
	private boolean isProcessStarted = false;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param sendBufferSize
	 *            Send buffer size.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @param newConnectionFlowIndex
	 *            {@link Flow} index to handle the connection
	 */
	public TcpConnectionHandler(Connection connection, int sendBufferSize,
			ManagedObjectExecuteContext<Indexed> executeContext, int newConnectionFlowIndex) {
		this.connection = connection;
		this.executeContext = executeContext;
		this.newConnectionFlowIndex = newConnectionFlowIndex;

		// Create the input stream
		this.inputStream = new ServerInputStreamImpl(connection.getWriteLock());

		// Create the output stream
		this.outputStream = new ServerOutputStreamImpl(this, sendBufferSize);
	}

	/*
	 * ====================== ConnectionHandler =======================
	 * 
	 * Thread-safe by the lock taken in SockerListener.
	 */

	@Override
	public void handleRead(ReadContext context) throws IOException {

		// Indicate data available from client
		synchronized (this.getWriteLock()) {

			// Write the data
			byte[] data = context.getData();
			this.inputStream.inputData(data, 0, (data.length - 1), true);

			// Notify potential waiting servicing
			if (this.asynchronousContext != null) {
				this.asynchronousContext.complete(null);
			}
		}

		// Only trigger servicing the connection once
		if (!this.isProcessStarted) {
			this.executeContext.invokeProcess(this.newConnectionFlowIndex, this, this, 0, null);
			this.isProcessStarted = true;
		}
	}

	/*
	 * =================== WriteBufferReceiver ============================+
	 */

	@Override
	public Object getWriteLock() {
		return this.connection.getWriteLock();
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
	 * {@link AsynchronousContext}.
	 */
	private AsynchronousContext asynchronousContext;

	@Override
	public void setAsynchronousContext(AsynchronousContext context) {
		synchronized (this.getWriteLock()) {
			this.asynchronousContext = context;
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
		synchronized (this.getWriteLock()) {

			// Determine if data available to read
			if (this.inputStream.available() > 0) {
				// Data is available, so do not wait
				return false;
			}

			// Wait for data from client
			this.asynchronousContext.start(null);
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