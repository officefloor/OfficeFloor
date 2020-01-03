package net.officefloor.compile.impl.structure;

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.ExecutiveNode;
import net.officefloor.compile.internal.structure.LinkTeamOversightNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TeamOversightNode;
import net.officefloor.frame.api.executive.TeamOversight;

/**
 * {@link TeamOversightNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamOversightNodeImpl implements TeamOversightNode {

	/**
	 * {@link TeamOversight} name.
	 */
	private final String teamOversightName;

	/**
	 * {@link ExecutiveNode}.
	 */
	private final ExecutiveNode executive;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {
	}

	/**
	 * Instantiate.
	 * 
	 * @param teamOversightName {@link TeamOversight} name.
	 * @param executive         Parent {@link ExecutiveNode}.
	 * @param context           {@link NodeContext}.
	 */
	public TeamOversightNodeImpl(String teamOversightName, ExecutiveNode executive, NodeContext context) {
		this.teamOversightName = teamOversightName;
		this.executive = executive;
		this.context = context;
	}

	/*
	 * ======================== Node ===============================
	 */

	@Override
	public String getNodeName() {
		return this.teamOversightName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return this.executive;
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes();
	}

	/*
	 * =================== OfficeFloorTeamOversight ========================
	 */

	@Override
	public String getOfficeFloorTeamOversightName() {
		return this.teamOversightName;
	}

	/*
	 * ======================= TeamOversightNode ============================
	 */

	@Override
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState());
	}

	/*
	 * ==================== LinkTeamOversightNode ==========================
	 */

	/**
	 * Linked {@link LinkTeamOversightNode}.
	 */
	private LinkTeamOversightNode linkedTeamOversightNode;

	@Override
	public boolean linkTeamOversightNode(LinkTeamOversightNode node) {
		return LinkUtil.linkTeamOversightNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedTeamOversightNode = link);
	}

	@Override
	public LinkTeamOversightNode getLinkedTeamOversightNode() {
		return this.linkedTeamOversightNode;
	}

}