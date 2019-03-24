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
package net.officefloor.tutorial.variablehttpserver;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the variables are passed between methods.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableHttpServerTest {

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void outIn() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/outIn"));
		response.assertResponse(200, mapper.writeValueAsString(
				new ServerResponse(new Person("Daniel", "Sagenschneider"), "Need to watch his code!")));
	}

	@Test
	public void varVal() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/varVal"));
		response.assertResponse(200, mapper.writeValueAsString(
				new ServerResponse(new Person("Daniel", "Sagenschneider"), "Need to watch his code!")));
	}

	@Test
	public void outVal() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/outVal"));
		response.assertResponse(200, mapper.writeValueAsString(
				new ServerResponse(new Person("Daniel", "Sagenschneider"), "Need to watch his code!")));
	}
}