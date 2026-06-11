package net.officefloor.tutorial.servlethttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests {@link Servlet} and {@link Filter} being used in {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

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
	// END SNIPPET: tutorial
}
