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
		
		// TODO provide resolution
		System.out.println("resolve " + injectee.getRequiredType().getTypeName());
		try {
			return ((Class) injectee.getRequiredType()).getConstructor().newInstance();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}