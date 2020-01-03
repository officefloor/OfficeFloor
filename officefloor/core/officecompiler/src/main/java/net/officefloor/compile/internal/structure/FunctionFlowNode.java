package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionFlow} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionFlowNode extends LinkFlowNode, FunctionFlow {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Function Flow";

	/**
	 * Initialises the {@link FunctionFlowNode}.
	 */
	void initialise();

	/**
	 * Indicates whether to spawn a {@link ThreadState} for this
	 * {@link FunctionFlow}.
	 * 
	 * @return <code>true</code> to spawn a {@link ThreadState} for this
	 *         {@link FunctionFlow}.
	 */
	boolean isSpawnThreadState();

	/**
	 * Specifies whether to spawn a {@link ThreadState} for this
	 * {@link FunctionFlow}.
	 * 
	 * @param isSpawnThreadState
	 *            <code>true</code> to spawn a {@link ThreadState} for this
	 *            {@link FunctionFlow}.
	 */
	void setSpawnThreadState(boolean isSpawnThreadState);

}