package net.officefloor.frame.api.managedobject.pool;

/**
 * Factory for the creation of the {@link ThreadCompletionListener}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadCompletionListenerFactory {

	/**
	 * Creates the {@link ThreadCompletionListener}.
	 * 
	 * @param pool
	 *            {@link ManagedObjectPool}.
	 * @return {@link ThreadCompletionListener} for the
	 *         {@link ManagedObjectPool}.
	 */
	ThreadCompletionListener createThreadCompletionListener(ManagedObjectPool pool);

}