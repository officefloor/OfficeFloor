/*-
 * #%L
 * Testing of HTTP Server
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

package net.officefloor.server.http.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
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
public class MockHttpServerTest {

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

	@BeforeEach
	protected void setUp() throws Exception {

		// Create the mock HTTP server
		this.compile.officeFloor((context) -> {
			DeployedOfficeInput input = context.getDeployedOffice().getDeployedOfficeInput("SERVICER", "service");
			this.server = MockHttpServer.configureMockHttpServer(input);
		});
	}

	@AfterEach
	protected void tearDown() throws Exception {

		// Ensure close OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can obtain the mock {@link HttpRequest}.
	 */
	@Test
	public void httpRequest() throws Exception {

		// Create and configure the request
		MockHttpRequestBuilder builder = MockHttpServer.mockRequest();
		builder.header("TEST", "value");
		builder.getHttpEntity().write(1);

		// Obtain the mock HTTP request
		HttpRequest request = builder.build();
		assertSame(HttpMethod.GET, request.getMethod(), "Incorrect method");
		assertEquals("/", request.getUri(), "Inocrrect request URI");
		assertSame(HttpVersion.HTTP_1_1, request.getVersion(), "Incorrect version");
		assertEquals(1, request.getHeaders().length(), "Should have header");
		assertEquals("value", request.getHeaders().getHeader("test").getValue(), "Incorrect header");
		assertEquals(1, request.getEntity().read(), "Incorrect entity");
	}

	/**
	 * Ensure can obtain mock {@link HttpResponse}.
	 */
	@Test
	public void httpResponse() throws Exception {

		// Create and configure the response
		MockHttpResponseBuilder builder = MockHttpServer.mockResponse();
		builder.getHeaders().addHeader("TEST", "value");
		builder.getEntity().write(1);

		// Validate the built response
		MockHttpResponse response = builder.build();
		assertSame(HttpVersion.HTTP_1_1, response.getVersion(), "Incorrect version");
		assertSame(HttpStatus.OK, response.getStatus(), "Incorrect status");
		assertEquals(3, response.getHeaders().size(), "Should have one header");
		assertEquals("application/octet-stream", response.getHeader("content-type").getValue(),
				"Incorrect content-type");
		assertEquals("1", response.getHeader("content-length").getValue(), "Incorrect content-length");
		assertEquals("value", response.getHeader("TEST").getValue(), "Incorrect header value");
		assertEquals(1, response.getEntity().read(), "Incorrect response");
		assertEquals(-1, response.getEntity().read(), "Should have read entity");
	}

	/**
	 * Ensure can mock a simple request.
	 */
	@Test
	public void simpleRequest() throws Exception {

		// Configure servicing
		this.compile.office((context) -> context.addSection("SERVICER", SimpleRequestHandler.class));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure can service request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		MockHttpResponse response = this.server.send(request);

		// Validate the response
		assertSame(HttpVersion.HTTP_1_1, response.getVersion(), "Incorrect version");
		assertSame(HttpStatus.OK, response.getStatus(), "Incorrect status");
		assertEquals(3, response.getHeaders().size(), "Should be one header (plus content-type and content-length)");
		assertEquals("text/plain", response.getHeader("content-type").getValue(), "Incorrect content-type");
		assertEquals("11", response.getHeader("content-length").getValue(), "Incorrect content-length");
		assertEquals("Value", response.getHeader("TEST").getValue(), "Incorrect header value");
		assertEquals("Hello World", response.getEntity(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET),
				"Incorrect response");
	}

	public static class SimpleRequestHandler {
		public void service(ServerHttpConnection connection) throws IOException {

			// Validate the request
			HttpRequest request = connection.getRequest();
			assertSame(HttpMethod.GET, request.getMethod(), "Incorrect method");
			assertEquals("/", request.getUri(), "Incorrect URI");
			assertSame(HttpVersion.HTTP_1_1, request.getVersion(), "Incorrect version");
			assertEquals(0, request.getHeaders().length(), "Should be no headers");
			assertEquals(-1, request.getEntity().read(), "Should be no entity");

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
	@Test
	public void noEntity() throws Exception {

		// Configure servicing
		this.compile.office((context) -> context.addSection("SERVICER", NoEntityHandler.class));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure can service request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		MockHttpResponse response = this.server.send(request);

		// Validate the response
		assertSame(HttpVersion.HTTP_1_1, response.getVersion(), "Incorrect version");
		assertSame(HttpStatus.NO_CONTENT, response.getStatus(), "Incorrect status");
		assertEquals(1, response.getHeaders().size(), "Should be just the one header");
		assertEquals("Value", response.getHeader("TEST").getValue(), "Incorrect header value");
		assertEquals("", response.getEntity(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET), "Should be no entity");
	}

	public static class NoEntityHandler {
		public void service(ServerHttpConnection connection) {
			connection.getResponse().getHeaders().addHeader("TEST", "Value");
		}
	}

	/**
	 * Ensure can mock multiple requests.
	 */
	@Test
	public void multipleRequests() throws Exception {

		// Configure servicing
		this.compile.office((context) -> context.addSection("SERVICER", MultipleRequestHandler.class));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure service multiple requests
		for (int i = 0; i < 100; i++) {

			// Ensure can service request
			MockHttpRequestBuilder request = MockHttpServer.mockRequest();
			MockHttpResponse response = this.server.send(request);

			// Validate the response
			assertSame(HttpVersion.HTTP_1_1, response.getVersion(), "Incorrect version");
			assertSame(HttpStatus.OK, response.getStatus(), "Incorrect status");
			assertEquals("RESPONSE", response.getEntity(null), "Incorrect response");
		}
	}

	public static class MultipleRequestHandler {
		public void service(ServerHttpConnection connection) throws Exception {
			connection.getResponse().getEntityWriter().write("RESPONSE");
		}
	}

	/**
	 * Ensure able to follow redirect.
	 * 
	 * @throws Exception
	 */
	@Test
	public void followRedirect() throws Exception {

		// Configure servicing
		this.compile.office((context) -> context.addSection("SERVICER", FollowRedirectHandler.class));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure follow redirect
		MockHttpResponse response = this.server.sendFollowRedirect(MockHttpServer.mockRequest("/initial"));
		response.assertResponse(200, "REDIRECTED");
	}

	public static class FollowRedirectHandler {
		public void service(ServerHttpConnection connection) throws Exception {
			HttpRequest request = connection.getRequest();
			HttpResponse response = connection.getResponse();
			switch (request.getUri()) {
			case "/initial":
				// Configure redirect
				response.setStatus(HttpStatus.SEE_OTHER);
				response.getHeaders().addHeader("location", "/redirect");

				// Ensure keeps cookies across redirect
				response.getCookies().setCookie("redirect", "cookie");
				break;

			case "/redirect":
				assertEquals("cookie", request.getCookies().getCookie("redirect").getValue(),
						"Ensure cookies passed on redirect");
				response.getEntityWriter().write("REDIRECTED");
				break;

			default:
				fail("Invalid URL: " + request.getUri());
				break;
			}
		}
	}

}
