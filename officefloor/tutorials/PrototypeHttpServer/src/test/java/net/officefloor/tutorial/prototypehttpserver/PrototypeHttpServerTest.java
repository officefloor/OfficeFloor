package net.officefloor.tutorial.prototypehttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

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
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	/**
	 * Ensure able to obtain end points.
	 */
	@Test
	public void ensurePrototypeEndPointsAvailable() {

		// Ensure able to obtain links
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/href"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure able to redirect to other template
		response = this.server.send(MockHttpServer.mockRequest("/href+link"));
		assertEquals("Should redirect", 303, response.getStatus().getStatusCode());
		assertEquals("Should redirect to form", "/form", response.getHeader("location").getValue());

		// Ensure able to obtain the form
		response = this.server.send(MockHttpServer.mockRequest("/form"));
		assertEquals("Should obtain form successfully", 200, response.getStatus().getStatusCode());

		// Ensure able to submit the form
		response = this.server.send(MockHttpServer.mockRequest("/form+handleSubmit").method(HttpMethod.POST));
		assertEquals("Should redirect on submit", 303, response.getStatus().getStatusCode());
		assertEquals("Should redirect to href", "/href", response.getHeader("location").getValue());
	}

}