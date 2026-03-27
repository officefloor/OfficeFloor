package net.officefloor.server.google.function.mock;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the {@link MockGoogleHttpFunctionRule}.
 */
public class MockGoogleHttpFunctionRuleTest {

	public final @Rule(order = 0) MockGoogleHttpFunctionRule httpFunction = new MockGoogleHttpFunctionRule(
			TestHttpFunction.class);

	public final @Rule(order = 1) OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Test
	public void request() {
		MockHttpResponse response = httpFunction.send(MockHttpServer.mockRequest());
		response.assertResponse(200, "TEST");
	}

	@Test
	public void requestSecure() {
		String url = httpFunction.getMockHttpServer().createClientUrl(true, "/");
		MockHttpResponse response = httpFunction.send(MockHttpServer.mockRequest(url).secure(true));
		response.assertResponse(200, "TEST-secure");
	}

}
