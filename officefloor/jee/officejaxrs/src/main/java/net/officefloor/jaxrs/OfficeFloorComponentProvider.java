package net.officefloor.jaxrs;

import java.util.Set;

import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ComponentProvider;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link ComponentProvider}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorComponentProvider implements ComponentProvider {

	/**
	 * {@link InjectionManager}.
	 */
	private InjectionManager injectionManager;

	/*
	 * ================== ComponentProvider ===================
	 */

	@Override
	public void initialize(InjectionManager injectionManager) {
		this.injectionManager = injectionManager;
	}

	@Override
	public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {
		try {
			Class<?> jaxRsResourceClass = Class.forName("net.officefloor.tutorial.jaxrsapp.JaxRsResource");
			Class<?> jaxRsDependencyClass = Class.forName("net.officefloor.tutorial.jaxrsapp.JaxRsDependency");
			Class<?> jaxRsOverrideClass = Class.forName("net.officefloor.jaxrs.JaxRsTest$OverrideJaxRsResource");
			if (jaxRsResourceClass.getName().equals(component.getName())) {
				System.out.println("JAXRS RESOURCE");
				Binding<?, ?> dependency = Bindings.service(jaxRsOverrideClass).to(jaxRsDependencyClass);
				dependency.ranked(0);
				this.injectionManager.register(dependency);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		// TODO determine if handle
		System.out.println("bind " + component.getName() + " (" + providerContracts + ")");
		return false;
	}

	@Override
	public void done() {
		// Nothing to complete

		// TODO REMOVE
		System.out.println("done");
	}

}