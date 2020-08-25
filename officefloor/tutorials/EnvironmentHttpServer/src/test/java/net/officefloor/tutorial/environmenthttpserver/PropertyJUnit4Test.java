package net.officefloor.tutorial.environmenthttpserver;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests with default properties.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyJUnit4Test {

	// START SNIPPET: tutorial
	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule(this).property("service.procedure.name",
			"PROPERTY");

	@Test
	public void applicationProperties() {
		this.server.send(MockWoofServer.mockRequest("/")).assertResponse(200, "PROPERTY");
	}
	// END SNIPPET: tutorial
}