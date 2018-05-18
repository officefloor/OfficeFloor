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

import junit.framework.TestCase;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure correctly renders the page.
 * 
 * @author Daniel Sagenschneider
 */
public class DiPojoHttpServerTest extends TestCase {

	private MockWoofServer server;

	@Override
	protected void tearDown() throws Exception {
		this.server.close();
	}

	/**
	 * Ensure render page correctly.
	 */
	// START SNIPPET: test
	public void testRenderPage() throws Exception {

		// Start the server
		this.server = MockWoofServer.open();

		// Obtain the page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/template"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure page contains correct rendered content
		String page = response.getEntity(null);
		assertTrue("Ensure correct page content", page.contains("Hello World"));
	}
	// END SNIPPET: test

}