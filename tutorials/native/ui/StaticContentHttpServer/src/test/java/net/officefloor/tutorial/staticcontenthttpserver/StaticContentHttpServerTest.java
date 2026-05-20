package net.officefloor.tutorial.staticcontenthttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the REST end points.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticContentHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void indexPage() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/"));
		response.assertResponse(200, "<html><body>Hello World</body></html>");
	}
	// END SNIPPET: tutorial

}