package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Extracts the extension interface from the {@link ManagedObject} within the
 * {@link ManagedObjectContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionExtractor<E> {

	/**
	 * Extracts the extension from the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to extract the extension interface from.
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} of the {@link ManagedObject} to
	 *            aid in extracting the extension interface.
	 * @return Extension Interface.
	 * @throws Throwable
	 *             If fails to extract the extension.
	 */
	E extractExtension(ManagedObject managedObject, ManagedObjectMetaData<?> managedObjectMetaData) throws Throwable;

}