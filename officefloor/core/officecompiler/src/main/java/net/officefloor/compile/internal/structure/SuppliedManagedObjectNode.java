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

import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Node for the supplied {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectNode extends Node {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Supplied Managed Object";

	/**
	 * Initialises the {@link SuppliedManagedObjectNode}.
	 */
	void initialise();

	/**
	 * Loads the {@link SuppliedManagedObject}.
	 * 
	 * @return {@link SuppliedManagedObject}. May be <code>null</code> if issue
	 *         in loading the {@link SuppliedManagedObject}.
	 */
	SuppliedManagedObject<?, ?> loadSuppliedManagedObject();

	/**
	 * Obtains the {@link SupplierNode} containing this
	 * {@link SuppliedManagedObject}.
	 * 
	 * @return Parent {@link SupplierNode}.
	 */
	SupplierNode getSupplierNode();

}