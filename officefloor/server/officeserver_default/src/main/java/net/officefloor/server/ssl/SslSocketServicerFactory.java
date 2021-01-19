/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.ssl;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLSession;

import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.server.RequestHandler;
import net.officefloor.server.RequestServicer;
import net.officefloor.server.RequestServicerFactory;
import net.officefloor.server.ResponseHeaderWriter;
import net.officefloor.server.ResponseWriter;
import net.officefloor.server.SocketRunnable;
import net.officefloor.server.SocketServicer;
import net.officefloor.server.SocketServicerFactory;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;
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
	 * {@link Executor}.
	 */
	private final Executor executor;

	/**
	 * Instantiate.
	 * 
	 * @param sslContext                     {@link SSLContext}.
	 * @param delegateSocketServicerFactory  Delegate {@link SocketServicerFactory}.
	 * @param delegateRequestServicerFactory Delegate
	 *                                       {@link RequestServicerFactory}.
	 * @param executor                       {@link Executor}.
	 */
	public SslSocketServicerFactory(SSLContext sslContext, SocketServicerFactory<R> delegateSocketServicerFactory,
			RequestServicerFactory<R> delegateRequestServicerFactory, Executor executor) {
		this.sslContext = sslContext;
		this.delegateSocketServicerFactory = delegateSocketServicerFactory;
		this.delegateRequestServicerFactory = delegateRequestServicerFactory;
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
	 * SSL {@link SocketServicer}.
	 */
	private class SslSocketServicer implements SocketRunnable, SocketServicer<R>, RequestServicer<R> {

		/**
		 * {@link SSLEngine}.
		 */
		private final SSLEngine engine;

		/**
		 * {@link RequestHandler}.
		 */
		private final RequestHandler<R> requestHandler;

		/**
		 * {@link StreamBufferPool}.
		 */
		private final StreamBufferPool<ByteBuffer> bufferPool;

		/**
		 * Delegate {@link SocketServicer}.
		 */
		private final SocketServicer<R> delegateSocketServicer;

		/**
		 * Delegate {@link RequestServicer}.
		 */
		private final RequestServicer<R> delegateRequestServicer;

		/**
		 * {@link ByteBuffer} instances containing the read data from the {@link Socket}
		 * to be unwrapped to the application.
		 */
		private final Deque<ByteBuffer> socketToUnwrapBuffers = new LinkedList<>();

		/**
		 * Limit of the current {@link Socket} data to unwrap {@link ByteBuffer}.
		 */
		private int currentSocketToUnwrapLimit = 0;

		/**
		 * {@link StreamBuffer} instance containing the unwrap to application data.
		 */
		private StreamBuffer<ByteBuffer> currentUnwrapToAppBuffer = null;

		/**
		 * Head {@link StreamBuffer} to linked list of {@link StreamBuffer} instances to
		 * release on servicing the request.
		 */
		private StreamBuffer<ByteBuffer> previousRequestBuffers = null;

		/**
		 * {@link StreamBuffer} instance containing the application to wrap data.
		 */
		private StreamBuffer<ByteBuffer> currentAppToWrapBuffer = null;

		/**
		 * Active {@link SslRequest} instances in order.
		 */
		private final List<SslRequest> sslRequests = new LinkedList<>();

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
		 * @param engine                  {@link SSLEngine}.
		 * @param requestHandler          {@link RequestHandler}.
		 * @param delegateSocketServicer  Delegate {@link SocketServicer}.
		 * @param delegateRequestServicer Delegate {@link RequestServicer}.
		 */
		private SslSocketServicer(SSLEngine engine, RequestHandler<R> requestHandler,
				SocketServicer<R> delegateSocketServicer, RequestServicer<R> delegateRequestServicer) {
			this.engine = engine;
			this.requestHandler = requestHandler;
			this.delegateSocketServicer = delegateSocketServicer;
			this.delegateRequestServicer = delegateRequestServicer;

			// Obtain the buffer pool and server overload handler
			this.bufferPool = requestHandler.getStreamBufferPool();
		}

		/*
		 * ================= SocketServicer ====================
		 */

		@Override
		public synchronized void service(StreamBuffer<ByteBuffer> readBuffer, long bytesRead, boolean isNewBuffer) {

			// Include the read data
			ByteBuffer buffer = readBuffer.pooledBuffer.duplicate();
			BufferJvmFix.flip(buffer);
			if (!isNewBuffer) {
				// Same buffer, so add just the new data
				BufferJvmFix.position(buffer, this.currentSocketToUnwrapLimit);
			}

			// Set the limit after the newly read data
			this.currentSocketToUnwrapLimit = BufferJvmFix.limit(buffer);

			// Add to the data to be processed
			this.socketToUnwrapBuffers.add(buffer);

			// Process (with handshake data written immediately)
			this.process(null);
		}

		@Override
		public synchronized void release() {

			// Release buffers
			if (this.currentUnwrapToAppBuffer != null) {
				this.currentUnwrapToAppBuffer.release();
				this.currentUnwrapToAppBuffer = null;
			}

			// Release the SSL requests
			for (Iterator<SslRequest> iterator = this.sslRequests.iterator(); iterator.hasNext();) {
				SslRequest sslRequest = iterator.next();

				// Release the previous request buffers
				while (sslRequest.releaseRequestBuffers != null) {
					StreamBuffer<ByteBuffer> release = sslRequest.releaseRequestBuffers;
					sslRequest.releaseRequestBuffers = sslRequest.releaseRequestBuffers.next;
					release.release();
				}

				// Release the response buffers
				while (sslRequest.headResponseBuffer != null) {
					StreamBuffer<ByteBuffer> release = sslRequest.headResponseBuffer;
					sslRequest.headResponseBuffer = sslRequest.headResponseBuffer.next;
					release.release();
				}

				// Release the prepare buffers
				while (sslRequest.prepareHeadBuffer != null) {
					StreamBuffer<ByteBuffer> release = sslRequest.prepareHeadBuffer;
					sslRequest.prepareHeadBuffer = sslRequest.prepareHeadBuffer.next;
					release.release();
				}

				// Released so remove request
				iterator.remove();
			}
		}

		/*
		 * ================== RequestServicer ===================
		 */

		@Override
		public synchronized ProcessManager service(R request, ResponseWriter responseWriter) {

			// Create and register the SSL request
			final SslRequest sslRequest = new SslRequest(this.previousRequestBuffers, responseWriter);
			this.sslRequests.add(sslRequest);
			this.previousRequestBuffers = null; // included for release

			// Application level request, so delegate
			try {
				return this.delegateRequestServicer.service(request, new ResponseWriter() {

					@Override
					public StreamBufferPool<ByteBuffer> getStreamBufferPool() {
						return SslSocketServicer.this.bufferPool;
					}

					@Override
					public void execute(SocketRunnable runnable) {
						SslSocketServicer.this.requestHandler.execute(runnable);
					}

					@Override
					public void write(ResponseHeaderWriter responseHeaderWriter,
							StreamBuffer<ByteBuffer> headResponseBuffer) {

						// Easy access to servicer
						SslSocketServicer servicer = SslSocketServicer.this;

						// Process request on socket thread
						servicer.requestHandler.execute(() -> {

							// Process the response
							synchronized (servicer) {

								// Register the response for request
								sslRequest.responseHeaderWriter = responseHeaderWriter;
								sslRequest.headResponseBuffer = headResponseBuffer;

								// Process SSL responses in order
								Iterator<SslRequest> iterator = servicer.sslRequests.iterator();
								while (iterator.hasNext()) {
									SslRequest completeRequest = iterator.next();

									// Determine if request is complete
									if ((completeRequest.responseHeaderWriter == null)
											&& (completeRequest.headResponseBuffer == null)) {
										return; // request not complete
									}

									// Release the previous request buffers
									while (completeRequest.releaseRequestBuffers != null) {
										StreamBuffer<ByteBuffer> release = completeRequest.releaseRequestBuffers;
										completeRequest.releaseRequestBuffers = completeRequest.releaseRequestBuffers.next;
										release.release();
									}

									// Include header information
									if (completeRequest.responseHeaderWriter != null) {
										completeRequest.prepareHeadBuffer = servicer.bufferPool.getPooledStreamBuffer();
										completeRequest.responseHeaderWriter.write(completeRequest.prepareHeadBuffer,
												servicer.bufferPool);
									}

									// Append the response buffers
									if (completeRequest.prepareHeadBuffer == null) {
										// Only response buffers (no header)
										completeRequest.prepareHeadBuffer = completeRequest.headResponseBuffer;
									} else {
										// Append response buffers to header
										StreamBuffer<ByteBuffer> responseTail = completeRequest.prepareHeadBuffer;
										while (responseTail.next != null) {
											responseTail = responseTail.next;
										}
										responseTail.next = completeRequest.headResponseBuffer;
									}
									completeRequest.headResponseBuffer = null; // included in response

									// Prepare the response buffers for writing
									StreamBuffer<ByteBuffer> buffer = completeRequest.prepareHeadBuffer;
									while (buffer != null) {
										if (buffer.pooledBuffer != null) {
											BufferJvmFix.flip(buffer.pooledBuffer);
										}
										buffer = buffer.next;
									}

									// Include the response
									if (servicer.currentAppToWrapBuffer == null) {
										// Only response to wrap
										servicer.currentAppToWrapBuffer = completeRequest.prepareHeadBuffer;
									} else {
										// Add to existing responses
										StreamBuffer<ByteBuffer> responseTail = servicer.currentAppToWrapBuffer;
										while (responseTail.next != null) {
											responseTail = responseTail.next;
										}
										responseTail.next = completeRequest.prepareHeadBuffer;
									}

									// Remove the request, as included
									iterator.remove();

									// Write the response
									servicer.process(completeRequest.responseWriter);
								}
							}
						});
					}

					@Override
					public void closeConnection(Throwable failure) {
						SslSocketServicer.this.requestHandler.closeConnection(failure);
					}
				});

			} catch (Throwable ex) {
				// Failure in servicing, so close connection
				this.requestHandler.closeConnection(ex);
				return null; // closing connection, so no process management
			}
		}

		/*
		 * ================== SocketRunnable =======================
		 */

		@Override
		public synchronized void run() throws Throwable {
			this.process(null);
		}

		/**
		 * Processes the data.
		 * 
		 * @param responseWriter {@link ResponseWriter} to use in sending the response.
		 */
		private void process(ResponseWriter responseWriter) {

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
						this.sslRunnable = new SslRunnable(this.engine.getDelegatedTask(), this);
						SslSocketServicerFactory.this.executor.execute(this.sslRunnable);
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
						ByteBuffer readBuffer;
						if (this.socketToUnwrapBuffers.size() == 1) {
							// Just the one read buffer
							readBuffer = this.socketToUnwrapBuffers.removeFirst();
						} else {
							// Must combine all input buffers
							int appToWrapLength = 0;
							for (ByteBuffer buffer : this.socketToUnwrapBuffers) {
								appToWrapLength += buffer.remaining();
							}
							readBuffer = ByteBuffer.allocate(appToWrapLength);
							for (ByteBuffer buffer : this.socketToUnwrapBuffers) {
								readBuffer.put(buffer);
							}
							BufferJvmFix.flip(readBuffer);
							this.socketToUnwrapBuffers.clear();
						}

						// Obtain the unwrap to application buffer
						boolean isNewBuffer = false;
						ByteBuffer unwrapBuffer;
						if (this.currentUnwrapToAppBuffer == null) {
							// Must have buffer
							this.currentUnwrapToAppBuffer = this.bufferPool.getPooledStreamBuffer();
							unwrapBuffer = this.currentUnwrapToAppBuffer.pooledBuffer;
							isNewBuffer = true;

						} else {
							// Obtain the buffer (could be overflow unpooled)
							unwrapBuffer = (this.currentUnwrapToAppBuffer.pooledBuffer != null)
									? this.currentUnwrapToAppBuffer.pooledBuffer
									: this.currentUnwrapToAppBuffer.unpooledByteBuffer;

							// Determine if require new buffer
							if (unwrapBuffer.remaining() == 0) {
								// Require new buffer, so track for releasing
								if (this.previousRequestBuffers == null) {
									// First previous for request
									this.previousRequestBuffers = this.currentAppToWrapBuffer;
								} else {
									// Append previous for request
									StreamBuffer<ByteBuffer> head = this.previousRequestBuffers;
									while (head.next != null) {
										head = head.next;
									}
									head.next = this.currentAppToWrapBuffer;
								}

								// Create new buffer
								this.currentUnwrapToAppBuffer = this.bufferPool.getPooledStreamBuffer();
								unwrapBuffer = this.currentUnwrapToAppBuffer.pooledBuffer;
								isNewBuffer = true;
							}
						}

						// Unwrap the socket data for the application
						SSLEngineResult sslEngineResult = this.engine.unwrap(readBuffer, unwrapBuffer);

						// Determine if consumed all data
						if (readBuffer.remaining() != 0) {
							// Keep track of buffer (for next unwrap)
							this.socketToUnwrapBuffers.addLast(readBuffer);
						}

						// Handle underflow / overflow
						Status status = sslEngineResult.getStatus();
						switch (status) {
						case BUFFER_UNDERFLOW:
							// Need further data
							return;

						case BUFFER_OVERFLOW:
							// Not enough space for application data
							this.currentUnwrapToAppBuffer.release();
							this.currentUnwrapToAppBuffer = null;

							// Create buffer with enough space
							int applicationBufferSize = session.getApplicationBufferSize();
							byte[] applicationData = new byte[applicationBufferSize];
							this.currentUnwrapToAppBuffer = this.bufferPool
									.getUnpooledStreamBuffer(ByteBuffer.wrap(applicationData));
							unwrapBuffer = this.currentUnwrapToAppBuffer.unpooledByteBuffer;
							isNewBuffer = true;

							// Wrap the data
							sslEngineResult = this.engine.unwrap(readBuffer, unwrapBuffer);
							status = sslEngineResult.getStatus();
							if (status == Status.BUFFER_UNDERFLOW) {
								return; // need further data
							}

						default:
							// No underflow / overflow, so carry on
							break;
						}

						// Process based on status
						switch (status) {
						case OK:
							// Handle any unwrapped data
							if (BufferJvmFix.position(unwrapBuffer) > 0) {

								// Determine if unpooled (transform to pooled)
								StreamBuffer<ByteBuffer> serviceStreamBuffer = (this.currentUnwrapToAppBuffer.pooledBuffer != null)
										? this.currentUnwrapToAppBuffer
										: new ServiceUnpooledStreamBuffer(this.currentUnwrapToAppBuffer);

								// Service the request
								this.delegateSocketServicer.service(serviceStreamBuffer,
										serviceStreamBuffer.pooledBuffer.position(), isNewBuffer);

							} else {
								// Release the unused buffer
								this.currentUnwrapToAppBuffer.release();
								this.currentUnwrapToAppBuffer = null;
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
								this.requestHandler.closeConnection(null);
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

						// Determine if handshake response
						ByteBuffer appToWrapBuffer;
						if (this.currentAppToWrapBuffer == null) {

							// Use the socket read data (as handshaking)
							if (this.socketToUnwrapBuffers.size() == 1) {
								// Just the one buffer
								appToWrapBuffer = this.socketToUnwrapBuffers.removeFirst();
							} else {
								// Must combine all input buffers
								int appToWrapLength = 0;
								for (ByteBuffer buffer : this.socketToUnwrapBuffers) {
									appToWrapLength += buffer.remaining();
								}
								appToWrapBuffer = ByteBuffer.allocate(appToWrapLength);
								for (ByteBuffer buffer : this.socketToUnwrapBuffers) {
									appToWrapBuffer.put(buffer);
								}
								BufferJvmFix.flip(appToWrapBuffer);
								this.socketToUnwrapBuffers.clear();
							}

							// Specify for processing
							this.currentAppToWrapBuffer = this.bufferPool.getUnpooledStreamBuffer(appToWrapBuffer);
						}

						// Wrap all the data
						Status status = Status.OK;
						StreamBuffer<ByteBuffer> responseHead = null;
						StreamBuffer<ByteBuffer> responseTail = null;
						while (this.currentAppToWrapBuffer != null) {

							// Obtain the response stream buffer
							StreamBuffer<ByteBuffer> wrapToResponseBuffer = this.bufferPool.getPooledStreamBuffer();

							// Determine if file buffer
							SSLEngineResult sslEngineResult;
							StreamBuffer<ByteBuffer> fileContents;
							long fileBytesCount;
							if (this.currentAppToWrapBuffer.fileBuffer != null) {
								// Obtain the file content
								FileBuffer fileBuffer = this.currentAppToWrapBuffer.fileBuffer;

								// Obtain the app to wrap buffer
								fileContents = this.bufferPool.getPooledStreamBuffer();
								appToWrapBuffer = fileContents.pooledBuffer;

								// Obtain the position and count
								long position = fileBuffer.position + fileBuffer.bytesWritten;
								fileBytesCount = (fileBuffer.count < 0 ? fileBuffer.file.size() - fileBuffer.position
										: fileBuffer.count);
								long count = fileBytesCount - fileBuffer.bytesWritten;

								// Read bytes from file
								int bytesRead = fileBuffer.file.read(appToWrapBuffer, position);
								BufferJvmFix.flip(appToWrapBuffer);
								if (bytesRead > count) {
									// Truncate off additional read data
									BufferJvmFix.limit(appToWrapBuffer, (int) count);
								}

								// Wrap the written data
								sslEngineResult = this.engine.wrap(appToWrapBuffer, wrapToResponseBuffer.pooledBuffer);

							} else {
								// Obtain the Pooled / Unpooled application data
								fileContents = null;
								fileBytesCount = -1;
								appToWrapBuffer = (this.currentAppToWrapBuffer.pooledBuffer != null)
										? this.currentAppToWrapBuffer.pooledBuffer
										: this.currentAppToWrapBuffer.unpooledByteBuffer;

								// Wrap the written data
								sslEngineResult = this.engine.wrap(appToWrapBuffer, wrapToResponseBuffer.pooledBuffer);
							}

							// Handle underflow / overflow
							status = sslEngineResult.getStatus();
							switch (status) {
							case BUFFER_OVERFLOW:

								// Not enough space on buffer, so release it
								wrapToResponseBuffer.release();

								// Create buffer with enough space
								int packetBufferSize = session.getPacketBufferSize();
								wrapToResponseBuffer = this.bufferPool
										.getUnpooledStreamBuffer(ByteBuffer.allocate(packetBufferSize));

								// Wrap the data
								sslEngineResult = this.engine.wrap(appToWrapBuffer,
										wrapToResponseBuffer.unpooledByteBuffer);

								// Prepare for writing
								BufferJvmFix.flip(wrapToResponseBuffer.unpooledByteBuffer);

							default:
								// Carry on to process
								break;
							}

							// Determine if stream buffer written
							boolean isStreamBufferWritten;
							if (fileContents != null) {
								// Release the file contents (as written)
								fileContents.release();

								// Increment the number of bytes read from file
								int bytesConsumed = sslEngineResult.bytesConsumed();
								FileBuffer fileBuffer = this.currentAppToWrapBuffer.fileBuffer;
								fileBuffer.bytesWritten += bytesConsumed;
								isStreamBufferWritten = (fileBuffer.bytesWritten == fileBytesCount);

								// Callback once stream buffer written
								if ((isStreamBufferWritten) && (fileBuffer.callback != null)) {
									fileBuffer.callback.complete(fileBuffer.file, true);
								}

							} else {
								// Determine if pooled / unpooled written
								isStreamBufferWritten = (appToWrapBuffer.remaining() == 0);
							}

							// Handle wrap
							status = sslEngineResult.getStatus();
							switch (status) {
							case OK:
							case CLOSED:
								// Include buffer in response
								if (responseHead == null) {
									// First buffer
									responseHead = wrapToResponseBuffer;
									responseTail = wrapToResponseBuffer;
								} else {
									// Append
									responseTail.next = wrapToResponseBuffer;
									responseTail = wrapToResponseBuffer;
								}
								break;

							default:
								throw new IllegalStateException("Unknown wrap status " + status);
							}

							// Move to next buffer, only if written all
							if (isStreamBufferWritten) {

								// Move to next buffer to wrap
								StreamBuffer<ByteBuffer> release = this.currentAppToWrapBuffer;
								this.currentAppToWrapBuffer = this.currentAppToWrapBuffer.next;

								// Release application data buffer (after move)
								release.release();
							}
						}

						// Send the response
						if (responseWriter != null) {
							// Write data for the response
							responseWriter.write(null, responseHead);

						} else {
							// Send the handshake data immediately
							this.requestHandler.sendImmediateData(responseHead);
						}

						// Determine if in close handshake
						if (status == Status.CLOSED) {
							HandshakeStatus closeHandshakeStatus = this.engine.getHandshakeStatus();
							switch (closeHandshakeStatus) {
							case NEED_TASK:
							case NEED_UNWRAP:
							case NEED_WRAP:
								// Allow close handshake to proceed
								break;
							case NOT_HANDSHAKING:
								// Close complete, close connection
								this.requestHandler.closeConnection(null);
								return; // closed, no further processing
							default:
								throw new IllegalStateException("Unknown status " + status);
							}
							break;
						}
					}
				}

			} catch (Throwable ex) {
				// Record failure of processing to fail further interaction
				if (ex instanceof IOException) {
					this.failure = (IOException) ex;
				} else {
					this.failure = new IOException(ex);
				}

				// Log SSL failure
				if (LOGGER.isLoggable(Level.INFO)) {
					LOGGER.log(Level.INFO, "Failure in SSL connection", ex);
				}

				// Failure, so close connection
				this.requestHandler.closeConnection(ex);
			}
		}
	}

	/**
	 * SSL request.
	 */
	private static class SslRequest {

		/**
		 * {@link ResponseWriter} for the request.
		 */
		private final ResponseWriter responseWriter;

		/**
		 * Head {@link StreamBuffer} to the linked list of {@link StreamBuffer}
		 * instances containing request {@link StreamBuffer} instances to release on
		 * response. May be <code>null</code>.
		 */
		private StreamBuffer<ByteBuffer> releaseRequestBuffers;

		/**
		 * {@link ResponseHeaderWriter}.
		 */
		private ResponseHeaderWriter responseHeaderWriter = null;

		/**
		 * Head {@link StreamBuffer} to the linked list of {@link StreamBuffer}
		 * instances for the response.
		 */
		private StreamBuffer<ByteBuffer> headResponseBuffer = null;

		/**
		 * Head {@link StreamBuffer} to the linked list of {@link StreamBuffer}
		 * instances for preparing the complete response.
		 */
		private StreamBuffer<ByteBuffer> prepareHeadBuffer = null;

		/**
		 * Instantiate.
		 * 
		 * @param releaseRequestBuffers Head {@link StreamBuffer} to the linked list of
		 *                              {@link StreamBuffer} instances containing
		 *                              request {@link StreamBuffer} instances to
		 *                              release on response. May be <code>null</code>.
		 * @param reponseWriter         {@link ResponseWriter}.
		 */
		private SslRequest(StreamBuffer<ByteBuffer> releaseRequestBuffers, ResponseWriter reponseWriter) {
			this.responseWriter = reponseWriter;
			this.releaseRequestBuffers = releaseRequestBuffers;
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
		 * @param task              SSL task to be run.
		 * @param sslSocketServicer {@link SslSocketServicer}.
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

				// Finished processing SSL task
				synchronized (this.sslSocketServicer) {
					this.sslSocketServicer.sslRunnable = null;
				}

				// Continue processing request
				this.sslSocketServicer.requestHandler.execute(this.sslSocketServicer);
			}
		}
	}

	/**
	 * Need to adapt unpooled {@link StreamBuffer} to pooled {@link StreamBuffer} to
	 * service.
	 */
	private static class ServiceUnpooledStreamBuffer extends StreamBuffer<ByteBuffer> {

		/**
		 * Unpooled {@link StreamBuffer}.
		 */
		private final StreamBuffer<ByteBuffer> unpooledStreamBuffer;

		/**
		 * Instantiate.
		 * 
		 * @param unpooledStreamBuffer Unpooled {@link StreamBuffer}.
		 */
		public ServiceUnpooledStreamBuffer(StreamBuffer<ByteBuffer> unpooledStreamBuffer) {
			super(unpooledStreamBuffer.unpooledByteBuffer, null, null);
			this.unpooledStreamBuffer = unpooledStreamBuffer;
		}

		/*
		 * ===================== StreamBuffer ==============================
		 */

		@Override
		public boolean write(byte datum) {
			throw new IllegalStateException("Should only read from " + this.getClass().getSimpleName());
		}

		@Override
		public int write(byte[] data, int offset, int length) {
			throw new IllegalStateException("Should only read from " + this.getClass().getSimpleName());
		}

		@Override
		public void release() {
			this.unpooledStreamBuffer.release();
		}
	}

}
