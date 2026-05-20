package net.officefloor.tutorial.prototypehttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the Prototype HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class PrototypeHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	/**
	 * {@link MockWoofServer}.
	 */
	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	/**
	 * Ensure able to obtain end points.
	 */
	@Test
	public void ensurePrototypeEndPointsAvailable() {

		// Ensure able to obtain links
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/href"));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");

		// Ensure able to redirect to other template
		response = this.server.send(MockHttpServer.mockRequest("/href+link"));
		assertEquals(303, response.getStatus().getStatusCode(), "Should redirect");
		assertEquals("/form", response.getHeader("location").getValue(), "Should redirect to form");

		// Ensure able to obtain the form
		response = this.server.send(MockHttpServer.mockRequest("/form"));
		assertEquals(200, response.getStatus().getStatusCode(), "Should obtain form successfully");

		// Ensure able to submit the form
		response = this.server.send(MockHttpServer.mockRequest("/form+handleSubmit").method(HttpMethod.POST));
		assertEquals(303, response.getStatus().getStatusCode(), "Should redirect on submit");
		assertEquals("/href", response.getHeader("location").getValue(), "Should redirect to href");
	}

}