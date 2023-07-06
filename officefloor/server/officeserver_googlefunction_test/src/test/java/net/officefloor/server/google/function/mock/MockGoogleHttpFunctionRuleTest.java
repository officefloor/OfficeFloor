package net.officefloor.server.google.function.mock;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.server.http.mock.MockHttpResponse;

/**
 * Tests the {@link MockGoogleHttpFunctionRule}.
 */
public class MockGoogleHttpFunctionRuleTest {

	public final @Rule MockGoogleHttpFunctionRule httpFunction = new MockGoogleHttpFunctionRule(TestHttpFunction.class);

	@Test
	public void request() {
		MockHttpResponse response = httpFunction.send(MockGoogleHttpFunctionExtension.mockRequest());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}
}
