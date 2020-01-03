package net.officefloor.compile.internal.structure;

/**
 * {@link LinkOfficeNode} that can be linked to another {@link LinkOfficeNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkOfficeNode extends Node {

	/**
	 * Links the input {@link LinkOfficeNode} to this {@link LinkOfficeNode}.
	 * 
	 * @param node
	 *            {@link LinkOfficeNode} to link to this {@link LinkOfficeNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkOfficeNode(LinkOfficeNode node);

	/**
	 * Obtains the {@link LinkOfficeNode} linked to this {@link LinkOfficeNode}.
	 * 
	 * @return {@link LinkOfficeNode} linked to this {@link LinkOfficeNode}.
	 */
	LinkOfficeNode getLinkedOfficeNode();

}