package net.officefloor.tutorial.sectionhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

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

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void testPageRendering() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));

		// Ensure request is successful
		assertEquals("Request should be successful", 200, response.getStatus().getStatusCode());

		// Ensure correct response
		String responseText = response.getEntity(null);
		assertTrue("Missing template section", responseText.contains("<p>Hi</p>"));
		assertTrue("Missing Hello section", responseText.contains("<p>Hello</p>"));
		assertFalse("NotRender section should not be rendered", responseText.contains("<p>Not rendered</p>"));
		assertTrue("Missing NoBean section", responseText.contains("<p>How are you?</p>"));
	}

}