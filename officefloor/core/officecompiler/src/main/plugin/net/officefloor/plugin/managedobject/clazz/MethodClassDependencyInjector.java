package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;

/**
 * {@link Field} {@link ClassDependencyInjector}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodClassDependencyInjector implements ClassDependencyInjector {

	/**
	 * {@link Method}.
	 */
	private final Method method;

	/**
	 * {@link ClassDependencyFactory} for the {@link Parameter} instances.
	 */
	private final ClassDependencyFactory[] parameterFactories;

	/**
	 * Instantiate.
	 * 
	 * @param method             {@link Method}.
	 * @param parameterFactories {@link ClassDependencyFactory} for the
	 *                           {@link Parameter} instances.
	 */
	public MethodClassDependencyInjector(Method method, ClassDependencyFactory[] parameterFactories) {
		this.method = method;
		this.parameterFactories = parameterFactories;
	}

	/*
	 * =================== ClassDependencyInjector =======================
	 */

	@Override
	public void injectDependencies(Object object, ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable {

		// Obtain the parameters
		Object[] parameters = new Object[this.parameterFactories.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.parameterFactories[i].createDependency(managedObject, context, registry);
		}

		// Load the dependencies
		try {
			this.method.invoke(object, parameters);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

}