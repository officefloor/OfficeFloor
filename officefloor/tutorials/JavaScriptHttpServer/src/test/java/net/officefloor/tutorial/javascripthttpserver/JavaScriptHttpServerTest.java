package net.officefloor.tutorial.javascripthttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the JavaScript HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaScriptHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void invalidIdentifier() throws Exception {
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest().header("Content-Type", "application/json")
						.entity(mapper.writeValueAsString(new Request(-1, "Daniel"))));
		response.assertResponse(400, "{\"error\":\"Invalid identifier\"}");
	}

	@Test
	public void invalidName() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest()
				.header("Content-Type", "application/json").entity(mapper.writeValueAsString(new Request(1, ""))));
		response.assertResponse(400, "{\"error\":\"Must provide name\"}");
	}

	@Test
	public void validRequest() throws Exception {
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest().header("Content-Type", "application/json")
						.entity(mapper.writeValueAsString(new Request(1, "Daniel"))));
		response.assertResponse(200, mapper.writeValueAsString(new Response("successful")));
	}
	// END SNIPPET: tutorial
}