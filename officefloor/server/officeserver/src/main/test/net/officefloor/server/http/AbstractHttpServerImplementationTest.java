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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;

import junit.framework.TestCase;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;
import net.officefloor.server.stream.ServerWriter;

/**
 * Abstract {@link TestCase} for testing a {@link HttpServerImplementation}.
 * 
 * @param <M>
 *            Type of momento from raw HTTP server.
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpServerImplementationTest<M> extends OfficeFrameTestCase {

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation serverLocation = new HttpServerLocationImpl();

	/**
	 * Creates a new {@link HttpHeader}.
	 * 
	 * @param name
	 *            {@link HttpHeader} name.
	 * @param value
	 *            {@link HttpHeader} value.
	 * @return New {@link HttpHeader}.
	 */
	protected static HttpHeader newHttpHeader(String name, String value) {
		return new SerialisableHttpHeader(name, value);
	}

	/**
	 * Test {@link Exception} to be thrown for testing handling
	 * {@link Escalation}.
	 */
	private static final Exception TEST_EXCEPTION = new Exception("Test Failure");

	/**
	 * Obtains the expected {@link HttpServerImplementation} {@link Class} being
	 * tested.
	 * 
	 * @return Expected {@link HttpServerImplementation} {@link Class}.
	 */
	protected abstract Class<? extends HttpServerImplementation> getHttpServerImplementationClass();

	/**
	 * <p>
	 * Starts a raw implementation of the underlying HTTP server. This allows
	 * performance comparisons of adding in {@link OfficeFloor} servicing
	 * overheads.
	 * <p>
	 * The raw implementation is to return &quote;hello world&quot; in UTF-8
	 * encoding for the response entity.
	 * 
	 * @param serverLocation
	 *            {@link HttpServerLocation}.
	 * @return Momento to provide to stopping the server.
	 * @throws Exception
	 *             If fails to start the raw HTTP server.
	 */
	protected abstract M startRawHttpServer(HttpServerLocation serverLocation) throws Exception;

	/**
	 * Stops the raw implementation.
	 * 
	 * @param momento
	 *            Momento provided from starting the raw HTTP server.
	 * @throws Exception
	 *             If fails to stop the raw HTTP server.
	 */
	protected abstract void stopRawHttpServer(M momento) throws Exception;

	/**
	 * Obtains the server response {@link HttpHeader} instances in the order
	 * they are sent from the server.
	 * 
	 * @return Server response {@link HttpHeader} instances in the order they
	 *         are sent from the server.
	 */
	protected abstract HttpHeader[] getServerResponseHeaderValues();

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	/**
	 * Momento of HTTP server.
	 */
	private M momento = null;

	/**
	 * Registered {@link PipelineExecutor} instances.
	 */
	private List<PipelineExecutor> executors = new LinkedList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Log GC
		this.setLogGC();

		// Reset the compare results
		CompareResult.reset(this.getClass());
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

		// Stop the raw HTTP server
		if (this.momento != null) {
			this.stopRawHttpServer(this.momento);
		}

		// Ensure the server has stopped serving requests
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			client.execute(new HttpGet(this.serverLocation.createClientUrl(false, "/test")));
			fail("Server should have stopped listening");
		} catch (HttpHostConnectException ex) {
			assertTrue("Incorrect connection error",
					ex.getMessage().startsWith(
							"Connect to " + this.serverLocation.getDomain() + ":" + this.serverLocation.getHttpPort())
							&& ex.getMessage().endsWith("failed: Connection refused"));
		}

		// Remaining tear down
		super.tearDown();
	}

	/**
	 * Starts the {@link HttpServer}.
	 * 
	 * @param sectionServicer
	 *            {@link Class} of the {@link ClassSectionSource} to service the
	 *            {@link HttpRequest}.
	 * @throws Exception
	 *             If fails to start the {@link HttpServer}.
	 */
	protected void startHttpServer(Class<?> sectionServicer) throws Exception {

		// Compile the OfficeFloor
		Closure<HttpServer> httpServer = new Closure<>();
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {

			// Obtain the deployer
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Obtain the input
			DeployedOfficeInput serviceHandler = context.getOfficeFloorDeployer().getDeployedOffice("OFFICE")
					.getDeployedOfficeInput("SERVICER", "service");

			// Configure the HTTP Server
			httpServer.value = new HttpServer(serviceHandler, deployer, context.getOfficeFloorSourceContext());

			// Provide thread object
			if (sectionServicer == ThreadedServicer.class) {
				context.addManagedObject("ThreadedManagedObject", ThreadedManagedObject.class,
						ManagedObjectScope.THREAD);
				deployer.addTeam("Threaded", ExecutorCachedTeamSource.class.getName()).addTypeQualification(null,
						ThreadedManagedObject.class.getName());
			}
		});
		compile.office((context) -> {
			context.getOfficeArchitect().enableAutoWireTeams();
			context.addSection("SERVICER", sectionServicer);
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();

		// Ensure correct HTTP server implementation
		Class<? extends HttpServerImplementation> expectedImplementation = this.getHttpServerImplementationClass();
		assertEquals("Incorrect HTTP Server implementation", expectedImplementation,
				httpServer.value.getHttpServerImplementation().getClass());
	}

	public static class Servicer {

		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("Incorrect request URI", "/test", connection.getRequest().getUri());
			connection.getResponse().getEntityWriter().write("hello world");
		}
	}

	public static class FastServicer {

		private static final byte[] HELLO_WORLD = "hello world"
				.getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		private static final HttpHeaderValue TEXT_PLAIN = new HttpHeaderValue("text/plain");

		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("Incorrect request URI", "/test", connection.getRequest().getUri());
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.setContentType(TEXT_PLAIN, null);
			response.getEntity().write(HELLO_WORLD);
		}
	}

	public static class FailServicer {
		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("Incorrect request URI", "/test", connection.getRequest().getUri());

			// Write some content, that should be reset
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.getHeaders().addHeader("test", "not sent");
			ServerWriter writer = response.getEntityWriter();
			writer.write("This content should be reset");
			writer.flush();

			// Escalate the test exception
			throw TEST_EXCEPTION;
		}
	}

	public static class EncodedUrlServicer {

		public void service(ServerHttpConnection connection) throws IOException {
			String requestUri = connection.getRequest().getUri();
			assertEquals("Should have encoded ? #", "/encoded-%3f-%23-+?query=string", requestUri);
			connection.getResponse().getEntityWriter().write("success");
		}
	}

	public static class ThreadedManagedObject {
	}

	public static class ThreadedServicer {

		@FlowInterface
		public static interface Flows {
			void doFlow(Thread thread);
		}

		public void service(Flows flows) {
			flows.doFlow(Thread.currentThread());
		}

		public void doFlow(ServerHttpConnection connection, @Parameter Thread thread,
				ThreadedManagedObject managedObject) throws IOException {
			assertEquals("Incorrect request URI", "/test", connection.getRequest().getUri());
			assertNotSame("Should be different handling thread", thread, Thread.currentThread());
			connection.getResponse().getEntityWriter().write("hello world");
		}
	}

	/**
	 * Ensure can send a single HTTP request.
	 */
	public void testSingleRequest() throws Exception {
		this.startHttpServer(Servicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can send a single HTTPS request.
	 */
	public void testSingleSecureRequest() throws Exception {
		this.startHttpServer(Servicer.class);
		this.doSingleRequest(true);
	}

	/**
	 * Ensure can send multiple HTTP requests.
	 */
	public void testMultipleIndividualRequests() throws Exception {
		this.startHttpServer(Servicer.class);
		for (int i = 0; i < 100; i++) {
			this.doSingleRequest(false);
		}
	}

	/**
	 * Ensure can send multiple HTTPS requests.
	 */
	public void testMultipleIndividualSecureRequests() throws Exception {
		this.startHttpServer(Servicer.class);
		for (int i = 0; i < 100; i++) {
			this.doSingleRequest(true);
		}
	}

	/**
	 * Ensure does not decode characters (allows for routing to work correctly
	 * and not find query string / fragment incorrectly).
	 */
	public void testNotDecodeRequetUrl() throws Exception {
		this.startHttpServer(EncodedUrlServicer.class);
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet(
					this.serverLocation.createClientUrl(false, "/encoded-%3f-%23-+?query=string#fragment")));
			assertEquals("Incorrect status", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect response", "success", HttpClientTestUtil.getEntityBody(response));
		}
	}

	/**
	 * Ensure can handle request with {@link ThreadedServicer}.
	 */
	public void testSingleThreadedHandlerRequest() throws Exception {
		this.startHttpServer(ThreadedServicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can handle request with {@link ThreadedServicer} for a secure
	 * connection.
	 */
	public void testSecureSingleThreadedHandlerRequest() throws Exception {
		this.startHttpServer(ThreadedServicer.class);
		this.doSingleRequest(true);
	}

	/**
	 * Undertakes a single request.
	 */
	private void doSingleRequest(boolean isSecure) throws IOException {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(isSecure)) {
			HttpResponse response = client.execute(new HttpGet(this.serverLocation.createClientUrl(isSecure, "/test")));
			assertEquals("Incorrect status", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect response", "hello world", HttpClientTestUtil.getEntityBody(response));
		}
	}

	/**
	 * Ensure raw secure request.
	 */
	public void testSecureSocket() throws Exception {
		this.startHttpServer(Servicer.class);

		// Create connection to server
		try (Socket socket = OfficeFloorDefaultSslContextSource.createClientSslContext(null).getSocketFactory()
				.createSocket(InetAddress.getLocalHost(), this.serverLocation.getHttpsPort())) {
			socket.setSoTimeout(1000);

			// Send the request
			socket.getOutputStream().write(this.createPipelineRequestData());

			// Obtain the response (may require multiple reads as data comes in)
			byte[] expectedResponseData = this.createPipelineResponseData();
			byte[] actualResponseData = new byte[expectedResponseData.length];
			int totalBytesRead = 0;
			while (totalBytesRead != expectedResponseData.length) {
				int bytesRead = socket.getInputStream().read(actualResponseData, totalBytesRead,
						(expectedResponseData.length - totalBytesRead));
				assertTrue("Must read bytes\n\nExpected: " + new String(expectedResponseData) + "\n\nActual: "
						+ new String(actualResponseData, 0, totalBytesRead), bytesRead > 0);
				totalBytesRead += bytesRead;
			}

			// Ensure correct data
			for (int i = 0; i < expectedResponseData.length; i++) {
				assertEquals(
						"Incorrect response byte " + i + " (of " + expectedResponseData.length + " bytes)\n\nExpected: "
								+ new String(expectedResponseData) + "\n\nActual: " + new String(actualResponseData),
						expectedResponseData[i], actualResponseData[i]);
			}
		}
	}

	/**
	 * Ensure can handle {@link Escalation}.
	 */
	public void testHandleError() throws Exception {
		this.startHttpServer(FailServicer.class);
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet(this.serverLocation.createClientUrl(false, "/test")));

			// Ensure flag as internal server error
			assertEquals("Incorrect status", 500, response.getStatusLine().getStatusCode());

			// Ensure exception in response
			StringWriter content = new StringWriter();
			TEST_EXCEPTION.printStackTrace(new PrintWriter(content));
			String contentText = content.toString();
			assertEquals("Incorrect response", contentText, HttpClientTestUtil.getEntityBody(response));

			// Ensure correct header information
			assertEquals("Incorrect error Content-Length", String.valueOf(contentText.length()),
					response.getFirstHeader("Content-Length").getValue());
			assertEquals("Incorrect error Content-Type", "text/plain",
					response.getFirstHeader("Content-Type").getValue());
		}
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 */
	public void testOfficeFloorPipelining() throws Exception {
		this.doPipeliningTest(true);
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 */
	public void testRawPipelining() throws Exception {
		this.doPipeliningTest(false);
	}

	/**
	 * Undertakes the pipelining test.
	 * 
	 * @param isOfficeFloor
	 *            If is {@link OfficeFloor} HTTP server.
	 */
	private void doPipeliningTest(boolean isOfficeFloor) throws Exception {

		// Start the HTTP server
		if (isOfficeFloor) {
			// Start the OfficeFloor HTTP server
			this.startHttpServer(FastServicer.class);
		} else {
			// Start the Raw HTTP server
			this.momento = this.startRawHttpServer(this.serverLocation);
		}

		// Create pipeline executor
		PipelineExecutor executor = new PipelineExecutor(this.serverLocation.getHttpPort());

		// Do warm up
		executor.doPipelineRun(100000).printResult(this.getName() + " WARMUP");

		// Undertake performance run
		PipelineResult result = executor.doPipelineRun(1000000);
		result.printResult(this.getName() + " RUN");

		// Load for comparison
		CompareResult.setResult("pipelining", isOfficeFloor, result);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 */
	public void testOfficeFloorThreadedHandler() throws Exception {
		this.doThreadedHandlerTest(true);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 */
	public void testRawThreadedHandler() throws Exception {
		this.doThreadedHandlerTest(false);
	}

	/**
	 * Undertakes the threaded handler test.
	 * 
	 * @param isOfficeFloor
	 *            If is {@link OfficeFloor} HTTP server.
	 */
	private void doThreadedHandlerTest(boolean isOfficeFloor) throws Exception {

		// Start the HTTP server
		if (isOfficeFloor) {
			// Start the OfficeFloor HTTP server
			this.startHttpServer(ThreadedServicer.class);
		} else {
			// Start the Raw HTTP server
			this.momento = this.startRawHttpServer(this.serverLocation);
		}

		// Create pipeline executor
		PipelineExecutor executor = new PipelineExecutor(this.serverLocation.getHttpPort());

		// Do warm up
		executor.doPipelineRun(1000).printResult(this.getName() + " WARMUP");

		// Undertake performance run
		PipelineResult result = executor.doPipelineRun(10000);
		result.printResult(this.getName() + " RUN");

		// Load for comparison
		CompareResult.setResult("threaded-handler", isOfficeFloor, result);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 */
	public void testOfficeFloorHeavyLoad() throws Exception {
		this.doHeavyLoadTest(true);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 */
	public void testRawHeavyLoad() throws Exception {
		this.doHeavyLoadTest(false);
	}

	/**
	 * Undertakes the heavy load test.
	 * 
	 * @param isOfficeFloor
	 *            Indicates if {@link OfficeFloor}.
	 */
	private void doHeavyLoadTest(boolean isOfficeFloor) throws Exception {
		// CPU for client and server
		int clientCount = Runtime.getRuntime().availableProcessors() / 2;
		if (clientCount == 0) {
			clientCount = 1; // ensure at least one client
		}
		this.doMultiClientLoadTest(isOfficeFloor, clientCount, 1000000, "Heavy Load (" + clientCount + " clients)");
	}

	/**
	 * Ensure can service overload requests pipelined.
	 */
	public void testOfficeFloorOverLoad() throws Exception {
		this.doOverLoadTest(true);
	}

	/**
	 * Ensure can service over requests pipelined.
	 */
	public void testRawOverLoad() throws Exception {
		this.doOverLoadTest(false);
	}

	/**
	 * Undertakes the over load test.
	 * 
	 * @param isOfficeFloor
	 *            Indicates if {@link OfficeFloor}.
	 */
	private void doOverLoadTest(boolean isOfficeFloor) throws Exception {
		int clientCount = Runtime.getRuntime().availableProcessors() * 4;
		this.doMultiClientLoadTest(isOfficeFloor, clientCount, 100000, "Over Load (" + clientCount + " clients)");
	}

	/**
	 * Undertakes the multi-client pipelining test.
	 * 
	 * @param isOfficeFloor
	 *            IF is {@link OfficeFloor} HTTP server.
	 * @param clientCount
	 *            Number of simultaneous clients.
	 * @param requestCount
	 *            Number of requests per client.
	 * @param resultName
	 *            Name of result fo comparison.
	 */
	public void doMultiClientLoadTest(boolean isOfficeFloor, int clientCount, int requestCount, String resultName)
			throws Exception {

		// Start the HTTP server
		if (isOfficeFloor) {
			// Start the OfficeFloor HTTP server
			this.startHttpServer(FastServicer.class);
		} else {
			// Start the Raw HTTP server
			this.momento = this.startRawHttpServer(this.serverLocation);
		}

		// Indicate details of test
		System.out.println("========= " + (isOfficeFloor ? "OfficeFloor" : "Raw") + " " + resultName + " =========");

		// Create the pipeline executors
		@SuppressWarnings("unchecked")
		PipelineExecutor[] executors = new AbstractHttpServerImplementationTest.PipelineExecutor[clientCount];
		for (int i = 0; i < clientCount; i++) {
			executors[i] = new PipelineExecutor(this.serverLocation.getHttpPort());
		}

		// Do warm up
		executors[0].doPipelineRun(requestCount).printResult(this.getName() + " WARMUP");

		// Run the executors
		for (PipelineExecutor executor : executors) {
			new Thread(executor.getRunnable(requestCount)).start();
		}

		// Wait for completion of run
		PipelineResult[] results = new PipelineResult[clientCount];
		for (int i = 0; i < clientCount; i++) {
			results[i] = executors[i].waitForCompletion();
		}

		// Provide summary of results
		long minStartTime = results[0].startTime;
		long maxEndTime = results[0].endTime;
		int totalRequests = 0;
		for (int i = 0; i < clientCount; i++) {
			PipelineResult result = results[i];
			result.printResult(this.getName() + " CLIENT-" + i);
			minStartTime = (result.startTime < minStartTime) ? result.startTime : minStartTime;
			maxEndTime = (result.endTime > maxEndTime) ? result.endTime : maxEndTime;
			totalRequests += result.requestCount;
		}
		PipelineResult result = new PipelineResult(minStartTime, maxEndTime, totalRequests);
		result.printResult(this.getName() + " TOTAL");

		// Provide results for comparison
		CompareResult.setResult(resultName, isOfficeFloor, result);
	}

	/**
	 * Creates the pipeline request data.
	 * 
	 * @return Pipeline request data.
	 */
	private byte[] createPipelineRequestData() {
		StringBuilder request = new StringBuilder();
		request.append("GET /test HTTP/1.1\n");
		request.append("test: value\n");
		request.append("\n");
		return UsAsciiUtil.convertToHttp(request.toString());
	}

	/**
	 * Creates the pipeline response data.
	 * 
	 * @return Pipeline response data.
	 */
	private byte[] createPipelineResponseData() {
		HttpHeader[] responseHeaders = this.getServerResponseHeaderValues();
		StringBuilder response = new StringBuilder();
		response.append("HTTP/1.1 200 OK\n");
		for (HttpHeader header : responseHeaders) {
			switch (header.getName().toLowerCase()) {
			case "content-length":
				response.append(header.getName() + ": 11\n");
				break;
			case "content-type":
				response.append(header.getName() + ": text/plain\n");
				break;
			default:
				response.append(header.getName() + ": " + header.getValue() + "\n");
				break;
			}
		}
		response.append("\n");
		response.append("hello world");
		return UsAsciiUtil.convertToHttp(response.toString());
	}

	/**
	 * Executes request in a pipeline for performance testing.
	 */
	private class PipelineExecutor {

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
		 * @throws IOException
		 *             If fails to connect to {@link HttpServer}.
		 */
		private PipelineExecutor(int port) throws IOException {

			// Pipeline requests to server
			this.selector = Selector.open();
			this.channel = SocketChannel.open(new InetSocketAddress("localhost", port));
			this.channel.configureBlocking(false);
			Socket socket = this.channel.socket();
			socket.setSendBufferSize(10 * 1024 * 1024);
			socket.setReceiveBufferSize(10 * 1024 * 1024);
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
			byte[] requestData = AbstractHttpServerImplementationTest.this.createPipelineRequestData();
			ByteBuffer requestBuffer = ByteBuffer.allocateDirect(requestData.length);
			requestBuffer.put(requestData);
			requestBuffer.flip();

			// Create the expected response
			byte[] responseData = AbstractHttpServerImplementationTest.this.createPipelineResponseData();

			// Initiate run variables
			int requestDataSent = 0;
			int requestSentCount = 0;
			int responseReceivedCount = 0;
			ByteBuffer responseBuffer = ByteBuffer.allocateDirect(1024);
			int responseDataPosition = 0;

			// Record start time
			long startTime = System.currentTimeMillis();

			// Start pipeline run
			long noDataStart = -1;
			while (responseReceivedCount < requestCount) {

				// Stop interest in writing if all requests sent
				if ((requestSentCount >= requestCount)
						&& (this.selectionKey.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
					// All requests written
					System.out.println(
							"All requests written (awaiting " + (requestCount - responseReceivedCount) + " responses)");
					this.selectionKey.interestOps(SelectionKey.OP_READ);
				}

				// Wait for some data
				this.selector.select(1000);
				if (this.selector.selectedKeys().size() == 0) {
					fail("Timed out waiting for response");
				}

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

						// Determine if time out
						if (bytesRead > 0) {
							// Have bytes, so reset
							noDataStart = -1;
						} else if (bytesRead < 0) {
							fail("Connection closed");
						} else {
							// No data
							if (noDataStart == -1) {
								// Capture start time of no data
								noDataStart = System.currentTimeMillis();
							} else if ((System.currentTimeMillis() - noDataStart) > (10 * 1000)) {
								fail("Timed out waiting on data");
							}
						}

						// Handle the data
						responseBuffer.flip();
						responseBuffer.mark();
						for (int i = 0; i < bytesRead; i++) {
							byte expectedCharacter = responseData[responseDataPosition];
							byte actualCharacter = responseBuffer.get();
							if (expectedCharacter != actualCharacter) {

								// Obtain the text
								byte[] responseBytes = new byte[bytesRead];
								responseBuffer.reset();
								responseBuffer.get(responseBytes);
								StringBuilder responseText = new StringBuilder(
										UsAsciiUtil.convertToString(responseBytes));

								// Add in quotes to identify incorrect
								// character
								responseText.insert(i, "{");
								responseText.insert(i + 2, "}");

								// Provide the error
								assertEquals("Incorrect character " + i + " of response " + responseReceivedCount + " ("
										+ UsAsciiUtil.convertToChar(expectedCharacter) + " != "
										+ UsAsciiUtil.convertToChar(actualCharacter) + "): " + responseText.toString(),
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
		 * Creates {@link Runnable} to run within another {@link Thread}.
		 * 
		 * @param requestCount
		 *            Request count.
		 * @return {@link Runnable}.
		 */
		private Runnable getRunnable(int requestCount) {
			return new Runnable() {
				@Override
				public void run() {
					try {
						// Undertake run
						PipelineExecutor.this.runResult = PipelineExecutor.this.doPipelineRun(requestCount);

					} catch (Throwable ex) {
						// Capture failure
						synchronized (PipelineExecutor.this) {
							PipelineExecutor.this.runResult = ex;
						}
					}

					// Notify complete
					synchronized (PipelineExecutor.this) {
						PipelineExecutor.this.notify();
					}
				}
			};
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
			this.wait(30 * 1000);
			if (this.runResult == null) {
				fail("Timed out waiting on pipeline completion");
			}

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

		private long getRunTime() {
			return this.endTime - this.startTime;
		}

		private int getRequestsPerSecond() {
			return (int) ((this.requestCount) / (((float) this.getRunTime()) / 1000.0));
		}

		private void printResult(String prefix) {
			long runTime = this.getRunTime();
			int requestsPerSecond = this.getRequestsPerSecond();
			System.out.println(prefix + " ran " + this.requestCount + " requests in " + runTime + " milliseconds ("
					+ requestsPerSecond + " per second)");
		}
	}

	/**
	 * Compares results of {@link OfficeFloor} servicing against Raw servicing.
	 */
	private static class CompareResult {

		private static Class<?> testClass;

		private static Map<Class<?>, Map<String, CompareResult>> results = new HashMap<>();

		private static void reset(Class<?> testClazz) {
			testClass = testClazz;
		}

		private static void setResult(String prefix, boolean isOfficeFloorResult, PipelineResult pipelineResult) {

			// Obtain the compare result
			Map<String, CompareResult> testResults = results.get(testClass);
			if (testResults == null) {
				testResults = new HashMap<>();
				results.put(testClass, testResults);
			}
			CompareResult result = testResults.get(prefix);
			if (result == null) {
				result = new CompareResult();
				testResults.put(prefix, result);
			}

			// Load the pipeline result
			if (isOfficeFloorResult) {
				result.officeFloorResult = pipelineResult;
			} else {
				result.rawResult = pipelineResult;
			}

			// Determine if have both values
			if (result.officeFloorResult == null) {
				return; // missing OfficeFloor result
			}
			if (result.rawResult == null) {
				return; // missing Raw result
			}

			// Have both values, so print comparison
			final String format = "%1$15s";
			StringWriter message = new StringWriter();
			PrintWriter out = new PrintWriter(message);
			out.println("============================================================");
			out.println(testClass.getSimpleName() + " - " + prefix);
			out.println();

			// Output headers
			out.print(String.format(format, ""));
			out.print(String.format(format, "OfficeFloor"));
			out.print(String.format(format, "Raw"));
			out.println(String.format(format, "Difference"));

			// Output the run time
			out.print(String.format(format, "Run time (ms)"));
			out.print(String.format(format, String.valueOf(result.officeFloorResult.getRunTime())));
			out.print(String.format(format, String.valueOf(result.rawResult.getRunTime())));
			out.println(String.format(format,
					String.valueOf(result.officeFloorResult.getRunTime() - result.rawResult.getRunTime())));

			// Output the requests per second
			out.print(String.format(format, "Requests/Sec"));
			out.print(String.format(format, String.valueOf(result.officeFloorResult.getRequestsPerSecond())));
			out.print(String.format(format, String.valueOf(result.rawResult.getRequestsPerSecond())));
			out.println(String.format(format, String.valueOf(
					result.rawResult.getRequestsPerSecond() - result.officeFloorResult.getRequestsPerSecond())));

			// Overhead increase
			out.println();
			out.print(String.format(format, "Overhead (%)"));
			out.print(String.format(format, ""));
			out.print(String.format(format, ""));
			long runtimeDifference = result.officeFloorResult.getRunTime() - result.rawResult.getRunTime();
			long maxRunTime = Math.max(result.officeFloorResult.getRunTime(), result.rawResult.getRunTime());
			float difference = (float) runtimeDifference / (float) maxRunTime;
			out.println(String.format(format, String.valueOf(difference)));

			out.println("============================================================");
			out.flush();
			System.out.println(message.toString());
		}

		private PipelineResult officeFloorResult = null;

		private PipelineResult rawResult = null;
	}

}