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
		builder.addHttpHeader("TEST", "value");
		builder.getHttpEntity().write(1);

		// Obtain the mock HTTP request
		HttpRequest request = builder.build();
		assertSame("Incorrect method", HttpMethod.GET, request.getHttpMethod());
		assertEquals("Inocrrect request URI", "/", request.getRequestURI());
		assertSame("Incorrect version", HttpVersion.HTTP_1_1, request.getHttpVersion());
		assertEquals("Should have header", 1, request.getHttpHeaders().length());
		assertEquals("Incorrect header", "value", request.getHttpHeaders().getHeader("test").getValue());
		assertEquals("Incorrect entity", 1, request.getEntity().read());
	}

	/**
	 * Ensure can obtain mock {@link HttpResponse}.
	 */
	public void testHttpResponse() throws Exception {

		// Create and configure the response
		MockHttpResponseBuilder builder = MockHttpServer.mockResponse();
		builder.getHttpHeaders().addHeader("TEST", "value");
		builder.getEntity().write(1);

		// Validate the built response
		MockHttpResponse response = builder.build();
		assertSame("Incorrect version", HttpVersion.HTTP_1_1, response.getHttpVersion());
		assertSame("Incorrect status", HttpStatus.OK, response.getHttpStatus());
		assertEquals("Should have one header", 1, response.getHttpHeaders().size());
		assertEquals("Incorrect header value", "value", response.getFirstHeader("TEST").getValue());
		assertEquals("Incorrect response", 1, response.getHttpEntity().read());
		assertEquals("Should have read entity", -1, response.getHttpEntity().read());
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
		assertSame("Incorrect version", HttpVersion.HTTP_1_1, response.getHttpVersion());
		assertSame("Incorrect status", HttpStatus.OK, response.getHttpStatus());
		assertEquals("Should be one headers", 1, response.getHttpHeaders().size());
		assertEquals("Incorrect header value", "Value", response.getFirstHeader("TEST").getValue());
		assertEquals("Incorrect response", "Hello World",
				response.getHttpEntity(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
	}

	public static class SimpleRequestHandler {
		public void service(ServerHttpConnection connection) throws IOException {

			// Validate the request
			HttpRequest request = connection.getHttpRequest();
			assertSame("Incorrect method", HttpMethod.GET, request.getHttpMethod());
			assertEquals("Incorrect URI", "/", request.getRequestURI());
			assertSame("Incorrect version", HttpVersion.HTTP_1_1, request.getHttpVersion());
			assertEquals("Should be no headers", 0, request.getHttpHeaders().length());
			assertEquals("Should be no entity", -1, request.getEntity().read());

			// Send content for response (to ensure handled)
			HttpResponse response = connection.getHttpResponse();
			response.getHttpHeaders().addHeader("TEST", "Value");
			ServerWriter writer = connection.getHttpResponse().getEntityWriter();
			writer.write("Hello World");
			writer.flush();
		}
	}

}