package net.officefloor.frame.api.managedobject;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Contract to provide control over asynchronous processing by the
 * {@link ManagedObject}.
 * <p>
 * Implemented by the {@link ManagedObjectSource} provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface AsynchronousManagedObject extends ManagedObject {

	/**
	 * Provides the {@link AsynchronousContext} to the
	 * {@link AsynchronousManagedObject} to enable call back to notify state and
	 * completion of asynchronous processing.
	 * 
	 * @param context
	 *            {@link AsynchronousContext}.
	 */
	void setAsynchronousContext(AsynchronousContext context);

}