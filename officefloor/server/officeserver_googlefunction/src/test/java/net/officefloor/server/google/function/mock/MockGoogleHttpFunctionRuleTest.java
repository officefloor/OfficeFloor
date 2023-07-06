package net.officefloor.server.google.function.mock;

import org.junit.ClassRule;
import org.junit.Test;

import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class MockGoogleHttpFunctionRuleTest {

	public static final @ClassRule MockGoogleHttpFunctionRule httpFunction = new MockGoogleHttpFunctionRule();

	/**
	 * Ensure servicing with {@link OfficeFloorHttpFunction}.
	 */
	@Test
	public void request() {
		SimpleRequestTestHelper.assertMockRequest(httpFunction);
	}
}
