package net.officefloor.frame.api.managedobject.pool;

/**
 * Factory for the creation a {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolFactory {

	/**
	 * Creates a {@link ManagedObjectPool}.
	 * 
	 * @param managedObjectPoolContext
	 *            {@link ManagedObjectPoolContext}.
	 * @return {@link ManagedObjectPool}.
	 * @throws Throwable
	 *             If fails to create the {@link ManagedObjectPool}.
	 */
	ManagedObjectPool createManagedObjectPool(ManagedObjectPoolContext managedObjectPoolContext) throws Throwable;

}