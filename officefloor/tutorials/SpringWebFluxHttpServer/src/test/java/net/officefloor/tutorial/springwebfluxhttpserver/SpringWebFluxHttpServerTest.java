package net.officefloor.tutorial.springwebfluxhttpserver;

import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.stereotype.Controller;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Spring Web MVC {@link Controller} HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebFluxHttpServerTest {

	// START SNIPPET: tutorial
	@ClassRule
	public static final MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void get() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/rest"));
		response.assertJson(200, new ResponseModel("GET Spring Dependency"));
	}

	@Test
	public void pathParam() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/rest/path/parameter"));
		response.assertJson(200, new ResponseModel("parameter"));
	}

	@Test
	public void post() {
		MockWoofResponse response = server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/rest/update", new RequestModel("INPUT")));
		response.assertJson(200, new ResponseModel[] { new ResponseModel("INPUT"), new ResponseModel("ANOTHER") });
	}
	// END SNIPPET: tutorial

}