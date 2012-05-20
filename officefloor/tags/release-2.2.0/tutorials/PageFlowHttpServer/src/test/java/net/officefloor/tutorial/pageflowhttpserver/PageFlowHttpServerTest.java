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

package net.officefloor.tutorial.pageflowhttpserver;

import junit.framework.TestCase;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the {@link PageFlowHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class PageFlowHttpServerTest extends TestCase {

	// START SNIPPET: test
	private final HttpClient client = new DefaultHttpClient();

	public void testPageInteraction() throws Exception {

		// Start server
		WoofOfficeFloorSource.main();

		// Request the template
		this.doRequest("http://localhost:7878/example");

		// Add an item
		this.doRequest("http://localhost:7878/example.links-addItem.task?name=Daniel&description=founder");

		// Clear the items
		this.doRequest("http://localhost:7878/example.links-clear.task");
	}

	private void doRequest(String url) throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(url));
		assertEquals("Request should be successful", 200, response
				.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);
	}
	// END SNIPPET: test

	@Override
	protected void tearDown() throws Exception {
		this.client.getConnectionManager().shutdown();
		AutoWireManagement.closeAllOfficeFloors();
	}

}