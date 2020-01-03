package net.officefloor.gef.section.test;

import net.officefloor.gef.section.SectionEditor;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Mock section {@link Class} for testing the {@link SectionEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSection {

	@FlowInterface
	public static interface Flows {

		void outputOne();

		void outputTwo(String value);
	}

	public void inputOne() {
	}

	public void inputTwo(MockObject object) {
		object.setValue("inputTwo");
	}

	public void inputThree(Flows flows) {
		flows.outputTwo("inputThree");
	}
}