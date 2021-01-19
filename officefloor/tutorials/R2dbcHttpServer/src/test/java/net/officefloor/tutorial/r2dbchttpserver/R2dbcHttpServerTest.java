package net.officefloor.tutorial.r2dbchttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the R2DBC Http Server.
 * 
 * @author Daniel Sagenschneider
 */
public class R2dbcHttpServerTest {

	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void getData() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/message/1"));
		response.assertJson(200, new Message("TEST"));
	}
}