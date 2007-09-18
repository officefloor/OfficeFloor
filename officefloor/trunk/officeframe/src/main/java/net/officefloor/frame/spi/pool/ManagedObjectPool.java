/*
 * Created on Jan 10, 2006
 */
package net.officefloor.frame.spi.pool;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * <p>
 * Pool of {@link net.officefloor.frame.spi.managedobject.ManagedObject}
 * instances.
 * 
 * @author Daniel
 */
public interface ManagedObjectPool {

	/**
	 * Initiates this {@link ManagedObjectPool}.
	 * 
	 * @param context
	 *            Context for this {@link ManagedObjectPool}.
	 * @throws Exception
	 *             If fails to initiate.
	 */
	void init(ManagedObjectPoolContext context) throws Exception;

	/**
	 * Sources the {@link ManagedObject} from this {@link ManagedObjectPool}.
	 * 
	 * @param user
	 *            {@link ManagedObjectUser} requiring the {@link ManagedObject}.
	 */
	void sourceManagedObject(ManagedObjectUser user);

	/**
	 * Returns an instance to the pool.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	void returnManagedObject(ManagedObject managedObject);

	/**
	 * Flags that the {@link ManagedObject} is lost.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to no longer be used.
	 */
	void lostManagedObject(ManagedObject managedObject);

}