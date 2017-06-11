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

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.supplier.SupplierType;

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
	 * {@link PropertyList} to source the supplier.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link SuppliedManagedObjectSourceNode} instances.
	 */
	private final List<SuppliedManagedObjectSourceNode> suppliedManagedObjects = new LinkedList<SuppliedManagedObjectSourceNode>();

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
	 * Instantiate.
	 * 
	 * @param supplierName
	 *            Name of the {@link OfficeFloorSupplier}.
	 * @param supplierSourceClassName
	 *            {@link SupplierSource} {@link Class} name.
	 * @param officeFloorNode
	 *            {@link OfficeFloorNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SupplierNodeImpl(String supplierName, String supplierSourceClassName, OfficeFloorNode officeFloorNode,
			NodeContext context) {
		this.supplierName = supplierName;
		this.supplierSourceClassName = supplierSourceClassName;
		this.officeFloorNode = officeFloorNode;
		this.context = context;

		// Create the additional objects
		this.propertyList = this.context.createPropertyList();
	}

	/*
	 * =============== Node ======================
	 */

	@Override
	public String getNodeName() {
		return this.supplierName;
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
		return this.officeFloorNode;
	}

	@Override
	public Node[] getChildNodes() {
		return this.suppliedManagedObjects.toArray(new Node[this.suppliedManagedObjects.size()]);
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
	public OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName, String type) {
		return this.addManagedObjectSource(managedObjectSourceName, type, null);
	}

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName, String type,
			String qualifier) {

		// Create the supplied managed object node
		SuppliedManagedObjectSourceNode suppliedManagedObjectNode = this.context
				.createSuppliedManagedObjectNode(qualifier, type, this);

		// Register the supplied managed object
		this.suppliedManagedObjects.add(suppliedManagedObjectNode);

		// Add and return the managed object source
		return this.officeFloorNode.addManagedObjectSource(managedObjectSourceName, suppliedManagedObjectNode);
	}

	/*
	 * =================== SupplierNode =========================
	 */

	@Override
	public OfficeFloorNode getOfficeFloorNode() {
		return this.officeFloorNode;
	}

	@Override
	public SupplierType loadSupplierType() {

		// Obtain the supplier source class
		Class<? extends SupplierSource> supplierSourceClass = this.context
				.getSupplierSourceClass(this.supplierSourceClassName, this);
		if (supplierSourceClass == null) {
			return null; // must obtain class
		}

		// Load and return the type
		SupplierLoader loader = this.context.getSupplierLoader(this);
		return loader.loadSupplierType(supplierSourceClass, this.propertyList);
	}

}