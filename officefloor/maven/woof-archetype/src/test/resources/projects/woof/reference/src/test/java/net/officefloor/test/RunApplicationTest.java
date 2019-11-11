package net.officefloor.test;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

public class RunApplicationTest {

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void ensureApplicationAvailable() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/hi/UnitTest"));
		response.assertResponse(200, "{\"message\":\"Hello UnitTest\"}", "content-type", "application/json");
	}
}