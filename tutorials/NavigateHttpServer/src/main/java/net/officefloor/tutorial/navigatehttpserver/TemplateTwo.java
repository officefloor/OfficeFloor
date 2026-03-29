package net.officefloor.tutorial.navigatehttpserver;

import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Logic for <code>TemplateTwo.ofp</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateTwo {

	@FlowInterface
	public interface Flows {
		void next();
	}

	public void process(Flows flows) {
		flows.next();
	}

}
// END SNIPPET: example