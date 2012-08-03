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

package net.officefloor.plugin.socket.server.ssl.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandlerContext;
import net.officefloor.plugin.socket.server.protocol.HeartBeatContext;
import net.officefloor.plugin.socket.server.protocol.ReadContext;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.ssl.SslTaskExecutor;

/**
 * SSL {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslConnectionHandler implements ConnectionHandler, ReadContext,
		HeartBeatContext {

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link SSLEngine}.
	 */
	private final SSLEngine engine;

	/**
	 * {@link SslTaskExecutor}.
	 */
	private final SslTaskExecutor taskExecutor;

	/**
	 * Wrapped {@link ConnectionHandler}.
	 */
	private final ConnectionHandler wrappedConnectionHandler;

	/**
	 * {@link Queue} of read data to be processed.
	 */
	private final Queue<byte[]> readData = new LinkedList<byte[]>();

	/**
	 * {@link Queue} of write data to be processed.
	 */
	private final Queue<WriteBuffer> writeData = new LinkedList<WriteBuffer>();

	/**
	 * Indicates if closing.
	 */
	private boolean isClosing = false;

	/**
	 * Current {@link SslTask} being run.
	 */
	private SslTask task = null;

	/**
	 * Failure in attempting to process.
	 */
	private IOException failure = null;

	/**
	 * To enable wrapped {@link ConnectionHandler} to use context object, need
	 * to intercept this and delegate to the {@link SslContextObject} to
	 * provide.
	 */
	private ConnectionHandlerContext connectionHandlerContext;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param engine
	 *            {@link SSLEngine}.
	 * @param taskExecutor
	 *            {@link SslTaskExecutor}.
	 * @param wrappedCommunicationProtocol
	 *            Wrapped {@link CommunicationProtocol}.
	 */
	public SslConnectionHandler(Connection connection, SSLEngine engine,
			SslTaskExecutor taskExecutor,
			CommunicationProtocol wrappedCommunicationProtocol) {
		this.engine = engine;
		this.taskExecutor = taskExecutor;
		this.connection = connection;

		// Create the connection handler to wrap
		Connection sslConnection = new SslConnectionImpl(this, this.connection);
		this.wrappedConnectionHandler = wrappedCommunicationProtocol
				.createConnectionHandler(sslConnection);
	}

	/**
	 * Process the data.
	 */
	private void process() {

		try {

			// Do not process if failure
			if (this.failure != null) {
				this.readData.clear();
				this.writeData.clear();
				return;
			}

			// Do not process if a task is being run
			if (this.task != null) {
				return;
			}

			// Process until not able to process further
			for (;;) {

				// Process based on state of engine
				boolean isInputData = false;
				boolean isOutputData = false;
				HandshakeStatus handshakeStatus = this.engine
						.getHandshakeStatus();
				switch (handshakeStatus) {
				case NOT_HANDSHAKING:
					// Flag data to process
					isInputData = (this.readData.size() > 0);
					isOutputData = (this.writeData.size() > 0);
					break;

				case NEED_WRAP:
					// Must always output data if required
					isOutputData = true;
					break;

				case NEED_UNWRAP:
					// Ensure have data to unwrap
					if (this.readData.size() == 0) {
						return; // must have data available to unwrap
					} else {
						// Input the data to unwrap
						isInputData = true;
					}
					break;

				case NEED_TASK:
					// Trigger processing of the delegated task
					Runnable delegatedTask = this.engine.getDelegatedTask();
					this.task = new SslTask(delegatedTask);
					this.taskExecutor.beginTask(this.task);
					return; // Must wait on task to complete

				default:
					// Should only be in above states
					throw new IllegalStateException("Illegal "
							+ SSLEngine.class.getSimpleName()
							+ " handshake state " + handshakeStatus);
				}

				// Handle actions
				if (isInputData || this.isClosing) {
					// Handle inputting data

					// Obtain temporary buffer to receive plain text
					int applicationBufferSize = this.engine.getSession()
							.getApplicationBufferSize();
					byte[] tempBytes = new byte[applicationBufferSize];
					ByteBuffer tempBuffer = ByteBuffer.wrap(tempBytes);

					// Obtain all the read data
					int availableData = 0;
					for (byte[] data : this.readData) {
						availableData += data.length;
					}
					byte[] dataToUnwrap = new byte[availableData];
					int readDataIndex = 0;
					for (byte[] data : this.readData) {
						System.arraycopy(data, 0, dataToUnwrap, readDataIndex,
								data.length);
						readDataIndex += data.length;
					}
					this.readData.clear();

					// Unwrap the read data
					SSLEngineResult sslEngineResult = this.engine.unwrap(
							ByteBuffer.wrap(dataToUnwrap), tempBuffer);

					// Determine if further data to consume on another pass
					int bytesConsumed = sslEngineResult.bytesConsumed();
					if (bytesConsumed < availableData) {
						// Make data available for further reading
						byte[] furtherData = new byte[availableData
								- bytesConsumed];
						System.arraycopy(dataToUnwrap, bytesConsumed,
								furtherData, 0, furtherData.length);
						this.readData.add(furtherData);
					}

					// Process based on status
					Status status = sslEngineResult.getStatus();
					switch (status) {
					case BUFFER_UNDERFLOW:
						// Return waiting for more data as require more
						return;
					case OK:
						// Handle the unwrapped data
						int bytesProduced = sslEngineResult.bytesProduced();
						this.readContextData = new byte[bytesProduced];
						System.arraycopy(tempBytes, 0, this.readContextData, 0,
								bytesProduced);
						this.wrappedConnectionHandler.handleRead(this);
						break;
					case CLOSED:
						// Should be no application input data on close.
						// Determine if in close handshake.
						HandshakeStatus closeHandshakeStatus = this.engine
								.getHandshakeStatus();
						switch (closeHandshakeStatus) {
						case NEED_TASK:
						case NEED_UNWRAP:
						case NEED_WRAP:
							// Allow close handshake to proceed
							break;
						case NOT_HANDSHAKING:
							// Close handshake complete, close connection
							this.connection.close();
							break;
						default:
							throw new IllegalStateException("Unknown status "
									+ status);
						}
						break;
					default:
						throw new IllegalStateException("Unknown status "
								+ status);
					}

				}

				if (isOutputData || this.isClosing) {
					// Handle outputting data

					// Obtain temporary buffer to receive cipher text
					int packetBufferSize = this.engine.getSession()
							.getPacketBufferSize();
					byte[] tempBytes = new byte[packetBufferSize];
					ByteBuffer tempBuffer = ByteBuffer.wrap(tempBytes);

					// Obtain all the write data
					int availableData = 0;
					for (WriteBuffer buffer : this.writeData) {
						int length = buffer.length();
						length = (length == -1) ? buffer.getDataBuffer()
								.remaining() : length;
						availableData += length;
					}
					byte[] dataToWrap = new byte[availableData];
					int writeDataIndex = 0;
					for (WriteBuffer buffer : this.writeData) {
						byte[] data = buffer.getData();
						int length;
						if (data != null) {
							length = buffer.length();
							System.arraycopy(data, 0, dataToWrap,
									writeDataIndex, length);
							writeDataIndex += length;
						} else {
							ByteBuffer dataBuffer = buffer.getDataBuffer();
							length = dataBuffer.remaining();
							dataBuffer.put(dataToWrap, writeDataIndex, length);
						}
						writeDataIndex += length;
					}

					// Wrap the written data
					SSLEngineResult sslEngineResult = this.engine.wrap(
							ByteBuffer.wrap(dataToWrap), tempBuffer);

					// Determine if further data to consume on another pass
					int bytesConsumed = sslEngineResult.bytesConsumed();
					if (bytesConsumed < availableData) {
						// Make data available for further writing
						byte[] furtherData = new byte[availableData
								- bytesConsumed];
						System.arraycopy(dataToWrap, bytesConsumed,
								furtherData, 0, furtherData.length);
						this.writeData.add(this.connection.createWriteBuffer(
								furtherData, furtherData.length));

					} else {
						// All data written, so check if close
						if (this.isClosing) {
							this.engine.closeOutbound();
						}
					}

					// Process based on status
					Status status = sslEngineResult.getStatus();
					switch (status) {
					case OK:
					case CLOSED:
						// Transfer the cipher text to connection
						int bytesProduced = sslEngineResult.bytesProduced();
						byte[] cipherData = new byte[bytesProduced];
						System.arraycopy(tempBytes, 0, cipherData, 0,
								bytesProduced);
						this.connection
								.writeData(new WriteBuffer[] { this.connection
										.createWriteBuffer(cipherData,
												cipherData.length) });

						// Handle close
						if (status == Status.CLOSED) {

							// Determine if in close handshake
							HandshakeStatus closeHandshakeStatus = this.engine
									.getHandshakeStatus();
							switch (closeHandshakeStatus) {
							case NEED_TASK:
							case NEED_UNWRAP:
							case NEED_WRAP:
								// Allow close handshake to proceed
								break;
							case NOT_HANDSHAKING:
								// Close handshake complete, close connection
								this.connection.close();
								break;
							default:
								throw new IllegalStateException(
										"Unknown status " + status);
							}
						}
						break;
					default:
						throw new IllegalStateException("Unknown status "
								+ status);
					}
				}
			}

		} catch (IOException ex) {
			// Record failure of processing to fail further interaction
			this.failure = ex;
		}
	}

	/**
	 * Written data to the SSL {@link Connection}.
	 * 
	 * @param data
	 *            {@link WriteBuffer} data.
	 */
	void writeData(WriteBuffer[] data) {

		synchronized (this.connection.getLock()) {

			// Queue the data
			for (WriteBuffer buffer : data) {
				this.writeData.add(buffer);
			}

			// Process data
			this.process();
		}
	}

	/**
	 * Triggers the closing the SSL {@link Connection}.
	 */
	void triggerClose() {

		synchronized (this.connection.getLock()) {

			// Flag to close
			this.isClosing = true;

			// Process the closing
			this.process();
		}
	}

	/**
	 * Indicates if closing or closed.
	 * 
	 * @return <code>true</code> if closing or closed.
	 */
	boolean isClosing() {

		synchronized (this.connection.getLock()) {

			// Indicate if closing or closed
			return this.isClosing;
		}
	}

	/*
	 * ======================== ConnectionHandler ============================
	 */

	@Override
	public void handleRead(ReadContext context) throws IOException {

		synchronized (this.connection.getLock()) {

			// Queue the data
			this.readData.add(context.getData());

			// Process data
			this.process();
		}
	}

	@Override
	public void handleHeartbeat(HeartBeatContext context) throws IOException {

		// Allow wrapped connection handler to handle heart beat
		this.wrappedConnectionHandler.handleHeartbeat(context);
	}

	/*
	 * =============== HeartBeatContext & ReadContext ======================
	 */

	@Override
	public long getTime() {
		return this.connectionHandlerContext.getTime();
	}

	/**
	 * {@link ReadContext} data.
	 */
	private byte[] readContextData = null;

	@Override
	public byte[] getData() {
		return this.readContextData;
	}

	/**
	 * Wraps the SSL task to be executed.
	 */
	private class SslTask implements Runnable {

		/**
		 * Actual SSL task to be run.
		 */
		private final Runnable task;

		/**
		 * Initiate.
		 * 
		 * @param task
		 *            SSL task to be run.
		 */
		public SslTask(Runnable task) {
			this.task = task;
		}

		/*
		 * =============== Runnable ===========================
		 */

		@Override
		public void run() {
			try {
				// Run task and ensure notify when complete
				this.task.run();

			} catch (Throwable ex) {
				// Flag failure in running task
				synchronized (SslConnectionHandler.this.connection.getLock()) {
					SslConnectionHandler.this.failure = new IOException(
							"SSL delegated task failed", ex);
				}

			} finally {
				// Flag task complete and trigger further processing
				synchronized (SslConnectionHandler.this.connection.getLock()) {
					SslConnectionHandler.this.task = null;
					SslConnectionHandler.this.process();
				}
			}
		}
	}

}