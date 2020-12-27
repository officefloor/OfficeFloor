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

package net.officefloor.server;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;
import net.officefloor.server.ssl.SslSocketServicerFactory;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Tests the {@link SocketListener}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public abstract class AbstractSocketManagerTester {

	/**
	 * {@link ThreadedTestSupport}.
	 */
	protected final ThreadedTestSupport threaded = new ThreadedTestSupport();

	/**
	 * Wraps the {@link SocketManager} for easier testing.
	 */
	protected SocketManagerTester tester = null;

	/**
	 * Indicates if secure.
	 */
	protected boolean isSecure = false;

	/**
	 * Obtains the buffer size.
	 * 
	 * @return Buffer size.
	 */
	protected abstract int getBufferSize();

	/**
	 * Creates the {@link StreamBufferPool}.
	 * 
	 * @param bufferSize Size of the pooled {@link StreamBuffer}.
	 * @return {@link StreamBufferPool}.
	 */
	protected abstract StreamBufferPool<ByteBuffer> createStreamBufferPool(int bufferSize);

	/**
	 * {@link SslSocketServicerFactory}.
	 */
	private SslSocketServicerFactory<?> sslSocketServicerFactory = null;

	/**
	 * {@link TestThread} instances used for testing.
	 */
	private Deque<TestThread> testThreads = new ConcurrentLinkedDeque<>();

	/**
	 * Name of test.
	 */
	private String testName;

	/**
	 * Creates a client {@link Socket}.
	 * 
	 * @param port Port of the {@link ServerSocket}.
	 * @return Client {@link Socket}.
	 */
	protected Socket createClient(int port) throws IOException {
		if (this.isSecure) {
			// Secure client
			try {
				return OfficeFloorDefaultSslContextSource.createClientSslContext(null).getSocketFactory()
						.createSocket(InetAddress.getLocalHost(), port);
			} catch (Exception ex) {
				return fail(ex);
			}
		} else {
			// Non-secure client
			return new Socket(InetAddress.getLocalHost(), 7878);
		}
	}

	/**
	 * Adapts the {@link SocketServicerFactory}.
	 * 
	 * @param socketServicerFactory  {@link SocketServicerFactory}.
	 * @param requestServicerFactory {@link RequestServicerFactory}.
	 * @param bufferPool             {@link StreamBufferPool}.
	 * @return Adapted {@link SocketServicerFactory}.
	 */
	protected <R> SocketServicerFactory<R> adaptSocketServicerFactory(SocketServicerFactory<R> socketServicerFactory,
			RequestServicerFactory<R> requestServicerFactory) {

		// Use as is, if not secure
		if (!this.isSecure) {
			return socketServicerFactory;
		}

		// Secure, so adapt
		try {
			// Obtain the SSL context
			SSLContext sslContext = OfficeFloorDefaultSslContextSource.createServerSslContext(null);

			// Create the executor
			Executor executor = (task) -> new TestThread("SslSocketServicer", () -> task.run()).start();

			// Create the SSL socket servicer
			SslSocketServicerFactory<R> sslSocketServicerFactory = new SslSocketServicerFactory<>(sslContext,
					socketServicerFactory, requestServicerFactory, executor);

			// Capture for adapting the request servicer factory
			this.sslSocketServicerFactory = sslSocketServicerFactory;

			// Return the SSL socket servicer
			return sslSocketServicerFactory;

		} catch (Exception ex) {
			return fail(ex);
		}
	}

	/**
	 * Adapts the {@link RequestServicerFactory}.
	 * 
	 * @param requestServicerFactory {@link RequestServicerFactory}.
	 * @return Adapted {@link RequestServicerFactory}.
	 */
	@SuppressWarnings("unchecked")
	protected <R> RequestServicerFactory<R> adaptRequestServicerFactory(
			RequestServicerFactory<R> requestServicerFactory) {

		// Use as is, if not secure
		if (!this.isSecure) {
			return requestServicerFactory;
		}

		// Secure, so adapt
		return (RequestServicerFactory<R>) this.sslSocketServicerFactory;
	}

	/**
	 * <p>
	 * Handles completion.
	 * <p>
	 * Allows overriding to handle completion (such as checking all
	 * {@link StreamBuffer} instances have been released).
	 * 
	 * @param bufferPool {@link StreamBufferPool} used by the {@link SocketManager}.
	 */
	protected void handleCompletion(StreamBufferPool<ByteBuffer> bufferPool) {
		// By default do nothing
	}

	@BeforeEach
	public void setup(TestInfo info) {
		this.testName = info.getDisplayName();
	}

	@AfterEach
	public void tearDown() throws Exception {

		// Ensure shut down tester
		if (this.tester != null) {
			this.tester.manager.shutdown();
			this.tester.waitForCompletion();
		}

		// Ensure all test threads complete without issue
		for (TestThread testThread : this.testThreads) {
			testThread.waitForCompletion(System.currentTimeMillis());
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
		 * {@link SocketManager} to test.
		 */
		private final SocketManager manager;

		/**
		 * {@link StreamBufferPool}.
		 */
		private final StreamBufferPool<ByteBuffer> bufferPool;

		/**
		 * {@link Thread} instances for the {@link SocketManager}.
		 */
		private final TestThread[] threads;

		/**
		 * Instantiate.
		 * 
		 * @param listenerCount Number of {@link SocketListener} instances.
		 */
		protected SocketManagerTester(int listenerCount) throws IOException {

			// Create the Socket Manager
			int bufferSize = AbstractSocketManagerTester.this.getBufferSize();
			this.bufferPool = AbstractSocketManagerTester.this
					.createStreamBufferPool(AbstractSocketManagerTester.this.getBufferSize());
			this.manager = new SocketManager(listenerCount, bufferSize * 4, 4, 10, this.bufferPool, bufferSize);

			// Start servicing the sockets
			Runnable[] runnables = this.manager.getRunnables();
			this.threads = new TestThread[runnables.length];
			for (int i = 0; i < runnables.length; i++) {
				final int index = i;
				this.threads[i] = new TestThread("SocketServicer" + index, () -> runnables[index].run());
			}
		}

		/**
		 * Binds the {@link ServerSocket}.
		 * 
		 * @param port                    Port for the {@link ServerSocket}.
		 * @param serverSocketDecorator   {@link ServerSocketDecorator}.
		 * @param acceptedSocketDecorator {@link AcceptedSocketDecorator}.
		 * @param socketServicerFactory   {@link SocketServicerFactory}.
		 * @param requestServicerFactory  {@link RequestServicerFactory}.
		 * @return {@link ServerSocket}.
		 */
		protected <R> ServerSocket bindServerSocket(ServerSocketDecorator serverSocketDecorator,
				AcceptedSocketDecorator acceptedSocketDecorator, SocketServicerFactory<R> socketServicerFactory,
				RequestServicerFactory<R> requestServicerFactory) throws IOException {

			// Adapt the socket servicer factory
			SocketServicerFactory<R> adaptedSocketServiceFactory = AbstractSocketManagerTester.this
					.adaptSocketServicerFactory(socketServicerFactory, requestServicerFactory);

			// Adapt the request servicer factory
			RequestServicerFactory<R> adaptedRequestServicerFactory = AbstractSocketManagerTester.this
					.adaptRequestServicerFactory(requestServicerFactory);

			// Bind the server socket
			return this.manager.bindServerSocket(DEFAULT_PORT, serverSocketDecorator, acceptedSocketDecorator,
					adaptedSocketServiceFactory, adaptedRequestServicerFactory);
		}

		/**
		 * Obtains the client {@link Socket}.
		 * 
		 * @return Client {@link Socket}.
		 */
		protected Socket getClient() throws IOException {
			Socket socket = AbstractSocketManagerTester.this.createClient(DEFAULT_PORT);
			socket.setSoTimeout(HttpClientTestUtil.getClientTimeout());
			return socket;
		}

		/**
		 * Creates a {@link StreamBuffer} with bytes.
		 * 
		 * @param responseWriter {@link ResponseWriter}.
		 * @param bytes          Bytes to write to the {@link StreamBuffer}.
		 * @return {@link StreamBuffer} for the bytes.
		 */
		protected StreamBuffer<ByteBuffer> createStreamBuffer(ResponseWriter responseWriter, int... bytes) {
			return this.createStreamBuffer(responseWriter.getStreamBufferPool(), bytes);
		}

		/**
		 * Creates a {@link StreamBuffer} with bytes.
		 * 
		 * @param bufferPool {@link StreamBufferPool}.
		 * @param bytes      Bytes to write to the {@link StreamBuffer}.
		 * @return {@link StreamBuffer} for the bytes.
		 */
		protected StreamBuffer<ByteBuffer> createStreamBuffer(StreamBufferPool<ByteBuffer> bufferPool, int... bytes) {
			StreamBuffer<ByteBuffer> streamBuffer = bufferPool.getPooledStreamBuffer();
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
		protected void waitForCompletion() throws Exception {
			try {
				long startTime = System.currentTimeMillis();
				for (int i = 0; i < this.threads.length; i++) {
					this.threads[i].waitForCompletion(startTime);
				}
			} finally {
				// As all complete, handle completion
				AbstractSocketManagerTester.this.handleCompletion(this.bufferPool);
			}
		}
	}

	/**
	 * {@link Runnable} that can throw an {@link Exception}.
	 */
	protected static interface FailableRunnable<T extends Throwable> {

		/**
		 * Executes logic.
		 * 
		 * @throws T Possible {@link Throwable}.
		 */
		void run() throws T;
	}

	/**
	 * Future on {@link Thread} completion.
	 */
	protected static interface Future {

		/**
		 * Waits for completion of the {@link Thread}.
		 * 
		 * @param startTime    Start time.
		 * @param secondsToRun Seconds to wait.
		 * @throws Exception If failure in {@link Thread} logic.
		 */
		void waitForCompletion(long startTime, int secondsToRun) throws Exception;

		/**
		 * Waits for completion of the {@link Thread}.
		 * 
		 * @param startTime Start time.
		 * @throws Exception If failure in {@link Thread} logic.
		 */
		void waitForCompletion(long startTime) throws Exception;
	}

	/**
	 * Delays running.
	 * 
	 * @return {@link Future}.
	 * @param runnable {@link FailableRunnable}.
	 */
	protected <T extends Throwable> Future delay(FailableRunnable<T> runnable) {
		return this.thread("delay", () -> {

			// Sleep some time to mimic delay
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
			}

			// Run delayed
			runnable.run();
		});
	}

	/**
	 * Runs in another {@link Thread}.
	 * 
	 * @param threadName Name to identify the {@link Thread}.
	 * @return {@link Future}.
	 * @param runnable {@link FailableRunnable}.
	 */
	protected <T extends Throwable> Future thread(String threadName, FailableRunnable<T> runnable) {
		TestThread thread = new TestThread(threadName, runnable);
		thread.start();
		return thread;
	}

	/**
	 * {@link Thread} for testing.
	 */
	private class TestThread extends Thread implements Future {

		private final FailableRunnable<?> runnable;

		private Object completion = null;

		private TestThread(String threadName, FailableRunnable<?> runnable) {
			super(threadName);
			this.runnable = runnable;

			// Register this test thread
			AbstractSocketManagerTester.this.testThreads.add(this);
		}

		@Override
		public void waitForCompletion(long startTime) throws Exception {
			this.waitForCompletion(startTime, 3);
		}

		@Override
		public synchronized void waitForCompletion(long startTime, int secondsToRun) throws Exception {
			while (this.completion == null) {

				// Determine if timed out
				AbstractSocketManagerTester.this.threaded.timeout(startTime, secondsToRun);

				// Not timed out, so wait a little longer
				this.wait(10);
			}

			// Determine if failure
			if (this.completion instanceof Throwable) {
				Throwable threadFailure = (Throwable) this.completion;

				// Detail the failure (for diagnosing)
				StringWriter buffer = new StringWriter();
				buffer.append("Test " + AbstractSocketManagerTester.this.testName + ": failure in "
						+ this.getClass().getSimpleName() + " " + this.getName() + ":\n");
				PrintWriter stackTraceWriter = new PrintWriter(buffer);
				threadFailure.printStackTrace(stackTraceWriter);
				stackTraceWriter.flush();
				System.err.println(buffer.toString());

				// Propagate the failure
				fail(threadFailure);
			}
		}

		@Override
		public void run() {
			try {
				// Run
				this.runnable.run();

				// Successful
				synchronized (this) {
					this.completion = "COMPLETE";
					this.notifyAll();
				}

			} catch (Throwable ex) {
				synchronized (this) {
					this.completion = ex;
					this.notifyAll();
				}
			}
		}
	}

}
