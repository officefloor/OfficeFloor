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

import net.officefloor.compile.impl.office.OfficeOutputTypeImpl;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeOutputNode;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.spi.office.OfficeOutput;

/**
 * Implementation of the {@link OfficeOutputNode}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeOutputNodeImpl implements OfficeOutputNode {

	/**
	 * Name of this {@link OfficeFloorOutput}.
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
		 * Argument type from this {@link OfficeOutput}.
		 */
		private final String argumentType;

		/**
		 * Instantiate.
		 * 
		 * @param argumentType
		 *            Argument type from this {@link OfficeOutput}.
		 */
		public InitialisedState(String argumentType) {
			this.argumentType = argumentType;
		}
	}

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of this {@link OfficeOutput}.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeOutputNodeImpl(String name, OfficeNode office, NodeContext context) {
		this.name = name;
		this.officeNode = office;
		this.context = context;
	}

	/*
	 * ================= Node =============================
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
	public void initialise(String argumentType) {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState(argumentType));
	}

	/*
	 * =================== OfficeOuput ===============================
	 */

	@Override
	public String getOfficeOutputName() {
		return this.name;
	}

	/*
	 * ================= OfficeOuputNode =============================
	 */

	@Override
	public OfficeOutputType loadOfficeOutputType(CompileContext compileContext) {
		return new OfficeOutputTypeImpl(this.name, this.state.argumentType);
	}

	/*
	 * ==================== LinkFlowNode =============================
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

}
