/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Builder of a {@link ManagedObject}.
 * 
 * @author Daniel
 */
public interface ManagedObjectBuilder<H extends Enum<H>> {

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
	 * Specifies the default timeout for asynchronous operations on the
	 * {@link ManagedObject}.
	 * 
	 * @param timeout
	 *            Default timeout.
	 */
	void setDefaultTimeout(long timeout);

	/**
	 * Specifies the {@link Office} to manage this {@link ManagedObject}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 */
	void setManagingOffice(String officeName);

	/**
	 * Obtains the {@link ManagedObjectHandlerBuilder}.
	 * 
	 * @return {@link ManagedObjectHandlerBuilder}.
	 */
	ManagedObjectHandlerBuilder<H> getManagedObjectHandlerBuilder();

}
