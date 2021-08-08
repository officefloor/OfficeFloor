/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * ============== AugmentedManagedObjectExecutionStrategy =========
	 */

	@Override
	public boolean isLinked() {
		return (this.linkedExecutionStrategyNode != null);
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
