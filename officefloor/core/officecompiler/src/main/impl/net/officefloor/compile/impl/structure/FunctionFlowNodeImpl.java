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
import net.officefloor.compile.internal.structure.FunctionFlowNode;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionFlowNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionFlowNodeImpl implements FunctionFlowNode {

	/**
	 * Name of this {@link FunctionFlow}.
	 */
	private final String flowName;

	/**
	 * Indicates if this {@link FunctionFlow} is for a
	 * {@link ManagedFunctionEscalationType}.
	 */
	private final boolean isEscalation;

	/**
	 * Parent {@link ManagedFunctionNode}.
	 */
	private final ManagedFunctionNode function;

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
	 * Indicates if spawn {@link ThreadState}.
	 */
	private boolean isSpawnThreadState = false;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of this {@link FunctionFlow}.
	 * @param isEscalation
	 *            Indicates if this {@link FunctionFlow} is for a
	 *            {@link ManagedFunctionEscalationType}.
	 * @param function
	 *            Parent {@link ManagedFunctionNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public FunctionFlowNodeImpl(String flowName, boolean isEscalation, ManagedFunctionNode function,
			NodeContext context) {
		this.flowName = flowName;
		this.isEscalation = isEscalation;
		this.function = function;
		this.context = context;
	}

	/*
	 * ================== Node ======================================
	 */

	@Override
	public String getNodeName() {
		return this.flowName;
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
		return this.function;
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
	 * ================== FunctionFlow =================================
	 */

	@Override
	public String getFunctionFlowName() {
		return this.flowName;
	}

	/*
	 * ================== FunctionFlowNode =============================
	 */

	@Override
	public boolean isSpawnThreadState() {
		return this.isSpawnThreadState;
	}

	@Override
	public void setSpawnThreadState(boolean isSpawnThreadState) {
		// May only specify if not escalation
		if (!this.isEscalation) {
			this.isSpawnThreadState = isSpawnThreadState;
		}
	}

	/*
	 * =================== LinkFlowNode ==================================
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
