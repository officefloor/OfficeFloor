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
package net.officefloor.server.http;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.net.SocketFactory;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import junit.framework.TestCase;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorExtension;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.stream.TemporaryFiles;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;

/**
 * Abstract {@link TestCase} for testing a {@link HttpServerImplementation}.
 * 
 * @param <M> Type of momento from raw HTTP server.
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpServerImplementationTest<M> extends OfficeFrameTestCase {

	/**
	 * Time out on waiting for data.
	 */
	private static final long WAIT_FOR_DATA_TIMEOUT = 20 * 1000;

	/**
	 * Time out on waiting for shutdown.
	 */
	private static final long WAIT_FOR_SHUTDOWN_TIMEOUT = 120 * 1000;

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation serverLocation = new HttpServerLocationImpl();

	/**
	 * Creates a new {@link HttpHeader}.
	 * 
	 * @param name  {@link HttpHeader} name.
	 * @param value {@link HttpHeader} value.
	 * @return New {@link HttpHeader}.
	 */
	protected static HttpHeader newHttpHeader(String name, String value) {
		return new SerialisableHttpHeader(name, value);
	}

	/**
	 * Test {@link Exception} to be thrown for testing handling {@link Escalation}.
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
	 * performance comparisons of adding in {@link OfficeFloor} servicing overheads.
	 * <p>
	 * The raw implementation is to return "hello world" in UTF-8 encoding for the
	 * response entity.
	 * 
	 * @param serverLocation {@link HttpServerLocation}.
	 * @return Momento to provide to stopping the server.
	 * @throws Exception If fails to start the raw HTTP server.
	 */
	protected abstract M startRawHttpServer(HttpServerLocation serverLocation) throws Exception;

	/**
	 * Stops the raw implementation.
	 * 
	 * @param momento Momento provided from starting the raw HTTP server.
	 * @throws Exception If fails to stop the raw HTTP server.
	 */
	protected abstract void stopRawHttpServer(M momento) throws Exception;

	/**
	 * Obtains the server response {@link HttpHeader} instances in the order they
	 * are sent from the server.
	 * 
	 * @return Server response {@link HttpHeader} instances in the order they are
	 *         sent from the server.
	 */
	protected abstract HttpHeader[] getServerResponseHeaderValues();

	/**
	 * Obtains the <code>Server</code> {@link HttpHeaderValue} suffix.
	 * 
	 * @return <code>Server</code> {@link HttpHeaderValue} suffix. May be
	 *         <code>null</code> if no suffix.
	 */
	protected abstract String getServerNameSuffix();

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
		System.out.println("START: " + this.getName());
		super.setUp();

		// Log GC
		this.setLogGC();

		// Reset the compare results
		CompareResult.reset(this.getClass());
	}

	@Override
	protected void tearDown() throws Exception {
		System.out.println("SHUTDOWN: " + this.getName());

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
			assertTrue("Incorrect connection error: " + ex.getMessage(), ex.getMessage().startsWith("Connect to ")
					&& ex.getMessage().contains("failed: Connection refused"));
		}

		// Remaining tear down
		super.tearDown();
		System.out.println("END: " + this.getName());
	}

	/**
	 * Starts the {@link HttpServer}.
	 * 
	 * @param sectionServicer {@link Class} of the {@link ClassSectionSource} to
	 *                        service the {@link HttpRequest}.
	 * @throws Exception If fails to start the {@link HttpServer}.
	 */
	protected void startHttpServer(Class<?> sectionServicer) throws Exception {
		startHttpServer(sectionServicer, null);
	}

	/**
	 * Starts the {@link HttpServer}.
	 * 
	 * @param sectionServicer {@link Class} of the {@link ClassSectionSource} to
	 *                        service the {@link HttpRequest}.
	 * @param extension       Additional {@link CompileOfficeFloorExtension}. May be
	 *                        <code>null</code>.
	 * @throws Exception If fails to start the {@link HttpServer}.
	 */
	protected void startHttpServer(Class<?> sectionServicer, CompileOfficeFloorExtension extension) throws Exception {

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
		if (extension != null) {
			compile.officeFloor(extension);
		}
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

	public static class BytesServicer {
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

	public static class BufferServicer {
		private static final ByteBuffer HELLO_WORLD;
		static {
			byte[] data = BytesServicer.HELLO_WORLD;
			HELLO_WORLD = ByteBuffer.allocate(data.length);
			HELLO_WORLD.put(data);
			HELLO_WORLD.flip();
		}

		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("Incorrect request URI", "/test", connection.getRequest().getUri());
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.setContentType(BytesServicer.TEXT_PLAIN, null);
			response.getEntity().write(HELLO_WORLD.duplicate());
		}
	}

	private static final FileChannel HELLO_WORLD_FILE;

	static {
		try {
			HELLO_WORLD_FILE = TemporaryFiles.getDefault().createTempFile("HelloWorld", BytesServicer.HELLO_WORLD);
		} catch (IOException ex) {
			throw fail(ex);
		}
	}

	public static class FileServicer {
		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("Incorrect request URI", "/test", connection.getRequest().getUri());
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.setContentType(BytesServicer.TEXT_PLAIN, null);
			response.getEntity().write(HELLO_WORLD_FILE, null);
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

	public static class FunctionalityServicer {
		public void service(ServerHttpConnection connection) throws IOException {

			// Assert has all content of request
			HttpRequest request = connection.getRequest();
			assertEquals("Incorrect method", HttpMethod.POST, request.getMethod());
			assertEquals("Incorrect request URI", "/functionality", request.getUri());
			assertEquals("Incorrect version", HttpVersion.HTTP_1_1, request.getVersion());
			StringBuilder allHeaders = new StringBuilder();
			allHeaders.append("\n");
			for (HttpHeader header : request.getHeaders()) {
				allHeaders.append("\n\t" + header.getName() + "=" + header.getValue());
			}
			allHeaders.append("\n");
			assertEquals("Incorrect number of headers (with extra by HTTP client):" + allHeaders.toString(), 8,
					request.getHeaders().length());
			assertEquals("Incorrect header", "header", request.getHeaders().getHeader("request").getValue());
			assertEquals("Incorrect number of cookies", 1, request.getCookies().length());
			assertEquals("Incorrect cookie", "cookie", request.getCookies().getCookie("request").getValue());
			assertEquals("Incorrect entity", "request", MockHttpServer.getContent(request, null));

			// Send a full response
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.setStatus(HttpStatus.OK);
			response.setContentType("text/test", null);
			response.getHeaders().addHeader("response", "header");
			response.getCookies().setCookie("response", "cookie");
			response.getEntityWriter().write("response");
		}
	}

	/**
	 * Ensure able to send all details and receive all details.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testFunctionality() throws Exception {
		this.startHttpServer(FunctionalityServicer.class);
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(false)) {
			HttpPost post = new HttpPost(this.serverLocation.createClientUrl(false, "/functionality"));
			post.addHeader("request", "header");
			post.addHeader("cookie", "request=cookie");
			post.setEntity(new StringEntity("request"));
			HttpResponse response = client.execute(post);

			// Validate the response
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Incorrect status: " + entity, 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect version: " + entity, new ProtocolVersion("HTTP", 1, 1),
					response.getStatusLine().getProtocolVersion());
			StringBuilder allHeaders = new StringBuilder();
			for (Header header : response.getAllHeaders()) {
				allHeaders.append("\n\t" + header.getName() + "=" + header.getValue());
			}
			Set<String> expectedHeaders = this.getUniqueResponseHeaderNames("header", "Set-Cookie", "Content-Type",
					"Content-Length");
			assertEquals("Incorrect number of headers:" + allHeaders.toString() + "\n\n" + expectedHeaders + "\n\n",
					expectedHeaders.size(), response.getAllHeaders().length);
			assertEquals("Incorrect header:" + allHeaders.toString(), "header",
					response.getFirstHeader("response").getValue());
			assertEquals("Incorrect cookie:" + allHeaders.toString(), "response=cookie",
					response.getFirstHeader("set-cookie").getValue());
			assertEquals("Incorrect Content-Type", "text/test", response.getFirstHeader("Content-Type").getValue());
			assertEquals("Incorrect entity", "response", entity);
		}
	}

	/**
	 * Ensure can send a single HTTP request.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSingleRequest() throws Exception {
		this.startHttpServer(Servicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can send a single HTTPS request.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSingleSecureRequest() throws Exception {
		this.startHttpServer(Servicer.class);
		this.doSingleRequest(true);
	}

	/**
	 * Ensure can send a single HTTP {@link ByteBuffer} response.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSingleBufferRequest() throws Exception {
		this.startHttpServer(BufferServicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can send a single HTTPS {@link ByteBuffer} response.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSingleSecureBufferRequest() throws Exception {
		this.startHttpServer(BufferServicer.class);
		this.doSingleRequest(true);
	}

	/**
	 * Ensure can send a single HTTP {@link FileBuffer} response.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSingleFileRequest() throws Exception {
		this.startHttpServer(FileServicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can send a single HTTPS {@link FileBuffer} response.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSingleSecureFileRequest() throws Exception {
		this.startHttpServer(FileServicer.class);
		this.doSingleRequest(true);
	}

	/**
	 * Ensure send Server and Date {@link HttpHeader} values.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testServerDateHeaders() throws Exception {
		new SystemPropertiesRule(HttpServer.PROPERTY_HTTP_SERVER_NAME, "OfficeFloorServer",
				HttpServer.PROPERTY_HTTP_DATE_HEADER, "true").run(() -> {

					// Obtain the server name
					String serverNameSuffix = this.getServerNameSuffix();
					String serverName = "OfficeFloorServer" + (serverNameSuffix == null ? "" : " " + serverNameSuffix);

					// Should setup server with System properties
					this.startHttpServer(BufferServicer.class);
					this.doSingleRequest(false, (response) -> {
						assertEquals("Incorrect Server HTTP header", serverName,
								response.getFirstHeader("Server").getValue());
						assertNotNull("Should have Date HTTP header", response.getFirstHeader("Date"));
					});
				});
	}

	/**
	 * Ensure closes the {@link FileChannel} on write.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSingleCloseFileRequest() throws Exception {
		CLOSE_FILE = TemporaryFiles.getDefault().createTempFile("testSingleCloseFileRequest",
				BytesServicer.HELLO_WORLD);
		this.startHttpServer(CloseFileServicer.class);
		this.doSingleRequest(false);
		assertFalse("File should be closed", CLOSE_FILE.isOpen());
	}

	/**
	 * Ensure closes the {@link FileChannel} on write.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSingleSecureCloseFileRequest() throws Exception {
		CLOSE_FILE = TemporaryFiles.getDefault().createTempFile("testSingleSecureCloseFileRequest",
				BytesServicer.HELLO_WORLD);
		this.startHttpServer(CloseFileServicer.class);
		this.doSingleRequest(true);
		assertFalse("File should be closed", CLOSE_FILE.isOpen());
	}

	private volatile static FileChannel CLOSE_FILE = null;

	public static class CloseFileServicer {
		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("Incorrect request URI", "/test", connection.getRequest().getUri());
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.setContentType(BytesServicer.TEXT_PLAIN, null);
			response.getEntity().write(CLOSE_FILE, 0, 6, (file, isWritten) -> {
				assertTrue("File should be written", isWritten);
				file.close();
			});
			response.getEntity().write(BytesServicer.HELLO_WORLD, 6, BytesServicer.HELLO_WORLD.length - 6);
		}
	}

	/**
	 * Ensure can send multiple HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testMultipleIndividualRequests() throws Exception {
		this.startHttpServer(Servicer.class);
		for (int i = 0; i < 100; i++) {
			this.doSingleRequest(false);
		}
	}

	/**
	 * Ensure can send multiple HTTPS requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testMultipleIndividualSecureRequests() throws Exception {
		this.startHttpServer(Servicer.class);
		for (int i = 0; i < 100; i++) {
			this.doSingleRequest(true);
		}
	}

	/**
	 * Ensure does not decode characters (allows for routing to work correctly and
	 * not find query string / fragment incorrectly).
	 * 
	 * @throws Exception If test failure.
	 */
	public void testNotDecodeRequestUrl() throws Exception {
		this.startHttpServer(EncodedUrlServicer.class);
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet(
					this.serverLocation.createClientUrl(false, "/encoded-%3f-%23-+?query=string#fragment")));
			assertEquals("Incorrect status", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect response", "success", HttpClientTestUtil.entityToString(response));
		}
	}

	/**
	 * Ensure can handle request with {@link ThreadedServicer}.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSingleThreadedHandlerRequest() throws Exception {
		this.startHttpServer(ThreadedServicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can handle request with {@link ThreadedServicer} for a secure
	 * connection.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSecureSingleThreadedHandlerRequest() throws Exception {
		this.startHttpServer(ThreadedServicer.class);
		this.doSingleRequest(true);
	}

	/**
	 * Undertakes a single request.
	 */
	private void doSingleRequest(boolean isSecure) throws IOException {
		this.doSingleRequest(isSecure, null);
	}

	/**
	 * Undertakes a single request.
	 */
	private void doSingleRequest(boolean isSecure, Consumer<HttpResponse> validator) throws IOException {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(isSecure)) {
			HttpResponse response = client.execute(new HttpGet(this.serverLocation.createClientUrl(isSecure, "/test")));
			assertEquals("Incorrect status", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect response", "hello world", HttpClientTestUtil.entityToString(response));
			if (validator != null) {
				validator.accept(response);
			}
		}
	}

	/**
	 * Ensure raw request.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSocket() throws Exception {
		this.doSocketTest(false);
	}

	/**
	 * Ensure raw secure request.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testSecureSocket() throws Exception {
		this.doSocketTest(true);
	}

	/**
	 * Undertakes the raw socket test.
	 * 
	 * @param isSecure If secure.
	 * @throws Exception If test failure.
	 */
	private void doSocketTest(boolean isSecure) throws Exception {
		this.startHttpServer(Servicer.class);

		// Create connection to server
		try (Socket socket = (isSecure
				? OfficeFloorDefaultSslContextSource.createClientSslContext(null).getSocketFactory()
						.createSocket(InetAddress.getLocalHost(), this.serverLocation.getHttpsPort())
				: SocketFactory.getDefault().createSocket(InetAddress.getLocalHost(),
						this.serverLocation.getHttpPort()))) {

			// Send the request
			socket.getOutputStream().write(this.createPipelineRequestData());

			// Obtain the response (may require multiple reads as data comes in)
			byte[] expectedResponseData = this.createPipelineResponseData();
			byte[] actualResponseData = new byte[expectedResponseData.length];
			int totalBytesRead = 0;
			while (totalBytesRead < expectedResponseData.length) {
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
	 * 
	 * @throws Exception If test failure.
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
			assertEquals("Incorrect response", contentText, HttpClientTestUtil.entityToString(response));

			// Ensure correct header information
			assertEquals("Incorrect error Content-Length", String.valueOf(contentText.length()),
					response.getFirstHeader("Content-Length").getValue());
			assertEquals("Incorrect error Content-Type", "text/plain",
					response.getFirstHeader("Content-Type").getValue());
		}
	}

	/**
	 * Ensure can handle pressure overloading the server.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testTeamPressureOverload() throws Exception {
		this.startHttpServer(PressureOverloadServicer.class, (context) -> {
			context.addManagedObject("MARKER", TeamMarker.class, ManagedObjectScope.THREAD);
			context.getOfficeFloorDeployer().addTeam("TEAM", ExecutorFixedTeamSource.class.getName());
		});
		PipelineExecutor executor = new PipelineExecutor(this.serverLocation.getHttpPort());
		executor.doPipelineRun(100000);
	}

	/**
	 * Marker {@link ManagedObject} for identifying {@link Team}.
	 */
	public static class TeamMarker {
	}

	/**
	 * Servicer that is slow causing significant back pressure.
	 */
	public static class PressureOverloadServicer {

		@NextFunction("backPressure")
		public Thread service() {
			return Thread.currentThread();
		}

		public void backPressure(@Parameter Thread serviceThread, ServerHttpConnection connection, TeamMarker marker)
				throws Exception {

			// Ensure using team
			assertNotEquals("Should be using team", serviceThread, Thread.currentThread());

			// Sleep some time to cause back pressure
			Thread.sleep(1000);

			// Provide response
			connection.getResponse().getEntityWriter().write("hello world");
		}
	}

	/**
	 * Verify pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testRawPipelineVerify() throws Exception {
		this.doPipelineVerifyTest(null);
	}

	/**
	 * Verify pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testBytesPipelineVerify() throws Exception {
		this.doPipelineVerifyTest(BytesServicer.class);
	}

	/**
	 * Verify pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testBufferPipelineVerify() throws Exception {
		this.doPipelineVerifyTest(BufferServicer.class);
	}

	/**
	 * Verify pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	public void testFilePipelineVerify() throws Exception {
		this.doPipelineVerifyTest(FileServicer.class);
	}

	/**
	 * Undertakes verifying the pipeline tests.
	 * 
	 * @param servicerClass Servicer {@link Class}.
	 * @throws Exception If fails test.
	 */
	public void doPipelineVerifyTest(Class<?> servicerClass) throws Exception {
		this.startAppropriateHttpServer(servicerClass);
		PipelineExecutor executor = new PipelineExecutor(this.serverLocation.getHttpPort());
		executor.doPipelineRun(10);
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testRawPipelining() throws Exception {
		this.doPipeliningTest(null);
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testBytesPipelining() throws Exception {
		this.doPipeliningTest(BytesServicer.class);
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testBufferPipelining() throws Exception {
		this.doPipeliningTest(BufferServicer.class);
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testFilePipelining() throws Exception {
		this.doPipeliningTest(FileServicer.class);
	}

	/**
	 * Undertakes the pipelining test.
	 * 
	 * @param servicerClass Servicer {@link Class}.
	 */
	private void doPipeliningTest(Class<?> servicerClass) throws Exception {

		// Start the HTTP server
		this.startAppropriateHttpServer(servicerClass);

		// Create pipeline executor
		PipelineExecutor executor = new PipelineExecutor(this.serverLocation.getHttpPort());

		// Do warm up
		executor.doPipelineRun(100000).printResult(this.getName() + " WARMUP");

		// Undertake performance run
		PipelineResult result = executor.doPipelineRun(1000000);
		result.printResult(this.getName() + " RUN");

		// Load for comparison
		CompareResult.setResult("pipelining", servicerClass, result);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testRawThreadedHandler() throws Exception {
		this.doThreadedHandlerTest(null);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testBytesThreadedHandler() throws Exception {
		this.doThreadedHandlerTest(BytesServicer.class);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testBufferThreadedHandler() throws Exception {
		this.doThreadedHandlerTest(BufferServicer.class);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testFileThreadedHandler() throws Exception {
		this.doThreadedHandlerTest(FileServicer.class);
	}

	/**
	 * Undertakes the threaded handler test.
	 * 
	 * @param servicerClass Servicer {@link Class}.
	 */
	private void doThreadedHandlerTest(Class<?> servicerClass) throws Exception {

		// Start the HTTP server
		this.startAppropriateHttpServer(servicerClass);

		// Create pipeline executor
		PipelineExecutor executor = new PipelineExecutor(this.serverLocation.getHttpPort());

		// Do warm up
		executor.doPipelineRun(1000).printResult(this.getName() + " WARMUP");

		// Undertake performance run
		PipelineResult result = executor.doPipelineRun(10000);
		result.printResult(this.getName() + " RUN");

		// Load for comparison
		CompareResult.setResult("threaded-handler", servicerClass, result);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testRawHeavyLoad() throws Exception {
		this.doHeavyLoadTest(null);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testBytesHeavyLoad() throws Exception {
		this.doHeavyLoadTest(BytesServicer.class);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testBufferHeavyLoad() throws Exception {
		this.doHeavyLoadTest(BufferServicer.class);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testFileHeavyLoad() throws Exception {
		this.doHeavyLoadTest(FileServicer.class);
	}

	/**
	 * Undertakes the heavy load test.
	 * 
	 * @param servicerClass Servicer {@link Class}.
	 */
	private void doHeavyLoadTest(Class<?> servicerClass) throws Exception {
		// CPU for client and server
		int clientCount = Runtime.getRuntime().availableProcessors() / 2;
		if (clientCount == 0) {
			clientCount = 1; // ensure at least one client
		}
		this.doMultiClientLoadTest(servicerClass, clientCount, 1000000, "Heavy Load (" + clientCount + " clients)");
	}

	/**
	 * Ensure can service over requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testRawOverLoad() throws Exception {
		this.doOverLoadTest(null);
	}

	/**
	 * Ensure can service overload requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testBytesOverLoad() throws Exception {
		this.doOverLoadTest(BytesServicer.class);
	}

	/**
	 * Ensure can service overload requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testBufferOverLoad() throws Exception {
		this.doOverLoadTest(BufferServicer.class);
	}

	/**
	 * Ensure can service overload requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void testFileOverLoad() throws Exception {
		this.doOverLoadTest(FileServicer.class);
	}

	/**
	 * Undertakes the over load test.
	 * 
	 * @param servicerClass Servicer {@link Class}.
	 */
	private void doOverLoadTest(Class<?> servicerClass) throws Exception {
		int clientCount = Runtime.getRuntime().availableProcessors() * 4;
		this.doMultiClientLoadTest(servicerClass, clientCount, 100000, "Over Load (" + clientCount + " clients)");
	}

	/**
	 * Starts the appropriate server for the servicer.
	 * 
	 * @param servicerClass Servicer {@link Class}.
	 */
	private void startAppropriateHttpServer(Class<?> servicerClass) throws Exception {

		// Create set of servicer
		Set<Class<?>> servicerClasses = new HashSet<>(
				Arrays.asList(BytesServicer.class, BufferServicer.class, FileServicer.class));

		// Start the HTTP server
		if (servicerClass == null) {
			// Start the Raw HTTP server
			this.momento = this.startRawHttpServer(this.serverLocation);
		} else if (servicerClasses.contains(servicerClass)) {
			// Start the OfficeFloor HTTP server
			this.startHttpServer(servicerClass);
		} else {
			fail("Invalid servicer " + servicerClass.getName());
		}
	}

	/**
	 * Undertakes the multi-client pipelining test.
	 * 
	 * @param servicerClass Servicer {@link Class}.
	 * @param clientCount   Number of simultaneous clients.
	 * @param requestCount  Number of requests per client.
	 * @param resultName    Name of result for comparison.
	 * @throws Exception If test failure.
	 */
	public void doMultiClientLoadTest(Class<?> servicerClass, int clientCount, int requestCount, String resultName)
			throws Exception {

		// Start the HTTP server
		this.startAppropriateHttpServer(servicerClass);

		// Indicate details of test
		System.out.println("========= " + (servicerClass == null ? "Raw" : servicerClass.getSimpleName()) + " "
				+ resultName + " =========");

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
		CompareResult.setResult(resultName, servicerClass, result);
	}

	/**
	 * Creates the pipeline request data.
	 * 
	 * @return Pipeline request data.
	 */
	private byte[] createPipelineRequestData() {
		StringBuilder request = new StringBuilder();
		request.append("GET /test HTTP/1.1\n");
		request.append("host: test\n");
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
	 * Obtains the {@link Set} of expected response {@link HttpHeader} instances.
	 * 
	 * @param additionalHeaderNames Additional {@link HttpHeader} names expected in
	 *                              the response.
	 * @return {@link Set} of expected response {@link HttpHeader} instances.
	 */
	private Set<String> getUniqueResponseHeaderNames(String... additionalHeaderNames) {
		Set<String> names = new HashSet<>();
		for (String additionalHeaderName : additionalHeaderNames) {
			names.add(additionalHeaderName.toLowerCase());
		}
		for (HttpHeader header : this.getServerResponseHeaderValues()) {
			names.add(header.getName().toLowerCase());
		}
		return names;
	}

	/**
	 * Executes request in a pipeline for performance testing.
	 */
	private class PipelineExecutor {

		/**
		 * Local host port to send requests.
		 */
		private final int port;

		/**
		 * {@link Selector}.
		 */
		private Selector selector;

		/**
		 * {@link SocketChannel}.
		 */
		private SocketChannel channel;

		/**
		 * {@link SelectionKey}.
		 */
		private SelectionKey selectionKey;

		/**
		 * Result of {@link Runnable}.
		 */
		private Object runResult = null;

		/**
		 * Instantiate.
		 * 
		 * @param port Port for the {@link HttpServer}.
		 * @throws IOException If fails to connect to {@link HttpServer}.
		 */
		private PipelineExecutor(int port) throws IOException {
			this.port = port;

			// Register
			AbstractHttpServerImplementationTest.this.executors.add(this);
		}

		/**
		 * Closes this {@link PipelineExecutor}.
		 */
		private void close() throws IOException {
			if (this.selector != null) {
				this.selector.close();
			}
			if (this.channel != null) {
				this.channel.close();
			}
		}

		/**
		 * Undertakes a pipeline run of requests.
		 * 
		 * @param requestCount Number of requests.
		 * @return {@link PipelineResult}.
		 */
		private PipelineResult doPipelineRun(int requestCount) throws IOException {

			// Pipeline requests to server
			// (Connect as about to test, to avoid timeouts closing connection)
			this.selector = Selector.open();
			this.channel = SocketChannel.open(new InetSocketAddress("localhost", this.port));
			this.channel.configureBlocking(false);
			Socket socket = this.channel.socket();
			socket.setSendBufferSize(10 * 1024 * 1024);
			socket.setReceiveBufferSize(10 * 1024 * 1024);
			this.selectionKey = this.channel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

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

				// Handle possible connection close
				if (!selectionKey.isValid()) {
					fail("Lost connection after sending " + requestSentCount + " requests and received "
							+ responseReceivedCount + " responses");
				}

				// Send the request
				try {
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
				} catch (Exception ex) {
					throw new RuntimeException("Exception on write for sending " + requestSentCount
							+ " requests and received " + responseReceivedCount + " responses", ex);
				}

				// Read in next response
				try {
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
								} else if ((System.currentTimeMillis() - noDataStart) > (WAIT_FOR_DATA_TIMEOUT)) {
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

									// Add in quotes to identify incorrect character
									responseText.insert(i, "{");
									responseText.insert(i + 2, "}");

									// Provide the error
									assertEquals("Incorrect character " + i + " of response " + responseReceivedCount
											+ " (" + UsAsciiUtil.convertToChar(expectedCharacter) + " != "
											+ UsAsciiUtil.convertToChar(actualCharacter) + "): "
											+ responseText.toString() + "\n\nEXPECTED:\n" + new String(responseData),
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
				} catch (Exception ex) {
					throw new RuntimeException("Exception on read for sending " + requestSentCount
							+ " requests and received " + responseReceivedCount + " responses", ex);
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
		 * @param requestCount Request count.
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
		 * @throws Exception If run failed.
		 */
		private synchronized PipelineResult waitForCompletion() throws Exception {

			// Determine if already complete
			if (this.runResult != null) {
				return this.returnResult();
			}

			// Wait for completion
			this.wait(WAIT_FOR_SHUTDOWN_TIMEOUT);
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
		 * @throws Exception If run failed.
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

		private static void setResult(String prefix, Class<?> servicerClass, PipelineResult pipelineResult) {

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
			if (servicerClass == null) {
				result.rawResult = pipelineResult;
			} else if (servicerClass == BytesServicer.class) {
				result.bytesResult = pipelineResult;
			} else if (servicerClass == BufferServicer.class) {
				result.bufferResult = pipelineResult;
			} else if (servicerClass == FileServicer.class) {
				result.fileResult = pipelineResult;
			} else {
				fail("Unknown servicer " + servicerClass.getName());
			}

			// Determine if have all values
			if ((result.rawResult == null) || (result.bytesResult == null) || (result.bufferResult == null)
					|| (result.fileResult == null)) {
				return;
			}

			// Have both values, so print comparison
			final String format = "%1$15s";
			StringWriter message = new StringWriter();
			PrintWriter out = new PrintWriter(message);
			out.println("=====================================================================================");
			out.println(testClass.getSimpleName() + " - " + prefix);
			out.println();

			// Output headers
			out.print(String.format(format, ""));
			out.print(String.format(format, "Raw"));
			out.print(String.format(format, "Bytes"));
			out.print(String.format(format, "Buffer"));
			out.print(String.format(format, "File"));
			out.println();

			// Output the run time
			out.print(String.format(format, "Run time (ms)"));
			out.print(String.format(format, String.valueOf(result.rawResult.getRunTime())));
			out.print(String.format(format, String.valueOf(result.bytesResult.getRunTime())));
			out.print(String.format(format, String.valueOf(result.bufferResult.getRunTime())));
			out.print(String.format(format, String.valueOf(result.fileResult.getRunTime())));
			out.println();

			// Output the requests per second
			out.print(String.format(format, "Requests/Sec"));
			out.print(String.format(format, String.valueOf(result.rawResult.getRequestsPerSecond())));
			out.print(String.format(format, String.valueOf(result.bytesResult.getRequestsPerSecond())));
			out.print(String.format(format, String.valueOf(result.bufferResult.getRequestsPerSecond())));
			out.print(String.format(format, String.valueOf(result.fileResult.getRequestsPerSecond())));
			out.println();

			// Calculate the overhead
			final PipelineResult rawResult = result.rawResult;
			Function<PipelineResult, String> overhead = (compare) -> {
				long runtimeDifference = compare.getRunTime() - rawResult.getRunTime();
				long maxRunTime = Math.max(compare.getRunTime(), rawResult.getRunTime());
				float difference = (float) runtimeDifference / (float) maxRunTime;
				return String.valueOf((int) (difference * 100));
			};

			// Overhead increase
			out.println();
			out.print(String.format(format, "Overhead (%)"));
			out.print(String.format(format, ""));
			out.print(String.format(format, overhead.apply(result.bytesResult)));
			out.print(String.format(format, overhead.apply(result.bufferResult)));
			out.print(String.format(format, overhead.apply(result.fileResult)));
			out.println();

			out.println("=====================================================================================");
			out.flush();
			System.out.println(message.toString());
		}

		private PipelineResult rawResult = null;

		private PipelineResult bytesResult = null;

		private PipelineResult bufferResult = null;

		private PipelineResult fileResult = null;
	}

}