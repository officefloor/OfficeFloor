/*-
 * #%L
 * Variable Tutorial
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.tutorial.variablehttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.model.test.variable.MockVar;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockObjectResponse;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the variables are passed between methods.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableHttpServerTest {

	// START SNIPPET: mockServer
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
	// END SNIPPET: mockServer

	// START SNIPPET: functions
	@Test
	public void setValues() {
		MockVar<Person> person = new MockVar<>();
		MockVar<String> description = new MockVar<>();
		new OutLogic().setValues(person, description);
		assertEquals("Incorrect first name", "Daniel", person.get().getFirstName());
		assertEquals("Incorrect second name", "Sagenschneider", person.get().getLastName());
		assertEquals("Incorrect description", "Need to watch his code!", description.get());
	}

	@Test
	public void sendValues() {
		MockObjectResponse<ServerResponse> response = new MockObjectResponse<>();
		new ValLogic().useValues(new Person("Daniel", "Sagenschneider"), "Need to watch his code!", response);
		assertEquals("Incorrect first name", "Daniel", response.getObject().getPerson().getFirstName());
		assertEquals("Incorrect second name", "Sagenschneider", response.getObject().getPerson().getLastName());
		assertEquals("Incorrect description", "Need to watch his code!", response.getObject().getDescription());
	}
	// END SNIPPET: functions
}
