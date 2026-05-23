package net.officefloor.tutorial.activityhttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the Activity HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void correctDepth() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest());
		response.assertJson(200, new Depth(2));
	}
	// END SNIPPET: tutorial
}
