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
package net.officefloor.tutorial.javascripthttpserver;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the JavaScript HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaScriptHttpServerTest {

	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule();

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void invalidIdentifier() throws Exception {
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest().header("Content-Type", "application/json")
						.entity(mapper.writeValueAsString(new Request(-1, "Daniel"))));
		response.assertResponse(400, "Invalid identifier");
	}

	@Test
	public void invalidName() throws Exception {
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest().entity(mapper.writeValueAsString(new Request(1, ""))));
		response.assertResponse(400, "Must provide name");
	}

	@Test
	public void validRequest() throws Exception {
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest().entity(mapper.writeValueAsString(new Request(1, ""))));
		response.assertResponse(200, mapper.writeValueAsString(new Response("successful")));
	}
}