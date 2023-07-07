package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;
import net.officefloor.server.google.function.officefloor.OfficeFloorHttpFunctionReference;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class GoogleHttpFunctionExtensionTest {

	private static final @RegisterExtension @Order(0) GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension();

	private static final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure using correct {@link Class} name.
	 */
	@Test
	public void ensureCorrectClassName() {
		assertEquals(OfficeFloorHttpFunction.class.getName(),
				OfficeFloorHttpFunctionReference.OFFICEFLOOR_HTTP_FUNCTION_CLASS_NAME,
				"Incorrect " + OfficeFloorHttpFunction.class.getSimpleName() + " class name");
	}

	/**
	 * Ensure servicing with {@link OfficeFloorHttpFunction}.
	 */
	@Test
	public void request() {
		SimpleRequestTestHelper.assertRequest();
	}
}
