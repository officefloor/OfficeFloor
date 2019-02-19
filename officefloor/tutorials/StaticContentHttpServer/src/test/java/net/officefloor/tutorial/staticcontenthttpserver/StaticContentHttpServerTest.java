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
package net.officefloor.tutorial.staticcontenthttpserver;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the REST end points.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticContentHttpServerTest {

	// START SNIPPET: tutorial
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void indexPage() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/"));
		response.assertResponse(200, "<html><body>Hello World</body></html>");
	}
	// END SNIPPET: tutorial

}