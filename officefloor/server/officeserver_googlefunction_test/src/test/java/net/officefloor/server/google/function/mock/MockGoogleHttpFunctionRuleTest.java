package net.officefloor.server.google.function.mock;

import static org.junit.Assert.assertEquals;

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
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}
}
