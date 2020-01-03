package net.officefloor.frame.impl.execute.flow;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link FlowMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowMetaDataImpl implements FlowMetaData {

	/**
	 * {@link ManagedFunctionMetaData} of the initial {@link ManagedFunction} of
	 * the {@link Flow}.
	 */
	private final ManagedFunctionMetaData<?, ?> initialFunctionMetaData;

	/**
	 * Indicates whether the {@link Flow} should be instigated in a spawned
	 * {@link ThreadState}.
	 */
	private final boolean isSpawnThreadState;

	/**
	 * Initiate.
	 * 
	 * @param isSpawnThreadState
	 *            Indicates whether the {@link Flow} should be instigated in a
	 *            spawned {@link ThreadState}.
	 * @param initialFunctionMetaData
	 *            {@link ManagedFunctionMetaData} of the initial
	 *            {@link ManagedFunction} of the {@link Flow}.
	 */
	public FlowMetaDataImpl(boolean isSpawnThreadState, ManagedFunctionMetaData<?, ?> initialFunctionMetaData) {
		this.isSpawnThreadState = isSpawnThreadState;
		this.initialFunctionMetaData = initialFunctionMetaData;
	}

	/*
	 * =================== FlowMetaData ======================================
	 */

	@Override
	public ManagedFunctionMetaData<?, ?> getInitialFunctionMetaData() {
		return this.initialFunctionMetaData;
	}

	@Override
	public boolean isSpawnThreadState() {
		return this.isSpawnThreadState;
	}

}