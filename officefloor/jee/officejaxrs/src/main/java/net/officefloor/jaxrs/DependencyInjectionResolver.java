package net.officefloor.jaxrs;

import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjectionResolver;

import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * {@link Dependency} {@link InjectionResolver}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyInjectionResolver implements InjectionResolver<Dependency> {

	/**
	 * {@link OfficeFloorDependencies}.
	 */
	private final OfficeFloorDependencies dependencies;

	/**
	 * Instantiate.
	 * 
	 * @param dependencies {@link OfficeFloorDependencies}.
	 */
	public DependencyInjectionResolver(OfficeFloorDependencies dependencies) {
		this.dependencies = dependencies;
	}

	/*
	 * ===================== InjectionResolver =====================
	 */

	@Override
	public Class<Dependency> getAnnotation() {
		return Dependency.class;
	}

	@Override
	public boolean isConstructorParameterIndicator() {
		return false;
	}

	@Override
	public boolean isMethodParameterIndicator() {
		return false;
	}

	@Override
	public Object resolve(Injectee injectee) {
		return this.dependencies.getDependency(injectee.getParent());
	}

}