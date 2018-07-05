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
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Function;

import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.server.RequestHandler.Execution;
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
	public void testShutdown() throws Exception {
		SocketManagerTester tester = new SocketManagerTester(1);
		tester.start();
		tester.shutdown();
		tester.waitForCompletion();
	}

	/**
	 * Ensure can shutdown multiple {@link SocketListener} instances.
	 */
	public void testShutdownMultipleListeners() throws Exception {
		SocketManagerTester tester = new SocketManagerTester(4);
		tester.start();
		tester.shutdown();
		tester.waitForCompletion();
	}

	/**
	 * Ensure can accept a connection.
	 */
	public void testAcceptConnection() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		Closure<ServerSocket> serverSocket = new Closure<>();
		ThreadSafeClosure<Socket> acceptedSocket = new ThreadSafeClosure<>();
		this.tester.bindServerSocket((socket) -> {
			serverSocket.value = socket;
			return 1000;
		}, (socket) -> {
			acceptedSocket.set(socket);
		}, (requestHandler) -> (buffer, isNewBuffer) -> fail("Should not be invoked, as only accepting connection"),
				(socketServicer) -> (request, responseWriter) -> fail("Should not be invoked, as no requests"));
		assertNotNull("Should have bound server socket", serverSocket.value);

		this.tester.start();

		// Undertake connect (will occur asynchronously)
		try (Socket client = this.tester.getClient()) {
			assertTrue("Should be connected", client.isConnected());

			// Ensure connects (must wait for connection)
			acceptedSocket.waitAndGet();
		}
	}

	/**
	 * Ensure can receive data on connection.
	 */
	public void testReceiveData() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ThreadSafeClosure<Byte> data = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			data.set(buffer.pooledBuffer.get(0));
		}, (socketServicer) -> (request, responseWriter) -> {
			fail("Should not be invoked, as no requests");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure received the data
			assertEquals("Should have received data", 1, (byte) data.waitAndGet());
		}
	}

	/**
	 * Ensure can receive request on connection.
	 */
	public void testReceiveRequest() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		final Object REQUEST = "TEST";

		// Bind to server socket
		ThreadSafeClosure<Object> receivedRequest = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest(REQUEST);
		}, (socketServicer) -> (request, responseWriter) -> {
			receivedRequest.set(request);
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure received the request
			assertSame("Should have received request", REQUEST, receivedRequest.waitAndGet());
		}
	}

	/**
	 * Ensure error if invoke request on another {@link Thread}.
	 */
	public void testIssueIfReceiveRequestOnAnotherThread() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ThreadSafeClosure<Throwable> illegalState = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			this.delay(() -> {
				try {
					requestHandler.handleRequest("illegal");
					illegalState.set(null);
					fail("Should not be successful");
				} catch (IllegalStateException ex) {
					illegalState.set(ex);
				}
			});
		}, (socketServicer) -> (request, responseWriter) -> {
			fail("Should not receive request");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure illegal state
			assertTrue("Should be illegal to handle request on another thread",
					illegalState.waitAndGet() instanceof IllegalStateException);
		}
	}

	/**
	 * Ensure can delay receiving request on connection via {@link Execution}.
	 */
	public void testDelayReceiveRequestViaExecution() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		final Object REQUEST = "TEST";

		// Bind to server socket
		ThreadSafeClosure<Object> receivedRequest = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			this.delay(() -> {
				requestHandler.execute(() -> requestHandler.handleRequest(REQUEST));
			});
		}, (socketServicer) -> (request, responseWriter) -> {
			receivedRequest.set(request);
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure received the request
			assertSame("Should have received request", REQUEST, receivedRequest.waitAndGet());
		}
	}

	/**
	 * Ensure can send response.
	 */
	public void testSendResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest("SEND");
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write(null, this.tester.createStreamBuffer(2));
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
			assertEquals("Incorrect response", 2, inputStream.read());
		}
	}

	/**
	 * Ensure can handle requests on multiple connections.
	 */
	public void testMultipleConnections() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest(buffer.pooledBuffer.get(0));
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write(null, this.tester.createStreamBuffer(10 + (byte) request));
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
			assertEquals("Incorrect response", 11 + i, inputStream.read());
		}
		for (int i = 0; i < connectionCount; i++) {
			clients[i].close();
		}
	}

	/**
	 * Ensure can send header.
	 */
	public void testSendHeaderOnly() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest("SEND");
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write((head, pool) -> head.write((byte) 2), null);
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
			assertEquals("Incorrect response", 2, inputStream.read());
		}
	}

	/**
	 * Ensure can send header and body.
	 */
	public void testSendHeaderAndBody() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest("SEND");
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write((head, pool) -> head.write((byte) 2), this.tester.createStreamBuffer(3));
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
			assertEquals("Incorrect header value", 2, inputStream.read());
			assertEquals("Incorrect body value", 3, inputStream.read());
		}
	}

	/**
	 * Ensure can send header and file.
	 */
	public void testSendHeaderAndFile() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Create file
		FileChannel file = TemporaryFiles.getDefault().createTempFile("testSendHeaderAndFile", new byte[] { 3 });
		StreamBuffer<ByteBuffer> fileBuffer = this.tester.bufferPool.getFileStreamBuffer(file, 0, -1, null);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest("SEND");
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write((head, pool) -> head.write((byte) 2), fileBuffer);
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
			assertEquals("Incorrect header value", 2, inputStream.read());
			assertEquals("Incorrect file value", 3, inputStream.read());

			// Ensure the file is still open
			assertTrue("File should still be open", file.isOpen());
		}
	}

	/**
	 * Ensure can send header and large file.
	 */
	public void testSendHeaderAndLargeFile() throws Exception {
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
		StreamBuffer<ByteBuffer> fileBuffer = this.tester.bufferPool.getFileStreamBuffer(file, 0, -1,
				(completedFile, isWritten) -> completedFile.close());
		fileBuffer.next = this.tester.createStreamBuffer(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest("SEND");
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write((head, pool) -> head.write((byte) 2), fileBuffer);
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
			assertEquals("Incorrect header value", 2, inputStream.read());
			for (int i = 0; i < fileSize; i++) {
				byte expected = transform.apply(i);
				assertEquals("Incorrect file value for byte " + i, expected, inputStream.read());
			}
			assertEquals("Incorrect final value", 1, inputStream.read());

			// File should be closed
			assertFalse("File should be closed", file.isOpen());
		}
	}

	/**
	 * Ensure can send header and file segment.
	 */
	public void testSendHeaderAndFileSegment() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Create file
		FileChannel file = TemporaryFiles.getDefault().createTempFile("testSendHeaderAndFileSegment",
				new byte[] { 1, 2, 3 });
		StreamBuffer<ByteBuffer> fileBuffer = this.tester.bufferPool.getFileStreamBuffer(file, 2, 1,
				(completedFile, isWritten) -> completedFile.close());
		fileBuffer.next = this.tester.createStreamBuffer(4);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest("SEND");
		}, (socketServicer) -> (request, responseWriter) -> {
			responseWriter.write((head, pool) -> head.write((byte) 2), fileBuffer);
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
			assertEquals("Incorrect header value", 2, inputStream.read());
			assertEquals("Incorrect file value", 3, inputStream.read());
			assertEquals("Incorrect further value", 4, inputStream.read());

			// File should be closed
			assertFalse("File should be closed", file.isOpen());
		}
	}

	/**
	 * Ensure can pipeline response. Ensure responses are sent in the same order
	 * requests are received.
	 */
	public void testInOrderPipelineResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest((byte) 1);
			requestHandler.handleRequest((byte) 2);
		}, (socketServicer) -> (request, responseWriter) -> {
			StreamBuffer<ByteBuffer> response = this.tester.createStreamBuffer((byte) request);
			responseWriter.write(null, response);
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
			assertEquals("Incorrect first response", 1, inputStream.read());
			assertEquals("Incorrect second response", 2, inputStream.read());
		}
	}

	/**
	 * Ensure can pipeline response. Ensure responses are sent in the same order
	 * requests are received.
	 */
	public void testOutOfOrderPipelineResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ThreadSafeClosure<ResponseWriter> writer = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest((byte) 1);
			requestHandler.handleRequest((byte) 2);
		}, (socketServicer) -> (request, responseWriter) -> {
			switch ((byte) request) {
			case 1:
				// First request, so capture writer for later
				writer.set(responseWriter);
				break;

			case 2:
				// Second request, so write (out of order)
				StreamBuffer<ByteBuffer> responseTwo = this.tester.createStreamBuffer(2);
				responseWriter.write(null, responseTwo);

				// Now write the first request (out of order)
				StreamBuffer<ByteBuffer> responseOne = this.tester.createStreamBuffer(1);
				writer.get().write(null, responseOne);
				break;

			default:
				fail("Invalid request " + request);
			}
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
			assertEquals("Incorrect first response", 1, inputStream.read());
			assertEquals("Incorrect second response", 2, inputStream.read());
		}
	}

	/**
	 * Ensure can select on write operations to complete writing the large
	 * response.
	 */
	public void testLargeResponse() throws IOException, InterruptedException {
		this.tester = new SocketManagerTester(1);

		final Function<Integer, Byte> indexValue = (index) -> (byte) (index % Byte.MAX_VALUE);

		// Bind to server socket
		final int responseSize = this.getBufferSize() * 5;
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest((byte) 1);
		}, (socketServicer) -> (request, responseWriter) -> {
			// Create very large response
			StreamBuffer<ByteBuffer> buffers = this.tester.bufferPool.getPooledStreamBuffer();
			StreamBuffer<ByteBuffer> buffer = buffers;
			for (int i = 0; i < responseSize; i++) {
				byte value = indexValue.apply(i);
				if (!buffer.write(value)) {
					// Buffer full so write use another
					buffer.next = this.tester.bufferPool.getPooledStreamBuffer();
					buffer = buffer.next;
					buffer.write(value);
				}
			}

			// Send large response
			responseWriter.write(null, buffers);
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
				assertEquals("Incorrect value for index " + i, (byte) indexValue.apply(i), inputStream.read());
			}
		}
	}

	/**
	 * Ensure can delay sending a response.
	 */
	public void testDelaySendResponse() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest("SEND");
		}, (socketServicer) -> (request, responseWriter) -> {
			this.delay(() -> responseWriter.write(null, this.tester.createStreamBuffer(1)));
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
			assertEquals("Incorrect response", 1, inputStream.read());
		}
	}

	/**
	 * Ensure can delay sending multiple responses.
	 */
	public void testDelaySendingMultipleResponses() throws IOException {
		this.tester = new SocketManagerTester(1);

		final byte RESPONSE_COUNT = 100;

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			for (byte i = 0; i < RESPONSE_COUNT; i++) {
				requestHandler.handleRequest(i);
			}
		}, (socketServicer) -> (request, responseWriter) -> {

			// Obtain the request
			byte index = (byte) request;

			// Create the response for the request
			StreamBuffer<ByteBuffer> response = this.tester.createStreamBuffer(index);
			response.next = this.tester.createStreamBuffer('*');

			// Delay only the even responses
			if ((index % 2) == 0) {
				// Delay sending the response
				this.delay(() -> responseWriter.write(null, response));
			} else {
				// Send the response immediately
				responseWriter.write(null, response);
			}
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
				assertEquals("Incorrect response", i, inputStream.read());
				assertEquals("Incorrect additional response " + i, '*', inputStream.read());
			}
		}
	}

	/**
	 * Ensure can delay sending header information.
	 */
	public void testDelaySendHeader() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			requestHandler.handleRequest("SEND");
		}, (socketServicer) -> (request, responseWriter) -> {
			this.delay(() -> responseWriter.write((buffer, pool) -> {
				StreamBuffer.write(new byte[] { 1, 2, 3 }, buffer, pool);
			}, null));
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
				assertEquals("Incorrect response", i, inputStream.read());
			}
		}
	}

	/**
	 * Ensure can delay sending multiple headers.
	 */
	public void testDelaySendingMultipleHeaders() throws IOException {
		this.tester = new SocketManagerTester(1);

		final byte RESPONSE_COUNT = 100;

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			for (byte i = 0; i < RESPONSE_COUNT; i++) {
				requestHandler.handleRequest(i);
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
				assertEquals("Incorrect response", i, inputStream.read());
				assertEquals("Incorrect additional response " + i, '*', inputStream.read());
			}
		}
	}

	/**
	 * Ensure can send immediate data.
	 */
	public void testImmediateData() throws IOException {

		// SSL itself sends immediate data
		if (this.isSecure) {
			return; // SSL connection tests this
		}

		// Create tester
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			StreamBuffer<ByteBuffer> immediate = this.tester.bufferPool.getPooledStreamBuffer();
			immediate.write((byte) 2);
			requestHandler.sendImmediateData(immediate);
		}, (socketServicer) -> (request, responseWriter) -> {
			fail("Immediate response, so no request to service");
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
			assertEquals("Incorrect response", 2, inputStream.read());
		}
	}

	/**
	 * Ensure can delay sending immediate data.
	 */
	public void testIssueIfImmediateDataOnAnotherThread() throws Exception {

		// SSL itself sends immediate data
		if (this.isSecure) {
			return; // SSL connection tests this
		}

		// Create tester
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		ThreadSafeClosure<Throwable> illegalState = new ThreadSafeClosure<>();
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			this.delay(() -> {
				StreamBuffer<ByteBuffer> immediate = this.tester.bufferPool.getPooledStreamBuffer();
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
		}, (socketServicer) -> (request, responseWriter) -> {
			fail("Immediate response, so no request to service");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Ensure illegal state
			assertTrue("Should be illegal to handle request on another thread",
					illegalState.waitAndGet() instanceof IllegalStateException);
		}
	}

	/**
	 * Ensure can delay sending immediate data.
	 */
	public void testDelayImmediateDataViaExecution() throws IOException {

		// SSL itself sends immediate data
		if (this.isSecure) {
			return; // SSL connection tests this
		}

		// Create tester
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer, isNewBuffer) -> {
			this.delay(() -> {
				requestHandler.execute(() -> {
					StreamBuffer<ByteBuffer> immediate = this.tester.bufferPool.getPooledStreamBuffer();
					immediate.write((byte) 2);
					requestHandler.sendImmediateData(immediate);
				});
			});
		}, (socketServicer) -> (request, responseWriter) -> {
			fail("Immediate response, so no request to service");
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
			assertEquals("Incorrect response", 2, inputStream.read());
		}
	}

}