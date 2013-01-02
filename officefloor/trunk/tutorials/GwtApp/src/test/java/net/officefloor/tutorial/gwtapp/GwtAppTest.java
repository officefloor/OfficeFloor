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
package net.officefloor.tutorial.gwtapp;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the GWT App.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtAppTest extends TestCase {

	/**
	 * Ensure includes GWT script and iframe.
	 */
	public void testIncludeGwtAspects() throws Exception {
		final HttpClient client = new DefaultHttpClient();
		try {
			// Start server
			WoofOfficeFloorSource.start();

			// Send request for page
			HttpResponse response = client.execute(new HttpGet(
					"http://localhost:7878/template.woof"));

			// Ensure request is successful
			assertEquals("Request should be successful", 200, response
					.getStatusLine().getStatusCode());

			// Obtain response body
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			response.getEntity().writeTo(buffer);
			String body = buffer.toString();

			// Ensure contains GWT script and iframe
			assertTrue("Should include GWT script",
					body.contains("src=\"template/template.nocache.js\""));
			assertTrue("Should include GWT iframe", body.contains("<iframe"));

		} finally {
			// Stop the client and server
			client.getConnectionManager().shutdown();
			WoofOfficeFloorSource.stop();
		}
	}

}