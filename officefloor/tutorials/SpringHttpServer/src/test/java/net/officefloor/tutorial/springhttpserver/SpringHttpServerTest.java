package net.officefloor.tutorial.springhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Spring HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: tutorial
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void springHello() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/"));

		// Ensure request is successful
		assertEquals("Request should be successful", 200, response.getStatus().getStatusCode());
		assertTrue("Incorrect response", response.getEntity(null).contains("Hello OfficeFloor, from Spring"));
	}
	// END SNIPPET: tutorial

}