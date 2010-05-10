package net.officefloor.example.weborchestration.route;

import net.officefloor.example.weborchestration.TestResetLocal;
import net.officefloor.example.weborchestration.WebUtil;

/**
 * Resets for next test.
 * 
 * @author daniel
 */
public class TestReset {

	/**
	 * Resets for the next Test.
	 * 
	 * @throws Exception
	 *             If fails to reset for next Test.
	 */
	public void resetForTest() throws Exception {

		// Reset the for testing
		TestResetLocal setup = WebUtil.lookupService(TestResetLocal.class);
		setup.reset();

		// Create customer
		setup.setupCustomer();

		// Create the products
		setup.setupProducts();
	}

}