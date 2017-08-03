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
package net.officefloor.server.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import junit.framework.TestCase;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;

/**
 * Abstract {@link TestCase} for testing a {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpServerImplementationTest extends OfficeFrameTestCase {

	/**
	 * Creates the {@link HttpServerImplementation} to test.
	 * 
	 * @return {@link HttpServerImplementation} to test.
	 */
	protected abstract HttpServerImplementation createHttpServerImplementation();

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Registered {@link PipelineExecutor} instances.
	 */
	private List<PipelineExecutor> executors = new LinkedList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Create the HTTP server implementation
		HttpServerImplementation implementation = this.createHttpServerImplementation();

		// Obtain the default SSL context
		SSLContext sslContext = OfficeFloorDefaultSslContextSource.createServerSslContext(null);

		// Compile the OfficeFloor
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {

			// Configure the HTTP Server
			HttpServer.configureHttpServer(7878, 7979, implementation, sslContext, "OFFICE", "SERVICER", "service",
					context.getOfficeFloorDeployer(), context.getOfficeFloorSourceContext());

		});
		compile.office((context) -> {
			context.addSection("SERVICER", Servicer.class);
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();
	}

	public static class Servicer {
		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("Incorrect request URI", "/test", connection.getHttpRequest().getRequestURI());
			connection.getHttpResponse().getEntityWriter().write("hello world");
		}
	}

	@Override
	protected void tearDown() throws Exception {

		// Close the executors
		for (PipelineExecutor executor : this.executors) {
			executor.close();
		}

		// Close the OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Remaining tear down
		super.tearDown();
	}

	/**
	 * Ensure can send a single HTTP request.
	 */
	public void testSingleRequest() throws IOException {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/test"));
			assertEquals("Incorrect status", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect response", "hello world", HttpClientTestUtil.getEntityBody(response));
		}
	}

	/**
	 * Ensure can send a single HTTPS request.
	 */
	public void testSingleSecureRequest() throws IOException {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(true)) {
			HttpResponse response = client.execute(new HttpGet("https://localhost:7979/test"));
			assertEquals("Incorrect status", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect response", "hello world", HttpClientTestUtil.getEntityBody(response));
		}
	}

	/**
	 * Ensure can send multiple HTTP requests.
	 */
	public void testMultipleRequests() throws IOException {
		for (int i = 0; i < 1000; i++) {
			this.testSingleRequest();
		}
	}

	/**
	 * Ensure can send multiple HTTPS requests.
	 */
	public void testMultipleSecureRequests() throws IOException {
		for (int i = 0; i < 1000; i++) {
			this.testSingleSecureRequest();
		}
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 */
	public void testPipelining() throws Exception {

		// Create pipeline executor
		PipelineExecutor executor = new PipelineExecutor(7878, 1000);

		// Do warm up
		executor.doPipelineRun(20000).printResult(this.getName() + " WARMUP");

		// Undertake performance run
		executor.doPipelineRun(100000).printResult(this.getName() + " RUN");
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 */
	public void testHeavyLoad() throws Exception {

		// Number of clients
		final int CLIENT_COUNT = 2;

		// Create the pipeline executors
		PipelineExecutor[] executors = new PipelineExecutor[CLIENT_COUNT];
		for (int i = 0; i < CLIENT_COUNT; i++) {
			executors[i] = new PipelineExecutor(7878, 100000);
		}

		// Do warm up
		executors[0].doPipelineRun(20000).printResult(this.getName() + " WARMUP");

		// Run the executors
		for (PipelineExecutor executor : executors) {
			new Thread(executor).start();
		}

		// Wait for completion of run
		PipelineResult[] results = new PipelineResult[CLIENT_COUNT];
		for (int i = 0; i < CLIENT_COUNT; i++) {
			results[i] = executors[i].waitForCompletion();
		}

		// Provide summary of results
		long minStartTime = results[0].startTime;
		long maxEndTime = results[0].endTime;
		int totalRequests = 0;
		for (int i = 0; i < CLIENT_COUNT; i++) {
			PipelineResult result = results[i];
			result.printResult(this.getName() + " CLIENT-" + i);
			minStartTime = (result.startTime < minStartTime) ? result.startTime : minStartTime;
			maxEndTime = (result.endTime > maxEndTime) ? result.endTime : maxEndTime;
			totalRequests += result.requestCount;
		}
		new PipelineResult(minStartTime, maxEndTime, totalRequests).printResult(this.getName() + " TOTAL");
	}

	/**
	 * Executes request in a pipeline for performance testing.
	 */
	private class PipelineExecutor implements Runnable {

		/**
		 * Number of requests to make.
		 */
		private final int requestCount;

		/**
		 * {@link Selector}.
		 */
		private final Selector selector;

		/**
		 * {@link SocketChannel}.
		 */
		private final SocketChannel channel;

		/**
		 * {@link SelectionKey}.
		 */
		private final SelectionKey selectionKey;

		/**
		 * Result of {@link Runnable}.
		 */
		private Object runResult = null;

		/**
		 * Instantiate.
		 * 
		 * @param port
		 *            Port for the {@link HttpServer}.
		 * @param requestCount
		 *            Number of requests to make.
		 * @throws IOException
		 *             If fails to connect to {@link HttpServer}.
		 */
		private PipelineExecutor(int port, int requestCount) throws IOException {
			this.requestCount = requestCount;

			// Pipeline requests to server
			this.selector = Selector.open();
			this.channel = SocketChannel.open(new InetSocketAddress("localhost", port));
			this.channel.configureBlocking(false);
			this.selectionKey = channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			// Register
			AbstractHttpServerImplementationTest.this.executors.add(this);
		}

		/**
		 * Closes this {@link PipelineExecutor}.
		 */
		private void close() throws IOException {
			this.channel.close();
			this.selector.close();
		}

		/**
		 * Undertakes a pipeline run of requests.
		 * 
		 * @param requestCount
		 *            Number of requests.
		 * @return {@link PipelineResult}.
		 */
		private PipelineResult doPipelineRun(int requestCount) throws IOException {

			// Reset selector for run
			this.selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			// Create the request
			StringBuilder request = new StringBuilder();
			request.append("GET /test HTTP/1.1\n");
			request.append("test: value\n");
			request.append("\n");
			byte[] requestData = UsAsciiUtil.convertToHttp(request.toString());
			ByteBuffer requestBuffer = ByteBuffer.allocateDirect(requestData.length);
			requestBuffer.put(requestData);
			requestBuffer.flip();

			// Create the expected response
			StringBuilder response = new StringBuilder();
			response.append("HTTP/1.1 200 OK\n");
			response.append("Server: WoOF 3.0.0\n");
			response.append("Content-Type: text/html; charset=UTF-8\n");
			response.append("Content-Length: 11\n");
			response.append("\n");
			response.append("hello world");
			byte[] responseData = UsAsciiUtil.convertToHttp(response.toString());

			// Initiate run variables
			int requestDataSent = 0;
			int requestSentCount = 0;
			int responseReceivedCount = 0;
			ByteBuffer responseBuffer = ByteBuffer.allocateDirect(1000);
			int responseDataPosition = 0;

			// Record start time
			long startTime = System.currentTimeMillis();

			// Start pipeline run
			while (responseReceivedCount < requestCount) {

				// Stop interest in writing if all requests sent
				if (requestSentCount >= requestCount) {
					// All requests written
					this.selectionKey.interestOps(SelectionKey.OP_READ);
				}

				// Wait for some data
				this.selector.select(10);

				// Send the request
				if (selectionKey.isWritable()) {
					// Keep writing until fill socket of all requests sent
					FINISHED_WRITING: while (requestSentCount < requestCount) {
						int writtenBytes = this.channel.write(requestBuffer);
						requestDataSent += writtenBytes;
						if (requestDataSent == requestData.length) {
							// Request written
							requestSentCount++;

							// Setup for next request
							requestDataSent = 0;
							requestBuffer.clear();
						} else {
							// Buffer full, so wait until can write again
							break FINISHED_WRITING;
						}
					}
				}

				// Read in next response
				if (selectionKey.isReadable()) {
					// Read in as many requests as possible
					int bytesRead;
					do {
						responseBuffer.clear();
						bytesRead = this.channel.read(responseBuffer);
						responseBuffer.flip();
						for (int i = 0; i < bytesRead; i++) {
							byte expectedCharacter = responseData[responseDataPosition];
							byte actualCharacter = responseBuffer.get();
							if (expectedCharacter != actualCharacter) {

								// Obtain the text
								byte[] responseBytes = new byte[bytesRead];
								responseBuffer.clear();
								responseBuffer.get(responseBytes);
								String responseText = UsAsciiUtil.convertToString(responseBytes);

								// Provide the error
								assertEquals(
										"Incorrect character " + responseDataPosition + " ("
												+ UsAsciiUtil.convertToChar(expectedCharacter) + " != "
												+ UsAsciiUtil.convertToChar(actualCharacter) + "): " + responseText,
										expectedCharacter, actualCharacter);
							}
							responseDataPosition = (responseDataPosition + 1) % responseData.length;
							if (responseDataPosition == 0) {
								// Another response received
								responseReceivedCount++;
							}
						}
					} while (bytesRead != 0);
				}
			}

			// Capture end time and calculate run time
			long endTime = System.currentTimeMillis();

			// Return performance result of pipeline
			return new PipelineResult(startTime, endTime, requestCount);
		}

		/**
		 * Waits for completion.
		 * 
		 * @return {@link PipelineResult}.
		 * @throws Exception
		 *             If run failed.
		 */
		private synchronized PipelineResult waitForCompletion() throws Exception {

			// Determine if already complete
			if (this.runResult != null) {
				return this.returnResult();
			}

			// Wait for completion
			this.wait();

			// Return the result
			return this.returnResult();
		}

		/**
		 * Obtains the result.
		 * 
		 * @return {@link PipelineResult}.
		 * @throws Exception
		 *             If run failed.
		 */
		private PipelineResult returnResult() throws Exception {

			// Provide result
			if (this.runResult instanceof PipelineResult) {
				return (PipelineResult) this.runResult;
			}

			// Propagate the failure
			throw fail((Throwable) this.runResult);
		}

		/*
		 * ============== Runnable =========================
		 */

		@Override
		public synchronized void run() {

			try {
				// Undertake run
				this.runResult = this.doPipelineRun(this.requestCount);

			} catch (Throwable ex) {
				// Capture failure
				this.runResult = ex;

			} finally {
				// Notify complete
				this.notify();
			}
		}
	}

	/**
	 * Result of a {@link PipelineExecutor}.
	 */
	private static class PipelineResult {

		private long startTime;

		private long endTime;

		private int requestCount;

		private PipelineResult(long startTime, long endTime, int requestCount) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.requestCount = requestCount;
		}

		private void printResult(String prefix) {
			long runTime = this.endTime - this.startTime;
			int requestsPerSecond = (int) ((this.requestCount) / (((float) runTime) / 1000.0));
			System.out.println(prefix + " ran " + this.requestCount + " requests in " + runTime + " milliseconds ("
					+ requestsPerSecond + " per second)");
		}
	}

}