package net.officefloor.server.google.function.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class MockGoogleHttpFunctionRuleTest {

	public static final @ClassRule MockGoogleHttpFunctionRule httpFunction = SimpleRequestTestHelper
			.loadApplication(new MockGoogleHttpFunctionRule());

	@Before
	public void openOfficeFloor() throws Exception {
		OfficeFloorHttpFunction.open();
	}

	@After
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
