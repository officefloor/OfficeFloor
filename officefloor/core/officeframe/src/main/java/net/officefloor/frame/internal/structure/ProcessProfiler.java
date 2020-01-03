package net.officefloor.frame.internal.structure;

/**
 * Profiler of the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessProfiler {

	/**
	 * Adds a {@link ThreadState} to be profiled.
	 * 
	 * @param threadState
	 *            {@link ThreadState}.
	 * @return {@link ThreadState} to be profiled.
	 */
	ThreadProfiler addThreadState(ThreadState threadState);

	/**
	 * Invoked once the {@link ProcessState} is complete.
	 */
	void processStateCompleted();

}