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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.server.RequestHandler.Execution;
import net.officefloor.server.http.stream.TemporaryFiles;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Tests the {@link SocketListener}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSocketManagerTestCase extends AbstractSocketManagerTester {

	/**
	 * Ensure can shutdown the {@link SocketListener}.
	 */
	@Test
	public void shutdown() throws Exception {
		SocketManagerTester tester = new SocketManagerTester(1);
		tester.start();
		tester.shutdown();
		tester.waitForCompletion();
	}

	/**
	 * Ensure can shutdown multiple {@link SocketListener} instances.
	 */
	@Test
	public void shutdownMultipleListeners() throws Exception {
		SocketManagerTester tester = new SocketManagerTester(4);
		tester.start();
		tester.shutdown();
		tester.waitForCompletion();
	}

	/**
	 * Ensure can accept a connection.
	 */
	@Test
	public void acceptConnection() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		Closure<ServerSocket> serverSocket = new Closure<>();
		ThreadSafeClosure<Socket> acceptedSocket = new ThreadSafeClosure<>();
		this.tester.bindServerSocket((socket) -> {
			serverSocket.value = socket;
			return 1000;
		}, (socket) -> {
			acceptedSocket.set(socket);
		}, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			fail("Should not be invoked, as only accepting connection");
		}, (socketServicer) -> (request, responseWriter) -> {
			return fail("Should not be invoked, as no requests");
		});
		assertNotNull(serverSocket.value, "Should have bound server socket");

		this.tester.start();

		// Undertake connect (will occur asynchronously)
		try (Socket client = this.tester.getClient()) {
			assertTrue(client.isConnected(), "Should be connected");

			// Ensure connects (must wait for connection)
			acceptedSocket.waitAndGet();
		}
	}

	/**
	 * Ensure can receive data on connection.
	 */
	@Test
	public void receiveData() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ThreadSafeClosure<Byte> data = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				byte value = buffer.pooledBuffer.get(0);
				data.set(value);
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			return fail("Should not be invoked, as no requests");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure received the data
			assertEquals(1, (byte) data.waitAndGet(), "Should have received data");
		}
	}

	/**
	 * Ensure can receive request on connection.
	 */
	@Test
	public void receiveRequest() throws IOException {
		this.tester = new SocketManagerTester(1);

		final Object REQUEST = "TEST";

		// Bind to server socket
		ThreadSafeClosure<Object> receivedRequest = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest(REQUEST);
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			receivedRequest.set(request);
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure received the request
			assertSame(REQUEST, receivedRequest.waitAndGet(), "Should have received request");
		}
	}

	/**
	 * Ensure error if invoke request on another {@link Thread}.
	 */
	@Test
	public void issueIfReceiveRequestOnAnotherThread() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ThreadSafeClosure<Throwable> illegalState = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				this.delay(() -> {
					try {
						requestHandler.handleRequest("illegal");
						illegalState.set(null);
						fail("Should not be successful");
					} catch (IllegalStateException ex) {
						illegalState.set(ex);
					}
				});
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			return fail("Should not receive request");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure illegal state
			assertTrue(illegalState.waitAndGet() instanceof IllegalStateException,
					"Should be illegal to handle request on another thread");
		}
	}

	/**
	 * Ensure can delay receiving request on connection via {@link Execution}.
	 */
	@Test
	public void delayReceiveRequestViaExecution() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		final Object REQUEST = "TEST";

		// Bind to server socket
		ThreadSafeClosure<Object> receivedRequest = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				this.delay(() -> {
					requestHandler.execute(() -> requestHandler.handleRequest(REQUEST));
				});
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			receivedRequest.set(request);
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure received the request
			assertSame(REQUEST, receivedRequest.waitAndGet(), "Should have received request");
		}
	}

	/**
	 * Ensure can send response.
	 */
	@Test
	public void sendResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest("SEND");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write(null, this.tester.createStreamBuffer(responseWriter.getStreamBufferPool(), 2));
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(2, inputStream.read(), "Incorrect response");
		}
	}

	/**
	 * Ensure can handle requests on multiple connections.
	 */
	@Test
	public void multipleConnections() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest(buffer.pooledBuffer.get(0));
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write(null,
					this.tester.createStreamBuffer(responseWriter.getStreamBufferPool(), 10 + (byte) request));
			return null;
		});

		this.tester.start();

		// Undertake multiple connections
		int connectionCount = Runtime.getRuntime().availableProcessors() * 4;
		Socket[] clients = new Socket[connectionCount];
		for (int i = 0; i < connectionCount; i++) {
			clients[i] = this.tester.getClient();
		}
		for (int i = 0; i < connectionCount; i++) {
			OutputStream outputStream = clients[i].getOutputStream();
			outputStream.write(i + 1);
			outputStream.flush();
		}
		for (int i = 0; i < connectionCount; i++) {
			InputStream inputStream = clients[i].getInputStream();
			assertEquals(11 + i, inputStream.read(), "Incorrect response");
		}
		for (int i = 0; i < connectionCount; i++) {
			clients[i].close();
		}
	}

	/**
	 * Ensure can send header.
	 */
	@Test
	public void sendHeaderOnly() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest("SEND");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write((head, pool) -> head.write((byte) 2), null);
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(2, inputStream.read(), "Incorrect response");
		}
	}

	/**
	 * Ensure can send header and body.
	 */
	@Test
	public void sendHeaderAndBody() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest("SEND");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write((head, pool) -> head.write((byte) 2),
					this.tester.createStreamBuffer(responseWriter.getStreamBufferPool(), 3));
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(2, inputStream.read(), "Incorrect header value");
			assertEquals(3, inputStream.read(), "Incorrect body value");
		}
	}

	/**
	 * Ensure can send header and file.
	 */
	@Test
	public void sendHeaderAndFile() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Create file
		FileChannel file = TemporaryFiles.getDefault().createTempFile("testSendHeaderAndFile", new byte[] { 3 });

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest("SEND");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			try {
				StreamBuffer<ByteBuffer> fileBuffer = responseWriter.getStreamBufferPool().getFileStreamBuffer(file, 0,
						-1, null);
				responseWriter.write((head, pool) -> head.write((byte) 2), fileBuffer);
			} catch (IOException ex) {
				// Failed, so provide invalid response
				responseWriter.write((head, pool) -> head.write((byte) 5), null);
			}
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(2, inputStream.read(), "Incorrect header value");
			assertEquals(3, inputStream.read(), "Incorrect file value");

			// Ensure the file is still open
			assertTrue(file.isOpen(), "File should still be open");
		}
	}

	/**
	 * Ensure can send header and large file.
	 */
	@Test
	public void sendHeaderAndLargeFile() throws Exception {
		this.tester = new SocketManagerTester(1);

		// Transform index into byte value
		Function<Integer, Byte> transform = (value) -> (byte) (value % Byte.MAX_VALUE);

		// Create the larger file than buffer size
		int fileSize = this.getBufferSize() < 10 ? 5 : 2 * 1000 * 1000;
		byte[] data = new byte[fileSize];
		for (int i = 0; i < fileSize; i++) {
			data[i] = transform.apply(i);
		}
		FileChannel file = TemporaryFiles.getDefault().createTempFile("testSendHeaderAndLargeFile", data);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest("SEND");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			try {
				StreamBufferPool<ByteBuffer> bufferPool = responseWriter.getStreamBufferPool();
				StreamBuffer<ByteBuffer> fileBuffer = bufferPool.getFileStreamBuffer(file, 0, -1,
						(completedFile, isWritten) -> completedFile.close());
				fileBuffer.next = this.tester.createStreamBuffer(bufferPool, 1);
				responseWriter.write((head, pool) -> head.write((byte) 2), fileBuffer);
			} catch (IOException ex) {
				// Failed, so provide invalid response
				responseWriter.write((head, pool) -> head.write((byte) 5), null);
			}
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(2, inputStream.read(), "Incorrect header value");
			for (int i = 0; i < fileSize; i++) {
				byte expected = transform.apply(i);
				assertEquals(expected, inputStream.read(), "Incorrect file value for byte " + i);
			}
			assertEquals(1, inputStream.read(), "Incorrect final value");

			// File should be closed
			assertFalse(file.isOpen(), "File should be closed");
		}
	}

	/**
	 * Ensure can send header and file segment.
	 */
	@Test
	public void sendHeaderAndFileSegment() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Create file
		FileChannel file = TemporaryFiles.getDefault().createTempFile("testSendHeaderAndFileSegment",
				new byte[] { 1, 2, 3 });

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest("SEND");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			try {
				StreamBufferPool<ByteBuffer> bufferPool = responseWriter.getStreamBufferPool();
				StreamBuffer<ByteBuffer> fileBuffer = bufferPool.getFileStreamBuffer(file, 2, 1,
						(completedFile, isWritten) -> completedFile.close());
				fileBuffer.next = this.tester.createStreamBuffer(bufferPool, 4);
				responseWriter.write((head, pool) -> head.write((byte) 2), fileBuffer);
			} catch (IOException ex) {
				// Failed, so provide invalid response
				responseWriter.write((head, pool) -> head.write((byte) 5), null);
			}
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(2, inputStream.read(), "Incorrect header value");
			assertEquals(3, inputStream.read(), "Incorrect file value");
			assertEquals(4, inputStream.read(), "Incorrect further value");

			// File should be closed
			assertFalse(file.isOpen(), "File should be closed");
		}
	}

	/**
	 * Ensure can pipeline response. Ensure responses are sent in the same order
	 * requests are received.
	 */
	@Test
	public void inOrderPipelineResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest((byte) 1);
				requestHandler.handleRequest((byte) 2);
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			StreamBuffer<ByteBuffer> response = this.tester.createStreamBuffer(responseWriter.getStreamBufferPool(),
					(byte) request);
			responseWriter.write(null, response);
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Trigger the requests
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(1, inputStream.read(), "Incorrect first response");
			assertEquals(2, inputStream.read(), "Incorrect second response");
		}
	}

	/**
	 * Ensure can pipeline response. Ensure responses are sent in the same order
	 * requests are received.
	 */
	@Test
	public void outOfOrderPipelineResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ThreadSafeClosure<ResponseWriter> writer = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest((byte) 1);
				requestHandler.handleRequest((byte) 2);
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			switch ((byte) request) {
			case 1:
				// First request, so capture writer for later
				writer.set(responseWriter);
				break;

			case 2:
				StreamBufferPool<ByteBuffer> bufferPool = responseWriter.getStreamBufferPool();

				// Second request, so write (out of order)
				StreamBuffer<ByteBuffer> responseTwo = this.tester.createStreamBuffer(bufferPool, 2);
				responseWriter.write(null, responseTwo);

				// Now write the first request (out of order)
				StreamBuffer<ByteBuffer> responseOne = this.tester.createStreamBuffer(bufferPool, 1);
				writer.get().write(null, responseOne);
				break;

			default:
				fail("Invalid request " + request);
			}
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Trigger the requests
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(1, inputStream.read(), "Incorrect first response");
			assertEquals(2, inputStream.read(), "Incorrect second response");
		}
	}

	/**
	 * Ensure can select on write operations to complete writing the large response.
	 */
	@Test
	public void largeResponse() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		final Function<Integer, Byte> indexValue = (index) -> (byte) (index % Byte.MAX_VALUE);

		// Bind to server socket
		final int responseSize = this.getBufferSize() * 5;
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest((byte) 1);
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			// Create very large response
			StreamBuffer<ByteBuffer> buffers = responseWriter.getStreamBufferPool().getPooledStreamBuffer();
			StreamBuffer<ByteBuffer> buffer = buffers;
			for (int i = 0; i < responseSize; i++) {
				byte value = indexValue.apply(i);
				if (!buffer.write(value)) {
					// Buffer full so write use another
					buffer.next = responseWriter.getStreamBufferPool().getPooledStreamBuffer();
					buffer = buffer.next;
					buffer.write(value);
				}
			}

			// Send large response
			responseWriter.write(null, buffers);
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Trigger the request
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure have all the data
			InputStream inputStream = client.getInputStream();
			for (int i = 0; i < responseSize; i++) {
				assertEquals((byte) indexValue.apply(i), inputStream.read(), "Incorrect value for index " + i);
			}
		}
	}

	/**
	 * Ensure can delay sending a response.
	 */
	@Test
	public void delaySendResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest("SEND");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			this.delay(() -> responseWriter.write(null,
					this.tester.createStreamBuffer(responseWriter.getStreamBufferPool(), 1)));
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(1, inputStream.read(), "Incorrect response");
		}
	}

	/**
	 * Ensure can delay sending multiple responses.
	 */
	@Test
	public void delaySendingMultipleResponses() throws IOException {
		this.tester = new SocketManagerTester(1);

		final byte RESPONSE_COUNT = 100;

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				for (byte i = 0; i < RESPONSE_COUNT; i++) {
					requestHandler.handleRequest(i);
				}
			}
		}, (socketServicer) -> (request, responseWriter) -> {

			// Obtain the request
			byte index = (byte) request;

			// Create the response for the request
			StreamBufferPool<ByteBuffer> bufferPool = responseWriter.getStreamBufferPool();
			StreamBuffer<ByteBuffer> response = this.tester.createStreamBuffer(bufferPool, index);
			response.next = this.tester.createStreamBuffer(bufferPool, '*');

			// Delay only the even responses
			if ((index % 2) == 0) {
				// Delay sending the response
				this.delay(() -> responseWriter.write(null, response));
			} else {
				// Send the response immediately
				responseWriter.write(null, response);
			}
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			for (byte i = 0; i < RESPONSE_COUNT; i++) {
				assertEquals(i, inputStream.read(), "Incorrect response");
				assertEquals('*', inputStream.read(), "Incorrect additional response " + i);
			}
		}
	}

	/**
	 * Ensure can delay sending header information.
	 */
	@Test
	public void delaySendHeader() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest("SEND");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			this.delay(() -> responseWriter.write((buffer, pool) -> {
				StreamBuffer.write(new byte[] { 1, 2, 3 }, buffer, pool);
			}, null));
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			for (int i = 1; i <= 3; i++) {
				assertEquals(i, inputStream.read(), "Incorrect response");
			}
		}
	}

	/**
	 * Ensure can delay sending multiple headers.
	 */
	@Test
	public void delaySendingMultipleHeaders() throws IOException {
		this.tester = new SocketManagerTester(1);

		final byte RESPONSE_COUNT = 100;

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				for (byte i = 0; i < RESPONSE_COUNT; i++) {
					requestHandler.handleRequest(i);
				}
			}
		}, (socketServicer) -> (request, responseWriter) -> {

			// Obtain the request
			byte index = (byte) request;

			// Create the header response
			ResponseHeaderWriter header = (buffer, pool) -> {
				StreamBuffer.write(new byte[] { index, (byte) '*' }, buffer, pool);
			};

			// Delay only the even responses
			if ((index % 2) == 0) {
				// Delay sending the response
				this.delay(() -> responseWriter.write(header, null));
			} else {
				// Send the response immediately
				responseWriter.write(header, null);
			}
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			for (byte i = 0; i < RESPONSE_COUNT; i++) {
				assertEquals(i, inputStream.read(), "Incorrect response");
				assertEquals('*', inputStream.read(), "Incorrect additional response " + i);
			}
		}
	}

	/**
	 * Ensure can send immediate data.
	 */
	@Test
	public void immediateData() throws IOException {

		// SSL itself sends immediate data
		if (this.isSecure) {
			return; // SSL connection tests this
		}

		// Create tester
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				StreamBuffer<ByteBuffer> immediate = requestHandler.getStreamBufferPool().getPooledStreamBuffer();
				immediate.write((byte) 2);
				requestHandler.sendImmediateData(immediate);
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			return fail("Immediate response, so no request to service");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(2, inputStream.read(), "Incorrect response");
		}
	}

	/**
	 * Ensure can delay sending immediate data.
	 */
	@Test
	public void issueIfImmediateDataOnAnotherThread() throws Exception {

		// SSL itself sends immediate data
		if (this.isSecure) {
			return; // SSL connection tests this
		}

		// Create tester
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ThreadSafeClosure<Throwable> illegalState = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				this.delay(() -> {
					StreamBuffer<ByteBuffer> immediate = requestHandler.getStreamBufferPool().getPooledStreamBuffer();
					immediate.write((byte) 2);
					try {
						requestHandler.sendImmediateData(immediate);
						illegalState.set(null);
						fail("Should not be successful");
					} catch (IllegalStateException ex) {
						immediate.release();
						illegalState.set(ex);
					}
				});
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			return fail("Immediate response, so no request to service");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure illegal state
			assertTrue(illegalState.waitAndGet() instanceof IllegalStateException,
					"Should be illegal to handle request on another thread");
		}
	}

	/**
	 * Ensure can delay sending immediate data.
	 */
	@Test
	public void delayImmediateDataViaExecution() throws IOException {

		// SSL itself sends immediate data
		if (this.isSecure) {
			return; // SSL connection tests this
		}

		// Create tester
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				this.delay(() -> {
					requestHandler.execute(() -> {
						StreamBuffer<ByteBuffer> immediate = requestHandler.getStreamBufferPool()
								.getPooledStreamBuffer();
						immediate.write((byte) 2);
						requestHandler.sendImmediateData(immediate);
					});
				});
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			return fail("Immediate response, so no request to service");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals(2, inputStream.read(), "Incorrect response");
		}
	}

	/**
	 * Ensure handle cancelling the processing.
	 */
	@Test
	public void cancelProcessing() throws IOException {

		// Create tester
		this.tester = new SocketManagerTester(1);

		// Process Manager
		boolean[] isCancelled = new boolean[] { false };
		ProcessManager manager = () -> {
			synchronized (isCancelled) {
				isCancelled[0] = true;
			}
		};

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandler.handleRequest("CANCEL");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			return manager;
		});

		this.tester.start();

		// Undertake connect and send data
		Socket client = this.tester.getClient();

		// Send some data (to trigger request)
		OutputStream outputStream = client.getOutputStream();
		outputStream.write(1);
		outputStream.flush();

		// Close socket
		client.close();

		// Wait for cancel
		this.threaded.waitForTrue(() -> {
			synchronized (isCancelled) {
				return isCancelled[0];
			}
		});
	}

	/**
	 * Ensure stop reading if overloading write.
	 */
	@Test
	public void stopReadingOnOverloadWrite() throws IOException {

		// SSL itself sends immediate data
		assumeFalse(this.isSecure, "Need to handle SSL for stop / start reading input");

		// Create tester
		final long upperMemoryThreshold = this.getBufferSize() * 10;
		this.tester = new SocketManagerTester(1, upperMemoryThreshold);

		// Bind to server socket
		AtomicInteger buffersWritten = new AtomicInteger(0);
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {

				// Write data until stop reading
				do {
					buffersWritten.incrementAndGet();
					StreamBuffer<ByteBuffer> write = requestHandler.getStreamBufferPool().getPooledStreamBuffer();
					while (write.pooledBuffer.hasRemaining()) {
						write.pooledBuffer.put((byte) 2);
					}
					requestHandler.sendImmediateData(write);
				} while (requestHandler.isReadingInput());
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			return fail("Immediate response, so no request to service");
		});

		this.tester.start();

		// Undertake connect and send data
		Socket client = this.tester.getClient();

		// Send some data (to trigger request)
		OutputStream outputStream = client.getOutputStream();
		outputStream.write(1);
		outputStream.flush();

		// Should stop reading
		this.threaded.waitForTrue(() -> !this.tester.isSocketListenerReading(0));

		// Determine the size of data written
		long writeDataSize = buffersWritten.get() * this.getBufferSize();
		assertTrue(upperMemoryThreshold < writeDataSize, "Due to socket buffers, should write more data");

		// Read response until all buffers read
		InputStream input = client.getInputStream();
		for (long i = 0; i < writeDataSize; i++) {
			assertEquals(2, input.read(), "Should read large response data");
		}

		// Should start reading again
		this.threaded.waitForTrue(() -> this.tester.isSocketListenerReading(0));
	}

}
