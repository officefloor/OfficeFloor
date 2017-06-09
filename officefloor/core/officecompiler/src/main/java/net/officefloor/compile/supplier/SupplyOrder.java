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
package net.officefloor.compile.supplier;

import net.officefloor.frame.internal.structure.Flow;

/**
 * Order for a {@link SuppliedManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplyOrder {

	/**
	 * Obtains the type of {@link Object} required.
	 * 
	 * @return Type of {@link Object} required.
	 */
	String getType();

	/**
	 * Obtains the qualifier for the {@link SuppliedManagedObject}.
	 * 
	 * @return Qualifier for the {@link SuppliedManagedObject}. May be
	 *         <code>null</code> if no qualifier.
	 */
	String getQualifier();

	/**
	 * <p>
	 * Invoked to fill this {@link SupplyOrder} with the
	 * {@link SuppliedManagedObject}.
	 * <p>
	 * Should this order not be able to be filled, then this method will not be
	 * invoked.
	 * 
	 * @param <D>
	 *            Dependency type keys.
	 * @param <F>
	 *            {@link Flow} type keys.
	 * @param suppliedManagedObject
	 *            {@link SuppliedManagedObject}.
	 */
	<D extends Enum<D>, F extends Enum<F>> void fillOrder(SuppliedManagedObject<D, F> suppliedManagedObject);

}