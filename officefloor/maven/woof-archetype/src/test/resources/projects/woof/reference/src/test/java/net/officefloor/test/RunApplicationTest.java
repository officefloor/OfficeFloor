package net.officefloor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Unit/System tests the application.
 */
public class RunApplicationTest {

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void ensureApplicationAvailable() throws Exception {

		// Ensure can obtain static page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/static"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertTrue("Incorrect page", response.getEntity(null).contains("<title>Static Page</title>"));
	}

}