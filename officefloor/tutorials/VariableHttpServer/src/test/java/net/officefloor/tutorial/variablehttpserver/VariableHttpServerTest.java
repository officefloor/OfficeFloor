package net.officefloor.tutorial.variablehttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.model.test.variable.MockVar;
import net.officefloor.woof.mock.MockObjectResponse;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the variables are passed between methods.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableHttpServerTest {

	// START SNIPPET: mockServer
	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void outIn() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/outIn"));
		response.assertJson(200, new ServerResponse(new Person("Daniel", "Sagenschneider"), "Need to watch his code!"));
	}

	@Test
	public void varVal() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/varVal"));
		response.assertJson(200, new ServerResponse(new Person("Daniel", "Sagenschneider"), "Need to watch his code!"));
	}

	@Test
	public void outVal() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/outVal"));
		response.assertJson(200, new ServerResponse(new Person("Daniel", "Sagenschneider"), "Need to watch his code!"));
	}
	// END SNIPPET: mockServer

	// START SNIPPET: functions
	@Test
	public void setValues() {
		MockVar<Person> person = new MockVar<>();
		MockVar<String> description = new MockVar<>();

		OutLogic.setValues(person, description);

		assertEquals("Daniel", person.get().getFirstName(), "Incorrect first name");
		assertEquals("Sagenschneider", person.get().getLastName(), "Incorrect second name");
		assertEquals("Need to watch his code!", description.get(), "Incorrect description");
	}

	@Test
	public void sendValues() {
		MockObjectResponse<ServerResponse> response = new MockObjectResponse<>();

		ValLogic.useValues(new Person("Daniel", "Sagenschneider"), "Need to watch his code!", response);

		assertEquals("Daniel", response.getObject().getPerson().getFirstName(), "Incorrect first name");
		assertEquals("Sagenschneider", response.getObject().getPerson().getLastName(), "Incorrect second name");
		assertEquals("Need to watch his code!", response.getObject().getDescription(), "Incorrect description");
	}
	// END SNIPPET: functions
}