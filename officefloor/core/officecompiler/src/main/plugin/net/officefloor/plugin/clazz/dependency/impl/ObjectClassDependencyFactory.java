package net.officefloor.plugin.clazz.dependency.impl;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;

/**
 * {@link ClassDependencyFactory} for dependency object.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectClassDependencyFactory implements ClassDependencyFactory {

	/**
	 * Dependency index.
	 */
	private final int dependencyIndex;

	/**
	 * Instantiate.
	 * 
	 * @param dependencyIndex Dependency index.
	 */
	public ObjectClassDependencyFactory(int dependencyIndex) {
		this.dependencyIndex = dependencyIndex;
	}

	/*
	 * ================== ClassDependencyFactory =======================
	 */

	@Override
	public Object createDependency(ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Exception {
		return registry.getObject(this.dependencyIndex);
	}

	@Override
	public Object createDependency(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {
		return context.getObject(this.dependencyIndex);
	}

	@Override
	public Object createDependency(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {
		throw new IllegalStateException("Object dependency not available for " + Administration.class.getSimpleName());
	}

}