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