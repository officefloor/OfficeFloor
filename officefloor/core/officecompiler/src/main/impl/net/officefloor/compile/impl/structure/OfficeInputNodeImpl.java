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

import net.officefloor.compile.impl.office.OfficeInputTypeImpl;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeInputNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.spi.office.OfficeInput;

/**
 * Implementation of the {@link OfficeInputNode}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeInputNodeImpl implements OfficeInputNode {

	/**
	 * Name of this {@link OfficeInput}.
	 */
	private final String name;

	/**
	 * Parent {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

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

		/**
		 * Parameter type of this {@link OfficeInput}.
		 */
		private final String parameterType;

		/**
		 * Instantiate.
		 * 
		 * @param parameterType
		 *            Parameter type of this {@link OfficeInput}.
		 */
		public InitialisedState(String parameterType) {
			this.parameterType = parameterType;
		}
	}

	/**
	 * Initialise.
	 * 
	 * @param name
	 *            Name of {@link OfficeInput}.
	 * @param officeNode
	 *            Parent {@link OfficeNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeInputNodeImpl(String name, OfficeNode officeNode, NodeContext context) {
		this.name = name;
		this.officeNode = officeNode;
		this.context = context;
	}

	/*
	 * ========================== Node ===============================
	 */

	@Override
	public String getNodeName() {
		return this.name;
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
		return this.officeNode;
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
	public void initialise(String parameterType) {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState(parameterType));
	}

	/*
	 * ======================= OfficeInputNode ===========================
	 */

	@Override
	public OfficeInputType loadOfficeInputType(CompileContext compileContext) {
		return new OfficeInputTypeImpl(this.name, this.state.parameterType);
	}

	/*
	 * ======================== LinkFlowNode =============================
	 */

	/**
	 * {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode = null;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {
		return LinkUtil.linkFlowNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedFlowNode = link);
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

	/*
	 * ========================= OfficeInput =============================
	 */

	@Override
	public String getOfficeInputName() {
		return this.name;
	}

}
