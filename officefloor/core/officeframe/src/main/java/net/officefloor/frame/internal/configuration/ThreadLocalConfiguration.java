package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;

/**
 * Provides configuration for the {@link OptionalThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalConfiguration {

	/**
	 * Specifies the {@link ManagedObjectIndex} identifying the
	 * {@link ManagedObject} for the {@link OptionalThreadLocal}.
	 * 
	 * @param managedObjectIndex {@link ManagedObjectIndex} identifying the
	 *                           {@link ManagedObject} for the
	 *                           {@link OptionalThreadLocal}.
	 */
	void setManagedObjectIndex(ManagedObjectIndex managedObjectIndex);

}