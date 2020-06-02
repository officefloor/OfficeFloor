package net.officefloor.plugin.managedobject.clazz;

import net.officefloor.frame.api.build.Indexed;
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

}