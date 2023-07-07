package net.officefloor.server.google.function.mock;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests the {@link MockGoogleHttpFunctionExtension}.
 */
public class MockGoogleHttpFunctionExtensionTest {

	public final @RegisterExtension @Order(0) MockGoogleHttpFunctionExtension httpFunction = new MockGoogleHttpFunctionExtension(
			TestHttpFunction.class);

	public final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure can request {@link TestHttpFunction}.
	 */
	@Test
	public void request() {
		MockHttpResponse response = httpFunction.send(MockHttpServer.mockRequest());
		response.assertResponse(200, "TEST");
	}
}