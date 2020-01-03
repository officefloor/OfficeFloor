package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Profiler of the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadProfiler extends FunctionState {

	/**
	 * Profiles execution of a {@link ManagedFunction}.
	 * 
	 * @param functionMetaData
	 *            {@link ManagedFunctionLogicMetaData} of the
	 *            {@link ManagedFunction} being executed.
	 */
	void profileManagedFunction(ManagedFunctionLogicMetaData functionMetaData);

}