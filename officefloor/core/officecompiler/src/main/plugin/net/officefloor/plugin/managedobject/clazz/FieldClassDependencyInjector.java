package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Field;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;

/**
 * {@link Field} {@link ClassDependencyInjector}.
 * 
 * @author Daniel Sagenschneider
 */
public class FieldClassDependencyInjector implements ClassDependencyInjector {

	/**
	 * {@link Field}.
	 */
	private final Field field;

	/**
	 * {@link ClassDependencyFactory}.
	 */
	private final ClassDependencyFactory factory;

	/**
	 * Instantiate.
	 * 
	 * @param field   {@link Field}.
	 * @param factory {@link ClassDependencyFactory}.
	 */
	public FieldClassDependencyInjector(Field field, ClassDependencyFactory factory) {
		this.field = field;
		this.factory = factory;
	}

	/*
	 * =================== ClassDependencyInjector =======================
	 */

	@Override
	public void injectDependencies(Object object, ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable {

		// Obtain the dependency
		Object dependency = this.factory.createDependency(managedObject, context, registry);

		// Load the dependency
		this.field.set(object, dependency);
	}

}