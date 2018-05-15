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
package net.officefloor.tutorial.sectionhttpserver;

import junit.framework.TestCase;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionHttpServerTest extends TestCase {

	/**
	 * {@link MockWoofServer}.
	 */
	private MockWoofServer server;

	@Override
	protected void setUp() throws Exception {
		this.server = MockWoofServer.open();
	}

	@Override
	protected void tearDown() throws Exception {
		this.server.close();
	}

	public void testPageRendering() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));

		// Ensure request is successful
		assertEquals("Request should be successful", 200, response.getStatus().getStatusCode());

		// Ensure correct response
		String responseText = response.getEntity(null);
		assertTrue("Missing template section", responseText.contains("<p>Hi</p>"));
		assertTrue("Missing Hello section", responseText.contains("<p>Hello</p>"));
		assertFalse("NotRender section should not be rendered", responseText.contains("<p>Not rendered</p>"));
		assertTrue("Missing NoBean section", responseText.contains("<p>How are you?</p>"));
	}

}