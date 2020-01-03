package net.officefloor.tutorial.activityhttpserver;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Activity HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityHttpServerTest {

	// START SNIPPET: tutorial
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void correctDepth() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest());
		response.assertResponse(200, mapper.writeValueAsString(new Depth(2)));
	}
	// END SNIPPET: tutorial
}