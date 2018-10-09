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

import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.SupplierThreadLocalNode;

/**
 * {@link SupplierThreadLocalNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierThreadLocalNodeImpl implements SupplierThreadLocalNode {

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
		return (this.qualifier != null ? this.qualifier + "-" : "") + this.type;
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
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState());
	}

	/*
	 * ================ SupplierThreadLocalNode =====================
	 */

	@Override
	public SupplierNode getSupplierNode() {
		return this.supplierNode;
	}

	/*
	 * =================== LinkObjectNode ============================
	 */

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		// TODO implement LinkObjectNode.linkObjectNode(...)
		throw new UnsupportedOperationException("TODO implement LinkObjectNode.linkObjectNode(...)");
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		// TODO implement LinkObjectNode.getLinkedObjectNode(...)
		throw new UnsupportedOperationException("TODO implement LinkObjectNode.getLinkedObjectNode(...)");
	}

}