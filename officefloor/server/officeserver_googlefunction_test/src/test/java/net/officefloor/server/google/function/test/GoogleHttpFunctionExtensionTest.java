package net.officefloor.server.google.function.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.wrap.TestHttpFunction;

/**
 * Tests the {@link GoogleHttpFunctionExtension}.
 */
public class GoogleHttpFunctionExtensionTest extends AbstractGoogleHttpFunctionTestCase {

	public final @RegisterExtension GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension(
			TestHttpFunction.class);

	/**
	 * Ensure can request {@link TestHttpFunction}.
	 */
	@Test
	public void request() throws Exception {
		this.doTest(7878);
	}

}