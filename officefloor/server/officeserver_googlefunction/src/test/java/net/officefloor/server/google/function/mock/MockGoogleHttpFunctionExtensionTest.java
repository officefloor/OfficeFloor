package net.officefloor.server.google.function.mock;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class MockGoogleHttpFunctionExtensionTest {

	private static final @RegisterExtension @Order(0) MockGoogleHttpFunctionExtension httpFunction = new MockGoogleHttpFunctionExtension();

	private static final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure servicing with {@link OfficeFloorHttpFunction}.
	 */
	@Test
	public void request() {
		SimpleRequestTestHelper.assertMockRequest(httpFunction.getMockHttpServer());
	}
}
