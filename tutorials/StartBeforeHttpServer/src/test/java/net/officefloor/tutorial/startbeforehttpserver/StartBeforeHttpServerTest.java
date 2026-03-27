package net.officefloor.tutorial.startbeforehttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the Start Before HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class StartBeforeHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void ensureSetup() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/message"));
		response.assertJson(200, new Message(1L, "SETUP"));
	}
	// END SNIPPET: tutorial

}