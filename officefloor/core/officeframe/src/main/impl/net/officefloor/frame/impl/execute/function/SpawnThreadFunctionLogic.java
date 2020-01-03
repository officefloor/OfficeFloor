package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionLogic} to spawn a {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class SpawnThreadFunctionLogic implements FunctionLogic {

	/**
	 * {@link FlowMetaData} for the {@link ThreadState}.
	 */
	private final FlowMetaData flowMetaData;

	/**
	 * Parameter for the initial {@link ManagedFunction} of the {@link Flow}.
	 */
	private final Object parameter;

	/**
	 * {@link FlowCompletion}.
	 */
	private final FlowCompletion completion;

	/**
	 * Current {@link ThreadState}
	 */
	private final ThreadState currentThreadState;

	/**
	 * Instantiate.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter for the initial {@link ManagedFunction} of the
	 *            {@link Flow}.
	 * @param completion
	 *            Optional {@link FlowCompletion}.
	 * @param currentThreadState
	 *            Current {@link ThreadState}.
	 */
	public SpawnThreadFunctionLogic(FlowMetaData flowMetaData, Object parameter, FlowCompletion completion,
			ThreadState currentThreadState) {
		this.flowMetaData = flowMetaData;
		this.parameter = parameter;
		this.completion = completion;
		this.currentThreadState = currentThreadState;
	}

	/*
	 * ========================= Object ================================
	 */

	@Override
	public String toString() {
		return "Spawning ThreadState (synchronize ThreadState "
				+ Integer.toHexString(this.currentThreadState.hashCode()) + ")";
	}

	/*
	 * ====================== FunctionState ============================
	 */

	@Override
	public boolean isRequireThreadStateSafety() {
		return true; // must synchronise on thread state to keep threads safe
	}

	@Override
	public FunctionState execute(Flow flow) {

		// Obtain the meta-data for the initial function of the flow
		ManagedFunctionMetaData<?, ?> initialFunctionMetaData = this.flowMetaData.getInitialFunctionMetaData();

		// Obtain the process state
		ProcessState processState = flow.getThreadState().getProcessState();

		// Create thread to execute asynchronously
		FunctionState spawnFunction = processState.spawnThreadState(initialFunctionMetaData, this.parameter,
				this.completion, false);

		// Return the function to spawn the thread state
		return spawnFunction;
	}

}