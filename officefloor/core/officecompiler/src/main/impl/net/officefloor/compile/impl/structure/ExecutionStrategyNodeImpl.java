/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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