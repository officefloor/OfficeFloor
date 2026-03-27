package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;
import net.officefloor.server.google.function.officefloor.OfficeFloorHttpFunctionReference;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class GoogleHttpFunctionExtensionTest {

	private static final @RegisterExtension GoogleHttpFunctionExtension httpFunction = SimpleRequestTestHelper
			.loadApplication(new GoogleHttpFunctionExtension());

	@BeforeEach
	public void openOfficeFloor() throws Exception {
		OfficeFloorHttpFunction.open();
	}

	@AfterEach
	public void closeOfficeFloor() throws Exception {
		OfficeFloorHttpFunction.close();
	}

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

	/**
	 * Ensure servicing secure with {@link OfficeFloorHttpFunction}.
	 */
	@Test
	public void requestSecure() {
		SimpleRequestTestHelper.assertSecureRequest();
	}

}
