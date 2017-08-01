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

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import junit.framework.TestCase;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;

/**
 * Abstract {@link TestCase} for testing a {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpServerImplementationTest extends TestCase {

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
			connection.getHttpResponse().getEntityWriter().write("ECHO");
		}
	}

	@Override
	protected void tearDown() throws Exception {

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
			assertEquals("Incorrect response", "ECHO", HttpClientTestUtil.getEntityBody(response));
		}
	}

	/**
	 * Ensure can send a single HTTPS request.
	 */
	public void testSingleSecureRequest() throws IOException {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(true)) {
			HttpResponse response = client.execute(new HttpGet("https://localhost:7979/test"));
			assertEquals("Incorrect status", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect response", "ECHO", HttpClientTestUtil.getEntityBody(response));
		}
	}

	/**
	 * Ensure can send multiple HTTP requests.
	 */
	public void testMultipleRequests() {
		fail("TODO ensure can send multiple requests");
	}

	/**
	 * Ensure can send multiple HTTPS requests.
	 */
	public void testMultipleSecureRequests() {
		fail("TODO ensure can send multiple secure requests");
	}

	/**
	 * Ensure can pipeline HTTP requests.
	 */
	public void testPipelining() {
		fail("TODO ensure can pipeline requests");
	}

	/**
	 * Ensure can pipeline HTTPS requests.
	 */
	public void testSecurePipelining() {
		fail("TODO ensure can secure pipeline requests");
	}

	/**
	 * Ensure can service multiple requests pipelined.
	 */
	public void testHeavyLoad() {
		fail("TODO performance test heavy load");
	}

	/**
	 * Ensure can service multiple secure requests pipelined.
	 */
	public void testSecureHeavyLoad() {
		fail("TODO performance test secure heavy load");
	}

}