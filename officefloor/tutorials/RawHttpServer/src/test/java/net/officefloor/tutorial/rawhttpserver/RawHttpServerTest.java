package net.officefloor.tutorial.rawhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

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

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	/**
	 * Ensure able to obtain the Raw HTML.
	 */
	@Test
	public void testRawHtml() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));
		assertEquals("Request should be successful", 200, response.getStatus().getStatusCode());

		// Ensure raw html rendered to page
		String responseText = response.getEntity(null);
		assertTrue("Should have raw HTML rendered", responseText.contains("Web on OfficeFloor (WoOF)"));
	}

}