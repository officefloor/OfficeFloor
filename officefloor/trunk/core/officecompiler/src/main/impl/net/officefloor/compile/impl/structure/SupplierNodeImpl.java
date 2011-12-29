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
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
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
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link SupplierType}.
	 */
	private SupplierType supplierType;

	/**
	 * Indicates if the {@link SupplierType} has been attempted to be loaded.
	 */
	private boolean isSupplierTypeLoaded = false;

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
		return this.officeFloorNode.addManagedObjectSource(
				managedObjectSourceName, new SuppliedManagedObjectNodeImpl(
						autoWire, this, this.context));
	}

	/*
	 * =================== SupplierNode =========================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SupplierType loadSupplierType() {

		// Only load the supplier type once (whether successful or not)
		if (this.isSupplierTypeLoaded) {
			return this.supplierType;
		}
		this.isSupplierTypeLoaded = true;

		// Load the supplier source class
		Class supplierSourceClass = this.context.getSupplierSourceClass(
				this.supplierSourceClassName, this.officeFloorLocation,
				this.supplierName);
		if (supplierSourceClass == null) {
			return null; // must have supplier source class
		}

		// Load the supplier type
		SupplierLoader supplierLoader = this.context.getSupplierLoader(
				this.officeFloorLocation, this.supplierName);
		this.supplierType = supplierLoader.loadSupplierType(
				supplierSourceClass, this.propertyList);

		// Return the supplier type
		return this.supplierType;
	}

}