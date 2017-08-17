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
package net.officefloor.server.ssl.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLSession;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.ConnectionHandler;
import net.officefloor.server.http.protocol.CommunicationProtocol;
import net.officefloor.server.http.protocol.Connection;
import net.officefloor.server.http.protocol.ReadContext;
import net.officefloor.server.http.protocol.WriteBuffer;
import net.officefloor.server.http.protocol.WriteBufferEnum;
import net.officefloor.server.ssl.SslFunctionExecutor;

/**
 * SSL {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslConnectionHandler implements ConnectionHandler, SslFunctionExecutor, ReadContext {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(SslConnectionHandler.class.getName());

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link SSLEngine}.
	 */
	private final SSLEngine engine;

	/**
	 * Send buffer size.
	 */
	private final int sendBufferSize;

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
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * {@link Flow} index of the {@link ManagedObjectExecuteContext} to execute
	 * the {@link SslRunnable} instances.
	 */
	private final int sslRunnableFlowIndex;

	/**
	 * Indicates if closing.
	 */
	private boolean isClosing = false;

	/**
	 * Current {@link SslRunnable} being run.
	 */
	private SslRunnable sslRunnable = null;

	/**
	 * Failure in attempting to process.
	 */
	private IOException failure = null;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param engine
	 *            {@link SSLEngine}.
	 * @param taskExecutor
	 *            {@link SslFunctionExecutor}.
	 * @param sendBufferSize
	 *            Send buffer size.
	 * @param wrappedCommunicationProtocol
	 *            Wrapped {@link CommunicationProtocol}.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @param sslRunnableFlowIndex
	 *            {@link Flow} index of the {@link ManagedObjectExecuteContext}
	 *            to execute the {@link SslRunnable} instances.
	 */
	public SslConnectionHandler(Connection connection, SSLEngine engine, int sendBufferSize,
			CommunicationProtocol wrappedCommunicationProtocol, ManagedObjectExecuteContext<Indexed> executeContext,
			int sslRunnableFlowIndex) {
		this.engine = engine;
		this.sendBufferSize = sendBufferSize;
		this.connection = connection;
		this.executeContext = executeContext;
		this.sslRunnableFlowIndex = sslRunnableFlowIndex;

		// Create the connection handler to wrap
		Connection sslConnection = new SslConnectionImpl(this, this.connection);
		this.wrappedConnectionHandler = wrappedCommunicationProtocol.createConnectionHandler(sslConnection,
				executeContext);
	}

	/**
	 * Process the data.
	 */
	private void process() {

		try {

			// Do not process if failure or connection closed
			if ((this.failure != null) || (this.connection.isClosed())) {
				this.readData.clear();
				this.writeData.clear();
				return;
			}

			// Do not process if a task is being run
			if (this.sslRunnable != null) {
				return;
			}

			// Process until not able to process further
			for (;;) {

				// Process based on state of engine
				boolean isInputData = false;
				boolean isOutputData = false;
				HandshakeStatus handshakeStatus = this.engine.getHandshakeStatus();
				switch (handshakeStatus) {

				case NEED_UNWRAP:
					// Ensure have data to unwrap
					if (this.readData.size() == 0) {
						return; // must have data available to unwrap

					} else {
						// Input the data to unwrap
						isInputData = true;
					}
					break;

				case NEED_WRAP:
					// Must always output data if required
					isOutputData = true;
					break;

				case NEED_TASK:
					// Trigger processing of the delegated task
					Runnable delegatedTask = this.engine.getDelegatedTask();
					this.sslRunnable = new SslRunnable(delegatedTask);
					this.beginRunnable(this.sslRunnable);
					return; // Must wait on task to complete

				case NOT_HANDSHAKING:
					// Flag data to process
					isInputData = (this.readData.size() > 0);
					isOutputData = ((this.writeData.size() > 0) || this.isClosing);
					if ((!isInputData) && (!isOutputData)) {
						return; // no data to process
					}
					break;

				default:
					// Should only be in above states
					throw new IllegalStateException(
							"Illegal " + SSLEngine.class.getSimpleName() + " handshake state " + handshakeStatus);
				}

				// Obtain the SSL session
				SSLSession session = this.engine.getSession();

				// Handle actions
				if (isInputData) {
					// Handle inputting data

					// Obtain all the read data
					int availableData = 0;
					for (byte[] data : this.readData) {
						availableData += data.length;
					}
					byte[] dataToUnwrap = new byte[availableData];
					int readDataIndex = 0;
					for (byte[] data : this.readData) {
						System.arraycopy(data, 0, dataToUnwrap, readDataIndex, data.length);
						readDataIndex += data.length;
					}
					this.readData.clear();

					// Wrap the read data at packet buffer size
					int packetBufferSize = session.getPacketBufferSize();
					ByteBuffer dataToUnwrapBuffer = ByteBuffer.wrap(dataToUnwrap, 0,
							Math.min(dataToUnwrap.length, packetBufferSize));

					// Obtain temporary buffer to receive plain text
					int applicationBufferSize = session.getApplicationBufferSize();
					byte[] tempBytes = new byte[applicationBufferSize];
					ByteBuffer tempBuffer = ByteBuffer.wrap(tempBytes);

					// Unwrap the read data
					SSLEngineResult sslEngineResult = this.engine.unwrap(dataToUnwrapBuffer, tempBuffer);

					// Determine if further data to consume on another pass
					int bytesConsumed = sslEngineResult.bytesConsumed();
					if (bytesConsumed < availableData) {
						// Make data available for further reading
						byte[] furtherData = new byte[availableData - bytesConsumed];
						System.arraycopy(dataToUnwrap, bytesConsumed, furtherData, 0, furtherData.length);
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
						if (bytesProduced > 0) {
							this.readContextData = new byte[bytesProduced];
							System.arraycopy(tempBytes, 0, this.readContextData, 0, bytesProduced);
							this.wrappedConnectionHandler.handleRead(this);
						}
						break;
					case CLOSED:
						// Should be no application input data on close.
						// Determine if in close handshake.
						HandshakeStatus closeHandshakeStatus = this.engine.getHandshakeStatus();
						switch (closeHandshakeStatus) {
						case NEED_TASK:
						case NEED_UNWRAP:
						case NEED_WRAP:
							// Allow close handshake to proceed
							break;
						case NOT_HANDSHAKING:
							// Close handshake complete, close connection
							this.connection.close();
							return; // closed, no further interaction
						default:
							throw new IllegalStateException("Unknown status " + status);
						}
						break;
					default:
						throw new IllegalStateException("Unknown status " + status);
					}
				}

				if (isOutputData) {
					// Handle outputting data

					// Obtain temporary buffer to receive cipher text
					int packetBufferSize = session.getPacketBufferSize();
					byte[] tempBytes = new byte[packetBufferSize];
					ByteBuffer tempBuffer = ByteBuffer.wrap(tempBytes);

					// Obtain all the write data
					int availableData = 0;
					for (WriteBuffer buffer : this.writeData) {
						WriteBufferEnum type = buffer.getType();
						switch (type) {
						case BYTE_ARRAY:
							availableData += buffer.length();
							break;
						case BYTE_BUFFER:
							availableData += buffer.getDataBuffer().remaining();
							break;
						default:
							throw new IllegalStateException("Unknown type " + type);
						}
					}
					byte[] dataToWrap = new byte[availableData];
					int writeDataIndex = 0;
					for (WriteBuffer buffer : this.writeData) {
						WriteBufferEnum type = buffer.getType();
						switch (type) {
						case BYTE_ARRAY:
							byte[] data = buffer.getData();
							int length = buffer.length();
							System.arraycopy(data, 0, dataToWrap, writeDataIndex, length);
							writeDataIndex += length;
							break;
						case BYTE_BUFFER:
							ByteBuffer dataBuffer = buffer.getDataBuffer();
							length = dataBuffer.remaining();
							dataBuffer.get(dataToWrap, writeDataIndex, length);
							writeDataIndex += length;
							break;
						default:
							throw new IllegalStateException("Unknown type " + type);
						}
					}
					this.writeData.clear();

					// Wrap the read data at application buffer size
					int applicationBufferSize = session.getApplicationBufferSize();
					ByteBuffer dataToWrapBuffer = ByteBuffer.wrap(dataToWrap, 0,
							Math.min(dataToWrap.length, applicationBufferSize));

					// Wrap the written data
					SSLEngineResult sslEngineResult = this.engine.wrap(dataToWrapBuffer, tempBuffer);

					// Determine if further data to consume on another pass
					int bytesConsumed = sslEngineResult.bytesConsumed();
					if (bytesConsumed < availableData) {
						// Make data available for further writing
						byte[] furtherData = new byte[availableData - bytesConsumed];
						System.arraycopy(dataToWrap, bytesConsumed, furtherData, 0, furtherData.length);
						this.writeData.add(this.connection.createWriteBuffer(furtherData, furtherData.length));

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
						// Handle cipher data produced for connection
						int bytesProduced = sslEngineResult.bytesProduced();
						if (bytesProduced > 0) {

							// Write the cipher data to the connection
							byte[] cipherData = new byte[bytesProduced];
							System.arraycopy(tempBytes, 0, cipherData, 0, bytesProduced);

							// Ensure all buffers within send buffer size
							WriteBuffer[] writeBuffers = new WriteBuffer[(cipherData.length / this.sendBufferSize)
									+ ((cipherData.length % this.sendBufferSize) > 0 ? 1 : 0)];

							// Re-use cipher data for first buffer
							int totalBytesLoaded = Math.min(cipherData.length, this.sendBufferSize);
							writeBuffers[0] = this.connection.createWriteBuffer(cipherData, totalBytesLoaded);

							// Load remaining buffers (if any)
							int writeBufferIndex = 1;
							while (totalBytesLoaded < cipherData.length) {

								// Determine the bytes for next buffer
								int bytesToLoad = Math.min((cipherData.length - totalBytesLoaded), this.sendBufferSize);
								byte[] writeBufferData = Arrays.copyOfRange(cipherData, totalBytesLoaded,
										(totalBytesLoaded + bytesToLoad));
								writeBuffers[writeBufferIndex++] = this.connection.createWriteBuffer(writeBufferData,
										bytesToLoad);

								// Increment the number of bytes loaded
								totalBytesLoaded += bytesToLoad;
							}

							// Write the data
							this.connection.writeData(writeBuffers);
						}

						// Handle close
						if (status == Status.CLOSED) {

							// Determine if in close handshake
							HandshakeStatus closeHandshakeStatus = this.engine.getHandshakeStatus();
							switch (closeHandshakeStatus) {
							case NEED_TASK:
							case NEED_UNWRAP:
							case NEED_WRAP:
								// Allow close handshake to proceed
								break;
							case NOT_HANDSHAKING:
								// Close handshake complete, close connection
								this.connection.close();
								return; // closed, no further processing
							default:
								throw new IllegalStateException("Unknown status " + status);
							}
						}
						break;
					default:
						throw new IllegalStateException("Unknown status " + status);
					}
				}
			}

		} catch (IOException ex) {
			// Record failure of processing to fail further interaction
			this.failure = ex;

			// Log SSL failure
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.log(Level.INFO, "Failure in connection", ex);
			}

			// Failure, so close connection
			try {
				this.connection.close();
			} catch (IOException ignore) {
			}
		}
	}

	/**
	 * Written data to the SSL {@link Connection}.
	 * 
	 * @param data
	 *            {@link WriteBuffer} data.
	 */
	void writeData(WriteBuffer[] data) {

		synchronized (this.connection.getWriteLock()) {

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

		synchronized (this.connection.getWriteLock()) {

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

		synchronized (this.connection.getWriteLock()) {

			// Indicate if closing or closed
			return this.isClosing;
		}
	}

	/*
	 * ===================== SslFunctionExecutor =====================
	 */

	@Override
	public void beginRunnable(Runnable runnable) {
		// Invoke process to execute the runnable
		this.executeContext.invokeProcess(this.sslRunnableFlowIndex, runnable, null, 0, null);
	}

	/*
	 * ======================== ConnectionHandler ============================
	 */

	@Override
	public void handleRead(ReadContext context) throws IOException {

		synchronized (this.connection.getWriteLock()) {

			// Queue the data
			this.readData.add(context.getData());

			// Process data
			this.process();
		}
	}

	/*
	 * ========================== ReadContext ================================
	 */

	/**
	 * {@link ReadContext} data.
	 */
	private byte[] readContextData = null;

	@Override
	public byte[] getData() {
		return this.readContextData;
	}

	/**
	 * Wraps the SSL {@link Runnable} to be executed.
	 */
	private class SslRunnable implements Runnable {

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
		public SslRunnable(Runnable task) {
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
				synchronized (SslConnectionHandler.this.connection.getWriteLock()) {
					SslConnectionHandler.this.failure = new IOException("SSL delegated runnable failed", ex);
				}

			} finally {
				// Flag task complete and trigger further processing
				synchronized (SslConnectionHandler.this.connection.getWriteLock()) {
					SslConnectionHandler.this.sslRunnable = null;
					SslConnectionHandler.this.process();
				}
			}
		}
	}

}