package net.officefloor.compile.spi.pool.source;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * Meta-data regarding the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolSourceMetaData {

	/**
	 * Obtains the type of object expected to be pooled by this
	 * {@link ManagedObjectPool}.
	 * 
	 * @return Type of object expected to be pooled by this
	 *         {@link ManagedObjectPool}. This may be a super type of the actual
	 *         object.
	 */
	Class<?> getPooledObjectType();

	/**
	 * Obtains the {@link ManagedObjectPoolFactory}.
	 * 
	 * @return {@link ManagedObjectPoolFactory}.
	 */
	ManagedObjectPoolFactory getManagedObjectPoolFactory();

	/**
	 * Obtains the {@link ThreadCompletionListenerFactory} instances.
	 * 
	 * @return {@link ThreadCompletionListenerFactory} instances. May be
	 *         <code>null</code> if no {@link ThreadCompletionListener}
	 *         instances.
	 */
	ThreadCompletionListenerFactory[] getThreadCompleteListenerFactories();

}