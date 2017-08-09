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
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;

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
			DeployedOfficeInput input = context.getOfficeFloorDeployer()
					.getDeployedOffice(context.getDeployedOffice().getDeployedOfficeName())
					.getDeployedOfficeInput("SERVICER", "service");
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
	 * Ensure can mock a simple request.
	 */
	public void testSimpleRequest() throws Exception {

		// Configure servicing
		this.compile.office((context) -> context.addSection("SERVICER", SimpleRequestHandler.class));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure can service request
		MockHttpRequestBuilder request = this.server.createMockHttpRequest();
		MockHttpResponse response = this.server.send(request);

		// Validate the response
		assertSame("Incorrect version", HttpVersion.HTTP_1_1, response.getHttpVersion());
		assertSame("Incorrect status", HttpStatus.OK, response.getHttpStatus());
		assertEquals("Should be no headers", 0, response.getHttpHeaders().size());
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
			connection.getHttpResponse().getEntityWriter().write("Hello World");
		}
	}

}