package net.officefloor.compile.internal.structure;

/**
 * {@link LinkExecutionStrategyNode} that can be linked to another
 * {@link LinkExecutionStrategyNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkExecutionStrategyNode extends Node {

	/**
	 * Links the input {@link LinkExecutionStrategyNode} to this
	 * {@link LinkExecutionStrategyNode}.
	 * 
	 * @param node {@link LinkExecutionStrategyNode} to link to this
	 *             {@link LinkExecutionStrategyNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkExecutionStrategyNode(LinkExecutionStrategyNode node);

	/**
	 * Obtains the {@link LinkExecutionStrategyNode} linked to this
	 * {@link LinkExecutionStrategyNode}.
	 * 
	 * @return {@link LinkExecutionStrategyNode} linked to this
	 *         {@link LinkExecutionStrategyNode}.
	 */
	LinkExecutionStrategyNode getLinkedExecutionStrategyNode();

}