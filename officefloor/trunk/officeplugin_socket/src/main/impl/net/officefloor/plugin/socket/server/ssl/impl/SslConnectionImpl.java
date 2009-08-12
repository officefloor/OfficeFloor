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
package net.officefloor.plugin.socket.server.ssl.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import net.officefloor.plugin.socket.server.ssl.SslConnection;
import net.officefloor.plugin.socket.server.ssl.SslTaskExecutor;
import net.officefloor.plugin.socket.server.ssl.TemporaryByteArrayFactory;
import net.officefloor.plugin.stream.BufferPopulator;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.GatheringBufferProcessor;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;
import net.officefloor.plugin.stream.synchronise.SynchronizedInputBufferStream;
import net.officefloor.plugin.stream.synchronise.SynchronizedOutputBufferStream;

/**
 * {@link SslConnection} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SslConnectionImpl implements SslConnection {

	/**
	 * Lock to coordinate access to this {@link SslConnection}.
	 */
	private final Object lock;

	/**
	 * Delegate {@link InputBufferStream} to read the cipher text from the peer.
	 */
	private final InputBufferStream inputDelegate;

	/**
	 * {@link BufferStream} containing the application data read from the peer.
	 */
	private final BufferStream inputBuffer;

	/**
	 * {@link SynchronizedInputBufferStream} provide access to the application
	 * to input plain text from the peer.
	 */
	private final SynchronizedInputBufferStream inputApplication;

	/**
	 * Delegate {@link OutputBufferStream} to write the cipher text to the peer.
	 */
	private final OutputBufferStream outputDelegate;

	/**
	 * {@link BufferStream} containing the application data to write to the
	 * peer.
	 */
	private final BufferStream outputBuffer;

	/**
	 * {@link SynchronizedOutputBufferStream} providing access to the
	 * application to output plain text to the peer.
	 */
	private final SynchronizedOutputBufferStream outputApplication;

	/**
	 * {@link SSLEngine} to wrap/unwrap between plain text and cipher text.
	 */
	private final SSLEngine engine;

	/**
	 * {@link TemporaryByteArrayFactory} to create necessary temporary
	 * <code>byte array</code> instances.
	 */
	private final TemporaryByteArrayFactory byteArrayFactory;

	/**
	 * {@link SslTaskExecutor} to execute any necessary {@link SslTask}
	 * instances.
	 */
	private final SslTaskExecutor taskExecutor;

	/**
	 * Temporary <code>byte array</code> to receive the results from the
	 * {@link SSLEngine}.
	 */
	private byte[] temporaryBytes = null;

	/**
	 * {@link SSLEngineResult} of last {@link SSLEngine} interaction.
	 */
	private SSLEngineResult sslEngineResult = null;

	/**
	 * {@link GatheringBufferProcessor} to handle inputting data.
	 */
	private final GatheringBufferProcessor inputProcessor = new GatheringBufferProcessor() {
		@Override
		public void process(ByteBuffer[] buffers) throws IOException {

			// Create the temporary buffer to write bytes
			ByteBuffer tempBuffer = ByteBuffer
					.wrap(SslConnectionImpl.this.temporaryBytes);

			// Attempt with first buffer (save copying if in first buffer)
			SslConnectionImpl.this.sslEngineResult = SslConnectionImpl.this.engine
					.unwrap(buffers[0], tempBuffer);
			Status status = SslConnectionImpl.this.sslEngineResult.getStatus();
			switch (status) {
			case BUFFER_UNDERFLOW:
				// Need more content so try with multiple buffers

				// Create temporary byte array for content of multiple buffers
				int packetBufferSize = SslConnectionImpl.this.engine
						.getSession().getPacketBufferSize();
				byte[] sourceBytes = SslConnectionImpl.this.byteArrayFactory
						.createSourceByteArray(packetBufferSize);

				// Ensure temporary byte array not used by another thread
				synchronized (sourceBytes) {

					// Fill the source bytes with contents of buffers
					int sourceIndex = 0;
					SOURCE_BYTES_READ: for (ByteBuffer buffer : buffers) {
						for (int i = buffer.position(); i < buffer.limit(); i++) {
							sourceBytes[sourceIndex++] = buffer.get(i);
							if (sourceIndex == sourceBytes.length) {
								// All bytes read
								break SOURCE_BYTES_READ;
							}
						}
					}

					// Create byte buffer with source bytes
					ByteBuffer sourceBuffer = ByteBuffer.wrap(sourceBytes, 0,
							sourceIndex);

					// Attempt to unwrap
					SslConnectionImpl.this.sslEngineResult = SslConnectionImpl.this.engine
							.unwrap(sourceBuffer, tempBuffer);
					status = SslConnectionImpl.this.sslEngineResult.getStatus();
					switch (status) {
					case OK:
						// Unwrapped content, so ensure processed from buffers
						int bytesConsumed = SslConnectionImpl.this.sslEngineResult
								.bytesConsumed();
						BYTES_CONSUMED: for (ByteBuffer buffer : buffers) {
							int bufferRemaining = buffer.remaining();
							if (bufferRemaining < bytesConsumed) {
								// Entire contents of buffer read
								buffer.position(buffer.limit());
								bytesConsumed -= bufferRemaining;
							} else {
								// Only partially read from buffer
								buffer.position(buffer.position()
										+ bytesConsumed);
								bytesConsumed = 0;

								// All bytes consumed
								break BYTES_CONSUMED;
							}
						}
						break;
					}
				}
				break;
			}
		}
	};

	/**
	 * {@link GatheringBufferProcessor} to handle outputting data.
	 */
	private final GatheringBufferProcessor outputProcessor = new GatheringBufferProcessor() {
		@Override
		public void process(ByteBuffer[] buffers) throws IOException {
			ByteBuffer tempBuffer = ByteBuffer
					.wrap(SslConnectionImpl.this.temporaryBytes);
			SslConnectionImpl.this.sslEngineResult = SslConnectionImpl.this.engine
					.wrap(buffers, tempBuffer);
		}
	};

	/**
	 * Current {@link SslTask} being run.
	 */
	private SslTask task = null;

	/**
	 * Failure in attempting to process.
	 */
	private IOException failure = null;

	/**
	 * Initiate.
	 *
	 * @param lock
	 *            Lock to coordinate access to this {@link SslConnection}.
	 * @param inputDelegate
	 *            Delegate {@link InputBufferStream} to read the cipher text
	 *            from the peer.
	 * @param outputDelegate
	 *            Delegate {@link OutputBufferStream} to write the cipher text
	 *            to the peer.
	 * @param engine
	 *            {@link SSLEngine} to wrap/unwrap between plain text and cipher
	 *            text.
	 * @param bufferSquirtFactory
	 *            {@link BufferSquirtFactory} for the {@link BufferStream}
	 *            instances containing the application data.
	 * @param byteArrayFactory
	 *            {@link TemporaryByteArrayFactory} to create necessary
	 *            temporary <code>byte array</code> instances.
	 * @param taskExecutor
	 *            {@link SslTaskExecutor} to execute any necessary
	 *            {@link SslTask} instances.
	 */
	public SslConnectionImpl(Object lock, InputBufferStream inputDelegate,
			OutputBufferStream outputDelegate, SSLEngine engine,
			BufferSquirtFactory bufferSquirtFactory,
			TemporaryByteArrayFactory byteArrayFactory,
			SslTaskExecutor taskExecutor) {
		this.lock = lock;
		this.inputDelegate = inputDelegate;
		this.outputDelegate = outputDelegate;
		this.engine = engine;
		this.byteArrayFactory = byteArrayFactory;
		this.taskExecutor = taskExecutor;

		// Create the buffer streams of application data
		this.inputBuffer = new BufferStreamImpl(bufferSquirtFactory);
		this.inputApplication = new SynchronizedInputBufferStream(
				this.inputBuffer.getInputBufferStream(), this.lock);
		this.outputBuffer = new BufferStreamImpl(bufferSquirtFactory);
		this.outputApplication = new SynchronizedOutputBufferStream(
				new SslOutputBufferStream(this.outputBuffer
						.getOutputBufferStream()), this.lock);
	}

	/**
	 * Throws the {@link IOException} if failure in processing.
	 *
	 * @throws IOException
	 *             {@link IOException} if failure in processing.
	 */
	private void ensureNoFailure() throws IOException {
		if (this.failure != null) {
			throw this.failure;
		}
	}

	/**
	 * Processes this {@link SslConnection}.
	 */
	private void process() {
		try {
			// Do not process if failure
			if (this.failure != null) {
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
					// Determine if data to input
					if (this.inputDelegate.available() > 0) {
						// Flag to input data
						isInputData = true;
					}

					// Determine if data to output
					if (this.outputBuffer.available() > 0) {
						// Flag to output data
						isOutputData = true;
					}

					break;

				case NEED_WRAP:
					// Must always output data if required
					isOutputData = true;
					break;

				case NEED_UNWRAP:
					// Ensure have data to unwrap
					if (this.inputDelegate.available() == 0) {
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
				if (isInputData) {
					// Handle inputting data

					// Obtain temporary buffer to receive plain text
					int applicationBufferSize = this.engine.getSession()
							.getApplicationBufferSize();
					byte[] tempBytes = this.byteArrayFactory
							.createDestinationByteArray(applicationBufferSize);

					// Ensure temporary byte array not used by another thread
					synchronized (tempBytes) {

						// Attempt to input data (and obtain results locally)
						this.temporaryBytes = tempBytes;
						this.inputDelegate.read(applicationBufferSize,
								this.inputProcessor);
						SSLEngineResult result = this.sslEngineResult;

						// Process based on status
						Status status = result.getStatus();
						switch (status) {
						case BUFFER_UNDERFLOW:
							// Return waiting for more data as require more
							return;
						case OK:
							// Transfer the plain text to input buffer
							this.inputBuffer.write(tempBytes, 0, result
									.bytesProduced());
							break;
						default:
							throw new IllegalStateException("Unknown status "
									+ status);
						}
					}

				} else if (isOutputData) {
					// Handle outputting data

					// Obtain temporary buffer to receive cipher text
					int packetBufferSize = this.engine.getSession()
							.getPacketBufferSize();
					byte[] tempBytes = this.byteArrayFactory
							.createDestinationByteArray(packetBufferSize);

					// Ensure temporary byte array not used by another thread
					synchronized (tempBytes) {

						// Attempt to output data (and obtain results locally)
						this.temporaryBytes = tempBytes;
						this.outputBuffer.read(packetBufferSize,
								this.outputProcessor);
						SSLEngineResult result = this.sslEngineResult;

						// Process based on status
						Status status = result.getStatus();
						switch (status) {
						case OK:
							// Transfer the cipher text to output delegate
							this.outputDelegate.write(tempBytes, 0, result
									.bytesProduced());
							break;
						default:
							throw new IllegalStateException("Unknown status "
									+ status);
						}
					}

				} else {
					// No input or output required so no further processing
					return;
				}
			}

		} catch (IOException ex) {
			// Record failure of processing to fail further interaction
			this.failure = ex;
		}
	}

	/*
	 * =================== SslConnection =========================
	 */

	@Override
	public void processDataFromPeer() throws IOException {
		synchronized (this.lock) {
			// Attempt to process the available data from peer
			this.process();

			// Propagate any failure in processing data
			this.ensureNoFailure();
		}
	}

	@Override
	public Object getLock() {
		return this.lock;
	}

	@Override
	public InputBufferStream getInputBufferStream() {
		return this.inputApplication;
	}

	@Override
	public OutputBufferStream getOutputBufferStream() {
		return this.outputApplication;
	}

	/**
	 * SSL {@link OutputBufferStream}.
	 */
	private class SslOutputBufferStream implements OutputBufferStream {

		/**
		 * Delegate {@link OutputBufferStream}.
		 */
		private final OutputBufferStream delegate;

		/**
		 * Initiate.
		 *
		 * @param delegate
		 *            Delegate {@link OutputBufferStream}.
		 */
		public SslOutputBufferStream(OutputBufferStream delegate) {
			this.delegate = delegate;
		}

		/*
		 * ================= OutputBufferStream ========================
		 */

		@Override
		public OutputStream getOutputStream() {
			return new SslOutputStream(this.delegate.getOutputStream());
		}

		@Override
		public void write(byte[] bytes) throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.write(bytes);
			SslConnectionImpl.this.process();
		}

		@Override
		public void write(byte[] data, int offset, int length)
				throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.write(data, offset, length);
			SslConnectionImpl.this.process();
		}

		@Override
		public void write(BufferPopulator populator) throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.write(populator);
			SslConnectionImpl.this.process();
		}

		@Override
		public void append(ByteBuffer buffer) throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.append(buffer);
			SslConnectionImpl.this.process();
		}

		@Override
		public void append(BufferSquirt squirt) throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.append(squirt);
			SslConnectionImpl.this.process();
		}

		@Override
		public void close() throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.close();
			SslConnectionImpl.this.process();
		}
	}

	/**
	 * SSL {@link OutputStream}.
	 */
	private class SslOutputStream extends OutputStream {

		/**
		 * Delegate {@link OutputStream}.
		 */
		private final OutputStream delegate;

		/**
		 * Initiate.
		 *
		 * @param delegate
		 *            Delegate {@link OutputStream}.
		 */
		public SslOutputStream(OutputStream delegate) {
			this.delegate = delegate;
		}

		/*
		 * ==================== OutputStream ==============================
		 */

		@Override
		public void write(int b) throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.write(b);
			SslConnectionImpl.this.process();
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.write(b, off, len);
			SslConnectionImpl.this.process();
		}

		@Override
		public void flush() throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.flush();
			SslConnectionImpl.this.process();
		}

		@Override
		public void close() throws IOException {
			SslConnectionImpl.this.ensureNoFailure();
			this.delegate.close();
			SslConnectionImpl.this.process();
		}
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
				synchronized (SslConnectionImpl.this.lock) {
					SslConnectionImpl.this.failure = new IOException(
							"SSL delegated task failed", ex);
				}

			} finally {
				// Flag task complete and trigger further processing
				synchronized (SslConnectionImpl.this.lock) {
					SslConnectionImpl.this.task = null;
					SslConnectionImpl.this.process();
				}
			}
		}
	}

}