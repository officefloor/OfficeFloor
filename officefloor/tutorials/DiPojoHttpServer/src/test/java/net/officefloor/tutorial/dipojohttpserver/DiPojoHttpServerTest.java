package net.officefloor.tutorial.dipojohttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Ensure correctly renders the page.
 * 
 * @author Daniel Sagenschneider
 */
public class DiPojoHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: test
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void ensureRenderPage() throws Exception {

		// Obtain the page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/template"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure page contains correct rendered content
		String page = response.getEntity(null);
		assertTrue("Ensure correct page content", page.contains("Hello World"));
	}
	// END SNIPPET: test

}