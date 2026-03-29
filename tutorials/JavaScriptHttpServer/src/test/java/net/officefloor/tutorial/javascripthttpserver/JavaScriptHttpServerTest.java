package net.officefloor.tutorial.javascripthttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
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

	@Test
	public void invalidIdentifier() throws Exception {
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.GET, "/", new Request(-1, "Daniel")));
		response.assertJsonError(new HttpException(400, "Invalid identifier"));
	}

	@Test
	public void invalidName() throws Exception {
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.GET, "/", new Request(1, "")));
		response.assertJsonError(new HttpException(400, "Must provide name"));
	}

	@Test
	public void validRequest() throws Exception {
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.GET, "/", new Request(1, "Daniel")));
		response.assertJson(200, new Response("successful"));
	}
	// END SNIPPET: tutorial
}