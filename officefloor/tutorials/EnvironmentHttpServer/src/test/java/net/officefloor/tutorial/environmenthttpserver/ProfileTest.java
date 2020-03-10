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
public class ProfileTest {

	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule().profile("mock");

	@Test
	public void applicationProperties() {
		this.server.send(MockWoofServer.mockRequest("/")).assertResponse(200, "MOCK");
	}
}