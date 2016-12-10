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
package net.officefloor.compile.internal.structure;

import net.officefloor.autowire.supplier.SupplyOrder;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;

/**
 * Supplier {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierNode extends Node, OfficeFloorSupplier {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Supplier";

	/**
	 * Initialises the {@link SupplierNode}.
	 */
	void initialise();

	/**
	 * Fill {@link SupplyOrder} instances.
	 * 
	 * @param supplyOrders
	 *            {@link SupplyOrder} instances to fill.
	 */
	void fillSupplyOrders(SupplyOrder... supplyOrders);

	/**
	 * Obtains the parent {@link OfficeFloorNode}.
	 * 
	 * @return Parent {@link OfficeFloorNode}.
	 */
	OfficeFloorNode getOfficeFloorNode();

}