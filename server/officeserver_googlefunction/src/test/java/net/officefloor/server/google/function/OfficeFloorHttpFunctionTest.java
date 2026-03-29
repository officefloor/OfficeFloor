package net.officefloor.server.google.function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.mock.MockGoogleHttpFunctionExtension;

/**
 * Tests the {@link OfficeFloorHttpFunction}.
 */
public class OfficeFloorHttpFunctionTest {

	public static final @RegisterExtension MockGoogleHttpFunctionExtension httpFunction = SimpleRequestTestHelper
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
	 * Ensure can request.
	 */
	@Test
	public void simpleRequest() {
		SimpleRequestTestHelper.assertMockRequest(httpFunction.getMockHttpServer());
	}

}
