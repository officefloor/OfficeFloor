package net.officefloor.tutorial.constantcachehttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.cache.Cache;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the constant {@link Cache} HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class ConstantCacheHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void helloWorld() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/cached/1"));
		response.assertJson(200, new Message("Hello World"));
	}
	
	@Test
	public void hiThere() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/cached/2"));
		response.assertJson(200, new Message("Hi there"));
	}	
	// END SNIPPET: tutorial

}