package net.officefloor.compile.internal.structure;

/**
 * {@link LinkObjectNode} that can be linked to another {@link LinkObjectNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkObjectNode extends Node {

	/**
	 * Links the input {@link LinkObjectNode} to this {@link LinkObjectNode}.
	 * 
	 * @param node
	 *            {@link LinkObjectNode} to link to this {@link LinkObjectNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkObjectNode(LinkObjectNode node);

	/**
	 * Obtains the {@link LinkObjectNode} linked to this {@link LinkObjectNode}.
	 * 
	 * @return {@link LinkObjectNode} linked to this {@link LinkObjectNode}.
	 */
	LinkObjectNode getLinkedObjectNode();

}