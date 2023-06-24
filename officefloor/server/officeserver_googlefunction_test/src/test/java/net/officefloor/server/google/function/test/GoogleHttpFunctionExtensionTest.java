package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.mock.MockHttpResponse;

/**
 * Tests the {@link GoogleHttpFunctionExtension}.
 */
public class GoogleHttpFunctionExtensionTest {

	public final @RegisterExtension GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension(
			TestHttpFunction.class);

	/**
	 * Ensure can request {@link TestHttpFunction}.
	 */
	@Test
	public void request() {
		MockHttpResponse response = httpFunction.send(GoogleHttpFunctionExtension.mockRequest());
		assertEquals("TEST", response.getEntity(null), "Incorrect response");
	}
}