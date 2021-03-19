/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Node for the supplied {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectSourceNode extends Node {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Supplied Managed Object Source";

	/**
	 * Initialises the {@link SuppliedManagedObjectSourceNode}.
	 */
	void initialise();

	/**
	 * Loads the {@link SuppliedManagedObjectSourceType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link SuppliedManagedObjectSourceType}. May be <code>null</code> if
	 *         issue in loading the {@link SuppliedManagedObjectSourceType}.
	 */
	SuppliedManagedObjectSourceType loadSuppliedManagedObjectSourceType(CompileContext compileContext);

	/**
	 * Obtains the {@link SupplierNode} containing this
	 * {@link SuppliedManagedObjectSource}.
	 * 
	 * @return Parent {@link SupplierNode}.
	 */
	SupplierNode getSupplierNode();

}
