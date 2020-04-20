package net.officefloor.tutorial.springcontrollerhttpserver;

import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.stereotype.Controller;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Spring {@link Controller} HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringControllerHttpServerTest {

	@ClassRule
	public static final MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void passThroughGet() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/rest"));
		response.assertJson(200, new ResponseModel("GET Spring Dependency"));
	}

	@Test
	public void officeFloorGet() {
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
		response.assertJson(200, new ResponseModel("INPUT"));
	}

	@Test
	public void html() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/officefloor/html?name=Daniel"));
		response.assertResponse(200, "<html><body><p >Hello Daniel</p></body></html>");
	}

}