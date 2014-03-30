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

import junit.framework.TestCase;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Tests the Secure Link.
 * 
 * @author Daniel Sagenschneider
 */
public class SecureLinkHttpServerTest extends TestCase {

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	@Override
	protected void tearDown() throws Exception {
		// Ensure stop
		try {
			this.client.close();
		} finally {
			WoofOfficeFloorSource.stop();
		}
	}

	/**
	 * Ensure the link is secure.
	 */
	// START SNIPPET: tutorial
	public void testLinkRenderedSecure() throws Exception {

		// Obtain the default host name for the link
		String hostName = HttpApplicationLocationManagedObjectSource
				.getDefaultHostName();

		// Start the server
		WoofOfficeFloorSource.start();

		// Obtain the page
		HttpResponse response = this.client.execute(new HttpGet("http://"
				+ hostName + ":7878"));
		String renderedPage = EntityUtils.toString(response.getEntity());

		// Ensure login form (link) is secure
		assertTrue(
				"Login form should be secure",
				renderedPage.contains("form action=\"https://" + hostName
						+ ":7979/-login.woof"));
	}
	// END SNIPPET: tutorial

}