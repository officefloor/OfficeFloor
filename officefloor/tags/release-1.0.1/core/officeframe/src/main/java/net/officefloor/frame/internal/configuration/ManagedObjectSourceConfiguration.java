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

package net.officefloor.frame.internal.configuration;

import java.util.Properties;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Configuration of a {@link ManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceConfiguration<F extends Enum<F>, MS extends ManagedObjectSource<?, F>> {

	/**
	 * Obtains the name of this {@link ManagedObjectSource}.
	 *
	 * @return Name of this {@link ManagedObjectSource}.
	 */
	String getManagedObjectSourceName();

	/**
	 * Obtains the {@link Class} of the {@link ManagedObjectSource}.
	 *
	 * @return {@link Class} of the {@link ManagedObjectSource}.
	 */
	Class<MS> getManagedObjectSourceClass();

	/**
	 * Obtains the properties to initialise the {@link ManagedObjectSource}.
	 *
	 * @return Properties to initialise the {@link ManagedObjectSource}.
	 */
	Properties getProperties();

	/**
	 * Obtains the {@link ManagingOfficeConfiguration} detailing the
	 * {@link Office} responsible for managing this {@link ManagedObjectSource}.
	 *
	 * @return {@link ManagingOfficeConfiguration}.
	 */
	ManagingOfficeConfiguration<F> getManagingOfficeConfiguration();

	/**
	 * Obtains the {@link ManagedObjectPool} for this
	 * {@link ManagedObjectSource}.
	 *
	 * @return {@link ManagedObjectPool} for this {@link ManagedObjectSource} or
	 *         <code>null</code> if not to be pooled.
	 */
	ManagedObjectPool getManagedObjectPool();

	/**
	 * Obtains the timeout to:
	 * <ol>
	 * <li>to source the {@link ManagedObject}</li>
	 * <li>have asynchronous operations on the {@link ManagedObject} complete</li>
	 * </ol>
	 *
	 * @return Timeout.
	 */
	long getTimeout();

}