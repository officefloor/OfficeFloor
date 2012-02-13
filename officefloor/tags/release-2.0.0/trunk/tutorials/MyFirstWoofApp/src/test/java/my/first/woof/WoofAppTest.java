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
package my.first.woof;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Tests that the WoOF App operates.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofAppTest extends TestCase {

	/**
	 * Ensure able to service with Hello World.
	 */
	public void testHelloWorld() throws Exception {

		// Run on non-conflicting port
		final int port = 18080;
		Server server = new Server(port);
		HttpClient client = new DefaultHttpClient();
		try {

			// Configure the server
			WebAppContext context = new WebAppContext();
			context.setResourceBase("./src/main/webapp");
			context.setContextPath("/");
			context.setParentLoaderPriority(true);
			server.setHandler(context);
			server.start();

			// Ensure can get Hello World
			HttpResponse response = client.execute(new HttpGet(
					"http://localhost:" + port + "/template"));
			assertEquals("Should be successful", 200, response.getStatusLine()
					.getStatusCode());

			// Ensure response contains Hello World
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			response.getEntity().writeTo(buffer);
			String body = buffer.toString();
			System.out.println("RESPONSE: " + body);
			assertTrue("Ensure contains Hello World",
					body.contains("Hello World"));

		} finally {
			// Ensure stop client and server
			client.getConnectionManager().shutdown();
			server.stop();
		}
	}

}