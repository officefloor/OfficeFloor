package net.officefloor.frame.impl.execute.function;

import java.util.logging.Logger;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;

/**
 * Tests the {@link Logger} available from the {@link ManagedFunctionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionLoggerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure appropriate {@link Logger}.
	 */
	public void testLogger() throws Exception {

		// Capture office name
		final String officeName = this.getOfficeName();

		// Create the managed function
		final String FUNCTION_NAME = "FUNCTION_LOGGER";
		Closure<Logger> logger = new Closure<>();
		this.constructFunction(FUNCTION_NAME, () -> (context) -> {
			logger.value = context.getLogger();
		});

		// Ensure provide appropriate logger
		try (OfficeFloor officeFloor = this.constructOfficeFloor()) {
			officeFloor.openOfficeFloor();

			// Invoke function and confirm correct logger
			officeFloor.getOffice(officeName).getFunctionManager(FUNCTION_NAME).invokeProcess(null, null);

			// Ensure correct logger
			assertNotNull("Should have logger", logger.value);
			assertEquals("Incorrect logger", FUNCTION_NAME, logger.value.getName());
		}
	}

}