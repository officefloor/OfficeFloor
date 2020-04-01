package net.officefloor.tutorial.servlethttpserver;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests {@link Servlet} and {@link Filter} being used in {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpServerTest {

	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void filterResponse() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/?filter=true"));
		response.assertResponse(200, "FILTER WITH DEPENDENCY");
	}

	@Test
	public void servletResponse() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/"));
		response.assertResponse(200, "SERVLET WITH DEPENDENCY");
	}

}