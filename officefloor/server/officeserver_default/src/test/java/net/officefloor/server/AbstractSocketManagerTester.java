/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Tests the {@link SocketListener}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSocketManagerTester extends OfficeFrameTestCase {

	/**
	 * Buffer size.
	 */
	private static final int BUFFER_SIZE = 1024;

	/**
	 * Wraps the {@link SocketManager} for easier testing.
	 */
	protected SocketManagerTester tester = null;

	/**
	 * Creates the {@link StreamBufferPool}.
	 * 
	 * @param bufferSize
	 *            Size of the pooled {@link StreamBuffer}.
	 * @return {@link StreamBufferPool}.
	 */
	protected abstract StreamBufferPool<ByteBuffer> createStreamBufferPool(int bufferSize);

	/**
	 * Creates a client {@link Socket}.
	 * 
	 * @param port
	 *            Port of the {@link ServerSocket}.
	 * @return Client {@link Socket}.
	 */
	protected Socket createClient(int port) throws IOException {
		return new Socket(InetAddress.getLocalHost(), 7878);
	}

	/**
	 * Adapts the {@link SocketServicerFactory}.
	 * 
	 * @param socketServicerFactory
	 *            {@link SocketServicerFactory}.
	 * @param requestServicerFactory
	 *            {@link RequestServicerFactory}.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 * @return Adapted {@link SocketServicerFactory}.
	 */
	protected <R> SocketServicerFactory<R> adaptSocketServicerFactory(SocketServicerFactory<R> socketServicerFactory,
			RequestServicerFactory<R> requestServicerFactory, StreamBufferPool<ByteBuffer> bufferPool) {
		return socketServicerFactory;
	}

	/**
	 * Adapts the {@link RequestServicerFactory}.
	 * 
	 * @param requestServicerFactory
	 *            {@link RequestServicerFactory}.
	 * @return Adapted {@link RequestServicerFactory}.
	 */
	protected <R> RequestServicerFactory<R> adaptRequestServicerFactory(
			RequestServicerFactory<R> requestServicerFactory) {
		return requestServicerFactory;
	}

	/**
	 * <p>
	 * Handles completion.
	 * <p>
	 * Allows overriding to handle completion (such as checking all
	 * {@link StreamBuffer} instances have been released).
	 * 
	 * @param bufferPool
	 *            {@link StreamBufferPool} used by the {@link SocketManager}.
	 */
	protected void handleCompletion(StreamBufferPool<ByteBuffer> bufferPool) {
		// By default do nothing
	}

	@Override
	protected void tearDown() throws Exception {

		// Ensure shut down tester
		if (this.tester != null) {
			this.tester.manager.shutdown();
			this.tester.waitForCompletion();
		}
	}

	/**
	 * Tester to wrap testing the {@link SocketManager}.
	 */
	protected class SocketManagerTester {

		/**
		 * Default port.
		 */
		private static final int DEFAULT_PORT = 7878;

		/**
		 * {@link StreamBufferPool}.
		 */
		protected final StreamBufferPool<ByteBuffer> bufferPool = AbstractSocketManagerTester.this
				.createStreamBufferPool(BUFFER_SIZE);

		/**
		 * {@link SocketManager} to test.
		 */
		private final SocketManager manager;

		/**
		 * {@link Thread} instances for the {@link SocketManager}.
		 */
		private final TestThread[] threads;

		/**
		 * Instantiate.
		 * 
		 * @param listenerCount
		 *            Number of {@link SocketListener} instances.
		 */
		protected SocketManagerTester(int listenerCount) throws IOException {

			// Create the Socket Manager
			this.manager = new SocketManager(1, this.bufferPool, BUFFER_SIZE);

			// Start servicing the sockets
			Runnable[] runnables = this.manager.getRunnables();
			this.threads = new TestThread[runnables.length];
			for (int i = 0; i < runnables.length; i++) {
				this.threads[i] = new TestThread(runnables[i]);
			}
		}

		/**
		 * Binds the {@link ServerSocket}.
		 * 
		 * @param port
		 *            Port for the {@link ServerSocket}.
		 * @param serverSocketDecorator
		 *            {@link ServerSocketDecorator}.
		 * @param acceptedSocketDecorator
		 *            {@link AcceptedSocketDecorator}.
		 * @param socketServicerFactory
		 *            {@link SocketServicerFactory}.
		 * @param requestServicerFactory
		 *            {@link RequestServicerFactory}.
		 */
		protected <R> void bindServerSocket(ServerSocketDecorator serverSocketDecorator,
				AcceptedSocketDecorator acceptedSocketDecorator, SocketServicerFactory<R> socketServicerFactory,
				RequestServicerFactory<R> requestServicerFactory) throws IOException {

			// Adapt the socket servicer factory
			SocketServicerFactory<R> adaptedSocketServiceFactory = AbstractSocketManagerTester.this
					.adaptSocketServicerFactory(socketServicerFactory, requestServicerFactory, this.bufferPool);

			// Adapt the request servicer factory
			RequestServicerFactory<R> adaptedRequestServicerFactory = AbstractSocketManagerTester.this
					.adaptRequestServicerFactory(requestServicerFactory);

			// Bind the server socket
			this.manager.bindServerSocket(DEFAULT_PORT, serverSocketDecorator, acceptedSocketDecorator,
					adaptedSocketServiceFactory, adaptedRequestServicerFactory);
		}

		/**
		 * Obtains the client {@link Socket}.
		 * 
		 * @return Client {@link Socket}.
		 */
		protected Socket getClient() throws IOException {
			return AbstractSocketManagerTester.this.createClient(DEFAULT_PORT);
		}

		/**
		 * Creates a {@link StreamBuffer} with bytes.
		 * 
		 * @param bytes
		 *            Bytes to write to the {@link StreamBuffer}.
		 * @return {@link StreamBuffer} for the bytes.
		 */
		protected StreamBuffer<ByteBuffer> createStreamBuffer(int... bytes) {
			StreamBuffer<ByteBuffer> streamBuffer = this.bufferPool.getPooledStreamBuffer();
			for (int i = 0; i < bytes.length; i++) {
				streamBuffer.write((byte) bytes[i]);
			}
			return streamBuffer;
		}

		/**
		 * Starts the {@link TestThread} instances.
		 */
		protected void start() {
			for (int i = 0; i < this.threads.length; i++) {
				this.threads[i].start();
			}
		}

		/**
		 * Triggers shutdown of the {@link SocketManager}.
		 */
		protected void shutdown() throws IOException {
			this.manager.shutdown();
		}

		/**
		 * Waits on completion of the {@link TestThread} instances.
		 */
		protected void waitForCompletion() {
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < this.threads.length; i++) {
				this.threads[i].waitForCompletion(startTime);
			}

			// As all complete, handle completion
			AbstractSocketManagerTester.this.handleCompletion(this.bufferPool);
		}
	}

	/**
	 * {@link Thread} for testing.
	 */
	protected class TestThread extends Thread {

		private final Runnable runnable;

		private Object completion = null;

		public TestThread(Runnable runnable) {
			this.runnable = runnable;
		}

		protected void waitForCompletion(long startTime) {
			this.waitForCompletion(startTime, 3);
		}

		protected synchronized void waitForCompletion(long startTime, int secondsToRun) {
			while (this.completion == null) {

				// Determine if timed out
				AbstractSocketManagerTester.this.timeout(startTime, secondsToRun);

				// Not timed out, so wait a little longer
				try {
					this.wait(10);
				} catch (InterruptedException ex) {
					throw AbstractSocketManagerTester.fail(ex);
				}
			}

			// Determine if failure
			if (this.completion instanceof Throwable) {
				throw fail((Throwable) this.completion);
			}
		}

		@Override
		public void run() {
			// Run
			try {
				this.runnable.run();
			} catch (Throwable ex) {
				synchronized (this) {
					this.completion = ex;
				}
			} finally {
				// Notify complete
				synchronized (this) {
					if (this.completion == null) {
						this.completion = "COMPLETE";
					}
					this.notify();
				}
			}
		}
	}

}