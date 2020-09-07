package net.officefloor.tutorial.sectionhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void testPageRendering() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));

		// Ensure request is successful
		assertEquals(200, response.getStatus().getStatusCode(), "Request should be successful");

		// Ensure correct response
		String responseText = response.getEntity(null);
		assertTrue(responseText.contains("<p>Hi</p>"), "Missing template section");
		assertTrue(responseText.contains("<p>Hello</p>"), "Missing Hello section");
		assertFalse(responseText.contains("<p>Not rendered</p>"), "NotRender section should not be rendered");
		assertTrue(responseText.contains("<p>How are you?</p>"), "Missing NoBean section");
	}

}