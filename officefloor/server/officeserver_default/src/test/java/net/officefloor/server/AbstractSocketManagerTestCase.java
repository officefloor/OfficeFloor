/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.server.http.stream.TemporaryFiles;
import net.officefloor.server.stream.StreamBuffer;

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
	 * Ensure can delay receiving request on connection via {@link SocketRunnable}.
	 */
	@Test
	public void delayReceiveRequestViaSocketRunnable() throws IOException, InterruptedException {
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
			responseWriter.write(null, this.tester.createStreamBuffer(responseWriter, 2));
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
			responseWriter.write(null, this.tester.createStreamBuffer(responseWriter, 10 + (byte) request));
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
					this.tester.createStreamBuffer(responseWriter, 3));
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
				StreamBuffer<ByteBuffer> fileBuffer = responseWriter.getStreamBufferPool().getFileStreamBuffer(file, 0,
						-1, (completedFile, isWritten) -> completedFile.close());
				fileBuffer.next = this.tester.createStreamBuffer(responseWriter, 1);
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
				StreamBuffer<ByteBuffer> fileBuffer = responseWriter.getStreamBufferPool().getFileStreamBuffer(file, 2,
						1, (completedFile, isWritten) -> completedFile.close());
				fileBuffer.next = this.tester.createStreamBuffer(responseWriter, 4);
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
			StreamBuffer<ByteBuffer> response = this.tester.createStreamBuffer(responseWriter, (byte) request);
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
				// Second request, so write (out of order)
				StreamBuffer<ByteBuffer> responseTwo = this.tester.createStreamBuffer(responseWriter, 2);
				responseWriter.write(null, responseTwo);

				// Now write the first request (out of order)
				ResponseWriter firstWriter = writer.get();
				StreamBuffer<ByteBuffer> responseOne = this.tester.createStreamBuffer(firstWriter, 1);
				firstWriter.write(null, responseOne);
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
				assertEquals(1, buffer.pooledBuffer.get(0), "Incorrect request data");
				requestHandler.handleRequest("SEND");
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			this.delay(() -> {
				responseWriter.write(null, this.tester.createStreamBuffer(responseWriter, 1));
			});
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
			int result = inputStream.read();
			assertEquals(1, result, "Incorrect response");
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
			StreamBuffer<ByteBuffer> response = this.tester.createStreamBuffer(responseWriter, index);
			response.next = this.tester.createStreamBuffer(responseWriter, '*');

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
	public void delayImmediateDataViaSocketRunnable() throws IOException {

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
		ThreadSafeClosure<Byte> requestData = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestData.set(buffer.pooledBuffer.get(0));
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

		// Wait for request
		assertEquals((byte) 1, requestData.waitAndGet(), "Incorrect request data");

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
	 * Ensure stop reading if too many socket requests.
	 */
	@Test
	public void stopReadingOnTooManySocketRequests() throws IOException {

		// Create tester
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		final int REQUEST_COUNT = 10;
		ThreadSafeClosure<RequestHandler<?>> requestHandlerClosure = new ThreadSafeClosure<>();
		Queue<ResponseWriter> responseWriters = new ConcurrentLinkedQueue<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, bytesRead, isNewBuffer) -> {
			if (bytesRead == 1) {
				requestHandlerClosure.set(requestHandler);
				for (int i = 0; i < REQUEST_COUNT; i++) {
					requestHandler.handleRequest("SEND " + i);
				}
			}
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriters.add(responseWriter);
			return null;
		});

		this.tester.start();

		// Undertake connect and send data
		Socket client = this.tester.getClient();

		// Send some data (to trigger request)
		OutputStream outputStream = client.getOutputStream();
		outputStream.write(1);
		outputStream.flush();

		// Wait until all requests undertaken
		this.threaded.waitForTrue(() -> responseWriters.size() == REQUEST_COUNT);

		// Should be stopped reading
		assertFalse(requestHandlerClosure.get().isReadingInput(), "Should stop reading");

		// Write the responses
		ResponseWriter responseWriter;
		while ((responseWriter = responseWriters.poll()) != null) {
			responseWriter.write(null, this.tester.createStreamBuffer(responseWriter, 1));
		}

		// Should allow reading again
		this.threaded.waitForTrue(() -> requestHandlerClosure.get().isReadingInput());

		// Read response until all buffers read
		InputStream input = client.getInputStream();
		for (long i = 0; i < REQUEST_COUNT; i++) {
			assertEquals(1, input.read(), "Should read responses");
		}
	}

}
