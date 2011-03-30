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
package net.officefloor.example.dynamichttpserver;

import junit.framework.TestCase;
import net.officefloor.plugin.autowire.AutoWireAdministration;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the {@link DynamicHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamicHttpServerTest extends TestCase {

	// START SNIPPET: test
	public void testDynamicPage() throws Exception {

		// Start server
		DynamicHttpServer.main(new String[0]);

		// Send request for dynamic page
		HttpResponse response = new DefaultHttpClient().execute(new HttpGet(
				"http://localhost:7878/example"));

		// Ensure request is successful
		assertEquals("Request should be successful", 200, response
				.getStatusLine().getStatusCode());

		// Indicate response
		response.getEntity().writeTo(System.out);
	}
	// END SNIPPET: test

	@Override
	protected void tearDown() throws Exception {
		AutoWireAdministration.closeAllOfficeFloors();
	}

}