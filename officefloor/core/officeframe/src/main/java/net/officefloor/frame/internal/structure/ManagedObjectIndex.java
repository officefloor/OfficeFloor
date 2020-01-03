package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Index of the {@link ManagedObject}, providing both the scope it lives within
 * and the index of it within that scope.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectIndex {

	/**
	 * Obtains the {@link ManagedObjectScope} that the {@link ManagedObject}
	 * resides within.
	 * 
	 * @return {@link ManagedObjectScope} that the {@link ManagedObject} resides
	 *         within.
	 */
	ManagedObjectScope getManagedObjectScope();

	/**
	 * Obtains the index of the {@link ManagedObject} within the
	 * {@link ManagedObjectScope}.
	 * 
	 * @return Index of the {@link ManagedObject} within the
	 *         {@link ManagedObjectScope}.
	 */
	int getIndexOfManagedObjectWithinScope();
}
