package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * Manages the clean up of {@link ManagedObject} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectCleanup {

	/**
	 * Creates the clean up {@link FunctionState}.
	 * 
	 * @param recycleFlowMetaData
	 *            {@link FlowMetaData} to recycle the {@link ManagedObject}.
	 * @param objectType
	 *            Type of the object from the {@link ManagedObject}.
	 * @param managedObject
	 *            {@link ManagedObject} to be cleaned up.
	 * @param managedObjectPool
	 *            Optional {@link ManagedObjectPool} to return the
	 *            {@link ManagedObject}. May be <code>null</code>.
	 * @return {@link FunctionState} to clean up the {@link ManagedObject}.
	 */
	FunctionState cleanup(FlowMetaData recycleFlowMetaData, Class<?> objectType, ManagedObject managedObject,
			ManagedObjectPool managedObjectPool);

}