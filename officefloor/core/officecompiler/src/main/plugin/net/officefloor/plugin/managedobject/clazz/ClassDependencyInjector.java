package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;

/**
 * Injector of dependencies into an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependencyInjector {

	/**
	 * Injects dependencies into the object.
	 * 
	 * @param object        Object to receive the dependencies.
	 * @param managedObject {@link ManagedObject}.
	 * @param context       {@link ManagedObjectContext}.
	 * @param registry      {@link ObjectRegistry}.
	 * @throws Throwable If fails to inject the dependencies.
	 */
	void injectDependencies(Object object, ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable;

	/**
	 * Injects dependencies into the object (typically to invoke {@link Method}
	 * against).
	 * 
	 * @param object  Object to receive the dependencies.
	 * @param context {@link ManagedFunctionContext}.
	 * @throws Throwable If fails to inject the dependencies.
	 */
	void injectDependencies(Object object, ManagedFunctionContext<Indexed, Indexed> context) throws Throwable;

	/**
	 * Injects dependencies into the object (typically to invoke {@link Method}
	 * against).
	 * 
	 * @param object  Object to receive the dependencies.
	 * @param context {@link AdministrationContext}.
	 * @throws Throwable If fails to inject the dependencies.
	 */
	void injectDependencies(Object object, AdministrationContext<Object, Indexed, Indexed> context) throws Throwable;
}