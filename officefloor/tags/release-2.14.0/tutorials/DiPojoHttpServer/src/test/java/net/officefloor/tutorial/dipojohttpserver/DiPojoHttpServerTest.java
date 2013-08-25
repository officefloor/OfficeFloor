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
package net.officefloor.tutorial.dipojohttpserver;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import junit.framework.TestCase;

/**
 * Ensure correctly renders the page.
 * 
 * @author Daniel Sagenschneider
 */
public class DiPojoHttpServerTest extends TestCase {

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

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

	/**
	 * Ensure render page correctly.
	 */
	// START SNIPPET: test
	public void testRenderPage() throws Exception {

		// Start the server
		WoofOfficeFloorSource.start();

		// Obtain the page
		HttpResponse response = this.client.execute(new HttpGet(
				"http://localhost:7878/template.woof"));
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Ensure page contains correct rendered content
		String page = EntityUtils.toString(response.getEntity());
		assertTrue("Ensure correct page content", page.contains("Hello World"));
	}
	// END SNIPPET: test

}