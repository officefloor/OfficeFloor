package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Instance of a {@link ManagedObjectSource} and items to support it.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceInstance<F extends Enum<F>> {

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<?, F> getManagedObjectSource();

	/**
	 * Obtains the {@link ManagedObjectExecuteManagerFactory} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectExecuteManagerFactory} for the
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectExecuteManagerFactory<F> getManagedObjectExecuteManagerFactory();

	/**
	 * Obtains the {@link ManagedObjectPool}.
	 * 
	 * @return {@link ManagedObjectPool} or <code>null</code> if
	 *         {@link ManagedObjectSource} is not pooled.
	 */
	ManagedObjectPool getManagedObjectPool();

}