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

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.SupplierThreadLocalNode;

/**
 * {@link SupplierThreadLocalNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierThreadLocalNodeImpl implements SupplierThreadLocalNode {

	/**
	 * Generates the name for the {@link SupplierThreadLocalNode}.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 * @return {@link SupplierThreadLocalNode} name.
	 */
	public static String getSupplierThreadLocalName(String qualifier, String type) {
		return CompileUtil.isBlank(qualifier) ? type : qualifier + "-" + type;
	}

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Qualifier. May be <code>null</code>.
	 */
	private final String qualifier;

	/**
	 * Type.
	 */
	private final String type;

	/**
	 * {@link SupplierNode}.
	 */
	private final SupplierNode supplierNode;

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
		 * {@link OptionalThreadLocalReceiver}.
		 */
		private final OptionalThreadLocalReceiver optionalThreadLocalReceiver;

		/**
		 * Instantiate.
		 * 
		 * @param optionalThreadLocalReceiver {@link OptionalThreadLocalReceiver}.
		 */
		private InitialisedState(OptionalThreadLocalReceiver optionalThreadLocalReceiver) {
			this.optionalThreadLocalReceiver = optionalThreadLocalReceiver;
		}
	}

	/**
	 * Initiate.
	 * 
	 * @param qualifier    Qualifier. May be <code>null</code>.
	 * @param type         Type.
	 * @param supplierNode {@link SupplierNode}.
	 * @param context      {@link NodeContext}.
	 */
	public SupplierThreadLocalNodeImpl(String qualifier, String type, SupplierNode supplierNode, NodeContext context) {
		this.name = getSupplierThreadLocalName(qualifier, type);
		this.qualifier = qualifier;
		this.type = type;
		this.supplierNode = supplierNode;
		this.context = context;
	}

	/*
	 * ========================= Node ===========================
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
		return this.supplierNode;
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
	public void initialise(OptionalThreadLocalReceiver optionalThreadLocalReceiver) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(optionalThreadLocalReceiver));
	}

	/*
	 * ============= OfficeFloorSupplierThreadLocal ================
	 */

	@Override
	public String getOfficeFloorSupplierThreadLocalName() {
		return this.name;
	}

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

	@Override
	public String getType() {
		return this.type;
	}

	/*
	 * ================= OfficeSupplierThreadLocal =================
	 */

	@Override
	public String getOfficeSupplierThreadLocalName() {
		return this.name;
	}

	/*
	 * ================ SupplierThreadLocalNode =====================
	 */

	@Override
	public SupplierNode getSupplierNode() {
		return this.supplierNode;
	}

	@Override
	public void buildSupplierThreadLocal(CompileContext context) {

		// Obtain the bound managed object fulfilling supplier thread local
		BoundManagedObjectNode managedObject = LinkUtil.retrieveTarget(this, BoundManagedObjectNode.class,
				this.context.getCompilerIssues());
		if (managedObject == null) {
			return; // must have dependency
		}

		// Source the optional thread local
		managedObject.buildSupplierThreadLocal(this.state.optionalThreadLocalReceiver);
	}

	/*
	 * =================== LinkObjectNode ============================
	 */

	/**
	 * linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectNode = null;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		return LinkUtil.linkObjectNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedObjectNode = link);
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}
