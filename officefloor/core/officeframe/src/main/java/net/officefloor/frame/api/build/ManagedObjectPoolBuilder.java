package net.officefloor.frame.api.build;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * Builder for the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolBuilder {

	/**
	 * <p>
	 * Adds a {@link ThreadCompletionListener}.
	 * <p>
	 * This allows the {@link ManagedObjectPool} to cache objects to
	 * {@link ThreadLocal} instances and be notified when the {@link Thread} is
	 * complete to clean up the {@link ThreadLocal} state.
	 * 
	 * @param threadCompletionListenerFactory
	 *            {@link ThreadCompletionListenerFactory}.
	 */
	void addThreadCompletionListener(ThreadCompletionListenerFactory threadCompletionListenerFactory);

}