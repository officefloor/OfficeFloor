package net.officefloor.frame.api.thread;

import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Factory for the {@link ThreadSynchroniser}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadSynchroniserFactory {

	/**
	 * Creates the {@link ThreadSynchroniser} for new {@link ThreadState}.
	 * 
	 * @return {@link ThreadSynchroniser} for new {@link ThreadState}.
	 */
	ThreadSynchroniser createThreadSynchroniser();
}