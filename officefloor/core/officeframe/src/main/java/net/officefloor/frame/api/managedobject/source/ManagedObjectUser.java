package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * User interested in using the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectUser {

	/**
	 * <p>
	 * Specifies the {@link ManagedObject} to be used.
	 * <p>
	 * This will be called by the
	 * {@link ManagedObjectSource#sourceManagedObject(ManagedObjectUser)} method
	 * to provide the {@link ManagedObject} to this {@link ManagedObjectUser}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to be used.
	 */
	void setManagedObject(ManagedObject managedObject);

	/**
	 * Indicates failure to obtain the {@link ManagedObject}.
	 * 
	 * @param cause
	 *            Cause of the failure.
	 */
	void setFailure(Throwable cause);

}