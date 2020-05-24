package net.officefloor.tutorial.jaxrshttpserver;

import org.junit.ClassRule;
import org.junit.Test;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the JAX-RS HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsHttpServerTest {

	// START SNIPPET: tutorial
	@ClassRule
	public static final MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void get() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/officefloor"));
		response.assertResponse(200, "GET OfficeFloor Dependency");
	}

	@Test
	public void pathParam() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/officefloor/changed/parameter"));
		response.assertJson(200, new ResponseModel("parameter"));
	}

	@Test
	public void post() {
		MockWoofResponse response = server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.PUT, "/officefloor/update", new RequestModel("INPUT")));
		response.assertJson(200, new ResponseModel("INPUT"));
	}
	// END SNIPPET: tutorial

}