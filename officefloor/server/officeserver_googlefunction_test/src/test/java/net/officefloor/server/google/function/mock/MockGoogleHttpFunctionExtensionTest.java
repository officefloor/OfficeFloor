package net.officefloor.server.google.function.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.server.http.mock.MockHttpResponse;

/**
 * Tests the {@link MockGoogleHttpFunctionExtension}.
 */
public class MockGoogleHttpFunctionExtensionTest {

	public final @RegisterExtension MockGoogleHttpFunctionExtension httpFunction = new MockGoogleHttpFunctionExtension(
			TestHttpFunction.class);

	/**
	 * Ensure can request {@link TestHttpFunction}.
	 */
	@Test
	public void request() {
		MockHttpResponse response = httpFunction.send(MockGoogleHttpFunctionExtension.mockRequest());
		assertEquals("TEST", response.getEntity(null), "Incorrect response");
	}
}