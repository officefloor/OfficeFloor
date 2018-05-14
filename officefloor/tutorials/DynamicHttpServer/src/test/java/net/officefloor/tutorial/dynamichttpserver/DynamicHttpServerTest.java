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
package net.officefloor.tutorial.dynamichttpserver;

import junit.framework.TestCase;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link DynamicHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamicHttpServerTest extends TestCase {

	/**
	 * {@link MockWoofServer}.
	 */
	private MockWoofServer server = null;

	// START SNIPPET: pojo
	public void testTemplateLogic() {

		TemplateLogic logic = new TemplateLogic();

		assertEquals("Number of properties", System.getProperties().size(),
				logic.getTemplateData().getProperties().length);

	}

	// END SNIPPET: pojo

	public void testDynamicPage() throws Exception {

		// Start server
		this.server = MockWoofServer.open();

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));

		// Ensure request is successful
		assertEquals("Request should be successful", 200, response.getStatus().getStatusCode());
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.server != null) {
			this.server.close();
		}
	}

}