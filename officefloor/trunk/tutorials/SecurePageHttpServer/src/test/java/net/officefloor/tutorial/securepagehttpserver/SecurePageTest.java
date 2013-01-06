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
package net.officefloor.tutorial.securepagehttpserver;

import java.io.IOException;

import junit.framework.TestCase;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Tests the Secure Page.
 * 
 * @author Daniel Sagenschneider
 */
public class SecurePageTest extends TestCase {

	private HttpClient client;

	@Override
	protected void setUp() throws Exception {
		this.client = new DefaultHttpClient();
		MockHttpServer.configureHttps(client, 7979);
	}

	@Override
	protected void tearDown() throws Exception {
		WoofOfficeFloorSource.stop();
	}

	// START SNIPPET: tutorial
	public void testSecurePage() throws Exception {

		// Start server
		WoofOfficeFloorSource.start();

		// Ensure redirect to secure access to page
		this.assertHttpRequest("http://localhost:7878/card.woof");

		// Ensure redirect to secure link access to page
		this.assertHttpRequest("http://localhost:7878/main-card.woof");
		
		// Post the card details
		this.assertHttpRequest("http://localhost:7878/card-save.woof?number=123");
	}

	private void assertHttpRequest(String url) throws IOException {
		HttpResponse response = this.client.execute(new HttpGet(url));
		assertEquals("Should be successful (after possible redirect)", 200,
				response.getStatusLine().getStatusCode());
		String entity = EntityUtils.toString(response.getEntity());
		assertTrue("Should be rendering page as secure (and not exception)",
				entity.contains("<h1>Enter card details</h1>"));
	}
	// END SNIPPET: tutorial

}