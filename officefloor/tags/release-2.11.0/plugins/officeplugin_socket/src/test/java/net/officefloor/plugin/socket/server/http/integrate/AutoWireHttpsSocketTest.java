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
package net.officefloor.plugin.socket.server.http.integrate;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.net.ssl.SSLPeerUnverifiedException;

import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpsServerSocketManagedObjectSource;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Ensure able to use {@link HttpsServerSocketManagedObjectSource} with the
 * {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireHttpsSocketTest extends OfficeFrameTestCase {

	/**
	 * Port for the test.
	 */
	private int port;

	/**
	 * {@link AutoWireOfficeFloorSource}.
	 */
	private final AutoWireOfficeFloorSource autoWire = new AutoWireOfficeFloorSource();

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor = null;

	@Override
	protected void setUp() throws Exception {

		// Obtain the port for the test
		this.port = MockHttpServer.getAvailablePort();

		// Add the section to handle the HTTP request
		this.autoWire.addSection("TEST", ClassSectionSource.class.getName(),
				MockSection.class.getName());

	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Ensure stop the client
			this.client.getConnectionManager().shutdown();

		} finally {
			// Ensure OfficeFloor is closed
			if (this.officeFloor != null) {
				this.officeFloor.closeOfficeFloor();
			}
		}
	}

	/**
	 * Ensure reports back if no matching cypher.
	 */
	public void testNoMatchingCypherForHttpsServer() throws Exception {

		// Register the managed object source
		HttpsServerSocketManagedObjectSource.autoWire(this.autoWire, this.port,
				null, "TEST", "handleRequest");

		// Open the OfficeFloor
		this.officeFloor = this.autoWire.openOfficeFloor();

		// Send request
		try {
			this.client.execute(new HttpGet("https://localhost:" + this.port));
			fail("Should not be successful");

		} catch (SSLPeerUnverifiedException ex) {
			assertEquals("Incorrect cause", "peer not authenticated",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can call the auto-wired HTTPS server.
	 */
	public void testCallAutoWiredHttpsServer() throws Exception {

		// Register the managed object source
		HttpsServerSocketManagedObjectSource.autoWire(this.autoWire, this.port,
				MockHttpServer.getSslEngineSourceClass(), "TEST",
				"handleRequest");

		// Open the OfficeFloor
		this.officeFloor = this.autoWire.openOfficeFloor();

		// Send request (with anonymous connection)
		MockHttpServer.configureHttps(this.client, this.port);
		HttpGet request = new HttpGet("https://localhost:" + this.port);
		org.apache.http.HttpResponse response = this.client.execute(request);

		// Ensure request successful
		assertEquals("Request must be successful", 200, response
				.getStatusLine().getStatusCode());

		// Ensure appropriate response
		assertEquals("Incorrect response", "hello world",
				MockHttpServer.getEntityBody(response));
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
		public void handleRequest(ServerHttpConnection connection)
				throws IOException {
			HttpResponse response = connection.getHttpResponse();
			Writer writer = new OutputStreamWriter(response.getEntity());
			writer.write("hello world");
			writer.flush();
		}
	}

}