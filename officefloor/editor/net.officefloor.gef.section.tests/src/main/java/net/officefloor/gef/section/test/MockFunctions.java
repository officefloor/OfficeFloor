package net.officefloor.gef.section.test;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.gef.section.SectionEditor;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Functions for testing {@link SectionEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockFunctions {

	@FlowInterface
	public static interface Flows {

		void outputOne();

		void outputTwo(String value);
	}

	public void functionOne() {
	}

	public void functionTwo(MockObject object, String parameter) {
		object.setValue("functionTwo");
	}

	public String functionThree(Flows flows) {
		flows.outputTwo("functionThree");
		return "functionThree";
	}

	public void functionFour() throws IOException, SQLException, RuntimeException {
	}
}