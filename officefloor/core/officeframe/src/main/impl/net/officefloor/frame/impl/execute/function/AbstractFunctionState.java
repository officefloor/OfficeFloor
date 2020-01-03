package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Abstract {@link FunctionState}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFunctionState extends AbstractLinkedListSetEntry<FunctionState, Flow>
		implements FunctionState {

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState;

	/**
	 * Instantiate.
	 * 
	 * @param threadState
	 *            {@link ThreadState}.
	 */
	public AbstractFunctionState(ThreadState threadState) {
		this.threadState = threadState;
	}

	/*
	 * ===================== FunctionState ========================
	 */

	@Override
	public ThreadState getThreadState() {
		return this.threadState;
	}

}
