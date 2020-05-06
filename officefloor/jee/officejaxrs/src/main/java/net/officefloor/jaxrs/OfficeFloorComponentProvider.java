package net.officefloor.jaxrs;

import java.util.Set;

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

		// Register OfficeFloor dependencies
		this.injectionManager.register(Bindings.injectionResolver(new DependencyInjectionResolver()));
	}

	@Override
	public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {

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