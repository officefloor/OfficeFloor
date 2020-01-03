package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Scopes for a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public enum ManagedObjectScope {

	/**
	 * {@link ManagedObject} bound to {@link ManagedFunction}.
	 */
	FUNCTION,

	/**
	 * {@link ManagedObject} bound to {@link ThreadState}.
	 */
	THREAD,

	/**
	 * {@link ManagedObject} bound to {@link ProcessState}.
	 */
	PROCESS
}
