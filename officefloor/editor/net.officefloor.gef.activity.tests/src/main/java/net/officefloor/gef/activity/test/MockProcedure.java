package net.officefloor.gef.activity.test;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Mock {@link Procedure} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockProcedure {

	@FlowInterface
	public static interface Flows {
		void flow();
	}

	public void procedure(Flows flows) {
		// Test method
	}

}