package net.officefloor.tutorial.corshttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.tutorial.corshttpserver.Logic.Response;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests CORS.
 * 
 * @author Daniel Sagenschneider
 */
public class CorsHttpServerTest {

	// START SNIPPET: options
	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void options() {
		this.doCorsOptionsTest("/");
	}

	@Test
	public void optionsWithPath() {
		this.doCorsOptionsTest("/path");
	}

	@Test
	public void optionsWithPathAndQuery() {
		this.doCorsOptionsTest("/path?name=value");
	}

	private void doCorsOptionsTest(String path) {
		this.server.send(MockWoofServer.mockRequest(path).method(HttpMethod.OPTIONS)).assertResponse(204, "",
				"Access-Control-Allow-Origin", "*", "Access-Control-Allow-Methods", "*", "Access-Control-Allow-Headers",
				"*");
	}
	// END SNIPPET: options

	// START SNIPPET: intercept
	@Test
	public void request() throws Exception {
		this.server.send(MockWoofServer.mockRequest("/path")).assertJson(200, new Response("TEST"),
				"Access-Control-Allow-Origin", "*");
	}
	// END SNIPPET: intercept

}