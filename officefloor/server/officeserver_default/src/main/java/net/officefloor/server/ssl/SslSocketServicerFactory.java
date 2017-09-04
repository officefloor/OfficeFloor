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
package net.officefloor.server.ssl;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLSession;

import net.officefloor.server.RequestHandler;
import net.officefloor.server.RequestServicer;
import net.officefloor.server.RequestServicerFactory;
import net.officefloor.server.ResponseWriter;
import net.officefloor.server.SocketServicer;
import net.officefloor.server.SocketServicerFactory;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * SSL {@link SocketServicerFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslSocketServicerFactory<R> implements SocketServicerFactory<R>, RequestServicerFactory<R> {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(SslSocketServicerFactory.class.getName());

	/**
	 * {@link SSLContext}.
	 */
	private final SSLContext sslContext;

	/**
	 * Delegate {@link SocketServicerFactory}.
	 */
	private final SocketServicerFactory<R> delegateSocketServicerFactory;

	/**
	 * Delegate {@link RequestServicerFactory}.
	 */
	private final RequestServicerFactory<R> delegateRequestServicerFactory;

	/**
	 * {@link StreamBufferPool}.
	 */
	private final StreamBufferPool<ByteBuffer> bufferPool;

	/**
	 * {@link Executor}.
	 */
	private final Executor executor;

	/**
	 * Instantiate.
	 * 
	 * @param sslContext
	 *            {@link SSLContext}.
	 * @param delegateSocketServicerFactory
	 *            Delegate {@link SocketServicerFactory}.
	 * @param delegateRequestServicerFactory
	 *            Delegate {@link RequestServicerFactory}.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 * @param executor
	 *            {@link Executor}.
	 */
	public SslSocketServicerFactory(SSLContext sslContext, SocketServicerFactory<R> delegateSocketServicerFactory,
			RequestServicerFactory<R> delegateRequestServicerFactory, StreamBufferPool<ByteBuffer> bufferPool,
			Executor executor) {
		this.sslContext = sslContext;
		this.delegateSocketServicerFactory = delegateSocketServicerFactory;
		this.delegateRequestServicerFactory = delegateRequestServicerFactory;
		this.bufferPool = bufferPool;
		this.executor = executor;
	}

	/*
	 * ================ SocketServicerFactory ==========================
	 */

	@Override
	public SocketServicer<R> createSocketServicer(RequestHandler<R> requestHandler) {

		// Create the entity
		SSLEngine engine = this.sslContext.createSSLEngine();
		engine.setUseClientMode(false); // server mode

		// Create the delegate socket servicer
		SocketServicer<R> delegateSocketServicer = this.delegateSocketServicerFactory
				.createSocketServicer(requestHandler);

		// Create the delegate request servicer
		RequestServicer<R> delegateRequestServicer = this.delegateRequestServicerFactory
				.createRequestServicer(delegateSocketServicer);

		// Return the SSL socket servicer
		return new SslSocketServicer(engine, requestHandler, delegateSocketServicer, delegateRequestServicer);
	}

	/*
	 * =============== RequestServicerFactory ===========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public RequestServicer<R> createRequestServicer(SocketServicer<R> socketServicer) {
		return (RequestServicer<R>) socketServicer;
	}

	/**
	 * SSL request.
	 */
	private static class SslRequest {

		/**
		 * Head {@link StreamBuffer} to linked list of {@link StreamBuffer}
		 * instances to write response.
		 */
		private final StreamBuffer<ByteBuffer> headWriteBuffer;

		/**
		 * Instantiate.
		 * 
		 * @param headWriteBuffer
		 *            Head {@link StreamBuffer} to linked list of
		 *            {@link StreamBuffer} instances to write response.
		 */
		private SslRequest(StreamBuffer<ByteBuffer> headWriteBuffer) {
			this.headWriteBuffer = headWriteBuffer;
		}
	}

	/**
	 * SSL {@link SocketServicer}.
	 */
	private class SslSocketServicer implements SocketServicer<R>, RequestServicer<Object> {

		/**
		 * {@link SSLEngine}.
		 */
		private final SSLEngine engine;

		/**
		 * {@link RequestHandler}.
		 */
		private final RequestHandler<R> requestHandler;

		/**
		 * Delegate {@link SocketServicer}.
		 */
		private final SocketServicer<R> delegateSocketServicer;

		/**
		 * Delegate {@link RequestServicer}.
		 */
		private final RequestServicer<R> delegateRequestServicer;

		/**
		 * {@link StreamBuffer} instances containing the read data from the
		 * {@link Socket} to be unwrapped to the application.
		 */
		private final Deque<StreamBuffer<ByteBuffer>> socketToUnwrapBuffers = new LinkedList<>();

		/**
		 * {@link StreamBuffer} instance containing the unwrap to application
		 * data.
		 */
		private StreamBuffer<ByteBuffer> currentUnwrapToAppBuffer = null;

		/**
		 * {@link StreamBuffer} instance containing the application to wrap
		 * data.
		 */
		private StreamBuffer<ByteBuffer> currentAppToWrapBuffer = null;

		/**
		 * Current {@link SslRunnable} being run.
		 */
		private SslRunnable sslRunnable = null;

		/**
		 * Failure in attempting to process.
		 */
		private IOException failure = null;

		/**
		 * Instantiate.
		 * 
		 * @param engine
		 *            {@link SSLEngine}.
		 * @param requestHandler
		 *            {@link RequestHandler}.
		 * @param delegateSocketServicer
		 *            Delegate {@link SocketServicer}.
		 * @param delegateRequestServicer
		 *            Delegate {@link RequestServicer}.
		 */
		private SslSocketServicer(SSLEngine engine, RequestHandler<R> requestHandler,
				SocketServicer<R> delegateSocketServicer, RequestServicer<R> delegateRequestServicer) {
			this.engine = engine;
			this.requestHandler = requestHandler;
			this.delegateSocketServicer = delegateSocketServicer;
			this.delegateRequestServicer = delegateRequestServicer;
		}

		/*
		 * ================= SocketServicer ====================
		 */

		@Override
		public synchronized void service(StreamBuffer<ByteBuffer> readBuffer) {

			// Capture the read buffer
			int previousBufferCount = this.socketToUnwrapBuffers.size();
			StreamBuffer<ByteBuffer> previousBuffer = (previousBufferCount == 0 ? null
					: this.socketToUnwrapBuffers.getLast());
			if (previousBuffer != readBuffer) {
				this.socketToUnwrapBuffers.add(readBuffer);
			}

			// Process
			this.process();
		}

		/*
		 * ================== RequestServicer ===================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void service(Object request, ResponseWriter responseWriter) {

			// Determine if SSL request
			if (request instanceof SslRequest) {
				SslRequest sslRequest = (SslRequest) request;
				responseWriter.write(null, sslRequest.headWriteBuffer);
				return; // written SSL response
			}

			// Application level request, so delegate
			this.delegateRequestServicer.service((R) request, responseWriter);
		}

		/**
		 * Processes the data.
		 */
		private void process() {

			try {

				// Do not process if failure
				if (this.failure != null) {
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
						if (this.socketToUnwrapBuffers.size() == 0) {
							return; // must have data available to unwrap

						} else {
							// Unwrap the input data
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
						SslSocketServicerFactory.this.executor.execute(new SslRunnable(delegatedTask, this));
						return; // Must wait on task to complete

					case NOT_HANDSHAKING:
						// Flag data to process
						isInputData = (this.socketToUnwrapBuffers.size() > 0);
						isOutputData = (this.currentAppToWrapBuffer != null);
						if ((!isInputData) && (!isOutputData)) {
							return; // no data to process
						}
						break;

					case FINISHED:
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

						// Obtain the read data
						StreamBuffer<ByteBuffer> readStream = this.socketToUnwrapBuffers.getFirst();
						ByteBuffer readBuffer = readStream.pooledBuffer.duplicate();
						readBuffer.flip(); // enable reading content

						// Obtain the unwrap to application buffer
						if (this.currentUnwrapToAppBuffer == null) {
							this.currentUnwrapToAppBuffer = SslSocketServicerFactory.this.bufferPool
									.getPooledStreamBuffer();
						}

						// Unwrap the socket data for the application
						SSLEngineResult sslEngineResult = this.engine.unwrap(readBuffer,
								this.currentUnwrapToAppBuffer.pooledBuffer);

						// Determine if further data to consume on another pass
						handshakeStatus = this.engine.getHandshakeStatus();
						if ((readBuffer.remaining() == 0) && (handshakeStatus == HandshakeStatus.NOT_HANDSHAKING)) {
							// Consumed the read buffer
							this.socketToUnwrapBuffers.remove(0);
						}

						// Process based on status
						Status status = sslEngineResult.getStatus();
						switch (status) {
						case BUFFER_UNDERFLOW:
							// Not enough data, so group all read socket data
							int packetBufferSize = session.getPacketBufferSize();

							// TODO implement
							throw new UnsupportedOperationException(
									"TODO implement combining socket read data for underflow (" + readBuffer.remaining()
											+ " - " + packetBufferSize + ")");

						case BUFFER_OVERFLOW:
							// Not enough space for application data
							int applicationBufferSize = session.getApplicationBufferSize();

							// TODO implement
							throw new UnsupportedOperationException(
									"TODO implement increasing application unwrap buffer size ("
											+ this.currentUnwrapToAppBuffer.pooledBuffer.remaining() + " - "
											+ applicationBufferSize + ")");

						case OK:
							// Handle any unwrapped data
							if (this.currentUnwrapToAppBuffer.pooledBuffer.position() > 0) {
								this.delegateSocketServicer.service(this.currentUnwrapToAppBuffer);
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
								this.requestHandler.closeConnection();
								return; // closed, no further interaction
							default:
								throw new IllegalStateException("Unknown status " + status);
							}
							break;
						default:
							throw new IllegalStateException("Unknown unwrap status " + status);
						}
					}

					if (isOutputData) {
						// Handle outputting data

						// Obtain the data to wrap
						if (this.currentAppToWrapBuffer == null) {

							// Determine if input data
							if (this.socketToUnwrapBuffers.size() > 0) {
								this.currentAppToWrapBuffer = this.socketToUnwrapBuffers.getFirst();

							} else {
								// No data to wrap
								return;
							}
						}

						// Obtain the response stream buffer
						StreamBuffer<ByteBuffer> wrapToResponseBuffer = SslSocketServicerFactory.this.bufferPool
								.getPooledStreamBuffer();

						// Wrap the written data
						ByteBuffer appToWrapBuffer = this.currentAppToWrapBuffer.pooledBuffer;
						appToWrapBuffer.flip();
						SSLEngineResult sslEngineResult = this.engine.wrap(appToWrapBuffer,
								wrapToResponseBuffer.pooledBuffer);

						// Handle underflow / overflow
						Status status = sslEngineResult.getStatus();
						switch (status) {
						case BUFFER_OVERFLOW:

							// Not enough space on buffer, so release it
							wrapToResponseBuffer.release();

							// Create buffer with enough space
							int packetBufferSize = session.getPacketBufferSize();
							byte[] packetData = new byte[packetBufferSize];
							wrapToResponseBuffer = SslSocketServicerFactory.this.bufferPool
									.getUnpooledStreamBuffer(ByteBuffer.wrap(packetData));

							// Wrap the data
							sslEngineResult = this.engine.wrap(appToWrapBuffer,
									wrapToResponseBuffer.unpooledByteBuffer);
							
							// Prepare for writing
							wrapToResponseBuffer.unpooledByteBuffer.flip();

						default:
							// Carry on to process
							break;
						}

						// Handle wrap
						status = sslEngineResult.getStatus();
						switch (status) {
						case OK:
							// Send the response
							this.requestHandler.sendResponse(wrapToResponseBuffer);
							break;

						case CLOSED:
							// Determine if in close handshake
							HandshakeStatus closeHandshakeStatus = this.engine.getHandshakeStatus();
							switch (closeHandshakeStatus) {
							case NEED_TASK:
							case NEED_UNWRAP:
							case NEED_WRAP:
								// Allow close handshake to proceed
								break;
							case NOT_HANDSHAKING:
								// Close handshake complete, close
								// connection
								// this.connection.close();
								return; // closed, no further processing
							default:
								throw new IllegalStateException("Unknown status " + status);
							}
							break;

						default:
							throw new IllegalStateException("Unknown wrap status " + status);
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
				this.requestHandler.closeConnection();
			}
		}
	}

	/**
	 * Wraps the SSL {@link Runnable} to be executed.
	 */
	private static class SslRunnable implements Runnable {

		/**
		 * Actual SSL task to be run.
		 */
		private final Runnable task;

		/**
		 * {@link SslSocketServicer}.
		 */
		private final SslSocketServicerFactory<?>.SslSocketServicer sslSocketServicer;

		/**
		 * Initiate.
		 * 
		 * @param task
		 *            SSL task to be run.
		 * @param sslSocketServicer
		 *            {@link SslSocketServicer}.
		 */
		private SslRunnable(Runnable task, SslSocketServicerFactory<?>.SslSocketServicer sslSocketServicer) {
			this.task = task;
			this.sslSocketServicer = sslSocketServicer;
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
				synchronized (this.sslSocketServicer) {
					this.sslSocketServicer.failure = new IOException("SSL delegated runnable failed", ex);
				}

			} finally {
				// Flag task complete and trigger further processing
				synchronized (this.sslSocketServicer) {
					this.sslSocketServicer.sslRunnable = null;
					this.sslSocketServicer.process();
				}
			}
		}
	}

}