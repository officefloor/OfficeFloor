package net.officefloor.tutorial.jaxrsapp;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;

/**
 * JAX-RS {@link Binder}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class JaxRsBinder extends AbstractBinder {

	@Override
	protected void configure() {
		this.bind(JaxRsDependency.class).to(JaxRsDependency.class);
	}
}
// END SNIPPET: tutorial