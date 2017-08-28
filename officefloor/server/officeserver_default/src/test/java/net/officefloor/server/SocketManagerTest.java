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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockBufferPool;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link SocketListener}.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketManagerTest extends OfficeFrameTestCase {

	/**
	 * Wraps the {@link SocketManager} for easier testing.
	 */
	private SocketManagerTester tester = null;

	@Override
	protected void tearDown() throws Exception {

		// Ensure shut down tester
		if (this.tester != null) {
			this.tester.manager.shutdown();
			this.tester.waitForCompletion();
		}
	}

	/**
	 * Ensure can shutdown the {@link SocketListener}.
	 */
	public void testShutdown() throws IOException {
		SocketManagerTester tester = new SocketManagerTester(1);
		tester.start();
		tester.manager.shutdown();
		tester.waitForCompletion();
	}

	/**
	 * Ensure can shutdown multiple {@link SocketListener} instances.
	 */
	public void testShutdownMultipleListeners() throws IOException {
		SocketManagerTester tester = new SocketManagerTester(4);
		tester.start();
		tester.manager.shutdown();
		tester.waitForCompletion();
	}

	/**
	 * Ensure can accept a connection.
	 */
	public void testAcceptConnection() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ServerSocket[] serverSocket = new ServerSocket[] { null };
		Socket[] acceptedSocket = new Socket[] { null };
		this.tester.manager.bindServerSocket(7878, (socket) -> {
			serverSocket[0] = socket;
			return 1000;
		}, (socket) -> {
			synchronized (acceptedSocket) {
				acceptedSocket[0] = socket;
				acceptedSocket.notify();
			}
		}, (buffer, requestHandler) -> {
			fail("Should not be invoked, as only accepting connection");
		}, (request, responseWriter) -> {
			fail("Should not be invoked, as no requests");
		});
		assertNotNull("Should have bound server socket", serverSocket[0]);

		this.tester.start();

		// Undertake connect (will occur asynchronously)
		try (Socket client = new Socket(InetAddress.getLocalHost(), 7878)) {
			assertTrue("Should be connected", client.isConnected());

			// Ensure connects (must wait for connection)
			long startTime = System.currentTimeMillis();
			synchronized (acceptedSocket) {
				while (acceptedSocket[0] == null) {
					this.timeout(startTime);
					acceptedSocket.wait(10);
				}
			}
		}
	}

	/**
	 * Ensure can receive data on connection.
	 */
	public void testReceiveData() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		byte[] data = new byte[] { 0 };
		this.tester.manager.bindServerSocket(7878, null, null, (buffer, requestHandler) -> {
			synchronized (data) {
				data[0] = buffer.getPooledBuffer().get(0);
				data.notify();
			}
		}, (request, responseWriter) -> {
			fail("Should not be invoked, as no requests");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = new Socket(InetAddress.getLocalHost(), 7878)) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Confirm received the data
			long startTime = System.currentTimeMillis();
			synchronized (data) {
				while (data[0] == 0) {
					this.timeout(startTime);
					data.wait(10);
				}
			}
		}

		// Ensure received the data
		assertEquals("Should have received data", 1, data[0]);
	}

	/**
	 * Ensure can receive request on connection.
	 */
	public void testReceiveRequest() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		final Object REQUEST = "TEST";

		// Bind to server socket
		Object[] receivedRequest = new Object[] { null };
		this.tester.manager.bindServerSocket(7878, null, null, (buffer, requestHandler) -> {
			requestHandler.handleRequest(REQUEST);
		}, (request, responseWriter) -> {
			synchronized (receivedRequest) {
				receivedRequest[0] = request;
				receivedRequest.notify();
			}
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = new Socket(InetAddress.getLocalHost(), 7878)) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Confirm received the request
			long startTime = System.currentTimeMillis();
			synchronized (receivedRequest) {
				while (receivedRequest[0] == null) {
					this.timeout(startTime);
					receivedRequest.wait(10);
				}
			}
		}

		// Ensure received the request
		assertSame("Should have received request", REQUEST, receivedRequest[0]);
	}

	/**
	 * Ensure can send response.
	 */
	public void testSendResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.manager.bindServerSocket(7878, null, null, (buffer, requestHandler) -> {
			requestHandler.handleRequest("SEND");
		}, (request, responseWriter) -> {
			responseWriter.write(new ArrayList<>(Arrays.asList(this.tester.createStreamBuffer(1))));
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = new Socket(InetAddress.getLocalHost(), 7878)) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			long startTime = System.currentTimeMillis();
			while (inputStream.available() == 0) {
				this.timeout(startTime);
				Thread.yield(); // allow processing
			}

			// Ensure correct byte
			int value = inputStream.read();
			assertEquals("Incorrect response", 1, value);
		}
	}

	/**
	 * Ensure can pipeline response. Ensure responses are sent in the same order
	 * requests are received.
	 */
	public void testPipelineResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ResponseWriter[] writer = new ResponseWriter[] { null };
		this.tester.manager.bindServerSocket(7878, null, null, (buffer, requestHandler) -> {
			requestHandler.handleRequest((byte) 1);
			requestHandler.handleRequest((byte) 2);
		}, (request, responseWriter) -> {
			switch ((byte) request) {
			case 1:
				// First request, so capture writer for later
				writer[0] = responseWriter;
				break;

			case 2:
				// Second request, so write (out of order)
				responseWriter.write(new ArrayList<>(Arrays.asList(this.tester.createStreamBuffer(2))));

				// Now write the first request (out of order)
				writer[0].write(new ArrayList<>(Arrays.asList(this.tester.createStreamBuffer(1))));

			default:
				fail("Invalid request " + request);
			}
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = new Socket(InetAddress.getLocalHost(), 7878)) {

			OutputStream outputStream = client.getOutputStream();

			// Trigger the requests
			outputStream.write(1);
			outputStream.flush();

			// Ensure receive the response
			InputStream inputStream = client.getInputStream();
			long startTime = System.currentTimeMillis();
			while (inputStream.available() == 0) {
				this.timeout(startTime);
				Thread.yield(); // allow processing
			}

			// Ensure correct order of responses
			assertEquals("Incorrect first response", 1, inputStream.read());
			assertEquals("Incorrect second response", 2, inputStream.read());
		}
	}

	/**
	 * Tester to wrap testing the {@link SocketManager}.
	 */
	private class SocketManagerTester {

		/**
		 * {@link BufferPool}.
		 */
		private final MockBufferPool bufferPool = new MockBufferPool(() -> ByteBuffer.allocateDirect(10));

		/**
		 * {@link SocketManager} to test.
		 */
		private final SocketManager manager;

		/**
		 * {@link Thread} instances for the {@link SocketManager}.
		 */
		private final SocketThread[] threads;

		/**
		 * Instantiate.
		 * 
		 * @param listenerCount
		 *            Number of {@link SocketListener} instances.
		 */
		private SocketManagerTester(int listenerCount) throws IOException {
			this.manager = new SocketManager(1, this.bufferPool);
			Runnable[] runnables = this.manager.getRunnables();
			this.threads = new SocketThread[runnables.length];
			for (int i = 0; i < runnables.length; i++) {
				this.threads[i] = new SocketThread(runnables[i]);
			}
		}

		/**
		 * Creates a {@link StreamBuffer} with bytes.
		 * 
		 * @param bytes
		 *            Bytes to write to the {@link StreamBuffer}.
		 * @return {@link StreamBuffer} for the bytes.
		 */
		private StreamBuffer<ByteBuffer> createStreamBuffer(int... bytes) {
			StreamBuffer<ByteBuffer> streamBuffer = this.bufferPool.getPooledStreamBuffer();
			for (int i = 0; i < bytes.length; i++) {
				streamBuffer.write((byte) bytes[i]);
			}
			return streamBuffer;
		}

		/**
		 * Starts the {@link SocketThread} instances.
		 */
		private void start() {
			for (int i = 0; i < this.threads.length; i++) {
				this.threads[i].start();
			}
		}

		/**
		 * Waits on completion of the {@link SocketThread} instances.
		 */
		private void waitForCompletion() {
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < this.threads.length; i++) {
				this.threads[i].waitForCompletion(startTime);
			}
		}
	}

	/**
	 * {@link Socket} {@link Thread} for testing.
	 */
	private class SocketThread extends Thread {

		private final Runnable runnable;

		private Object completion = null;

		private SocketThread(Runnable runnable) {
			this.runnable = runnable;
		}

		private synchronized void waitForCompletion(long startTime) {
			while (this.completion == null) {

				// Determine if timed out
				SocketManagerTest.this.timeout(startTime);

				// Not timed out, so wait a little longer
				try {
					this.wait(10);
				} catch (InterruptedException ex) {
					throw SocketManagerTest.fail(ex);
				}
			}
		}

		@Override
		public void run() {
			// Run
			try {
				this.runnable.run();
			} catch (Throwable ex) {
				this.completion = ex;
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