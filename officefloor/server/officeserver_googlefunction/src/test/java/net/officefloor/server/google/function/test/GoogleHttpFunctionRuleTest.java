package net.officefloor.server.google.function.test;

import org.junit.ClassRule;
import org.junit.Test;

import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class GoogleHttpFunctionRuleTest {

	public static final @ClassRule GoogleHttpFunctionRule httpFunction = new GoogleHttpFunctionRule();

	/**
	 * Ensure servicing with {@link OfficeFloorHttpFunction}.
	 */
	@Test
	public void request() {
		SimpleRequestTestHelper.assertRequest();
	}
}
