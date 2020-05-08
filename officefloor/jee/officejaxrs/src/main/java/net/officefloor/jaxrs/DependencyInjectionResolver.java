package net.officefloor.jaxrs;

import java.lang.reflect.Type;

import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjectionResolver;

import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;

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

		// Obtain the OfficeFloor qualifier
		String dependencyName = injectee.getParent().toString();
		String qualifier;
		try {
			qualifier = DependencyMetaData.getTypeQualifier(dependencyName, injectee.getRequiredQualifiers());
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}

		// Obtain the required class
		Type requiredType = injectee.getRequiredType();
		if (!(requiredType instanceof Class)) {
			throw new IllegalStateException("Can only map " + Class.class.getSimpleName() + " typed dependencies");
		}
		Class<?> requiredClass = (Class<?>) requiredType;

		// Provide the dependency
//		ServletManager servletManager = ServletSupplierSource.getServletManager();
//		return servletManager.getDependency(qualifier, requiredClass);
		
		return null;
	}
}