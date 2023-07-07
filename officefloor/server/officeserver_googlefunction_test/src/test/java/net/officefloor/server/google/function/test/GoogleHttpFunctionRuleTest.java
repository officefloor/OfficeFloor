package net.officefloor.server.google.function.test;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the {@link GoogleHttpFunctionRule}.
 */
public class GoogleHttpFunctionRuleTest extends AbstractGoogleHttpFunctionTestCase {

	public final @Rule(order = 0) GoogleHttpFunctionRule httpFunction = new GoogleHttpFunctionRule(
			TestHttpFunction.class);

	public final @Rule(order = 1) OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Test
	public void request() throws Exception {
		this.doTest(7878);
	}

}
