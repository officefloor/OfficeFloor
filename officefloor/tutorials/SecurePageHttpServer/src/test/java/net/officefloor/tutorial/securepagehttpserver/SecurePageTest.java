/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.securepagehttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the Secure Page.
 * 
 * @author Daniel Sagenschneider
 */
public class SecurePageTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: tutorial
	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule(true);

	@Test
	public void testSecurePage() throws Exception {

		// Ensure redirect to secure access to page
		this.assertHttpRequest("http://localhost:7878/card");

		// Ensure redirect to secure link access to page
		this.assertHttpRequest("http://localhost:7878/main+card");
	}

	private void assertHttpRequest(String url) throws IOException {
		HttpResponse response = this.client.execute(new HttpGet(url));
		assertEquals("Should be successful (after possible redirect)", 200, response.getStatusLine().getStatusCode());
		String entity = EntityUtils.toString(response.getEntity());
		assertTrue("Should be rendering page as secure (and not exception)",
				entity.contains("<h1>Enter card details</h1>"));
	}
	// END SNIPPET: tutorial

}