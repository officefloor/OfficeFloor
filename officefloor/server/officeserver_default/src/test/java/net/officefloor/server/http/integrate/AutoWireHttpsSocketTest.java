/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.http.integrate;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpTestUtil;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.source.HttpsServerSocketManagedObjectSource;

/**
 * Ensure able to use {@link HttpsServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireHttpsSocketTest extends OfficeFrameTestCase {

	/**
	 * {@link CompileOfficeFloor}.
	 */
	private final CompileOfficeFloor compile = new CompileOfficeFloor();

	/**
	 * Port for the test.
	 */
	private int port;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	@Override
	protected void setUp() throws Exception {

		// Obtain the port for the test
		this.port = HttpTestUtil.getAvailablePort();

		// Add the section to handle the HTTP request
		this.compile.office((context) -> context.addSection("TEST", MockSection.class));
	}

	@Override
	protected void tearDown() throws Exception {
		// Ensure OfficeFloor is closed
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure reports back if no matching cypher.
	 */
	public void testNoMatchingCypherForHttpsServer() throws Exception {

		// Register the managed object source
		this.compile.officeFloor(
				(context) -> HttpsServerSocketManagedObjectSource.configure(context.getOfficeFloorDeployer(), this.port,
						null, context.getDeployedOffice(), "TEST", "handleRequest"));

		// Open the OfficeFloor
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Use default SslContext which should not match on cypher
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

			// Send request
			try {
				client.execute(new HttpGet("https://localhost:" + this.port));
				fail("Should not be successful");

			} catch (SSLHandshakeException ex) {
				assertEquals("Incorrect cause", "Remote host closed connection during handshake", ex.getMessage());
			}
		}
	}

	/**
	 * Ensure can call the auto-wired HTTPS server.
	 */
	public void testCallAutoWiredHttpsServer() throws Exception {

		// Register the managed object source
		this.compile.officeFloor(
				(context) -> HttpsServerSocketManagedObjectSource.configure(context.getOfficeFloorDeployer(), this.port,
						HttpTestUtil.getSslEngineSourceClass(), context.getDeployedOffice(), "TEST", "handleRequest"));

		// Open the OfficeFloor
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request (with OfficeFloor test SslContext)
		try (CloseableHttpClient client = HttpTestUtil.createHttpClient(true)) {

			HttpGet request = new HttpGet("https://localhost:" + this.port);
			org.apache.http.HttpResponse response = client.execute(request);

			// Ensure request successful
			assertEquals("Request must be successful", 200, response.getStatusLine().getStatusCode());

			// Ensure appropriate response
			assertEquals("Incorrect response", "hello world", HttpTestUtil.getEntityBody(response));
		}
	}

	/**
	 * Section logic to handle the HTTP request.
	 */
	public static class MockSection {

		/**
		 * Handles the request for testing.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 */
		public void handleRequest(ServerHttpConnection connection) throws IOException {
			HttpResponse response = connection.getHttpResponse();
			Writer writer = new OutputStreamWriter(response.getEntity());
			writer.write("hello world");
			writer.flush();
		}
	}

}