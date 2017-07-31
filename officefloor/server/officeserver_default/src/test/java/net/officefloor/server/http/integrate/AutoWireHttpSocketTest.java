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

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpTestUtil;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.source.HttpServerSocketManagedObjectSource;

/**
 * Ensure able to use {@link HttpServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireHttpSocketTest extends OfficeFrameTestCase {

	/**
	 * Ensure can call the auto-wired HTTP server.
	 */
	public void testCallAutoWiredHttpServer() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();
		final CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> HttpServerSocketManagedObjectSource.configure(context.getOfficeFloorDeployer(),
				PORT, context.getDeployedOffice(), "TEST", "handleRequest"));
		compile.office((context) -> {
			context.getOfficeArchitect().enableAutoWireObjects();
			context.addSection("TEST", MockSection.class);	
		});

		// Open the OfficeFloor
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();
		try {

			try (CloseableHttpClient client = HttpTestUtil.createHttpClient(false)) {

				// Send request
				HttpGet request = new HttpGet("http://localhost:" + PORT);
				org.apache.http.HttpResponse response = client.execute(request);

				// Ensure request successful
				assertEquals("Request must be successful", 200, response.getStatusLine().getStatusCode());

				// Ensure appropriate response
				assertEquals("Incorrect response", "hello world", HttpTestUtil.getEntityBody(response));
			}

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
		public void handleRequest(ServerHttpConnection connection) throws IOException {
			HttpResponse response = connection.getHttpResponse();
			Writer writer = new OutputStreamWriter(response.getEntity());
			writer.write("hello world");
			writer.flush();
		}
	}

}