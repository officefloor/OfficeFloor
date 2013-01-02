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
package net.officefloor.tutorial.servletmigration;

import java.io.ByteArrayOutputStream;

import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import junit.framework.TestCase;

/**
 * Tests the {@link ExampleHttpServlet} is servicing requests.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleHttpServletTest extends TestCase {

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * Ensure {@link ExampleHttpServlet} servicing request.
	 */
	// START SNIPPET: tutorial
	public void testExampleHttpServlet() throws Exception {

		// Start WoOF (with Servlet extension on class path)
		WoofOfficeFloorSource.start();

		// Undertake the request to be serviced by Servlet
		HttpGet request = new HttpGet("http://localhost:7878/example");
		HttpResponse response = this.client.execute(request);
		assertEquals("Ensure request successful", 200, response.getStatusLine()
				.getStatusCode());

		// Ensure response entity is as expected
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		assertEquals("Incorrect response entity", "SERVLET", buffer.toString());
	}
	// END SNIPPET: tutorial

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			this.client.getConnectionManager().shutdown();

		} finally {
			// Stop the server
			WoofOfficeFloorSource.stop();
		}
	}

}