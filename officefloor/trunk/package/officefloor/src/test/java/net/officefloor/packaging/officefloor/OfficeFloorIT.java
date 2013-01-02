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
package net.officefloor.packaging.officefloor;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Integration test to invoke the opened OfficeFloor.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorIT extends TestCase {

	/**
	 * Ensure application is running and can serve up the landing page.
	 */
	public void testRequestLandingPage() throws Exception {

		final HttpClient client = new DefaultHttpClient();

		// Request the landing page
		String landingPage = this.doRequest(client, "");

		// Ensure appropriate landing page
		assertTrue("Incorrect landing page:\n" + landingPage,
				landingPage.contains("OfficeFloor"));
	}

	/**
	 * Undertakes the request.
	 * 
	 * @param client
	 *            {@link HttpClient}.
	 * @param uri
	 *            URI.
	 * @return String content of response entity.
	 */
	private String doRequest(HttpClient client, String uri) throws Exception {
		HttpResponse response = client.execute(new HttpGet(
				"http://localhost:7878/" + uri));
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());
		StringWriter buffer = new StringWriter();
		Reader reader = new InputStreamReader(response.getEntity().getContent());
		for (int character = reader.read(); character != -1; character = reader
				.read()) {
			buffer.write(character);
		}
		return buffer.toString();
	}

}