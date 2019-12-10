/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.tutorial.activityhttpserver;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Activity HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityHttpServerTest {

	// START SNIPPET: tutorial
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void correctDepth() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest());
		response.assertResponse(200, mapper.writeValueAsString(new Depth(2)));
	}
	// END SNIPPET: tutorial
}