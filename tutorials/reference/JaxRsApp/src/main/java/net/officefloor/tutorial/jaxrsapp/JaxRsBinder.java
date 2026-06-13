package net.officefloor.tutorial.jaxrsapp;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

// START SNIPPET: tutorial
public class JaxRsBinder extends AbstractBinder {

	@Override
	protected void configure() {
		this.bind(JaxRsDependency.class).to(JaxRsDependency.class);
	}
}
// END SNIPPET: tutorial