package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Meta-data of a {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlowMetaData {

	/**
	 * Obtains the {@link ManagedFunctionMetaData} of the initial
	 * {@link ManagedFunction} within the {@link Flow}.
	 * 
	 * @return {@link ManagedFunctionMetaData} of the initial
	 *         {@link ManagedFunction} within the {@link Flow}.
	 */
	ManagedFunctionMetaData<?, ?> getInitialFunctionMetaData();

	/**
	 * Indicates whether the {@link Flow} should be instigated within a spawned
	 * {@link ThreadState}.
	 * 
	 * @return <code>true</code> to execute the {@link Flow} within a spawned
	 *         {@link ThreadState}.
	 */
	boolean isSpawnThreadState();

}