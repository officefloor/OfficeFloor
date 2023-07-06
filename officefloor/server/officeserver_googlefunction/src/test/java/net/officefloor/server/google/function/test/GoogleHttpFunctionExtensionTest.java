package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;
import net.officefloor.server.google.function.officefloor.OfficeFloorHttpFunctionReference;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class GoogleHttpFunctionExtensionTest {

	public static final @RegisterExtension GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension();

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
