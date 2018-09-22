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
import net.officefloor.compile.internal.structure.LinkExecutionStrategyNode;
import net.officefloor.compile.internal.structure.ManagedObjectExecutionStrategyNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectExecutionStrategy;

/**
 * {@link ManagedObjectExecutionStrategyNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecutionStrategyNodeImpl implements ManagedObjectExecutionStrategyNode {

	/**
	 * Name of this {@link OfficeFloorManagedObjectExecutionStrategy}.
	 */
	private final String executionStrategyName;

	/**
	 * {@link ManagedObjectSourceNode} containing this
	 * {@link ManagedObjectExecutionStrategyNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSourceNode;

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
	 * @param executionStrategyName   Name of this
	 *                                {@link OfficeFloorManagedObjectExecutionStrategy}.
	 * @param managedObjectSourceNode {@link ManagedObjectSourceNode} containing
	 *                                this
	 *                                {@link ManagedObjectExecutionStrategyNode}.
	 * @param context                 {@link NodeContext}.
	 */
	public ManagedObjectExecutionStrategyNodeImpl(String executionStrategyName,
			ManagedObjectSourceNode managedObjectSourceNode, NodeContext context) {
		this.executionStrategyName = executionStrategyName;
		this.managedObjectSourceNode = managedObjectSourceNode;
		this.context = context;
	}

	/*
	 * ====================== Node ===============================
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
		return this.managedObjectSourceNode;
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
	 * ============= OfficeFloorManagedObjectExecutionStrategy ========
	 */

	@Override
	public String getManagedObjectExecutionStrategyName() {
		return this.executionStrategyName;
	}

	/*
	 * ================ ExecutionStrategyNode =========================
	 */

	@Override
	public void initialise() {
		this.state = new InitialisedState();
	}

	/*
	 * ================ LinkExecutionStrategyNode =====================
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