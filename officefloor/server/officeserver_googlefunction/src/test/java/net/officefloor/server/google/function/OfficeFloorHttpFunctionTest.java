package net.officefloor.server.google.function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.mock.MockGoogleHttpFunctionExtension;

/**
 * Tests the {@link OfficeFloorHttpFunction}.
 */
public class OfficeFloorHttpFunctionTest {

	public static final @RegisterExtension MockGoogleHttpFunctionExtension httpFunction = new MockGoogleHttpFunctionExtension(
			OfficeFloorHttpFunction.class);

	/**
	 * Ensure can request.
	 */
	@Test
	public void simpleRequest() {
		SimpleRequestTestHelper.assertMockRequest(httpFunction);
	}

}
