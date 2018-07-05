/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * Stress tests the {@link SocketManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketManagerStressTest extends AbstractSocketManagerTester {

	@Override
	protected int getBufferSize() {
		return 16;
	}

	@Override
	protected StreamBufferPool<ByteBuffer> createStreamBufferPool(int bufferSize) {
		return new ThreadLocalStreamBufferPool(() -> ByteBuffer.allocateDirect(bufferSize), 1000, 1000);
	}

	/**
	 * Ensure can service pipeline requests.
	 */
	public void testPipeline() throws Exception {

		// Start the server
		this.startServer(false);

		// Single client pipeline
		this.doPipeline(20000);
	}

	/**
	 * Ensure can service pipeline requests by multiple clients.
	 */
	public void testMultiClient() throws Exception {

		// Start the server
		this.startServer(false);

		// Metrics for tests
		int clientCount = 10;
		int requestCount = 20000;

		// Start the clients
		Future[] clients = new Future[clientCount];
		for (int i = 0; i < clients.length; i++) {
			clients[i] = this.thread(() -> this.doPipeline(requestCount));
		}

		// Wait for clients to finish
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < clients.length; i++) {
			clients[i].waitForCompletion(startTime, 30);
		}
	}

	/**
	 * Ensure can service pipeline requests with {@link RequestServicer}
	 * executing on another {@link Thread}.
	 */
	public void testPipelineThreaded() throws Exception {

		// Start the server
		this.startServer(true);

		// Single client with threaded servicing
		this.doPipeline(10000);
	}

	/**
	 * Ensure can threaded service pipeline requests by multiple clients.
	 */
	public void testMultiClientThreaded() throws Exception {

		// Start the server
		this.startServer(true);

		// Metrics for tests
		int clientCount = 10;
		int requestCount = 10000;

		// Start the clients
		Future[] clients = new Future[clientCount];
		for (int i = 0; i < clients.length; i++) {
			clients[i] = this.thread(() -> this.doPipeline(requestCount));
		}

		// Wait for clients to finish
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < clients.length; i++) {
			clients[i].waitForCompletion(startTime, 30);
		}
	}

	private void startServer(boolean isThreaded) throws Exception {

		// Start the socket manager
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, new IntegerSocketServicer(),
				(socketServicer) -> (request, responseWriter) -> {
					Runnable runnable = () -> SocketManagerStressTest.this.writeInteger(request, responseWriter);
					if (isThreaded) {
						SocketManagerStressTest.this.thread(() -> {
							runnable.run();
						});
					} else {
						runnable.run();
					}
				});

		this.tester.start();
	}

	private void doPipeline(int requestCount) throws Exception {

		// Send pipeline requests
		try (Socket socket = this.tester.getClient()) {
			socket.setSoTimeout(5 * 1000);

			// Send pipeline of requests
			OutputStream output = socket.getOutputStream();
			for (int i = 0; i < requestCount; i++) {
				writeInteger(i, output);
			}
			output.flush();

			// Ensure have correct responses
			InputStream input = socket.getInputStream();
			for (int i = 0; i < requestCount; i++) {
				try {
					assertEquals("Incorrect value", i, readInteger(input));
				} catch (SocketTimeoutException ex) {
					fail("Socket timeout for " + i + " : " + ex.getMessage());
				}
			}
		}
	}

	private static int readInteger(InputStream input) throws IOException {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			value <<= 8;
			value += (input.read() & 0xff);
		}
		return value;
	}

	private static interface IntegerWriter<T extends Throwable> {
		void write(byte b1, byte b2, byte b3, byte b4) throws T;
	}

	private static <T extends Throwable> void writeInteger(int value, IntegerWriter<T> writer) throws T {
		byte b1 = (byte) ((value & 0xff000000) >> 24);
		byte b2 = (byte) ((value & 0x00ff0000) >> 16);
		byte b3 = (byte) ((value & 0x0000ff00) >> 8);
		byte b4 = (byte) ((value & 0x000000ff) >> 0);
		writer.write(b1, b2, b3, b4);
	}

	private static void writeInteger(int value, OutputStream output) throws IOException {
		writeInteger(value, (b1, b2, b3, b4) -> {
			output.write(b1);
			output.write(b2);
			output.write(b3);
			output.write(b4);
		});
	}

	private void writeInteger(int value, ResponseWriter responseWriter) {
		writeInteger(value, (b1, b2, b3, b4) -> {
			StreamBuffer<ByteBuffer> response = this.tester.createStreamBuffer(b3);
			response.next = this.tester.createStreamBuffer(b4);
			responseWriter.write((buffer, pool) -> {
				StreamBuffer.write(new byte[] { b1, b2 }, buffer, pool);
			}, response);
		});
	}

	private static class IntegerSocketServicer implements SocketServicerFactory<Integer>, SocketServicer<Integer> {

		private final RequestHandler<Integer> requestHandler;

		private byte byteCount = 0;

		private int value = 0;

		private int previousPosition = 0;

		private int expectedValue = 0;

		private IntegerSocketServicer() {
			this.requestHandler = null;
		}

		private IntegerSocketServicer(RequestHandler<Integer> requestHandler) {
			this.requestHandler = requestHandler;
		}

		@Override
		public SocketServicer<Integer> createSocketServicer(RequestHandler<Integer> requestHandler) {
			return new IntegerSocketServicer(requestHandler);
		}

		@Override
		public void service(StreamBuffer<ByteBuffer> readBuffer, boolean isNewBuffer) {

			// Setup for reading
			int position = readBuffer.pooledBuffer.position();
			ByteBuffer data = readBuffer.pooledBuffer.duplicate();
			data.flip();
			if (!isNewBuffer) {
				data.position(this.previousPosition);
			}
			this.previousPosition = position;

			// Read the data
			while (data.remaining() > 0) {
				int readByte = data.get();
				this.value <<= 8;
				this.value += (readByte & 0xff);
				this.byteCount++;
				if (this.byteCount == 4) {

					// Ensure correct expected value
					assertEquals("Incorrect value", this.expectedValue++, this.value);

					// Have integer request
					this.requestHandler.handleRequest(this.value);

					// Reset for next
					this.byteCount = 0;
					this.value = 0;
				}
			}
		}
	}

}