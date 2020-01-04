package net.officefloor.tutorial.corshttpserver;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.tutorial.corshttpserver.Logic.Response;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests CORS.
 * 
 * @author Daniel Sagenschneider
 */
public class CorsHttpServerTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	// START SNIPPET: options
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

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
		this.server.send(MockWoofServer.mockRequest("/path")).assertResponse(200,
				mapper.writeValueAsString(new Response("TEST")), "Access-Control-Allow-Origin", "*", "Content-Type",
				"application/json");
	}
	// END SNIPPET: intercept

}