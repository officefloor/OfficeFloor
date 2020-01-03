package net.officefloor.activity.source;

import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Example {@link ClassSectionSource} section.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleSection {

	@FlowInterface
	public static interface Flows {
		void output(String argument);
	}

	public void input(@Parameter String parameter, Flows flows) {
		flows.output(parameter);
	}

}