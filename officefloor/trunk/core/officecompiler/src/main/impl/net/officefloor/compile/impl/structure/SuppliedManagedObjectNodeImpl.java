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
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.compile.internal.structure.NodeContext;
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
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

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
	public SuppliedManagedObjectNodeImpl(AutoWire autoWire,
			SupplierNode supplierNode, NodeContext context) {
		this.autoWire = autoWire;
		this.supplierNode = supplierNode;
		this.context = context;
	}

	/*
	 * ===================== SuppliedManagedObjectNode =================
	 */

	@Override
	public void loadSuppliedManagedObject() {

		// Load the supplier type
		SupplierType supplierType = this.supplierNode.loadSupplierType();
		if (supplierType == null) {
			return; // no supplier type, so no supply of managed objects
		}

		// TODO implement SuppliedManagedObjectNode.loadSuppliedManagedObject
		throw new UnsupportedOperationException(
				"TODO implement SuppliedManagedObjectNode.loadSuppliedManagedObject");
	}

	@Override
	public SuppliedManagedObject<?, ?> getSuppliedManagedObject() {
		// TODO implement SuppliedManagedObjectNode.getSuppliedManagedObject
		throw new UnsupportedOperationException(
				"TODO implement SuppliedManagedObjectNode.getSuppliedManagedObject");
	}

}