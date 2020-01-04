package net.officefloor.tutorial.dynamichttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link DynamicHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamicHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	// START SNIPPET: pojo
	@Test
	public void templateLogic() {

		TemplateLogic logic = new TemplateLogic();

		assertEquals("Number of properties", System.getProperties().size(),
				logic.getTemplateData().getProperties().length);

	}
	// END SNIPPET: pojo

	@Test
	public void dynamicPage() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));

		// Ensure request is successful
		assertEquals("Request should be successful", 200, response.getStatus().getStatusCode());
	}

}