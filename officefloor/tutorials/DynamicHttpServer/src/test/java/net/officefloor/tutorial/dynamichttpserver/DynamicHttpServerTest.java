package net.officefloor.tutorial.dynamichttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

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

	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	// START SNIPPET: pojo
	@Test
	public void templateLogic() {

		TemplateLogic logic = new TemplateLogic();

		assertEquals(System.getProperties().size(), logic.getTemplateData().getProperties().length,
				"Number of properties");

	}
	// END SNIPPET: pojo

	@Test
	public void dynamicPage() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));

		// Ensure request is successful
		assertEquals(200, response.getStatus().getStatusCode(), "Request should be successful");
	}

}