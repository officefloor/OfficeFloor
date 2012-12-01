/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.tutorial.rawhttpserver;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the web application is returning correctly.
 * 
 * @author Daniel Sagenschneider
 */
public class RawHttpServerTest extends TestCase {

	/**
	 * Ensure able to obtain the Raw HTML.
	 */
	public void testRawHtml() throws Exception {

		// Start server
		WoofOfficeFloorSource.start();

		// Send request for dynamic page
		HttpResponse response = new DefaultHttpClient().execute(new HttpGet(
				"http://localhost:7878/example.woof"));

		// Ensure request is successful
		assertEquals("Request should be successful", 200, response
				.getStatusLine().getStatusCode());

		// Indicate response
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String responseText = new String(buffer.toByteArray());

		// Ensure raw html rendered to page
		assertTrue("Should have raw HTML rendered",
				responseText.contains("Web on OfficeFloor (WoOF)"));
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop server
		WoofOfficeFloorSource.stop();
	}

}