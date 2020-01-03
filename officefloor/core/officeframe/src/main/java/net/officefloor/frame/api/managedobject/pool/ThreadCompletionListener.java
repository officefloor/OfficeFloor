package net.officefloor.frame.api.managedobject.pool;

/**
 * Listener on the completion of a {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadCompletionListener {

	/**
	 * Notifies that the {@link Thread} has completed.
	 */
	void threadComplete();

}