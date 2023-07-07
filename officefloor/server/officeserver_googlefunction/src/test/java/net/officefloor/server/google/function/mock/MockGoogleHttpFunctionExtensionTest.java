package net.officefloor.server.google.function.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class MockGoogleHttpFunctionExtensionTest {

	private static final @RegisterExtension MockGoogleHttpFunctionExtension httpFunction = SimpleRequestTestHelper
			.loadApplication(new MockGoogleHttpFunctionExtension());

	@BeforeEach
	public void openOfficeFloor() throws Exception {
		OfficeFloorHttpFunction.open();
	}

	@AfterEach
	public void closeOfficeFloor() throws Exception {
		OfficeFloorHttpFunction.close();
	}

	/**
	 * Ensure servicing with {@link OfficeFloorHttpFunction}.
	 */
	@Test
	public void request() {
		SimpleRequestTestHelper.assertMockRequest(httpFunction.getMockHttpServer());
	}
}
