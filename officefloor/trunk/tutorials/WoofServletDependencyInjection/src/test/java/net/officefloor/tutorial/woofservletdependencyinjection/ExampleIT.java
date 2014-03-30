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
package net.officefloor.tutorial.woofservletdependencyinjection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Tests the {@link ExampleServlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleIT extends TestCase {

	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	@Override
	protected void tearDown() throws Exception {
		this.client.close();
	}

	// START SNIPPET: example
	public void testTemplate() throws IOException {

		// Undertake the request
		HttpGet request = new HttpGet(
				"http://localhost:18181/test/template.woof");
		HttpResponse response = this.client.execute(request);
		assertEquals("Request should be successful", 200, response
				.getStatusLine().getStatusCode());

		// Ensure content is as expected
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		assertEquals("Incorrect response entity",
				"<html><body>MESSAGE</body></html>", buffer.toString());
	}
	// END SNIPPET: example

}