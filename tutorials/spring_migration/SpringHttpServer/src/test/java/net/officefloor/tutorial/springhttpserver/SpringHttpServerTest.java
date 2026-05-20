package net.officefloor.tutorial.springhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

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
	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void springHello() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/"));

		// Ensure request is successful
		assertEquals(200, response.getStatus().getStatusCode(), "Request should be successful");
		assertTrue(response.getEntity(null).contains("Hello OfficeFloor, from Spring"), "Incorrect response");
	}
	// END SNIPPET: tutorial

}