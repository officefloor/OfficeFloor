package net.officefloor.server.google.function.test;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Ensure can run {@link HttpFunction} on alternate port.
 */
public class AlternatePortTest extends AbstractGoogleHttpFunctionTestCase {

	private static final int ALTERNATE_PORT = 8787;

	public final @RegisterExtension @Order(0) GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension(
			TestHttpFunction.class).port(ALTERNATE_PORT);

	public final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure can request {@link TestHttpFunction}.
	 */
	@Test
	public void request() throws Exception {
		this.doTest(ALTERNATE_PORT);
	}

}
