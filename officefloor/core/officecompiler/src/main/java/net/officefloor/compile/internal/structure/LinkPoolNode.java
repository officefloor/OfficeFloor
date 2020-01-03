package net.officefloor.compile.internal.structure;

/**
 * {@link LinkPoolNode} that can be linked to another {@link LinkPoolNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkPoolNode extends Node {

	/**
	 * Links the input {@link LinkPoolNode} to this {@link LinkPoolNode}.
	 * 
	 * @param node
	 *            {@link LinkPoolNode} to link to this {@link LinkPoolNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkPoolNode(LinkPoolNode node);

	/**
	 * Obtains the {@link LinkPoolNode} linked to this {@link LinkPoolNode}.
	 * 
	 * @return {@link LinkPoolNode} linked to this {@link LinkPoolNode}.
	 */
	LinkPoolNode getLinkedPoolNode();

}