package net.officefloor.server.google.function.test;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.mock.MockHttpResponse;

/**
 * Tests the {@link GoogleHttpFunctionRule}.
 */
public class GoogleHttpFunctionRuleTest {

	public final @Rule GoogleHttpFunctionRule httpFunction = new GoogleHttpFunctionRule(TestHttpFunction.class);

	@Test
	public void request() {
		MockHttpResponse response = httpFunction.send(GoogleHttpFunctionExtension.mockRequest());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}
}
