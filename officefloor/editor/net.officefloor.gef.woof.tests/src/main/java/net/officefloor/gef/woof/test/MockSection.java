package net.officefloor.gef.woof.test;

import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Mock section {@link Class} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSection {

	@FlowInterface
	public static interface Flows {
		void flow();
	}

	public void input(Flows flows) {
	}

}