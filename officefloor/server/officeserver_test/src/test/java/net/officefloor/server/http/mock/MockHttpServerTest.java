package net.officefloor.server.http.mock;

import java.io.IOException;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;

/**
 * Tests the {@link MockHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpServerTest extends OfficeFrameTestCase {

	/**
	 * {@link CompileOfficeFloor}.
	 */
	private final CompileOfficeFloor compile = new CompileOfficeFloor();

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Create the mock HTTP server
		this.compile.officeFloor((context) -> {
			DeployedOfficeInput input = context.getDeployedOffice().getDeployedOfficeInput("SERVICER", "service");
			this.server = MockHttpServer.configureMockHttpServer(input);
		});
	}

	@Override
	protected void tearDown() throws Exception {

		// Ensure close OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Remaining tear down
		super.tearDown();
	}

	/**
	 * Ensure can obtain the mock {@link HttpRequest}.
	 */
	public void testHttpRequest() throws Exception {

		// Create and configure the request
		MockHttpRequestBuilder builder = MockHttpServer.mockRequest();
		builder.header("TEST", "value");
		builder.getHttpEntity().write(1);

		// Obtain the mock HTTP request
		HttpRequest request = builder.build();
		assertSame("Incorrect method", HttpMethod.GET, request.getMethod());
		assertEquals("Inocrrect request URI", "/", request.getUri());
		assertSame("Incorrect version", HttpVersion.HTTP_1_1, request.getVersion());
		assertEquals("Should have header", 1, request.getHeaders().length());
		assertEquals("Incorrect header", "value", request.getHeaders().getHeader("test").getValue());
		assertEquals("Incorrect entity", 1, request.getEntity().read());
	}

	/**
	 * Ensure can obtain mock {@link HttpResponse}.
	 */
	public void testHttpResponse() throws Exception {

		// Create and configure the response
		MockHttpResponseBuilder builder = MockHttpServer.mockResponse();
		builder.getHeaders().addHeader("TEST", "value");
		builder.getEntity().write(1);

		// Validate the built response
		MockHttpResponse response = builder.build();
		assertSame("Incorrect version", HttpVersion.HTTP_1_1, response.getVersion());
		assertSame("Incorrect status", HttpStatus.OK, response.getStatus());
		assertEquals("Should have one header", 3, response.getHeaders().size());
		assertEquals("Incorrect content-type", "application/octet-stream",
				response.getHeader("content-type").getValue());
		assertEquals("Incorrect content-length", "1", response.getHeader("content-length").getValue());
		assertEquals("Incorrect header value", "value", response.getHeader("TEST").getValue());
		assertEquals("Incorrect response", 1, response.getEntity().read());
		assertEquals("Should have read entity", -1, response.getEntity().read());
	}

	/**
	 * Ensure can mock a simple request.
	 */
	public void testSimpleRequest() throws Exception {

		// Configure servicing
		this.compile.office((context) -> context.addSection("SERVICER", SimpleRequestHandler.class));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure can service request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		MockHttpResponse response = this.server.send(request);

		// Validate the response
		assertSame("Incorrect version", HttpVersion.HTTP_1_1, response.getVersion());
		assertSame("Incorrect status", HttpStatus.OK, response.getStatus());
		assertEquals("Should be one header (plus content-type and content-length)", 3, response.getHeaders().size());
		assertEquals("Incorrect content-type", "text/plain", response.getHeader("content-type").getValue());
		assertEquals("Incorrect content-length", "11", response.getHeader("content-length").getValue());
		assertEquals("Incorrect header value", "Value", response.getHeader("TEST").getValue());
		assertEquals("Incorrect response", "Hello World",
				response.getEntity(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
	}

	public static class SimpleRequestHandler {
		public void service(ServerHttpConnection connection) throws IOException {

			// Validate the request
			HttpRequest request = connection.getRequest();
			assertSame("Incorrect method", HttpMethod.GET, request.getMethod());
			assertEquals("Incorrect URI", "/", request.getUri());
			assertSame("Incorrect version", HttpVersion.HTTP_1_1, request.getVersion());
			assertEquals("Should be no headers", 0, request.getHeaders().length());
			assertEquals("Should be no entity", -1, request.getEntity().read());

			// Send content for response (to ensure handled)
			HttpResponse response = connection.getResponse();
			response.getHeaders().addHeader("TEST", "Value");
			ServerWriter writer = connection.getResponse().getEntityWriter();
			writer.write("Hello World");
			writer.flush();
		}
	}

	/**
	 * Ensure can mock with no entity.
	 */
	public void testNoEntity() throws Exception {

		// Configure servicing
		this.compile.office((context) -> context.addSection("SERVICER", NoEntityHandler.class));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure can service request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		MockHttpResponse response = this.server.send(request);

		// Validate the response
		assertSame("Incorrect version", HttpVersion.HTTP_1_1, response.getVersion());
		assertSame("Incorrect status", HttpStatus.NO_CONTENT, response.getStatus());
		assertEquals("Should be just the one header", 1, response.getHeaders().size());
		assertEquals("Incorrect header value", "Value", response.getHeader("TEST").getValue());
		assertEquals("Should be no entity", "", response.getEntity(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
	}

	public static class NoEntityHandler {
		public void service(ServerHttpConnection connection) {
			connection.getResponse().getHeaders().addHeader("TEST", "Value");
		}
	}

	/**
	 * Ensure can mock multiple requests.
	 */
	public void testMultipleRequests() throws Exception {

		// Configure servicing
		this.compile.office((context) -> context.addSection("SERVICER", MultipleRequestHandler.class));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure service multiple requests
		for (int i = 0; i < 100; i++) {

			// Ensure can service request
			MockHttpRequestBuilder request = MockHttpServer.mockRequest();
			MockHttpResponse response = this.server.send(request);

			// Validate the response
			assertSame("Incorrect version", HttpVersion.HTTP_1_1, response.getVersion());
			assertSame("Incorrect status", HttpStatus.OK, response.getStatus());
			assertEquals("Incorrect response", "RESPONSE", response.getEntity(null));
		}
	}

	public static class MultipleRequestHandler {
		public void service(ServerHttpConnection connection) throws Exception {
			connection.getResponse().getEntityWriter().write("RESPONSE");
		}
	}

}