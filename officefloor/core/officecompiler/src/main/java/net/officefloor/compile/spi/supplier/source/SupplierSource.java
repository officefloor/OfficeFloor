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
package net.officefloor.compile.spi.supplier.source;

import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Supplies {@link ManagedObjectSource} instances.
 * <p>
 * This allows for plugging in object libraries.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierSource {

	/**
	 * <p>
	 * Obtains the {@link SupplierSourceSpecification} for this
	 * {@link SupplierSource}.
	 * <p>
	 * This enables the {@link SupplierSourceContext} to be populated with the
	 * necessary details as per this {@link SupplierSourceSpecification} in
	 * loading the {@link SupplierType}.
	 * 
	 * @return {@link SupplierSourceSpecification}.
	 */
	SupplierSourceSpecification getSpecification();

	/**
	 * Supplies the necessary {@link ManagedObjectSource} instances.
	 * 
	 * @param context
	 *            {@link SupplierSourceContext}.
	 * @throws Exception
	 *             If fails to provide supply of {@link ManagedObjectSource}
	 *             instances.
	 */
	void supply(SupplierSourceContext context) throws Exception;

}