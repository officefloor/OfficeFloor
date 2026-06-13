package net.officefloor.tutorial.environmenthttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

public class ProfileTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension().profile("mock");

	@Test
	public void applicationProperties() {
		this.server.send(MockWoofServer.mockRequest("/")).assertResponse(200, "MOCK");
	}
	// END SNIPPET: tutorial
}