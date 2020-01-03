package net.officefloor.compile.internal.structure;

/**
 * {@link LinkTeamOversightNode} that can be linked to another
 * {@link LinkTeamOversightNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkTeamOversightNode extends Node {

	/**
	 * Links the input {@link LinkTeamOversightNode} to this
	 * {@link LinkTeamOversightNode}.
	 * 
	 * @param node {@link LinkTeamOversightNode} to link to this
	 *             {@link LinkTeamOversightNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkTeamOversightNode(LinkTeamOversightNode node);

	/**
	 * Obtains the {@link LinkTeamOversightNode} linked to this
	 * {@link LinkTeamOversightNode}.
	 * 
	 * @return {@link LinkTeamOversightNode} linked to this
	 *         {@link LinkTeamOversightNode}.
	 */
	LinkTeamOversightNode getLinkedTeamOversightNode();

}