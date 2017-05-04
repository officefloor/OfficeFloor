/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.autowire.supplier.SupplyOrder;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link SuppliedManagedObjectNodeImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectNodeImpl implements SuppliedManagedObjectNode {

	/**
	 * {@link AutoWire} to identify the supplied {@link ManagedObjectSource}.
	 */
	private final AutoWire autoWire;

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
	 * @param autoWire
	 *            {@link AutoWire} to identify the supplied
	 *            {@link ManagedObjectSource}.
	 * @param supplierNode
	 *            {@link SupplierNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SuppliedManagedObjectNodeImpl(AutoWire autoWire, SupplierNode supplierNode, NodeContext context) {
		this.autoWire = autoWire;
		this.supplierNode = supplierNode;
		this.context = context;
	}

	/*
	 * ===================== Node =================
	 */

	@Override
	public String getNodeName() {
		return this.autoWire.getQualifiedType();
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
	 * ===================== SuppliedManagedObjectNode =================
	 */

	@Override
	public SupplierNode getSupplierNode() {
		return this.supplierNode;
	}

	@Override
	public SuppliedManagedObject<?, ?> loadSuppliedManagedObject() {
		SupplyOrderImpl order = new SupplyOrderImpl();
		this.supplierNode.fillSupplyOrders(order);
		return order.suppliedManagedObject;
	}

	/**
	 * {@link SupplyOrder} implementation.
	 */
	private class SupplyOrderImpl implements SupplyOrder {

		/**
		 * {@link SuppliedManagedObject}.
		 */
		private SuppliedManagedObject<?, ?> suppliedManagedObject = null;

		/*
		 * =========================== SupplyOrder =========================
		 */

		@Override
		public AutoWire getAutoWire() {
			return SuppliedManagedObjectNodeImpl.this.autoWire;
		}

		@Override
		public <D extends Enum<D>, F extends Enum<F>> void fillOrder(
				SuppliedManagedObject<D, F> suppliedManagedObject) {
			this.suppliedManagedObject = suppliedManagedObject;
		}
	}

}