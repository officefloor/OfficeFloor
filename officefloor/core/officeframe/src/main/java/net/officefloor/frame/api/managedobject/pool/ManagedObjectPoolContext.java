package net.officefloor.frame.api.managedobject.pool;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolContext {

	/**
	 * {@link ManagedObjectSource} to have its {@link ManagedObject} instances
	 * pooled.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<?, ?> getManagedObjectSource();

	/**
	 * Indicates if the current {@link Thread} is managed. A managed
	 * {@link Thread} will notify the {@link ThreadCompletionListener} instances
	 * of its completion.
	 * 
	 * @return <code>true</code> if the current {@link Thread} is managed.
	 */
	boolean isCurrentThreadManaged();

}