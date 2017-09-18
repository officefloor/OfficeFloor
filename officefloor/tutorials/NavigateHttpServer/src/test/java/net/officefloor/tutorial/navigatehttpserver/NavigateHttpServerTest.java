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
package net.officefloor.tutorial.navigatehttpserver;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import junit.framework.TestCase;
import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientTestUtil;

/**
 * Tests the {@link NavigateHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class NavigateHttpServerTest extends TestCase {

	// START SNIPPET: test
	private final CloseableHttpClient client = HttpClientTestUtil.createHttpClient();

	public void testNavigate() throws Exception {

		// Start server
		OfficeFloorMain.open();

		// Request template one
		this.doRequest("http://localhost:7878/one.woof");

		// Click on link on template one
		this.doRequest("http://localhost:7878/one-navigate.woof");

		// Submit on template two
		this.doRequest("http://localhost:7878/two-process.woof");
	}

	private void doRequest(String url) throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(url));
		assertEquals("Request should be successful", 200, response.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);
	}

	// END SNIPPET: test

	@Override
	protected void tearDown() throws Exception {
		this.client.close();
		OfficeFloorMain.close();
	}

}