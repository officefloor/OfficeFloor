package net.officefloor.compile.internal.structure;

/**
 * {@link LinkFlowNode} that can be linked to another {@link LinkFlowNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkFlowNode extends Node {

	/**
	 * Links the input {@link LinkFlowNode} to this {@link LinkFlowNode}.
	 * 
	 * @param node
	 *            {@link LinkFlowNode} to link to this {@link LinkFlowNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkFlowNode(LinkFlowNode node);

	/**
	 * Obtains the {@link LinkFlowNode} linked to this {@link LinkFlowNode}.
	 * 
	 * @return {@link LinkFlowNode} linked to this {@link LinkFlowNode}.
	 */
	LinkFlowNode getLinkedFlowNode();

}