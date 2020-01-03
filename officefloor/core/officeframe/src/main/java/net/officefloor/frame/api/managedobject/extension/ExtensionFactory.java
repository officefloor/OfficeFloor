package net.officefloor.frame.api.managedobject.extension;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Creates a specific extension for the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface ExtensionFactory<E> {

	/**
	 * Creates the specific extension for the {@link ManagedObject}.
	 *
	 * @param managedObject
	 *            {@link ManagedObject} that is have the extension created for
	 *            it.
	 * @return Extension for the {@link ManagedObject}.
	 * @throws Throwable
	 *             If fails to create extension.
	 */
	E createExtension(ManagedObject managedObject) throws Throwable;

}