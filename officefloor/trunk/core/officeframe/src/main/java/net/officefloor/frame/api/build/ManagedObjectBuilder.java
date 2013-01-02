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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Builder of a {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectBuilder<F extends Enum<F>> {

	/**
	 * Specifies a property for the {@link ManagedObjectSource}.
	 *
	 * @param name
	 *            Name of property.
	 * @param value
	 *            Value of property.
	 */
	void addProperty(String name, String value);

	/**
	 * Specifies the {@link ManagedObjectPool} for this {@link ManagedObject}.
	 *
	 * @param pool
	 *            {@link ManagedObjectPool} for this {@link ManagedObject}.
	 */
	void setManagedObjectPool(ManagedObjectPool pool);

	/**
	 * Specifies the timeout to:
	 * <ol>
	 * <li>to source the {@link ManagedObject}</li>
	 * <li>have asynchronous operations on the {@link ManagedObject} complete</li>
	 * </ol>
	 *
	 * @param timeout
	 *            Timeout.
	 */
	void setTimeout(long timeout);

	/**
	 * Specifies the {@link Office} to manage this {@link ManagedObject}.
	 *
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @return {@link ManagingOfficeBuilder}.
	 */
	ManagingOfficeBuilder<F> setManagingOffice(String officeName);

}