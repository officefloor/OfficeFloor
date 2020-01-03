package net.officefloor.compile.internal.structure;

/**
 * {@link LinkTeamNode} that can be linked to another {@link LinkTeamNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkTeamNode extends Node {

	/**
	 * Links the input {@link LinkTeamNode} to this {@link LinkTeamNode}.
	 * 
	 * @param node
	 *            {@link LinkTeamNode} to link to this {@link LinkTeamNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkTeamNode(LinkTeamNode node);

	/**
	 * Obtains the {@link LinkTeamNode} linked to this {@link LinkTeamNode}.
	 * 
	 * @return {@link LinkTeamNode} linked to this {@link LinkTeamNode}.
	 */
	LinkTeamNode getLinkedTeamNode();

}