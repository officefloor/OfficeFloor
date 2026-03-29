package net.officefloor.tutorial.springwebfluxhttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.stereotype.Controller;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the Spring Web MVC {@link Controller} HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebFluxProcedureHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public static final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void get() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/officefloor/rest"));
		response.assertJson(200, new ResponseModel("GET Spring Dependency"));
	}

	@Test
	public void pathParam() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/officefloor/changed/parameter"));
		response.assertJson(200, new ResponseModel("parameter"));
	}

	@Test
	public void put() {
		MockWoofResponse response = server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.PUT, "/officefloor/update", new RequestModel("INPUT")));
		response.assertJson(200, new ResponseModel[] { new ResponseModel("INPUT"), new ResponseModel("ANOTHER") });
	}
	// END SNIPPET: tutorial

}