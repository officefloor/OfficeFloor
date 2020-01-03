package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionState} to synchronise the {@link ProcessState} with the
 * current {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class SynchroniseProcessStateFunctionLogic implements FunctionLogic {

	/**
	 * Current {@link ThreadState}.
	 */
	private final ThreadState currentThreadState;

	/**
	 * Instantiate.
	 * 
	 * @param currentThreadState
	 *            Current {@link ThreadState}.
	 */
	public SynchroniseProcessStateFunctionLogic(ThreadState currentThreadState) {
		this.currentThreadState = currentThreadState;
	}

	/*
	 * ======================== Object =============================
	 */

	@Override
	public String toString() {
		return "Synchronising ThreadState " + Integer.toHexString(this.currentThreadState.hashCode())
				+ " to ThreadState "
				+ Integer.toHexString(this.currentThreadState.getProcessState().getMainThreadState().hashCode());
	}

	/*
	 * ===================== FunctionLogic ==========================
	 */

	@Override
	public boolean isRequireThreadStateSafety() {
		// Ensure have thread state safety, to synchronise process state
		return true;
	}

	@Override
	public FunctionState execute(Flow flow) {

		// Synchronise process state (always undertaken via main thread state)
		synchronized (flow.getThreadState().getProcessState().getMainThreadState()) {
		}

		// Synchronized
		return null;
	}

}