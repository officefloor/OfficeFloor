/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireOfficeFloorSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;

/**
 * Ensure able to use {@link HttpServerSocketManagedObjectSource} with the
 * {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireHttpSocketTest extends OfficeFrameTestCase {

	/**
	 * Ensure can call the auto-wired HTTP server.
	 */
	public void testCallAutoWiredHttpServer() throws Exception {

		final int PORT = MockHttpServer.getAvailablePort();
		final AutoWireOfficeFloorSource autoWire = new AutoWireOfficeFloorSource();

		// Add the section to handle the HTTP request
		autoWire.addSection("TEST", ClassSectionSource.class,
				MockSection.class.getName());

		// Register the managed object source
		HttpServerSocketManagedObjectSource.autoWire(autoWire, PORT, "TEST",
				"handleRequest");

		// Open the OfficeFloor
		OfficeFloor officeFloor = autoWire.openOfficeFloor();
		try {

			// Send request
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet("http://localhost:" + PORT);
			org.apache.http.HttpResponse response = client.execute(request);

			// Ensure request successful
			assertEquals("Request must be successful", 200, response
					.getStatusLine().getStatusCode());

			// Ensure appropriate response
			assertEquals("Incorrect response", "hello world",
					MockHttpServer.getEntityBody(response));

		} finally {
			// Ensure OfficeFloor is closed
			if (officeFloor != null) {
				officeFloor.closeOfficeFloor();
			}
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
		public void handleRequest(ServerHttpConnection connection)
				throws IOException {
			HttpResponse response = connection.getHttpResponse();
			Writer writer = new OutputStreamWriter(response.getBody()
					.getOutputStream());
			writer.write("hello world");
			writer.flush();
		}
	}

}