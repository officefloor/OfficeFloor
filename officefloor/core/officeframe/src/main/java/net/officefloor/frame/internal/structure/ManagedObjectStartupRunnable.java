package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;

/**
 * {@link Runnable} wrapping the invocation of the
 * {@link ManagedObjectStartupProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectStartupRunnable extends Runnable {

	/**
	 * Indicates to execute this {@link ManagedObjectStartupRunnable} concurrently
	 * on another {@link Thread}.
	 * 
	 * @return <code>true</code> to execute concurrently.
	 */
	boolean isConcurrent();

}