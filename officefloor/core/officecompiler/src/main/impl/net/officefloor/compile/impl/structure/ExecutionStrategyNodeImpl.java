package net.officefloor.compile.impl.structure;

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.ExecutionStrategyNode;
import net.officefloor.compile.internal.structure.ExecutiveNode;
import net.officefloor.compile.internal.structure.LinkExecutionStrategyNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.frame.api.executive.ExecutionStrategy;

/**
 * {@link ExecutionStrategyNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutionStrategyNodeImpl implements ExecutionStrategyNode {

	/**
	 * {@link ExecutionStrategy} name.
	 */
	private final String executionStrategyName;

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
	 * @param executionStrategyName {@link ExecutionStrategy} name.
	 * @param executive             Parent {@link ExecutiveNode}.
	 * @param context               {@link NodeContext}.
	 */
	public ExecutionStrategyNodeImpl(String executionStrategyName, ExecutiveNode executive, NodeContext context) {
		this.executionStrategyName = executionStrategyName;
		this.executive = executive;
		this.context = context;
	}

	/*
	 * ================== Node ========================
	 */

	@Override
	public String getNodeName() {
		return this.executionStrategyName;
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
	 * =============== OfficeFloorExecutionStrategy ==================
	 */

	@Override
	public String getOfficeFloorExecutionStratgyName() {
		return this.executionStrategyName;
	}

	/*
	 * ================== ExecutionStrategyNode ======================
	 */

	@Override
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState());
	}

	/*
	 * ================= LinkExecutionStrategyName ====================
	 */

	/**
	 * Linked {@link LinkExecutionStrategyNode}.
	 */
	private LinkExecutionStrategyNode linkedExecutionStrategyNode = null;

	@Override
	public boolean linkExecutionStrategyNode(LinkExecutionStrategyNode node) {
		return LinkUtil.linkExecutionStrategyNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedExecutionStrategyNode = link);
	}

	@Override
	public LinkExecutionStrategyNode getLinkedExecutionStrategyNode() {
		return this.linkedExecutionStrategyNode;
	}

}