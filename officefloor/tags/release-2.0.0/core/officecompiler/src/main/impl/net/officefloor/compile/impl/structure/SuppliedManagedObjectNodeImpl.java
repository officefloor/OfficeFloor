/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import net.officefloor.compile.internal.structure.SuppliedManagedObjectNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

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
	 * {@link SuppliedManagedObject}.
	 */
	private SuppliedManagedObject<?, ?> suppliedManagedObject = null;

	/**
	 * Initiate.
	 * 
	 * @param autoWire
	 *            {@link AutoWire} to identify the supplied
	 *            {@link ManagedObjectSource}.
	 * @param supplierNode
	 *            {@link SupplierNode}.
	 */
	public SuppliedManagedObjectNodeImpl(AutoWire autoWire,
			SupplierNode supplierNode) {
		this.autoWire = autoWire;
		this.supplierNode = supplierNode;
	}

	/*
	 * ===================== SuppliedManagedObjectNode =================
	 */

	@Override
	public void loadSuppliedManagedObject() {
		// Load the supplier (to fill this supply order)
		this.supplierNode.fillSupplyOrders();
	}

	@Override
	public SuppliedManagedObject<?, ?> getSuppliedManagedObject() {
		return this.suppliedManagedObject;
	}

	/*
	 * =========================== SupplyOrder =========================
	 */

	@Override
	public AutoWire getAutoWire() {
		return this.autoWire;
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>> void fillOrder(
			SuppliedManagedObject<D, F> suppliedManagedObject) {
		this.suppliedManagedObject = suppliedManagedObject;
	}

}