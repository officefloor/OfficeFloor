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
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;

/**
 * {@link ManagedObjectFlowNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFlowNodeImpl implements ManagedObjectFlowNode {

	/**
	 * Name of this {@link ManagedObjectFlow}.
	 */
	private final String managedObjectFlowName;

	/**
	 * Parent {@link ManagedObjectSourceNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSource;

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
	 * Initiate.
	 * 
	 * @param managedObjectFlowName Name of this {@link ManagedObjectFlow}.
	 * @param managedObjectSource   Parent {@link ManagedObjectSourceNode}.
	 * @param context               {@link NodeContext}.
	 */
	public ManagedObjectFlowNodeImpl(String managedObjectFlowName, ManagedObjectSourceNode managedObjectSource,
			NodeContext context) {
		this.managedObjectFlowName = managedObjectFlowName;
		this.managedObjectSource = managedObjectSource;
		this.context = context;
	}

	/*
	 * =================== Node =============================
	 */

	@Override
	public String getNodeName() {
		return this.managedObjectFlowName;
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
		return this.managedObjectSource;
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes();
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState());
	}

	/*
	 * =================== ManagedObjectFlow =============================
	 */

	@Override
	public String getManagedObjectFlowName() {
		return this.managedObjectFlowName;
	}

	/*
	 * ================ AugmentedManagedObjectFlow =======================
	 */

	@Override
	public boolean isLinked() {
		return (this.linkedFlowNode != null);
	}

	/*
	 * =================== LinkFlowNode =================================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {
		return LinkUtil.linkFlowNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedFlowNode = link);
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

}
