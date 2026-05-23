package net.officefloor.tutorial.rawhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the web application is returning correctly.
 * 
 * @author Daniel Sagenschneider
 */
public class RawHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	/**
	 * Ensure able to obtain the Raw HTML.
	 */
	@Test
	public void testRawHtml() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));
		assertEquals(200, response.getStatus().getStatusCode(), "Request should be successful");

		// Ensure raw html rendered to page
		String responseText = response.getEntity(null);
		assertTrue(responseText.contains("Web on OfficeFloor (WoOF)"), "Should have raw HTML rendered");
	}

}