package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data for the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessMetaData {

	/**
	 * Creates a {@link ProcessState} identifier for a new {@link ProcessState}.
	 * 
	 * @return New {@link ProcessState} identifier.
	 */
	Object createProcessIdentifier();

	/**
	 * Obtains the {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 * instances bound to the {@link ProcessState}.
	 * 
	 * @return {@link ManagedObjectMetaData} of the {@link ManagedObject} instances
	 *         bound to the {@link ProcessState}.
	 */
	ManagedObjectMetaData<?>[] getManagedObjectMetaData();

	/**
	 * Obtains the {@link ThreadMetaData} of {@link ThreadState} instances spawned
	 * from the {@link ProcessState} of this {@link ProcessMetaData}.
	 * 
	 * @return {@link ThreadMetaData} of {@link ThreadState} instances spawned from
	 *         the {@link ProcessState} of this {@link ProcessMetaData}.
	 */
	ThreadMetaData getThreadMetaData();

}