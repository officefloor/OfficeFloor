package net.officefloor.plugin.managedobject.clazz.injection;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyFactory;

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

}