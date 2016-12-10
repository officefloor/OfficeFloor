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
package net.officefloor.autowire.supplier;

import net.officefloor.autowire.AutoWire;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Order for a {@link SuppliedManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplyOrder {

	/**
	 * <p>
	 * Obtains the {@link AutoWire} to identify the {@link ManagedObject} to be
	 * supplied.
	 * <p>
	 * This must match exactly the {@link AutoWire} for the
	 * {@link SuppliedManagedObject}.
	 * 
	 * @return {@link AutoWire} to identify the {@link ManagedObject} to be
	 *         supplied.
	 */
	AutoWire getAutoWire();

	/**
	 * <p>
	 * Invoked to fill this {@link SupplyOrder} with the
	 * {@link SuppliedManagedObject}.
	 * <p>
	 * Should this order not be able to be filled (i.e. no
	 * {@link SuppliedManagedObject} matching the {@link AutoWire}) then this
	 * method will not be invoked.
	 * 
	 * @param <D>
	 *            Dependency type keys.
	 * @param <F>
	 *            {@link JobSequence} type keys.
	 * @param suppliedManagedObject
	 *            {@link SuppliedManagedObject}.
	 */
	<D extends Enum<D>, F extends Enum<F>> void fillOrder(
			SuppliedManagedObject<D, F> suppliedManagedObject);

}