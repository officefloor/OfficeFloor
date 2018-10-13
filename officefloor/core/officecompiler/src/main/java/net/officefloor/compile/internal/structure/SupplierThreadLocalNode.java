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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.officefloor.OfficeFloorSupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;

/**
 * Node for a {@link SupplierThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierThreadLocalNode extends LinkObjectNode, OfficeFloorSupplierThreadLocal {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Supplier Thread Local";

	/**
	 * Initialises the {@link SupplierThreadLocalNode}.
	 * 
	 * @param optionalThreadLocalReceiver {@link OptionalThreadLocalReceiver}.
	 */
	void initialise(OptionalThreadLocalReceiver optionalThreadLocalReceiver);

	/**
	 * Obtains the {@link SupplierNode} containing this
	 * {@link SupplierThreadLocalNode}.
	 * 
	 * @return Parent {@link SupplierNode}.
	 */
	SupplierNode getSupplierNode();

	/**
	 * Builds the {@link SupplierThreadLocal}.
	 * 
	 * @param context {@link CompileContext}.
	 */
	void buildSupplierThreadLocal(CompileContext context);

}