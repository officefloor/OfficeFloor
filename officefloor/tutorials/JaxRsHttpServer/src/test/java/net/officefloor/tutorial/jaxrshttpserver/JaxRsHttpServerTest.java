package net.officefloor.tutorial.jaxrshttpserver;

import org.junit.ClassRule;
import org.junit.Test;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.servlet.supply.ServletWoofExtensionService;
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
	public static final MockWoofServerRule server = new MockWoofServerRule()
			.property(ServletWoofExtensionService.getChainServletsPropertyName("OFFICE"), "true");

	@Test
	public void get() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/jaxrs"));
		response.assertJson(200, new ResponseModel("GET JAX-RS Dependency"));
	}

	@Test
	public void pathParam() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/jaxrs/path/parameter"));
		response.assertJson(200, new ResponseModel("parameter"));
	}

	@Test
	public void post() {
		MockWoofResponse response = server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/jaxrs/update", new RequestModel("INPUT")));
		response.assertJson(200, new ResponseModel("INPUT"));
	}
	// END SNIPPET: tutorial

}