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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import net.officefloor.server.stream.BufferJvmFix;
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
	@Test
	public void pipeline() throws Exception {

		// Start the server
		this.startServer(false);

		// Single client pipeline
		this.doPipeline(20000);
	}

	/**
	 * Ensure can service pipeline requests by multiple clients.
	 */
	@Test
	public void multiClient() throws Exception {

		// Start the server
		this.startServer(false);

		// Metrics for tests
		int clientCount = 10;
		int requestCount = 20000;

		// Start the clients
		Future[] clients = new Future[clientCount];
		for (int i = 0; i < clients.length; i++) {
			clients[i] = this.thread("client" + i, () -> this.doPipeline(requestCount));
		}

		// Wait for clients to finish
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < clients.length; i++) {
			clients[i].waitForCompletion(startTime, 120);
		}
	}

	/**
	 * Ensure can service pipeline requests with {@link RequestServicer} executing
	 * on another {@link Thread}.
	 */
	@Test
	public void pipelineThreaded() throws Exception {

		// Start the server
		this.startServer(true);

		// Single client with threaded servicing
		this.doPipeline(10000);
	}

	/**
	 * Ensure can threaded service pipeline requests by multiple clients.
	 */
	@Test
	public void multiClientThreaded() throws Exception {

		// Start the server
		this.startServer(true);

		// Metrics for tests
		int clientCount = 10;
		int requestCount = 1000;

		// Start the clients
		Future[] clients = new Future[clientCount];
		for (int i = 0; i < clients.length; i++) {
			clients[i] = this.thread("client" + i, () -> this.doPipeline(requestCount));
		}

		// Wait for clients to finish
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < clients.length; i++) {
			clients[i].waitForCompletion(startTime, 120);
		}
	}

	private void startServer(boolean isThreaded) throws Exception {

		// Start the socket manager
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, new IntegerSocketServicer(),
				(socketServicer) -> (request, responseWriter) -> {
					FailableRunnable<IOException> runnable = () -> SocketManagerStressTest.this.writeInteger(request,
							responseWriter);
					if (isThreaded) {
						SocketManagerStressTest.this.thread("ThreadedServicer", runnable);
					} else {
						runnable.run();
					}
					return null;
				});

		this.tester.start();
	}

	private void doPipeline(int requestCount) throws Exception {

		// Send pipeline requests
		try (Socket socket = this.tester.getClient()) {

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
					assertEquals(i, readInteger(input), "Incorrect value");
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

	private void writeInteger(int value, ResponseWriter responseWriter) throws IOException {
		writeInteger(value, (b1, b2, b3, b4) -> {
			StreamBuffer<ByteBuffer> response = this.tester.createStreamBuffer(responseWriter, b3);
			response.next = this.tester.createStreamBuffer(responseWriter, b4);
			responseWriter.write((buffer, pool, overloadHandler) -> {
				StreamBuffer.write(new byte[] { b1, b2 }, buffer, pool, overloadHandler);
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
		public void service(StreamBuffer<ByteBuffer> readBuffer, long bytesRead, boolean isNewBuffer)
				throws IOException {

			// Setup for reading
			int position = BufferJvmFix.position(readBuffer.pooledBuffer);
			ByteBuffer data = readBuffer.pooledBuffer.duplicate();
			BufferJvmFix.flip(data);
			if (!isNewBuffer) {
				BufferJvmFix.position(data, this.previousPosition);
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
					assertEquals(this.expectedValue++, this.value, "Incorrect value");

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
