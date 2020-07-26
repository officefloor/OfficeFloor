package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Field;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
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

		// Ensure the field is accessible
		this.field.setAccessible(true);
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

	@Override
	public void injectDependencies(Object object, ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Obtain the dependency
		Object dependency = this.factory.createDependency(context);

		// Load the dependency
		this.field.set(object, dependency);
	}

	@Override
	public void injectDependencies(Object object, AdministrationContext<Object, Indexed, Indexed> context)
			throws Throwable {

		// Obtain the dependency
		Object dependency = this.factory.createDependency(context);

		// Load the dependency
		this.field.set(object, dependency);
	}

}