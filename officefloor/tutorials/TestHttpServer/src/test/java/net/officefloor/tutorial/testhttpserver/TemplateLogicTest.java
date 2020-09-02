package net.officefloor.tutorial.testhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.testhttpserver.TemplateLogic.Parameters;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogicTest {

	/**
	 * Main to run for manual testing.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: unit
	@Test
	public void unitTest() {

		// Load the parameters
		Parameters parameters = new Parameters();
		parameters.setA("1");
		parameters.setB("2");
		assertNull(parameters.getResult(), "Shoud not have result");

		// Test
		TemplateLogic logic = new TemplateLogic();
		logic.add(parameters, new Calculator());
		assertEquals("3", parameters.getResult(), "Incorrect result");
	}

	// END SNIPPET: unit

	// START SNIPPET: system
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void systemTest() throws Exception {

		// Send request to add
		MockHttpResponse response = this.server
				.sendFollowRedirect(MockHttpServer.mockRequest("/template+add?a=1&b=2").method(HttpMethod.POST));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");

		// Ensure added the values
		String entity = response.getEntity(null);
		assertTrue(entity.contains("= 3"), "Should have added the values");
	}
	// END SNIPPET: system

	// START SNIPPET: inject-dependency
	@Test
	public void injectDependency(Calculator calculator) {
		int result = calculator.plus(1, 2);
		assertEquals(3, result, "Should calculate correct result");
	}
	// END SNIPPET: inject-dependency
}