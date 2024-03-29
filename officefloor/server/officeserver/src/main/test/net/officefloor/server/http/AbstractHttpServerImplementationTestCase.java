/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.http;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import junit.framework.TestCase;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.BackPressureTeamSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.LogTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.http.stream.TemporaryFiles;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;
import net.officefloor.test.StressTest;
import net.officefloor.test.system.SystemPropertiesRule;

/**
 * Abstract {@link TestCase} for testing a {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public abstract class AbstractHttpServerImplementationTestCase {

	/**
	 * Time out on waiting for data (no data in this time considers the test
	 * stalled).
	 */
	private static final long WAIT_FOR_DATA_TIMEOUT = 1 * 60 * 1000;

	/**
	 * Time out on for pipeline run to complete.
	 */
	private static final long MAX_PIPELINE_RUN_TIME = 5 * 60 * 1000;

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation serverLocation = new HttpServerLocationImpl();

	/**
	 * {@link LogTestSupport}.
	 */
	private final LogTestSupport log = new LogTestSupport();

	/**
	 * {@link ThreadedTestSupport}.
	 */
	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Name of test.
	 */
	private String testName;

	/**
	 * <p>
	 * Obtains the request count.
	 * <p>
	 * Allows changing the number for server implementations that have higher
	 * overheads.
	 * 
	 * @return Request count.
	 */
	protected int getRequestCount() {
		return 1000000;
	}

	/**
	 * Obtains the adjusted request count.
	 * 
	 * @param reduction Reduction in magnitude of requests.
	 * @return Adjusted request count.
	 */
	private int getAdjustedRequestCount(int reduction) {
		return Math.max(10, this.getRequestCount() / reduction);
	}

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
	 * @return {@link AutoCloseable} To stop the server.
	 * @throws Exception If fails to start the raw HTTP server.
	 */
	protected abstract AutoCloseable startRawHttpServer(HttpServerLocation serverLocation) throws Exception;

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
	 * Stops the server.
	 */
	private AutoCloseable stopServer;

	/**
	 * Registered {@link PipelineExecutor} instances.
	 */
	private List<PipelineExecutor> executors = new LinkedList<>();

	/**
	 * Start timestamp.
	 */
	private LocalDateTime startTime;

	/**
	 * Formats the time.
	 */
	private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	@BeforeEach
	public void setUp(TestInfo testInfo) throws Exception {

		// Capture the test name
		this.testName = testInfo.getDisplayName();

		this.startTime = LocalDateTime.now();
		System.out.println();
		System.out.println("START: " + this.testName + " (" + this.timeFormatter.format(this.startTime) + ")");

		// Log GC
		this.log.setLogGC();

		// Reset the compare results
		CompareResult.reset(this.getClass());
	}

	@AfterEach
	public void tearDown() throws Exception {
		LocalDateTime shutdownTime = LocalDateTime.now();
		System.out.println("SHUTDOWN: " + this.testName + " (" + this.timeFormatter.format(shutdownTime)
				+ "), run time " + (this.startTime.until(shutdownTime, ChronoUnit.SECONDS) + " seconds"));

		// Close the executors
		for (PipelineExecutor executor : this.executors) {
			executor.close();
		}

		// Stop the server
		if (this.stopServer != null) {
			this.stopServer.close();
		}

		// Ensure the server has stopped serving requests
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			client.execute(new HttpGet(this.serverLocation.createClientUrl(false, "/test")));
			fail("Server should have stopped listening");
		} catch (HttpHostConnectException ex) {
			assertTrue(
					ex.getMessage().startsWith("Connect to ") && ex.getMessage().contains("failed: Connection refused"),
					"Incorrect connection error: " + ex.getMessage());
		}

		// Remaining tear down
		LocalDateTime endTime = LocalDateTime.now();
		System.out.println("END: " + this.testName + " (" + this.timeFormatter.format(endTime) + "), shutdown time "
				+ shutdownTime.until(endTime, ChronoUnit.SECONDS) + " seconds");
	}

	/**
	 * Starts the {@link HttpServer}.
	 * 
	 * @param sectionServicer {@link Class} of the {@link ClassSectionSource} to
	 *                        service the {@link HttpRequest}.
	 * @throws Exception If fails to start the {@link HttpServer}.
	 */
	protected void startHttpServer(Class<?> sectionServicer) throws Exception {
		startHttpServer(sectionServicer, null, null);
	}

	/**
	 * Starts the {@link HttpServer}.
	 * 
	 * @param sectionServicer {@link Class} of the {@link ClassSectionSource} to
	 *                        service the {@link HttpRequest}.
	 * @param qualifier       Qualifier for the {@link ExternalServiceInput}. May be
	 *                        <code>null</code>.
	 * @param extension       Additional {@link OfficeFloorExtensionService}. May be
	 *                        <code>null</code>.
	 * @throws Exception If fails to start the {@link HttpServer}.
	 */
	protected void startHttpServer(Class<?> sectionServicer, String qualifier, OfficeFloorExtensionService extension)
			throws Exception {

		// Compile the OfficeFloor
		Closure<HttpServer> httpServer = new Closure<>();
		this.stopServer = this.startHttpServer((deployer, context) -> {

			// Obtain the input
			DeployedOfficeInput serviceHandler = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME)
					.getDeployedOfficeInput("SERVICER", "service");

			// Configure the HTTP Server
			httpServer.value = new HttpServer(serviceHandler, qualifier, deployer, context);

			// Provide thread object
			if (sectionServicer == ThreadedServicer.class) {
				addManagedObject(deployer, "ThreadedManagedObject", ThreadedManagedObject.class,
						ManagedObjectScope.THREAD);
				deployer.addTeam("Threaded", ExecutorCachedTeamSource.class.getName()).addTypeQualification(null,
						ThreadedManagedObject.class.getName());
			}

			// Undertake extension
			if (extension != null) {
				extension.extendOfficeFloor(deployer, context);
			}
		}, (architect, context) -> {
			architect.enableAutoWireObjects();
			architect.enableAutoWireTeams();
			architect.addOfficeSection("SERVICER", ClassSectionSource.class.getName(), sectionServicer.getName());
		});

		// Ensure correct HTTP server implementation
		Class<? extends HttpServerImplementation> expectedImplementation = this.getHttpServerImplementationClass();
		assertEquals(expectedImplementation, httpServer.value.getHttpServerImplementation().getClass(),
				"Incorrect HTTP Server implementation");
	}

	/**
	 * Starts the {@link HttpServer}.
	 * 
	 * @param officeFloorExtension {@link OfficeFloorExtensionService} to configure
	 *                             the {@link OfficeFloor}.
	 * @param officeExtension      {@link OfficeExtensionService} to configure the
	 *                             {@link OfficeFloor}.
	 * @return {@link AutoCloseable} to stop the server.
	 * @throws Exception If fails to start the {@link HttpServer}.
	 */
	protected AutoCloseable startHttpServer(OfficeFloorExtensionService officeFloorExtension,
			OfficeExtensionService officeExtension) throws Exception {
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> officeFloorExtension.extendOfficeFloor(context.getOfficeFloorDeployer(),
				(OfficeFloorExtensionContext) context.getOfficeFloorSourceContext()));
		compile.office((context) -> officeExtension.extendOffice(context.getOfficeArchitect(),
				(OfficeExtensionContext) context.getOfficeSourceContext()));
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();
		return officeFloor;
	}

	/**
	 * Convenience method to add an {@link OfficeFloorManagedObject}.
	 * 
	 * @param deployer           {@link OfficeFloorDeployer}.
	 * @param managedObjectName  Name of the {@link ManagedObject}.
	 * @param managedObjectClass Type of the {@link Object}.
	 * @param scope              {@link ManagedObjectScope}.
	 * @return {@link OfficeFloorManagedObject}.
	 */
	private static OfficeFloorManagedObject addManagedObject(OfficeFloorDeployer deployer, String managedObjectName,
			Class<?> managedObjectClass, ManagedObjectScope scope) {
		OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource(managedObjectName + "_SOURCE",
				ClassManagedObjectSource.class.getName());
		mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, managedObjectClass.getName());
		return mos.addOfficeFloorManagedObject(managedObjectName, scope);
	}

	public static class Servicer {
		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("/test", connection.getRequest().getUri(), "Incorrect request URI");
			connection.getResponse().getEntityWriter().write("hello world");
		}
	}

	public static class BytesServicer {
		private static final byte[] HELLO_WORLD = "hello world"
				.getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		private static final HttpHeaderValue TEXT_PLAIN = new HttpHeaderValue("text/plain");

		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("/test", connection.getRequest().getUri(), "Incorrect request URI");
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
			BufferJvmFix.flip(HELLO_WORLD);
		}

		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("/test", connection.getRequest().getUri(), "Incorrect request URI");
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.setContentType(BytesServicer.TEXT_PLAIN, null);
			response.getEntity().write(HELLO_WORLD.duplicate());
		}
	}

	private static final FileChannel HELLO_WORLD_FILE;

	static {
		FileChannel helloWorldFile = null;
		try {
			helloWorldFile = TemporaryFiles.getDefault().createTempFile("HelloWorld", BytesServicer.HELLO_WORLD);
		} catch (IOException ex) {
			fail(ex);
		}
		HELLO_WORLD_FILE = helloWorldFile;
	}

	public static class FileServicer {
		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("/test", connection.getRequest().getUri(), "Incorrect request URI");
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.setContentType(BytesServicer.TEXT_PLAIN, null);
			response.getEntity().write(HELLO_WORLD_FILE, null);
		}
	}

	public static class FailServicer {
		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("/test", connection.getRequest().getUri(), "Incorrect request URI");

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
			assertEquals("/encoded-%3F-%23-+?query=string", requestUri, "Should have encoded ? #");
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
			assertEquals("/test", connection.getRequest().getUri(), "Incorrect request URI");
			assertNotSame(thread, Thread.currentThread(), "Should be different handling thread");
			connection.getResponse().getEntityWriter().write("hello world");
		}
	}

	public static class AsyncServicer {
		public void service(AsynchronousFlow async, ServerHttpConnection connection) {
			new Thread(() -> {

				try {
					Thread.sleep(1); // ensure less chance of immediate return
				} catch (InterruptedException ex) {
					// carry on
				}

				// Now complete asynchronous operation
				async.complete(() -> {
					connection.getResponse().getEntityWriter().write("hello world");
				});
			}).start();
		}
	}

	public static class FunctionalityServicer {
		public void service(ServerHttpConnection connection) throws IOException {

			// Assert has all content of request
			HttpRequest request = connection.getRequest();
			assertEquals(HttpMethod.POST, request.getMethod(), "Incorrect method");
			assertEquals("/functionality", request.getUri(), "Incorrect request URI");
			assertEquals(HttpVersion.HTTP_1_1, request.getVersion(), "Incorrect version");
			StringBuilder allHeaders = new StringBuilder();
			allHeaders.append("\n");
			for (HttpHeader header : request.getHeaders()) {
				allHeaders.append("\n\t" + header.getName() + "=" + header.getValue());
			}
			allHeaders.append("\n");
			assertEquals(8, request.getHeaders().length(),
					"Incorrect number of headers (with extra by HTTP client):" + allHeaders.toString());
			assertEquals("header", request.getHeaders().getHeader("request").getValue(), "Incorrect header");
			assertEquals(1, request.getCookies().length(), "Incorrect number of cookies");
			assertEquals("cookie", request.getCookies().getCookie("request").getValue(), "Incorrect cookie");
			assertEquals("request", EntityUtil.toString(request, null), "Incorrect entity");

			// Send a full response
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.setStatus(HttpStatus.OK);
			response.setContentType("text/test", null);
			response.getHeaders().addHeader("response", "header");
			response.getCookies().setCookie("response", "cookie");
			response.getEntityWriter().write("response");
		}
	}

	public static final String CONNECTION_QUALIFIER_ONE = "QUALIFIED_ONE";

	public static final String CONNECTION_QUALIFIER_TWO = "QUALIFIED_TWO";

	public static class QualifiedServicer {
		public void service(@Qualified(CONNECTION_QUALIFIER_ONE) ServerHttpConnection connectionOne,
				@Qualified(CONNECTION_QUALIFIER_TWO) ServerHttpConnection connectionTwo) throws IOException {
			assertNull(connectionTwo, "Should not have other qualified connection");
			connectionOne.getResponse().getEntityWriter().write("Qualified");
		}
	}

	/**
	 * Ensure able to service asynchronous servicing.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void async() throws Exception {
		this.startHttpServer(AsyncServicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure able to send all details and receive all details.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void functionality() throws Exception {
		this.startHttpServer(FunctionalityServicer.class);
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(false)) {
			HttpPost post = new HttpPost(this.serverLocation.createClientUrl(false, "/functionality"));
			post.addHeader("request", "header");
			post.addHeader("cookie", "request=cookie");
			post.setEntity(new StringEntity("request"));
			HttpResponse response = client.execute(post);

			// Validate the response
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals(200, response.getStatusLine().getStatusCode(), "Incorrect status: " + entity);
			assertEquals(new ProtocolVersion("HTTP", 1, 1), response.getStatusLine().getProtocolVersion(),
					"Incorrect version: " + entity);
			StringBuilder allHeaders = new StringBuilder();
			for (Header header : response.getAllHeaders()) {
				allHeaders.append("\n\t" + header.getName() + "=" + header.getValue());
			}
			Set<String> expectedHeaders = this.getUniqueResponseHeaderNames("header", "Set-Cookie", "Content-Type",
					"Content-Length");
			assertEquals(expectedHeaders.size(), response.getAllHeaders().length,
					"Incorrect number of headers:" + allHeaders.toString() + "\n\n" + expectedHeaders + "\n\n");
			assertEquals("header", response.getFirstHeader("response").getValue(),
					"Incorrect header:" + allHeaders.toString());
			assertEquals("response=cookie", response.getFirstHeader("set-cookie").getValue(),
					"Incorrect cookie:" + allHeaders.toString());
			assertEquals("text/test", response.getFirstHeader("Content-Type").getValue(), "Incorrect Content-Type");
			assertEquals("response", entity, "Incorrect entity");
		}
	}

	/**
	 * Ensure can qualify the {@link ServerHttpConnection}.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void qualified() throws Exception {
		this.startHttpServer(QualifiedServicer.class, CONNECTION_QUALIFIER_ONE, (deployer, context) -> {

			// Configure the other qualified Server HTTP Connection
			DeployedOfficeInput serviceHandler = deployer.getDeployedOffice("OFFICE").getDeployedOfficeInput("SERVICER",
					"service");
			serviceHandler.addExternalServiceInput(ServerHttpConnection.class, CONNECTION_QUALIFIER_TWO,
					ManagedObject.class, null);
		});
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(false)) {

			// Ensure can call server
			HttpResponse response = client.execute(new HttpGet(this.serverLocation.createClientUrl(false, "/")));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful");
			assertEquals("Qualified", entity, "Incorrect response");
		}
	}

	/**
	 * Ensure can handle raw single HTTP request.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void rawSingleRequest() throws Exception {
		this.startAppropriateHttpServer(null);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can send a single HTTP request.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void singleRequest() throws Exception {
		this.startHttpServer(Servicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can send a single HTTPS request.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void singleSecureRequest() throws Exception {
		this.startHttpServer(Servicer.class);
		this.doSingleRequest(true);
	}

	/**
	 * Ensure can send a single HTTP {@link ByteBuffer} response.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void singleBufferRequest() throws Exception {
		this.startHttpServer(BufferServicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can send a single HTTPS {@link ByteBuffer} response.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void singleSecureBufferRequest() throws Exception {
		this.startHttpServer(BufferServicer.class);
		this.doSingleRequest(true);
	}

	/**
	 * Ensure can send a single HTTP {@link FileBuffer} response.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void singleFileRequest() throws Exception {
		this.startHttpServer(FileServicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can send a single HTTPS {@link FileBuffer} response.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void singleSecureFileRequest() throws Exception {
		this.startHttpServer(FileServicer.class);
		this.doSingleRequest(true);
	}

	/**
	 * Ensure send Server and Date {@link HttpHeader} values.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void serverDateHeaders() throws Exception {
		new SystemPropertiesRule(HttpServer.PROPERTY_HTTP_SERVER_NAME, "OfficeFloorServer",
				HttpServer.PROPERTY_HTTP_DATE_HEADER, "true").run(() -> {

					// Obtain the server name
					String serverName = this.getServerName();

					// Should setup server with System properties
					this.startHttpServer(BufferServicer.class);
					this.doSingleRequest(false, (response) -> {
						assertEquals(serverName, response.getFirstHeader("Server").getValue(),
								"Incorrect Server HTTP header");
						assertNotNull(response.getFirstHeader("Date"), "Should have Date HTTP header");
					});
				});
	}

	/**
	 * Obtains the server name.
	 * 
	 * @return Server name.
	 */
	protected String getServerName() {
		String serverNameSuffix = this.getServerNameSuffix();
		return "OfficeFloorServer" + (serverNameSuffix == null ? "" : " " + serverNameSuffix);
	}

	/**
	 * Ensure closes the {@link FileChannel} on write.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void singleCloseFileRequest() throws Exception {
		CLOSE_FILE = TemporaryFiles.getDefault().createTempFile("testSingleCloseFileRequest",
				BytesServicer.HELLO_WORLD);
		this.startHttpServer(CloseFileServicer.class);
		this.doSingleRequest(false);
		assertFalse(CLOSE_FILE.isOpen(), "File should be closed");
	}

	/**
	 * Ensure closes the {@link FileChannel} on write.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void singleSecureCloseFileRequest() throws Exception {
		CLOSE_FILE = TemporaryFiles.getDefault().createTempFile("testSingleSecureCloseFileRequest",
				BytesServicer.HELLO_WORLD);
		this.startHttpServer(CloseFileServicer.class);
		this.doSingleRequest(true);
		assertFalse(CLOSE_FILE.isOpen(), "File should be closed");
	}

	private volatile static FileChannel CLOSE_FILE = null;

	public static class CloseFileServicer {
		public void service(ServerHttpConnection connection) throws Exception {
			assertEquals("/test", connection.getRequest().getUri(), "Incorrect request URI");
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.setContentType(BytesServicer.TEXT_PLAIN, null);
			response.getEntity().write(CLOSE_FILE, 0, 6, (file, isWritten) -> {
				assertTrue(isWritten, "File should be written");
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
	public void multipleIndividualRequests() throws Exception {
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
	public void multipleIndividualSecureRequests() throws Exception {
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
	@Test
	public void notDecodeRequestUrl() throws Exception {
		this.startHttpServer(EncodedUrlServicer.class);
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet(
					this.serverLocation.createClientUrl(false, "/encoded-%3F-%23-+?query=string#fragment")));
			String responseEntity = HttpClientTestUtil.entityToString(response);
			assertEquals(200, response.getStatusLine().getStatusCode(), "Incorrect status: " + responseEntity);
			assertEquals("success", responseEntity, "Incorrect response");
		}
	}

	/**
	 * Ensure can handle request with {@link ThreadedServicer}.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void singleThreadedHandlerRequest() throws Exception {
		this.startHttpServer(ThreadedServicer.class);
		this.doSingleRequest(false);
	}

	/**
	 * Ensure can handle request with {@link ThreadedServicer} for a secure
	 * connection.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void secureSingleThreadedHandlerRequest() throws Exception {
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
			String url = this.serverLocation.createClientUrl(isSecure, "/test");
			HttpResponse response = client.execute(new HttpGet(url));
			String entity = HttpClientTestUtil.entityToString(response);
			assertEquals(200, response.getStatusLine().getStatusCode(), "Incorrect status: " + entity);
			assertEquals("hello world", entity, "Incorrect response");
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
	@Test
	public void socket() throws Exception {
		this.doSocketTest(false);
	}

	/**
	 * Ensure raw secure request.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void secureSocket() throws Exception {
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

		// Ensure can connect
		this.doSingleRequest(isSecure);

		// Create connection to server
		String localhost = "localhost";
		int port = isSecure ? this.serverLocation.getHttpsPort() : this.serverLocation.getHttpPort();
		ConnectedSocketFactory connectedSocketFactory = () -> {
			if (!isSecure) {
				// Non-secure socket
				return SocketFactory.getDefault().createSocket(localhost, port);
			} else {
				// Secure socket
				return OfficeFloorDefaultSslContextSource.createClientSslContext(null).getSocketFactory()
						.createSocket(localhost, port);
			}
		};
		try (Socket socket = connectSocket(connectedSocketFactory)) {

			// Send the request
			socket.getOutputStream().write(this.createPipelineRequestData());

			// Obtain the response (may require multiple reads as data comes in)
			byte[] expectedResponseData = this.createPipelineResponseData();
			byte[] actualResponseData = new byte[expectedResponseData.length];
			int totalBytesRead = 0;
			while (totalBytesRead < expectedResponseData.length) {
				int bytesRead = socket.getInputStream().read(actualResponseData, totalBytesRead,
						(expectedResponseData.length - totalBytesRead));
				assertTrue(bytesRead > 0, "Must read bytes\n\nExpected: " + new String(expectedResponseData)
						+ "\n\nActual: " + new String(actualResponseData, 0, totalBytesRead));
				totalBytesRead += bytesRead;
			}

			// Ensure correct data
			for (int i = 0; i < expectedResponseData.length; i++) {
				assertEquals(expectedResponseData[i], actualResponseData[i],
						"Incorrect response byte " + i + " (of " + expectedResponseData.length + " bytes)\n\nExpected: "
								+ new String(expectedResponseData) + "\n\nActual: " + new String(actualResponseData));
			}
		}
	}

	/**
	 * Factory interface for a connected {@link Socket}.
	 */
	private static interface ConnectedSocketFactory {

		/**
		 * Creates the connected {@link Socket}.
		 * 
		 * @return Connected {@link Socket}.
		 * @throws Exception If fails to create the connected {@link Socket}.
		 */
		Socket create() throws Exception;
	}

	/**
	 * <p>
	 * Connects the {@link Socket}.
	 * </p>
	 * This undertakes retries to make the tests more resilient.
	 * 
	 * @param socketFactory {@link ConnectedSocketFactory}.
	 * @return Connected {@link Socket}.
	 * @throws Exception If fails to connect.
	 */
	private static Socket connectSocket(ConnectedSocketFactory socketFactory) throws Exception {
		final int RETRY_COUNT = 3;
		ConnectException connectException = null;
		for (int retry = 0; retry < RETRY_COUNT; retry++) {
			try {
				return socketFactory.create();
			} catch (ConnectException ex) {
				if (ex.getMessage().startsWith("Connection timed out")) {
					connectException = ex; // capture for propagate on too many retries
				} else {
					// Propagate other exception
					throw ex;
				}
			}
		}
		return fail(RETRY_COUNT + " socket connection attempts timed out", connectException);
	}

	/**
	 * Ensure can handle {@link Escalation}.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void handleError() throws Exception {
		this.startHttpServer(FailServicer.class);
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet(this.serverLocation.createClientUrl(false, "/test")));

			// Ensure flag as internal server error
			assertEquals(500, response.getStatusLine().getStatusCode(), "Incorrect status");

			// Ensure exception in response
			StringWriter content = new StringWriter();
			TEST_EXCEPTION.printStackTrace(new PrintWriter(content));
			String contentText = content.toString();
			assertEquals(contentText, HttpClientTestUtil.entityToString(response), "Incorrect response");

			// Ensure correct header information
			assertEquals(String.valueOf(contentText.length()), response.getFirstHeader("Content-Length").getValue(),
					"Incorrect error Content-Length");
			assertEquals("text/plain", response.getFirstHeader("Content-Type").getValue(),
					"Incorrect error Content-Type");
		}
	}

	/**
	 * Ensure can handle pressure overloading the server.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void teamPressureOverload() throws Exception {
		this.startHttpServer(PressureOverloadServicer.class, null, (deployer, context) -> {

			// Configure marker
			addManagedObject(deployer, "MARKER", TeamMarker.class, ManagedObjectScope.THREAD);

			// Configure teams
			OfficeFloorTeam team = deployer.addTeam("TEAM", BackPressureTeamSource.class.getName());
			team.addTypeQualification(null, TeamMarker.class.getName());
			team.requestNoTeamOversight();
		});
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet(this.serverLocation.createClientUrl(false, "/test")));

			// Ensure notify of overloaded server
			String responseBody = EntityUtils.toString(response.getEntity());
			assertEquals(503, response.getStatusLine().getStatusCode(),
					"Response status should indicate that server is overloaded:\n" + responseBody);
			assertTrue(responseBody.contains(BackPressureTeamSource.BACK_PRESSURE_EXCEPTION.getMessage()),
					"Should provide detail of failure:\n" + responseBody);
		}
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

		@Next("backPressure")
		public Thread service() {
			return Thread.currentThread();
		}

		public void backPressure(@Parameter Thread serviceThread, ServerHttpConnection connection, TeamMarker marker)
				throws Exception {
			fail("Should not be invoked, as back pressure from Team");
		}
	}

	/**
	 * Ensure {@link ProcessState} instances for connection are cancelled on loss of
	 * connection.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void cancelConnection() throws Exception {
		this.doCancelConnectionTest(false);
	}

	/**
	 * Ensure {@link ProcessState} instances for connection are cancelled on loss of
	 * secure connection.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void secureCancelConnection() throws Exception {
		this.doCancelConnectionTest(true);
	}

	/**
	 * Marker {@link ManagedObject} for identifying {@link Team}.
	 */
	public static class TeamTwoMarker {
	}

	/**
	 * Indicates if handle cancel.
	 * 
	 * @return <code>true</code> if handle cancel.
	 */
	protected boolean isHandleCancel() {
		return true;
	}

	/**
	 * Undertakes the cancel connection test.
	 * 
	 * @param isSecure Indicates if secure.
	 */
	private void doCancelConnectionTest(boolean isSecure) throws Exception {

		// Determine if handle cancel
		if (!this.isHandleCancel()) {
			System.out.println(this.testName + " not handle cancel, so not running test");
			return;
		}

		// Start the server
		CancelConnectionManagedObjectSource mos = new CancelConnectionManagedObjectSource();
		this.startHttpServer(CancelConnectionServicer.class, null, (deployer, context) -> {

			// Load the managed object source
			deployer.addManagedObjectSource("MOS", mos).addOfficeFloorManagedObject("MO", ManagedObjectScope.PROCESS);

			// Configure team one
			addManagedObject(deployer, "MARKER_ONE", TeamMarker.class, ManagedObjectScope.PROCESS);
			deployer.addTeam("TEAM_ONE", ExecutorCachedTeamSource.class.getName()).addTypeQualification(null,
					TeamMarker.class.getName());

			// Configure team two
			addManagedObject(deployer, "MARKER_TWO", TeamTwoMarker.class, ManagedObjectScope.PROCESS);
			deployer.addTeam("TEAM_TWO", ExecutorCachedTeamSource.class.getName()).addTypeQualification(null,
					TeamTwoMarker.class.getName());
		});

		// Reset state
		synchronized (CancelConnectionServicer.isContinue) {
			CancelConnectionServicer.isContinue[0] = false;
			CancelConnectionServicer.isBlocked = false;
		}

		// Create connection to server
		InetAddress localhost = InetAddress.getLocalHost();
		int port = isSecure ? this.serverLocation.getHttpsPort() : this.serverLocation.getHttpPort();
		try (Socket socket = (isSecure ? OfficeFloorDefaultSslContextSource.createClientSslContext(null)
				.getSocketFactory().createSocket(localhost, port)
				: SocketFactory.getDefault().createSocket(localhost, port))) {

			// Send many requests
			OutputStream socketOutput = socket.getOutputStream();
			socketOutput.write(this.createPipelineRequestData());
			socketOutput.flush();

			// Wait for blocking on servicing
			long startTime = System.currentTimeMillis();
			synchronized (CancelConnectionServicer.isContinue) {
				while (!CancelConnectionServicer.isBlocked) {
					this.threading.timeout(startTime);
					CancelConnectionServicer.isContinue.wait(10);
				}
			}

			// Sever (close) the connection
			socket.close();

			// Trigger servicing
			synchronized (CancelConnectionServicer.isContinue) {
				CancelConnectionServicer.isContinue[0] = true;
				CancelConnectionServicer.isContinue.notifyAll();
			}

			// Wait for completion
			this.threading.waitForTrue(() -> mos.completed.get() == 1);
		}
	}

	public static class CancelConnectionServicer {

		private static boolean isBlocked = false;

		private static final boolean[] isContinue = new boolean[] { false };

		@Next("loopOne")
		public void service(CancelConnectionManagedObjectSource mos) throws Throwable {

			// Only one request gets serviced, while rest chain behind
			long endTime = System.currentTimeMillis() + 3000;
			synchronized (isContinue) {

				// Flag now blocking
				isBlocked = true;
				isContinue.notifyAll();

				// Wait to continue
				while (!isContinue[0]) {
					if (endTime < System.currentTimeMillis()) {
						fail("Timed out waiting on servicing");
					}
					isContinue.wait(10);
				}
			}
		}

		@Next("loopTwo")
		public void loopOne(TeamMarker marker) throws Exception {
			// loops around (on different team to allow socket listener to close connection)
			Thread.sleep(1);
		}

		@Next("loopOne")
		public void loopTwo(TeamTwoMarker marker) {
			// loop
		}
	}

	public static class CancelConnectionManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject, ManagedObjectPool {

		private AtomicInteger created = new AtomicInteger(0);

		private AtomicInteger completed = new AtomicInteger(0);

		/*
		 * ==================== ManagedObjectSource ==========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.getManagedObjectSourceContext().setDefaultManagedObjectPool((source) -> this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			created.incrementAndGet();
			return this;
		}

		/*
		 * ======================= ManagedObject ==============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== ManagedObjectPool ============================
		 */

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			completed.incrementAndGet();
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			completed.incrementAndGet();
		}

		@Override
		public void empty() {
			// nothing to tidy up
		}
	}

	/**
	 * Verify pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void rawPipelineVerify() throws Exception {
		this.doPipelineVerifyTest(null);
	}

	/**
	 * Verify pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void bytesPipelineVerify() throws Exception {
		this.doPipelineVerifyTest(BytesServicer.class);
	}

	/**
	 * Verify pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void bufferPipelineVerify() throws Exception {
		this.doPipelineVerifyTest(BufferServicer.class);
	}

	/**
	 * Verify pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@Test
	public void filePipelineVerify() throws Exception {
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
	public void rawPipelining() throws Exception {
		this.doPipeliningTest(null);
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void bytesPipelining() throws Exception {
		this.doPipeliningTest(BytesServicer.class);
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void bufferPipelining() throws Exception {
		this.doPipeliningTest(BufferServicer.class);
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void filePipelining() throws Exception {
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
		executor.doPipelineRun(this.getAdjustedRequestCount(10)).printResult(this.testName + " WARMUP");

		// Undertake performance run
		PipelineResult result = executor.doPipelineRun(this.getAdjustedRequestCount(1));
		result.printResult(this.testName + " RUN");

		// Load for comparison
		CompareResult.setResult("pipelining", servicerClass, result);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void rawThreadedHandler() throws Exception {
		this.doThreadedHandlerTest(null);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void bytesThreadedHandler() throws Exception {
		this.doThreadedHandlerTest(BytesServicer.class);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void bufferThreadedHandler() throws Exception {
		this.doThreadedHandlerTest(BufferServicer.class);
	}

	/**
	 * Ensure can handle HTTP requests with threaded handler.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void fileThreadedHandler() throws Exception {
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
		executor.doPipelineRun(this.getAdjustedRequestCount(1000)).printResult(this.testName + " WARMUP");

		// Undertake performance run
		PipelineResult result = executor.doPipelineRun(this.getAdjustedRequestCount(100));
		result.printResult(this.testName + " RUN");

		// Load for comparison
		CompareResult.setResult("threaded-handler", servicerClass, result);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void rawHeavyLoad() throws Exception {
		this.doHeavyLoadTest(null);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void bytesHeavyLoad() throws Exception {
		this.doHeavyLoadTest(BytesServicer.class);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void bufferHeavyLoad() throws Exception {
		this.doHeavyLoadTest(BufferServicer.class);
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void fileHeavyLoad() throws Exception {
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
		this.doMultiClientLoadTest(servicerClass, clientCount, this.getRequestCount(),
				"Heavy Load (" + clientCount + " clients)");
	}

	/**
	 * Ensure can service over requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void rawOverLoad() throws Exception {
		this.doOverLoadTest(null);
	}

	/**
	 * Ensure can service overload requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void bytesOverLoad() throws Exception {
		this.doOverLoadTest(BytesServicer.class);
	}

	/**
	 * Ensure can service overload requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void bufferOverLoad() throws Exception {
		this.doOverLoadTest(BufferServicer.class);
	}

	/**
	 * Ensure can service overload requests pipelined.
	 * 
	 * @throws Exception If test failure.
	 */
	@StressTest
	public void fileOverLoad() throws Exception {
		this.doOverLoadTest(FileServicer.class);
	}

	/**
	 * Undertakes the over load test.
	 * 
	 * @param servicerClass Servicer {@link Class}.
	 */
	private void doOverLoadTest(Class<?> servicerClass) throws Exception {
		int clientCount = Runtime.getRuntime().availableProcessors() * 4;
		this.doMultiClientLoadTest(servicerClass, clientCount, this.getAdjustedRequestCount(10),
				"Over Load (" + clientCount + " clients)");
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
			this.stopServer = this.startRawHttpServer(this.serverLocation);
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
		PipelineExecutor[] executors = new AbstractHttpServerImplementationTestCase.PipelineExecutor[clientCount];
		for (int i = 0; i < clientCount; i++) {
			executors[i] = new PipelineExecutor(this.serverLocation.getHttpPort());
		}

		// Do warm up
		executors[0].doPipelineRun(requestCount).printResult(this.testName + " WARMUP");

		// Run the executors
		for (PipelineExecutor executor : executors) {
			new Thread(executor.getRunnable(requestCount)).start();
		}

		// Wait for completion of run
		PipelineResult[] results = new PipelineResult[clientCount];
		for (int i = 0; i < clientCount; i++) {
			results[i] = executors[i].waitForPipelineRunComplete();
		}

		// Provide summary of results
		long minStartTime = results[0].startTime;
		long maxEndTime = results[0].endTime;
		int totalRequests = 0;
		for (int i = 0; i < clientCount; i++) {
			PipelineResult result = results[i];
			result.printResult(this.testName + " CLIENT-" + i);
			minStartTime = (result.startTime < minStartTime) ? result.startTime : minStartTime;
			maxEndTime = (result.endTime > maxEndTime) ? result.endTime : maxEndTime;
			totalRequests += result.requestCount;
		}
		PipelineResult result = new PipelineResult(minStartTime, maxEndTime, totalRequests);
		result.printResult(this.testName + " TOTAL");

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
		request.append("host: localhost\n");
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
		 * Flags to stop the pipeline (as typically timed out running).
		 */
		private volatile boolean isStop = false;

		/**
		 * Instantiate.
		 * 
		 * @param port Port for the {@link HttpServer}.
		 * @throws IOException If fails to connect to {@link HttpServer}.
		 */
		private PipelineExecutor(int port) throws IOException {
			this.port = port;

			// Register
			AbstractHttpServerImplementationTestCase.this.executors.add(this);
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
			byte[] requestData = AbstractHttpServerImplementationTestCase.this.createPipelineRequestData();
			ByteBuffer requestBuffer = ByteBuffer.allocateDirect(requestData.length);
			requestBuffer.put(requestData);
			BufferJvmFix.flip(requestBuffer);

			// Create the expected response
			byte[] responseData = AbstractHttpServerImplementationTestCase.this.createPipelineResponseData();

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

				// Determine if stop
				if (this.isStop) {
					fail("Pipeline stopped");
				}

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
								BufferJvmFix.clear(requestBuffer);
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
							BufferJvmFix.clear(responseBuffer);
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
									fail("Timed out (" + WAIT_FOR_DATA_TIMEOUT + " milliseconds) waiting on data");
								}
							}

							// Handle the data
							BufferJvmFix.flip(responseBuffer);
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
									assertEquals(expectedCharacter, actualCharacter,
											"Incorrect character " + i + " of response " + responseReceivedCount + " ("
													+ UsAsciiUtil.convertToChar(expectedCharacter) + " != "
													+ UsAsciiUtil.convertToChar(actualCharacter) + "):\n"
													+ responseText.toString() + "\n\nEXPECTED:\n"
													+ new String(responseData));
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

					} finally {
						// Notify complete
						synchronized (PipelineExecutor.this) {
							PipelineExecutor.this.notify();
						}
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
		private synchronized PipelineResult waitForPipelineRunComplete() throws Exception {

			// Determine if already complete
			if (this.runResult != null) {
				return this.returnResult();
			}

			// Wait for completion of pipeline run
			this.wait(MAX_PIPELINE_RUN_TIME);
			if (this.runResult == null) {
				this.isStop = true; // ensure stop pipeline (avoids it continuing to run)
				fail("Timed out waiting on pipeline completion (" + MAX_PIPELINE_RUN_TIME + " milliseconds)");
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
			return fail((Throwable) this.runResult);
		}
	}

	/**
	 * Result of a pipeline execution.
	 */
	protected static class PipelineResult {

		private long startTime;

		private long endTime;

		private int requestCount;

		public PipelineResult(long startTime, long endTime, int requestCount) {
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
	protected static class CompareResult {

		private static Class<?> testClass;

		private static Map<Class<?>, Map<String, CompareResult>> results = new HashMap<>();

		private static void reset(Class<?> testClazz) {
			testClass = testClazz;
		}

		public static boolean setResult(String prefix, Class<?> servicerClass, PipelineResult pipelineResult) {

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
				return false; // not complete with results
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

			// Have all results
			return true;
		}

		private PipelineResult rawResult = null;

		private PipelineResult bytesResult = null;

		private PipelineResult bufferResult = null;

		private PipelineResult fileResult = null;
	}

}
