package net.officefloor.compile.pool;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * <code>Type definition</code> of a {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolType {

	/**
	 * Obtains the type of object being pooled.
	 * 
	 * @return Type of object being pooled.
	 */
	Class<?> getPooledObjectType();

	/**
	 * Obtains the {@link ManagedObjectPoolFactory} for the
	 * {@link ManagedObjectPool}.
	 * 
	 * @return {@link ManagedObjectPoolFactory} for the
	 *         {@link ManagedObjectPool}.
	 */
	ManagedObjectPoolFactory getManagedObjectPoolFactory();

	/**
	 * Obtains the {@link ThreadCompletionListenerFactory} instances.
	 * 
	 * @return {@link ThreadCompletionListenerFactory} instances.
	 */
	ThreadCompletionListenerFactory[] getThreadCompletionListenerFactories();

}