package net.officefloor.gef.woof.test;

import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Example template logic.
 * 
 * @author Daniel Sagenschneider
 */
public class Template {

	@FlowInterface
	public static interface Flows {

		void flow();
	}
	
	public Template redirect() {
		return this;
	}

	public void getTemplate(Flows flows) {
	}
}