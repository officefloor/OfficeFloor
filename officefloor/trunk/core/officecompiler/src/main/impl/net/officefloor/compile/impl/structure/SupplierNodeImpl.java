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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.autowire.supplier.SupplyOrder;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link SupplierNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierNodeImpl implements SupplierNode {

	/**
	 * Name of the {@link OfficeFloorSupplier}.
	 */
	private final String supplierName;

	/**
	 * {@link SupplierSource} {@link Class} name.
	 */
	private final String supplierSourceClassName;

	/**
	 * {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode officeFloorNode;

	/**
	 * {@link OfficeFloor} location.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link PropertyList} to source the supplier.
	 */
	private final PropertyList propertyList = new PropertyListImpl();

	/**
	 * {@link SupplyOrder} instances.
	 */
	private final List<SupplyOrder> supplyOrders = new LinkedList<SupplyOrder>();

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Indicates if the {@link SupplyOrder} instances have been filled.
	 */
	private boolean isSupplyOrdersFilled = false;

	/**
	 * Initiate.
	 * 
	 * @param supplierName
	 *            Name of the {@link OfficeFloorSupplier}.
	 * @param supplierSourceClassName
	 *            {@link SupplierSource} {@link Class} name.
	 * @param officeFloorNode
	 *            {@link OfficeFloorNode}.
	 * @param officeFloorLocation
	 *            {@link OfficeFloor} location.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SupplierNodeImpl(String supplierName,
			String supplierSourceClassName, OfficeFloorNode officeFloorNode,
			String officeFloorLocation, NodeContext context) {
		this.supplierName = supplierName;
		this.supplierSourceClassName = supplierSourceClassName;
		this.officeFloorNode = officeFloorNode;
		this.officeFloorLocation = officeFloorLocation;
		this.context = context;
	}

	/*
	 * =============== OfficeFloorSupplier ======================
	 */

	@Override
	public String getOfficeFloorSupplierName() {
		return this.supplierName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName, AutoWire autoWire) {

		// Ensure supply orders not yet filled
		if (this.isSupplyOrdersFilled) {
			throw new IllegalStateException("Can not add "
					+ OfficeFloorManagedObjectSource.class.getSimpleName()
					+ " once " + SupplyOrder.class.getSimpleName()
					+ " instances are filled");
		}

		// Create the supplied managed object node
		SuppliedManagedObjectNode suppliedManagedObjectNode = new SuppliedManagedObjectNodeImpl(
				autoWire, this);

		// Register the supply order
		this.supplyOrders.add(suppliedManagedObjectNode);

		// Add and return the managed object source
		return this.officeFloorNode.addManagedObjectSource(
				managedObjectSourceName, suppliedManagedObjectNode);
	}

	/*
	 * =================== SupplierNode =========================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void fillSupplyOrders() {

		// Only fill supply orders once (whether successful or not)
		if (this.isSupplyOrdersFilled) {
			return;
		}
		this.isSupplyOrdersFilled = true;

		// Load the supplier source class
		Class supplierSourceClass = this.context.getSupplierSourceClass(
				this.supplierSourceClassName, this.officeFloorLocation,
				this.supplierName);
		if (supplierSourceClass == null) {
			return; // must have supplier source class
		}

		// Fill the supply orders
		SupplierLoader supplierLoader = this.context.getSupplierLoader(
				this.officeFloorLocation, this.supplierName);
		supplierLoader.fillSupplyOrders(supplierSourceClass, this.propertyList,
				this.supplyOrders.toArray(new SupplyOrder[this.supplyOrders
						.size()]));
	}

}