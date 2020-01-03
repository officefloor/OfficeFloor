package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Configuration of the {@link ManagedObjectSource} to be bound for the input
 * {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface BoundInputManagedObjectConfiguration {

	/**
	 * Obtains the name of the input {@link ManagedObject}.
	 *
	 * @return Name of the input {@link ManagedObject}.
	 */
	String getInputManagedObjectName();

	/**
	 * Obtains the name of the {@link ManagedObjectSource} to be bound for the
	 * input {@link ManagedObject}.
	 *
	 * @return Name of the {@link ManagedObjectSource} to be bound for the input
	 *         {@link ManagedObject}.
	 */
	String getBoundManagedObjectSourceName();

}