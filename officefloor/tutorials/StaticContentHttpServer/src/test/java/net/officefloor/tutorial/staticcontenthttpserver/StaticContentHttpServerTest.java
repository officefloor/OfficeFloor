package net.officefloor.tutorial.staticcontenthttpserver;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the REST end points.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticContentHttpServerTest {

	// START SNIPPET: tutorial
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void indexPage() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/"));
		response.assertResponse(200, "<html><body>Hello World</body></html>");
	}
	// END SNIPPET: tutorial

}