package net.officefloor.frame.impl.construct.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FlowConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowConfigurationImpl<F extends Enum<F>> implements FlowConfiguration<F> {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * Reference to the initial {@link ManagedFunction} of this {@link Flow}.
	 */
	private final ManagedFunctionReference functionReference;

	/**
	 * Indicates whether to spawn the {@link ThreadState}.
	 */
	private final boolean isSpawnThreadState;

	/**
	 * Index of the {@link Flow}.
	 */
	private final int index;

	/**
	 * Key of the {@link Flow}.
	 */
	private final F key;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of this {@link Flow}.
	 * @param functionReference
	 *            Reference to the initial {@link ManagedFunction} of this
	 *            {@link Flow}.
	 * @param isSpawnThreadState
	 *            Indicates whether to spawn the {@link ThreadState}.
	 * @param index
	 *            Index of this {@link Flow}.
	 * @param key
	 *            Key of the {@link Flow}.
	 */
	public FlowConfigurationImpl(String flowName, ManagedFunctionReference functionReference,
			boolean isSpawnThreadState, int index, F key) {
		this.flowName = flowName;
		this.functionReference = functionReference;
		this.isSpawnThreadState = isSpawnThreadState;
		this.index = index;
		this.key = key;
	}

	/*
	 * ======================= FlowConfiguration ==============================
	 */

	@Override
	public String getFlowName() {
		return this.flowName;
	}

	@Override
	public ManagedFunctionReference getInitialFunction() {
		return this.functionReference;
	}

	@Override
	public boolean isSpawnThreadState() {
		return this.isSpawnThreadState;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public F getKey() {
		return this.key;
	}

}