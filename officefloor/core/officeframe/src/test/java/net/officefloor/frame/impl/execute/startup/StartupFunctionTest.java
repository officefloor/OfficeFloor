package net.officefloor.frame.impl.execute.startup;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure startup {@link ManagedFunction} instances are invoked.
 *
 * @author Daniel Sagenschneider
 */
public class StartupFunctionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure invoke startup function.
	 */
	public void testStartupFunction() throws Exception {

		// Construct the function
		TestWork work = new TestWork();
		this.constructFunction(work, "startup");

		// Construct startup
		this.getOfficeBuilder().addStartupFunction("startup");

		// Open the office
		this.constructOfficeFloor().openOfficeFloor();

		// Ensure the startup function is invoked
		assertTrue("Should have invoked startup function", work.isStartupInvoked);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public boolean isStartupInvoked = false;

		public void startup() {
			this.isStartupInvoked = true;
		}
	}

}
