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
package net.officefloor.tutorial.securelinkhttpserver;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import junit.framework.TestCase;
import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.impl.HttpServerLocationImpl;

/**
 * Tests the Secure Link.
 * 
 * @author Daniel Sagenschneider
 */
public class SecureLinkHttpServerTest extends TestCase {

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpClientTestUtil.createHttpClient();

	@Override
	protected void tearDown() throws Exception {
		// Ensure stop
		try {
			this.client.close();
		} finally {
			OfficeFloorMain.close();
		}
	}

	/**
	 * Ensure the link is secure.
	 */
	// START SNIPPET: tutorial
	public void testLinkRenderedSecure() throws Exception {

		// Start the server
		OfficeFloorMain.open();

		// Obtain the host name
		String hostName = HttpServerLocationImpl.getDefaultHostName();

		// Obtain the page
		HttpResponse response = this.client.execute(new HttpGet("http://" + hostName + ":7878"));
		String renderedPage = EntityUtils.toString(response.getEntity());

		System.out.println("page: " + renderedPage);

		// Ensure login form (link) is secure
		assertTrue("Login form should be secure",
				renderedPage.contains("form action=\"https://" + hostName + ":7979/+login"));
	}
	// END SNIPPET: tutorial

}