package net.officefloor.server.google.function.test;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests the {@link GoogleHttpFunctionExtension}.
 */
public class GoogleHttpFunctionExtensionTest extends AbstractGoogleHttpFunctionTestCase {

	public final @RegisterExtension @Order(0) GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension(
			TestHttpFunction.class);

	private final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure can request {@link TestHttpFunction}.
	 */
	@Test
	public void request() throws Exception {
		this.doTest(7878);
	}

}