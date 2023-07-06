package net.officefloor.server.google.function.test;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.google.function.wrap.TestHttpFunction;

/**
 * Tests the {@link GoogleHttpFunctionRule}.
 */
public class GoogleHttpFunctionRuleTest extends AbstractGoogleHttpFunctionTestCase {

	public final @Rule GoogleHttpFunctionRule httpFunction = new GoogleHttpFunctionRule(TestHttpFunction.class);

	@Test
	public void request() throws Exception {
		this.doTest(7878);
	}
	
}
